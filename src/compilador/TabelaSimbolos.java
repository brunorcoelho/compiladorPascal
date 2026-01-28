package compilador;

import java.util.ArrayList;
import java.util.List;

public class TabelaSimbolos {

    private List<Simbolo> simbolos;
    private String escopoAtual; // "global" ou nome do procedimento

    public TabelaSimbolos() {
        this.simbolos = new ArrayList<>();
        this.escopoAtual = "global";
    }

    // Muda o escopo atual (ao entrar em um procedimento)
    public void entrarEscopo(String novoEscopo) {
        this.escopoAtual = novoEscopo;
    }

    // Volta ao escopo global (ao sair de um procedimento)
    public void sairEscopo() {
        this.escopoAtual = "global";
    }

    public String getEscopoAtual() {
        return escopoAtual;
    }

    // Adiciona um símbolo à tabela
    public void adicionar(Simbolo simbolo) {
        simbolos.add(simbolo);
    }

    // Busca um símbolo pelo nome (primeiro no escopo atual, depois global)
    public Simbolo buscar(String nome) {
        // Primeiro busca no escopo atual
        for (Simbolo s : simbolos) {
            if (s.getNome().equals(nome) && s.getEscopo().equals(escopoAtual)) {
                return s;
            }
        }
        // Se não encontrou, busca no escopo global
        for (Simbolo s : simbolos) {
            if (s.getNome().equals(nome) && s.getEscopo().equals("global")) {
                return s;
            }
        }
        return null; // Não encontrado
    }

    // Verifica se símbolo já existe no escopo atual
    public boolean existeNoEscopoAtual(String nome) {
        for (Simbolo s : simbolos) {
            if (s.getNome().equals(nome) && s.getEscopo().equals(escopoAtual)) {
                return true;
            }
        }
        return false;
    }

    // Imprime toda a tabela (para debug)
    public void imprimir() {
        System.out.println("\n=== TABELA DE SÍMBOLOS ===");
        for (Simbolo s : simbolos) {
            System.out.println(s);
        }
        System.out.println("===========================\n");
    }
}