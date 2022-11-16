package com.example.libraryapi.api.resource;

import com.example.libraryapi.api.dto.LoanDTO;
import com.example.libraryapi.api.dto.LoanFilterDTO;
import com.example.libraryapi.api.dto.ReturnedLoanDTO;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.entity.Loan;
import com.example.libraryapi.service.BookService;
import com.example.libraryapi.service.LoanService;
import com.example.libraryapi.service.LoanServiceTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static com.example.libraryapi.service.LoanServiceTest.createLoan;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class) // para poder rodar com o springboot
@ExtendWith(MockitoExtension.class) //para poder usar anotação @Mock
@ActiveProfiles("test") // setando que esse está no perfil de teste
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;
    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Deve realizar um emprestimo")
    void createLoanTest() throws Exception{

        //cenário

        //book que vou ter no bd
        Book book = Book.builder().id(1L).isbn("123").build();

        // fulano vai pedir o livro de isbn 123
        LoanDTO loanDto = LoanDTO.builder().isbn("123").customerEmail("customer@email.com").customer("fulano").build();

        // criando o json que vai pedir o emprestimo
        String json = new ObjectMapper().writeValueAsString(loanDto);

        // simulando a chamada ao service.getBookByIsbn --> que retorna o book que criamos
        BDDMockito.given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.of( book ));

        // simulando salvar loan no bd
        Loan loan = Loan.builder().id(1L)
                .customer("fulano")
                .book( book )
                .loanDate(LocalDate.now())
                .build();
        BDDMockito.given( loanService.save(Mockito.any( Loan.class )) )
                .willReturn( loan );


        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);//enviando o json que criei no cenário

        // verificação
        mvc.perform( request )
                .andExpect( status().isCreated() )
                .andExpect( content().string("1") );
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro inexistente")
    public void invalidIsbnCreateLoanTest() throws Exception{

        //book que vou ter no bd
        Book book = Book.builder().id(1L).isbn("123").build();

        // fulano vai pedir o livro de isbn 123 que não existe
        LoanDTO loanDto = LoanDTO.builder().isbn("123").customer("fulano").build();

        // criando o json que vai pedir o emprestimo
        String json = new ObjectMapper().writeValueAsString(loanDto);

        // simulando a chamada ao service.getBookByIsbn --> que retorna o book que criamos
        BDDMockito.given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.of(book) );

        // ao tentar salvar o emprestimo o serviço verificou
        // que o livro já estava emprestado e impediu o emprestimo
        BDDMockito.given( loanService.save(Mockito.any(Loan.class)) )
                .willThrow( new BusinessException("Book already loaned."));

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);//enviando o json que criei no cenário

        // verificação
        mvc.perform( request )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("allErrors", Matchers.hasSize(1)))
                .andExpect(jsonPath("allErrors[0]")
                        .value("Book already loaned."));

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro emprestado")
    public void loanedBookErrorOnCreateLoanTest() throws Exception{


        // fulano vai pedir o livro de isbn 123 que não existe
        LoanDTO loanDto = LoanDTO.builder().isbn("123").customer("fulano").build();
        // criando o json que vai pedir o emprestimo
        String json = new ObjectMapper().writeValueAsString(loanDto);

        // simulando a chamada ao service.getBookByIsbn --> que retorna o book que criamos
        BDDMockito.given(bookService.getBookByIsbn("123"))
                .willReturn(Optional.empty() );

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);//enviando o json que criei no cenário

        // verificação
        mvc.perform( request )
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("allErrors", Matchers.hasSize(1)))
                .andExpect(jsonPath("allErrors[0]")
                        .value("Book not found for passed ISBN"));

    }

    @Test
    @DisplayName("Deve retornar um livro")
    void returnBookTest() throws Exception {
        // cenário { returned: true} -> por isso usar o patch
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();

        String json = new ObjectMapper().writeValueAsString(dto);

        //quando eu chamar o getById() vou retornar esse loan aqui
        Loan loan = Loan.builder().id(1L).build();
        BDDMockito.given( loanService.getById(Mockito.anyLong()) ).willReturn(Optional.of(loan));

        //mockando o envio do postman para o loan de id 1
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(  LOAN_API.concat("/1")  )
                .accept(  MediaType.APPLICATION_JSON  )
                .contentType(  MediaType.APPLICATION_JSON  )
                .content(  json  );//enviando o json que criei no cenário


        // verificação
        mvc.perform(  request  )
                .andExpect(  status().isOk()  );
        // vendo se o loanService.update() foi chamado uma vez
        Mockito.verify(loanService, Mockito.times(1)).update(loan);

    }

    @Test
    @DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente.")
    void returnInexistentBookTest() throws Exception {
        // cenário { returned: true} -> por isso usar o patch
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();

        String json = new ObjectMapper().writeValueAsString(dto);

        //quando eu chamar loanService.getById() -> esse emprestimo não vai existir
        BDDMockito.given( loanService.getById(Mockito.anyLong()) )
                .willReturn(Optional.empty());


        //mockando o envio do postman para o loan de id 1
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(  LOAN_API.concat("/1")  )
                .accept(  MediaType.APPLICATION_JSON  )
                .contentType(  MediaType.APPLICATION_JSON  )
                .content(  json  );//enviando o json que criei no cenário


        // verificação
        mvc.perform(  request  )
                .andExpect(  status().isNotFound()  );

    }

    @Test
    @DisplayName("Deve filtrar emprestimos")
    void findLoanTest() throws Exception {

        //cenário
        // TENHO UM EMPRESTIMO COM UM LIVRO
        Long id = 1L;
        Loan loan = LoanServiceTest.createLoan();
        loan.setId(  id  );
        Book book = Book.builder().id(1L).isbn("321").build();
        loan.setBook(book);

        // VOU QUERER O PAGE NA PAGINA 0 E TAMANHO 100
        PageRequest pageRequest = PageRequest.of(0, 100);

        // VOU COLOCAR O MEU LOAN DENTRO DO PAGE -> TAMANHO TOTAL EH DE 1 LOAN
        Page<Loan> page = new PageImpl<>
                (Arrays.asList(loan), pageRequest, 1);

        // QUANDO EU CHAMAR LOANSERVICE.FIND(DTO, PAGEABLE) VOU RETORNAR A PAGE
        // QUE EU CRIEI ACIMA
        BDDMockito.given( loanService.find( Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class) ) )
                .willReturn( page );

        // ESSE EH O QUERY METHOD QUE VOU ENVIAR PELO POSTMAN
        // VOU BUSCAR O ISBN DO LIVRO NO LOAN QUE CRIEI ACIMA
        // + CUSTOMER DO LOAN QUE CRIEI ACIMA
        // VOU QUERER A PAGINA = 0 E O TAMANHO = 100
        String queryString = String.format("?isbn=%s&customer=%s&page=0&size=100",
                book.getIsbn(),
                loan.getCustomer()  );


        //simulando solicitação à API
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);


        //VERIFICAÇÃO
        mvc
                .perform( request )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("content", Matchers.hasSize(1)) )
                .andExpect( jsonPath("totalElements").value(1) )
                .andExpect( jsonPath("pageable.pageSize").value(100) )
                .andExpect( jsonPath("pageable.pageNumber").value(0) );


    }

}
