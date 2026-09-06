package com.finora_app.finora.application.usuario;

public class CredenciaisInvalidasException extends RuntimeException {

	public CredenciaisInvalidasException() {
		super("Credenciais invalidas");
	}
}
