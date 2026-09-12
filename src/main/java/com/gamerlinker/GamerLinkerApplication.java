package com.gamerlinker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GamerLinkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GamerLinkerApplication.class, args);
        System.out.println("=================================================");
        System.out.println("🚀 Servidor GamerLinker em JAVA rodando!");
        System.out.println("Acesse no navegador: http://localhost:8080");
        System.out.println("=================================================");
    }
}
