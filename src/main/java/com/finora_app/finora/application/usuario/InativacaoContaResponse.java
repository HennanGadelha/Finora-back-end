package com.finora_app.finora.application.usuario;

import java.time.Instant;

public record InativacaoContaResponse(String status, Instant deactivatedAt) {
}