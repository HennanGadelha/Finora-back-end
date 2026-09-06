package com.finora_app.finora.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SenhaServiceTest {

	private final SenhaService service = new SenhaService(4);

	@Test
	void deveGerarHashECompararSenhaCorreta() {
		String hash = service.gerarHash("senha-segura");

		assertNotEquals("senha-segura", hash);
		assertTrue(service.comparar("senha-segura", hash));
		assertFalse(service.comparar("senha-incorreta", hash));
	}

	@Test
	void deveRecusarSenhaAusenteAoGerarHash() {
		assertThrows(IllegalArgumentException.class, () -> service.gerarHash(" "));
		assertThrows(IllegalArgumentException.class, () -> service.gerarHash(null));
	}

	@Test
	void deveRetornarFalsoParaCredencialIncompleta() {
		assertFalse(service.comparar(null, "hash"));
		assertFalse(service.comparar("senha", null));
		assertFalse(service.comparar("senha", ""));
	}

	@Test
	void deveRecusarFatorBcryptForaDoLimite() {
		assertThrows(IllegalArgumentException.class, () -> new SenhaService(3));
		assertThrows(IllegalArgumentException.class, () -> new SenhaService(32));
	}
}
