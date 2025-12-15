package com.example.bookstore.author;

import com.example.bookstore.author.dto.AuthorDto;
import com.example.bookstore.author.dto.AuthorUpsertRequest;
import com.example.bookstore.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/authors")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuthorController {

    private final AuthorService authorService;

    public AdminAuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(@Valid @RequestBody AuthorUpsertRequest req) {
        AuthorDto dto = authorService.create(req);
        return ResponseEntity.ok(ApiResponse.success("생성됨.", java.util.Map.of("author", dto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> patch(@PathVariable Long id, @Valid @RequestBody AuthorUpsertRequest req) {
        AuthorDto dto = authorService.patch(id, req);
        return ResponseEntity.ok(ApiResponse.success("수정됨.", java.util.Map.of("author", dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
