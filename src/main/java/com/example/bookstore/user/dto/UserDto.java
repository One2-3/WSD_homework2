package com.example.bookstore.user.dto;

import com.example.bookstore.user.Gender;
import com.example.bookstore.user.User;
import com.example.bookstore.user.UserRole;

import java.time.Instant;
import java.time.LocalDate;

public record UserDto(
        Long id,
        String email,
        String name,
        String address,
        String region,
        String phone,
        Gender gender,
        LocalDate birthdate,
        UserRole role,
        Instant updatedAt
) {
    public static UserDto from(User u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                u.getName(),
                u.getAddress(),
                u.getRegion(),
                u.getPhone(),
                u.getGender(),
                u.getBirthdate(),
                u.getRole(),
                u.getUpdatedAt()
        );
    }
}
