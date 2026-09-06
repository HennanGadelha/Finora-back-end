package com.finora_app.finora.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import java.time.LocalDate;
import com.finora_app.finora.application.usuario.CadastroUsuarioRequest;
import com.finora_app.finora.application.usuario.UsuarioApplicationService;
import com.finora_app.finora.domain.usuario.Email;
import com.finora_app.finora.repository.usuario.UsuarioPersistido;
import com.finora_app.finora.repository.usuario.UsuarioRepository;

public final class UsuarioMockadoInitializer implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioMockadoInitializer.class);

	private final UsuarioApplicationService usuarioService;
	private final UsuarioRepository usuarioRepository;
	private final String nome;
	private final String email;
	private final String senha;
	private final LocalDate dataNascimento;

	public UsuarioMockadoInitializer(UsuarioApplicationService usuarioService, UsuarioRepository usuarioRepository,
			String nome, String email, String senha, LocalDate dataNascimento) {
		this.usuarioService = usuarioService;
		this.usuarioRepository = usuarioRepository;
		this.nome = nome;
		this.email = email;
		this.senha = senha;
		this.dataNascimento = dataNascimento;
	}

	@Override
	public void run(String... args) {
		UsuarioPersistido existente = usuarioRepository.buscarPorEmail(new Email(email)).orElse(null);
		if (existente != null) {
			if (!existente.usuario().estaAtivo()) {
				throw new IllegalStateException("Usuario mockado existente esta inativo");
			}
			LOGGER.info("event=test_user_already_available userId={}", existente.usuario().id());
			return;
		}

		usuarioService.cadastrar(new CadastroUsuarioRequest(nome, email, senha, dataNascimento));
		LOGGER.info("event=test_user_created");
	}
}