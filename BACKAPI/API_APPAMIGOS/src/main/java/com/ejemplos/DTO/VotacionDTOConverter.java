package com.ejemplos.DTO;

import com.ejemplos.modelo.Votacion;
import com.ejemplos.modelo.Voto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
@Component
public class VotacionDTOConverter {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public VotacionDTO convertToDTO(Votacion votacion) {
        if (votacion == null) return null;
        
        VotacionDTO dto = new VotacionDTO();
        dto.setId(votacion.getId());
        dto.setPregunta(votacion.getPregunta());
        dto.setFechaCreacion(votacion.getFechaCreacion());
        dto.setFechaLimite(votacion.getFechaLimite());

        
        // Convertir opciones de JSON string a List
        try {
            List<String> opciones = objectMapper.readValue(
                votacion.getOpciones(), 
                new TypeReference<List<String>>() {}
            );
            dto.setOpciones(opciones);
        } catch (Exception e) {
            dto.setOpciones(new ArrayList<>());
        }
        
        if (votacion.getGrupo() != null) {
            dto.setGrupoId(votacion.getGrupo().getId());
            dto.setGrupoNombre(votacion.getGrupo().getNombre());
        }
        
        // Calcular resumen de votos
        if (votacion.getVotos() != null) {
            dto.setTotalVotos(votacion.getVotos().size());
            dto.setResumenVotos(calcularResumenVotos(votacion.getVotos(), dto.getOpciones()));
        }
        
        return dto;
    }
    
    public Votacion convertToEntity(VotacionCreateDTO dto) {
        if (dto == null) return null;
        
        Votacion votacion = new Votacion();
        votacion.setPregunta(dto.getPregunta());
        votacion.setFechaLimite(dto.getFechaLimite());

        
        // Convertir opciones de List a JSON string
        try {
            String opcionesJson = objectMapper.writeValueAsString(dto.getOpciones());
            votacion.setOpciones(opcionesJson);
        } catch (Exception e) {
            votacion.setOpciones("[]");
        }
        
        return votacion;
    }
    
    private List<VotoResumenDTO> calcularResumenVotos(List<Voto> votos, List<String> opciones) {
        // Contar votos por índice de opción
        Map<Integer, Long> conteoVotos = votos.stream()
            .collect(Collectors.groupingBy(Voto::getOpcionSeleccionada, Collectors.counting()));
        
        int totalVotos = votos.size();
        
        // Crear resumen para cada opción usando el índice
        return IntStream.range(0, opciones.size())
            .mapToObj(index -> {
                VotoResumenDTO resumen = new VotoResumenDTO();
                resumen.setOpcion(opciones.get(index));
                
                // Obtener cantidad de votos para este índice (0 si no existe)
                Long cantidad = conteoVotos.getOrDefault(index, 0L);
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