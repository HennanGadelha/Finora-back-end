package com.finora_app.finora.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.finora_app.finora.application.usuario.UsuarioApplicationService;
import com.finora_app.finora.domain.usuario.Email;
import com.finora_app.finora.domain.usuario.Usuario;
import com.finora_app.finora.repository.usuario.UsuarioPersistido;
import com.finora_app.finora.repository.usuario.UsuarioRepository;

class UsuarioMockadoInitializerTest {

	private static final String EMAIL = "usuario.teste@finora.local";

	private final UsuarioApplicationService service = org.mockito.Mockito.mock(UsuarioApplicationService.class);
	private final UsuarioRepository repository = org.mockito.Mockito.mock(UsuarioRepository.class);

	@Test
	void deveCriarUsuarioMockadoQuandoAindaNaoExiste() {
		when(repository.buscarPorEmail(any())).thenReturn(Optional.empty());
		UsuarioMockadoInitializer initializer = new UsuarioMockadoInitializer(service, repository, "Usuario de Teste",
				EMAIL, "senha-fornecida-no-ambiente");

		initializer.run();

		verify(service).cadastrar(new com.finora_app.finora.application.usuario.CadastroUsuarioRequest("Usuario de Teste",
				EMAIL, "senha-fornecida-no-ambiente"));
	}

	@Test
	void deveReutilizarUsuarioMockadoAtivoSemCadastrarNovamente() {
		Usuario usuario = Usuario.criar("Usuario de Teste", EMAIL, Instant.parse("2026-01-01T10:00:00Z"));
		when(repository.buscarPorEmail(any())).thenReturn(Optional.of(new UsuarioPersistido(usuario, "hash")));
		UsuarioMockadoInitializer initializer = new UsuarioMockadoInitializer(service, repository, "Usuario de Teste",
				EMAIL, "senha");

		initializer.run();

		verify(service, never()).cadastrar(any());
	}

	@Test
	void deveRejeitarUsuarioMockadoExistenteInativo() {
		UUID id = UUID.randomUUID();
		Instant agora = Instant.parse("2026-01-01T10:00:00Z");
		Usuario usuario = Usuario.reidratar(id, "Usuario de Teste", new Email(EMAIL), "BRL", "America/Sao_Paulo",
				com.finora_app.finora.domain.usuario.StatusUsuario.INATIVO, agora, agora, agora);
		when(repository.buscarPorEmail(any())).thenReturn(Optional.of(new UsuarioPersistido(usuario, "hash")));
		UsuarioMockadoInitializer initializer = new UsuarioMockadoInitializer(service, repository, "Usuario de Teste",
				EMAIL, "senha");

		assertThrows(IllegalStateException.class, initializer::run);
		verify(service, never()).cadastrar(any());
	}
}