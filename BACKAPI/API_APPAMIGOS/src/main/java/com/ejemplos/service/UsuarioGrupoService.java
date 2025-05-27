package com.ejemplos.service;

import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.UsuarioGrupo;
import com.ejemplos.modelo.UsuarioGrupoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioGrupoService {

    @Autowired
    private UsuarioGrupoRepository usuarioGrupoRepository;

    public UsuarioGrupo guardar(UsuarioGrupo usuarioGrupo) {
        return usuarioGrupoRepository.save(usuarioGrupo);
    }

    public List<Usuario> obtenerUsuariosPorGrupoId(Long grupoId) {
        return usuarioGrupoRepository.findByGrupoId(grupoId)
                .stream()
                .map(UsuarioGrupo::getUsuario)
                .collect(Collectors.toList());
    }

    public List<UsuarioGrupo> obtenerPorUsuarioId(Long usuarioId) {
        return usuarioGrupoRepository.findByUsuarioId(usuarioId);
    }
    
    public boolean usuarioPerteneceAlGrupo(Long usuarioId, Long grupoId) {
        return usuarioGrupoRepository.existsByUsuarioIdAndGrupoId(usuarioId, grupoId);
    }

}
