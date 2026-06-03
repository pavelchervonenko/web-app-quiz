package com.quizapp.backend.config;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

import org.springframework.validation.annotation.Validated;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Component
@ConfigurationProperties(prefix = "rsa")
@Validated
@Setter
@Getter
public class RsaKeyProperties {

    @NotNull
    private RSAPublicKey publicKey;

    @NotNull
    private RSAPrivateKey privateKey;
}
