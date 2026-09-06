package com.finora_app.finora.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.finora_app.finora.application.usuario.UsuarioApplicationService;
import com.finora_app.finora.repository.usuario.UsuarioRepository;

@Configuration
public class UsuarioMockadoConfiguration {

	@Bean
	@ConditionalOnProperty(prefix = "finora.test-user", name = "enabled", havingValue = "true")
	UsuarioMockadoInitializer usuarioMockadoInitializer(UsuarioApplicationService usuarioService,
			UsuarioRepository usuarioRepository, @Value("${finora.test-user.name}") String nome,
			@Value("${finora.test-user.email}") String email, @Value("${finora.test-user.password}") String senha) {
		return new UsuarioMockadoInitializer(usuarioService, usuarioRepository, nome, email, senha);
	}
}