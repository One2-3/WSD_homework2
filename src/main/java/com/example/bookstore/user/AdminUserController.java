package com.example.bookstore.user;

import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ApiMeta;
import com.example.bookstore.common.ApiResponse;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.common.ItemsPayload;
import com.example.bookstore.user.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserService userService;

    public AdminUserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<ItemsPayload<UserDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        Page<User> page = userRepository.findAllByDeletedAtIsNull(pageable);
        List<UserDto> items = page.getContent().stream().map(UserDto::from).toList();
        return ApiResponse.ok("OK", new ItemsPayload<>(items), ApiMeta.fromPage(page));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> get(@PathVariable Long id) {
        User u = userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "리소스를 찾을 수 없습니다."));
        return ApiResponse.ok("OK", UserDto.from(u));
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ApiResponse.ok("비활성화 완료");
    }
}
