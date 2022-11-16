package com.example.libraryapi.model.repository;

import com.example.libraryapi.model.entity.Book;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {


    boolean existsByIsbn(String isbn);

    void delete(Book entity);

    Optional<Book> findByIsbn(String isbn);

//    Book findBooksFetchLoan();
}
