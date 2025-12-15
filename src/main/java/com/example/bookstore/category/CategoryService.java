package com.example.bookstore.category;

import com.example.bookstore.category.dto.CategoryDto;
import com.example.bookstore.category.dto.CategoryUpsertRequest;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> list() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .toList();
    }

    public CategoryDto create(CategoryUpsertRequest req) {
        categoryRepository.findByName(req.name()).ifPresent(x -> {
            throw new ApiException(ErrorCode.CONFLICT, "이미 존재하는 카테고리명입니다.");
        });

        Category c = new Category();
        c.setName(req.name());
        categoryRepository.save(c);
        return new CategoryDto(c.getId(), c.getName());
    }

    public CategoryDto patch(Long id, CategoryUpsertRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다."));
        c.setName(req.name());
        return new CategoryDto(c.getId(), c.getName());
    }

    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "카테고리를 찾을 수 없습니다.");
        }
        categoryRepository.deleteById(id);
    }
}
