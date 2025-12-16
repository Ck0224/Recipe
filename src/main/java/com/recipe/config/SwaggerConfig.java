package com.recipe.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI recipeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("食谱管理系统API")
                        .description("食谱管理系统前后端对接接口文档")
                        .version("1.0.0"));
    }
}
