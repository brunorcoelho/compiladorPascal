# 📚 DOCUMENTAÇÃO COMPLETA - COMPILADOR LALG

## Índice

1. [Visão Geral](#visão-geral)
2. [Arquitetura do Compilador](#arquitetura-do-compilador)
3. [Análise Léxica](#análise-léxica)
4. [Análise Sintática](#análise-sintática)
5. [Análise Semântica](#análise-semântica)
6. [Geração de Código](#geração-de-código)
7. [Máquina Virtual](#máquina-virtual)
8. [Fluxo de Execução](#fluxo-de-execução)

---

# Visão Geral

Este projeto implementa um **compilador completo** para a linguagem **LALG** (uma linguagem simplificada baseada em Pascal), incluindo todas as fases de compilação e uma máquina virtual para executar o código gerado.

## Fases do Compilador

```
Código Fonte (Pascal)
        ↓
   ┌─────────────────┐
   │ Análise Léxica  │  → Transforma texto em TOKENS
   └─────────────────┘
        ↓
   ┌─────────────────┐
   │Análise Sintática│  → Verifica estrutura gramatical
   └─────────────────┘
        ↓
   ┌─────────────────┐
   │Análise Semântica│  → Verifica tipos e declarações
   └─────────────────┘
        ↓
   ┌─────────────────┐
   │Geração de Código│  → Gera instruções da VM
   └─────────────────┘
        ↓
   ┌─────────────────┐
   │ Máquina Virtual │  → Executa o código
   └─────────────────┘
```

## Arquivos do Projeto

| Arquivo | Fase | Descrição |
|---------|------|-----------|
| `Token.java` | Léxica | Enum com todos os tipos de token |
| `TokenInformacoes.java` | Léxica | Classe que representa um token |
| `ScannerLexico.java` | Léxica | Analisador léxico |
| `Parser.java` | Sintática/Semântica/Geração | Analisador sintático descendente recursivo |
| `Simbolo.java` | Semântica | Representa uma entrada na tabela de símbolos |
| `TabelaSimbolos.java` | Semântica | Gerencia símbolos (variáveis, procedimentos) |
| `Instrucao.java` | Geração | Representa uma instrução da VM |
| `GeradorCodigo.java` | Geração | Gera e gerencia o código objeto |
| `MaquinaVirtual.java` | Execução | Interpreta e executa o código |
| `Main.java` | - | Ponto de entrada, integra todas as fases |

---

# Análise Léxica

A análise léxica é a **primeira fase** do compilador. Ela lê o código fonte caractere por caractere e agrupa em **tokens** (unidades léxicas).

## Token.java

### O que é?

É uma **enumeração (enum)** que define todos os tipos de tokens que a linguagem LALG reconhece.

### Código

```java
public enum Token {
    // Palavras reservadas
    PROGRAM,    // program
    BEGIN,      // begin
    END,        // end
    IF,         // if
    THEN,       // then
    ELSE,       // else
    WHILE,      // while
    DO,         // do
    PROCEDURE,  // procedure
    VAR,        // var
    READ,       // read
    WRITE,      // write
    REAL,       // real
    INTEGER,    // integer
    
    // Operadores aritméticos
    MAIS,       // +
    MENOS,      // -
    MULT,       // *
    DIV,        // /
    
    // Operadores relacionais
    IGUAL,      // =
    DIFERENTE,  // <>
    MENOR,      // <
    MAIOR,      // >
    MENOR_IGUAL,// <=
    MAIOR_IGUAL,// >=
    
    // Delimitadores e outros
    ATRIB,      // :=
    PONTO,      // .
    VIRGULA,    // ,
    PONTO_VIRGULA, // ;
    DOIS_PONTOS,   // :
    ABRE_PAREN,    // (
    FECHA_PAREN,   // )
    DOLAR,         // $ (marca fim de bloco)
    
    // Identificadores e literais
    IDENT,       // nomes de variáveis/procedimentos
    NUMERO_INT,  // números inteiros (ex: 42)
    NUMERO_REAL, // números reais (ex: 3.14)
    
    // Fim de arquivo
    EOF
}
```

### Por que usar enum?

1. **Segurança de tipo**: Só valores válidos são aceitos
2. **Legibilidade**: Código fica mais claro
3. **Comparação fácil**: Usar `==` é seguro

---

## TokenInformacoes.java

### O que é?

É uma **classe** que encapsula todas as informações de um token encontrado:
- O **tipo** do token (enum Token)
- O **lexema** (texto original)
- A **linha** onde foi encontrado

### Código e Explicação

```java
public class TokenInformacoes {
    private Token tipo;     // O tipo do token (ex: IDENT, PROGRAM, MAIS)
    private String lexema;  // O texto original (ex: "contador", "program", "+")
    private int linha;      // Número da linha no código fonte

    // Construtor: cria um novo token com todas as informações
    public TokenInformacoes(Token tipo, String lexema, int linha) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linha = linha;
    }

    // Getters: métodos para acessar os atributos
    public Token getTipo() { return tipo; }
    public String getLexema() { return lexema; }
    public int getLinha() { return linha; }

    // toString: representação textual para debug
    @Override
    public String toString() {
        return "Token[" + tipo + ", '" + lexema + "', linha " + linha + "]";
    }
}
```

### Exemplo de uso

```
Código fonte: "var x : integer;"

Tokens gerados:
Token[VAR, 'var', linha 1]
Token[IDENT, 'x', linha 1]
Token[DOIS_PONTOS, ':', linha 1]
Token[INTEGER, 'integer', linha 1]
Token[PONTO_VIRGULA, ';', linha 1]
```

---

## ScannerLexico.java

### O que é?

É o **analisador léxico** (também chamado de scanner ou lexer). Ele percorre o código fonte e identifica tokens.

### Atributos da Classe

```java
public class ScannerLexico {
    private String codigoFonte;     // O código fonte completo
    private int posicao;            // Posição atual no código
    private int linha;              // Linha atual (para mensagens de erro)
    private char caractereAtual;    // Caractere sendo analisado
    
    // Mapa de palavras reservadas (ex: "program" → Token.PROGRAM)
    private static final Map<String, Token> palavrasReservadas = new HashMap<>();
    
    // Bloco estático: inicializa as palavras reservadas
    static {
        palavrasReservadas.put("program", Token.PROGRAM);
        palavrasReservadas.put("begin", Token.BEGIN);
        // ... outras palavras
    }
}
```

### Construtor

```java
public ScannerLexico(String codigoFonte) {
    this.codigoFonte = codigoFonte;
    this.posicao = 0;
    this.linha = 1;
    // Pega o primeiro caractere (ou '\0' se vazio)
    this.caractereAtual = codigoFonte.length() > 0 ? codigoFonte.charAt(0) : '\0';
}
```

### Método `avancar()`

Move para o próximo caractere do código fonte.

```java
private void avancar() {
    posicao++;
    if (posicao < codigoFonte.length()) {
        caractereAtual = codigoFonte.charAt(posicao);
    } else {
        caractereAtual = '\0';  // Fim do arquivo
    }
}
```

### Método `proxCaractere()`

Olha o próximo caractere SEM avançar (lookahead).

```java
private char proxCaractere() {
    int proxPosicao = posicao + 1;
    if (proxPosicao < codigoFonte.length()) {
        return codigoFonte.charAt(proxPosicao);
    }
    return '\0';
}
```

### Método `pularEspacosEComentarios()`

Ignora espaços, tabs, quebras de linha e comentários.

```java
private void pularEspacosEComentarios() {
    while (caractereAtual != '\0') {
        // Espaços e tabs
        if (caractereAtual == ' ' || caractereAtual == '\t' || caractereAtual == '\r') {
            avancar();
        }
        // Quebra de linha (incrementa contador de linha)
        else if (caractereAtual == '\n') {
            linha++;
            avancar();
        }
        // Comentário estilo { ... }
        else if (caractereAtual == '{') {
            avancar();
            while (caractereAtual != '}' && caractereAtual != '\0') {
                if (caractereAtual == '\n') linha++;
                avancar();
            }
            avancar();  // Pula o '}'
        }
        // Comentário estilo /* ... */
        else if (caractereAtual == '/' && proxCaractere() == '*') {
            avancar();  // Pula '/'
            avancar();  // Pula '*'
            while (!(caractereAtual == '*' && proxCaractere() == '/') && caractereAtual != '\0') {
                if (caractereAtual == '\n') linha++;
                avancar();
            }
            avancar();  // Pula '*'
            avancar();  // Pula '/'
        }
        else {
            break;  // Encontrou algo que não é espaço/comentário
        }
    }
}
```

### Método `lerIdentificadorOuPalavraReservada()`

Lê um identificador (nome de variável) ou palavra reservada.

```java
private TokenInformacoes lerIdentificadorOuPalavraReservada() {
    StringBuilder lexema = new StringBuilder();

    // Enquanto for letra, dígito ou underline, continua lendo
    while (Character.isLetterOrDigit(caractereAtual) || caractereAtual == '_') {
        lexema.append(caractereAtual);
        avancar();
    }

    String palavra = lexema.toString();

    // Verifica se é palavra reservada (case insensitive)
    Token tipo = palavrasReservadas.get(palavra.toLowerCase());
    
    if (tipo != null) {
        // É palavra reservada
        return new TokenInformacoes(tipo, palavra, linha);
    } else {
        // É identificador (nome de variável)
        return new TokenInformacoes(Token.IDENT, palavra, linha);
    }
}
```

### Método `lerNumero()`

Lê números inteiros ou reais.

```java
private TokenInformacoes lerNumero() {
    StringBuilder lexema = new StringBuilder();
    
    // Parte inteira
    while (Character.isDigit(caractereAtual)) {
        lexema.append(caractereAtual);
        avancar();
    }
    
    // Verifica se tem parte decimal (número real)
    if (caractereAtual == '.' && Character.isDigit(proxCaractere())) {
        lexema.append(caractereAtual);  // Adiciona o ponto
        avancar();
        
        // Parte decimal
        while (Character.isDigit(caractereAtual)) {
            lexema.append(caractereAtual);
            avancar();
        }
        return new TokenInformacoes(Token.NUMERO_REAL, lexema.toString(), linha);
    }
    
    return new TokenInformacoes(Token.NUMERO_INT, lexema.toString(), linha);
}
```

### Método `lerOperadorOuDelimitador()`

Lê operadores e delimitadores (caracteres especiais).

```java
private TokenInformacoes lerOperadorOuDelimitador() {
    int linhaAtual = linha;
    
    switch (caractereAtual) {
        // Operadores aritméticos
        case '+':
            avancar();
            return new TokenInformacoes(Token.MAIS, "+", linhaAtual);
        case '-':
            avancar();
            return new TokenInformacoes(Token.MENOS, "-", linhaAtual);
        case '*':
            avancar();
            return new TokenInformacoes(Token.MULT, "*", linhaAtual);
        case '/':
            avancar();
            return new TokenInformacoes(Token.DIV, "/", linhaAtual);
            
        // Dois pontos pode ser : ou :=
        case ':':
            avancar();
            if (caractereAtual == '=') {
                avancar();
                return new TokenInformacoes(Token.ATRIB, ":=", linhaAtual);
            }
            return new TokenInformacoes(Token.DOIS_PONTOS, ":", linhaAtual);
            
        // Menor pode ser <, <= ou <>
        case '<':
            avancar();
            if (caractereAtual == '=') {
                avancar();
                return new TokenInformacoes(Token.MENOR_IGUAL, "<=", linhaAtual);
            } else if (caractereAtual == '>') {
                avancar();
                return new TokenInformacoes(Token.DIFERENTE, "<>", linhaAtual);
            }
            return new TokenInformacoes(Token.MENOR, "<", linhaAtual);
            
        // ... outros casos similares
    }
}
```

### Método `proximoToken()` - O Método Principal

Este é o método que coordena todo o processo de análise léxica.

```java
public TokenInformacoes proximoToken() {
    // Primeiro, pula espaços e comentários
    pularEspacosEComentarios();

    // Fim do arquivo
    if (caractereAtual == '\0') {
        return new TokenInformacoes(Token.EOF, "", linha);
    }

    // Se começa com letra, é identificador ou palavra reservada
    if (Character.isLetter(caractereAtual)) {
        return lerIdentificadorOuPalavraReservada();
    }

    // Se começa com dígito, é número
    if (Character.isDigit(caractereAtual)) {
        return lerNumero();
    }

    // Tenta ler operador ou delimitador
    TokenInformacoes token = lerOperadorOuDelimitador();
    if (token != null) {
        return token;
    }

    // Caractere não reconhecido - ERRO LÉXICO
    char charInvalido = caractereAtual;
    avancar();
    throw new RuntimeException(
        "Erro léxico na linha " + linha + ": caractere inválido '" + charInvalido + "'"
    );
}
```

---

# Análise Sintática

A análise sintática é a **segunda fase**. Ela verifica se os tokens formam uma estrutura gramatical válida.

## Parser.java - Estrutura Geral

### O que é?

É um **analisador sintático descendente recursivo**. Cada regra da gramática vira um método.

### Atributos

```java
public class Parser {
    private ScannerLexico lexer;     // O analisador léxico
    private TokenInformacoes tokenAtual; // Token sendo analisado
    private TabelaSimbolos tabela;   // Tabela de símbolos (semântica)
    private Token tipoAtual;         // Tipo atual sendo declarado
    private List<String> variaveisTemp; // Lista temporária de variáveis
    private List<Integer> indicesDsviProcs; // Índices dos DSVIs
    private GeradorCodigo gerador;   // Gerador de código
    private String identAtual;       // Identificador atual
    private String operadorRelacional; // Operador relacional atual
    private int enderecoProc;        // Endereço do procedimento
    private int numParametros;       // Número de parâmetros
    private int numLocais;           // Número de variáveis locais
}
```

### Construtor

```java
public Parser(ScannerLexico lexer) {
    this.lexer = lexer;
    this.tokenAtual = lexer.proximoToken();  // Pega o primeiro token
    this.tabela = new TabelaSimbolos();
    this.gerador = new GeradorCodigo();
}
```

### Métodos Auxiliares

#### `consumir(Token esperado)`
Verifica se o token atual é o esperado e avança.

```java
private void consumir(Token esperado) {
    if (tokenAtual.getTipo() == esperado) {
        tokenAtual = lexer.proximoToken();  // Avança para o próximo
    } else {
        erro("Esperado " + esperado + " mas encontrado " + tokenAtual.getTipo());
    }
}
```

#### `verificar(Token tipo)`
Verifica se o token atual é de determinado tipo SEM consumir.

```java
private boolean verificar(Token tipo) {
    return tokenAtual.getTipo() == tipo;
}
```

#### `erro(String mensagem)`
Lança exceção de erro sintático.

```java
private void erro(String mensagem) {
    throw new RuntimeException(
        "Erro sintático na linha " + tokenAtual.getLinha() + ": " + mensagem
    );
}
```

### Método `parse()` - Ponto de Entrada

```java
public void parse() {
    programa();
    System.out.println("Análise sintática concluída com sucesso!");
}
```

### Regra `programa()`

Corresponde à regra: `<programa> -> program ident <corpo> .`

```java
private void programa() {
    gerador.emitir("INPP");      // Inicia programa (VM)
    consumir(Token.PROGRAM);     // Espera "program"
    consumir(Token.IDENT);       // Espera nome do programa
    corpo();                     // Processa corpo
    consumir(Token.PONTO);       // Espera "."
    gerador.emitir("PARA");      // Para programa (VM)
}
```

### Regra `corpo()`

Corresponde à regra: `<corpo> -> <dc> begin <comandos> end`

```java
private void corpo() {
    dc();                    // Processa declarações
    consumir(Token.BEGIN);   // Espera "begin"
    comandos();              // Processa comandos
    consumir(Token.END);     // Espera "end"
}
```

### Regra `dc()` - Declarações

Processa declarações de variáveis e procedimentos.

```java
private void dc() {
    if (verificar(Token.VAR)) {
        dc_v();      // Declaração de variáveis
        mais_dc();   // Mais declarações
    } else if (verificar(Token.PROCEDURE)) {
        // Emite DSVI para pular os procedimentos
        indicesDsviProcs.clear();
        indicesDsviProcs.add(gerador.getProximoIndice());
        gerador.emitir("DSVI", 0);  // Será preenchido depois
        
        dc_p();      // Declaração de procedimento
        mais_dc_p(); // Mais procedimentos
        
        // Preenche todos os DSVIs com endereço após os procedimentos
        int enderecoFinal = gerador.getProximoIndice();
        for (int indice : indicesDsviProcs) {
            gerador.alterarArgumento(indice, enderecoFinal);
        }
    }
}
```

### Regra `dc_v()` - Declaração de Variáveis

```java
private void dc_v() {
    consumir(Token.VAR);        // Espera "var"
    variaveisTemp.clear();       // Limpa lista temporária
    variaveis();                 // Lê nomes das variáveis
    consumir(Token.DOIS_PONTOS); // Espera ":"
    tipo_var();                  // Lê o tipo (integer ou real)
    
    // Para cada variável declarada
    for (String nomeVar : variaveisTemp) {
        // Aloca memória
        int endereco = gerador.alocarMemoria();
        gerador.emitir("ALME", 1);
        
        // Conta locais se não for global
        if (!tabela.getEscopoAtual().equals("global")) {
            numLocais++;
        }
        
        // Adiciona à tabela de símbolos
        Simbolo s = new Simbolo(nomeVar, tipoAtual, 
            Simbolo.Categoria.VARIAVEL, tabela.getEscopoAtual(), endereco);
        tabela.adicionar(s);
    }
}
```

### Regra `dc_p()` - Declaração de Procedimento

```java
private void dc_p() {
    consumir(Token.PROCEDURE);
    String nomeProcedimento = tokenAtual.getLexema();
    consumir(Token.IDENT);
    
    // Verifica se já foi declarado
    if (tabela.buscar(nomeProcedimento) != null) {
        erroSemantico("Procedimento '" + nomeProcedimento + "' já declarado");
    }
    
    // Guarda endereço de início
    enderecoProc = gerador.getProximoIndice();
    numParametros = 0;
    numLocais = 0;
    
    // Adiciona à tabela
    Simbolo proc = new Simbolo(nomeProcedimento, null, 
        Simbolo.Categoria.PROCEDIMENTO, "global", enderecoProc);
    tabela.adicionar(proc);
    
    tabela.entrarEscopo(nomeProcedimento);  // Entra no escopo
    parametros();
    corpo_p();
    tabela.sairEscopo();  // Sai do escopo
}
```

### Regra `corpo_p()` - Corpo do Procedimento

```java
private void corpo_p() {
    dc_loc();                    // Declarações locais
    consumir(Token.BEGIN);
    comandos();
    consumir(Token.END);
    
    // Gera código de retorno
    gerador.emitir("DESM", numParametros + numLocais);
    gerador.emitir("RTPR");
}
```

### Regra `comando()` - Processa um Comando

```java
private void comando() {
    if (verificar(Token.READ)) {
        // Comando de leitura: read(variavel)
        consumir(Token.READ);
        consumir(Token.ABRE_PAREN);
        String nomeVar = tokenAtual.getLexema();
        consumir(Token.IDENT);
        consumir(Token.FECHA_PAREN);
        consumir(Token.PONTO_VIRGULA);
        
        Simbolo s = tabela.buscar(nomeVar);
        gerador.emitir("LEIT");              // Lê valor
        gerador.emitir("ARMZ", s.getEndereco()); // Armazena
        
    } else if (verificar(Token.WRITE)) {
        // Comando de escrita: write(expressao)
        consumir(Token.WRITE);
        consumir(Token.ABRE_PAREN);
        consumir(Token.IDENT);
        consumir(Token.FECHA_PAREN);
        consumir(Token.PONTO_VIRGULA);
        gerador.emitir("IMPR");
        
    } else if (verificar(Token.IF)) {
        // Comando condicional: if condicao then comandos else comandos $
        consumir(Token.IF);
        condicao();
        consumir(Token.THEN);
        
        // DSVF: se falso, pula para else
        int indiceDsvf = gerador.getProximoIndice();
        gerador.emitir("DSVF", 0);
        
        comandos();
        
        // DSVI: pula o else
        int indiceDsvi = gerador.getProximoIndice();
        gerador.emitir("DSVI", 0);
        
        // Preenche DSVF (aqui começa o else)
        gerador.alterarArgumento(indiceDsvf, gerador.getProximoIndice());
        
        pfalsa();  // Processa else
        
        // Preenche DSVI (aqui termina o if)
        gerador.alterarArgumento(indiceDsvi, gerador.getProximoIndice());
        
        consumir(Token.DOLAR);
        
    } else if (verificar(Token.WHILE)) {
        // Comando de repetição: while condicao do comandos $
        consumir(Token.WHILE);
        
        int inicioWhile = gerador.getProximoIndice();  // Marca início
        
        condicao();
        consumir(Token.DO);
        
        // DSVF: se falso, sai do loop
        int indiceDsvf = gerador.getProximoIndice();
        gerador.emitir("DSVF", 0);
        
        comandos();
        
        // DSVI: volta para o início
        gerador.emitir("DSVI", inicioWhile);
        
        // Preenche DSVF (aqui termina o loop)
        gerador.alterarArgumento(indiceDsvf, gerador.getProximoIndice());
        
        consumir(Token.DOLAR);
        
    } else if (verificar(Token.IDENT)) {
        // Atribuição ou chamada de procedimento
        identAtual = tokenAtual.getLexema();
        Simbolo s = tabela.buscar(identAtual);
        if (s == null) {
            erroSemantico("'" + identAtual + "' não declarado");
        }
        consumir(Token.IDENT);
        restoIdent();
        consumir(Token.PONTO_VIRGULA);
    }
}
```

### Regra `expressao()` - Expressões Aritméticas

```java
// <expressao> -> <termo> <outros_termos>
private void expressao() {
    termo();
    outros_termos();
}

// <outros_termos> -> <op_ad> <termo> <outros_termos> | λ
private void outros_termos() {
    if (verificar(Token.MAIS) || verificar(Token.MENOS)) {
        boolean soma = verificar(Token.MAIS);
        op_ad();
        termo();
        
        // Gera código DEPOIS do termo
        if (soma) {
            gerador.emitir("SOMA");
        } else {
            gerador.emitir("SUBT");
        }
        
        outros_termos();
    }
}

// <termo> -> <fator> <mais_fatores>
private void termo() {
    fator();
    mais_fatores();
}

// <mais_fatores> -> <op_mul> <fator> <mais_fatores> | λ
private void mais_fatores() {
    if (verificar(Token.MULT) || verificar(Token.DIV)) {
        boolean mult = verificar(Token.MULT);
        op_mul();
        fator();
        
        if (mult) {
            gerador.emitir("MULT");
        } else {
            gerador.emitir("DIVI");
        }
        
        mais_fatores();
    }
}

// <fator> -> ident | numero_int | numero_real | ( <expressao> )
private void fator() {
    if (verificar(Token.IDENT)) {
        Simbolo s = tabela.buscar(tokenAtual.getLexema());
        consumir(Token.IDENT);
        gerador.emitir("CRVL", s.getEndereco());  // Carrega valor
    } else if (verificar(Token.NUMERO_INT)) {
        int valor = Integer.parseInt(tokenAtual.getLexema());
        consumir(Token.NUMERO_INT);
        gerador.emitir("CRCT", valor);  // Carrega constante
    } else if (verificar(Token.NUMERO_REAL)) {
        double valor = Double.parseDouble(tokenAtual.getLexema());
        consumir(Token.NUMERO_REAL);
        gerador.emitir("CRCT", valor);
    } else if (verificar(Token.ABRE_PAREN)) {
        consumir(Token.ABRE_PAREN);
        expressao();
        consumir(Token.FECHA_PAREN);
    }
}
```

### Regra `condicao()` - Condições

```java
private void condicao() {
    expressao();
    relacao();
    expressao();
    
    // Gera instrução de comparação
    gerador.emitir(operadorRelacional);
}

private void relacao() {
    if (verificar(Token.IGUAL)) {
        operadorRelacional = "CMIG";  // ==
        consumir(Token.IGUAL);
    } else if (verificar(Token.DIFERENTE)) {
        operadorRelacional = "CMDG";  // <>
        consumir(Token.DIFERENTE);
    } else if (verificar(Token.MAIOR_IGUAL)) {
        operadorRelacional = "CMAI";  // >=
        consumir(Token.MAIOR_IGUAL);
    } else if (verificar(Token.MENOR_IGUAL)) {
        operadorRelacional = "CPMI";  // <=
        consumir(Token.MENOR_IGUAL);
    } else if (verificar(Token.MAIOR)) {
        operadorRelacional = "CMMA";  // >
        consumir(Token.MAIOR);
    } else if (verificar(Token.MENOR)) {
        operadorRelacional = "CMME";  // <
        consumir(Token.MENOR);
    }
}
```

---

# Análise Semântica

A análise semântica verifica o **significado** do programa: declarações, tipos, escopos.

## Simbolo.java

### O que é?

Representa uma entrada na tabela de símbolos (variável, procedimento ou parâmetro).

```java
public class Simbolo {
    
    // Categorias possíveis
    public enum Categoria {
        VARIAVEL,     // Variável
        PROCEDIMENTO, // Procedimento
        PARAMETRO     // Parâmetro de procedimento
    }

    private String nome;       // Nome do símbolo
    private Token tipo;        // Tipo (INTEGER, REAL, ou null para procedimento)
    private Categoria categoria;
    private String escopo;     // "global" ou nome do procedimento
    private int endereco;      // Endereço na memória

    // Construtor
    public Simbolo(String nome, Token tipo, Categoria categoria, 
                   String escopo, int endereco) {
        this.nome = nome;
        this.tipo = tipo;
        this.categoria = categoria;
        this.escopo = escopo;
        this.endereco = endereco;
    }

    // Getters para acessar os atributos
    public String getNome() { return nome; }
    public Token getTipo() { return tipo; }
    public Categoria getCategoria() { return categoria; }
    public String getEscopo() { return escopo; }
    public int getEndereco() { return endereco; }
}
```

## TabelaSimbolos.java

### O que é?

Gerencia todos os símbolos do programa. Permite adicionar, buscar e verificar símbolos.

```java
public class TabelaSimbolos {
    
    private List<Simbolo> simbolos;
    private String escopoAtual;  // "global" ou nome do procedimento

    public TabelaSimbolos() {
        this.simbolos = new ArrayList<>();
        this.escopoAtual = "global";
    }

    // Muda o escopo (ao entrar em um procedimento)
    public void entrarEscopo(String novoEscopo) {
        this.escopoAtual = novoEscopo;
    }

    // Volta ao escopo global
    public void sairEscopo() {
        this.escopoAtual = "global";
    }

    // Adiciona um símbolo
    public void adicionar(Simbolo simbolo) {
        simbolos.add(simbolo);
    }

    // Busca um símbolo pelo nome
    // Primeiro no escopo atual, depois no global
    public Simbolo buscar(String nome) {
        // Busca no escopo atual
        for (Simbolo s : simbolos) {
            if (s.getNome().equals(nome) && s.getEscopo().equals(escopoAtual)) {
                return s;
            }
        }
        // Busca no escopo global
        for (Simbolo s : simbolos) {
            if (s.getNome().equals(nome) && s.getEscopo().equals("global")) {
                return s;
            }
        }
        return null;  // Não encontrado
    }

    // Verifica se existe no escopo atual
    public boolean existeNoEscopoAtual(String nome) {
        for (Simbolo s : simbolos) {
            if (s.getNome().equals(nome) && s.getEscopo().equals(escopoAtual)) {
                return true;
            }
        }
        return false;
    }
}
```

---

# Geração de Código

A geração de código produz instruções para a máquina virtual.

## Instrucao.java

### O que é?

Representa uma instrução da máquina virtual.

```java
public class Instrucao {
    
    private String operacao;  // Ex: "SOMA", "CRCT", "DSVI"
    private String argumento; // Ex: "5", "42", "" (vazio)

    // Construtor sem argumento
    public Instrucao(String operacao) {
        this.operacao = operacao;
        this.argumento = "";
    }

    // Construtor com argumento inteiro
    public Instrucao(String operacao, int argumento) {
        this.operacao = operacao;
        this.argumento = String.valueOf(argumento);
    }

    // Construtor com argumento double
    public Instrucao(String operacao, double argumento) {
        this.operacao = operacao;
        this.argumento = String.valueOf(argumento);
    }

    // Getters e setters
    public String getOperacao() { return operacao; }
    public String getArgumento() { return argumento; }
    public void setArgumento(String argumento) { this.argumento = argumento; }

    @Override
    public String toString() {
        if (argumento.isEmpty()) {
            return operacao;
        }
        return operacao + " " + argumento;
    }
}
```

## GeradorCodigo.java

### O que é?

Gerencia a geração e armazenamento das instruções.

```java
public class GeradorCodigo {
    
    private List<Instrucao> codigo;
    private int enderecoAtual;  // Próximo endereço de memória

    public GeradorCodigo() {
        this.codigo = new ArrayList<>();
        this.enderecoAtual = 0;
    }

    // Retorna índice da próxima instrução (para backpatching)
    public int getProximoIndice() {
        return codigo.size();
    }

    // Aloca posição de memória
    public int alocarMemoria() {
        return enderecoAtual++;
    }

    // Emite instrução sem argumento
    public void emitir(String operacao) {
        codigo.add(new Instrucao(operacao));
    }

    // Emite instrução com argumento
    public void emitir(String operacao, int argumento) {
        codigo.add(new Instrucao(operacao, argumento));
    }

    // Altera argumento (backpatching - preencher depois)
    public void alterarArgumento(int indice, int novoValor) {
        codigo.get(indice).setArgumento(String.valueOf(novoValor));
    }

    // Retorna lista de instruções
    public List<Instrucao> getCodigo() {
        return codigo;
    }
}
```

### Backpatching

É a técnica de emitir uma instrução com endereço temporário (0) e preenchê-lo depois:

```java
// Exemplo: IF/ELSE
int indiceDsvf = gerador.getProximoIndice();
gerador.emitir("DSVF", 0);  // Endereço ainda não sabemos
// ... comandos do then ...
gerador.alterarArgumento(indiceDsvf, gerador.getProximoIndice());  // Agora sabemos!
```

---

# Máquina Virtual

## Instruções da VM

| Instrução | Descrição | Exemplo |
|-----------|-----------|---------|
| `INPP` | Inicia programa | |
| `PARA` | Para programa | |
| `ALME n` | Aloca n posições de memória | `ALME 1` |
| `CRCT v` | Carrega constante v na pilha | `CRCT 5` |
| `CRVL e` | Carrega valor do endereço e | `CRVL 0` |
| `ARMZ e` | Armazena topo da pilha no endereço e | `ARMZ 0` |
| `SOMA` | Soma dois valores do topo | |
| `SUBT` | Subtrai | |
| `MULT` | Multiplica | |
| `DIVI` | Divide | |
| `LEIT` | Lê valor do usuário | |
| `IMPR` | Imprime valor | |
| `CMIG` | Compara igual (==) | |
| `CMDG` | Compara diferente (<>) | |
| `CMAI` | Compara maior ou igual (>=) | |
| `CPMI` | Compara menor ou igual (<=) | |
| `CMMA` | Compara maior (>) | |
| `CMME` | Compara menor (<) | |
| `DSVF e` | Desvia para e se topo = 0 | `DSVF 15` |
| `DSVI e` | Desvia incondicionalmente | `DSVI 20` |
| `PUSHER e` | Empilha endereço de retorno | `PUSHER 30` |
| `CHPR e` | Chama procedimento em e | `CHPR 10` |
| `RTPR` | Retorna de procedimento | |
| `DESM n` | Desempilha n valores | `DESM 5` |
| `PARAM e` | Passa parâmetro | `PARAM 3` |

---

## 📋 Tabela Detalhada de Instruções (Código Objeto)

Esta seção explica cada instrução em detalhes, com exemplos de como a pilha se comporta.

---

### INPP - Inicializa Programa

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `INPP` |
| **Argumentos** | Nenhum |
| **Ação** | Inicializa a máquina virtual (pilha, memória, PC) |
| **Pilha Antes** | `[]` |
| **Pilha Depois** | `[]` |
| **Quando Gerada** | No início do método `programa()` |

**Código que gera:**
```java
gerador.emitir("INPP");
```

---

### PARA - Para Programa

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `PARA` |
| **Argumentos** | Nenhum |
| **Ação** | Encerra a execução do programa |
| **Pilha Antes** | Qualquer |
| **Pilha Depois** | N/A (programa encerra) |
| **Quando Gerada** | No final do método `programa()` |

**Código que gera:**
```java
gerador.emitir("PARA");
```

---

### ALME - Aloca Memória

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `ALME n` |
| **Argumentos** | n = quantidade de posições |
| **Ação** | Reserva n posições na memória |
| **Pilha Antes** | `[]` |
| **Pilha Depois** | `[]` |
| **Quando Gerada** | Ao declarar variáveis em `dc_v()` |

**Exemplo:** `ALME 1` reserva 1 posição de memória.

**Código que gera:**
```java
gerador.emitir("ALME", 1);
```

---

### CRCT - Carrega Constante

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `CRCT v` |
| **Argumentos** | v = valor da constante |
| **Ação** | Empilha o valor v |
| **Pilha Antes** | `[...]` |
| **Pilha Depois** | `[..., v]` |
| **Quando Gerada** | Ao encontrar número em `fator()` |

**Exemplo:** Para o número `5` no código:
```
CRCT 5    → Pilha: [5]
```

**Código que gera:**
```java
gerador.emitir("CRCT", 5);  // inteiro
gerador.emitir("CRCT", 3.14);  // real
```

---

### CRVL - Carrega Valor

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `CRVL e` |
| **Argumentos** | e = endereço de memória |
| **Ação** | Empilha o valor armazenado em memória[e] |
| **Pilha Antes** | `[...]` |
| **Pilha Depois** | `[..., memória[e]]` |
| **Quando Gerada** | Ao usar variável em expressão |

**Exemplo:** Se `x` está no endereço 3 e contém valor 10:
```
CRVL 3    → Pilha: [10]
```

**Código que gera:**
```java
Simbolo s = tabela.buscar(nomeVariavel);
gerador.emitir("CRVL", s.getEndereco());
```

---

### ARMZ - Armazena

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `ARMZ e` |
| **Argumentos** | e = endereço de memória |
| **Ação** | Desempilha topo e armazena em memória[e] |
| **Pilha Antes** | `[..., v]` |
| **Pilha Depois** | `[...]` |
| **Quando Gerada** | Em atribuição `x := expressao` |

**Exemplo:** Atribuir 5 para `x` (endereço 3):
```
CRCT 5    → Pilha: [5]
ARMZ 3    → Pilha: [], memória[3] = 5
```

**Código que gera:**
```java
gerador.emitir("ARMZ", s.getEndereco());
```

---

### SOMA - Soma

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `SOMA` |
| **Argumentos** | Nenhum |
| **Ação** | Desempilha dois valores, empilha a soma |
| **Pilha Antes** | `[..., a, b]` |
| **Pilha Depois** | `[..., a+b]` |
| **Quando Gerada** | Operador `+` em expressão |

**Exemplo:** `3 + 5`
```
CRCT 3    → Pilha: [3]
CRCT 5    → Pilha: [3, 5]
SOMA      → Pilha: [8]
```

**Código que gera:**
```java
gerador.emitir("SOMA");
```

---

### SUBT - Subtração

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `SUBT` |
| **Argumentos** | Nenhum |
| **Ação** | Desempilha dois valores, empilha a-b |
| **Pilha Antes** | `[..., a, b]` |
| **Pilha Depois** | `[..., a-b]` |
| **Quando Gerada** | Operador `-` em expressão |

**⚠️ Atenção:** A ordem importa! `a - b`, não `b - a`.

**Exemplo:** `10 - 3`
```
CRCT 10   → Pilha: [10]
CRCT 3    → Pilha: [10, 3]
SUBT      → Pilha: [7]    (10 - 3 = 7)
```

---

### MULT - Multiplicação

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `MULT` |
| **Argumentos** | Nenhum |
| **Ação** | Desempilha dois valores, empilha a*b |
| **Pilha Antes** | `[..., a, b]` |
| **Pilha Depois** | `[..., a*b]` |
| **Quando Gerada** | Operador `*` em expressão |

**Exemplo:** `4 * 5`
```
CRCT 4    → Pilha: [4]
CRCT 5    → Pilha: [4, 5]
MULT      → Pilha: [20]
```

---

### DIVI - Divisão

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `DIVI` |
| **Argumentos** | Nenhum |
| **Ação** | Desempilha dois valores, empilha a/b |
| **Pilha Antes** | `[..., a, b]` |
| **Pilha Depois** | `[..., a/b]` |
| **Quando Gerada** | Operador `/` em expressão |

**⚠️ Atenção:** Divisão por zero causa erro!

**Exemplo:** `10 / 2`
```
CRCT 10   → Pilha: [10]
CRCT 2    → Pilha: [10, 2]
DIVI      → Pilha: [5.0]
```

---

### LEIT - Leitura

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `LEIT` |
| **Argumentos** | Nenhum |
| **Ação** | Lê valor do usuário e empilha |
| **Pilha Antes** | `[...]` |
| **Pilha Depois** | `[..., valor_lido]` |
| **Quando Gerada** | Comando `read(x)` |

**Exemplo:** Usuário digita 42:
```
LEIT      → Pilha: [42.0]
ARMZ 0    → Pilha: [], memória[0] = 42.0
```

**Código que gera:**
```java
gerador.emitir("LEIT");
```

---

### IMPR - Impressão

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `IMPR` |
| **Argumentos** | Nenhum |
| **Ação** | Desempilha topo e imprime na tela |
| **Pilha Antes** | `[..., v]` |
| **Pilha Depois** | `[...]` |
| **Quando Gerada** | Comando `write(x)` |

**Exemplo:**
```
CRVL 0    → Pilha: [42.0]
IMPR      → Pilha: [], Tela: "Saída: 42.0"
```

---

### CMIG - Compara Igual

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `CMIG` |
| **Argumentos** | Nenhum |
| **Ação** | Desempilha dois valores, empilha 1 se iguais, 0 se diferentes |
| **Pilha Antes** | `[..., a, b]` |
| **Pilha Depois** | `[..., resultado]` |
| **Quando Gerada** | Operador `=` em condição |

**Exemplo:** `x = 5` onde x = 5:
```
CRVL 0    → Pilha: [5]
CRCT 5    → Pilha: [5, 5]
CMIG      → Pilha: [1.0]   (são iguais!)
```

---

### CMDG - Compara Diferente

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `CMDG` |
| **Argumentos** | Nenhum |
| **Ação** | Empilha 1 se diferentes, 0 se iguais |
| **Quando Gerada** | Operador `<>` em condição |

---

### CMAI - Compara Maior ou Igual

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `CMAI` |
| **Argumentos** | Nenhum |
| **Ação** | Empilha 1 se a >= b, 0 caso contrário |
| **Quando Gerada** | Operador `>=` em condição |

**Exemplo:** `x >= 10` onde x = 15:
```
CRVL 0    → Pilha: [15]
CRCT 10   → Pilha: [15, 10]
CMAI      → Pilha: [1.0]   (15 >= 10 é verdade)
```

---

### CPMI - Compara Menor ou Igual

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `CPMI` |
| **Argumentos** | Nenhum |
| **Ação** | Empilha 1 se a <= b, 0 caso contrário |
| **Quando Gerada** | Operador `<=` em condição |

---

### CMMA - Compara Maior

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `CMMA` |
| **Argumentos** | Nenhum |
| **Ação** | Empilha 1 se a > b, 0 caso contrário |
| **Quando Gerada** | Operador `>` em condição |

---

### CMME - Compara Menor

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `CMME` |
| **Argumentos** | Nenhum |
| **Ação** | Empilha 1 se a < b, 0 caso contrário |
| **Quando Gerada** | Operador `<` em condição |

---

### DSVF - Desvio Se Falso

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `DSVF e` |
| **Argumentos** | e = endereço de destino |
| **Ação** | Se topo = 0 (falso), PC = e; senão, PC++ |
| **Pilha Antes** | `[..., v]` |
| **Pilha Depois** | `[...]` |
| **Quando Gerada** | IF e WHILE (após condição) |

**Exemplo:** IF com condição falsa:
```
CRVL 0    → Pilha: [0]
CRCT 5    → Pilha: [0, 5]
CMIG      → Pilha: [0.0]   (0 != 5)
DSVF 20   → Pilha: [], PC = 20 (pula para else)
```

**Código que gera:**
```java
int indiceDsvf = gerador.getProximoIndice();
gerador.emitir("DSVF", 0);  // Backpatching
// ... depois ...
gerador.alterarArgumento(indiceDsvf, destino);
```

---

### DSVI - Desvio Incondicional

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `DSVI e` |
| **Argumentos** | e = endereço de destino |
| **Ação** | PC = e (sempre desvia) |
| **Pilha Antes** | `[...]` |
| **Pilha Depois** | `[...]` (não muda) |
| **Quando Gerada** | Pular procedimentos, pular else, voltar ao while |

**Exemplo:** Pular bloco de procedimentos:
```
DSVI 50   → PC = 50 (vai para o begin principal)
```

---

### PUSHER - Empilha Endereço de Retorno

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `PUSHER e` |
| **Argumentos** | e = endereço de retorno |
| **Ação** | Empilha e na pilha de retorno |
| **Pilha Retorno Antes** | `[...]` |
| **Pilha Retorno Depois** | `[..., e]` |
| **Quando Gerada** | Antes de `CHPR` |

**Importante:** Empilha na pilha de RETORNO, não na de operandos!

---

### CHPR - Chama Procedimento

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `CHPR e` |
| **Argumentos** | e = endereço do procedimento |
| **Ação** | PC = e (salta para o procedimento) |
| **Pilha** | Não muda |
| **Quando Gerada** | Chamada de procedimento |

**Exemplo:** Chamar procedimento que começa na linha 10:
```
PUSHER 50   → Retorno: [50]
CHPR 10     → PC = 10 (vai para o proc)
```

---

### RTPR - Retorna de Procedimento

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `RTPR` |
| **Argumentos** | Nenhum |
| **Ação** | PC = desempilha pilha de retorno |
| **Pilha Retorno Antes** | `[..., e]` |
| **Pilha Retorno Depois** | `[...]` |
| **Quando Gerada** | Fim do procedimento |

**Exemplo:**
```
RTPR      → Retorno: [50] → []
          → PC = 50 (volta para quem chamou)
```

---

### DESM - Desempilha Múltiplos

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `DESM n` |
| **Argumentos** | n = quantidade para desempilhar |
| **Ação** | Remove n valores da pilha (limpeza) |
| **Quando Gerada** | Antes de retornar do procedimento |

**Por que precisamos?** Para limpar parâmetros e variáveis locais antes de retornar.

**Código que gera:**
```java
gerador.emitir("DESM", numParametros + numLocais);
```

---

### PARAM - Passa Parâmetro

| Campo | Valor |
|-------|-------|
| **Sintaxe** | `PARAM e` |
| **Argumentos** | e = endereço do parâmetro |
| **Ação** | Copia valor do endereço e para a pilha de parâmetros |
| **Quando Gerada** | Ao passar argumentos para procedimento |

**Código que gera:**
```java
gerador.emitir("PARAM", s.getEndereco());
```

---

## Resumo Visual das Instruções

### Por Categoria

**Controle de Programa:**
| Instrução | Função |
|-----------|--------|
| `INPP` | Inicia |
| `PARA` | Encerra |

**Memória:**
| Instrução | Função |
|-----------|--------|
| `ALME` | Aloca |
| `CRCT` | Carrega constante |
| `CRVL` | Carrega valor |
| `ARMZ` | Armazena |

**Aritméticas:**
| Instrução | Operação |
|-----------|----------|
| `SOMA` | + |
| `SUBT` | - |
| `MULT` | * |
| `DIVI` | / |

**Comparações:**
| Instrução | Operação |
|-----------|----------|
| `CMIG` | == |
| `CMDG` | <> |
| `CMAI` | >= |
| `CPMI` | <= |
| `CMMA` | > |
| `CMME` | < |

**Entrada/Saída:**
| Instrução | Função |
|-----------|--------|
| `LEIT` | Lê do usuário |
| `IMPR` | Imprime na tela |

**Desvios:**
| Instrução | Função |
|-----------|--------|
| `DSVF` | Desvia se falso |
| `DSVI` | Desvia sempre |

**Procedimentos:**
| Instrução | Função |
|-----------|--------|
| `PUSHER` | Empilha retorno |
| `CHPR` | Chama procedimento |
| `RTPR` | Retorna |
| `DESM` | Limpa pilha |
| `PARAM` | Passa parâmetro |



## MaquinaVirtual.java

### Estrutura

```java
public class MaquinaVirtual {
    
    private List<Instrucao> codigo;    // Código a executar
    private double[] memoria;          // Memória (variáveis)
    private Stack<Double> pilha;       // Pilha de operandos
    private Stack<Integer> retorno;    // Pilha de retorno
    private int pc;                    // Program Counter
    private Scanner scanner;

    public MaquinaVirtual(List<Instrucao> codigo) {
        this.codigo = codigo;
        this.memoria = new double[1000];
        this.pilha = new Stack<>();
        this.retorno = new Stack<>();
        this.pc = 0;
        this.scanner = new Scanner(System.in);
    }
}
```

### Método `executar()`

```java
public void executar() {
    while (pc < codigo.size()) {
        Instrucao inst = codigo.get(pc);
        String op = inst.getOperacao();
        String arg = inst.getArgumento();

        switch (op) {
            case "INPP":
                pc++;
                break;
                
            case "PARA":
                return;  // Fim do programa
                
            case "CRCT":
                pilha.push(Double.parseDouble(arg));
                pc++;
                break;
                
            case "CRVL":
                pilha.push(memoria[Integer.parseInt(arg)]);
                pc++;
                break;
                
            case "ARMZ":
                memoria[Integer.parseInt(arg)] = pilha.pop();
                pc++;
                break;
                
            case "SOMA":
                double b = pilha.pop();
                double a = pilha.pop();
                pilha.push(a + b);
                pc++;
                break;
                
            case "DSVF":
                if (pilha.pop() == 0.0) {
                    pc = Integer.parseInt(arg);  // Desvia
                } else {
                    pc++;  // Continua
                }
                break;
                
            case "DSVI":
                pc = Integer.parseInt(arg);  // Desvia sempre
                break;
                
            case "CHPR":
                pc = Integer.parseInt(arg);  // Vai para o procedimento
                break;
                
            case "RTPR":
                pc = retorno.pop();  // Volta para quem chamou
                break;
                
            // ... outros casos
        }
    }
}
```

---

# Fluxo de Execução

## Main.java

```java
public class Main {
    public static void main(String[] args) {
        // 1. Lê o arquivo fonte
        String codigoFonte = new String(Files.readAllBytes(Paths.get(caminho)));

        // 2. Cria o analisador léxico
        ScannerLexico lexer = new ScannerLexico(codigoFonte);

        // 3. Cria o parser (que faz sintático, semântico e geração)
        Parser parser = new Parser(lexer);
        parser.parse();

        // 4. Imprime tabela de símbolos
        parser.getTabela().imprimir();

        // 5. Imprime código gerado
        parser.getGerador().imprimir();

        // 6. Executa na máquina virtual
        MaquinaVirtual vm = new MaquinaVirtual(parser.getGerador().getCodigo());
        vm.executar();
    }
}
```

## Exemplo Completo

### Código fonte (Pascal):

```pascal
program exemplo
var x, y : integer;
begin
    read(x);
    read(y);
    x := x + y;
    write(x);
end.
```

### Tokens gerados:

```
PROGRAM, IDENT(exemplo), VAR, IDENT(x), VIRGULA, IDENT(y), 
DOIS_PONTOS, INTEGER, PONTO_VIRGULA, BEGIN, READ, ABRE_PAREN, 
IDENT(x), FECHA_PAREN, PONTO_VIRGULA, READ, ABRE_PAREN, IDENT(y), 
FECHA_PAREN, PONTO_VIRGULA, IDENT(x), ATRIB, IDENT(x), MAIS, 
IDENT(y), PONTO_VIRGULA, WRITE, ABRE_PAREN, IDENT(x), FECHA_PAREN, 
PONTO_VIRGULA, END, PONTO, EOF
```

### Tabela de símbolos:

```
Simbolo[x, INTEGER, VARIAVEL, escopo: global, end: 0]
Simbolo[y, INTEGER, VARIAVEL, escopo: global, end: 1]
```

### Código gerado:

```
0: INPP
1: ALME 1
2: ALME 1
3: LEIT
4: ARMZ 0
5: LEIT
6: ARMZ 1
7: CRVL 0
8: CRVL 1
9: SOMA
10: ARMZ 0
11: CRVL 0
12: IMPR
13: PARA
```

### Execução:

```
Digite um valor: 10
Digite um valor: 5
Saída: 15.0
```

---

# Conceitos Importantes

## Gramática LALG

A gramática define a estrutura válida dos programas. Exemplo de regras:

```
<programa>  -> program ident <corpo> .
<corpo>     -> <dc> begin <comandos> end
<dc>        -> <dc_v> <mais_dc> | <dc_p> <mais_dc_p>
<comando>   -> read(ident) | write(ident) | if <cond> then <cmd> else <cmd> $ | ...
<expressao> -> <termo> <outros_termos>
<termo>     -> <fator> <mais_fatores>
<fator>     -> ident | numero | ( <expressao> )
```

## Parser Descendente Recursivo

Cada regra da gramática vira um método:

- `<programa>` → `programa()`
- `<corpo>` → `corpo()`
- `<expressao>` → `expressao()`
- etc.

## Máquina de Pilha

A VM é baseada em pilha, não em registradores:

```
CRCT 5    → Pilha: [5]
CRCT 3    → Pilha: [5, 3]
SOMA      → Pilha: [8]        (5 + 3)
ARMZ 0    → Pilha: [], memoria[0] = 8
```

## Escopo

- **Escopo global**: Variáveis declaradas fora de procedimentos
- **Escopo local**: Variáveis declaradas dentro de procedimentos
- A busca é feita primeiro no escopo local, depois no global

---

# Resumo Final

Este compilador implementa todas as fases clássicas:

1. **Análise Léxica**: Transforma texto em tokens
2. **Análise Sintática**: Verifica estrutura gramatical
3. **Análise Semântica**: Verifica declarações e tipos
4. **Geração de Código**: Produz código intermediário
5. **Execução**: Máquina virtual interpreta o código

O projeto demonstra os conceitos fundamentais de compiladores de forma completa e funcional.

---

# 🔧 Desafios Encontrados e Soluções

Esta seção documenta os principais problemas encontrados durante o desenvolvimento e como foram resolvidos. Use isto para explicar ao professor as dificuldades técnicas do projeto.

---

## Problema 1: Distinguir Operadores de Múltiplos Caracteres

### O Desafio

Como diferenciar `:` de `:=`, ou `<` de `<=` e `<>`?

Quando encontramos o caractere `<`, não sabemos imediatamente se é:
- Apenas `<` (menor que)
- `<=` (menor ou igual)
- `<>` (diferente)

### A Solução

Implementamos **lookahead** (olhar adiante) com o método `proxCaractere()`:

```java
case '<':
    avancar();  // Consumimos o '<'
    if (caractereAtual == '=') {
        avancar();
        return new TokenInformacoes(Token.MENOR_IGUAL, "<=", linhaAtual);
    } else if (caractereAtual == '>') {
        avancar();
        return new TokenInformacoes(Token.DIFERENTE, "<>", linhaAtual);
    }
    return new TokenInformacoes(Token.MENOR, "<", linhaAtual);
```

### Por que isso é difícil?

- Requer **decisão condicional** baseada no próximo caractere
- O scanner precisa "olhar adiante" sem consumir
- Errar aqui causa erros em cascata (léxico → sintático)

---

## Problema 2: Escopo de Variáveis

### O Desafio

Variáveis com mesmo nome podem existir em escopos diferentes:
- `x` global
- `x` dentro do procedimento "um"

Como o parser sabe qual `x` usar?

### A Solução

Implementamos **escopo** na tabela de símbolos:

```java
public Simbolo buscar(String nome) {
    // PRIMEIRO busca no escopo atual (local)
    for (Simbolo s : simbolos) {
        if (s.getNome().equals(nome) && s.getEscopo().equals(escopoAtual)) {
            return s;
        }
    }
    // DEPOIS busca no escopo global
    for (Simbolo s : simbolos) {
        if (s.getNome().equals(nome) && s.getEscopo().equals("global")) {
            return s;
        }
    }
    return null;
}
```

### Por que isso é difícil?

- Requer entendimento de **escopos hierárquicos**
- A busca deve priorizar o escopo local
- Mudança de escopo (entrar/sair de procedimento) precisa ser sincronizada com o parser

---

## Problema 3: Backpatching (Preenchimento Posterior)

### O Desafio

Ao gerar um `DSVI` (desvio incondicional), não sabemos o endereço de destino ainda:

```
DSVF ???   ← Para onde vai se falso? Não sabemos ainda!
... comandos do then ...
DSVI ???   ← Para onde pula? Não sabemos ainda!
... comandos do else ...
           ← SÓ AQUI sabemos os endereços!
```

### A Solução

Implementamos **backpatching**:

1. Emitimos a instrução com endereço temporário (0)
2. Guardamos o índice dessa instrução
3. Depois, quando sabemos o endereço real, voltamos e preenchemos

```java
// PASSO 1: Emite com endereço temporário
int indiceDsvf = gerador.getProximoIndice();
gerador.emitir("DSVF", 0);  // 0 é placeholder

// PASSO 2: Processa os comandos
comandos();

// PASSO 3: Agora sabemos! Preenche o endereço real
gerador.alterarArgumento(indiceDsvf, gerador.getProximoIndice());
```

### Por que isso é difícil?

- Requer **duas passagens** sobre a mesma instrução
- Precisa guardar índices corretamente
- Loops e condicionais aninhados complicam muito

---

## Problema 4: Múltiplos Procedimentos e DSVIs

### O Desafio

Na referência, cada procedimento tinha um `DSVI` antes dele:

```
DSVI 71  ← Pula procedimento "um"
... código do proc "um" ...
DSVI 71  ← Pula procedimento "dois" também!
... código do proc "dois" ...
         ← Linha 71: aqui começa o begin principal
```

Inicialmente só gerávamos UM `DSVI`, mas a referência tinha múltiplos.

### A Solução

Usamos uma **lista** para guardar todos os índices de DSVIs:

```java
private List<Integer> indicesDsviProcs = new ArrayList<>();

// Em dc() - primeiro procedimento
indicesDsviProcs.clear();
indicesDsviProcs.add(gerador.getProximoIndice());
gerador.emitir("DSVI", 0);

// Em mais_dc_p() - procedimentos adicionais
indicesDsviProcs.add(gerador.getProximoIndice());
gerador.emitir("DSVI", 0);

// No final, preenche TODOS os DSVIs
int enderecoFinal = gerador.getProximoIndice();
for (int indice : indicesDsviProcs) {
    gerador.alterarArgumento(indice, enderecoFinal);
}
```

### Por que isso é difícil?

- Requer entendimento de que **todos** os DSVIs apontam para o mesmo lugar
- A gramática é recursiva, então `mais_dc_p()` é chamado múltiplas vezes
- Cada chamada precisa adicionar seu DSVI à lista

---

## Problema 5: Ordem de Geração de Código nas Expressões

### O Desafio

Para a expressão `a + b * c`, a ordem importa!

A multiplicação tem **precedência maior**, então precisa ser calculada primeiro:
1. Calcular `b * c`
2. Depois calcular `a + (resultado)`

Mas a gramática lê **da esquerda para direita**...

### A Solução

A gramática já resolve isso com a hierarquia:

```
<expressao>  → <termo> <outros_termos>
<termo>      → <fator> <mais_fatores>
<fator>      → identificador | número
```

A multiplicação acontece em `<termo>`, que é processado **antes** de `+` em `<outros_termos>`.

Geramos o código **DEPOIS** de processar o operando direito:

```java
private void outros_termos() {
    if (verificar(Token.MAIS)) {
        boolean soma = true;
        op_ad();
        termo();           // PRIMEIRO processa o termo (que faz MULT antes)
        gerador.emitir("SOMA");  // DEPOIS emite a SOMA
        outros_termos();
    }
}
```

### Por que isso é difícil?

- A precedência vem da **estrutura da gramática**, não do código explícito
- O código é gerado em ordem **postfix** (operador depois dos operandos)
- Entender isso requer pensar na pilha de execução

---

## Problema 6: Chamada de Procedimento vs Atribuição

### O Desafio

Quando encontramos um identificador, não sabemos se é:
- **Atribuição**: `x := 5;`
- **Chamada de procedimento**: `proc(a, b);`

Ambos começam com `IDENT`!

### A Solução

Verificamos se o próximo token é `:=` (atribuição) ou `(` (chamada):

```java
private void restoIdent() {
    if (verificar(Token.ATRIB)) {
        // É ATRIBUIÇÃO
        consumir(Token.ATRIB);
        expressao();
        Simbolo s = tabela.buscar(identAtual);
        gerador.emitir("ARMZ", s.getEndereco());
    } else if (verificar(Token.ABRE_PAREN)) {
        // É CHAMADA DE PROCEDIMENTO
        Simbolo proc = tabela.buscar(identAtual);
        consumir(Token.ABRE_PAREN);
        
        gerador.emitir("PUSHER", 0);  // Endereço de retorno
        argumentos();
        consumir(Token.FECHA_PAREN);
        
        gerador.emitir("CHPR", proc.getEndereco());
    }
}
```

### Por que isso é difícil?

- Requer **lookahead** do parser
- A gramática usa uma técnica chamada **fatoração à esquerda**
- Errar aqui faz o parser travar em erros confusos

---

## Problema 7: Valores Temporários na Pilha

### O Desafio

Durante cálculos, valores intermediários ficam na pilha. Ex: `a + b * c`

```
CRVL a     → Pilha: [a]
CRVL b     → Pilha: [a, b]
CRVL c     → Pilha: [a, b, c]
MULT       → Pilha: [a, b*c]
SOMA       → Pilha: [a+b*c]
```

Se a ordem errar, o cálculo fica errado!

### A Solução

Seguimos rigorosamente a gramática e geramos código **postfix**:
- Primeiro carrega os operandos
- Depois emite a operação

A pilha funciona como LIFO (Last In, First Out), então:
- `MULT` pega os dois últimos valores (b, c)
- `SOMA` pega os dois seguintes (a, resultado)

### Por que isso é difícil?

- Requer **pensar em termos de pilha**, não de registradores
- Debug é complicado porque valores são "invisíveis"
- Expressões complexas geram sequências longas de instruções

---

## Problema 8: Loops While com Backpatching

### O Desafio

O WHILE precisa de **dois** desvios:
1. `DSVF` → Sai do loop se condição falsa (endereço ainda desconhecido)
2. `DSVI` → Volta para o início (endereço conhecido)

```
inicioWhile:     ← DSVI volta aqui
  CRVL x
  CRCT 10
  CPMI
  DSVF ???       ← Para onde sair? Não sabemos!
  ... comandos ...
  DSVI inicio    ← Fácil, já sabemos
                 ← SÓ AQUI sabemos para onde DSVF vai
```

### A Solução

```java
// Marca o início ANTES da condição
int inicioWhile = gerador.getProximoIndice();

condicao();
consumir(Token.DO);

// DSVF: sai se falso (endereço temporário)
int indiceDsvf = gerador.getProximoIndice();
gerador.emitir("DSVF", 0);

comandos();

// DSVI: volta para o início
gerador.emitir("DSVI", inicioWhile);

// Agora preenche DSVF
gerador.alterarArgumento(indiceDsvf, gerador.getProximoIndice());
```

### Por que isso é difícil?

- Mistura **dois tipos** de backpatching
- O início é conhecido, o fim não
- Loops aninhados complicam muito

---

## Problema 9: Contagem e Desalocação de Memória

### O Desafio

Ao sair de um procedimento, precisamos desalocar:
- Parâmetros
- Variáveis locais

Mas como saber quantos desalocar?

### A Solução

Contamos durante a declaração:

```java
// Ao declarar parâmetro
private void parametro() {
    // ...
    numParametros++;
    // ...
}

// Ao declarar variável local
private void dc_v() {
    // ...
    if (!tabela.getEscopoAtual().equals("global")) {
        numLocais++;
    }
    // ...
}

// No final do procedimento
private void corpo_p() {
    // ...
    gerador.emitir("DESM", numParametros + numLocais);
    gerador.emitir("RTPR");
}
```

### Por que isso é difícil?

- Requer **estado global** no parser
- Os contadores precisam ser zerados a cada procedimento
- Errar faz a pilha ficar inconsistente

---

## Problema 10: Execução da Máquina Virtual

### O Desafio

A VM precisa interpretar muitas instruções diferentes, cada uma com comportamento único. Como organizar isso de forma legível?

### A Solução

Usamos um `switch` gigante, mas com cada caso bem documentado:

```java
switch (op) {
    case "SOMA":
        double b = pilha.pop();
        double a = pilha.pop();
        pilha.push(a + b);
        pc++;
        break;
    // ...
}
```

### Por que isso é difícil?

- O switch tem **muitos casos**
- Cada instrução tem semântica diferente
- Bugs na VM são difíceis de rastrear

---

# Dicas para Explicar ao Professor

## Conceitos Chave para Demonstrar Entendimento

1. **Análise Léxica**: "O scanner lê caractere por caractere e agrupa em tokens, usando lookahead para operadores compostos"

2. **Parser Descendente Recursivo**: "Cada regra da gramática vira uma função, e a recursão mútua processa estruturas aninhadas"

3. **Tabela de Símbolos**: "Armazena informações de variáveis e procedimentos, com busca hierárquica por escopo"

4. **Backpatching**: "Emitimos instruções de desvio com endereço temporário e preenchemos depois quando descobrimos o destino"

5. **Máquina de Pilha**: "Operandos são empilhados, operações consomem do topo e empilham o resultado"

## Perguntas que o Professor Pode Fazer

### "Por que você usou backpatching?"
> "Porque não sabemos o endereço de destino no momento que geramos a instrução de desvio. Por exemplo, em um IF, o DSVF precisa pular para o ELSE, mas ainda não processamos os comandos do THEN para saber onde o ELSE começa."

### "Como funciona a precedência de operadores?"
> "A precedência vem da estrutura da gramática. Expressão chama termo, que chama fator. Multiplicação está em 'mais_fatores', que é processado antes de 'outros_termos' (soma). Assim, MULT é gerado antes de SOMA naturalmente."

### "O que acontece se uma variável não estiver declarada?"
> "A tabela de símbolos retorna null, e o parser lança um erro semântico indicando a linha e o nome da variável não declarada."

### "Como a VM sabe para onde voltar após um procedimento?"
> "O endereço de retorno é empilhado com PUSHER antes do CHPR. Quando RTPR executa, ele desempilha e usa esse endereço para definir o PC."

### "Por que usar uma pilha em vez de registradores?"
> "A pilha simplifica a geração de código para expressões complexas e chamadas aninhadas. Não precisamos alocar registradores - só empilhamos e desempilhamos."

---

# Checklist de Apresentação

- [ ] Mostrar o código fonte de teste
- [ ] Mostrar os tokens gerados
- [ ] Mostrar a tabela de símbolos
- [ ] Mostrar o código gerado
- [ ] Executar e mostrar entrada/saída
- [ ] Explicar um trecho de geração de código (IF ou WHILE)
- [ ] Explicar backpatching com exemplo
- [ ] Mostrar tratamento de erro (variável não declarada)
