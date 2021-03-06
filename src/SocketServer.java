
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
        int port ;
        ServerSocket serverSocket;
        if (args.length < 1){
            System.out.println("Please, specify port");
            System.exit(-1);
        }
        port = Integer.parseInt(args[0]);

        try {

            serverSocket = new ServerSocket(port);
            System.out.println("SocketServer started on port: "
                    + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
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
                fileName = "var/www" + fileName;

                FileInputStream fis = null;
                boolean fileExists = true;
                try {
                    fis = new FileInputStream(fileName);
                } catch (FileNotFoundException e) {
                    fileExists = false;
                }


                String serverLine = "Server: Alexey's Java Socket Server" + CRLF;
                String statusLine = null;
                String contentTypeLine = null;
                String entityBody = null;
                String contentLengthLine = "error" + CRLF;

                if (url.equalsIgnoreCase("/")) {
                    fis = new FileInputStream("var/www/index.html");

                    statusLine = "HTTP/1.1 200 OK" + CRLF;
                    contentTypeLine = "Content-type: text/html" + CRLF;
                    contentLengthLine = "Content-Length: "
                            + (new Integer(fis.available())).toString() + CRLF;
                    outWriter(200,statusLine,serverLine,contentTypeLine, contentLengthLine, fis);
                } else if (fileExists) {
                    statusLine = "HTTP/1.1 200 OK" + CRLF;
                    contentTypeLine = "Content-type: " + contentType(fileName)
                            + CRLF;
                    contentLengthLine = "Content-Length: "
                            + (new Integer(fis.available())).toString() + CRLF;
                    outWriter(200,statusLine,serverLine,contentTypeLine, contentLengthLine, fis);
                } else {
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

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")
                || fileName.endsWith(".txt")) {
            return "text/html";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "application/octet-stream";
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