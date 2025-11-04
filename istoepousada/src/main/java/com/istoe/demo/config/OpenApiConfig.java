package com.istoe.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = createInfo();
        info.setContact(createContact());
        info.setLicense(createLicense());
        return new OpenAPI()
                .info(info);
    }

    private Info createInfo(){
        var info = new Info();
        info.setTitle("MS-BIOMETRIA-FACIAL");
        info.setDescription("Microservice para realização de cadastro e registro de biometria facial do SIISP");
        info.setVersion("v1");
        return info;
    }

    private Contact createContact(){
        var contato = new Contact();
        contato.setName("");
        contato.setEmail("");
        contato.setUrl("");
        return contato;
    }

    private License createLicense(){
        var licenca = new License();
        licenca.setName("Copyright (C) Todos os direitos reservados ");
        licenca.setUrl("https://seap.ma.gov.br");
        return licenca;
    }
}
