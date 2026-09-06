package com.finora_app.finora.application.usuario;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UsuarioControllerTest {

	private final UsuarioApplicationService service = mock(UsuarioApplicationService.class);
	private MockMvc mockMvc;

	@BeforeEach
	void configurarMockMvc() {
		mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(service))
				.setControllerAdvice(new UsuarioExceptionHandler()).build();
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
}
