package com.ft.warehousefullfilmentsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI warehouseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Warehouse Fulfilment System API")
                        .description(
                                "REST API for managing products, inventory, " +
                                        "stock transactions, and customer orders."
                        )
                        .version("1.0.0"));
    }
}