package compilador;

import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class MaquinaVirtual {

    private List<Instrucao> codigo;
    private double[] memoria;
    private Stack<Double> pilha;
    private Stack<Integer> retorno;
    private int pc;
    private Scanner scanner;

    public MaquinaVirtual(List<Instrucao> codigo) {
        this.codigo = codigo;
        this.memoria = new double[1000];
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
                case "INPP": // início do programa
                    pc++;
                    break;
                case "PARA": // fim do programa
                    System.out.println("\n=== PROGRAMA FINALIZADO ===");
                    return;
                case "ALME": // alocar memória
                    pc++;
                    break;
                case "CRCT": // carregar constante na pilha
                    pilha.push(Double.parseDouble(arg));
                    pc++;
                    break;
                case "CRVL": // carregar valor da memória na pilha
                    pilha.push(memoria[Integer.parseInt(arg)]);
                    pc++;
                    break;
                case "ARMZ": // armazenar valor da pilha na memória
                    memoria[Integer.parseInt(arg)] = pilha.pop();
                    pc++;
                    break;
                case "SOMA": // soma dois valores do topo da pilha
                    double b1 = pilha.pop();
                    double a1 = pilha.pop();
                    pilha.push(a1 + b1);
                    pc++;
                    break;
                case "SUBT": // subtrai dois valores do topo da pilha
                    double b2 = pilha.pop();
                    double a2 = pilha.pop();
                    pilha.push(a2 - b2);
                    pc++;
                    break;
                case "MULT": // multiplica dois valores do topo da pilha
                    double b3 = pilha.pop();
                    double a3 = pilha.pop();
                    pilha.push(a3 * b3);
                    pc++;
                    break;
                case "DIVI": // divide dois valores do topo da pilha
                    double b4 = pilha.pop();
                    double a4 = pilha.pop();
                    pilha.push(a4 / b4);
                    pc++;
                    break;
                case "LEIT": // lê valor do usuário
                    System.out.print("Digite um valor: ");
                    double valor = scanner.nextDouble();
                    pilha.push(valor);
                    pc++;
                    break;
                case "IMPR": // imprime valor do topo da pilha
                    System.out.println("Saída: " + pilha.pop());
                    pc++;
                    break;
                case "CMIG": // compara se é igual (==)
                    pilha.push(pilha.pop().equals(pilha.pop()) ? 1.0 : 0.0);
                    pc++;
                    break;
                case "CMDG": // compara se é diferente (<>)
                    pilha.push(!pilha.pop().equals(pilha.pop()) ? 1.0 : 0.0);
                    pc++;
                    break;
                case "CMAI": // compara se é maior ou igual (>=)
                    double r1 = pilha.pop();
                    double l1 = pilha.pop();
                    pilha.push(l1 >= r1 ? 1.0 : 0.0);
                    pc++;
                    break;
                case "CPMI": // compara se é menor ou igual (<=)
                    double r2 = pilha.pop();
                    double l2 = pilha.pop();
                    pilha.push(l2 <= r2 ? 1.0 : 0.0);
                    pc++;
                    break;
                case "CMMA": // compara se é maior (>)
                    double r3 = pilha.pop();
                    double l3 = pilha.pop();
                    pilha.push(l3 > r3 ? 1.0 : 0.0);
                    pc++;
                    break;
                case "CMME": // compara se é menor (<)
                    double r4 = pilha.pop();
                    double l4 = pilha.pop();
                    pilha.push(l4 < r4 ? 1.0 : 0.0);
                    pc++;
                    break;
                case "DSVF": // desvia pra outra instrução se o topo da pilha for falso (0)
                    if (pilha.pop() == 0.0) {
                        pc = Integer.parseInt(arg);
                    } else {
                        pc++;
                    }
                    break;
                case "DSVI": // desvio incondicional
                    pc = Integer.parseInt(arg);
                    break;
                case "PUSHER": // empilha endereço de retorno
                    retorno.push(Integer.parseInt(arg));
                    pc++;
                    break;
                case "CHPR": // chama procedimento
                    pc = Integer.parseInt(arg);
                    break;
                case "RTPR": // retorna do procedimento
                    pc = retorno.pop();
                    break;
                case "PARAM": // passa parâmetro (empilha valor)
                    int endParam = Integer.parseInt(arg);
                    pilha.push(memoria[endParam]);
                    pc++;
                    break;
                case "DESM": // desempilha (limpa memória local)
                    pc++;
                    break;
                default:
                    System.out.println("ERRO: Instrução desconhecida: " + op);
                    return;
            }
        }
    }
}