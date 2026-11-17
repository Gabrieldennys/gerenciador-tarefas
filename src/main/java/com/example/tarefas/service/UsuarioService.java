package com.example.tarefas.service;

import com.example.tarefas.dto.UsuarioRequest;
import com.example.tarefas.dto.UsuarioResponse;
import com.example.tarefas.exception.RecursoNaoEncontradoException;
import com.example.tarefas.exception.RegraNegocioException;
import com.example.tarefas.model.Usuario;
import com.example.tarefas.repository.TarefaRepository;
import com.example.tarefas.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TarefaRepository tarefaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, TarefaRepository tarefaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tarefaRepository = tarefaRepository;
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new RegraNegocioException("Já existe um usuário com este e-mail");
        }
        Usuario u = new Usuario();
        u.setNome(req.nome());
        u.setEmail(req.email());
        return UsuarioResponse.from(usuarioRepository.save(u));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.from(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest req) {
        Usuario u = buscarEntidade(id);
        if (usuarioRepository.existsByEmailAndIdNot(req.email(), id)) {
            throw new RegraNegocioException("Já existe um usuário com este e-mail");
        }
        u.setNome(req.nome());
        u.setEmail(req.email());
        return UsuarioResponse.from(usuarioRepository.save(u));
    }

    @Transactional
    public void deletar(Long id) {
        Usuario u = buscarEntidade(id);
        if (tarefaRepository.existsByUsuarioId(id)) {
            throw new RegraNegocioException("Não é possível excluir um usuário que possui tarefas");
        }
        usuarioRepository.delete(u);
    }

    public Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }
}
