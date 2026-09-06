package com.finora_app.finora.application.usuario;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finora_app.finora.domain.usuario.Usuario;
import com.finora_app.finora.domain.usuario.Email;
import com.finora_app.finora.repository.usuario.EmailJaCadastradoException;
import com.finora_app.finora.repository.usuario.UsuarioPersistido;
import com.finora_app.finora.repository.usuario.UsuarioRepository;
import com.finora_app.finora.repository.usuario.UsuarioNaoEncontradoException;
import com.finora_app.finora.security.JwtService;
import com.finora_app.finora.security.SenhaService;

@Service
public class UsuarioApplicationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioApplicationService.class);

	private final UsuarioRepository usuarioRepository;
	private final SenhaService senhaService;
	private final JwtService jwtService;
	private final Clock clock;

	@Autowired
	public UsuarioApplicationService(UsuarioRepository usuarioRepository, SenhaService senhaService,
			JwtService jwtService) {
		this(usuarioRepository, senhaService, jwtService, Clock.systemUTC());
	}

	UsuarioApplicationService(UsuarioRepository usuarioRepository, SenhaService senhaService, JwtService jwtService,
			Clock clock) {
		this.usuarioRepository = usuarioRepository;
		this.senhaService = senhaService;
		this.jwtService = jwtService;
		this.clock = clock;
	}

	@Transactional
	public CadastroUsuarioResponse cadastrar(CadastroUsuarioRequest request) {
		validarCadastro(request);
		Usuario usuario = Usuario.criar(request.nome(), request.email(), Instant.now(clock));
		String senhaHash = senhaService.gerarHash(request.senha());
		try {
			usuarioRepository.salvar(usuario, senhaHash);
		} catch (EmailJaCadastradoException exception) {
			LOGGER.warn("event=user_registration_rejected reason=duplicate_email");
			throw exception;
		}
		LOGGER.info("event=user_registered userId={}", usuario.id());
		return CadastroUsuarioResponse.de(usuario);
	}

	@Transactional(readOnly = true)
	public AutenticacaoResponse autenticar(AutenticacaoRequest request) {
		validarAutenticacao(request);
		UsuarioPersistido persistido = usuarioRepository.buscarPorEmail(new Email(request.email()))
				.orElseThrow(CredenciaisInvalidasException::new);
		if (!persistido.usuario().estaAtivo() || !senhaService.comparar(request.senha(), persistido.senhaHash())) {
			LOGGER.warn("event=user_authentication_rejected reason=invalid_credentials");
			throw new CredenciaisInvalidasException();
		}
		String token = jwtService.emitir(persistido.usuario().id());
		LOGGER.info("event=user_authenticated userId={}", persistido.usuario().id());
		return new AutenticacaoResponse(token);
	}

	@Transactional(readOnly = true)
	public PerfilUsuarioResponse consultarPerfil(UUID usuarioId) {
		Usuario usuario = buscarUsuarioAtivo(usuarioId);
		return PerfilUsuarioResponse.de(usuario);
	}

	@Transactional
	public PerfilUsuarioResponse atualizarPerfil(UUID usuarioId, AtualizarPerfilRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Dados do perfil sao obrigatorios");
		}
		Usuario usuario = buscarUsuarioAtivo(usuarioId);
		usuario.atualizarDadosCadastrais(request.nome(), request.moeda(), request.fusoHorario(), Instant.now(clock));
		usuarioRepository.atualizar(usuario);
		LOGGER.info("event=user_profile_updated userId={}", usuario.id());
		return PerfilUsuarioResponse.de(usuario);
	}

	private Usuario buscarUsuarioAtivo(UUID usuarioId) {
		if (usuarioId == null) {
			throw new IllegalArgumentException("Identidade autenticada e obrigatoria");
		}
		Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
				.map(UsuarioPersistido::usuario)
				.orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario nao encontrado"));
		if (!usuario.estaAtivo()) {
			throw new UsuarioNaoEncontradoException("Usuario ativo nao encontrado");
		}
		return usuario;
	}

	private static void validarCadastro(CadastroUsuarioRequest request) {
		if (request == null || vazio(request.nome()) || vazio(request.email()) || vazio(request.senha())) {
			throw new IllegalArgumentException("Nome, e-mail e senha sao obrigatorios");
		}
	}

	private static void validarAutenticacao(AutenticacaoRequest request) {
		if (request == null || vazio(request.email()) || vazio(request.senha())) {
			throw new IllegalArgumentException("E-mail e senha sao obrigatorios");
		}
	}

	private static boolean vazio(String valor) {
		return valor == null || valor.isBlank();
	}
}
