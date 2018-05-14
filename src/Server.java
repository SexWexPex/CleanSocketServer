import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;

public class Server {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8080), 0);

        server.createContext("/", new IndexHandler());

        server.setExecutor(null);
        server.start();
    }

    static class IndexHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String root = "c:/cntserver/var/www";
            URI uri = t.getRequestURI();
            System.out.println("looking for: "+ root + uri.getPath());
            String uriPath = uri.getPath();

            if (uriPath.equalsIgnoreCase("/") || uriPath.equalsIgnoreCase("/index") || uriPath.equalsIgnoreCase("/index.html")) {
                String path = "/index.html";
                File file = new File(root + path).getCanonicalFile();
                String mime = "text/html";

                Headers headers = t.getResponseHeaders();
                headers.set("Content-Type", mime);
                t.sendResponseHeaders(200, 0);

                OutputStream os = t.getResponseBody();
                FileInputStream fs = new FileInputStream(file);
                final byte[] buffer = new byte[0x10000];
                int count = 0;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer,0,count);
                }
                fs.close();
                os.close();
            }
            else if (uri.getPath().equalsIgnoreCase("/kitten")){
                String path = "/kitten.jpeg";
                File file = new File(root + path).getCanonicalFile();
                String mime = "image/jpeg";

                byte[] bytes  = new byte [(int)file.length()];

                OutputStream os = t.getResponseBody();
                FileInputStream fs = new FileInputStream(file);

                BufferedInputStream bufferedInputStream = new BufferedInputStream(fs);
                bufferedInputStream.read(bytes, 0, bytes.length);


                Headers headers = t.getResponseHeaders();
                headers.set("Content-Type", mime);
                t.sendResponseHeaders(200, file.length());
                os.write(bytes, 0, bytes.length);
                os.close();
                fs.close();
            }
            else {
                String response = "404 (Not Found)\n";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

}