package com.finora_app.finora.application.usuario;

import java.time.Instant;
import java.util.UUID;

import com.finora_app.finora.domain.usuario.Usuario;

public record PerfilUsuarioResponse(UUID id, String nome, String email, String moeda, String fusoHorario,

		String status, Instant createdAt, Instant updatedAt) {

	public static PerfilUsuarioResponse de(Usuario usuario) {
		return new PerfilUsuarioResponse(usuario.id(), usuario.nome(), usuario.email().valor(), usuario.moeda(),
				usuario.fusoHorario(), usuario.status().name(), usuario.createdAt(), usuario.updatedAt());
	}
}