package com.example.libraryapi.api.resource;

import com.example.libraryapi.api.dto.BookDTO;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.service.BookService;
import com.example.libraryapi.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class) // para poder rodar com o springboot
@ExtendWith(MockitoExtension.class) //para poder usar anotação @Mock
@ActiveProfiles("test") // setando que esse está no perfil de teste
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
class BookControllerTest {

    static String BOOK_API = "/api/books";

    // spybean também é uma opção quando eu quiser usar um método real
    // por exemplo: usar um modelmapper existente ao inves de simular
    // a transformação de um CarroDto em um Carro com BDDMockito.given
    // eu já uso o modelMapper direto -> pesquisar melhor spybean

    @Autowired //injetando o MockMvc --> que vai simular uma requisição HTTP para a api
    MockMvc mvc;

    @MockBean //mock utilizado pelo SpringBoot
    BookService service;

    @MockBean
    LoanService loanService;

    @Test
    @DisplayName("deve criar um livro com sucesso")
    void createBookTest() throws Exception {

        BookDTO dto = createBook();
        Book savedBook = Book.builder().id(10L).author("Arthur").title("Aventuras").isbn("001").build();

        //simulando que salvamos o dto e retornando savedBook
        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        // criando o json a partir do dto
        String json = new ObjectMapper().writeValueAsString(dto);

        // mockando a requisição HTTP para o meu método verdadeiro
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON) // tipo que vou mandar
                .accept(MediaType.APPLICATION_JSON) // tipo que a aplicação aceita
                .content(json); // o conteúdo é o json que criamos acima

