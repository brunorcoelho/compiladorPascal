package compilador;

public class Parser {
    private ScannerLexico lexer;
    private TokenInformacoes tokenAtual;
    private TabelaSimbolos tabela;
    private Token tipoAtual;
    private java.util.List<String> variaveisTemp = new java.util.ArrayList<>();

    public Parser(ScannerLexico lexer) {
        this.lexer = lexer;
        this.tokenAtual = lexer.proximoToken();
        this.tabela = new TabelaSimbolos();
    }

    public TabelaSimbolos getTabela() {
        return tabela;
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

    public void parse() {
        programa();
        System.out.println("Análise sintática concluida com sucesso!");
    }

    private void programa() {
        consumir(Token.PROGRAM);
        consumir(Token.IDENT);
        corpo();
        consumir(Token.PONTO);
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
            dc_p();
            mais_dc_p();
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

        // Guarda o nome do procedimento
        String nomeProcedimento = tokenAtual.getLexema();
        consumir(Token.IDENT);

        // Adiciona procedimento à tabela (no escopo global)
        Simbolo proc = new Simbolo(nomeProcedimento, null, Simbolo.Categoria.PROCEDIMENTO, "global");
        tabela.adicionar(proc);

        // Entra no escopo do procedimento
        tabela.entrarEscopo(nomeProcedimento);

        parametros();
        corpo_p();

        // Sai do escopo do procedimento
        tabela.sairEscopo();
    }

    private void mais_dc_p() {
        if (verificar(Token.PONTO_VIRGULA)) {
            consumir(Token.PONTO_VIRGULA);
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
        variaveisTemp.clear(); // Limpa lista
        variaveis();
        consumir(Token.DOIS_PONTOS);
        tipo_var();

        // Adiciona parâmetros à tabela (como PARAMETRO, não VARIAVEL)
        for (String nomeParam : variaveisTemp) {
            Simbolo s = new Simbolo(nomeParam, tipoAtual, Simbolo.Categoria.PARAMETRO, tabela.getEscopoAtual());
            tabela.adicionar(s);
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
        consumir(Token.BEGIN);
        comandos();
        consumir(Token.END);
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
        variaveisTemp.clear(); // Limpa a lista temporária
        variaveis();
        consumir(Token.DOIS_PONTOS);
        tipo_var();

        // Agora adiciona todas as variáveis com o tipo correto
        for (String nomeVar : variaveisTemp) {
            Simbolo s = new Simbolo(nomeVar, tipoAtual, Simbolo.Categoria.VARIAVEL, tabela.getEscopoAtual());
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
            consumir(Token.IDENT);
            consumir(Token.FECHA_PAREN);
            consumir(Token.PONTO_VIRGULA);
        } else if (verificar(Token.WRITE)) {
            consumir(Token.WRITE);
            consumir(Token.ABRE_PAREN);
            consumir(Token.IDENT);
            consumir(Token.FECHA_PAREN);
            consumir(Token.PONTO_VIRGULA);
        } else if (verificar(Token.IF)) {
            consumir(Token.IF);
            condicao();
            consumir(Token.THEN);
            comandos();
            pfalsa();
            consumir(Token.DOLAR);
        } else if (verificar(Token.WHILE)) {
            consumir(Token.WHILE);
            condicao();
            consumir(Token.DO);
            comandos();
            consumir(Token.DOLAR);
        } else if (verificar(Token.IDENT)) {
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
        } else {
            lista_arg();
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
        consumir(Token.IDENT);
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
    }

    // <relacao> -> = | <> | >= | <= | > | <
    private void relacao() {
        if (verificar(Token.IGUAL)) {
            consumir(Token.IGUAL);
        } else if (verificar(Token.DIFERENTE)) {
            consumir(Token.DIFERENTE);
        } else if (verificar(Token.MAIOR_IGUAL)) {
            consumir(Token.MAIOR_IGUAL);
        } else if (verificar(Token.MENOR_IGUAL)) {
            consumir(Token.MENOR_IGUAL);
        } else if (verificar(Token.MAIOR)) {
            consumir(Token.MAIOR);
        } else if (verificar(Token.MENOR)) {
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
            consumir(Token.IDENT);
        } else if (verificar(Token.NUMERO_REAL)) {
            consumir(Token.NUMERO_REAL);
        } else if (verificar(Token.NUMERO_INT)) {
            consumir(Token.NUMERO_INT);
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
            op_ad();
            termo();
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
            op_mul();
            fator();
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
