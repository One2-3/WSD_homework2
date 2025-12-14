package com.example.bookstore.security;

import com.example.bookstore.user.UserRole;

public record UserPrincipal(Long userId, UserRole role) {
}
