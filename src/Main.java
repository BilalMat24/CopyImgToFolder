import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    private static final String BASE_PATH = "C:\\Users\\bilal.biaz\\ProgettoVIT\\cams";
    private static final String DEST_PATH = "C:\\Users\\bilal.biaz\\ProgettoVIT\\ControlloImmagini\\nuovaImmagini\\2024\\12\\16\\";

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
                File renamedFile = sostituisciNomeImmagine(file, destSubDir.getPath(), sourceSubDir.getName());

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

    private static File sostituisciNomeImmagine(File file, String destPath, String sourceSubDirName) {
        // Parte iniziale fissa
        String fixedPrefix = "2024725000";

        // Generazione del timestamp attuale
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(new Date());

        // Estrazione del nome del file originale
        String originalName = file.getName();
        String camPart = extractCamPart(originalName, sourceSubDirName); // Passiamo il fallback della cartella

        // Composizione del nuovo nome
        String newFileName = String.format("%s_%s_%s.jpg", fixedPrefix, timestamp, camPart);

        // Creazione del nuovo file nella directory di destinazione
        return new File(destPath, newFileName);
    }


    // Metodo di supporto per estrarre la parte "camXXX" dal nome originale
    private static String extractCamPart(String originalName, String sourceSubDirName) {
        String[] parts = originalName.split("_");

        // Si assume che la parte "camXXX" sia sempre l'ultima parte valida prima dell'estensione
        String lastPart = parts[parts.length - 1];

        // Rimuove l'estensione, se presente
        if (lastPart.contains(".")) {
            lastPart = lastPart.substring(0, lastPart.lastIndexOf('.'));
        }

        // Controllo: restituisce solo se è nel formato "camXXX", altrimenti usa il nome della cartella
        return lastPart.matches("cam\\d+") ? lastPart : sourceSubDirName;
    }


}