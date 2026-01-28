package compilador;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        // Caminho absoluto para o arquivo de teste
        String caminhoArquivo = "/home/bruno/Documentos/Universidade/Projeto de Compiladores/Trabalho/descricao/correto.pascal.txt";

        try {
            // Lê o conteúdo do arquivo
            String codigoFonte = new String(Files.readAllBytes(Paths.get(caminhoArquivo)));

            System.out.println("=== COMPILADOR LALG ===\n");
            System.out.println("Arquivo: " + caminhoArquivo);
            System.out.println("------------------------\n");

            // Cria o scanner léxico
            ScannerLexico lexer = new ScannerLexico(codigoFonte);

            // Primeiro, lista todos os tokens para visualização
            System.out.println("=== TOKENS ENCONTRADOS ===\n");
            ScannerLexico lexerVisual = new ScannerLexico(codigoFonte);
            TokenInformacoes token;
            int contadorTokens = 0;
            do {
                token = lexerVisual.proximoToken();
                System.out.println(token);
                contadorTokens++;
            } while (token.getTipo() != Token.EOF);

            System.out.println("\n>>> Total de tokens: " + contadorTokens + " <<<\n");

            // Agora faz análise sintática
            System.out.println("=== ANÁLISE SINTÁTICA ===\n");
            Parser parser = new Parser(lexer);
            parser.parse();

            // Imprime a tabela de símbolos
            parser.getTabela().imprimir();

            // Imprime o código gerado
            parser.getGerador().imprimir();
            // Executa o código na máquina virtual
            MaquinaVirtual vm = new MaquinaVirtual(parser.getGerador().getCodigo());
            vm.executar();

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }
}
