package com.finora_app.finora.repository.usuario;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.finora_app.finora.domain.usuario.Usuario;

class UsuarioPersistidoTest {

	@Test
	void deveRejeitarRegistroSemUsuarioOuHash() {
		Usuario usuario = Usuario.criar("Pessoa", "pessoa@exemplo.com", LocalDate.of(1990, 1, 1),
				Instant.parse("2026-01-01T00:00:00Z"));

		assertThrows(IllegalArgumentException.class, () -> new UsuarioPersistido(null, "hash"));
		assertThrows(IllegalArgumentException.class, () -> new UsuarioPersistido(usuario, null));
		assertThrows(IllegalArgumentException.class, () -> new UsuarioPersistido(usuario, " "));
	}
}