package com.example.bookstore.author;

import com.example.bookstore.author.dto.AuthorDto;
import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.common.ItemsPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ItemsPayload<AuthorDto>>> list() {
        List<AuthorDto> items = authorService.list();
        return ResponseEntity.ok(ApiResponse.success("OK", new ItemsPayload<>(items)));
    }
}
