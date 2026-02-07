package compilador;

public class Parser {
    private ScannerLexico lexer;
    private TokenInformacoes tokenAtual;
    private TabelaSimbolos tabela;
    private Token tipoAtual;
    private java.util.List<String> variaveisTemp = new java.util.ArrayList<>();
    private java.util.List<Integer> indicesDsviProcs = new java.util.ArrayList<>();
    private GeradorCodigo gerador;
    private String identAtual;
    private String operadorRelacional;
    private int enderecoProc;
    private int numParametros;
    private int numLocais;
    private java.util.List<Integer> enderecosParametros = new java.util.ArrayList<>();

    public Parser(ScannerLexico lexer) {
        this.lexer = lexer;
        this.tokenAtual = lexer.proximoToken();
        this.tabela = new TabelaSimbolos();
        this.gerador = new GeradorCodigo();
    }

    public TabelaSimbolos getTabela() {
        return tabela;
    }

    public GeradorCodigo getGerador() {
        return gerador;
    }

    private void avancar() {
        tokenAtual = lexer.proximoToken();
    }

    private boolean verificar(Token tipo) {
        return tokenAtual.getTipo() == tipo;
    }

    private void consumir(Token tipoEsperado) {
        if (verificar(tipoEsperado)) {
            avancar();
        } else {
            erro("Esperado '" + tipoEsperado + "', encontrado '" +
                    tokenAtual.getTipo() + "' ('" + tokenAtual.getLexema() + "')");
        }
    }

    private void erro(String mensagem) {
        throw new RuntimeException("Erro de sintaxe na linha " + tokenAtual.getLinha() + ": " + mensagem);
    }

    private void erroSemantico(String mensagem) {
        throw new RuntimeException("Erro semântico na linha " + tokenAtual.getLinha() + ": " + mensagem);
    }

    public void parse() {
        programa();
        System.out.println("Análise sintática concluida com sucesso!");
    }

    private void programa() {
        gerador.emitir("INPP");
        consumir(Token.PROGRAM);
        consumir(Token.IDENT);
        corpo();
        consumir(Token.PONTO);
        gerador.emitir("PARA");
    }

    private void corpo() {
        dc();
        consumir(Token.BEGIN);
        comandos();
        consumir(Token.END);
    }

    private void dc() {
        if (verificar(Token.VAR)) {
            dc_v();
            mais_dc();
        } else if (verificar(Token.PROCEDURE)) {
            // Limpa lista e adiciona primeiro DSVI
            indicesDsviProcs.clear();
            indicesDsviProcs.add(gerador.getProximoIndice());
            gerador.emitir("DSVI", 0);
            dc_p();
            mais_dc_p();
            // Preenche TODOS os DSVIs com o endereço atual
            int enderecoFinal = gerador.getProximoIndice();
            for (int indice : indicesDsviProcs) {
                gerador.alterarArgumento(indice, enderecoFinal);
            }
        }
    }

    private void mais_dc() {
        if (verificar(Token.PONTO_VIRGULA)) {
            consumir(Token.PONTO_VIRGULA);
            dc();
        }
    }

    private void dc_p() {
        consumir(Token.PROCEDURE);
        String nomeProcedimento = tokenAtual.getLexema();
        consumir(Token.IDENT);
        if (tabela.buscar(nomeProcedimento) != null) {
            erroSemantico("Procedimento '" + nomeProcedimento + "' já declarado");
        }

        // Guarda o endereço onde o procedimento começa (APÓS o DSVI que será emitido no
        // dc())
        enderecoProc = gerador.getProximoIndice();
        numParametros = 0;
        numLocais = 0;
        Simbolo proc = new Simbolo(nomeProcedimento, null, Simbolo.Categoria.PROCEDIMENTO, "global", enderecoProc);
        tabela.adicionar(proc);
        tabela.entrarEscopo(nomeProcedimento);
        enderecosParametros.clear();
        parametros();
        corpo_p();
        tabela.sairEscopo();
    }

    private void mais_dc_p() {
        if (verificar(Token.PONTO_VIRGULA)) {
            consumir(Token.PONTO_VIRGULA);
            // DSVI extra - adiciona à lista para preencher depois
            indicesDsviProcs.add(gerador.getProximoIndice());
            gerador.emitir("DSVI", 0);
            dc_p();
            mais_dc_p();
        }
    }

