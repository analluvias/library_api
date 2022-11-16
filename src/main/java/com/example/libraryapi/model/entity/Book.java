package com.example.libraryapi.model.entity;

import java.util.List;
import lombok.*;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Book {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private String isbn;

    //FETCHTYPE.LAZY -> QUANDO EU BUSCAR O LIVRO EU NÃO VOU BUSCCAR OS EMPRESTIMOS DE QUE ELE FAZ PARTE
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<Loan> loans;
}
