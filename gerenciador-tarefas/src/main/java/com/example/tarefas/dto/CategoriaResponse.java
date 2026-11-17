package com.example.tarefas.dto;

import com.example.tarefas.model.Categoria;

public record CategoriaResponse(Long id, String nome) {
    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome());
    }
}
