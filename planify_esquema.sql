-- Script de creación de base de datos para Planify
-- Generado automáticamente el 2025-05-19 17:53:34

CREATE DATABASE IF NOT EXISTS proyectointegrado_appamigos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE proyectointegrado_appamigos;

-- Tabla grupos
CREATE TABLE grupos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo_invitacion VARCHAR(255),
    nombre VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Tabla usuarios
CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) unique,
    nombre VARCHAR(255),
    password VARCHAR(255),
    PRIMARY KEY (id),
) ENGINE=InnoDB;

-- Tabla eventos
CREATE TABLE eventos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    descripcion LONGTEXT,
    fecha DATETIME(6),
    titulo VARCHAR(255),
    ubicacion VARCHAR(255),
    grupo_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT FK_evento_grupo FOREIGN KEY (grupo_id) REFERENCES grupos (id)
) ENGINE=InnoDB;

-- Tabla gastos
CREATE TABLE gastos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    monto DECIMAL(38, 2),
    titulo VARCHAR(255),
    pagado_por BIGINT,
    grupo_id BIGINT,
    evento_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT FK_gasto_pagado_por FOREIGN KEY (pagado_por) REFERENCES usuarios (id),
    CONSTRAINT FK_gasto_grupo FOREIGN KEY (grupo_id) REFERENCES grupos (id),
    CONSTRAINT FK_gasto_evento FOREIGN KEY (evento_id) REFERENCES eventos (id)
) ENGINE=InnoDB;

-- Tabla gastos_usuarios
CREATE TABLE gastos_usuarios (
    gasto_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT FK_gasto_usuario_gasto FOREIGN KEY (gasto_id) REFERENCES gastos (id),
    CONSTRAINT FK_gasto_usuario_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB;

-- Tabla imagenes
CREATE TABLE imagenes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    url VARCHAR(255),
    evento_id BIGINT,
    usuario_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT FK_imagen_evento FOREIGN KEY (evento_id) REFERENCES eventos (id),
    CONSTRAINT FK_imagen_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB;

-- Tabla notas
CREATE TABLE notas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(255),
    contenido LONGTEXT,
    grupo_id BIGINT,
    creada_por BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT FK_nota_grupo FOREIGN KEY (grupo_id) REFERENCES grupos (id),
    CONSTRAINT FK_nota_usuario FOREIGN KEY (creada_por) REFERENCES usuarios (id)
) ENGINE=InnoDB;

-- Tabla votaciones
CREATE TABLE votaciones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pregunta VARCHAR(255),
    opciones LONGTEXT,
    fecha_creacion DATETIME(6),
    grupo_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT FK_votacion_grupo FOREIGN KEY (grupo_id) REFERENCES grupos (id)
) ENGINE=InnoDB;

-- Tabla votos
CREATE TABLE votos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    opcion_seleccionada INT,
    votacion_id BIGINT,
    usuario_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT FK_voto_votacion FOREIGN KEY (votacion_id) REFERENCES votaciones (id),
    CONSTRAINT FK_voto_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB;
-- Tabla asistencias a eventos
CREATE TABLE asistencias_eventos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    evento_id BIGINT NOT NULL,
    asistio BIT,
    PRIMARY KEY (id),
    CONSTRAINT FK_asistencia_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT FK_asistencia_evento FOREIGN KEY (evento_id) REFERENCES eventos(id)
) ENGINE=InnoDB;