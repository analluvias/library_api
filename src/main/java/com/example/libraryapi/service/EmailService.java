package com.example.libraryapi.service;

import java.util.List;

public interface EmailService {

    // para podermos enviar email precisamos de um servidor de emails
    // o mais simples eh o mailtrap (para aplicações em desenvolvimento)

    void sendMails(String message, List<String> allEmails);
}
