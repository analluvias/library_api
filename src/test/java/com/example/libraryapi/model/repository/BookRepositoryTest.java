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

}
