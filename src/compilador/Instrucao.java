package compilador;

public class Instrucao {

    private String operacao; // Ex: "CRCT", "SOMA", "ARMZ"
    private String argumento; // Ex: "5", "3", "" (vazio se não tiver)

    public Instrucao(String operacao) {
        this.operacao = operacao;
        this.argumento = "";
    }

    public Instrucao(String operacao, String argumento) {
        this.operacao = operacao;
        this.argumento = argumento;
    }

    public Instrucao(String operacao, int argumento) {
        this.operacao = operacao;
        this.argumento = String.valueOf(argumento);
    }

    public Instrucao(String operacao, double argumento) {
        this.operacao = operacao;
        this.argumento = String.valueOf(argumento);
    }

    public String getOperacao() {
        return operacao;
    }

    public String getArgumento() {
        return argumento;
    }

    public void setArgumento(String argumento) {
        this.argumento = argumento;
    }

    @Override
    public String toString() {
        if (argumento.isEmpty()) {
            return operacao;
        }
        return operacao + " " + argumento;
    }
}