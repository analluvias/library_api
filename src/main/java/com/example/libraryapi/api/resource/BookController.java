package com.example.libraryapi.api.resource;

import com.example.libraryapi.api.dto.BookDTO;
import com.example.libraryapi.api.exception.ApiErrors;
import com.example.libraryapi.exception.BusinessException;
import com.example.libraryapi.model.entity.Book;
import com.example.libraryapi.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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

    //exceptionHandler desse erro (que é lançado quando o @Valid não for cumprido)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationExceptions(MethodArgumentNotValidException exception){
        //o binding result terá todas as mensagens de erro da validação
        BindingResult bindingResult = exception.getBindingResult();

        return new ApiErrors(bindingResult);
    }

    //exceptionHandler desse erro BusinessException
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBusinessException(BusinessException exception){
        //o binding result terá todas as mensagens de erro da validação

        return new ApiErrors(exception);
    }

}
