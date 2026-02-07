package compilador;

import java.util.ArrayList;
import java.util.List;

public class GeradorCodigo {

    private List<Instrucao> codigo;
    private int enderecoAtual; 

    public GeradorCodigo() {
        this.codigo = new ArrayList<>();
        this.enderecoAtual = 0;
    }

    
    public int getProximoIndice() {
        return codigo.size();
    }

    
    public int alocarMemoria() {
        return enderecoAtual++;
    }

    
    public void emitir(String operacao) {
        codigo.add(new Instrucao(operacao));
    }

    
    public void emitir(String operacao, int argumento) {
        codigo.add(new Instrucao(operacao, argumento));
    }

    
    public void emitir(String operacao, double argumento) {
        codigo.add(new Instrucao(operacao, argumento));
    }

    
    public void emitir(String operacao, String argumento) {
        codigo.add(new Instrucao(operacao, argumento));
    }

    
    public void alterarArgumento(int indice, int novoValor) {
        codigo.get(indice).setArgumento(String.valueOf(novoValor));
    }

    
    public void imprimir() {
        System.out.println("\n=== CÓDIGO GERADO ===");
        for (int i = 0; i < codigo.size(); i++) {
            System.out.println(i + ": " + codigo.get(i));
        }
        System.out.println("=====================\n");
    }

    
    public List<Instrucao> getCodigo() {
        return codigo;
    }
}