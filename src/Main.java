import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.file.*;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static final String USERS_FILE = "../data/users.txt"; // chemin relatif
    static final String FILES_FOLDER = "../files/";

    public static void main(String[] args) throws IOException {
        // Crée dossier data si absent
        File dataDir = new File("../data");
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        // Crée dossier files si absent
        File filesDir = new File(FILES_FOLDER);
        if (!filesDir.exists()) {
            filesDir.mkdir();
        }

        // Crée fichier users.txt s’il n’existe pas
        File usersFile = new File(USERS_FILE);
        if (!usersFile.exists()) {
            usersFile.createNewFile();
        }

        System.out.println("Bienvenue dans l'application !");
        System.out.println("1. Créer un compte");
        System.out.println("2. Se connecter");

        int choix = scanner.nextInt();
        scanner.nextLine(); // consommer le retour à la ligne

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

        // Lancer le serveur HTTP à la fin
        new Thread(() -> startHttpServer()).start();
    }

    public static void registerUser() throws IOException {
        System.out.print("Nom d'utilisateur : ");
        String username = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String password = scanner.nextLine();

        FileWriter fw = new FileWriter(USERS_FILE, true);
        fw.write(username + ":" + password + "\n");
        fw.close();

        System.out.println("Compte créé avec succès !");
    }

    public static boolean loginUser() throws IOException {
        System.out.print("Nom d'utilisateur : ");
        String username = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String password = scanner.nextLine();

        BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password>
                reader.close();
                return true;
            }
        }
        reader.close();
        return false;
    }

    public static void showFiles() {
        File folder = new File(FILES_FOLDER);
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Le dossier " + FILES_FOLDER + " n'existe pas.");
            return;
        }
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

        // Suppression de l'ouverture locale du fichier (pas d'interface graphique)
        // Desktop.getDesktop().open(selectedFile); // supprimé

        // Affiche lien HTTP pour téléchargement
        System.out.println("Lien de téléchargement : http://3.85.234.48:8088/" + selectedF>
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
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getI>
            OutputStream out = clientSocket.getOutputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine == null || !requestLine.startsWith("GET")) return;

            String[] parts = requestLine.split(" ");
            String path = URLDecoder.decode(parts[1], "UTF-8");
            String filePath = FILES_FOLDER + path.substring(1); // retire le '/'

            File file = new File(filePath);
            if (file.exists() && !file.isDirectory()) {
                byte[] content = Files.readAllBytes(file.toPath());
                String header = "HTTP/1.1 200 OK\r\nContent-Length: " + content.length + ">
                out.write(header.getBytes());
                out.write(content);
            } else {
                String notFound = "HTTP/1.1 404 Not Found\r\n\r\nFichier introuvable.";
                out.write(notFound.getBytes());
            }
        } catch (IOException e) {
            System.out.println("Erreur de traitement : " + e.getMessage());
        }
    }
}





