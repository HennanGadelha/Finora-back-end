package com.finora_app.finora.application.usuario;

import java.util.UUID;

import com.finora_app.finora.domain.usuario.Usuario;

public record CadastroUsuarioResponse(UUID id, String nome, String email, String status) {

	public static CadastroUsuarioResponse de(Usuario usuario) {
		return new CadastroUsuarioResponse(usuario.id(), usuario.nome(), usuario.email().valor(), usuario.status().name());
	}
}
