import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int MAX_CONNECTIONS = 10;
    private static final int TIMEOUT_SECONDS = 5;
    public static void main(String[] args) throws IOException {

        String[] root_and_port = parseArguments(args);
        if(root_and_port == null) {
            System.out.println("Please check your input arguments");
            return;
        }
        System.out.println(root_and_port[0]);
        int port = Integer.parseInt(root_and_port[1]);
        if(port < 8000 || port > 9999){
            System.out.println("Port number must be in 8000~9999");
            return;
        }
        ServerSocket server = null;
        try {
            //Establish the listen socket to listen fo incoming connections
            server = new ServerSocket(port);
            System.out.println("Server is listening on port: " + port);

            while (true) {
                //Socket object to receive incoming client requests
                Socket client = server.accept();
                int connectionCount = Thread.activeCount();
                //Displaying that new client is connected to server
                System.out.println("Accepted connection from " + client.getInetAddress() + ":" + client.getPort() + " (total connections: " + connectionCount + ")");
                // Create a new thread object
                HandleRequest handleRequest = new HandleRequest(server, client, root_and_port[0], connectionCount);
                // This thread will handle the client separately
                handleRequest.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(server != null){
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }
    private static String[] parseArguments(String[] args){
        String root_path = "", port_number = "";
        if(args == null || args.length == 0 || args.length > 4) {
            System.out.println("Please input correct arguments");
        }

        if(args[0].equals("-document_root")) root_path = args[1];
        else {
            System.out.println("Please input '-document_root' for receiving root path");
            return null;
        }

        if(args[2].equals("-port")) port_number = args[3];
        else{
            System.out.println("Please input '-port' for receiving port number");
            return null;
        }
        return new String[]{root_path, port_number};
    }
}
