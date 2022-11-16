package com.example.libraryapi.service;

import com.example.libraryapi.model.entity.Loan;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


//Criando o serviço que irá enviar os emails scheduled
@Service
@RequiredArgsConstructor
public class ScheduleService {

    // http://www.cronmaker.com/ -> facilita criar a expressão
    // NESSE CASO VAI RODAR DIARIAMENTE A QUALQUER MES E A QUALQUER ANO
    private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";

    @Value("${application.mail.lateloans.message}")
    private String message;

    private final LoanService loanService;

    private final EmailService emailService;

    @Scheduled(cron = CRON_LATE_LOANS)
    public void SendMailToLateLoans(){

        List<Loan> allLateLoans = loanService.getAllLateLoans();

        List<String> allEmails = allLateLoans.stream()
                .map(loan -> loan.getCustomerEmail())
                .collect(Collectors.toList());

        emailService.sendMails(message, allEmails);
    }

}
