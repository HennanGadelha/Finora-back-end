package com.finora_app.finora.repository.usuario;

import com.finora_app.finora.domain.usuario.Usuario;

public record UsuarioPersistido(Usuario usuario, String senhaHash) {

	public UsuarioPersistido {
		if (usuario == null || senhaHash == null || senhaHash.isBlank()) {
			throw new IllegalArgumentException("Usuario e hash da senha sao obrigatorios");
		}
	}
}