
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
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
                    SelectionKey selectionKey = (SelectionKey) key;
                    //selectedKeys.remove(key);

                    if(selectionKey.isValid()){

                        if (selectionKey.isAcceptable()) {

                            //System.out.println("isAcceptable");

                            SocketChannel client = serverSocket.accept();
                            client.configureBlocking(false);

                            client.register(selector, SelectionKey.OP_READ);

                            System.out.println("Connexion acceptée client : " + client);

                        }
                        else if(selectionKey.isReadable()){
                            pool.execute(new HandlerReader(serverSocket,selectionKey,selector));
                        }
                    }

                   // pool.execute(new Handler(serverSocket,selectionKey,selector));
                }

                selectedKeys.clear();
            } // end for loop
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    static class HandlerAccept implements Runnable {

        SelectionKey selectionKey;
        ServerSocketChannel serverSocket;
        Selector selector;

        HandlerAccept(ServerSocketChannel serverSocket, SelectionKey selectionKey, Selector selector) throws IOException {
            this.serverSocket = serverSocket;
            this.selectionKey = selectionKey;
            this.selector = selector;
        }

        public void run() {
            try {


                System.out.println("isAcceptable");

                // Accept the new client connection
                SocketChannel client = null;
                client = serverSocket.accept();

                if (client == null) {
                    System.out.println("client null");
                    return;
                }

                client.configureBlocking(false);

                // Add the new connection to the selector
                client.register(selector, SelectionKey.OP_READ);

                System.out.println("Accepted new connection from client: " + client);

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }


    static class HandlerReader implements Runnable {

        SelectionKey selectionKey;
        ServerSocketChannel serverSocket;
        Selector selector;

        HandlerReader(ServerSocketChannel serverSocket, SelectionKey selectionKey, Selector selector) throws IOException {
            this.serverSocket = serverSocket;
            this.selectionKey = selectionKey;
            this.selector = selector;
        }

        public void run() {
            try {
                SocketChannel client = (SocketChannel) selectionKey.channel();

                ByteBuffer buffer = ByteBuffer.allocate(256);

                client.read(buffer);

                String output = new String(buffer.array()).trim();

                System.out.println("Message read from client: " + output);

                buffer.flip();

                //client.write(buffer);

                if (!buffer.hasRemaining())
                    client.close();

            }
            catch (ClosedChannelException e) {
                System.out.println("close channel");
                e.printStackTrace();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }


    /*
   echo des messages reçus (le tout via la socket).
   NB classe Runnable : le code exécuté est défini dans la
   méthode run().
*/
    static class Handler implements Runnable {

        SelectionKey selectionKey;
        ServerSocketChannel serverSocket;
        Selector selector;

        Handler(ServerSocketChannel serverSocket, SelectionKey selectionKey, Selector selector) throws IOException {
            this.serverSocket = serverSocket;
            this.selectionKey = selectionKey;
            this.selector = selector;
        }

        public void run() {
            try {

                if (/*selectionKey.isValid() &&*/ selectionKey.isAcceptable()) {
                    System.out.println("isAcceptable");

                    // Accept the new client connection
                    SocketChannel client = null;
                    client = serverSocket.accept();

                    if(client == null) {
                        System.out.println("client null");
                        return;
                    }

                    client.configureBlocking(false);

                    // Add the new connection to the selector
                    client.register(selector, SelectionKey.OP_READ);

                    System.out.println("Accepted new connection from client: " + client);

                } else if (/*selectionKey.isValid() &&*/ selectionKey.isReadable()) {

                    //System.out.println("isReadable");
                    // Read the data from client
                    SocketChannel client = (SocketChannel) selectionKey.channel();

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
