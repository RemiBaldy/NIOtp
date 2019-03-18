
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class EchoServerWSPool {
    public static void main(String[] args) {
        try {
            Selector selector = Selector.open();

            HashMap<SelectionKey,Boolean> listTreatedThread = new HashMap<>();

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

                        listTreatedThread.putIfAbsent(selectionKey, false);

                        if (selectionKey.isAcceptable()) {


                            //System.out.println("isAcceptable");

                            SocketChannel client = serverSocket.accept();
                            client.configureBlocking(false);

                            client.register(selector, SelectionKey.OP_READ);

                            System.out.println("Connexion acceptée client : " + client);

                        }
                        else if(selectionKey.isReadable()){
                            //System.out.println("is Readable");
                            if(listTreatedThread.get(selectionKey)) {
                                //System.out.println("clé deja en traitement dans un thread");
                                break;
                            }
                            listTreatedThread.replace(selectionKey,true);

                            pool.execute(new HandlerReader(serverSocket,selectionKey,selector));
                        }
                    }

                }
                selectedKeys.clear();

            } // end for loop
        } catch (IOException e) {
            e.printStackTrace();
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

                client.write(buffer);

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



}
