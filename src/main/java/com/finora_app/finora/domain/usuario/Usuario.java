package com.finora_app.finora.domain.usuario;

import java.time.Instant;
import java.util.UUID;

public final class Usuario {

	public static final String MOEDA_PADRAO = "BRL";
	public static final String FUSO_HORARIO_PADRAO = "America/Sao_Paulo";

	private final UUID id;
	private final Email email;
	private final Instant createdAt;
	private String nome;
	private String moeda;
	private String fusoHorario;
	private StatusUsuario status;
	private Instant updatedAt;
	private Instant deactivatedAt;

	private Usuario(UUID id, String nome, Email email, String moeda, String fusoHorario, Instant momentoCriacao) {
		this.id = id;
		this.nome = validarNome(nome);
		this.email = email;
		this.moeda = validarMoeda(moeda);
		this.fusoHorario = validarFusoHorario(fusoHorario);
		this.createdAt = validarInstante(momentoCriacao, "momento de criacao");
		this.updatedAt = this.createdAt;
		this.status = StatusUsuario.ATIVO;
	}

	public static Usuario criar(String nome, String email, Instant momentoCriacao) {
		return criar(nome, new Email(email), momentoCriacao);
	}

	public static Usuario criar(String nome, Email email, Instant momentoCriacao) {
		if (email == null) {
			throw new ErroDeDominioException("E-mail e obrigatorio");
		}
		return new Usuario(UUID.randomUUID(), nome, email, MOEDA_PADRAO, FUSO_HORARIO_PADRAO, momentoCriacao);
	}

	public void atualizarDadosCadastrais(String nome, String moeda, String fusoHorario, Instant momentoAtualizacao) {
		String nomeValidado = validarNome(nome);
		String moedaValidada = validarMoeda(moeda);
		String fusoHorarioValidado = validarFusoHorario(fusoHorario);
		Instant instanteValidado = validarInstante(momentoAtualizacao, "momento de atualizacao");

		this.nome = nomeValidado;
		this.moeda = moedaValidada;
		this.fusoHorario = fusoHorarioValidado;
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

	public String moeda() {
		return moeda;
	}

	public String fusoHorario() {
		return fusoHorario;
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

	private static String validarNome(String nome) {
		if (nome == null || nome.isBlank() || nome.trim().length() > 120) {
			throw new ErroDeDominioException("Nome do usuario invalido");
		}
		return nome.trim();
	}

	private static String validarMoeda(String moeda) {
		if (!MOEDA_PADRAO.equals(moeda)) {
			throw new ErroDeDominioException("Moeda deve ser BRL");
		}
		return moeda;
	}

	private static String validarFusoHorario(String fusoHorario) {
		if (!FUSO_HORARIO_PADRAO.equals(fusoHorario)) {
			throw new ErroDeDominioException("Fuso horario deve ser America/Sao_Paulo");
		}
		return fusoHorario;
	}

	private static Instant validarInstante(Instant instante, String descricao) {
		if (instante == null) {
			throw new ErroDeDominioException("O " + descricao + " e obrigatorio");
		}
		return instante;
	}
}