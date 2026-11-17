package com.example.tarefas.dto;

import com.example.tarefas.model.StatusTarefa;
import com.example.tarefas.model.Tarefa;

import java.time.LocalDate;

public record TarefaResponse(
        Long id,
        String titulo,
        String descricao,
        StatusTarefa status,
        LocalDate dataLimite,
        Long usuarioId,
        String categoria) {

    public static TarefaResponse from(Tarefa t) {
        return new TarefaResponse(
                t.getId(),
                t.getTitulo(),
                t.getDescricao(),
                t.getStatus(),
                t.getDataLimite(),
                t.getUsuario().getId(),
                t.getCategoria() != null ? t.getCategoria().getNome() : null);
    }
}
