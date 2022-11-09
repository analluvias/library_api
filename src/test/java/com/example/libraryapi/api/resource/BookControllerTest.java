package com.example.libraryapi.api.resource;

import com.example.libraryapi.api.dto.BookDTO;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.service.BookService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class) // para poder rodar com o springboot
@ExtendWith(MockitoExtension.class) //para poder usar anotação @Mock
@ActiveProfiles("test") // setando que esse está no perfil de teste
@WebMvcTest
@AutoConfigureMockMvc
class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired //injetando o MockMvc --> que vai simular uma requisição HTTP para a api
    MockMvc mvc;

    @MockBean //mock utilizado pelo SpringBoot
    BookService service;

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


    private static BookDTO createBook() {
        return BookDTO.builder().author("Arthur").title("Aventuras").isbn("001").build();
    }
}
