package com.finora_app.finora.application.usuario;

import io.swagger.v3.oas.annotations.media.Schema;

public record CadastroUsuarioRequest(String nome, String email,
		@Schema(accessMode = Schema.AccessMode.WRITE_ONLY) String senha) {
}