    private void parametros() {
        if (verificar(Token.ABRE_PAREN)) {
            consumir(Token.ABRE_PAREN);
            lista_par();
            consumir(Token.FECHA_PAREN);
        }
    }

    // <lista_par> -> <variaveis> : <tipo_var> <mais_par>
    private void lista_par() {
        variaveisTemp.clear();
        variaveis();
        consumir(Token.DOIS_PONTOS);
        tipo_var();
        // Adiciona parâmetros à tabela (NÃO emite ALME - parâmetros vêm pela pilha)
        for (String nomeParam : variaveisTemp) {
            int endereco = gerador.alocarMemoria();
            // Parâmetros não emitem ALME aqui
            Simbolo s = new Simbolo(nomeParam, tipoAtual, Simbolo.Categoria.PARAMETRO, tabela.getEscopoAtual(),
                    endereco);
            tabela.adicionar(s);
            numParametros++;
            enderecosParametros.add(endereco);
        }
        mais_par();
    }

    private void mais_par() {
        if (verificar(Token.PONTO_VIRGULA)) {
            consumir(Token.PONTO_VIRGULA);
            lista_par();
        }
    }

    private void corpo_p() {
        dc_loc();

        // Desempilha parâmetros da pilha para memória (ordem reversa)
        for (int i = enderecosParametros.size() - 1; i >= 0; i--) {
            gerador.emitir("ARMZ", enderecosParametros.get(i));
        }

        consumir(Token.BEGIN);
        comandos();
        consumir(Token.END);

        // DESM: Desempilha parâmetros e variáveis locais
        // numParametros foi incrementado em lista_par
        gerador.emitir("DESM", numParametros + numLocais);
        gerador.emitir("RTPR");
    }

    private void dc_loc() {
        if (verificar(Token.VAR)) {
            dc_v();
            mais_dc();
        }
    }

    private void mais_dcloc() {
        if (verificar(Token.PONTO_VIRGULA)) {
            consumir(Token.PONTO_VIRGULA);
            dc_loc();
        }
    }

    // <dc_v> -> var <variaveis> : <tipo_var>
    private void dc_v() {
        consumir(Token.VAR);
        variaveisTemp.clear();
        variaveis();
        consumir(Token.DOIS_PONTOS);
        tipo_var();
        // Adiciona todas as variáveis com o tipo correto
        for (String nomeVar : variaveisTemp) {
            if (tabela.existeNoEscopoAtual(nomeVar)) {
                erroSemantico("Variável '" + nomeVar + "' já declarada no escopo atual");
            }
            int endereco = gerador.alocarMemoria();

            // Só emite ALME se for variável GLOBAL
            gerador.emitir("ALME", 1);
            if (!tabela.getEscopoAtual().equals("global")) {
                numLocais++;
            }
            Simbolo s = new Simbolo(nomeVar, tipoAtual, Simbolo.Categoria.VARIAVEL, tabela.getEscopoAtual(), endereco);
            tabela.adicionar(s);
        }
    }

    private void tipo_var() {
        if (verificar(Token.REAL)) {
            tipoAtual = Token.REAL;
            consumir(Token.REAL);
        } else if (verificar(Token.INTEGER)) {
            tipoAtual = Token.INTEGER;
            consumir(Token.INTEGER);
        } else {
            erro("Esperado 'real' ou 'integer'");
        }
    }

    private void variaveis() {
        String nomeVar = tokenAtual.getLexema();
        consumir(Token.IDENT);

        // Adiciona à lista temporária (tipo será definido em dc_v)
        variaveisTemp.add(nomeVar);

        mais_var();
    }

    private void mais_var() {
        if (verificar(Token.VIRGULA)) {
            consumir(Token.VIRGULA);
            variaveis();
        }
    }

    private void comandos() {
        comando();
        mais_comandos();
    }

    private void mais_comandos() {
        if (verificar(Token.READ) || verificar(Token.WRITE) || verificar(Token.IF) || verificar(Token.WHILE)
                || verificar(Token.IDENT)) {
            comandos();
        }
    }

