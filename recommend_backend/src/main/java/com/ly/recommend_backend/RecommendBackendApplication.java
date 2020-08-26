package com.ly.recommend_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class RecommendBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendBackendApplication.class, args);
    }


    @RequestMapping("/nice")
    public String sayHello(@RequestParam(value="name" , defaultValue = "world") String name) {
        return String.format("hello " + name + "!");
    }



}
