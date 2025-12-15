package com.example.bookstore.author;

import com.example.bookstore.author.dto.AuthorDto;
import com.example.bookstore.author.dto.AuthorUpsertRequest;
import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    public List<AuthorDto> list() {
        return authorRepository.findAll().stream()
                .map(a -> new AuthorDto(a.getId(), a.getName()))
                .toList();
    }

    public AuthorDto create(AuthorUpsertRequest req) {
        Author a = new Author();
        a.setName(req.name());
        authorRepository.save(a);
        return new AuthorDto(a.getId(), a.getName());
    }

    public AuthorDto patch(Long id, AuthorUpsertRequest req) {
        Author a = authorRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "저자를 찾을 수 없습니다."));
        a.setName(req.name());
        return new AuthorDto(a.getId(), a.getName());
    }

    public void delete(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "저자를 찾을 수 없습니다.");
        }
        authorRepository.deleteById(id);
    }
}
