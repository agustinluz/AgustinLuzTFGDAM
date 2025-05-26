package com.ejemplos.service;

import com.ejemplos.DTO.ImagenDTO;
import com.ejemplos.DTO.ImagenCreateDTO;
import com.ejemplos.DTO.ImagenDTOConverter;
import com.ejemplos.modelo.Imagen;
import com.ejemplos.modelo.ImagenRepository;
import com.ejemplos.modelo.Evento;
import com.ejemplos.modelo.Usuario;
import com.ejemplos.modelo.Grupo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImagenService {
    
    @Autowired
    private ImagenRepository imagenRepository;
    
    @Autowired
    private ImagenDTOConverter imagenDTOConverter;
    
    public ImagenDTO subirImagen(MultipartFile archivo, ImagenCreateDTO createDTO) throws IOException {
        // Validar que el archivo no esté vacío
        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }
        
        // Validar tipo de archivo
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }
        
        // Validar tamaño del archivo (opcional - máximo 5MB)
        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo no puede superar los 5MB");
        }
        
        // Crear entidad Imagen
        Imagen imagen = new Imagen();
        imagen.setNombre(archivo.getOriginalFilename());
        imagen.setTipoContenido(contentType);
        imagen.setDatos(archivo.getBytes());
        
        // Establecer relaciones según corresponda
        if (createDTO.getEventoId() != null) {
            Evento evento = new Evento();
            evento.setId(createDTO.getEventoId());
            imagen.setEvento(evento);
        }
        
        if (createDTO.getUsuarioId() != null) {
            Usuario usuario = new Usuario();
            usuario.setId(createDTO.getUsuarioId());
            imagen.setUsuario(usuario);
        }
        
        if (createDTO.getGrupoId() != null) {
            Grupo grupo = new Grupo();
            grupo.setId(createDTO.getGrupoId());
            imagen.setGrupo(grupo);
        }
        
        // Guardar en base de datos MySQL
        imagen = imagenRepository.save(imagen);
        
        return imagenDTOConverter.convertToDTO(imagen);
    }
    
    public List<ImagenDTO> obtenerImagenesPorGrupo(Long grupoId) {
        List<Imagen> imagenes = imagenRepository.findByGrupoId(grupoId);
        return imagenes.stream()
                .map(imagenDTOConverter::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ImagenDTO obtenerImagenPorId(Long id) {
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
        return imagenDTOConverter.convertToDTO(imagen);
    }
    
    public byte[] obtenerDatosImagen(Long id) {
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
        return imagen.getDatos();
    }
    
    public String obtenerTipoContenido(Long id) {
        Imagen imagen = imagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + id));
        return imagen.getTipoContenido();
    }
    
    public void eliminarImagen(Long id) {
        if (!imagenRepository.existsById(id)) {
            throw new RuntimeException("Imagen no encontrada con ID: " + id);
        }
        imagenRepository.deleteById(id);
    }
}