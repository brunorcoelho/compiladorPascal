package compilador;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        String caminhoFonte = "../descricao/correto.pascal.txt";
        String caminhoObjeto = "../descricao/saida.obj";

        try {

            System.out.println("=== COMPILADOR PASCAL DESCENDENTE RECURSIVO ===\n");

            String codigoFonte = new String(Files.readAllBytes(Paths.get(caminhoFonte)));

            ScannerLexico lexer = new ScannerLexico(codigoFonte);

            System.out.println("=== TOKENS ENCONTRADOS ===");
            ScannerLexico lexerVisual = new ScannerLexico(codigoFonte);
            TokenInformacoes token;
            int contadorTokens = 0;
            do {
                token = lexerVisual.proximoToken();
                System.out.println(token);
                contadorTokens++;
            } while (token.getTipo() != Token.EOF);
            System.out.println("\n>>> Total de tokens: " + contadorTokens + " <<<\n");

            System.out.println("=== ANÁLISE SINTÁTICA ===");
            Parser parser = new Parser(lexer);
            parser.parse();

            parser.getTabela().imprimir();

            parser.getGerador().imprimir();

            salvarCodigoObjeto(parser.getGerador().getCodigo(), caminhoObjeto);
            System.out.println("\n>>> Código objeto salvo em: " + caminhoObjeto + " <<<\n");

            System.out.println("=== CARREGANDO CÓDIGO OBJETO ===\n");
            List<Instrucao> codigoCarregado = carregarCodigoObjeto(caminhoObjeto);
            System.out.println(">>> " + codigoCarregado.size() + " instruções carregadas <<<");

            MaquinaVirtual vm = new MaquinaVirtual(codigoCarregado);
            vm.executar();

        } catch (IOException e) {
            System.err.println("Erro ao ler/escrever arquivo: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void salvarCodigoObjeto(List<Instrucao> codigo, String caminho) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(caminho))) {
            for (Instrucao inst : codigo) {
                if (inst.getArgumento() != null) {
                    writer.println(inst.getOperacao() + " " + inst.getArgumento());
                } else {
                    writer.println(inst.getOperacao());
                }
            }
        }
    }

    private static List<Instrucao> carregarCodigoObjeto(String caminho) throws IOException {
        List<Instrucao> codigo = new ArrayList<>();
        List<String> linhas = Files.readAllLines(Paths.get(caminho));

        for (String linha : linhas) {
            linha = linha.trim();
            if (linha.isEmpty())
                continue;

            String[] partes = linha.split("\\s+", 2);
            String operacao = partes[0];
            String argumento = partes.length > 1 ? partes[1] : null;

            codigo.add(new Instrucao(operacao, argumento));
        }

        return codigo;
    }
}
