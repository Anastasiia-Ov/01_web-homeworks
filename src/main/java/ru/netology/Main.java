package ru.netology;

import java.util.List;

public class Main {
  public static void main(String[] args) {
//    final List<String> validPaths = List.of(
//            "/index.html", "/spring.svg", "/spring.png",
//            "/resources.html", "/styles.css", "/app.js", "/links.html",
//            "/forms.html", "/classic.html", "/events.html", "/events.js");
    Server server = new Server();

    // Обработчик GET /messages
    server.addHandler("GET", "/messages", (request, out) -> {
      String responseBody = "Hello from /messages!";
      out.write(("HTTP/1.1 200 OK\r\n" +
              "Content-Type: text/plain\r\n" +
              "Content-Length: " + responseBody.length() + "\r\n" +
              "Connection: close\r\n" +
              "\r\n").getBytes());
      out.write(responseBody.getBytes());
    });

    // Обработчик POST /messages
    server.addHandler("POST", "/messages", (request, out) -> {
      String responseBody = "Received message: " + request.getBody();
      out.write(("HTTP/1.1 200 OK\r\n" +
              "Content-Type: text/plain\r\n" +
              "Content-Length: " + responseBody.length() + "\r\n" +
              "Connection: close\r\n" +
              "\r\n").getBytes());
      out.write(responseBody.getBytes());
    });

    server.listen(8379);
  }
}

