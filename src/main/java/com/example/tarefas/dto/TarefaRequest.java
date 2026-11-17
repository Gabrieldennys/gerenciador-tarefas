package com.example.tarefas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TarefaRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 100, message = "O título deve ter no máximo 100 caracteres") String titulo,

        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres") String descricao,

        @FutureOrPresent(message = "A data limite não pode estar no passado") LocalDate dataLimite,

        @NotNull(message = "O usuário é obrigatório") Long usuarioId,

        Long categoriaId) {
}
