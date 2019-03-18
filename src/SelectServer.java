import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;

public class SelectServer {

    public static void main(String[] args) {

        try {
            Selector selector = Selector.open();

            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 4567);
            serverSocket.bind(hostAddress);
            serverSocket.configureBlocking(false);
            int ops = serverSocket.validOps();
            SelectionKey selectKy = serverSocket.register(selector, ops);

            for (; ; ) {

                System.out.println("Select (bloquant)... en attente de requêtes client");
                int noOfKeys = selector.select();

                System.out.println("Nombre de clés : " + noOfKeys);
                Set selectedKeys = selector.selectedKeys();

                for(Object key : selectedKeys){
                    SelectionKey selectionKey = (SelectionKey) key;
                    selectedKeys.remove(key);

                    if (selectionKey.isAcceptable()) {

                        //System.out.println("isAcceptable");

                        SocketChannel client = serverSocket.accept();
                        client.configureBlocking(false);

                        client.register(selector, SelectionKey.OP_READ);

                        System.out.println("Connexion acceptée client : " + client);

                    } else if (selectionKey.isReadable()) {

                        //System.out.println("isReadable");

                        SocketChannel client = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        client.read(buffer);

                        String output = new String(buffer.array()).trim();

                        System.out.println("Message du client : " + output);

                        buffer.flip();

                        client.write(buffer);

                        if(!buffer.hasRemaining())
                            client.close();
                    }
                }
                selectedKeys.clear();
            } // end for loop
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
