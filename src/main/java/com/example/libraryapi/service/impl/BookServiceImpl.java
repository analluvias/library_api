package com.example.libraryapi.service.impl;

import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.repository.BookRepository;
import com.example.libraryapi.service.BookService;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {
    private BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Object save(Book book) {

        if (repository.existsByIsbn( book.getIsbn() )){

            throw new BusinessException("Isbn já cadastrado.");

        }

        return repository.save(book);
    }
}
