-- Schema for Planify - generated from entity classes
CREATE DATABASE IF NOT EXISTS proyectointegrado_appamigos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE proyectointegrado_appamigos;

-- Table grupos
CREATE TABLE grupos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo_invitacion VARCHAR(255),
    nombre VARCHAR(255),
    imagen_perfil LONGTEXT,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Table usuarios
CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    nombre VARCHAR(255),
    password VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Table usuarios_grupos
CREATE TABLE usuarios_grupos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT,
    grupo_id BIGINT,
    rol VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_usuario_grupo_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_usuario_grupo_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(id)
) ENGINE=InnoDB;

-- Table invitaciones
CREATE TABLE invitaciones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    grupo_id BIGINT,
    usuario_id BIGINT,
    estado VARCHAR(255),
    fecha DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_invitacion_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(id),
    CONSTRAINT fk_invitacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- Table eventos
CREATE TABLE eventos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    descripcion LONGTEXT,
    fecha DATETIME,
    titulo VARCHAR(255),
    ubicacion VARCHAR(255),
    grupo_id BIGINT NOT NULL,
    creador_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_evento_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(id),
    CONSTRAINT fk_evento_creador FOREIGN KEY (creador_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- Table evento_asistentes
CREATE TABLE evento_asistentes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    evento_id BIGINT,
    usuario_id BIGINT,
    asistio BIT,
    PRIMARY KEY (id),
    CONSTRAINT fk_evento_asistente_evento FOREIGN KEY (evento_id) REFERENCES eventos(id),
    CONSTRAINT fk_evento_asistente_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- Table asistencias_eventos
CREATE TABLE asistencias_eventos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    evento_id BIGINT NOT NULL,
    asistio BIT,
    PRIMARY KEY (id),
    CONSTRAINT fk_asistencia_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_asistencia_evento FOREIGN KEY (evento_id) REFERENCES eventos(id)
) ENGINE=InnoDB;

-- Table gastos
CREATE TABLE gastos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    monto DECIMAL(38,2),
    titulo VARCHAR(255),
    partes_iguales BIT,
    fecha_creacion DATETIME,
    pagado_por BIGINT,
    grupo_id BIGINT,
    evento_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_gasto_pagado_por FOREIGN KEY (pagado_por) REFERENCES usuarios(id),
    CONSTRAINT fk_gasto_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(id),
    CONSTRAINT fk_gasto_evento FOREIGN KEY (evento_id) REFERENCES eventos(id)
) ENGINE=InnoDB;

-- Table gastos_usuarios
CREATE TABLE gastos_usuarios (
    gasto_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT fk_gasto_usuario_gasto FOREIGN KEY (gasto_id) REFERENCES gastos(id),
    CONSTRAINT fk_gasto_usuario_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- Table cantidades_personalizadas
CREATE TABLE cantidades_personalizadas (
    gasto_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    monto DECIMAL(38,2),
    CONSTRAINT fk_cant_personalizada_gasto FOREIGN KEY (gasto_id) REFERENCES gastos(id),
    CONSTRAINT fk_cant_personalizada_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- Table deudas_gastos
CREATE TABLE deudas_gastos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    gasto_id BIGINT NOT NULL,
    deudor_id BIGINT NOT NULL,
    acreedor_id BIGINT NOT NULL,
    monto DECIMAL(38,2),
    saldado BIT,
    fecha_saldado DATETIME,
    metodo_pago VARCHAR(255),
    notas VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_deuda_gasto FOREIGN KEY (gasto_id) REFERENCES gastos(id),
    CONSTRAINT fk_deuda_deudor FOREIGN KEY (deudor_id) REFERENCES usuarios(id),
    CONSTRAINT fk_deuda_acreedor FOREIGN KEY (acreedor_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- Table imagenes
CREATE TABLE imagenes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255),
    tipo_contenido VARCHAR(100),
    tamaño BIGINT,
    datos LONGTEXT,
    fecha_creacion DATETIME,
    evento_id BIGINT,
    usuario_id BIGINT,
    grupo_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_imagen_evento FOREIGN KEY (evento_id) REFERENCES eventos(id),
    CONSTRAINT fk_imagen_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_imagen_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(id)
) ENGINE=InnoDB;

-- Table notas
CREATE TABLE notas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(255),
    fecha_creacion DATETIME,
    contenido LONGTEXT,
    grupo_id BIGINT,
    creada_por BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_nota_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(id),
    CONSTRAINT fk_nota_usuario FOREIGN KEY (creada_por) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- Table votaciones
CREATE TABLE votaciones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    grupo_id BIGINT NOT NULL,
    creador_id BIGINT NOT NULL,
    fecha_creacion DATETIME,
    fecha_cierre DATETIME,
    estado VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_votacion_grupo FOREIGN KEY (grupo_id) REFERENCES grupos(id),
    CONSTRAINT fk_votacion_creador FOREIGN KEY (creador_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;

-- Table votacion_opciones
CREATE TABLE votacion_opciones (
    votacion_id BIGINT NOT NULL,
    opcion VARCHAR(255),
    CONSTRAINT fk_votacion_opcion FOREIGN KEY (votacion_id) REFERENCES votaciones(id)
) ENGINE=InnoDB;

-- Table votos
CREATE TABLE votos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    votacion_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    opcion VARCHAR(255) NOT NULL,
    fecha_voto DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_voto_votacion FOREIGN KEY (votacion_id) REFERENCES votaciones(id),
    CONSTRAINT fk_voto_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
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