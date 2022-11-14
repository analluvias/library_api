package com.example.libraryapi.api.resource;

import com.example.libraryapi.api.dto.BookDTO;
import com.example.libraryapi.api.exception.ApiErrors;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService service;
    private ModelMapper modelMapper;

    public BookController(BookService service, ModelMapper mapper) {
        this.service = service;
        this.modelMapper = mapper;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto){

        // A anotação @valid serve para que o @NotNull sejam levados em conta

        // de dto para book
        Book book = modelMapper.map(dto, Book.class);

        book = (Book) service.save(book);

        // de book para dto
        return modelMapper.map(book, BookDTO.class);

    }

    @GetMapping("/{id}")
    public BookDTO get(@PathVariable Long id){

        //procura o livro
        // -> se existir retorna o dto dele
        // -> senão retornar uma exceção com cod not found
        return service.getById(id)
                .map( book -> modelMapper.map(book, BookDTO.class) )
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete( @PathVariable Long id ){

        Book book = service.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );

        service.delete(book);

    }

    @PutMapping("/{id}")
    public BookDTO update( @PathVariable Long id, @RequestBody BookDTO dto){

        // verificando se o livro existe na base
        //se existir atualiza e retorna o dto
        //se não, retorna exception com cod notfound
        return service.getById( id )
                .map( book_found ->
                {
                    //atualizando objeto a partir do json
                    book_found.setAuthor( dto.getAuthor() );
                    book_found.setTitle( dto.getTitle() );

                    //atualizando no bd
                    book_found = service.update(book_found);

                    // retornando um dto a partir do livro atualiado
                    return modelMapper.map(book_found, BookDTO.class);

                })
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );


    }

    @GetMapping
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest){

        Book filter = modelMapper.map(dto, Book.class);

        // mandando buscar através do pageable (retorna uma lista)
        Page<Book> result = service.find(filter, pageRequest);

        // mandando cada elemento da lista ser passado de livro para livrodto
        // e guardando numa lista de BookDTO
        List<BookDTO> list = result.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        //retornando o pageable de booksdto
        return new PageImpl<BookDTO>(  list, (Pageable) pageRequest, result.getTotalElements()  );
    }



}
