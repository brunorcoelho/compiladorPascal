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
                case "INPP":
                    
                    pc++;
                    break;

                case "PARA":
                    
                    System.out.println("\n=== PROGRAMA FINALIZADO ===");
                    return;

                case "ALME":
                    
                    pc++;
                    break;

                case "CRCT":
                    
                    pilha.push(Double.parseDouble(arg));
                    pc++;
                    break;

                case "CRVL":
                    
                    pilha.push(memoria[Integer.parseInt(arg)]);
                    pc++;
                    break;

                case "ARMZ":
                    
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
                    
                    System.out.print("Digite um valor: ");
                    double valor = scanner.nextDouble();
                    pilha.push(valor);
                    pc++;
                    break;

                case "IMPR":
                    
                    System.out.println("Saída: " + pilha.pop());
                    pc++;
                    break;

                
                case "CMIG": 
                    pilha.push(pilha.pop().equals(pilha.pop()) ? 1.0 : 0.0);
                    pc++;
                    break;

                case "CMDG": 
                    pilha.push(!pilha.pop().equals(pilha.pop()) ? 1.0 : 0.0);
                    pc++;
                    break;

                case "CMAI": 
                    double r1 = pilha.pop();
                    double l1 = pilha.pop();
                    pilha.push(l1 >= r1 ? 1.0 : 0.0);
                    pc++;
                    break;

                case "CPMI": 
                    double r2 = pilha.pop();
                    double l2 = pilha.pop();
                    pilha.push(l2 <= r2 ? 1.0 : 0.0);
                    pc++;
                    break;

                case "CMMA": 
                    double r3 = pilha.pop();
                    double l3 = pilha.pop();
                    pilha.push(l3 > r3 ? 1.0 : 0.0);
                    pc++;
                    break;

                case "CMME": 
                    double r4 = pilha.pop();
                    double l4 = pilha.pop();
                    pilha.push(l4 < r4 ? 1.0 : 0.0);
                    pc++;
                    break;

                
                case "DSVF":
                    
                    if (pilha.pop() == 0.0) {
                        pc = Integer.parseInt(arg);
                    } else {
                        pc++;
                    }
                    break;

                case "DSVI":
                    
                    pc = Integer.parseInt(arg);
                    break;

                
                case "PUSHER":
                    
                    retorno.push(Integer.parseInt(arg));
                    pc++;
                    break;

                case "CHPR":
                    
                    pc = Integer.parseInt(arg);
                    break;

                case "RTPR":
                    
                    pc = retorno.pop();
                    break;

                case "PARAM":
                    
                    int endParam = Integer.parseInt(arg);
                    pilha.push(memoria[endParam]);
                    pc++;
                    break;

                case "DESM":
                    
                    pc++;
                    break;

                default:
                    System.out.println("ERRO: Instrução desconhecida: " + op);
                    return;
            }
        }
    }
}