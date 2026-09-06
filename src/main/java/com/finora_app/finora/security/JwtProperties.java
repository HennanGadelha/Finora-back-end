package com.finora_app.finora.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, Duration expiration) {

	public JwtProperties {
		if (secret == null || secret.isBlank()) {
			throw new IllegalArgumentException("JWT_SECRET deve ser configurado");
		}
		if (secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
			throw new IllegalArgumentException("JWT_SECRET deve possuir pelo menos 32 bytes");
		}
		if (expiration == null || expiration.isZero() || expiration.isNegative()) {
			throw new IllegalArgumentException("A expiracao do JWT deve ser positiva");
		}
	}
}
