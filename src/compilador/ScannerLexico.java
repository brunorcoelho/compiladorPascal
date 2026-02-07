package compilador;

import java.util.HashMap;
import java.util.Map;

public class ScannerLexico {
    private String codigoFonte;
    private int posicao;
    private int linha;
    private char caractereAtual;
    private static final Map<String, Token> palavrasReservadas = new HashMap<>();

    static {
        palavrasReservadas.put("program", Token.PROGRAM);
        palavrasReservadas.put("begin", Token.BEGIN);
        palavrasReservadas.put("end", Token.END);
        palavrasReservadas.put("if", Token.IF);
        palavrasReservadas.put("then", Token.THEN);
        palavrasReservadas.put("else", Token.ELSE);
        palavrasReservadas.put("while", Token.WHILE);
        palavrasReservadas.put("do", Token.DO);
        palavrasReservadas.put("procedure", Token.PROCEDURE);
        palavrasReservadas.put("var", Token.VAR);
        palavrasReservadas.put("read", Token.READ);
        palavrasReservadas.put("write", Token.WRITE);
        palavrasReservadas.put("real", Token.REAL);
        palavrasReservadas.put("integer", Token.INTEGER);
    }

    public ScannerLexico(String codigoFonte) {
        this.codigoFonte = codigoFonte;
        this.posicao = 0;
        this.linha = 1;
        this.caractereAtual = codigoFonte.length() > 0 ? codigoFonte.charAt(0) : '\0';
    }

    private void avancar() {
        posicao++;
        if (posicao < codigoFonte.length()) {
            caractereAtual = codigoFonte.charAt(posicao);
        } else {
            caractereAtual = '\0';
        }
    }

    private char proxCaractere() {
        int proxPosicao = posicao + 1;
        if (proxPosicao < codigoFonte.length()) {
            return codigoFonte.charAt(proxPosicao);
        }
        return '\0';
    }

    private void pularEspacosEComentarios() {
        while (caractereAtual != '\0') {
            if (caractereAtual == ' ' || caractereAtual == '\t' || caractereAtual == '\r') {
                avancar();
            } else if (caractereAtual == '\n') {
                linha++;
                avancar();
            } else if (caractereAtual == '{') {
                avancar();
                while (caractereAtual != '}' && caractereAtual != '\0') {
                    if (caractereAtual == '\n')
                        linha++;
                    avancar();
                }
                avancar();
            } else if (caractereAtual == '/' && proxCaractere() == '*') {
                avancar();
                avancar();
                while (!(caractereAtual == '*' && proxCaractere() == '/') && caractereAtual != '\0') {
                    if (caractereAtual == '\n')
                        linha++;
                    avancar();
                }
                avancar();
                avancar();
            } else {
                break;
            }
        }
    }

    
    
    private TokenInformacoes lerIdentificadorOuPalavraReservada() {
        StringBuilder lexema = new StringBuilder();

        
        while (Character.isLetterOrDigit(caractereAtual) || caractereAtual == '_') {
            lexema.append(caractereAtual);
            avancar();
        }

        String palavra = lexema.toString();

        
        Token tipo = palavrasReservadas.get(palavra.toLowerCase());
        if (tipo != null) {
            return new TokenInformacoes(tipo, palavra, linha);
        } else {
            return new TokenInformacoes(Token.IDENT, palavra, linha);
        }
    }

    

    private TokenInformacoes lerNumero() {
        StringBuilder lexema = new StringBuilder();
        
        while (Character.isDigit(caractereAtual)) {
            lexema.append(caractereAtual);
            avancar();
        }
        
        if (caractereAtual == '.' && Character.isDigit(proxCaractere())) {
            lexema.append(caractereAtual);
            avancar();
            
            while (Character.isDigit(caractereAtual)) {
                lexema.append(caractereAtual);
                avancar();
            }
            return new TokenInformacoes(Token.NUMERO_REAL, lexema.toString(), linha);
        }
        return new TokenInformacoes(Token.NUMERO_INT, lexema.toString(), linha);
    }

    
    private TokenInformacoes lerOperadorOuDelimitador() {
        int linhaAtual = linha;
        switch (caractereAtual) {
            
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
            
            case '.':
                avancar();
                return new TokenInformacoes(Token.PONTO, ".", linhaAtual);
            case ',':
                avancar();
                return new TokenInformacoes(Token.VIRGULA, ",", linhaAtual);
            case ';':
                avancar();
                return new TokenInformacoes(Token.PONTO_VIRGULA, ";", linhaAtual);
            case '(':
                avancar();
                return new TokenInformacoes(Token.ABRE_PAREN, "(", linhaAtual);
            case ')':
                avancar();
                return new TokenInformacoes(Token.FECHA_PAREN, ")", linhaAtual);
            case '$':
                avancar();
                return new TokenInformacoes(Token.DOLAR, "$", linhaAtual);
            
            case ':':
                avancar();
                if (caractereAtual == '=') {
                    avancar();
                    return new TokenInformacoes(Token.ATRIB, ":=", linhaAtual);
                }
                return new TokenInformacoes(Token.DOIS_PONTOS, ":", linhaAtual);
            
            case '=':
                avancar();
                return new TokenInformacoes(Token.IGUAL, "=", linhaAtual);
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
            case '>':
                avancar();
                if (caractereAtual == '=') {
                    avancar();
                    return new TokenInformacoes(Token.MAIOR_IGUAL, ">=", linhaAtual);
                }
                return new TokenInformacoes(Token.MAIOR, ">", linhaAtual);
            default:
                return null; 
        }
    }

    
    public TokenInformacoes proximoToken() {
        pularEspacosEComentarios();

        
        if (caractereAtual == '\0') {
            return new TokenInformacoes(Token.EOF, "", linha);
        }

        
        if (Character.isLetter(caractereAtual)) {
            return lerIdentificadorOuPalavraReservada();
        }

        

        if (Character.isDigit(caractereAtual)) {
            return lerNumero();
        }

        
        TokenInformacoes token = lerOperadorOuDelimitador();
        if (token != null) {
            return token;
        }

        
        char charInvalido = caractereAtual;
        avancar();
        throw new RuntimeException("Erro léxico na linha" + linha + ": caractere inválido'" + charInvalido + "'");
    }

}