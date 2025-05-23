package com.mulaflow.mulaflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class BaseConfig {
    @Value("${spring.profiles.active}")
    protected String environment;
}
