package com.example.tarefas.service;

import com.example.tarefas.dto.TarefaRequest;
import com.example.tarefas.dto.TarefaResponse;
import com.example.tarefas.exception.RecursoNaoEncontradoException;
import com.example.tarefas.exception.RegraNegocioException;
import com.example.tarefas.model.Categoria;
import com.example.tarefas.model.StatusTarefa;
import com.example.tarefas.model.Tarefa;
import com.example.tarefas.model.Usuario;
import com.example.tarefas.repository.TarefaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;

    public TarefaService(TarefaRepository tarefaRepository,
                         UsuarioService usuarioService,
                         CategoriaService categoriaService) {
        this.tarefaRepository = tarefaRepository;
        this.usuarioService = usuarioService;
        this.categoriaService = categoriaService;
    }

    @Transactional
    public TarefaResponse criar(TarefaRequest req) {
        Tarefa t = new Tarefa();
        preencher(t, req);
        return TarefaResponse.from(tarefaRepository.save(t));
    }

    @Transactional(readOnly = true)
    public Page<TarefaResponse> listar(StatusTarefa status, Long usuarioId, Pageable pageable) {
        Page<Tarefa> pagina;
        if (status != null) {
            pagina = tarefaRepository.findByStatus(status, pageable);
        } else if (usuarioId != null) {
            pagina = tarefaRepository.findByUsuarioId(usuarioId, pageable);
        } else {
            pagina = tarefaRepository.findAll(pageable);
        }
        return pagina.map(TarefaResponse::from);
    }

    @Transactional(readOnly = true)
    public TarefaResponse buscar(Long id) {
        return TarefaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public TarefaResponse atualizar(Long id, TarefaRequest req) {
        Tarefa t = buscarEntidade(id);
        if (t.getStatus() == StatusTarefa.CONCLUIDA) {
            throw new RegraNegocioException("Tarefas concluídas não podem ser editadas");
        }
        preencher(t, req);
        return TarefaResponse.from(tarefaRepository.save(t));
    }

    @Transactional
    public TarefaResponse alterarStatus(Long id, StatusTarefa novoStatus) {
        Tarefa t = buscarEntidade(id);
        if (t.getStatus() == StatusTarefa.CONCLUIDA) {
            throw new RegraNegocioException("Uma tarefa concluída não pode mudar de status");
        }
        t.setStatus(novoStatus);
        return TarefaResponse.from(tarefaRepository.save(t));
    }

    @Transactional
    public void deletar(Long id) {
        tarefaRepository.delete(buscarEntidade(id));
    }

    private void preencher(Tarefa t, TarefaRequest req) {
        Usuario usuario = usuarioService.buscarEntidade(req.usuarioId());
        Categoria categoria = req.categoriaId() != null
                ? categoriaService.buscarEntidade(req.categoriaId())
                : null;
        t.setTitulo(req.titulo());
        t.setDescricao(req.descricao());
        t.setDataLimite(req.dataLimite());
        t.setUsuario(usuario);
        t.setCategoria(categoria);
    }

    private Tarefa buscarEntidade(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa não encontrada: " + id));
    }
}
