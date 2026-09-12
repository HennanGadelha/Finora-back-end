package com.finora_app.finora.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

class SecurityConfigTest {

	@Test
	void devePermitirMetodosEHeadersDoFrontendNaConfiguracaoCors() {
		MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/register");
		request.addHeader(HttpHeaders.ORIGIN, "http://localhost:5000");
		request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
		request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type");
		CorsConfiguration configuration = new SecurityConfig().corsConfigurationSource()
				.getCorsConfiguration(request);

		assertEquals(java.util.List.of("*"), configuration.getAllowedOriginPatterns());
		assertTrue(configuration.getAllowedMethods().contains("OPTIONS"));
		assertTrue(configuration.getAllowedMethods().contains("POST"));
		assertTrue(configuration.getAllowedHeaders().contains("Content-Type"));
		assertTrue(configuration.getAllowedHeaders().contains("Authorization"));
	}
}
