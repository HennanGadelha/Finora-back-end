package com.finora_app.finora.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private static final MacAlgorithm ALGORITHM = MacAlgorithm.HS256;

	private final JwtEncoder encoder;
	private final JwtDecoder decoder;
	private final JwtProperties properties;

	public JwtService(JwtProperties properties) {
		this.properties = properties;
		SecretKeySpec secretKey = new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		this.encoder = NimbusJwtEncoder.withSecretKey(secretKey).build();
		this.decoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(ALGORITHM).build();
	}

	public String emitir(UUID usuarioId) {
		if (usuarioId == null) {
			throw new IllegalArgumentException("Identidade do usuario e obrigatoria");
		}
		Instant emitidoEm = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(usuarioId.toString())
				.issuedAt(emitidoEm)
				.expiresAt(emitidoEm.plus(properties.expiration()))
				.build();
		return encoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(ALGORITHM).build(), claims)).getTokenValue();
	}

	public UUID validar(String token) {
		if (token == null || token.isBlank()) {
			throw new IllegalArgumentException("Token e obrigatorio");
		}
		Jwt jwt = decoder.decode(token);
		String subject = jwt.getSubject();
		if (subject == null || subject.isBlank()) {
			throw new IllegalArgumentException("Token sem identidade");
		}
		try {
			return UUID.fromString(subject);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Identidade do token invalida", exception);
		}
	}
}
