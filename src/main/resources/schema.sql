-- =========================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS SIVIL
-- PostgreSQL para Deploy en Render
-- =========================================

-- Crear tipos ENUM personalizados
CREATE TYPE IF NOT EXISTS tipo_usuario_enum AS ENUM ('comprador', 'vendedor', 'admin');
CREATE TYPE IF NOT EXISTS estado_enum AS ENUM ('activo', 'inactivo');
CREATE TYPE IF NOT EXISTS estado_venta_enum AS ENUM ('activa', 'inactiva', 'finalizada');
CREATE TYPE IF NOT EXISTS estado_compra_enum AS ENUM ('pendiente', 'procesada', 'enviada', 'entregada');
CREATE TYPE IF NOT EXISTS metodo_pago_enum AS ENUM ('tarjeta', 'efectivo');
CREATE TYPE IF NOT EXISTS estado_pago_enum AS ENUM ('pendiente', 'completado', 'fallido');

-- Función para actualizar timestamp automáticamente
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_ultima_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- =========================================
-- TABLA: USUARIOS
-- =========================================
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario SERIAL PRIMARY KEY,
    nombre_usuario VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    contraseña VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    telefono VARCHAR(15),
    direccion TEXT,
    tipo_usuario tipo_usuario_enum NOT NULL,
    estado estado_enum DEFAULT 'activo',
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
-- TABLA: LIBROS
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
    estado estado_enum DEFAULT 'activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trigger para libros
DROP TRIGGER IF EXISTS update_libros_timestamp ON libros;
CREATE TRIGGER update_libros_timestamp
    BEFORE UPDATE ON libros
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- =========================================
-- TABLA: VENTAS
-- =========================================
CREATE TABLE IF NOT EXISTS ventas (
    id_venta SERIAL PRIMARY KEY,
    numero_factura VARCHAR(50) UNIQUE NOT NULL,
    id_vendedor INTEGER NOT NULL,
    nombre_cliente VARCHAR(150) NOT NULL,
    contacto_cliente VARCHAR(100),
    identificacion_cliente VARCHAR(50),
    subtotal DECIMAL(10,2) NOT NULL,
    descuento_aplicado DECIMAL(10,2) DEFAULT 0,
    impuestos DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    tipo_pago metodo_pago_enum NOT NULL,
    estado estado_venta_enum DEFAULT 'activa',
    motivo_inactivacion TEXT,
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ventas_vendedor FOREIGN KEY (id_vendedor) 
        REFERENCES usuarios(id_usuario) ON DELETE RESTRICT
);

-- Trigger para ventas
DROP TRIGGER IF EXISTS update_ventas_timestamp ON ventas;
CREATE TRIGGER update_ventas_timestamp
    BEFORE UPDATE ON ventas
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- =========================================
-- TABLA: COMPRAS_ONLINE
-- =========================================
CREATE TABLE IF NOT EXISTS compras_online (
    id_compra SERIAL PRIMARY KEY,
    numero_orden VARCHAR(50) UNIQUE NOT NULL,
    id_comprador INTEGER NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    impuestos DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    direccion_entrega TEXT,
    estado_compra estado_compra_enum DEFAULT 'pendiente',
    metodo_pago metodo_pago_enum DEFAULT 'tarjeta',
    fecha_compra TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_compras_comprador FOREIGN KEY (id_comprador) 
        REFERENCES usuarios(id_usuario) ON DELETE RESTRICT
);

-- Trigger para compras
DROP TRIGGER IF EXISTS update_compras_timestamp ON compras_online;
CREATE TRIGGER update_compras_timestamp
    BEFORE UPDATE ON compras_online
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- =========================================
-- TABLA: DETALLE_VENTA
-- =========================================
CREATE TABLE IF NOT EXISTS detalle_venta (
    id_detalle_venta SERIAL PRIMARY KEY,
    id_venta INTEGER NOT NULL,
    id_libro INTEGER NOT NULL,
    cantidad INTEGER CHECK (cantidad > 0) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal_item DECIMAL(10,2) NOT NULL,
    
    CONSTRAINT fk_detalle_venta_venta FOREIGN KEY (id_venta) 
        REFERENCES ventas(id_venta) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_venta_libro FOREIGN KEY (id_libro) 
        REFERENCES libros(id_libro) ON DELETE RESTRICT
);

-- =========================================
-- TABLA: DETALLE_COMPRA
-- =========================================
CREATE TABLE IF NOT EXISTS detalle_compra (
    id_detalle_compra SERIAL PRIMARY KEY,
    id_compra INTEGER NOT NULL,
    id_libro INTEGER NOT NULL,
    cantidad INTEGER CHECK (cantidad > 0) NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal_item DECIMAL(10,2) NOT NULL,
    
    CONSTRAINT fk_detalle_compra_compra FOREIGN KEY (id_compra) 
        REFERENCES compras_online(id_compra) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_compra_libro FOREIGN KEY (id_libro) 
        REFERENCES libros(id_libro) ON DELETE RESTRICT
);

-- =========================================
-- TABLA: PAGOS
-- =========================================
CREATE TABLE IF NOT EXISTS pagos (
    id_pago SERIAL PRIMARY KEY,
    id_compra INTEGER,
    id_venta INTEGER,
    metodo_pago metodo_pago_enum NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    estado_pago estado_pago_enum DEFAULT 'pendiente',
    datos_tarjeta_encriptados TEXT,
    fecha_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    referencia_transaccion VARCHAR(100) UNIQUE,
    
    CONSTRAINT fk_pagos_compra FOREIGN KEY (id_compra) 
        REFERENCES compras_online(id_compra) ON DELETE CASCADE,
    CONSTRAINT fk_pagos_venta FOREIGN KEY (id_venta) 
        REFERENCES ventas(id_venta) ON DELETE CASCADE,
    CONSTRAINT chk_pago_referencia CHECK (
        (id_compra IS NOT NULL AND id_venta IS NULL) OR 
        (id_compra IS NULL AND id_venta IS NOT NULL)
    )
);

-- =========================================
-- TABLA: COMPROBANTES_PAGO
-- =========================================
CREATE TABLE IF NOT EXISTS comprobantes_pago (
    id_comprobante SERIAL PRIMARY KEY,
    id_pago INTEGER NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    tipo_archivo VARCHAR(10) DEFAULT 'PDF',
    tamaño_archivo INTEGER,
    fecha_subida TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_comprobantes_pago FOREIGN KEY (id_pago) 
        REFERENCES pagos(id_pago) ON DELETE CASCADE
);

-- =========================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- =========================================
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_usuarios_tipo ON usuarios(tipo_usuario);
CREATE INDEX IF NOT EXISTS idx_libros_codigo ON libros(codigo_libro);
CREATE INDEX IF NOT EXISTS idx_libros_titulo ON libros(titulo);
CREATE INDEX IF NOT EXISTS idx_libros_autor ON libros(autor);
CREATE INDEX IF NOT EXISTS idx_libros_estado_stock ON libros(estado, cantidad_stock);
CREATE INDEX IF NOT EXISTS idx_ventas_fecha ON ventas(fecha_venta);
CREATE INDEX IF NOT EXISTS idx_ventas_vendedor ON ventas(id_vendedor);
CREATE INDEX IF NOT EXISTS idx_compras_fecha ON compras_online(fecha_compra);
CREATE INDEX IF NOT EXISTS idx_compras_comprador ON compras_online(id_comprador);
CREATE INDEX IF NOT EXISTS idx_pagos_referencia ON pagos(referencia_transaccion);