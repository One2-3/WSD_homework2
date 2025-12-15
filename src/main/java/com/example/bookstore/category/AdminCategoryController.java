package com.example.bookstore.category;

import com.example.bookstore.category.dto.CategoryDto;
import com.example.bookstore.category.dto.CategoryUpsertRequest;
import com.example.bookstore.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(@Valid @RequestBody CategoryUpsertRequest req) {
        CategoryDto dto = categoryService.create(req);
        return ResponseEntity.ok(ApiResponse.success("생성됨.", java.util.Map.of("category", dto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> patch(@PathVariable Long id, @Valid @RequestBody CategoryUpsertRequest req) {
        CategoryDto dto = categoryService.patch(id, req);
        return ResponseEntity.ok(ApiResponse.success("수정됨.", java.util.Map.of("category", dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
