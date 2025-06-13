-- Schema para proyectointegrado_appamigos
CREATE DATABASE IF NOT EXISTS proyectointegrado_appamigos
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
USE proyectointegrado_appamigos;

-- -----------------------------------------------------
-- Tabla grupos
-- -----------------------------------------------------
CREATE TABLE grupos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  codigo_invitacion VARCHAR(255),
  nombre VARCHAR(255),
  imagen_perfil LONGTEXT,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla usuarios
-- -----------------------------------------------------
CREATE TABLE usuarios (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL UNIQUE,
  nombre VARCHAR(255),
  foto_perfil LONGTEXT,
  password VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla usuarios_grupos (UsuarioGrupo)
-- -----------------------------------------------------
CREATE TABLE usuarios_grupos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  grupo_id BIGINT NOT NULL,
  rol VARCHAR(255),
  PRIMARY KEY (id),
  INDEX (usuario_id),
  INDEX (grupo_id),
  CONSTRAINT fk_ug_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_ug_grupo FOREIGN KEY (grupo_id)
    REFERENCES grupos(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla invitaciones
-- -----------------------------------------------------
CREATE TABLE invitaciones (
  id BIGINT NOT NULL AUTO_INCREMENT,
  grupo_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  estado VARCHAR(20) NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX (grupo_id),
  INDEX (usuario_id),
  CONSTRAINT fk_inv_grupo FOREIGN KEY (grupo_id)
    REFERENCES grupos(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_inv_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla eventos
-- -----------------------------------------------------
CREATE TABLE eventos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  descripcion LONGTEXT,
  fecha DATETIME,
  titulo VARCHAR(255),
  ubicacion VARCHAR(255),
  grupo_id BIGINT NOT NULL,
  creador_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  INDEX (grupo_id),
  INDEX (creador_id),
  CONSTRAINT fk_evt_grupo FOREIGN KEY (grupo_id)
    REFERENCES grupos(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_evt_creador FOREIGN KEY (creador_id)
    REFERENCES usuarios(id)
    ON DELETE SET NULL
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla evento_asistentes (EventoAsistente)
-- -----------------------------------------------------
CREATE TABLE evento_asistentes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  evento_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  asistio BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id),
  INDEX (evento_id),
  INDEX (usuario_id),
  CONSTRAINT fk_ea_evento FOREIGN KEY (evento_id)
    REFERENCES eventos(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_ea_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla asistencias_eventos (AsistenciaEvento)
-- -----------------------------------------------------
CREATE TABLE asistencias_eventos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  evento_id BIGINT NOT NULL,
  asistio BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  INDEX (usuario_id),
  INDEX (evento_id),
  CONSTRAINT fk_ase_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_ase_evento FOREIGN KEY (evento_id)
    REFERENCES eventos(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla gastos
-- -----------------------------------------------------
CREATE TABLE gastos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  monto DECIMAL(38,2),
  titulo VARCHAR(255),
  partes_iguales BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  pagado_por BIGINT,
  grupo_id BIGINT,
  evento_id BIGINT,
  PRIMARY KEY (id),
  INDEX (pagado_por),
  INDEX (grupo_id),
  INDEX (evento_id),
  CONSTRAINT fk_gas_usuario FOREIGN KEY (pagado_por)
    REFERENCES usuarios(id)
    ON DELETE SET NULL,
  CONSTRAINT fk_gas_grupo FOREIGN KEY (grupo_id)
    REFERENCES grupos(id)
    ON DELETE SET NULL,
  CONSTRAINT fk_gas_evento FOREIGN KEY (evento_id)
    REFERENCES eventos(id)
    ON DELETE SET NULL
) ENGINE=InnoDB;

-- Tabla de asociación gastos_usuarios
CREATE TABLE gastos_usuarios (
  gasto_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  PRIMARY KEY (gasto_id, usuario_id),
  CONSTRAINT fk_gu_gasto FOREIGN KEY (gasto_id)
    REFERENCES gastos(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_gu_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabla cantidades_personalizadas
CREATE TABLE cantidades_personalizadas (
  gasto_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  monto DECIMAL(38,2),
  PRIMARY KEY (gasto_id, usuario_id),
  CONSTRAINT fk_cp_gasto FOREIGN KEY (gasto_id)
    REFERENCES gastos(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_cp_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla deudas_gastos
-- -----------------------------------------------------
CREATE TABLE deudas_gastos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  gasto_id BIGINT NOT NULL,
  deudor_id BIGINT NOT NULL,
  acreedor_id BIGINT NOT NULL,
  monto DECIMAL(38,2),
  saldado BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_saldado DATETIME,
  metodo_pago VARCHAR(255),
  notas TEXT,
  PRIMARY KEY (id),
  INDEX (gasto_id),
  INDEX (deudor_id),
  INDEX (acreedor_id),
  CONSTRAINT fk_dg_gasto FOREIGN KEY (gasto_id)
    REFERENCES gastos(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_dg_deudor FOREIGN KEY (deudor_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_dg_acreedor FOREIGN KEY (acreedor_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla imagenes
-- -----------------------------------------------------
CREATE TABLE imagenes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255),
  tipo_contenido VARCHAR(100),
  tamaño BIGINT,
  datos LONGTEXT,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  evento_id BIGINT,
  usuario_id BIGINT,
  grupo_id BIGINT,
  PRIMARY KEY (id),
  INDEX (evento_id),
  INDEX (usuario_id),
  INDEX (grupo_id),
  CONSTRAINT fk_img_evento FOREIGN KEY (evento_id)
    REFERENCES eventos(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_img_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_img_grupo FOREIGN KEY (grupo_id)
    REFERENCES grupos(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla notas
-- -----------------------------------------------------
CREATE TABLE notas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  titulo VARCHAR(255),
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  contenido LONGTEXT,
  grupo_id BIGINT,
  creada_por BIGINT,
  evento_id BIGINT,
  PRIMARY KEY (id),
  INDEX (grupo_id),
  INDEX (creada_por),
  INDEX (evento_id),
  CONSTRAINT fk_not_grupo FOREIGN KEY (grupo_id)
    REFERENCES grupos(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_not_usuario FOREIGN KEY (creada_por)
    REFERENCES usuarios(id)
    ON DELETE SET NULL,
  CONSTRAINT fk_not_evento FOREIGN KEY (evento_id)
    REFERENCES eventos(id)
    ON DELETE SET NULL
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla votaciones
-- -----------------------------------------------------
CREATE TABLE votaciones (
  id BIGINT NOT NULL AUTO_INCREMENT,
  titulo VARCHAR(255) NOT NULL,
  descripcion TEXT,
  grupo_id BIGINT NOT NULL,
  creador_id BIGINT NOT NULL,
  fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
  fecha_cierre DATETIME,
  estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
  PRIMARY KEY (id),
  INDEX (grupo_id),
  INDEX (creador_id),
  CONSTRAINT fk_vot_grupo FOREIGN KEY (grupo_id)
    REFERENCES grupos(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_vot_creador FOREIGN KEY (creador_id)
    REFERENCES usuarios(id)
    ON DELETE SET NULL
) ENGINE=InnoDB;

-- Tabla votacion_opciones
CREATE TABLE votacion_opciones (
  votacion_id BIGINT NOT NULL,
  opcion VARCHAR(255) NOT NULL,
  PRIMARY KEY (votacion_id, opcion),
  CONSTRAINT fk_vo_votacion FOREIGN KEY (votacion_id)
    REFERENCES votaciones(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla votos
-- -----------------------------------------------------
CREATE TABLE votos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  votacion_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  opcion VARCHAR(255) NOT NULL,
  fecha_voto DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX (votacion_id),
  INDEX (usuario_id),
  CONSTRAINT fk_vt_votacion FOREIGN KEY (votacion_id)
    REFERENCES votaciones(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_vt_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;
