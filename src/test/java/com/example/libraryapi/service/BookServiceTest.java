package com.example.libraryapi.service;

import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.repository.BookRepository;
import com.example.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class BookServiceTest {
    
    BookService service;
    @MockBean
    BookRepository repository;

    // injetando a implementação do service dentro
    // do nosso atributo (que eh a interface que o service vai implementar)
    @BeforeEach
    void setUp(){
        this.service = new BookServiceImpl( repository );
    }
    
    @Test
    @DisplayName("Deve salvar um livro")
    void saveBookTest(){
        //cenáro

        //criar livro valido
        Book book = createBook();

        // mandando retornar falso que já existe livro com esse isbn - mock
        Mockito.when(repository.existsByIsbn( Mockito.anyString()) ).thenReturn(false);

        // mandando salvar e retornar um livro - mock
        Mockito.when( repository.save(book) )
                .thenReturn(Book.builder().id(1L)
                        .isbn("123")
                        .author("fulano")
                        .title("titulo").build());

        //execucao
        Book savedBook = (Book) service.save(book);

        //verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("titulo");
        assertThat(savedBook.getAuthor()).isEqualTo("fulano");
    }



    @Test
    @DisplayName("Deve lançar erro de negocio ao tentar salvar livro com isbn duplicado")
    void shouldNotSaveBookWithDuplicatedISBN(){

        //cenario
        Book book = createBook();
        // quando chamarmos o existsByIsbn retornar true, aí entra na exceção
        Mockito.when(repository.existsByIsbn( Mockito.anyString()) ).thenReturn(true);

        //execução
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        // verificações

        // 1 - lançou exceção
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");

        // 2 - não chamou o repository.save() nenhuma vez
        Mockito.verify(repository, Mockito.never()).save(book);

    }

    private static Book createBook() {
        return Book.builder().isbn("123").author("fulano").title("titulo").build();
    }


    
}
