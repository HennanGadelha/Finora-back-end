package com.finora_app.finora.repository.usuario;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.finora_app.finora.domain.usuario.Email;
import com.finora_app.finora.domain.usuario.Usuario;

public interface UsuarioRepository {

	void salvar(Usuario usuario, String senhaHash);

	Optional<UsuarioPersistido> buscarPorEmail(Email email);

	Optional<UsuarioPersistido> buscarPorId(UUID id);

	void atualizar(Usuario usuario);

	void inativar(Usuario usuario, Instant momentoInativacao);
}