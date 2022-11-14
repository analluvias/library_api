package com.example.libraryapi.model.repository;

import com.example.libraryapi.model.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest // para testar com o banco -> mas eh um h2
class BookRepositoryTest {

    //entitymanager eh quem consegue mexer no banco de dados diretamente
    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    void returnTrueWhenIsbnExists(){
        //cenário
        String isbn = "123";

        //mandando salvar no h2
        Book book = Book.builder().title("Aventurar").author("Fulano").isbn(isbn).build();
        entityManager.persist(book);

        //execução
        boolean exists = repository.existsByIsbn(isbn);

        //verificação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando nao existir um livro na base com o isbn informado")
    void returnFalseWhenIsbnDoesNotExist(){
        //cenário
        String isbn = "123";

        //execução
        boolean exists = repository.existsByIsbn(isbn);

        //verificação
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    void findByIdTest(){
        //cenário
        Book book = createNewBook();

        // mandando salvar livro no h2
        entityManager.persist(book);

        //execução

        // mandando repo recuperar book no h2
        Optional<Book> foundBook = repository.findById(book.getId());

        //verificações
        assertThat(foundBook.isPresent()).isTrue();

    }

    @Test
    @DisplayName("Deve salvar um livro")
    void saveBookTest(){
        //cenário
        Book book = createNewBook();

        //execução
        Book savedBook = repository.save(book);

        //verificação
        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    void deleteBookTest(){
        //cenário
        Book book = createNewBook();
        entityManager.persist(book);

        Book foundBook = entityManager.find(Book.class, book.getId());

        //execução
        repository.delete(foundBook);

        //verificação -> buscando o livro deletado e verificando que eh nulo
        Book deletedBook = entityManager.find(Book.class, book.getId());
        assertThat(deletedBook).isNull();
    }

    private Book createNewBook(){
        return Book.builder().title("aventuras").author("fulano").isbn("123").build();
    }

}
