package com.example.tarefas.repository;

import com.example.tarefas.model.StatusTarefa;
import com.example.tarefas.model.Tarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    Page<Tarefa> findByStatus(StatusTarefa status, Pageable pageable);
    Page<Tarefa> findByUsuarioId(Long usuarioId, Pageable pageable);
    boolean existsByUsuarioId(Long usuarioId);
    boolean existsByCategoriaId(Long categoriaId);
}
