package com.example.libraryapi.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.entity.Loan;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest // anotação teste de integração com banco em memória
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository repository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("deve verificar se existe empretimo nao devolvido p/ o livro")
    void existsByBookAndNotReturned(){
        //cenário
        Book book = createNewBook();
        entityManager.persist(  book  );

        Loan loan = Loan.builder().book( book ).customer("fulano").loanDate(LocalDate.now()).build();
        entityManager.persist(  loan  );


        //execução -> o livro está emprestado
        boolean exists = repository.existsByBookAndNotReturned(book);


        //verificação
        assertThat( exists ).isTrue();

    }

    @Test
    @DisplayName("deve buscar emprestimo pelo isbn do livro ou customer")
    public void findByBookIsbnOrCustomerTest(){

        //cenário
        Book book = createNewBook();
        entityManager.persist(  book  );

        Loan loan = Loan.builder().book( book ).customer("fulano").loanDate(LocalDate.now()).build();
        entityManager.persist(  loan  );

        Page<Loan> pageResult = repository.findByBookIsbnOrCustomer
                        ("123",
                        "fulano",
                        PageRequest.of(0, 10));

        assertThat( pageResult.getContent() ).hasSize(1);
        assertThat( pageResult.getContent() ).contains(loan);
        assertThat( pageResult.getPageable().getPageSize() ).isEqualTo(10);
        assertThat( pageResult.getPageable().getPageNumber()).isEqualTo(0);
        assertThat( pageResult.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("deve obter emprestimos cuja date de empretimo for" +
            " menor ou igual a tres dias atras e não retornados")
    public void findByLoanDateLessThanAndNotReturnedTest(){
        // cenário - criando um loan de dias atrás e salvando
        Book book = createNewBook();

        entityManager.persist( book );

        Loan loan = Loan.builder()
                .book( book )
                .customer("fulano")
                .loanDate( LocalDate.now().minusDays(5) )
                .build();

        entityManager.persist(  loan  );


        // execução - chamando diretamente o método do repositório
        List<Loan> returned = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));


        // verificação
        assertThat( returned ).hasSize(1).contains(loan);

    }

    @Test
    @DisplayName(" não retornar vazio, pois não há nenhum atrasado")
    public void notFindByLoanDateLessThanAndNotReturnedTest(){
        // cenário - criando um loan de dias atrás e salvando
        Book book = createNewBook();

        entityManager.persist( book );

        Loan loan = Loan.builder()
                .book( book )
                .customer("fulano")
                .loanDate( LocalDate.now() )
                .build();

        entityManager.persist(  loan  );


        // execução - chamando diretamente o método do repositório
        List<Loan> returned = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));


        // verificação
        assertThat( returned ).isEmpty();

    }

    private Book createNewBook(){
        return Book.builder().title("aventuras").author("fulano").isbn("123").build();
    }
}
