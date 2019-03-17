
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Stress1 {

    public static void main(String[] args) throws IOException {
        Socket echoSocket; // la socket client
        String ip; // adresse IPv4 du serveur en notation pointée
        int port; // port TCP serveur
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out;
        BufferedReader in;
        boolean fini = false;


        /* Traitement des arguments */
        if (args.length != 3) {
            /* erreur de syntaxe */
            System.out.println("Usage: java Stress1 @server @port @ConnectionsCount");
            System.exit(1);
        }
        ip = args[0];
        port = Integer.parseInt(args[1]);
        int numberClients = Integer.parseInt(args[2]);

        if (port > 65535) {
            System.err.println("Port hors limite");
            System.exit(3);
        }

        int totalTime = 0;
        /* Connexion */
        for (int i = 0; i < numberClients; i++) {
            try {
                echoSocket = new Socket(ip, port);

                /* Initialisation d'agréables flux d'entrée/sortie */
                out = new PrintWriter(echoSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            } catch (UnknownHostException e) {
                System.err.println("Connexion: hôte inconnu : " + ip);
                e.printStackTrace();
                return;
            }

            /* Session */
            try {
                /* Jusqu'à fermeture de la socket (ou de stdin)     */
                /* recopier à l'écran ce qui est lu dans la socket  */
                /* recopier dans la socket ce qui est lu dans stdin */

                String lu;
                String tampon;

                if (fini == true) break; /* on sort de la boucle infinie */

                out.println("java client stress1 n "+i);
                long startTimeMsg = System.nanoTime();

                /* réception des données */
                lu = in.readLine();

                long responseTime = System.nanoTime() - startTimeMsg;

                if (lu == null) {
                    System.err.println("Connexion terminée par l'hôte distant");
                    break; /* on sort de la boucle infinie */
                }
                System.out.println("reçu : " + lu);
                System.out.println("response time msg : "+responseTime/1000 + " microsec");

                totalTime += responseTime;


                /* On ferme tout */
                in.close();
                out.close();
                stdin.close();
                echoSocket.close();

                System.err.println("Fin de la session.");
            } catch (IOException e) {
                System.err.println("Erreur E/S socket");
                e.printStackTrace();
                System.exit(8);
            }

        }
        System.out.println("Test fini, temps total : " + totalTime/1000 + " microSecondes");
        System.out.println("Test fini, temps total : " + totalTime/1000000 + " milliSecondes");
        return;
    }
}
