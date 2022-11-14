package com.example.libraryapi.api.resource;

import com.example.libraryapi.api.dto.LoanDTO;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.entity.Loan;
import com.example.libraryapi.service.BookService;
import com.example.libraryapi.service.LoanService;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor //cria construtor de classes final
public class LoanController {

    private final LoanService service;
    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public long create(@RequestBody LoanDTO dto ){

        Book bookByIsbn = bookService.getBookByIsbn(dto.getIsbn())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST,"Book not found for passed ISBN"));

        Loan entity = Loan.builder().book( bookByIsbn )
                .customer(dto.getCustomer() )
                .loanDate( LocalDate.now() ).build();

        entity = service.save(entity);

        return entity.getId();

        
    }
}
