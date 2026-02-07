package compilador;

public class Simbolo {

    
    public enum Categoria {
        VARIAVEL,
        PROCEDIMENTO,
        PARAMETRO
    }

    private String nome;
    private Token tipo; 
    private Categoria categoria;
    private String escopo; 
    private int endereco;

    public Simbolo(String nome, Token tipo, Categoria categoria, String escopo, int endereco) {
        this.nome = nome;
        this.tipo = tipo;
        this.categoria = categoria;
        this.escopo = escopo;
        this.endereco = endereco;
    }

    
    public String getNome() {
        return nome;
    }

    public Token getTipo() {
        return tipo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getEscopo() {
        return escopo;
    }

    public int getEndereco() {
        return endereco;
    }

    @Override
    public String toString() {
        return "Simbolo[" + nome + ", " + tipo + ", " + categoria + ", escopo: " + escopo + ", end: " + endereco + "]";
    }
}