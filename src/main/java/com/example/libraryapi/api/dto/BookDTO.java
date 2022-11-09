package com.example.libraryapi.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookDTO {

    private Long id;

    @NotEmpty(message = "title must not be empty")
    private String title;

    @NotEmpty(message = "author must not be empty")
    private String author;

    @NotEmpty(message = "isbn must not be empty")
    private String isbn;
}
