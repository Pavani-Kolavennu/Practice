
package com.library;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookController {

    @Value("${server.port}")
    private int port;

    @GetMapping("/books")
    public Map<String, Object> getBooks() {

        List<Map<String, Object>> books = List.of(
            Map.of("id", 1, "title", "Java Programming"),
            Map.of("id", 2, "title", "Microservices Architecture"),
            Map.of("id", 3, "title", "Spring Boot Basics")
        );

        return Map.of(
            "instancePort", port,
            "books", books
        );
    }
}
