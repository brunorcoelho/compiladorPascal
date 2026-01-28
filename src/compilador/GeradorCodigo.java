package compilador;

import java.util.ArrayList;
import java.util.List;

public class GeradorCodigo {

    private List<Instrucao> codigo;
    private int enderecoAtual; // Próximo endereço de memória disponível

    public GeradorCodigo() {
        this.codigo = new ArrayList<>();
        this.enderecoAtual = 0;
    }

    // Retorna o índice da próxima instrução
    public int getProximoIndice() {
        return codigo.size();
    }

    // Aloca uma posição de memória e retorna o endereço
    public int alocarMemoria() {
        return enderecoAtual++;
    }

    // Adiciona uma instrução sem argumento
    public void emitir(String operacao) {
        codigo.add(new Instrucao(operacao));
    }

    // Adiciona uma instrução com argumento inteiro
    public void emitir(String operacao, int argumento) {
        codigo.add(new Instrucao(operacao, argumento));
    }

    // Adiciona uma instrução com argumento double
    public void emitir(String operacao, double argumento) {
        codigo.add(new Instrucao(operacao, argumento));
    }

    // Adiciona uma instrução com argumento string
    public void emitir(String operacao, String argumento) {
        codigo.add(new Instrucao(operacao, argumento));
    }

    // Altera o argumento de uma instrução (usado para backpatching)
    public void alterarArgumento(int indice, int novoValor) {
        codigo.get(indice).setArgumento(String.valueOf(novoValor));
    }

    // Imprime o código gerado
    public void imprimir() {
        System.out.println("\n=== CÓDIGO GERADO ===");
        for (int i = 0; i < codigo.size(); i++) {
            System.out.println(i + ": " + codigo.get(i));
        }
        System.out.println("=====================\n");
    }

    // Retorna o código como lista
    public List<Instrucao> getCodigo() {
        return codigo;
    }
}