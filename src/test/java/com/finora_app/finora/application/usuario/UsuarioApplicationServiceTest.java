package com.finora_app.finora.application.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finora_app.finora.domain.usuario.Usuario;
import com.finora_app.finora.repository.usuario.EmailJaCadastradoException;
import com.finora_app.finora.repository.usuario.UsuarioPersistido;
import com.finora_app.finora.repository.usuario.UsuarioRepository;
import com.finora_app.finora.security.JwtService;
import com.finora_app.finora.security.SenhaService;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {

	private static final Instant AGORA = Instant.parse("2026-01-01T10:00:00Z");
	private static final LocalDate DATA_NASCIMENTO = LocalDate.of(1990, 1, 1);

	@Mock
	private UsuarioRepository repository;
	@Mock
	private SenhaService senhaService;
	@Mock
	private JwtService jwtService;

	private UsuarioApplicationService service;

	@BeforeEach
	void configurarService() {
		service = new UsuarioApplicationService(repository, senhaService, jwtService,
				Clock.fixed(AGORA, ZoneOffset.UTC));
	}

	@Test
	void deveCadastrarUsuarioAtivoComHashSemPersistirSenha() {
		when(senhaService.gerarHash("senha-segura")).thenReturn("$2a$hash");
		CadastroUsuarioResponse response = service.cadastrar(
				new CadastroUsuarioRequest("Pessoa Finora", "PESSOA@EXEMPLO.COM", "senha-segura", DATA_NASCIMENTO));

		assertEquals("pessoa@exemplo.com", response.email());
		assertEquals("ATIVO", response.status());
		verify(repository).salvar(any(Usuario.class), eq("$2a$hash"));
		verify(senhaService).gerarHash("senha-segura");
	}

	@Test
	void devePropagarConflitoDeEmailSemPersistirSegundaConta() {
		when(senhaService.gerarHash("senha-segura")).thenReturn("hash");
		doThrow(new EmailJaCadastradoException("duplicado", new RuntimeException()))
			.when(repository).salvar(any(Usuario.class), eq("hash"));

		assertThrows(EmailJaCadastradoException.class, () -> service.cadastrar(
				new CadastroUsuarioRequest("Pessoa", "pessoa@exemplo.com", "senha-segura", DATA_NASCIMENTO)));
	}

	@Test
	void deveRejeitarCadastroComCamposObrigatoriosAusentes() {
		assertThrows(IllegalArgumentException.class, () -> service.cadastrar(null));
		assertThrows(IllegalArgumentException.class, () -> service.cadastrar(
				new CadastroUsuarioRequest("", "pessoa@exemplo.com", "senha", DATA_NASCIMENTO)));
		assertThrows(IllegalArgumentException.class, () -> service.cadastrar(
				new CadastroUsuarioRequest("Pessoa", "pessoa@exemplo.com", "senha", null)));
		verify(repository, never()).salvar(any(), any());
	}

	@Test
	void deveAutenticarUsuarioAtivoComSenhaCorreta() {
		UUID id = UUID.randomUUID();
		Usuario usuario = Usuario.reidratar(id, "Pessoa", new com.finora_app.finora.domain.usuario.Email("pessoa@exemplo.com"),
				DATA_NASCIMENTO, com.finora_app.finora.domain.usuario.StatusUsuario.ATIVO,
				AGORA, AGORA, null);
		when(repository.buscarPorEmail(any())).thenReturn(Optional.of(new UsuarioPersistido(usuario, "hash")));
		when(senhaService.comparar("senha", "hash")).thenReturn(true);
		when(jwtService.emitir(id)).thenReturn("token-seguro");

		AutenticacaoResponse response = service.autenticar(new AutenticacaoRequest("PESSOA@EXEMPLO.COM", "senha"));

		assertEquals("token-seguro", response.token());
		verify(jwtService).emitir(id);
	}

	@Test
	void deveRejeitarCredencialIncorretaUsuarioInexistenteEContaInativa() {
		when(repository.buscarPorEmail(any())).thenReturn(Optional.empty());
		assertThrows(CredenciaisInvalidasException.class,
				() -> service.autenticar(new AutenticacaoRequest("ausente@exemplo.com", "senha")));

		UUID id = UUID.randomUUID();
		Usuario inativo = Usuario.reidratar(id, "Pessoa", new com.finora_app.finora.domain.usuario.Email("pessoa@exemplo.com"),
				DATA_NASCIMENTO, com.finora_app.finora.domain.usuario.StatusUsuario.INATIVO,
				AGORA, AGORA, AGORA);
		when(repository.buscarPorEmail(any())).thenReturn(Optional.of(new UsuarioPersistido(inativo, "hash")));
		assertThrows(CredenciaisInvalidasException.class,
				() -> service.autenticar(new AutenticacaoRequest("pessoa@exemplo.com", "senha")));
		verify(jwtService, never()).emitir(any());
	}

	@Test
	void deveRejeitarAutenticacaoComCamposAusentes() {
		assertThrows(IllegalArgumentException.class, () -> service.autenticar(null));
		assertThrows(IllegalArgumentException.class,
				() -> service.autenticar(new AutenticacaoRequest("", "senha")));
	}

	@Test
	void deveConsultarPerfilSemExporHash() {
		UUID id = UUID.randomUUID();
		Usuario usuario = Usuario.reidratar(id, "Pessoa", new com.finora_app.finora.domain.usuario.Email("pessoa@exemplo.com"),
				DATA_NASCIMENTO, com.finora_app.finora.domain.usuario.StatusUsuario.ATIVO,
				AGORA, AGORA, null);
		when(repository.buscarPorId(id)).thenReturn(Optional.of(new UsuarioPersistido(usuario, "hash-secreto")));

		PerfilUsuarioResponse response = service.consultarPerfil(id);

		assertEquals(id, response.id());
		assertEquals("pessoa@exemplo.com", response.email());
		assertEquals(DATA_NASCIMENTO, response.dataNascimento());
		assertEquals("ATIVO", response.status());
		verify(repository).buscarPorId(id);
	}

	@Test
	void deveAtualizarPerfilDoUsuarioAutenticado() {
		UUID id = UUID.randomUUID();
		Usuario usuario = Usuario.reidratar(id, "Pessoa", new com.finora_app.finora.domain.usuario.Email("pessoa@exemplo.com"),
				DATA_NASCIMENTO, com.finora_app.finora.domain.usuario.StatusUsuario.ATIVO,
				AGORA, AGORA, null);
		when(repository.buscarPorId(id)).thenReturn(Optional.of(new UsuarioPersistido(usuario, "hash-secreto")));

		PerfilUsuarioResponse response = service.atualizarPerfil(id,
				new AtualizarPerfilRequest("Pessoa Atualizada", DATA_NASCIMENTO.plusDays(1)));

		assertEquals("Pessoa Atualizada", response.nome());
		assertEquals(AGORA, response.updatedAt());
		verify(repository).atualizar(usuario);
	}

	@Test
	void deveRejeitarConsultaEAtualizacaoDeContaInativa() {
		UUID id = UUID.randomUUID();
		Usuario usuario = Usuario.reidratar(id, "Pessoa", new com.finora_app.finora.domain.usuario.Email("pessoa@exemplo.com"),
				DATA_NASCIMENTO, com.finora_app.finora.domain.usuario.StatusUsuario.INATIVO,
				AGORA, AGORA, AGORA);
		when(repository.buscarPorId(id)).thenReturn(Optional.of(new UsuarioPersistido(usuario, "hash-secreto")));

		assertThrows(com.finora_app.finora.repository.usuario.UsuarioNaoEncontradoException.class,
				() -> service.consultarPerfil(id));
		assertThrows(com.finora_app.finora.repository.usuario.UsuarioNaoEncontradoException.class,
				() -> service.atualizarPerfil(id, new AtualizarPerfilRequest("Outro Nome", DATA_NASCIMENTO)));
		verify(repository, never()).atualizar(any());
	}

	@Test
	void deveInativarContaConfirmadaERegistrarMomento() {
		UUID id = UUID.randomUUID();
		Usuario usuario = Usuario.reidratar(id, "Pessoa", new com.finora_app.finora.domain.usuario.Email("pessoa@exemplo.com"),
				DATA_NASCIMENTO, com.finora_app.finora.domain.usuario.StatusUsuario.ATIVO,
				AGORA, AGORA, null);
		when(repository.buscarPorId(id)).thenReturn(Optional.of(new UsuarioPersistido(usuario, "hash-secreto")));

		InativacaoContaResponse response = service.inativarConta(id, new InativarContaRequest(true));

		assertEquals("INATIVO", response.status());
		assertEquals(AGORA, response.deactivatedAt());
		verify(repository).inativar(usuario, AGORA);
	}

	@Test
	void deveRejeitarInativacaoSemConfirmacaoSemPersistir() {
		UUID id = UUID.randomUUID();

		assertThrows(IllegalArgumentException.class, () -> service.inativarConta(id, new InativarContaRequest(false)));
		assertThrows(IllegalArgumentException.class, () -> service.inativarConta(id, null));
		verifyNoInteractions(repository);
	}
}
