package com.example.libraryapi.service;

import com.example.libraryapi.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {
    Object save(Book any);

    Optional<Book> getById(Long id);

    void delete(Book book);

    Book update(Book book);

    Page<Book> find(  Book any, Pageable pageRequest  );

    Optional<Book> getBookByIsbn(String isbn);
}
