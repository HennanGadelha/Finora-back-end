package com.finora_app.finora.domain.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class UsuarioTest {

	private static final Instant CRIACAO = Instant.parse("2026-01-01T10:00:00Z");

	@Test
	void deveCriarUsuarioAtivoComValoresPadrao() {
		Usuario usuario = Usuario.criar("Pessoa Finora", "Pessoa@Exemplo.com", CRIACAO);

		assertNotNull(usuario.id());
		assertEquals("pessoa@exemplo.com", usuario.email().valor());
		assertEquals(StatusUsuario.ATIVO, usuario.status());
		assertEquals(true, usuario.estaAtivo());
		assertEquals("BRL", usuario.moeda());
		assertEquals("America/Sao_Paulo", usuario.fusoHorario());
		assertEquals(CRIACAO, usuario.createdAt());
		assertEquals(CRIACAO, usuario.updatedAt());
		assertNull(usuario.deactivatedAt());
	}

	@Test
	void deveAtualizarSomenteDadosCadastraisPermitidos() {
		Usuario usuario = Usuario.criar("Nome Inicial", "pessoa@exemplo.com", CRIACAO);
		Instant atualizacao = Instant.parse("2026-01-02T10:00:00Z");

		usuario.atualizarDadosCadastrais("Nome Atualizado", "BRL", "America/Sao_Paulo", atualizacao);

		assertEquals("Nome Atualizado", usuario.nome());
		assertEquals("pessoa@exemplo.com", usuario.email().valor());
		assertEquals(StatusUsuario.ATIVO, usuario.status());
		assertEquals(atualizacao, usuario.updatedAt());
	}

	@Test
	void deveRejeitarDadosCadastraisInvalidos() {
		Usuario usuario = Usuario.criar("Nome Inicial", "pessoa@exemplo.com", CRIACAO);
		String nomeMuitoLongo = "N".repeat(121);

		assertThrows(ErroDeDominioException.class,
				() -> usuario.atualizarDadosCadastrais("", "BRL", "America/Sao_Paulo", CRIACAO));
		assertThrows(ErroDeDominioException.class,
				() -> usuario.atualizarDadosCadastrais(nomeMuitoLongo, "BRL", "America/Sao_Paulo", CRIACAO));
		assertThrows(ErroDeDominioException.class,
				() -> usuario.atualizarDadosCadastrais("Nome", "USD", "America/Sao_Paulo", CRIACAO));
		assertThrows(ErroDeDominioException.class,
				() -> usuario.atualizarDadosCadastrais("Nome", "BRL", "UTC", CRIACAO));
		assertEquals("Nome Inicial", usuario.nome());
		assertEquals(CRIACAO, usuario.updatedAt());
	}

	@Test
	void deveInativarContaAtivaRegistrandoData() {
		Usuario usuario = Usuario.criar("Pessoa Finora", "pessoa@exemplo.com", CRIACAO);
		Instant inativacao = Instant.parse("2026-01-03T10:00:00Z");

		usuario.inativar(inativacao);

		assertFalse(usuario.estaAtivo());
		assertEquals(StatusUsuario.INATIVO, usuario.status());
		assertEquals(inativacao, usuario.deactivatedAt());
		assertEquals(inativacao, usuario.updatedAt());
	}

	@Test
	void deveRejeitarEmailNuloNaCriacao() {
		assertThrows(ErroDeDominioException.class, () -> Usuario.criar("Pessoa", (Email) null, CRIACAO));
	}

	@Test
	void naoDeveInativarContaMaisDeUmaVez() {
		Usuario usuario = Usuario.criar("Pessoa Finora", "pessoa@exemplo.com", CRIACAO);
		usuario.inativar(Instant.parse("2026-01-03T10:00:00Z"));

		assertThrows(ErroDeDominioException.class,
				() -> usuario.inativar(Instant.parse("2026-01-04T10:00:00Z")));
	}

	@Test
	void deveRejeitarCamposObrigatoriosAusentes() {
		assertThrows(ErroDeDominioException.class,
				() -> Usuario.criar(null, "pessoa@exemplo.com", CRIACAO));
		assertThrows(ErroDeDominioException.class,
				() -> Usuario.criar("Pessoa", "pessoa@exemplo.com", null));
	}
}