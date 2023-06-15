//package com.myblogbackend.blog.fakers;
//
//import com.github.javafaker.Faker;
//import com.myblogbackend.blog.repositories.UsersRepository;
//import com.myblogbackend.blog.request.SignUpFormRequest;
//import com.myblogbackend.blog.services.AuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
//
//@Component
//@Order(2)
//@RequiredArgsConstructor
//public class GenerateUserCommand implements CommandLineRunner {
//
//    private final AuthService authService;
//    private final Environment environment;
//    private final UsersRepository usersRepository;
//
//    @Override
//    public void run(final String... args) throws Exception {
//        boolean shouldGenerate = environment.getProperty("app.generate-users", Boolean.class, true);
//        if (!shouldGenerate) {
//            return;
//        }
//        Faker faker = new Faker();
//        var userFound = usersRepository.findAll();
//        if (userFound.isEmpty()) {
//            SignUpFormRequest signUpFormRequest = SignUpFormRequest.builder()
//                    .email("tuanbeovnn@gmail.com")
//                    .password("123456")
//                    .name(faker.commerce().department())
//                    .build();
//            authService.registerUser(signUpFormRequest, null);
//        }
//
//    }
//}