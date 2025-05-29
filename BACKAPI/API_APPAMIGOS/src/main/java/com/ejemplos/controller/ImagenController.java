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
     * Subir una nueva imagen - se guarda directamente en MySQL
     */
    @PostMapping("/subir")
    public ResponseEntity<?> subirImagen(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "eventoId", required = false) Long eventoId,
            @RequestParam(value = "usuarioId", required = false) Long usuarioId,
            @RequestParam(value = "grupoId", required = false) Long grupoId) {
        
        try {
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
     * Obtener todas las imágenes de un grupo (sin los datos binarios)
     */
    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<ImagenDTO>> obtenerImagenesPorGrupo(@PathVariable Long grupoId) {
        try {
            List<ImagenDTO> imagenes = imagenService.obtenerImagenesPorGrupo(grupoId);
            return ResponseEntity.ok(imagenes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtener información de una imagen específica por ID (sin los datos binarios)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ImagenDTO> obtenerImagenPorId(@PathVariable Long id) {
        try {
            ImagenDTO imagen = imagenService.obtenerImagenPorId(id);
            return ResponseEntity.ok(imagen);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Servir los datos binarios de la imagen desde MySQL
     */
    @GetMapping("/{id}/datos")
    public ResponseEntity<byte[]> obtenerDatosImagen(@PathVariable Long id) {
        try {
            byte[] datos = imagenService.obtenerDatosImagen(id);
            String tipoContenido = imagenService.obtenerTipoContenido(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(tipoContenido));
            headers.setContentLength(datos.length);
            headers.setCacheControl("max-age=3600"); // Cache por 1 hora
            
            return new ResponseEntity<>(datos, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Eliminar una imagen de MySQL
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