package com.example.libraryapi;

import com.example.libraryapi.service.EmailService;
import java.util.Arrays;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // permite agendar tarefas
public class LibraryApiApplication {

	// usando apenas para testar o envio de email
	@Autowired
	private EmailService emailService;

	// usando apenas para testar o envio de email
	@Bean
	public CommandLineRunner runner(){
		return args -> {
			List<String> emails = Arrays.asList("e0a667caa3-667218@inbox.mailtrap.io");
			emailService.sendMails("Testando serviço de email.", emails);
			System.out.println("Email enviado");
		};
	}

	// injetando o modelMapper no contexto
	// será um singleton usado por toda a aplicação
	// preciso que o dto e a entity tenham propriedades com o mesmo nome
	// para que funcione
	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}

	// qualquer anotação do spring pode ter um método com @scheduled
	// basta ter o @EnableScheduling na classe de configuração
	// (ou seja -> em cima do "public class LibraryApiApplication")
	// @Scheduled(cron = "30 19 6 1/1 * ?") definindo tempo de execução
	// dessa tarefa -> é feita por uma pequena expressão
	// http://www.cronmaker.com/ -> facilita criar a expressão
//	@Scheduled(cron = "30 19 6 1/1 * ?")
//	public void testeAgendamentoTarefas(){
//		System.out.println("agendamento de tarefas");
//	}

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
