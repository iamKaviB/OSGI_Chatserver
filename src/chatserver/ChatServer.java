package chatserver;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * IT NO : IT19968216
 * NAME : D.P.K.D Balasooriya
 */

public class ChatServer {

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 9001;


    /**
     * Create map to store names and print writers
     * this map is thread safe
     */
    private static Map<String , PrintWriter> nameWriters = new HashMap();

    /**
     * The application main method, which just listens on a port and
     * spawns handler threads.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
            	Socket socket  = listener.accept();
                Thread handlerThread = new Thread(new Handler(socket));
                handlerThread.start();
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;


        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and broadcasts them.
         */
        public void run() {
            try {

                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // handle client register request
                while (true) {
                    out.println("SUBMITNAME ");
                    name = in.readLine().substring(11);
                    if (name == null) {
                        return;
                    }

                    if (register(name.split(" ")[0])) {
                        break;
                    }

                }


                // Accept messages from this client
                // handle broadcast messages and peer to peer messages
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }else if(input.startsWith("BROADCAST ")){
                        broadcast(name , input.substring(9));
                    }else if(input.startsWith("DIRECTMESSAGE ")){
                        String message = in.readLine().substring(8);

                        // to be direct message user list
                        String sendToBe[] = input.substring(14).split(" ");

                        direct(sendToBe , message);
                    }
                }

            } catch (IOException e) {
                System.out.println(e);
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                if (name != null) {
                    nameWriters.remove(name);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }


        /**
         * @param name - client name
         * @param input - message
         * broadcast messages to all users
         */
        public void broadcast(String name , String input){
            for(String key : nameWriters.keySet()){
                nameWriters.get(key).println("BROADCAST "+ name + ": " + input);
            }
        }

        /**
         *
         * @param names - recievers
         * @param message - message
         * @throws IOException
         * peer to peeer
         */
        public void direct(String names[] , String message) throws IOException {
            for(String key : names){
                nameWriters.get(key).println("MESSAGE "+ name + ": " + message);
            }

        }

        /**
         * registering an user
         */
        public void getUserList(){
            String nameList = String.join(" " , nameWriters.keySet());
            for (String key : nameWriters.keySet()){
                    nameWriters.get(key).println("NAMELIST "+ nameList);
            }

        }

        /**
         *
         * @param name - client
         * @return if user had registered or not
         * register new user
         * nameWriter hashmap is threadsafe
         */
        private boolean register(String name){
            if (!nameWriters.containsKey(name)) {
                out.println("NAMEACCEPT ");
                synchronized (nameWriters){
                    nameWriters.put(name, out);
                }
                broadcast("SERVER" , name + " is now connected");
                getUserList();
                return true;
            }
            return false;
        }

    }
}