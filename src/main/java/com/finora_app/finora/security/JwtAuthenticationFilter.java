package com.finora_app.finora.security;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.finora_app.finora.repository.usuario.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UsuarioRepository usuarioRepository;

	public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
		this.jwtService = jwtService;
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			UUID usuarioId = jwtService.validar(authorization.substring("Bearer ".length()).trim());
			boolean usuarioAtivo = usuarioRepository.buscarPorId(usuarioId)
					.map(persistido -> persistido.usuario().estaAtivo())
					.orElse(false);
			if (!usuarioAtivo) {
				responderNaoAutorizado(response);
				return;
			}
			var autenticacao = new UsernamePasswordAuthenticationToken(usuarioId, null, List.of());
			SecurityContextHolder.getContext().setAuthentication(autenticacao);
			filterChain.doFilter(request, response);
		} catch (JwtException | IllegalArgumentException exception) {
			SecurityContextHolder.clearContext();
			responderNaoAutorizado(response);
		}
	}

	private static void responderNaoAutorizado(HttpServletResponse response) throws IOException {
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Autenticacao invalida");
	}
}
