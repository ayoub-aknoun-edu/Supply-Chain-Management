package com.hashing.backend;

import com.hashing.backend.model.User;
import com.hashing.backend.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }


    // add 2 user (manager and user) to the database
    @Bean
    CommandLineRunner run(UserService userService) {
        return args -> {

            // add 2 user (manager and user) to the database if not exist
            if (userService.findByUsernameCmd("manager").isEmpty()) {
                userService.createUser(
                    User.builder()
                            .username("manager")
                            .email("administrator@gmail.com")
                            .password("123456")
                            .role(User.Role.MANAGER)
                            .build()
            );
            }
            if (userService.findByUsernameCmd("user").isEmpty()) {
                userService.createUser(
                    User.builder()
                            .username("user")
                            .email("user@gmail.com")
                            .password("123456")
                            .role(User.Role.USER)
                            .build()
            );
            }
        };
    }
}
