package com.finora_app.finora.domain.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EmailTest {

	@Test
	void deveNormalizarEmailParaMinusculas() {
		Email email = new Email("  Pessoa@EXEMPLO.COM ");

		assertEquals("pessoa@exemplo.com", email.valor());
	}

	@Test
	void deveRejeitarEmailInvalido() {
		assertThrows(ErroDeDominioException.class, () -> new Email("email-invalido"));
	}

	@Test
	void deveRejeitarEmailAusente() {
		assertThrows(ErroDeDominioException.class, () -> new Email(null));
	}

	@Test
	void deveRejeitarEmailEmBranco() {
		assertThrows(ErroDeDominioException.class, () -> new Email("   "));
	}

	@Test
	void deveRejeitarEmailMuitoLongo() {
		String email = "a".repeat(312) + "@example.com";

		assertThrows(ErroDeDominioException.class, () -> new Email(email));
	}
}