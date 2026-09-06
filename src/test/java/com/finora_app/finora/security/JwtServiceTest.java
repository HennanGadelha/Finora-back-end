package com.finora_app.finora.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import javax.crypto.spec.SecretKeySpec;

class JwtServiceTest {

	private static final String SECRET = "12345678901234567890123456789012";
	private final JwtProperties properties = new JwtProperties(SECRET, Duration.ofMinutes(15));
	private final JwtService service = new JwtService(properties);

	@Test
	void deveEmitirEValidarIdentidade() {
		UUID usuarioId = UUID.randomUUID();

		String token = service.emitir(usuarioId);

		assertEquals(usuarioId, service.validar(token));
	}

	@Test
	void deveRecusarEmissaoSemIdentidade() {
		assertThrows(IllegalArgumentException.class, () -> service.emitir(null));
	}

	@Test
	void deveRejeitarTokenComAssinaturaInvalida() {
		String token = service.emitir(UUID.randomUUID());
		String[] partes = token.split("\\.");
		String payloadAdulterado = (partes[1].charAt(0) == 'a' ? "b" : "a") + partes[1].substring(1);
		String tokenAdulterado = partes[0] + "." + payloadAdulterado + "." + partes[2];

		assertThrows(RuntimeException.class, () -> service.validar(tokenAdulterado));
	}

	@Test
	void deveRejeitarTokenExpirado() {
		JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(
				new SecretKeySpec(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256")).build();
		String token = encoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				JwtClaimsSet.builder().subject(UUID.randomUUID().toString())
						.issuedAt(java.time.Instant.now().minusSeconds(120))
						.expiresAt(java.time.Instant.now().minusSeconds(60)).build())).getTokenValue();

		assertThrows(RuntimeException.class, () -> service.validar(token));
	}

	@Test
	void deveRejeitarTokenSemIdentidadeValida() {
		JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(
				new SecretKeySpec(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256")).build();
		String token = encoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				JwtClaimsSet.builder().subject("identidade-invalida").build())).getTokenValue();

		assertThrows(IllegalArgumentException.class, () -> service.validar(token));
		String tokenSemSubject = encoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), JwtClaimsSet.builder()
						.issuedAt(java.time.Instant.now())
						.expiresAt(java.time.Instant.now().plusSeconds(60)).build())).getTokenValue();
		assertThrows(IllegalArgumentException.class, () -> service.validar(tokenSemSubject));
		assertThrows(IllegalArgumentException.class, () -> service.validar(null));
		assertThrows(IllegalArgumentException.class, () -> service.validar(" "));
	}
}