        // aqui eu já estou fazendo os testes com o Mock
        mvc
                .perform( request )
                .andExpect( status().isCreated() )
                .andExpect( jsonPath("id").isNotEmpty() )
                .andExpect( jsonPath("title").value( dto.getTitle() ) )
                .andExpect( jsonPath("author").value( dto.getAuthor() ) )
                .andExpect( jsonPath("isbn").value( dto.getIsbn() ) );
    }


    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação do livro")
    void createInvalidBookTest() throws Exception{
        String json = new ObjectMapper().writeValueAsString( new BookDTO() );

        // mockando a requisição HTTP para o meu método verdadeiro
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON) // tipo que vou mandar
                .accept(MediaType.APPLICATION_JSON) // tipo que a aplicação aceita
                .content(json); // o conteúdo é o json que criamos acima

        mvc.perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("allErrors", Matchers.hasSize(3) ) );
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar livro com isbn já utilizado por outro")
    public void createBookWithDuplicatedIsbn() throws Exception{

         BookDTO dto = createBook();

        String json = new ObjectMapper().writeValueAsString( dto );

        // sempre que eu usar o service.save(), mando trowar BusinessException
        BDDMockito.given( service.save( Mockito.any(Book.class) ) )
                .willThrow( new BusinessException("Isbn já cadastrado.") );

        // mockando a requisição HTTP para o meu método verdadeiro
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON) // tipo que vou mandar
                .accept(MediaType.APPLICATION_JSON) // tipo que a aplicação aceita
                .content(json); // o conteúdo é o json que criamos acima

        mvc.perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("allErrors", Matchers.hasSize(1) ) )
                .andExpect(jsonPath("allErrors[0]").value("Isbn já cadastrado."));

    }

    @Test
    @DisplayName("Deve obter informações de um livro")
    void getBookDetailsTest() throws Exception {

        //cenário (given)
        Long id = 1L;

        Book book = Book.builder()
                .id(id)
                .title(createBook().getTitle())
                .author(createBook().getAuthor())
                .isbn(createBook().getIsbn())
                .build();

        BDDMockito.given( service.getById(id) ).willReturn( Optional.of(book) );

        //execução (when)

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // verificação
        mvc.perform( request )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("title").value( book.getTitle() ) )
                .andExpect( jsonPath("author").value( book.getAuthor() ) )
                .andExpect( jsonPath("isbn").value( book.getIsbn() ) );
    }

    @Test
    @DisplayName("Deve retornar resource not found qunado o livro procurado nao existir")
    void bookNotFoundTest() throws Exception{

        // cenário

        //quando eu chamar o service.getById --> retornar um optional vazio
        BDDMockito.given( service.getById( anyLong() ) ).willReturn( Optional.empty() );

        // execução --> chamando a api para o get
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        //verificação --> o id procurado nao existe
        mvc
                .perform( request )
                .andExpect( status().isNotFound() );
    }

    @Test
    @DisplayName("Deve deletar um livro")
    void deleteBookTest() throws Exception{

        //mockando o getById para que o livro exista
        BDDMockito.given( service.getById( anyLong() ) )
                .willReturn( Optional.of( Book.builder().id(1L).build() ) );

        // execução --> chamando a api para o delete do livro 1
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        // verificação -> esperando status no_content depois de deletar com sucesso
        mvc
                .perform( request )
                .andExpect( status().isNoContent() );

    }

    @Test
    @DisplayName("Deve retornar resource not found quando não encontrar um livro p/ deletar")
    void deleteInexistentBookTest() throws Exception{

        //mockando o getById para que o livro exista
        BDDMockito.given( service.getById( anyLong() ) )
                .willReturn( Optional.empty() ) ;

        // execução --> chamando a api para o delete do livro 1
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        // verificação -> esperando status no_content depois de deletar com sucesso
        mvc
                .perform( request )
                .andExpect( status().isNotFound() );

    }

    @Test
    @DisplayName("Deve atualizar um livro")
    void updateBookTest() throws Exception{

        Long id = 1L;

        // livro 1L no BD
        Book book_to_update = Book.builder()
                .id(id)
                .title("some title")
                .author("some author")
                .isbn("123")
                .build();

        //mockando o getById para que o livro exista
        BDDMockito.given( service.getById( id ) )
                .willReturn( Optional.of( book_to_update ) );

        //mockando o service.update para que atualizemos o livro
        BDDMockito.given( service.update( book_to_update ) )
                .willReturn(
                        Book.builder()
                                .id( id )
                                .isbn(createBook().getIsbn() )
                                .title(createBook().getTitle())
                                .author(createBook().getAuthor())
                                .build()
                );

        // criando o json que vou enviar para atualizar livro 1L
        String json = new ObjectMapper().writeValueAsString( createBook() );

        // execução --> chamando a api para o delete do livro 1
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content( json )
                .accept(MediaType.APPLICATION_JSON)
                .contentType( MediaType.APPLICATION_JSON );

        // verificação -> vendo se atualizou e se está de acordo com o json que enviei
        mvc
                .perform( request )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id) )
                .andExpect( jsonPath("title").value( createBook().getTitle() ) )
                .andExpect( jsonPath("author").value( createBook().getAuthor() ) )
                .andExpect( jsonPath("isbn").value( createBook().getIsbn() ) );

    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente")
    void updateInexistentBookTest() throws Exception{

        Long id = 1L;

        //mockando o getById para que o livro nao exista
        BDDMockito.given( service.getById( id ) )
                .willReturn( Optional.empty() );

        // criando o json que vou enviar para atualizar livro 1L
        String json = new ObjectMapper().writeValueAsString( createBook() );

        // execução --> chamando a api para o delete do livro 1
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content( json )
                .accept(MediaType.APPLICATION_JSON)
                .contentType( MediaType.APPLICATION_JSON );

        // verificação -> vendo se retornou status not found
        mvc
                .perform( request )
                .andExpect( status().isNotFound() );

    }


    @Test
    @DisplayName("Deve filtrar livros")
    void findBooksTest() throws Exception{

        // cenário
        Long id = 1L;

        Book book = Book.builder()
                    .id(createBook().getId())
                    .author(createBook().getAuthor())
                    .isbn(createBook().getIsbn())
                    .title(createBook().getTitle())
                    .build();

        // simulando pedido de pesquisa paginado
        BDDMockito.given( service.find( Mockito.any(Book.class), Mockito.any(Pageable.class) ) )
                .willReturn( new PageImpl<Book>
                        (  Arrays.asList( book ), PageRequest.of(0, 100), 1));

        // pesquisa que vou fazer
        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(),
                book.getAuthor()  );

        //simulando solicitação à API
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform( request )
                .andExpect( status().isOk() )
                .andExpect( jsonPath("content", Matchers.hasSize(1)) )
                .andExpect( jsonPath("totalElements").value(1) )
                .andExpect( jsonPath("pageable.pageSize").value(100) )
                .andExpect( jsonPath("pageable.pageNumber").value(0) );

    }

    private static BookDTO createBook() {
        return BookDTO.builder().author("Arthur").title("Aventuras").isbn("001").build();
    }
}
