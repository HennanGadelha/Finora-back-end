package com.finora_app.finora.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SenhaService {

	private final BCryptPasswordEncoder encoder;

	public SenhaService(@Value("${security.password.bcrypt.strength:12}") int strength) {
		this.encoder = new BCryptPasswordEncoder(validarStrength(strength));
	}

	public String gerarHash(String senha) {
		validarSenha(senha);
		return encoder.encode(senha);
	}

	public boolean comparar(String senha, String hash) {
		if (senha == null || hash == null || hash.isBlank()) {
			return false;
		}
		return encoder.matches(senha, hash);
	}

	private static int validarStrength(int strength) {
		if (strength < 4 || strength > 31) {
			throw new IllegalArgumentException("O fator BCrypt deve estar entre 4 e 31");
		}
		return strength;
	}

	private static void validarSenha(String senha) {
		if (senha == null || senha.isBlank()) {
			throw new IllegalArgumentException("Senha e obrigatoria");
		}
	}
}
