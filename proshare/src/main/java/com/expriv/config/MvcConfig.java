package com.expriv.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/home").setViewName("home");
        registry.addViewController("/index").setViewName("index");
        registry.addViewController("/").setViewName("index");
        registry.addViewController("register_success").setViewName("register_success");
        registry.addViewController("forgot-password").setViewName("forgot-password");
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/about").setViewName("about");
        registry.addViewController("/registration").setViewName("registration");
        registry.addViewController("/training").setViewName("training");
        registry.addViewController("/evaluation").setViewName("evaluation");
        registry.addViewController("/training_complete").setViewName("training_complete");
        registry.addViewController("/evaluation_complete").setViewName("evaluation_complete");
        registry.addViewController("/feedback").setViewName("feedback");
        registry.addViewController("/payment").setViewName("payment");
        registry.addViewController("/payment_complete").setViewName("payment_complete");

    }



}
