package com.zyfgoup.config;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Zyfgoup
 * @Date 2021/1/14 18:37
 * @Description
 */

@EnableSwagger2
@Configuration
public class Swagger2Config {


    @Bean(name = "cloudbase-consumer")
    public Docket createRestApi() {
        //=====添加head参数start============================
        ParameterBuilder parameter1 = new ParameterBuilder();
        List<Parameter> pars = new ArrayList<Parameter>();
        parameter1.name("Authorization").description("Authorization令牌").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
        pars.add(parameter1.build());
        // =========添加head参数end===================
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.zyfgoup"))
                //要使用@Api注解来注解Controller类 @ApiOperation注解方法
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(pars).groupName("cloudbase-consumer"); // 分组

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("consumer-接口文档")
                .description("接口文档-消费者")
                .termsOfServiceUrl("http://localhost:8882/consumer/doc.html")
                .contact(new Contact("zyfgoup", "", "619122012@qq.com"))
                .version("v1.0")
                .build();
    }

}
