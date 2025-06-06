package es.tfg.codeguard;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodeGuardApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeGuardApplication.class, args);
	}

}
