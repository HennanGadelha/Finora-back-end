package com.finora_app.finora.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class JwtPropertiesTest {

	@Test
	void deveAceitarConfiguracaoValida() {
		assertDoesNotThrow(() -> new JwtProperties("12345678901234567890123456789012", Duration.ofMinutes(15)));
	}

	@Test
	void deveRecusarSegredoAusenteOuCurto() {
		assertThrows(IllegalArgumentException.class, () -> new JwtProperties(null, Duration.ofMinutes(15)));
		assertThrows(IllegalArgumentException.class, () -> new JwtProperties(" ", Duration.ofMinutes(15)));
		assertThrows(IllegalArgumentException.class, () -> new JwtProperties("curto", Duration.ofMinutes(15)));
	}

	@Test
	void deveRecusarExpiracaoInvalida() {
		assertThrows(IllegalArgumentException.class,
				() -> new JwtProperties("12345678901234567890123456789012", null));
		assertThrows(IllegalArgumentException.class,
				() -> new JwtProperties("12345678901234567890123456789012", Duration.ZERO));
		assertThrows(IllegalArgumentException.class,
				() -> new JwtProperties("12345678901234567890123456789012", Duration.ofSeconds(-1)));
	}
}
