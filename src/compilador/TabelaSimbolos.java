package compilador;

import java.util.ArrayList;
import java.util.List;

public class TabelaSimbolos {

    private List<Simbolo> simbolos;
    private String escopoAtual; 

    public TabelaSimbolos() {
        this.simbolos = new ArrayList<>();
        this.escopoAtual = "global";
    }

    
    public void entrarEscopo(String novoEscopo) {
        this.escopoAtual = novoEscopo;
    }

    
    public void sairEscopo() {
        this.escopoAtual = "global";
    }

    public String getEscopoAtual() {
        return escopoAtual;
    }

    
    public void adicionar(Simbolo simbolo) {
        simbolos.add(simbolo);
    }

    
    public Simbolo buscar(String nome) {
        
        for (Simbolo s : simbolos) {
            if (s.getNome().equals(nome) && s.getEscopo().equals(escopoAtual)) {
                return s;
            }
        }
        
        for (Simbolo s : simbolos) {
            if (s.getNome().equals(nome) && s.getEscopo().equals("global")) {
                return s;
            }
        }
        return null; 
    }

    
    public boolean existeNoEscopoAtual(String nome) {
        for (Simbolo s : simbolos) {
            if (s.getNome().equals(nome) && s.getEscopo().equals(escopoAtual)) {
                return true;
            }
        }
        return false;
    }

    
    public void imprimir() {
        System.out.println("\n=== TABELA DE SÍMBOLOS ===");
        for (Simbolo s : simbolos) {
            System.out.println(s);
        }
        System.out.println("===========================\n");
    }
}