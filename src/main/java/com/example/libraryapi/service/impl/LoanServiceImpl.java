package com.example.libraryapi.service.impl;

import com.example.libraryapi.api.dto.LoanFilterDTO;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.entity.Loan;
import com.example.libraryapi.model.repository.LoanRepository;
import com.example.libraryapi.service.LoanService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LoanServiceImpl implements LoanService {
    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save( Loan loan ) {
        if ( repository.existsByBookAndNotReturned(loan.getBook()) ){
           throw new BusinessException("Book already loaned.");
        }
        return repository.save(loan);

    }

    @Override
    public Optional<Loan> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return repository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable) {
        return repository
                .findByBookIsbnOrCustomer(
                        filterDTO.getIsbn(),
                        filterDTO.getCustomer(),
                        pageable);
    }

    @Override
    public Page<Loan> getLoansByBook(Book book, Pageable pageable) {
        return repository.findByBook(book, pageable);
    }

    @Override
    public List<Loan> getAllLateLoans() {
        // são 4 dias de emprestimo pra estar atrasado
        // se chegar no 3o e ele tiver entregue, ainda estará em dia
        final Integer loanDays = 4;

        // dia que o customer fez emprestimo (há quatro dias)
        // e começa a estar atrasado hoje
        LocalDate threeDaysAgo = LocalDate.now().minusDays(loanDays);

        // encontre pela data de emprestimo menor que (ou igual) a threeDaysAgo
        // e que não esteja retornado
        return repository.findByLoanDateLessThanAndNotReturned(threeDaysAgo);
    }
}
