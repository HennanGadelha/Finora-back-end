package com.finora_app.finora.application.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

	private final UsuarioApplicationService service;

	public UsuarioController(UsuarioApplicationService service) {
		this.service = service;
	}

	@PostMapping("/register")
	public ResponseEntity<CadastroUsuarioResponse> cadastrar(@RequestBody CadastroUsuarioRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(request));
	}

	@PostMapping("/login")
	public AutenticacaoResponse autenticar(@RequestBody AutenticacaoRequest request) {
		return service.autenticar(request);
	}
}
