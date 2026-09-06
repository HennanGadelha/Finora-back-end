package com.finora_app.finora.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
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
	private static final LocalDate DATA_NASCIMENTO = LocalDate.of(1990, 1, 1);

	private final UsuarioApplicationService service = org.mockito.Mockito.mock(UsuarioApplicationService.class);
	private final UsuarioRepository repository = org.mockito.Mockito.mock(UsuarioRepository.class);

	@Test
	void deveCriarUsuarioMockadoQuandoAindaNaoExiste() {
		when(repository.buscarPorEmail(any())).thenReturn(Optional.empty());
		UsuarioMockadoInitializer initializer = new UsuarioMockadoInitializer(service, repository, "Usuario de Teste",
				EMAIL, "senha-fornecida-no-ambiente", DATA_NASCIMENTO);

		initializer.run();

		verify(service).cadastrar(new com.finora_app.finora.application.usuario.CadastroUsuarioRequest("Usuario de Teste",
				EMAIL, "senha-fornecida-no-ambiente", DATA_NASCIMENTO));
	}

	@Test
	void deveReutilizarUsuarioMockadoAtivoSemCadastrarNovamente() {
		Usuario usuario = Usuario.criar("Usuario de Teste", EMAIL, DATA_NASCIMENTO,
				Instant.parse("2026-01-01T10:00:00Z"));
		when(repository.buscarPorEmail(any())).thenReturn(Optional.of(new UsuarioPersistido(usuario, "hash")));
		UsuarioMockadoInitializer initializer = new UsuarioMockadoInitializer(service, repository, "Usuario de Teste",
				EMAIL, "senha", DATA_NASCIMENTO);

		initializer.run();

		verify(service, never()).cadastrar(any());
	}

	@Test
	void deveRejeitarUsuarioMockadoExistenteInativo() {
		UUID id = UUID.randomUUID();
		Instant agora = Instant.parse("2026-01-01T10:00:00Z");
		Usuario usuario = Usuario.reidratar(id, "Usuario de Teste", new Email(EMAIL), DATA_NASCIMENTO,
				com.finora_app.finora.domain.usuario.StatusUsuario.INATIVO, agora, agora, agora);
		when(repository.buscarPorEmail(any())).thenReturn(Optional.of(new UsuarioPersistido(usuario, "hash")));
		UsuarioMockadoInitializer initializer = new UsuarioMockadoInitializer(service, repository, "Usuario de Teste",
				EMAIL, "senha", DATA_NASCIMENTO);

		assertThrows(IllegalStateException.class, initializer::run);
		verify(service, never()).cadastrar(any());
	}
}