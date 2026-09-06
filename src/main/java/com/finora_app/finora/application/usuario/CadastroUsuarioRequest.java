package com.finora_app.finora.application.usuario;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.OptBoolean;

@JsonIgnoreProperties(ignoreUnknown = false)
public record CadastroUsuarioRequest(String nome, String email, String senha,
		@JsonFormat(pattern = "dd/MM/uuuu", lenient = OptBoolean.FALSE) LocalDate dataNascimento) {

	@JsonAnySetter
	public void rejeitarCampoDesconhecido(String nome, Object valor) {
		throw new IllegalArgumentException("Campo de requisicao desconhecido: " + nome);
	}
}
