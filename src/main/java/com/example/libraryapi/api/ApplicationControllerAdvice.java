package com.example.libraryapi.api;

import com.example.libraryapi.api.exception.ApiErrors;
import com.example.libraryapi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice //advice de todos os meus controllers
public class ApplicationControllerAdvice {

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

    //exceptionHandler do erro ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    @ResponseStatus
    public ResponseEntity handleResponseStatusException( ResponseStatusException ex ){

        //esse ex.getStatus pega o status que lançamos la no controller "throw new(status, mensagem)"
        return new ResponseEntity(new ApiErrors(ex), ex.getStatus());

    }

}
