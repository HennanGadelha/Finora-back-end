package com.finora_app.finora.application.usuario;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

class UsuarioControllerTest {

	private final UsuarioApplicationService service = mock(UsuarioApplicationService.class);
	private MockMvc mockMvc;

	@BeforeEach
	void configurarMockMvc() {
		SecurityContextHolder.clearContext();
		mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(service))
				.setControllerAdvice(new UsuarioExceptionHandler())
				.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();
	}

	@Test
	void deveRetornarCadastroSemSenhaOuHash() throws Exception {
		UUID id = UUID.randomUUID();
		when(service.cadastrar(any())).thenReturn(new CadastroUsuarioResponse(id, "Pessoa", "pessoa@exemplo.com", "ATIVO"));

		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Pessoa\",\"email\":\"pessoa@exemplo.com\",\"senha\":\"senha\"}"))
				.andExpect(status().isCreated())
				.andExpect(content().string(not(containsString("senha"))))
				.andExpect(content().string(not(containsString("hash"))));
	}

	@Test
	void deveRetornarSomenteTokenNoLogin() throws Exception {
		when(service.autenticar(any())).thenReturn(new AutenticacaoResponse("token-seguro"));

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"pessoa@exemplo.com\",\"senha\":\"senha\"}"))
				.andExpect(status().isOk())
				.andExpect(content().json("{\"token\":\"token-seguro\"}"))
				.andExpect(content().string(not(containsString("senha"))))
				.andExpect(content().string(not(containsString("hash"))));
	}

	@Test
	void deveMapearErrosDeEntradaConflitoECredencial() throws Exception {
		doThrow(new IllegalArgumentException("Nome obrigatorio")).when(service).cadastrar(any());
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest());

		reset(service);
		doThrow(new com.finora_app.finora.repository.usuario.EmailJaCadastradoException(
				"duplicado", new RuntimeException())).when(service).cadastrar(any());
		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isConflict());

		when(service.autenticar(any())).thenThrow(new CredenciaisInvalidasException());
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveConsultarEAtualizarSomenteOUsuarioAutenticado() throws Exception {
		UUID id = UUID.randomUUID();
		when(service.consultarPerfil(id)).thenReturn(new PerfilUsuarioResponse(id, "Pessoa", "pessoa@exemplo.com",
				"BRL", "America/Sao_Paulo", "ATIVO", null, null));
		when(service.atualizarPerfil(any(), any())).thenReturn(new PerfilUsuarioResponse(id, "Pessoa Atualizada",
				"pessoa@exemplo.com", "BRL", "America/Sao_Paulo", "ATIVO", null, null));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(id, null));

		mockMvc.perform(get("/api/users/me"))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("hash"))))
				.andExpect(content().string(not(containsString("senha"))));
		mockMvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"Pessoa Atualizada\",\"moeda\":\"BRL\",\"fusoHorario\":\"America/Sao_Paulo\"}"))
				.andExpect(status().isOk());
		org.mockito.Mockito.verify(service).consultarPerfil(id);
		org.mockito.Mockito.verify(service).atualizarPerfil(org.mockito.ArgumentMatchers.eq(id), any());
	}

	@Test
	void deveInativarApenasAContaAutenticada() throws Exception {
		UUID id = UUID.randomUUID();
		when(service.inativarConta(any(), any())).thenReturn(new InativacaoContaResponse("INATIVO", null));
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(id, null));

		mockMvc.perform(post("/api/users/me/deactivate").contentType(MediaType.APPLICATION_JSON)
				.content("{\"confirmar\":true}"))
				.andExpect(status().isOk())
				.andExpect(content().json("{\"status\":\"INATIVO\"}"));
		org.mockito.Mockito.verify(service).inativarConta(org.mockito.ArgumentMatchers.eq(id), any());
	}
}
