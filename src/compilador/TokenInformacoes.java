package compilador;

public class TokenInformacoes {
    private Token tipo;
    private String lexema;
    private int linha;

    public Token getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinha() {
        return linha;
    }

    public TokenInformacoes(Token tipo, String lexema, int linha) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linha = linha;
    }

    @Override
    public String toString() {
        return "Token[" + tipo + ", '" + lexema + "', linha " + linha + "]";
    }
}