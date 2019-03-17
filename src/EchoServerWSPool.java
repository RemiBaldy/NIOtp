
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class EchoServerWSPool {
    public static void main(String[] args) {
        try {
            Selector selector = Selector.open();

            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 4567);
            serverSocket.bind(hostAddress);
            serverSocket.configureBlocking(false);
            int ops = serverSocket.validOps();
            SelectionKey selectKy = serverSocket.register(selector, ops);

            ExecutorService pool = Executors.newWorkStealingPool();

            for (; ; ) {

                System.out.println("Waiting for select...");
                int noOfKeys = selector.select();

                System.out.println("Number of selected keys: " + noOfKeys);
                Set selectedKeys = selector.selectedKeys();

                for (Object key : selectedKeys) {
                    SelectionKey ky = (SelectionKey) key;
                    selectedKeys.remove(ky);
                    pool.execute(new Handler(serverSocket,ky,selector));
                }

                selectedKeys.clear();
            } // end for loop
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
   echo des messages reçus (le tout via la socket).
   NB classe Runnable : le code exécuté est défini dans la
   méthode run().
*/
    static class Handler implements Runnable {

        SelectionKey ky;
        ServerSocketChannel serverSocket;
        Selector selector;

        Handler(ServerSocketChannel serverSocket, SelectionKey ky, Selector selector) throws IOException {
            this.serverSocket = serverSocket;
            this.ky = ky;
            this.selector = selector;
        }

        public void run() {
            try {

                if (ky.isAcceptable()) {
                    System.out.println("isAcceptable");

                    // Accept the new client connection
                    SocketChannel client = null;
                    client = serverSocket.accept();

                    client.configureBlocking(false);

                    // Add the new connection to the selector
                    client.register(selector, SelectionKey.OP_READ);

                    System.out.println("Accepted new connection from client: " + client);
                } else if (ky.isReadable()) {

                    //System.out.println("isReadable");
                    // Read the data from client
                    SocketChannel client = (SocketChannel) ky.channel();

                    ByteBuffer buffer = ByteBuffer.allocate(256);
                    client.read(buffer);

                    String output = new String(buffer.array()).trim();


                    System.out.println("Message read from client: " + output);

                    buffer.flip();

                    client.write(buffer);

                    if (!buffer.hasRemaining())
                        client.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}