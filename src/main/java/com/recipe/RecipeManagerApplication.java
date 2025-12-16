package com.recipe;  // 必须与目录结构匹配

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RecipeManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecipeManagerApplication.class, args);
    }
}