package com.ejemplos.controller;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ejemplos.DTO.Categoria;
import com.ejemplos.DTO.Producto;
import com.ejemplos.service.CategoriaService;

@Controller
public class CategoriaController {
	
	@Autowired
	private CategoriaService categoriaService;
	
	//Mostrar listado categorias
	@GetMapping("/categoria")
	public String listar(Model model) {
		model.addAttribute("titulo","Listado de categorias");
		try {
			model.addAttribute("categorias", categoriaService.obtenerCategorias());
		}catch(Exception e) {
			model.addAttribute("categorias", new ArrayList<Categoria>());
		}
		return "mostrar";
	}
	
	@GetMapping("/categoria/{id}")
	public String listarProductos(@PathVariable(value="id") Long id, Model model) {
		model.addAttribute("titulo","Listado de productos");
		try {
			model.addAttribute("productos", categoriaService.obtenerProductos(id));
		}catch(Exception e) {
			model.addAttribute("productos", new ArrayList<Producto>());
		}
		return "mostrarProductos";
	}	
}

