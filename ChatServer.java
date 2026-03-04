import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) throws Exception {

        System.out.println("Server started...");

        ServerSocket listener = new ServerSocket(5000);

        try {
            while (true) {
                new ClientHandler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class ClientHandler extends Thread {

        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            try {

                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;

                while ((message = in.readLine()) != null) {

                    if (message.equalsIgnoreCase("/time")) {
                        out.println("Server Time: " + new Date());
                    }

                    else if (message.equalsIgnoreCase("/count")) {
                        synchronized (clientWriters) {
                            out.println("Connected Clients: " + clientWriters.size());
                        }
                    }

                    else if (message.equalsIgnoreCase("/exit")) {
                        out.println("Disconnecting...");
                        break;
                    }

                    else {
                        synchronized (clientWriters) {
                            for (PrintWriter writer : clientWriters) {
                                writer.println(message);
                            }
                        }
                    }

                }

            } catch (Exception e) {
                System.out.println(e);
            }

            finally {

                if (out != null) {
                    clientWriters.remove(out);
                }

                try {
                    socket.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