    private void comando() {
        if (verificar(Token.READ)) {
            consumir(Token.READ);
            consumir(Token.ABRE_PAREN);
            String nomeVar = tokenAtual.getLexema();
            Simbolo s = tabela.buscar(nomeVar);
            if (s == null) {
                erroSemantico("Variável '" + nomeVar + "' não declarada");
            }
            consumir(Token.IDENT);
            consumir(Token.FECHA_PAREN);
            consumir(Token.PONTO_VIRGULA);

            // Gera código: LEIT + ARMZ
            gerador.emitir("LEIT");
            gerador.emitir("ARMZ", s.getEndereco());
        } else if (verificar(Token.WRITE)) {
            consumir(Token.WRITE);
            consumir(Token.ABRE_PAREN);
            String nomeVar = tokenAtual.getLexema();
            Simbolo s = tabela.buscar(nomeVar);
            if (s == null) {
                erroSemantico("Variável '" + nomeVar + "' não declarada");
            }
            consumir(Token.IDENT);
            consumir(Token.FECHA_PAREN);
            consumir(Token.PONTO_VIRGULA);

            // Gera código: CRVL + IMPR
            gerador.emitir("CRVL", s.getEndereco());
            gerador.emitir("IMPR");
        } else if (verificar(Token.IF)) {
            consumir(Token.IF);
            condicao();
            consumir(Token.THEN);

            // DSVF: Se condição falsa, pula para else ou fim
            int indiceDsvf = gerador.getProximoIndice();
            gerador.emitir("DSVF", 0); // Endereço temporário

            comandos();

            // DSVI: Pula o else (após then)
            int indiceDsvi = gerador.getProximoIndice();
            gerador.emitir("DSVI", 0); // Endereço temporário

            // Agora sabemos onde começa o else
            gerador.alterarArgumento(indiceDsvf, gerador.getProximoIndice());

            pfalsa();

            // Agora sabemos onde termina o if
            gerador.alterarArgumento(indiceDsvi, gerador.getProximoIndice());

            consumir(Token.DOLAR);
        } else if (verificar(Token.WHILE)) {
            consumir(Token.WHILE);

            // Marca o início do loop (para voltar aqui)
            int inicioWhile = gerador.getProximoIndice();

            condicao();
            consumir(Token.DO);

            // DSVF: Se condição falsa, sai do loop
            int indiceDsvf = gerador.getProximoIndice();
            gerador.emitir("DSVF", 0); // Endereço temporário

            comandos();

            // DSVI: Volta para o início do loop
            gerador.emitir("DSVI", inicioWhile);

            // Preenche o DSVF com o endereço após o loop
            gerador.alterarArgumento(indiceDsvf, gerador.getProximoIndice());

            consumir(Token.DOLAR);
        } else if (verificar(Token.IDENT)) {
            identAtual = tokenAtual.getLexema(); // Guarda para usar depois
            Simbolo s = tabela.buscar(identAtual);
            if (s == null) {
                erroSemantico("'" + identAtual + "' não declarado");
            }
            consumir(Token.IDENT);
            restoIdent();
            consumir(Token.PONTO_VIRGULA);
        } else {
            erro("Comando inválido");
        }
    }

    private void pfalsa() {
        if (verificar(Token.ELSE)) {
            consumir(Token.ELSE);
            comandos();
        }
    }

    private void restoIdent() {
        if (verificar(Token.ATRIB)) {
            consumir(Token.ATRIB);
            expressao();
            Simbolo s = tabela.buscar(identAtual);
            gerador.emitir("ARMZ", s.getEndereco());
        } else {
            // É uma chamada de procedimento
            Simbolo proc = tabela.buscar(identAtual);

            // PUSHER: guarda endereço de retorno (será preenchido depois)
            int indicePusher = gerador.getProximoIndice();
            gerador.emitir("PUSHER", 0); // Temporário

            lista_arg();

            // Preenche PUSHER com endereço após CHPR
            gerador.alterarArgumento(indicePusher, gerador.getProximoIndice() + 1);

            // CHPR: chama o procedimento
            gerador.emitir("CHPR", proc.getEndereco());
        }
    }

    private void lista_arg() {
        if (verificar(Token.ABRE_PAREN)) {
            consumir(Token.ABRE_PAREN);
            argumentos();
            consumir(Token.FECHA_PAREN);
        }
    }

    private void argumentos() {
        String nomeArg = tokenAtual.getLexema();
        Simbolo s = tabela.buscar(nomeArg);
        consumir(Token.IDENT);

        // PARAM: passa o endereço da variável
        gerador.emitir("PARAM", s.getEndereco());

        mais_ident();
    }

