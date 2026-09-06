package com.finora_app.finora.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "JWT_SECRET=test-only-jwt-secret-with-at-least-32-bytes")
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deveExporContratoOpenApiComRotasEAutenticacaoBearer() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("/api/auth/register")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("/api/users/me")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("bearerAuth")))
				.andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("hash-secreto"))));
	}

	@Test
	void deveExporSwaggerUiEManterRotasProtegidas() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/users/me"))
				.andExpect(status().isForbidden());
	}
}