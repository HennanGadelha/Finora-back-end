package com.finora_app.finora.repository.usuario;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.finora_app.finora.domain.usuario.Email;
import com.finora_app.finora.domain.usuario.StatusUsuario;
import com.finora_app.finora.domain.usuario.Usuario;

@Repository
public class JdbcUsuarioRepository implements UsuarioRepository {

	private static final String COLUNAS = "id, name, email, password_hash, birth_date, status, "
			+ "created_at, updated_at, deactivated_at";

	private static final String BUSCAR_POR_EMAIL = "SELECT " + COLUNAS + " FROM users WHERE email = ?";
	private static final String BUSCAR_POR_ID = "SELECT " + COLUNAS + " FROM users WHERE id = ?";

	private final JdbcTemplate jdbcTemplate;

	public JdbcUsuarioRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void salvar(Usuario usuario, String senhaHash) {
		validarEntrada(usuario, senhaHash);
		String sql = "INSERT INTO users (id, name, email, password_hash, birth_date, status, created_at, updated_at) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			jdbcTemplate.update(sql, usuario.id(), usuario.nome(), usuario.email().valor(), senhaHash,
					usuario.dataNascimento(), "ACTIVE", Timestamp.from(usuario.createdAt()),
					Timestamp.from(usuario.updatedAt()));
		} catch (DataIntegrityViolationException exception) {
			if (exception.getMessage() != null && exception.getMessage().contains("users_email_unique")) {
				throw new EmailJaCadastradoException("E-mail ja cadastrado", exception);
			}
			throw exception;
		}
	}

	@Override
	public Optional<UsuarioPersistido> buscarPorEmail(Email email) {
		if (email == null) {
			throw new IllegalArgumentException("E-mail e obrigatorio");
		}
		return buscarUm(BUSCAR_POR_EMAIL, email.valor());
	}

	@Override
	public Optional<UsuarioPersistido> buscarPorId(UUID id) {
		if (id == null) {
			throw new IllegalArgumentException("Identidade e obrigatoria");
		}
		return buscarUm(BUSCAR_POR_ID, id);
	}

	@Override
	public void atualizar(Usuario usuario) {
		if (usuario == null) {
			throw new IllegalArgumentException("Usuario e obrigatorio");
		}
		String sql = "UPDATE users SET name = ?, birth_date = ? WHERE id = ?";
		int alterados = jdbcTemplate.update(sql, usuario.nome(), usuario.dataNascimento(), usuario.id());
		if (alterados == 0) {
			throw new UsuarioNaoEncontradoException("Usuario nao encontrado");
		}
	}

	@Override
	public void inativar(Usuario usuario, Instant momentoInativacao) {
		if (usuario == null || momentoInativacao == null) {
			throw new IllegalArgumentException("Usuario e momento de inativacao sao obrigatorios");
		}
		String sql = "UPDATE users SET status = ?, deactivated_at = ? WHERE id = ? AND status = ?";
		int alterados = jdbcTemplate.update(sql, "INACTIVE", Timestamp.from(momentoInativacao), usuario.id(), "ACTIVE");
		if (alterados == 0) {
			throw new UsuarioNaoEncontradoException("Usuario ativo nao encontrado");
		}
	}

	private Optional<UsuarioPersistido> buscarUm(String sql, Object parametro) {
		return jdbcTemplate.query(sql, new UsuarioRowMapper(), parametro).stream().findFirst();
	}

	private static void validarEntrada(Usuario usuario, String senhaHash) {
		if (usuario == null || senhaHash == null || senhaHash.isBlank()) {
			throw new IllegalArgumentException("Usuario e hash da senha sao obrigatorios");
		}
	}

	private static StatusUsuario statusDoBanco(String status) {
		return switch (status) {
		case "ACTIVE" -> StatusUsuario.ATIVO;
		case "INACTIVE" -> StatusUsuario.INATIVO;
		default -> throw new IllegalArgumentException("Status persistido invalido");
		};
	}

	private static class UsuarioRowMapper implements RowMapper<UsuarioPersistido> {

		@Override
		public UsuarioPersistido mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			Usuario usuario = Usuario.reidratar(
					resultSet.getObject("id", UUID.class),
					resultSet.getString("name"),
					new Email(resultSet.getString("email")),
					resultSet.getObject("birth_date", LocalDate.class),
					statusDoBanco(resultSet.getString("status")),
					resultSet.getTimestamp("created_at").toInstant(),
					resultSet.getTimestamp("updated_at").toInstant(),
					resultSet.getTimestamp("deactivated_at") == null
							? null
							: resultSet.getTimestamp("deactivated_at").toInstant());
			return new UsuarioPersistido(usuario, resultSet.getString("password_hash"));
		}
	}
}