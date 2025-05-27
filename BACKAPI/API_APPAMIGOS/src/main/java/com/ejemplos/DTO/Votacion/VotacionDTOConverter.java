package com.ejemplos.DTO;

import com.ejemplos.modelo.Votacion;
import com.ejemplos.modelo.Voto;
import com.ejemplos.modelo.VotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class VotacionDTOConverter {
    
    @Autowired
    private VotoRepository votoRepository;
    
    public VotacionDTO convertToDTO(Votacion votacion) {
        if (votacion == null) return null;
        
        VotacionDTO dto = new VotacionDTO();
        dto.setId(votacion.getId());
        dto.setPregunta(votacion.getTitulo()); // Mapeo de titulo a pregunta
        dto.setOpciones(votacion.getOpciones() != null ? new ArrayList<>(votacion.getOpciones()) : new ArrayList<>());
        dto.setFechaCreacion(votacion.getFechaCreacion());
        dto.setFechaLimite(votacion.getFechaCierre()); // Mapeo de fechaCierre a fechaLimite
        
        // Información del grupo
        if (votacion.getGrupo() != null) {
            dto.setGrupoId(votacion.getGrupo().getId());
            dto.setGrupoNombre(votacion.getGrupo().getNombre());
        }
        
        // Información del creador
        if (votacion.getCreador() != null) {
            dto.setCreadaPorId(votacion.getCreador().getId());
            dto.setCreadaPorNombre(votacion.getCreador().getNombre());
        }
        
        // Calcular resumen de votos
        List<Voto> votos = votoRepository.findByVotacionId(votacion.getId());
        dto.setTotalVotos(votos.size());
        dto.setResumenVotos(calcularResumenVotos(votos, dto.getOpciones()));
        
        return dto;
    }
    
    public Votacion convertToEntity(VotacionCreateDTO dto) {
        if (dto == null) return null;
        
        Votacion votacion = new Votacion();
        votacion.setTitulo(dto.getPregunta()); // Mapeo de pregunta a titulo
        votacion.setOpciones(dto.getOpciones() != null ? new ArrayList<>(dto.getOpciones()) : new ArrayList<>());
        votacion.setFechaCierre(dto.getFechaLimite()); // Mapeo de fechaLimite a fechaCierre
        
        return votacion;
    }
    
    public void updateEntityFromDTO(Votacion votacion, VotacionUpdateDTO dto) {
        if (votacion == null || dto == null) return;
        
        if (dto.getTitulo() != null) {
            votacion.setTitulo(dto.getTitulo());
        }
        
        if (dto.getDescripcion() != null) {
            votacion.setDescripcion(dto.getDescripcion());
        }
        
        if (dto.getOpciones() != null) {
            votacion.setOpciones(new ArrayList<>(dto.getOpciones()));
        }
    }
    
    private List<VotoResumenDTO> calcularResumenVotos(List<Voto> votos, List<String> opciones) {
        if (opciones == null || opciones.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Contar votos por opción (usando String directamente, no índice)
        Map<String, Long> conteoVotos = votos.stream()
            .collect(Collectors.groupingBy(Voto::getOpcion, Collectors.counting()));
        
        int totalVotos = votos.size();
        
        // Crear resumen para cada opción
        return opciones.stream()
            .map(opcion -> {
                VotoResumenDTO resumen = new VotoResumenDTO();
                resumen.setOpcion(opcion);
                
                // Obtener cantidad de votos para esta opción (0 si no existe)
                Long cantidad = conteoVotos.getOrDefault(opcion, 0L);
                resumen.setCantidad(cantidad.intValue());
                
                // Calcular porcentaje
                if (totalVotos > 0) {
                    double porcentaje = (cantidad.doubleValue() * 100.0) / totalVotos;
                    resumen.setPorcentaje(Math.round(porcentaje * 100.0) / 100.0);
                } else {
                    resumen.setPorcentaje(0.0);
                }
                
                return resumen;
            })
            .collect(Collectors.toList());
    }
}