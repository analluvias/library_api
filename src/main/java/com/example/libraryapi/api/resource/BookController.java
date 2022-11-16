package com.example.libraryapi.api.resource;

import com.example.libraryapi.api.dto.BookDTO;
import com.example.libraryapi.api.dto.LoanDTO;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.model.entity.Loan;
import com.example.libraryapi.service.BookService;
import com.example.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book API") //dando título no swagger
@Slf4j // anotação faz com que tenhamos um objeto de log para fazer um log qualquer
public class BookController {

    private final BookService service;
    private final ModelMapper modelMapper;

    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("CREATE A BOOK") // dando nome no swagger
    public BookDTO create(@RequestBody @Valid BookDTO dto){ // A anotação @valid serve para que o @NotNull sejam levados em conta
        // criando um log -> criando um livro para o isbn ____
        log.info("creating a book for isbn: {}", dto.getIsbn());

        // de dto para book
        Book book = modelMapper.map(dto, Book.class);

        book = (Book) service.save(book);

        // de book para dto
        return modelMapper.map(book, BookDTO.class);

    }

    @GetMapping("/{id}")
    @ApiOperation("OBTAINS A BOOK DETAILS BY ID") // dando nome no swagger
    public BookDTO get(@PathVariable Long id){

        // informando que estou obtando os detalhes de livro de id ____
        log.info("obtaining details for book id: {}", id);

        // procura o livro
        // -> se existir retorna o dto dele
        // -> senão retornar uma exceção com cod not found
        return service.getById(id)
                .map( book -> modelMapper.map(book, BookDTO.class) )
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("DELETE A BOOK BY ID") // dando nome no swagger
    @ApiResponses({
            @ApiResponse(code = 204, message = "Book seccesfully deleted")
    })
    public void delete( @PathVariable Long id ){

        // informando que estou deletando livro de id ____
        log.info("deleting book of id: {}", id);

        Book book = service.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );

        service.delete(book);

    }

    @PutMapping("/{id}")
    @ApiOperation("UPDATE A BOOK BY ID") // dando nome no swagger
    public BookDTO update( @PathVariable Long id, @RequestBody BookDTO dto){

        // informando que estou updating livro de id ____
        log.info("updating book of id: {}", id);

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
    @ApiOperation("FIND BOOKS BY PARAMS") // dando nome no swagger
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

    //esse será um subrecurso -> pelo id de um livro, vou trazer de que emprestimos ele faz parte
    @GetMapping("/{id}/loans")
    @ApiOperation("OBTAINS LOANS OF A BOOK BY ITS ID") // dando nome no swagger
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable){

        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Page<Loan> loansByBook = loanService.getLoansByBook(book, pageable);

        List<LoanDTO> list = loansByBook.getContent()
                .stream()
                .map(loan -> {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);

                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());

        return new PageImpl<>(list, pageable, list.size());

    }

}
