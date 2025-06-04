package com.ejemplos.service;

import com.ejemplos.modelo.Imagen;
import com.ejemplos.modelo.ImagenRepository;
import com.ejemplos.DTO.Imagen.ImagenCreateDTO;
import com.ejemplos.DTO.Imagen.ImagenDTO;
import com.ejemplos.DTO.Imagen.ImagenDTOConverter;
import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.Grupo;
import com.ejemplos.modelo.GrupoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImagenService {
    
    @Autowired
    private ImagenRepository imagenRepository;
    
    @Autowired
    private ImagenDTOConverter converter;
    
    @Autowired 
    private GrupoRepository grupoRepository;
    
    public ImagenDTO subirImagen(MultipartFile archivo, ImagenCreateDTO createDTO) throws IOException {
        // Convertir archivo a Base64
        String base64Data = Base64.getEncoder().encodeToString(archivo.getBytes());
        
        Imagen imagen = new Imagen();
        imagen.setNombre(archivo.getOriginalFilename());
        imagen.setTipoContenido(archivo.getContentType());
        imagen.setDatos(base64Data);
        imagen.setTamaño(archivo.getSize());
        
        // Asignar relaciones si es necesario
        if (createDTO.getGrupoId() != null) {
            Grupo grupo = grupoRepository.findById(createDTO.getGrupoId())
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado"));
            imagen.setGrupo(grupo);
        }
        
        // Similar para evento y usuario...
        
        Imagen imagenGuardada = imagenRepository.save(imagen);
        return converter.convertToDTO(imagenGuardada);
    }
    
    public List<ImagenDTO> obtenerImagenesPorGrupoSinDatos(Long grupoId) {
        List<Imagen> imagenes = imagenRepository.findByGrupoIdOrderByFechaCreacionDesc(grupoId);
        return imagenes.stream()
                .map(img -> new ImagenDTO(
                    img.getId(), img.getNombre(), img.getTipoContenido(), 
                    img.getTamaño(), img.getFechaCreacion(),
                    img.getEvento() != null ? img.getEvento().getId() : null,
                    img.getUsuario() != null ? img.getUsuario().getId() : null,
                    img.getGrupo() != null ? img.getGrupo().getId() : null,
                    img.getUsuario() != null ? img.getUsuario().getNombre() : null
                ))
                .collect(Collectors.toList());
    }
    
    public ImagenDTO obtenerImagenCompletaPorId(Long id) {
        Imagen imagen = imagenRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));
        return converter.convertToDTO(imagen); // Este incluye los datos Base64
    }
    /**
     * Elimina una imagen por su ID
     * @param id ID de la imagen a eliminar
     * @throws RuntimeException si la imagen no existe
     */
    public void eliminarImagen(Long id) {
        // Verificar que la imagen existe antes de eliminar
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
        
        // Eliminar la imagen
        imagenRepository.delete(imagen);
    }

    /**
     * Elimina una imagen por su ID con validación adicional de permisos
     * @param id ID de la imagen a eliminar
     * @param usuarioId ID del usuario que intenta eliminar (opcional para validación)
     * @throws RuntimeException si la imagen no existe
     * @throws IllegalArgumentException si el usuario no tiene permisos
     */
    public void eliminarImagenConValidacion(Long id, Long usuarioId) {
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
        
        // Validación opcional: verificar si el usuario tiene permisos para eliminar
        // (puedes ajustar esta lógica según tus reglas de negocio)
        if (usuarioId != null && imagen.getUsuario() != null && 
            !imagen.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permisos para eliminar esta imagen");
        }
        
        imagenRepository.delete(imagen);
    }

    /**
     * Elimina múltiples imágenes por sus IDs
     * @param ids Lista de IDs de las imágenes a eliminar
     * @return Número de imágenes eliminadas exitosamente
     */
    public int eliminarImagenes(List<Long> ids) {
        int eliminadas = 0;
        
        for (Long id : ids) {
            try {
                eliminarImagen(id);
                eliminadas++;
            } catch (RuntimeException e) {
                // Log del error si es necesario
                // Continúa con las siguientes imágenes
            }
        }
        
        return eliminadas;
    }
}