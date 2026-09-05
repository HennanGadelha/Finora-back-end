package com.finora_app.finora.domain.usuario;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String valor) {

	private static final int TAMANHO_MAXIMO = 320;
	private static final Pattern FORMATO_VALIDO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	public Email {
		if (valor == null || valor.isBlank()) {
			throw new ErroDeDominioException("E-mail e obrigatorio");
		}

		String valorNormalizado = valor.trim().toLowerCase(Locale.ROOT);
		if (valorNormalizado.length() > TAMANHO_MAXIMO || !FORMATO_VALIDO.matcher(valorNormalizado).matches()) {
			throw new ErroDeDominioException("E-mail invalido");
		}
		valor = valorNormalizado;
	}
}