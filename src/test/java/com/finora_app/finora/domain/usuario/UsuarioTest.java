package com.finora_app.finora.domain.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class UsuarioTest {

	private static final Instant CRIACAO = Instant.parse("2026-01-01T10:00:00Z");
	private static final LocalDate DATA_NASCIMENTO = LocalDate.of(1990, 1, 1);

	@Test
	void deveCriarUsuarioAtivoComValoresPadrao() {
		Usuario usuario = Usuario.criar("Pessoa Finora", "Pessoa@Exemplo.com", DATA_NASCIMENTO, CRIACAO);

		assertNotNull(usuario.id());
		assertEquals("pessoa@exemplo.com", usuario.email().valor());
		assertEquals(StatusUsuario.ATIVO, usuario.status());
		assertEquals(true, usuario.estaAtivo());
		assertEquals(DATA_NASCIMENTO, usuario.dataNascimento());
		assertEquals(CRIACAO, usuario.createdAt());
		assertEquals(CRIACAO, usuario.updatedAt());
		assertNull(usuario.deactivatedAt());
	}

	@Test
	void deveAtualizarSomenteDadosCadastraisPermitidos() {
		Usuario usuario = Usuario.criar("Nome Inicial", "pessoa@exemplo.com", DATA_NASCIMENTO, CRIACAO);
		Instant atualizacao = Instant.parse("2026-01-02T10:00:00Z");

		usuario.atualizarDadosCadastrais("Nome Atualizado", DATA_NASCIMENTO.plusDays(1), atualizacao);

		assertEquals("Nome Atualizado", usuario.nome());
		assertEquals("pessoa@exemplo.com", usuario.email().valor());
		assertEquals(StatusUsuario.ATIVO, usuario.status());
		assertEquals(atualizacao, usuario.updatedAt());
	}

	@Test
	void deveRejeitarDadosCadastraisInvalidos() {
		Usuario usuario = Usuario.criar("Nome Inicial", "pessoa@exemplo.com", DATA_NASCIMENTO, CRIACAO);
		String nomeMuitoLongo = "N".repeat(121);

		assertThrows(ErroDeDominioException.class,
				() -> usuario.atualizarDadosCadastrais("", DATA_NASCIMENTO, CRIACAO));
		assertThrows(ErroDeDominioException.class,
				() -> usuario.atualizarDadosCadastrais(nomeMuitoLongo, DATA_NASCIMENTO, CRIACAO));
		assertThrows(ErroDeDominioException.class,
				() -> usuario.atualizarDadosCadastrais("Nome", null, CRIACAO));
		assertEquals("Nome Inicial", usuario.nome());
		assertEquals(CRIACAO, usuario.updatedAt());
	}

	@Test
	void deveInativarContaAtivaRegistrandoData() {
		Usuario usuario = Usuario.criar("Pessoa Finora", "pessoa@exemplo.com", DATA_NASCIMENTO, CRIACAO);
		Instant inativacao = Instant.parse("2026-01-03T10:00:00Z");

		usuario.inativar(inativacao);

		assertFalse(usuario.estaAtivo());
		assertEquals(StatusUsuario.INATIVO, usuario.status());
		assertEquals(inativacao, usuario.deactivatedAt());
		assertEquals(inativacao, usuario.updatedAt());
	}

	@Test
	void deveRejeitarEmailNuloNaCriacao() {
		assertThrows(ErroDeDominioException.class,
				() -> Usuario.criar("Pessoa", (Email) null, DATA_NASCIMENTO, CRIACAO));
	}

	@Test
	void naoDeveInativarContaMaisDeUmaVez() {
		Usuario usuario = Usuario.criar("Pessoa Finora", "pessoa@exemplo.com", DATA_NASCIMENTO, CRIACAO);
		usuario.inativar(Instant.parse("2026-01-03T10:00:00Z"));

		assertThrows(ErroDeDominioException.class,
				() -> usuario.inativar(Instant.parse("2026-01-04T10:00:00Z")));
	}

	@Test
	void deveRejeitarCamposObrigatoriosAusentes() {
		assertThrows(ErroDeDominioException.class,
				() -> Usuario.criar(null, "pessoa@exemplo.com", DATA_NASCIMENTO, CRIACAO));
		assertThrows(ErroDeDominioException.class,
				() -> Usuario.criar("Pessoa", "pessoa@exemplo.com", null, CRIACAO));
	}

	@Test
	void deveRejeitarEstadoPersistidoInconsistente() {
		Email email = new Email("pessoa@exemplo.com");

		assertThrows(ErroDeDominioException.class, () -> Usuario.reidratar(null, "Pessoa", email,
				DATA_NASCIMENTO, StatusUsuario.ATIVO, CRIACAO, CRIACAO, null));
		assertThrows(ErroDeDominioException.class, () -> Usuario.reidratar(null, "Pessoa", null,
				DATA_NASCIMENTO, StatusUsuario.ATIVO, CRIACAO, CRIACAO, null));
		assertThrows(ErroDeDominioException.class, () -> Usuario.reidratar(null, "Pessoa", email,
				DATA_NASCIMENTO, null, CRIACAO, CRIACAO, null));
		assertThrows(ErroDeDominioException.class, () -> Usuario.reidratar(null, "Pessoa", email,
				DATA_NASCIMENTO, StatusUsuario.ATIVO, CRIACAO, CRIACAO, CRIACAO));
		assertThrows(ErroDeDominioException.class, () -> Usuario.reidratar(null, "Pessoa", email,
				DATA_NASCIMENTO, StatusUsuario.INATIVO, CRIACAO, CRIACAO, null));
	}
}