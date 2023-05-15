import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public class HandleRequest extends Thread{
    Socket client;
    String root_path;
    ServerSocket server;
    private static final int MAX_CONNECTIONS = 10;
    private static int TIMEOUT_SECONDS;
    private long startTime = System.currentTimeMillis();
    private boolean isRunning = false;
    private static final String CRLF = "\r\n";

    public HandleRequest(ServerSocket server, Socket client, String root_path, int connectionCount) {
        this.server = server;
        this.client = client;
        this.root_path = root_path;
        if (connectionCount > MAX_CONNECTIONS) TIMEOUT_SECONDS = 1;
        else if (connectionCount >= 5) TIMEOUT_SECONDS = 3;
        else TIMEOUT_SECONDS = 5;
    }


    @Override
    public void run() {
        if(isRunning){
            System.out.println("This thread" + client.getRemoteSocketAddress()+ "is running!");
            return;
        }else{
            isRunning = true;
        }
        BufferedReader input = null;
        BufferedOutputStream outputStream = null;
        try {
            while (isRunning) {
                if (System.currentTimeMillis() - startTime > TIMEOUT_SECONDS * 1000) {
                    isRunning = false;
                    System.out.println("Connection Timeout...");
                    break;
                }
                // get the input_stream of client
                input = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
                if(input.ready()){
                    // get the first line in Http request
                    String line = input.readLine();
                    if(line == null || line.length() == 0) return;
                    System.out.println("Received request at "+client.getRemoteSocketAddress()+" : " + line);
                    StringTokenizer tokens = new StringTokenizer(line);
                    String method = tokens.hasMoreTokens() ? tokens.nextToken() : "*";
                    String resource = tokens.hasMoreTokens() ? tokens.nextToken() : "*";
                    String version = tokens.hasMoreTokens() ? tokens.nextToken() : "*";

                    Path file_path = this.getFilePath(resource);
                    System.out.println("Path:"+resource);
                    if(version.equals("HTTP/1.0")){
                        if(method.equals("GET") && Files.isReadable(file_path)){
                            System.out.println("200 OK");
                            String content_type = getContentType(file_path);
                            sendResponse(version, "200 OK", content_type, Files.readAllBytes(file_path), outputStream);
                        }
                        else if (!method.equals("GET") || resource.charAt(0) != '/') {
                            System.out.println("400 Bad Request");
                            sendResponse(version,"400 Bad Request", "text/html", "<h1>400 Bad Request</h1>".getBytes(), outputStream);
                        }
                        else if(!Files.exists(file_path)){
                            System.out.println("404 Not Found");
                            sendResponse(version,"404 Not found", "text/html", "<h1>404 Not Found</h1>".getBytes(), outputStream);
                        }
                        else if (!Files.isReadable(file_path)){
                            System.out.println("403 Forbidden");
                            sendResponse(version,"403 Forbidden", "text/html", "<h1>403 Forbidden</h1>".getBytes(), outputStream);
                        }
                        isRunning = false;
                        break;
                    }else{
                        if(method.equals("GET") && version.equals("HTTP/1.1") && Files.isReadable(file_path)){
                            System.out.println("200 OK");
                            String content_type = getContentType(file_path);
                            sendResponse(version,"200 OK", content_type, Files.readAllBytes(file_path), outputStream);
                        }
                        else if (!method.equals("GET") || resource.charAt(0) != '/') {
                            System.out.println("400 Bad Request");
                            sendResponse(version,"400 Bad Request", "text/html", "<h1>400 Bad Request</h1>".getBytes(), outputStream);
                        }
                        else if(!Files.exists(file_path)){
                            System.out.println("404 Not Found");
                            sendResponse(version,"404 Not found", "text/html", "<h1>404 Not Found</h1>".getBytes(), outputStream);
                        }
                        else if (!Files.isReadable(file_path)){
                            System.out.println("403 Forbidden");
                            sendResponse(version,"403 Forbidden", "text/html", "<h1>403 Forbidden</h1>".getBytes(), outputStream);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(input != null) input.close();
                if(outputStream != null) outputStream.close();
                if (client != null) {
                    client.close();
                    System.out.println("Client disconnected: " + client.getRemoteSocketAddress()+CRLF);
                }
                isRunning = false;
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    // return response
    private void sendResponse(String version, String status, String contentType, byte[] content, BufferedOutputStream clientOutput)
            throws IOException {
        // get output from the client
        StringBuilder sb = new StringBuilder();
        clientOutput = new BufferedOutputStream(this.client.getOutputStream());
        clientOutput.write((version+" " + status + CRLF).getBytes());
        clientOutput.write(("Content-Type: " + contentType + CRLF).getBytes());
        clientOutput.write(("Content-Length: " + content.length + CRLF).getBytes());
        clientOutput.write(("Date: "+(new java.util.Date()).toString()+CRLF).getBytes());
        clientOutput.write(CRLF.getBytes());
        clientOutput.write(content);
        clientOutput.write(CRLF.getBytes());
        clientOutput.flush();
        sb.append(("HTTP/1.1 " + status + CRLF));
        sb.append(("Content-Type: " + contentType + CRLF));
        sb.append(("Content-Length: " + content.length + CRLF));
        sb.append(("Date: " + (new java.util.Date()).toString())+CRLF);
        System.out.println(sb.toString());
    }

    private String getContentType(Path filePath) throws IOException {
        return Files.probeContentType(filePath);
    }
    private Path getFilePath(String path){
        if("/".equals(path)) path = "/index.html";
        String abs_path = this.root_path + path;
        return Paths.get(abs_path);
    }
}
