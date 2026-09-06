package com.finora_app.finora.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import com.finora_app.finora.domain.usuario.Usuario;
import com.finora_app.finora.repository.usuario.UsuarioPersistido;
import com.finora_app.finora.repository.usuario.UsuarioRepository;

import jakarta.servlet.FilterChain;

class JwtAuthenticationFilterTest {

	private final JwtService jwtService = new JwtService(
			new JwtProperties("12345678901234567890123456789012", java.time.Duration.ofMinutes(15)));
	private final UsuarioRepository repository = mock(UsuarioRepository.class);
	private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, repository);

	@AfterEach
	void limparContexto() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void deveProsseguirSemTokenEDeixarRequisicaoNaoAutenticada() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
		verifyNoInteractions(repository);
		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void deveProsseguirSemAutenticarEsquemaDiferenteDeBearer() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Basic credencial");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		verify(chain).doFilter(request, response);
		verifyNoInteractions(repository);
	}

	@Test
	void deveAutenticarTokenDeUsuarioAtivo() throws Exception {
		UUID usuarioId = UUID.randomUUID();
		Usuario usuario = Usuario.criar("Pessoa Finora", "pessoa@exemplo.com", Instant.now());
		UsuarioPersistido persistido = new UsuarioPersistido(usuarioComId(usuario, usuarioId), "hash");
		when(repository.buscarPorId(usuarioId)).thenReturn(Optional.of(persistido));
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer " + jwtService.emitir(usuarioId));
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		assertEquals(usuarioId, authentication.getPrincipal());
		assertEquals(200, response.getStatus());
		verify(chain).doFilter(request, response);
	}

	@Test
	void deveRejeitarTokenDeUsuarioInativo() throws Exception {
		UUID usuarioId = UUID.randomUUID();
		Usuario usuario = Usuario.criar("Pessoa Finora", "pessoa@exemplo.com", Instant.now());
		usuario.inativar(Instant.now());
		when(repository.buscarPorId(usuarioId)).thenReturn(Optional.of(new UsuarioPersistido(
				usuarioComId(usuario, usuarioId), "hash")));
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer " + jwtService.emitir(usuarioId));
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus());
		verifyNoInteractions(chain);
	}

	@Test
	void deveRejeitarTokenInvalido() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer token-invalido");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		assertEquals(401, response.getStatus());
		assertTrue(response.getErrorMessage().contains("Autenticacao invalida"));
		verifyNoInteractions(repository, chain);
	}

	private static Usuario usuarioComId(Usuario usuario, UUID id) {
		return Usuario.reidratar(id, usuario.nome(), usuario.email(), usuario.moeda(), usuario.fusoHorario(),
				usuario.status(), usuario.createdAt(), usuario.updatedAt(), usuario.deactivatedAt());
	}
}
