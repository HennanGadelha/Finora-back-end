package com.finora_app.finora.domain.usuario;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class Usuario {

	private final UUID id;
	private final Email email;
	private final Instant createdAt;
	private String nome;
	private LocalDate dataNascimento;
	private StatusUsuario status;
	private Instant updatedAt;
	private Instant deactivatedAt;

	private Usuario(UUID id, String nome, Email email, LocalDate dataNascimento, Instant momentoCriacao) {
		this.id = id;
		this.nome = validarNome(nome);
		this.email = email;
		this.dataNascimento = validarDataNascimento(dataNascimento);
		this.createdAt = validarInstante(momentoCriacao, "momento de criacao");
		this.updatedAt = this.createdAt;
		this.status = StatusUsuario.ATIVO;
	}

	public static Usuario criar(String nome, String email, LocalDate dataNascimento, Instant momentoCriacao) {
		return criar(nome, new Email(email), dataNascimento, momentoCriacao);
	}

	public static Usuario criar(String nome, Email email, LocalDate dataNascimento, Instant momentoCriacao) {
		if (email == null) {
			throw new ErroDeDominioException("E-mail e obrigatorio");
		}
		return new Usuario(UUID.randomUUID(), nome, email, dataNascimento, momentoCriacao);
	}

	public static Usuario reidratar(UUID id, String nome, Email email, LocalDate dataNascimento, StatusUsuario status,
			Instant createdAt, Instant updatedAt, Instant deactivatedAt) {
		if (email == null || status == null) {
			throw new ErroDeDominioException("Dados persistidos do usuario invalidos");
		}

		Usuario usuario = new Usuario(validarId(id), nome, email, dataNascimento, createdAt);
		usuario.status = status;
		usuario.updatedAt = validarInstante(updatedAt, "momento de atualizacao");
		usuario.deactivatedAt = deactivatedAt;
		if ((status == StatusUsuario.ATIVO && deactivatedAt != null)
				|| (status == StatusUsuario.INATIVO && deactivatedAt == null)) {
			throw new ErroDeDominioException("Estado persistido do usuario inconsistente");
		}
		return usuario;
	}

	public void atualizarDadosCadastrais(String nome, LocalDate dataNascimento, Instant momentoAtualizacao) {
		String nomeValidado = validarNome(nome);
		LocalDate dataNascimentoValidada = validarDataNascimento(dataNascimento);
		Instant instanteValidado = validarInstante(momentoAtualizacao, "momento de atualizacao");

		this.nome = nomeValidado;
		this.dataNascimento = dataNascimentoValidada;
		this.updatedAt = instanteValidado;
	}

	public void inativar(Instant momentoInativacao) {
		if (status == StatusUsuario.INATIVO) {
			throw new ErroDeDominioException("Usuario ja esta inativo");
		}

		Instant instante = validarInstante(momentoInativacao, "momento de inativacao");
		status = StatusUsuario.INATIVO;
		deactivatedAt = instante;
		updatedAt = instante;
	}

	public UUID id() {
		return id;
	}

	public String nome() {
		return nome;
	}

	public Email email() {
		return email;
	}

	public LocalDate dataNascimento() {
		return dataNascimento;
	}

	public StatusUsuario status() {
		return status;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}

	public Instant deactivatedAt() {
		return deactivatedAt;
	}

	public boolean estaAtivo() {
		return status == StatusUsuario.ATIVO;
	}

	private static UUID validarId(UUID id) {
		if (id == null) {
			throw new ErroDeDominioException("Identidade do usuario e obrigatoria");
		}
		return id;
	}

	private static String validarNome(String nome) {
		if (nome == null || nome.isBlank() || nome.trim().length() > 120) {
			throw new ErroDeDominioException("Nome do usuario invalido");
		}
		return nome.trim();
	}

	private static LocalDate validarDataNascimento(LocalDate dataNascimento) {
		if (dataNascimento == null) {
			throw new ErroDeDominioException("Data de nascimento e obrigatoria");
		}
		return dataNascimento;
	}

	private static Instant validarInstante(Instant instante, String descricao) {
		if (instante == null) {
			throw new ErroDeDominioException("O " + descricao + " e obrigatorio");
		}
		return instante;
	}
}