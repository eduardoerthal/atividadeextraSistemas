import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String path = "data/programas.txt";
        if (args != null && args.length > 0) {
            path = args[0];
        }

        Buddy buddy = new Buddy();

        // leitura do arquivo
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;
                String label = parts[0];
                String sizeToken = parts[1];
                int sizeBytes = parseSizeToken(sizeToken);
                boolean ok = buddy.allocate(label, sizeBytes);
                if (ok) {
                    System.out.println(label + " alocado (" + sizeBytes + " bytes / " + (sizeBytes / 1024) + "KB)");
                } else {
                    System.out.println(label + " FALHOU (" + sizeBytes + " bytes / " + (sizeBytes / 1024) + "KB)");
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo: " + e.getMessage());
            return;
        }

        buddy.printReport();
    }


    private static int parseSizeToken(String tok) {
        tok = tok.trim().toUpperCase();
        try {
            if (tok.endsWith("KB")) {
                String num = tok.substring(0, tok.length() - 2);
                return Integer.parseInt(num) * 1024;
            } else if (tok.endsWith("K")) {
                String num = tok.substring(0, tok.length() - 1);
                return Integer.parseInt(num) * 1024;
            } else if (tok.endsWith("MB")) {
                String num = tok.substring(0, tok.length() - 2);
                return Integer.parseInt(num) * 1024 * 1024;
            } else if (tok.endsWith("M")) {
                String num = tok.substring(0, tok.length() - 1);
                return Integer.parseInt(num) * 1024 * 1024;
            } else {
                return Integer.parseInt(tok);
            }
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
