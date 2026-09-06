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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Usuários", description = "Cadastro, autenticação e ciclo de vida da própria conta")
public class UsuarioController {

	private final UsuarioApplicationService service;

	public UsuarioController(UsuarioApplicationService service) {
		this.service = service;
	}

	@PostMapping("/auth/register")
	@Operation(summary = "Cadastrar usuário", description = "Cria uma conta ativa sem retornar senha ou hash.")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Conta criada"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
			@ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content(schema = @Schema(implementation = ErroResponse.class))) })
	public ResponseEntity<CadastroUsuarioResponse> cadastrar(@RequestBody CadastroUsuarioRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(request));
	}

	@PostMapping("/auth/login")
	@Operation(summary = "Autenticar usuário", description = "Emite um JWT para uma conta ativa com credenciais válidas.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "JWT emitido"),
			@ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content(schema = @Schema(implementation = ErroResponse.class))) })
	public AutenticacaoResponse autenticar(@RequestBody AutenticacaoRequest request) {
		return service.autenticar(request);
	}

	@GetMapping("/users/me")
	@Operation(summary = "Consultar meu perfil", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Perfil retornado"),
			@ApiResponse(responseCode = "401", description = "Autenticação necessária"),
			@ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ErroResponse.class))) })
	public PerfilUsuarioResponse consultarPerfil(@AuthenticationPrincipal UUID usuarioId) {
		return service.consultarPerfil(usuarioId);
	}

	@PutMapping("/users/me")
	@Operation(summary = "Atualizar meu perfil", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Perfil atualizado"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
			@ApiResponse(responseCode = "401", description = "Autenticação necessária") })
	public PerfilUsuarioResponse atualizarPerfil(@AuthenticationPrincipal UUID usuarioId,
			@RequestBody AtualizarPerfilRequest request) {
		return service.atualizarPerfil(usuarioId, request);
	}

	@PostMapping("/users/me/deactivate")
	@Operation(summary = "Inativar minha conta", description = "Inativa a conta somente após confirmação explícita.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Conta inativada"),
			@ApiResponse(responseCode = "400", description = "Confirmação ausente ou inválida", content = @Content(schema = @Schema(implementation = ErroResponse.class))),
			@ApiResponse(responseCode = "401", description = "Autenticação necessária") })
	public InativacaoContaResponse inativarConta(@AuthenticationPrincipal UUID usuarioId,
			@RequestBody InativarContaRequest request) {
		return service.inativarConta(usuarioId, request);
	}
}
