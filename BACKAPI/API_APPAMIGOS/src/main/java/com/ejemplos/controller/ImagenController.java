package com.ejemplos.controller;

import com.ejemplos.DTO.Imagen.ImagenCreateDTO;
import com.ejemplos.DTO.Imagen.ImagenDTO;
import com.ejemplos.service.ImagenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {
    
    @Autowired
    private ImagenService imagenService;
    
    /**
     * Subir una nueva imagen - se guarda como Base64 en MySQL
     */
    @PostMapping("/subir")
    public ResponseEntity<?> subirImagen(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "eventoId", required = false) Long eventoId,
            @RequestParam(value = "usuarioId", required = false) Long usuarioId,
            @RequestParam(value = "grupoId", required = false) Long grupoId) {
        
        try {
            // Validaciones básicas
            if (archivo.isEmpty()) {
                return ResponseEntity.badRequest().body("El archivo está vacío");
            }
            
            if (archivo.getSize() > 5 * 1024 * 1024) { // 5MB máximo
                return ResponseEntity.badRequest().body("El archivo es demasiado grande (máximo 5MB)");
            }
            
            ImagenCreateDTO createDTO = new ImagenCreateDTO(eventoId, usuarioId, grupoId);
            ImagenDTO imagenDTO = imagenService.subirImagen(archivo, createDTO);
            return ResponseEntity.ok(imagenDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir la imagen: " + e.getMessage());
        }
    }
    
    /**
     * Obtener todas las imágenes de un grupo (sin los datos Base64 para optimizar)
     */
    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<ImagenDTO>> obtenerImagenesPorGrupo(@PathVariable Long grupoId) {
        try {
            List<ImagenDTO> imagenes = imagenService.obtenerImagenesPorGrupoSinDatos(grupoId);
            return ResponseEntity.ok(imagenes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtener imagen completa con datos Base64
     */
    @GetMapping("/{id}")
    public ResponseEntity<ImagenDTO> obtenerImagenCompleta(@PathVariable Long id) {
        try {
            ImagenDTO imagen = imagenService.obtenerImagenCompletaPorId(id);
            return ResponseEntity.ok(imagen);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Ya no necesitas este endpoint porque los datos van en el DTO
     */
    // @GetMapping("/{id}/datos") - ELIMINADO
    
    /**
     * Eliminar una imagen
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarImagen(@PathVariable Long id) {
        try {
            imagenService.eliminarImagen(id);
            return ResponseEntity.ok().body("Imagen eliminada correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Imagen no encontrada");
        }
    }
}