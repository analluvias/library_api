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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    @DisplayName("Deve obter um livro por id")
    void getByIdTest(){

        // cenário
        Long id = 1L;

        Book book = createBook();
        book.setId( id );

        Mockito.when( repository.findById( id ) ).thenReturn(Optional.of( book ) );

        //execução
        Optional<Book> foundBook = service.getById( id );

        //verificações
        assertThat( foundBook.isPresent() ).isTrue();
        assertThat( foundBook.get().getId() ).isEqualTo( id );
        assertThat( foundBook.get().getAuthor() ).isEqualTo( book.getAuthor() );
        assertThat( foundBook.get().getTitle() ).isEqualTo( book.getTitle() );
        assertThat( foundBook.get().getIsbn() ).isEqualTo( book.getIsbn() );

    }


    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por id inexistente")
    void BookNotFoundByIdTest(){

        // cenário
        Long id = 1L;

        Mockito.when( repository.findById( id ) ).thenReturn( Optional.empty() );

        //execução
        Optional<Book> book = service.getById( id );

        //verificações
        assertThat( book).isNotPresent();

    }

    @Test
    @DisplayName("Deve deletar livro existente")
    void DeleteBookTest(){

        // cenário
        Long id = 1L;

        Book book = createBook();
        book.setId( id );

        doNothing().when(repository).delete(book);

        //execução
        org.junit.jupiter.api.Assertions.assertDoesNotThrow( () -> service.delete( book ) );

        //verificação
        Mockito.verify( repository, Mockito.times(1) ).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao deletar livro inexistente")
    void DeleteInvalidBookTest(){

        // cenário
        Book book = createBook();

        doNothing().when(repository).delete(book);

        //execução
        org.junit.jupiter.api.Assertions.assertThrows
                (IllegalArgumentException.class, () -> service.delete( book ) );

        //verificação
        Mockito.verify( repository, Mockito.never() ).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao dar update livro inexistente")
    void UpdateInvalidBookTest(){

        // cenário
        Book book = createBook();

        //execução -> chamando o service.update()
        org.junit.jupiter.api.Assertions.assertThrows
                (IllegalArgumentException.class, () -> service.update( book ) );

        //verificação --> não entrou no repository
        Mockito.verify( repository, Mockito.never() ).save(book);
    }

    @Test
    @DisplayName("Deve dar update em livro existente")
    void UpdateBookTest(){

        // cenário

        //livro a atualizar
        Book book = createBook();
        book.setId(1L);

        //livro atualizado
        Book updatedBook = Book.builder().isbn("novo").title("novo").author("novo").id(1L).build();

        // simulação
        Mockito.when(repository.save(book)).thenReturn(updatedBook);


        //execução -> chamando o service.update()
        Book bookfinal = service.update(book);


        //verificação --> não entrou no repository
        assertThat(bookfinal.getId()).isEqualTo( updatedBook.getId() );
        assertThat(bookfinal.getTitle()).isEqualTo( updatedBook.getTitle() );
        assertThat(bookfinal.getIsbn()).isEqualTo( updatedBook.getIsbn() );
        assertThat(bookfinal.getAuthor()).isEqualTo( updatedBook.getAuthor()) ;
    }

    @Test
    @DisplayName("Deve filtrar livros PELAS PROPRIEDADES")
    void findBookTest(){

        //cenário
        Book book = createBook();

        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<Book> page = new PageImpl<>
                (Arrays.asList(book), pageRequest, 1);

        Mockito.when( repository.findAll( Mockito.any(Example.class), Mockito.any(PageRequest.class) ) )
                .thenReturn( page );


        //execução
        Page<Book> result = service.find(book, pageRequest);


        //verificação
        assertThat(  result.getTotalElements()  ).isEqualTo(1);
        assertThat( result.getContent() ).isEqualTo(Arrays.asList(book));
        assertThat( result.getPageable().getPageNumber() ).isEqualTo( 0 );
        assertThat( result.getPageable().getPageSize() ).isEqualTo( 10 );


    }


    @Test
    @DisplayName("deve obter um livro pelo isbn")
    void getBookByIsbnTest(){

        //cenário
        String isbn = "123";

        //simulando que o repo retornou o livro que existe na base
        Mockito.when( repository.findByIsbn(isbn) )
                .thenReturn( Optional.of( Book.builder().id(1L).isbn(isbn).build() ) );


        //execução
        Optional<Book> book = service.getBookByIsbn(isbn);


        //verificação
        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1L);
        assertThat(book.get().getIsbn()).isEqualTo( isbn );
        // verificando que findByIsbn() foi chamado uma vez
        verify( repository, times(1)).findByIsbn(isbn);
    }

    private static Book createBook() {
        return Book.builder().isbn("123").author("fulano").title("titulo").build();
    }


    
}
