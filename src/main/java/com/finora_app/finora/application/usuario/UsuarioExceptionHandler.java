package com.finora_app.finora.application.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.finora_app.finora.repository.usuario.EmailJaCadastradoException;

@RestControllerAdvice
public class UsuarioExceptionHandler {

	@ExceptionHandler(EmailJaCadastradoException.class)
	ResponseEntity<ErroResponse> emailDuplicado(EmailJaCadastradoException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResponse("E-mail ja cadastrado"));
	}

	@ExceptionHandler(CredenciaisInvalidasException.class)
	ResponseEntity<ErroResponse> credenciaisInvalidas(CredenciaisInvalidasException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErroResponse("Credenciais invalidas"));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<ErroResponse> dadosInvalidos(RuntimeException exception) {
		return ResponseEntity.badRequest().body(new ErroResponse(exception.getMessage()));
	}
}
