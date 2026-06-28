package com.fiscalizacao.ms_priorizacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Priorização")
                        .description("""
                                Microsserviço responsável por calcular e ranquear a prioridade de ocorrências \
                                urbanas com base em tipo, quantidade de denúncias e idade da ocorrência.

                                **Algoritmo de pontuação (score máximo: 80 pts):**
                                - Tipo da ocorrência: até 40 pts (ESGOTO=40, BURACO=35, SEMAFORO=30, LAMPADA=15)
                                - Quantidade de denúncias: até 20 pts (≥10=20, 5-9=10, 2-4=5)
                                - Idade da ocorrência: até 20 pts (≥30 dias=20, 14-29 dias=10, 7-13 dias=5)

                                **Níveis de prioridade:**
                                - `CRITICA`: score ≥ 65
                                - `ALTA`: score 40–64
                                - `MEDIA`: score 20–39
                                - `BAIXA`: score < 20
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe TCC Fiscalização Urbana")
                                .email("lucascamilo373@gmail.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8085").description("Local"),
                        new Server().url("https://somar.up.railway.app").description("Produção")
                ));
    }
}
