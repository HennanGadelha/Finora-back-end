package com.finora_app.finora.application.usuario;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.finora_app.finora.domain.usuario.Usuario;

public record PerfilUsuarioResponse(UUID id, String nome, String email,
		@JsonFormat(pattern = "dd/MM/uuuu") LocalDate dataNascimento, String status, Instant createdAt,
		Instant updatedAt) {

	public static PerfilUsuarioResponse de(Usuario usuario) {
		return new PerfilUsuarioResponse(usuario.id(), usuario.nome(), usuario.email().valor(),
				usuario.dataNascimento(), usuario.status().name(), usuario.createdAt(), usuario.updatedAt());
	}
}