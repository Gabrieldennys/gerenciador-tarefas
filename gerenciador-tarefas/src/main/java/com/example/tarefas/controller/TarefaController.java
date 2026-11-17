package com.example.tarefas.controller;

import com.example.tarefas.dto.TarefaRequest;
import com.example.tarefas.dto.TarefaResponse;
import com.example.tarefas.model.StatusTarefa;
import com.example.tarefas.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tarefas")
public class TarefaController {

    private final TarefaService service;

    public TarefaController(TarefaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaResponse criar(@RequestBody @Valid TarefaRequest req) {
        return service.criar(req);
    }

    // Ex.: GET /tarefas?status=PENDENTE&page=0&size=5
    @GetMapping
    public Page<TarefaResponse> listar(@RequestParam(required = false) StatusTarefa status,
                                       @RequestParam(required = false) Long usuarioId,
                                       Pageable pageable) {
        return service.listar(status, usuarioId, pageable);
    }

    @GetMapping("/{id}")
    public TarefaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public TarefaResponse atualizar(@PathVariable Long id, @RequestBody @Valid TarefaRequest req) {
        return service.atualizar(id, req);
    }

    // Ex.: PATCH /tarefas/1/status?novo=CONCLUIDA
    @PatchMapping("/{id}/status")
    public TarefaResponse alterarStatus(@PathVariable Long id, @RequestParam("novo") StatusTarefa novo) {
        return service.alterarStatus(id, novo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
