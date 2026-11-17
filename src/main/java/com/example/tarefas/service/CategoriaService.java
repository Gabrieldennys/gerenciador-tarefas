package com.example.tarefas.service;

import com.example.tarefas.dto.CategoriaRequest;
import com.example.tarefas.dto.CategoriaResponse;
import com.example.tarefas.exception.RecursoNaoEncontradoException;
import com.example.tarefas.exception.RegraNegocioException;
import com.example.tarefas.model.Categoria;
import com.example.tarefas.repository.CategoriaRepository;
import com.example.tarefas.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final TarefaRepository tarefaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, TarefaRepository tarefaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.tarefaRepository = tarefaRepository;
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest req) {
        if (categoriaRepository.existsByNome(req.nome())) {
            throw new RegraNegocioException("Já existe uma categoria com este nome");
        }
        Categoria c = new Categoria();
        c.setNome(req.nome());
        return CategoriaResponse.from(categoriaRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().map(CategoriaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest req) {
        Categoria c = buscarEntidade(id);
        if (categoriaRepository.existsByNomeAndIdNot(req.nome(), id)) {
            throw new RegraNegocioException("Já existe uma categoria com este nome");
        }
        c.setNome(req.nome());
        return CategoriaResponse.from(categoriaRepository.save(c));
    }

    @Transactional
    public void deletar(Long id) {
        Categoria c = buscarEntidade(id);
        if (tarefaRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Não é possível excluir uma categoria que possui tarefas");
        }
        categoriaRepository.delete(c);
    }

    public Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + id));
    }
}
