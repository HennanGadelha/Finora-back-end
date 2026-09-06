package com.finora_app.finora.repository.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.finora_app.finora.domain.usuario.Email;
import com.finora_app.finora.domain.usuario.StatusUsuario;
import com.finora_app.finora.domain.usuario.Usuario;

@SpringBootTest(properties = "JWT_SECRET=test-only-jwt-secret-with-at-least-32-bytes")
class JdbcUsuarioRepositoryIntegrationTest {

	private static final Instant CRIACAO = Instant.parse("2026-01-01T10:00:00Z");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UsuarioRepository repository;

	@BeforeEach
	void limparUsuarios() {
		jdbcTemplate.update("DELETE FROM users");
	}

	@Test
	void deveCriarEConsultarUsuarioPorEmailEIdentidade() {
		Usuario usuario = criarUsuario("Pessoa@Exemplo.com");

		repository.salvar(usuario, "{bcrypt}hash-da-senha");

		UsuarioPersistido porEmail = repository.buscarPorEmail(new Email("PESSOA@EXEMPLO.COM")).orElseThrow();
		UsuarioPersistido porId = repository.buscarPorId(usuario.id()).orElseThrow();

		assertEquals(usuario.id(), porEmail.usuario().id());
		assertEquals("pessoa@exemplo.com", porEmail.usuario().email().valor());
		assertEquals("{bcrypt}hash-da-senha", porEmail.senhaHash());
		assertEquals(usuario.id(), porId.usuario().id());
		assertEquals(StatusUsuario.ATIVO, porId.usuario().status());
		assertNotNull(porId.usuario().createdAt());
	}

	@Test
	void deveRetornarVazioQuandoUsuarioNaoExiste() {
		assertTrue(repository.buscarPorEmail(new Email("ausente@exemplo.com")).isEmpty());
		assertTrue(repository.buscarPorId(UUID.randomUUID()).isEmpty());
	}

	@Test
	void deveTraduzirEmailDuplicado() {
		repository.salvar(criarUsuario("primeiro@exemplo.com"), "hash-1");

		assertThrows(EmailJaCadastradoException.class,
				() -> repository.salvar(criarUsuario("PRIMEIRO@EXEMPLO.COM"), "hash-2"));
	}

	@Test
	void deveAtualizarSomenteDadosCadastrais() {
		Usuario usuario = criarUsuario("pessoa@exemplo.com");
		repository.salvar(usuario, "hash-original");
		usuario.atualizarDadosCadastrais("Nome Atualizado", "BRL", "America/Sao_Paulo",
				Instant.parse("2026-01-02T10:00:00Z"));

		repository.atualizar(usuario);
		UsuarioPersistido atualizado = repository.buscarPorId(usuario.id()).orElseThrow();

		assertEquals("Nome Atualizado", atualizado.usuario().nome());
		assertEquals("hash-original", atualizado.senhaHash());
		assertEquals("pessoa@exemplo.com", atualizado.usuario().email().valor());
		assertEquals(StatusUsuario.ATIVO, atualizado.usuario().status());
	}

	@Test
	void deveInativarPreservandoCadastroERegistrandoData() {
		Usuario usuario = criarUsuario("pessoa@exemplo.com");
		repository.salvar(usuario, "hash-original");
		Instant inativacao = Instant.parse("2026-01-03T10:00:00Z");

		repository.inativar(usuario, inativacao);
		UsuarioPersistido inativo = repository.buscarPorId(usuario.id()).orElseThrow();

		assertEquals(StatusUsuario.INATIVO, inativo.usuario().status());
		assertEquals(inativacao, inativo.usuario().deactivatedAt());
		assertEquals("pessoa@exemplo.com", inativo.usuario().email().valor());
		assertEquals("hash-original", inativo.senhaHash());
		assertFalse(inativo.usuario().estaAtivo());
	}

	@Test
	void deveRejeitarOperacoesComArgumentosObrigatoriosAusentes() {
		assertThrows(IllegalArgumentException.class, () -> repository.salvar(null, "hash"));
		assertThrows(IllegalArgumentException.class, () -> repository.salvar(criarUsuario("a@exemplo.com"), ""));
		assertThrows(IllegalArgumentException.class, () -> repository.salvar(criarUsuario("c@exemplo.com"), null));
		assertThrows(IllegalArgumentException.class, () -> repository.buscarPorEmail(null));
		assertThrows(IllegalArgumentException.class, () -> repository.buscarPorId(null));
		assertThrows(IllegalArgumentException.class, () -> repository.atualizar(null));
		assertThrows(IllegalArgumentException.class, () -> repository.inativar(null, CRIACAO));
		assertThrows(IllegalArgumentException.class, () -> repository.inativar(criarUsuario("b@exemplo.com"), null));
	}

	@Test
	void deveRejeitarAtualizacaoEInativacaoDeUsuarioInexistente() {
		Usuario usuario = criarUsuario("ausente@exemplo.com");

		assertThrows(UsuarioNaoEncontradoException.class, () -> repository.atualizar(usuario));
		assertThrows(UsuarioNaoEncontradoException.class, () -> repository.inativar(usuario, CRIACAO));
	}

	private static Usuario criarUsuario(String email) {
		return Usuario.criar("Pessoa Finora", email, CRIACAO);
	}
}