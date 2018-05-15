
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class SocketServer {
    public static void main(String args[]) {
        int port;
        ServerSocket server_socket;
        try {
            port = 8080;
        } catch (Exception e) {
            port = 1500;
        }
        try {

            server_socket = new ServerSocket(port);
            System.out.println("SocketServer started on port: "
                    + server_socket.getLocalPort());

            while (true) {
                Socket socket = server_socket.accept();
                System.out.println("New connection accepted on: "
                        + socket.getInetAddress() + ":" + socket.getPort());

                try {
                    httpRequestHandler request = new httpRequestHandler(socket);

                    Thread thread = new Thread(request);


                    thread.start();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

class httpRequestHandler implements Runnable {
    final static String CRLF = "\r\n";

    Socket socket;

    InputStream input;

    OutputStream output;

    BufferedReader br;

    public httpRequestHandler(Socket socket) throws Exception {
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.br = new BufferedReader(new InputStreamReader(socket
                .getInputStream()));
    }

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {
        while (true) {

            String headerLine = br.readLine();
            if (headerLine.equals(CRLF) || headerLine.equals(""))
                break;

            StringTokenizer s = new StringTokenizer(headerLine);
            String temp = s.nextToken();

            if (temp.equals("GET")) {



                String fileName = s.nextToken();
                String url = fileName;

                String root = "src/var/www";



                String serverLine = "Server: Alexey's Java Socket Server" + CRLF;
                String statusLine = null;
                String contentTypeLine = null;

                String contentLengthLine = "error" + CRLF;

                if (url.equalsIgnoreCase("/")
                        || url.equalsIgnoreCase("/index.html")){

                    FileInputStream fis = new FileInputStream(root + "/index.html");

                    statusLine = "HTTP/1.1 200 OK" + CRLF;
                    contentTypeLine = "Content-type: text/html" + CRLF;
                    contentLengthLine = "Content-Length: "
                            + (new Integer(fis.available())).toString() + CRLF;

                    outWriter(200,statusLine,serverLine,contentTypeLine, contentLengthLine, fis);
                }
                else if(url.equalsIgnoreCase("/kitten") || url.equalsIgnoreCase("/kitten.jpeg")){
                    FileInputStream fis = new FileInputStream(root + "/kitten.jpeg");

                    statusLine = "HTTP/1.1 200 OK" + CRLF;
                    contentTypeLine = "Content-type: image/jpeg" + CRLF;
                    contentLengthLine = "Content-Length: "
                            + (new Integer(fis.available())).toString() + CRLF;

                    outWriter(200,statusLine,serverLine,contentTypeLine, contentLengthLine, fis);
                }
                else {
                    FileInputStream fis = null;
                    statusLine = "HTTP/1.1 404 Not Found" + CRLF;
                    contentTypeLine = "Content-type: text/html" + CRLF;

                    outWriter(404,statusLine,serverLine,contentTypeLine, contentLengthLine, fis);
                }

            }
        }
        try {
            output.close();
            br.close();
            socket.close();
        } catch (Exception e) {
        }
    }

    private static void sendBytes(FileInputStream fis, OutputStream os)
            throws Exception {

        byte[] buffer = new byte[1024];
        int bytes = 0;

        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private void outWriter(int responseCode, String statusLine,
                           String serverLine, String contentTypeLine,
                           String contentLengthLine, FileInputStream fis) throws Exception {

        output.write(statusLine.getBytes());
        output.write(serverLine.getBytes());
        output.write(contentTypeLine.getBytes());
        output.write(contentLengthLine.getBytes());
        output.write(CRLF.getBytes());

        if (responseCode == 200){
            sendBytes(fis, output);
            fis.close();
        }
        else if (responseCode == 404){
            String entityBody = "<HTML>"
                    + "<HEAD><TITLE>Hey, Buddy!</TITLE></HEAD>"
                    + "<BODY><h1 style=\"text-align: center;\"><strong>404 Not Found</strong></h1></BODY></HTML>";

            output.write(entityBody.getBytes());
        }
    }
}