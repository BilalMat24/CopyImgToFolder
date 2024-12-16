import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    private static final String BASE_PATH = "C:\\Users\\bilal.biaz\\ProgettoVIT\\cams";
    private static final String DEST_PATH = "C:\\Users\\bilal.biaz\\ProgettoVIT\\ControlloImmagini\\nuovaImmagini\\2024\\12\\03\\";

    public static void main(String[] args) {
        File baseDir = new File(BASE_PATH);

        // Controlla se il path di origine esiste ed è una directory
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            System.out.println("La directory di origine non esiste o non è valida: " + BASE_PATH);
            return;
        }

        File[] subDirectories = baseDir.listFiles(File::isDirectory);

        if (subDirectories == null || subDirectories.length == 0) {
            System.out.println("Nessuna sottocartella trovata in " + BASE_PATH);
            return;
        }

        // Cicla tutte le sottocartelle contemporaneamente usando thread
        for (File sourceSubDir : subDirectories) {
            new Thread(() -> processSubDirectory(sourceSubDir)).start();
        }
    }

    private static void processSubDirectory(File sourceSubDir) {
        String subDirName = sourceSubDir.getName();
        File destSubDir = new File(DEST_PATH, subDirName);

        // Verifica se la directory di destinazione esiste
        if (!destSubDir.exists() || !destSubDir.isDirectory()) {
            System.out.println("La directory di destinazione non esiste: " + destSubDir.getPath());
            return;
        }

        File[] files = sourceSubDir.listFiles(File::isFile);

        if (files == null || files.length == 0) {
            System.out.println("Nessun file trovato nella cartella: " + sourceSubDir.getPath());
            return;
        }

        for (File file : files) {
            try {
                // Genera il nuovo nome del file basato sul timestamp mantenendo il formato richiesto
                File renamedFile = sostituisciNomeImmagine(file, destSubDir);

                // Copia il file con il nuovo nome nella directory corrispondente
                Files.copy(file.toPath(), renamedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                System.out.println("File copiato: " + file.getName() + " in " + renamedFile.getPath());

                // Attendi 5 secondi prima di copiare il prossimo file
                Thread.sleep(5000);
            } catch (IOException e) {
                System.out.println("Errore durante la copia del file: " + file.getPath());
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println("Thread interrotto durante il ritardo.");
                Thread.currentThread().interrupt();
            }
        }
    }

    private static File sostituisciNomeImmagine(File file, File destDir) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(new Date());
        String originalFileName = file.getName();

        // Sostituisci la parte del timestamp nel nome originale
        String[] parts = originalFileName.split("_");
        if (parts.length >= 3) {
            parts[1] = timestamp; // Aggiorna solo il timestamp centrale
        }
        String newFileName = String.join("_", parts);

        return new File(destDir, newFileName);
    }
}