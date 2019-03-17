
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Stress2 {

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
            System.out.println("Usage: java EchoClient @server @port @numberClients");
            System.exit(1);
        }
        ip = args[0];
        port = Integer.parseInt(args[1]);
        int numberClients = Integer.parseInt(args[2]);

        if (port > 65535) {
            System.err.println("Port hors limite");
            System.exit(3);
        }

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

                /* réception des données */
                lu = in.readLine();
                if (lu == null) {
                    System.err.println("Connexion terminée par l'hôte distant");
                    break; /* on sort de la boucle infinie */
                }
                System.out.println("reçu: " + lu);


                if (fini == true) break; /* on sort de la boucle infinie */

                out.println("java client stress2 n "+i);
                long startTimeMsg1 = System.nanoTime();
                out.println("java client stress2 n "+i);
                long startTimeMsg2 = System.nanoTime();


                /* réception des données */
                lu = in.readLine();
                long responseTime1 = System.nanoTime() - startTimeMsg1;

                String lu2 = in.readLine();
                long responseTime2 = System.nanoTime() - startTimeMsg2;

                System.out.println("reçu: " + lu);
                System.out.println("response time msg1 : "+responseTime1/1000 + " microsec");

                System.out.println("reçu: " + lu2);
                System.out.println("response time msg2 : "+responseTime2/1000 + " microsec");


                if (lu == null) {
                    System.err.println("Connexion terminée par l'hôte distant");
                    break; /* on sort de la boucle infinie */
                }



                /* On ferme tout */
                in.close();
                out.close();
                stdin.close();
                //echoSocket.close();

                System.err.println("Fin de la session.");
            } catch (IOException e) {
                System.err.println("Erreur E/S socket");
                e.printStackTrace();
                System.exit(8);
            }

        }


        return;
    }
}
