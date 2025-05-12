package com.ejemplos.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ejemplos.DTO.Categoria;
import com.ejemplos.DTO.Producto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {
	@Value("${rutaapi}")
	String basePath;
	
	@Autowired
	private RestTemplate restTemplate;
	
	public List<Categoria>obtenerCategorias(){
		Categoria [] response = restTemplate.getForObject(basePath+"/categoria", Categoria[].class);
		return Arrays.asList(response);
	}
	
	public List<Producto> obtenerProductos(Long id) {
		Producto [] response = restTemplate.getForObject(basePath+"/categoria/"+id, Producto[].class);
		return Arrays.asList(response);
	}
}
