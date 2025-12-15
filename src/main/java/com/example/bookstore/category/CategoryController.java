package com.example.bookstore.category;

import com.example.bookstore.category.dto.CategoryDto;
import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.common.ItemsPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ItemsPayload<CategoryDto>>> list() {
        List<CategoryDto> items = categoryService.list();
        return ResponseEntity.ok(ApiResponse.success("OK", new ItemsPayload<>(items)));
    }
}
