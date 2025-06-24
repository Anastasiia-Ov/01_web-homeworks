package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    private int port;
    private final List<String> validPaths = List.of(
            "/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html",
            "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final int POOL_SIZE = 64;
    private final Map<String, Map<String, Handler>> handlers = new HashMap<>();


//    public Server(List<String> validPaths) {
//        this.validPaths.addAll(validPaths);
//    }

    // метод запуска
    public void startServer() {
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(POOL_SIZE);
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    threadPool.submit(() -> {
                        try {
                            processConnection(socket);
                        } finally {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // метод обработки конкретного подключения
    public void processConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
            // must be in form GET /path HTTP/1.1
            final String requestLine = in.readLine();
            final String[] parts = requestLine.split(" ");

            if (parts.length != 3) return;

            final String method = parts[0];
            final String path = parts[1];

            Map<String, String> headers = new HashMap<>();
            String line;
            while (!(line = in.readLine()).isEmpty()) {
                int colonPos = line.indexOf(":");
                if (colonPos != -1) {
                    String headerName = line.substring(0, colonPos).trim();
                    String headerValue = line.substring(colonPos + 1).trim();
                    headers.put(headerName.toLowerCase(), headerValue);
                }
            }

            String body = "";
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                String contentLengthStr = headers.get("content-length");
                if (contentLengthStr != null) {
                    int contentLength = Integer.parseInt(contentLengthStr);
                    char[] bodyChars = new char[contentLength];
                    in.read(bodyChars);
                    body = new String(bodyChars);
                }
            }

            Request request = new Request(method, path, headers, body);

            Handler handler = null;
            Map<String, Handler> methodHandlers = handlers.get(request.getMethod().toUpperCase());
            if (methodHandlers != null) {
                handler = methodHandlers.get(request.getPath());
            }

            if (handler != null) {
                handler.handle(request, out);
            } else {
                // Отправка 404 Not Found
                String responseBody = "404 Not Found";
                out.write(("HTTP/1.1 404 Not Found\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + responseBody.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n").getBytes());
                out.write(responseBody.getBytes());
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method.toUpperCase(), k -> new HashMap<>()).put(path, handler);
    }

    public void listen(int port) {
        this.port = port;
        startServer();
    }
}
