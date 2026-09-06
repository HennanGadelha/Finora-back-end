package com.finora_app.finora.application.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UsuarioController {

	private final UsuarioApplicationService service;

	public UsuarioController(UsuarioApplicationService service) {
		this.service = service;
	}

	@PostMapping("/auth/register")
	public ResponseEntity<CadastroUsuarioResponse> cadastrar(@RequestBody CadastroUsuarioRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(request));
	}

	@PostMapping("/auth/login")
	public AutenticacaoResponse autenticar(@RequestBody AutenticacaoRequest request) {
		return service.autenticar(request);
	}

	@GetMapping("/users/me")
	public PerfilUsuarioResponse consultarPerfil(@AuthenticationPrincipal UUID usuarioId) {
		return service.consultarPerfil(usuarioId);
	}

	@PutMapping("/users/me")
	public PerfilUsuarioResponse atualizarPerfil(@AuthenticationPrincipal UUID usuarioId,
			@RequestBody AtualizarPerfilRequest request) {
		return service.atualizarPerfil(usuarioId, request);
	}
}
