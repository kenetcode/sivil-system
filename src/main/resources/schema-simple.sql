-- =========================================
-- VERSIÓN SIMPLIFICADA SIN ENUMs PARA RENDER
-- Solo usar si la versión con ENUMs falla
-- =========================================

-- Función para actualizar timestamp automáticamente
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_ultima_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- =========================================
-- TABLA: USUARIOS (sin ENUMs)
-- =========================================
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario SERIAL PRIMARY KEY,
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    contraseña VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    telefono VARCHAR(15),
    direccion TEXT,
    tipo_usuario VARCHAR(20) NOT NULL CHECK (tipo_usuario IN ('comprador', 'vendedor', 'admin')),
    estado VARCHAR(20) DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trigger para usuarios
DROP TRIGGER IF EXISTS update_usuarios_timestamp ON usuarios;
CREATE TRIGGER update_usuarios_timestamp
    BEFORE UPDATE ON usuarios
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- =========================================
-- TABLA: LIBROS (sin ENUMs)
-- =========================================
CREATE TABLE IF NOT EXISTS libros (
    id_libro SERIAL PRIMARY KEY,
    codigo_libro VARCHAR(20) UNIQUE NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    año_publicacion INTEGER CHECK (año_publicacion >= 1900 AND año_publicacion <= EXTRACT(YEAR FROM CURRENT_DATE)),
    precio DECIMAL(10,2) CHECK (precio > 0) NOT NULL,
    cantidad_stock INTEGER CHECK (cantidad_stock >= 0) DEFAULT 0,
    categoria VARCHAR(50),
    editorial VARCHAR(100),
    descripcion TEXT,
    imagen_url VARCHAR(500),
    estado VARCHAR(20) DEFAULT 'activo' CHECK (estado IN ('activo', 'inactivo')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trigger para libros
DROP TRIGGER IF EXISTS update_libros_timestamp ON libros;
CREATE TRIGGER update_libros_timestamp
    BEFORE UPDATE ON libros
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();