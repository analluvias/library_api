package com.example.libraryapi.api.exception;

import com.example.libraryapi.exception.BusinessException;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.server.ResponseStatusException;

public class ApiErrors {

    List<String> allErrors;
    public ApiErrors(BindingResult bindingResult) {

        this.allErrors = new ArrayList<>();

        //passando as mensagens dos errors para a lista
        bindingResult.getAllErrors().forEach( error -> this.allErrors.add(error.getDefaultMessage()) );

    }

    public ApiErrors(BusinessException exception) {
        this.allErrors = Arrays.asList(exception.getMessage());
    }

    public ApiErrors(ResponseStatusException exception) {
        this.allErrors = Arrays.asList(exception.getReason());
    }

    public List<String> getAllErrors() {
        return allErrors;
    }
}
