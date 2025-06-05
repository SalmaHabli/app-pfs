import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static final String DATA_DIR = System.getProperty("user.dir") + "/data";
    static final String FILES_DIR = System.getProperty("user.dir") + "/files";
    static final String USERS_FILE = DATA_DIR + "/users.txt";

    public static void main(String[] args) throws IOException {
        // Crée dossiers data et files s'ils n'existent pas
        new File(DATA_DIR).mkdirs();
        new File(FILES_DIR).mkdirs();

        // Crée fichier users.txt s'il n'existe pas
        File usersFile = new File(USERS_FILE);
        if (!usersFile.exists()) {
            usersFile.createNewFile();
        }

        System.out.println("Bienvenue dans l'application !");
        System.out.println("1. Créer un compte");
        System.out.println("2. Se connecter");

        int choix = scanner.nextInt();
        scanner.nextLine(); // consomme le retour à la ligne

        if (choix == 1) {
            registerUser();
            if (loginUser()) {
                showFiles();
            }
        } else if (choix == 2) {
            if (loginUser()) {
                showFiles();
            } else {
                System.out.println("Identifiants incorrects.");
            }
        }

        // Lance serveur HTTP dans un thread séparé
        new Thread(() -> startHttpServer()).start();
    }

    public static void registerUser() throws IOException {
        System.out.print("Nom d'utilisateur : ");
        String username = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String password = scanner.nextLine();

        try (FileWriter fw = new FileWriter(USERS_FILE, true)) {
            fw.write(username + ":" + password + "\n");
        }

        System.out.println("Compte créé avec succès !");
    }

    public static boolean loginUser() throws IOException {
        System.out.print("Nom d'utilisateur : ");
        String username = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String password = scanner.nextLine();

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void showFiles() {
        File folder = new File(FILES_DIR);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Le dossier " + FILES_DIR + " n'existe pas.");
            return;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("Aucun fichier disponible.");
            return;
        }

        System.out.println("Fichiers disponibles :");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }

        System.out.print("Choisissez un fichier à ouvrir (numéro) : ");
        int choix = scanner.nextInt();
        scanner.nextLine();

        if (choix < 1 || choix > files.length) {
            System.out.println("Choix invalide.");
            return;
        }

        File selectedFile = files[choix - 1];

        // Affiche lien HTTP pour téléchargement (change l'IP si besoin)
        System.out.println("Lien de téléchargement : http://3.85.234.48:8088/" + selectedFile.getName());
    }

    public static void startHttpServer() {
        try (ServerSocket serverSocket = new ServerSocket(8088)) {
            System.out.println("Serveur HTTP actif sur le port 8088...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleRequest(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Erreur serveur HTTP : " + e.getMessage());
        }
    }

    public static void handleRequest(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || !requestLine.startsWith("GET")) return;

            String[] parts = requestLine.split(" ");
            String path = URLDecoder.decode(parts[1], "UTF-8");
            String filePath = FILES_DIR + path;

            File file = new File(filePath);
            if (file.exists() && !file.isDirectory()) {
                byte[] content = Files.readAllBytes(file.toPath());
                String header = "HTTP/1.1 200 OK\r\nContent-Length: " + content.length + "\r\n\r\n";
                out.write(header.getBytes());
                out.write(content);
            } else {
                String notFound = "HTTP/1.1 404 Not Found\r\n\r\nFichier introuvable.";
                out.write(notFound.getBytes());
            }
        } catch (IOException e) {
            System.out.println("Erreur de traitement : " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
        }
    }
}
