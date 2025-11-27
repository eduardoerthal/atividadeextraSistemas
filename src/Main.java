import java.nio.file.*;
import java.util.List;

public class Main {

    public static int parseSizeKB(String token) {
        token = token.trim();
        int valor = Integer.parseInt(token);
        return valor * 1024; // converte sempre para BYTES
    }

    public static void main(String[] args) throws Exception {

        Buddy buddy = new Buddy();

        // busca e le meu arquivo de programas
        List<String> lines = Files.readAllLines(Paths.get("data/programas.txt"));

        int i = 0;
        while (i < lines.size()) {

            String line = lines.get(i).trim();
            i = i + 1;

            // comando para ignorar linhas em branco e comentarios que usei #
            if (line.length() == 0) continue;
            if (line.startsWith("#")) continue;

            // pega programa + tamanho
            String[] parts = line.split("\\s+");

            String label = parts[0];      // A, B, C e etc..
            int sizeBytes = parseSizeKB(parts[1]);

            boolean ok = buddy.allocate(label, sizeBytes);

            if (ok)
                System.out.println(label + " alocado (" + sizeBytes + " bytes / " + (sizeBytes / 1024) + "KB)");
            else
                System.out.println(label + " FALHOU (" + sizeBytes + " bytes / " + (sizeBytes / 1024) + "KB)");
        }

        // report final para o alocador
        buddy.printReport();
    }
}
