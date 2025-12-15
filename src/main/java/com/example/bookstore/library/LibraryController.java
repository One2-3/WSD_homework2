package com.example.bookstore.library;

import com.example.bookstore.common.*;
import com.example.bookstore.library.dto.*;
import com.example.bookstore.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ItemsPayload<LibraryItemDto>>> list(
            @AuthenticationPrincipal UserPrincipal me,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Integer limit
    ) {
        var pageable = PageableUtil.pageRequest(page, size, limit);
        Page<LibraryItemDto> result = libraryService.list(me.userId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok("OK", new ItemsPayload<>(result.getContent()), ApiMeta.fromPage(result)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LibraryItemPayload>> add(
            @AuthenticationPrincipal UserPrincipal me,
            @Valid @RequestBody BookIdRequest req
    ) {
        LibraryItemDto item = libraryService.add(me.userId(), req.book_id());
        return ResponseEntity.ok(ApiResponse.ok("라이브러리에 추가되었습니다.", new LibraryItemPayload(item)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> remove(
            @AuthenticationPrincipal UserPrincipal me,
            @Valid @RequestBody BookIdRequest req
    ) {
        libraryService.remove(me.userId(), req.book_id());
        return ResponseEntity.ok(ApiResponse.ok("OK"));
    }
}
