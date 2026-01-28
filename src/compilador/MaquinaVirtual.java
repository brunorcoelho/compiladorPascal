package compilador;

import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class MaquinaVirtual {

    private List<Instrucao> codigo;
    private double[] memoria; // Memória para variáveis
    private Stack<Double> pilha; // Pilha de operandos
    private Stack<Integer> retorno; // Pilha de endereços de retorno
    private int pc; // Program Counter (próxima instrução)
    private Scanner scanner;

    public MaquinaVirtual(List<Instrucao> codigo) {
        this.codigo = codigo;
        this.memoria = new double[1000]; // 1000 posições de memória
        this.pilha = new Stack<>();
        this.retorno = new Stack<>();
        this.pc = 0;
        this.scanner = new Scanner(System.in);
    }

    public void executar() {
        System.out.println("\n=== EXECUTANDO PROGRAMA ===\n");

        while (pc < codigo.size()) {
            Instrucao inst = codigo.get(pc);
            String op = inst.getOperacao();
            String arg = inst.getArgumento();

            switch (op) {
                case "INPP":
                    // Inicia programa - nada a fazer
                    pc++;
                    break;

                case "PARA":
                    // Para programa
                    System.out.println("\n=== PROGRAMA FINALIZADO ===");
                    return;

                case "ALME":
                    // Aloca memória - nada a fazer (já alocado)
                    pc++;
                    break;

                case "CRCT":
                    // Carrega constante na pilha
                    pilha.push(Double.parseDouble(arg));
                    pc++;
                    break;

                case "CRVL":
                    // Carrega valor da memória na pilha
                    pilha.push(memoria[Integer.parseInt(arg)]);
                    pc++;
                    break;

                case "ARMZ":
                    // Armazena topo da pilha na memória
                    memoria[Integer.parseInt(arg)] = pilha.pop();
                    pc++;
                    break;

                case "SOMA":
                    double b1 = pilha.pop();
                    double a1 = pilha.pop();
                    pilha.push(a1 + b1);
                    pc++;
                    break;

                case "SUBT":
                    double b2 = pilha.pop();
                    double a2 = pilha.pop();
                    pilha.push(a2 - b2);
                    pc++;
                    break;

                case "MULT":
                    double b3 = pilha.pop();
                    double a3 = pilha.pop();
                    pilha.push(a3 * b3);
                    pc++;
                    break;

                case "DIVI":
                    double b4 = pilha.pop();
                    double a4 = pilha.pop();
                    pilha.push(a4 / b4);
                    pc++;
                    break;

                case "LEIT":
                    // Lê valor do usuário
                    System.out.print("Digite um valor: ");
                    double valor = scanner.nextDouble();
                    pilha.push(valor);
                    pc++;
                    break;

                case "IMPR":
                    // Imprime valor
                    System.out.println("Saída: " + pilha.pop());
                    pc++;
                    break;

                // Comparações
                case "CMIG": // ==
                    pilha.push(pilha.pop().equals(pilha.pop()) ? 1.0 : 0.0);
                    pc++;
                    break;

                case "CMDG": // <>
                    pilha.push(!pilha.pop().equals(pilha.pop()) ? 1.0 : 0.0);
                    pc++;
                    break;

                case "CMAI": // >=
                    double r1 = pilha.pop();
                    double l1 = pilha.pop();
                    pilha.push(l1 >= r1 ? 1.0 : 0.0);
                    pc++;
                    break;

                case "CPMI": // <=
                    double r2 = pilha.pop();
                    double l2 = pilha.pop();
                    pilha.push(l2 <= r2 ? 1.0 : 0.0);
                    pc++;
                    break;

                case "CMMA": // >
                    double r3 = pilha.pop();
                    double l3 = pilha.pop();
                    pilha.push(l3 > r3 ? 1.0 : 0.0);
                    pc++;
                    break;

                case "CMME": // <
                    double r4 = pilha.pop();
                    double l4 = pilha.pop();
                    pilha.push(l4 < r4 ? 1.0 : 0.0);
                    pc++;
                    break;

                // Desvios
                case "DSVF":
                    // Desvia se falso (topo = 0)
                    if (pilha.pop() == 0.0) {
                        pc = Integer.parseInt(arg);
                    } else {
                        pc++;
                    }
                    break;

                case "DSVI":
                    // Desvio incondicional
                    pc = Integer.parseInt(arg);
                    break;

                // Procedimentos
                case "PUSHER":
                    // Empilha endereço de retorno
                    retorno.push(Integer.parseInt(arg));
                    pc++;
                    break;

                case "CHPR":
                    // Chama procedimento
                    pc = Integer.parseInt(arg);
                    break;

                case "RTPR":
                    // Retorna de procedimento
                    pc = retorno.pop();
                    break;

                case "PARAM":
                    // Passa parâmetro (copia valor para posição)
                    // Por simplicidade, só incrementa PC
                    pc++;
                    break;

                case "DESM":
                    // Desempilha N valores (limpeza)
                    pc++;
                    break;

                default:
                    System.out.println("ERRO: Instrução desconhecida: " + op);
                    return;
            }
        }
    }
}