    private void mais_ident() {
        if (verificar(Token.VIRGULA)) {
            consumir(Token.VIRGULA);
            argumentos();
        }
    }

    private void condicao() {
        expressao();
        relacao();
        expressao();
        gerador.emitir(operadorRelacional);
    }

    // <relacao> -> = | <> | >= | <= | > | <
    private void relacao() {
        if (verificar(Token.IGUAL)) {
            operadorRelacional = "CMIG";
            consumir(Token.IGUAL);
        } else if (verificar(Token.DIFERENTE)) {
            operadorRelacional = "CMDG";
            consumir(Token.DIFERENTE);
        } else if (verificar(Token.MAIOR_IGUAL)) {
            operadorRelacional = "CMAI";
            consumir(Token.MAIOR_IGUAL);
        } else if (verificar(Token.MENOR_IGUAL)) {
            operadorRelacional = "CPMI";
            consumir(Token.MENOR_IGUAL);
        } else if (verificar(Token.MAIOR)) {
            operadorRelacional = "CMMA";
            consumir(Token.MAIOR);
        } else if (verificar(Token.MENOR)) {
            operadorRelacional = "CMME";
            consumir(Token.MENOR);
        } else {
            erro("Operador relacional esperado");
        }
    }

    // <expressao> -> <termo> <outros_termos>
    private void expressao() {
        termo();
        outros_termos();
    }

    // <termo> -> <op_un> <fator> <mais_fatores>
    private void termo() {
        op_un();
        fator();
        mais_fatores();
    }

    // <op_un> -> - | λ
    private void op_un() {
        if (verificar(Token.MENOS)) {
            consumir(Token.MENOS);
        }
    }

    // <fator> -> ident | numero_real | numero_int | (<expressao>)
    private void fator() {
        if (verificar(Token.IDENT)) {
            String nomeVar = tokenAtual.getLexema();
            Simbolo s = tabela.buscar(nomeVar);
            if (s == null) {
                erroSemantico("Variável '" + nomeVar + "' não declarada");
            }
            consumir(Token.IDENT);

            // Gera CRVL para carregar valor da variável
            gerador.emitir("CRVL", s.getEndereco());

        } else if (verificar(Token.NUMERO_REAL)) {
            String valor = tokenAtual.getLexema();
            consumir(Token.NUMERO_REAL);

            // Gera CRCT para carregar constante
            gerador.emitir("CRCT", valor);

        } else if (verificar(Token.NUMERO_INT)) {
            String valor = tokenAtual.getLexema();
            consumir(Token.NUMERO_INT);

            // Gera CRCT para carregar constante
            gerador.emitir("CRCT", valor);

        } else if (verificar(Token.ABRE_PAREN)) {
            consumir(Token.ABRE_PAREN);
            expressao();
            consumir(Token.FECHA_PAREN);
        } else {
            erro("Fator esperado");
        }
    }

    // <outros_termos> -> <op_ad> <termo> <outros_termos> | λ
    private void outros_termos() {
        if (verificar(Token.MAIS) || verificar(Token.MENOS)) {
            boolean soma = verificar(Token.MAIS); // Guarda qual operador
            op_ad();
            termo();

            // Gera código da operação DEPOIS do termo
            if (soma) {
                gerador.emitir("SOMA");
            } else {
                gerador.emitir("SUBT");
            }

            outros_termos();
        }
    }

    // <op_ad> -> + | -
    private void op_ad() {
        if (verificar(Token.MAIS)) {
            consumir(Token.MAIS);
        } else {
            consumir(Token.MENOS);
        }
    }

    // <mais_fatores> -> <op_mul> <fator> <mais_fatores> | λ
    private void mais_fatores() {
        if (verificar(Token.MULT) || verificar(Token.DIV)) {
            boolean mult = verificar(Token.MULT); // Guarda qual operador
            op_mul();
            fator();

            // Gera código da operação DEPOIS do fator
            if (mult) {
                gerador.emitir("MULT");
            } else {
                gerador.emitir("DIVI");
            }

            mais_fatores();
        }
    }

    // <op_mul> -> * | /
    private void op_mul() {
        if (verificar(Token.MULT)) {
            consumir(Token.MULT);
        } else {
            consumir(Token.DIV);
        }
    }
}
