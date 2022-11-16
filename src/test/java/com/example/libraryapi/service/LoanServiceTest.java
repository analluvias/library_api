package com.example.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.libraryapi.api.dto.LoanFilterDTO;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.entity.Loan;
import com.example.libraryapi.model.repository.LoanRepository;
import com.example.libraryapi.service.impl.LoanServiceImpl;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {


    private LoanService service;
    @MockBean  //nosso repo ainda não "existe" nesse teste
    private LoanRepository repository;

    @BeforeEach
    void setUp(){
        this.service = new LoanServiceImpl(repository);

    }

    @Test
    @DisplayName("deve salvar um emprestimo")
    void saveLoanTest(){

        //criando o loan com o livro de id 1 para o customer customer
        Book book = Book.builder().id(1L).build();
        String customer = "fulano";

        Loan loanToSave = Loan.builder()
                .book( book )
                .customer( customer )
                .loanDate( LocalDate.now() )
                .build();

        //loan salvo no db
        Loan savedLoan = Loan.builder()
                .id(1L)
                .book( book )
                .loanDate(LocalDate.now())
                .customer( customer )
                .build();

        //vou verificar se o livro já existe na base,
        // e ele retorna falso (nao existe)
        when( repository.existsByBookAndNotReturned(book) ).thenReturn( false );
        //quando eu mandar o loanToSave, retorne o savedLoan
        when( repository.save( loanToSave ) ).thenReturn( savedLoan );


        //execução
        Loan loan = service.save( loanToSave );

        //verificações

        assertThat( loan.getId() ).isEqualTo( savedLoan.getId() );
        assertThat( loan.getBook().getId() ).isEqualTo( savedLoan.getBook().getId() );
        assertThat( loan.getCustomer() ).isEqualTo( savedLoan.getCustomer() );
        assertThat( loan.getLoanDate() ).isEqualTo( savedLoan.getLoanDate() );

    }


    @Test
    @DisplayName("deve lançar erro de negocio ao slvar um emprestimo com livro já emprestado")
    void loanedBookSaveTest(){

        //criando o loan com o livro de id 1 para o customer customer
        Book book = Book.builder().id(1L).build();
        String customer = "fulano";

        Loan loanToSave = Loan.builder()
                .book( book )
                .customer( customer )
                .loanDate( LocalDate.now() )
                .build();

        // quando eu mandar o "book" que já foi emprestado,
        // então ele vai retornar que ele já existe na base de emprestimos
        when( repository.existsByBookAndNotReturned(book) ).thenReturn( true );


        //execução -> vamos capturar o erro
        Throwable exception = catchThrowable(() -> service.save(loanToSave));


        //verificações
        assertThat( exception )
                .isInstanceOf( BusinessException.class )
                .hasMessage("Book already loaned.");
        //verificando que nunca chamou orepository.save(loanToSave)
        verify(repository, never()).save(loanToSave);
    }

    @Test
    @DisplayName(" deve obter as informações de um emprestimo pelo id")
    void getLoanDetailsTest(){
        //cenário
        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when( repository.findById(id) ).thenReturn(Optional.of(loan));


        //execução
        Optional<Loan> result = service.getById(id);


        //verificação
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository).findById(id);
    }

    @Test
    @DisplayName("deve atualizar um emprestimo")
    void updateLoanTest(){

        //cenário
        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(id);
        loan.setReturned(true);

        when( repository.save(loan) ).thenReturn( loan );


        //execução
        Loan updatedLoan = service.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue(); //garantindo que o getReturned = true -> que ele foi devolvido
        verify(repository).save(loan); //garantindo que chamou o método save
    }

    @Test
    @DisplayName("Deve filtrar EMPRESTIMOS PELAS PROPRIEDADES")
    void findBookTest(){

        //cenário
        LoanFilterDTO dto = LoanFilterDTO.builder().customer("fulano").isbn("321").build();

        Loan loan = createLoan();
        loan.setId(1L);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> list = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<>
                (list, pageRequest, list.size());

        Mockito.when(
                repository.findByBookIsbnOrCustomer(
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.any(PageRequest.class) ) )
                .thenReturn( page );


        //execução
        Page<Loan> result = service.find( dto, pageRequest );


        //verificação
        AssertionsForClassTypes.assertThat( result.getTotalElements()  ).isEqualTo(1);
        AssertionsForClassTypes.assertThat( result.getContent() ).isEqualTo(list);
        AssertionsForClassTypes.assertThat( result.getPageable().getPageNumber() ).isEqualTo( 0 );
        AssertionsForClassTypes.assertThat( result.getPageable().getPageSize() ).isEqualTo( 10 );


    }

    public static Loan createLoan(){
        Book book = Book.builder().id(1L).build();
        String customer = "fulano";

        return Loan.builder()
                .book( book )
                .customer( customer )
                .loanDate( LocalDate.now() )
                .build();
    }
}
