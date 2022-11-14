package com.example.libraryapi.service.impl;

import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.repository.BookRepository;
import com.example.libraryapi.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.Optional;

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

    @Override
    public Optional<Book> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void delete(Book book) {
        if (book == null || book.getId() == null){
            throw new IllegalArgumentException("Book id cannot be null");
        }
        repository.delete(book);
    }

    @Override
    public Book update(Book book) {
        if (book == null || book.getId() == null){
            throw new IllegalArgumentException("Book id cannot be null");
        }

        return repository.save(book);
    }

    @Override
    public Page<Book> find(Book filter, org.springframework.data.domain.Pageable pageRequest) {

        //criando o mecanismo de pesquisa a partir do livro enviado pelo json
        Example<Book> example = Example.of(filter,
                ExampleMatcher
                        .matching()
                        .withIgnoreCase()
                        .withIncludeNullValues()
                        .withStringMatcher( ExampleMatcher.StringMatcher.CONTAINING ) );

        return repository.findAll(example, pageRequest);
    }

    public Optional<Book> getBookByIsbn(String isbn) {
        return null;
    }
}
