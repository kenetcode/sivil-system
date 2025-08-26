# Documentación Completa - Base de Datos Sistema SIVIL

## DEFINICIÓN DE TABLAS

### **1. USUARIOS**
```
USUARIOS
├── id_usuario (PK, INT, AUTO_INCREMENT)
├── nombre_usuario (VARCHAR(50), UNIQUE, NOT NULL)
├── email (VARCHAR(100), UNIQUE, NOT NULL)
├── contraseña (VARCHAR(255), NOT NULL) -- Encriptada
├── nombre_completo (VARCHAR(150), NOT NULL)
├── telefono (VARCHAR(15))
├── direccion (TEXT)
├── tipo_usuario (ENUM('comprador', 'vendedor', 'admin'), NOT NULL)
├── estado (ENUM('activo', 'inactivo'), DEFAULT 'activo')
├── fecha_creacion (DATETIME, DEFAULT CURRENT_TIMESTAMP)
└── fecha_ultima_actualizacion (DATETIME, ON UPDATE CURRENT_TIMESTAMP)
```

### **2. LIBROS**
```
LIBROS
├── id_libro (PK, INT, AUTO_INCREMENT)
├── codigo_libro (VARCHAR(20), UNIQUE, NOT NULL)
├── titulo (VARCHAR(200), NOT NULL)
├── autor (VARCHAR(100), NOT NULL)
├── año_publicacion (INT, CHECK >= 1900 AND <= YEAR(CURDATE()))
├── precio (DECIMAL(10,2), CHECK > 0)
├── cantidad_stock (INT, CHECK >= 0, DEFAULT 0)
├── categoria (VARCHAR(50))
├── editorial (VARCHAR(100))
├── descripcion (TEXT)
├── imagen_url (VARCHAR(500))
├── estado (ENUM('activo', 'inactivo'), DEFAULT 'activo')
├── fecha_creacion (DATETIME, DEFAULT CURRENT_TIMESTAMP)
└── fecha_ultima_actualizacion (DATETIME, ON UPDATE CURRENT_TIMESTAMP)
```

### **3. VENTAS**
```
VENTAS
├── id_venta (PK, INT, AUTO_INCREMENT)
├── numero_factura (VARCHAR(50), UNIQUE, NOT NULL)
├── id_vendedor (FK → usuarios.id_usuario, NOT NULL)
├── nombre_cliente (VARCHAR(150), NOT NULL)
├── contacto_cliente (VARCHAR(100))
├── identificacion_cliente (VARCHAR(50))
├── subtotal (DECIMAL(10,2), NOT NULL)
├── descuento_aplicado (DECIMAL(10,2), DEFAULT 0)
├── impuestos (DECIMAL(10,2), DEFAULT 0)
├── total (DECIMAL(10,2), NOT NULL)
├── tipo_pago (ENUM('efectivo', 'tarjeta'), NOT NULL)
├── estado (ENUM('activa', 'inactiva', 'finalizada'), DEFAULT 'activa')
├── motivo_inactivacion (TEXT)
├── fecha_venta (DATETIME, DEFAULT CURRENT_TIMESTAMP)
└── fecha_modificacion (DATETIME, ON UPDATE CURRENT_TIMESTAMP)
```

### **4. COMPRAS_ONLINE**
```
COMPRAS_ONLINE
├── id_compra (PK, INT, AUTO_INCREMENT)
├── numero_orden (VARCHAR(50), UNIQUE, NOT NULL)
├── id_comprador (FK → usuarios.id_usuario, NOT NULL)
├── subtotal (DECIMAL(10,2), NOT NULL)
├── impuestos (DECIMAL(10,2), DEFAULT 0)
├── total (DECIMAL(10,2), NOT NULL)
├── direccion_entrega (TEXT)
├── estado_compra (ENUM('pendiente', 'procesada', 'enviada', 'entregada'), DEFAULT 'pendiente')
├── metodo_pago (VARCHAR(50), DEFAULT 'tarjeta')
├── fecha_compra (DATETIME, DEFAULT CURRENT_TIMESTAMP)
└── fecha_modificacion (DATETIME, ON UPDATE CURRENT_TIMESTAMP)
```

### **5. DETALLE_VENTA**
```
DETALLE_VENTA
├── id_detalle_venta (PK, INT, AUTO_INCREMENT)
├── id_venta (FK → ventas.id_venta, NOT NULL)
├── id_libro (FK → libros.id_libro, NOT NULL)
├── cantidad (INT, CHECK > 0, NOT NULL)
├── precio_unitario (DECIMAL(10,2), NOT NULL)
└── subtotal_item (DECIMAL(10,2), NOT NULL)
```

### **6. DETALLE_COMPRA**
```
DETALLE_COMPRA
├── id_detalle_compra (PK, INT, AUTO_INCREMENT)
├── id_compra (FK → compras_online.id_compra, NOT NULL)
├── id_libro (FK → libros.id_libro, NOT NULL)
├── cantidad (INT, CHECK > 0, NOT NULL)
├── precio_unitario (DECIMAL(10,2), NOT NULL)
└── subtotal_item (DECIMAL(10,2), NOT NULL)
```

### **7. PAGOS**
```
PAGOS
├── id_pago (PK, INT, AUTO_INCREMENT)
├── id_compra (FK → compras_online.id_compra, NULL)
├── id_venta (FK → ventas.id_venta, NULL)
├── metodo_pago (ENUM('tarjeta', 'efectivo'), NOT NULL)
├── monto (DECIMAL(10,2), NOT NULL)
├── estado_pago (ENUM('pendiente', 'completado', 'fallido'), DEFAULT 'pendiente')
├── datos_tarjeta_encriptados (TEXT) -- Solo para tarjeta
├── fecha_pago (DATETIME, DEFAULT CURRENT_TIMESTAMP)
└── referencia_transaccion (VARCHAR(100), UNIQUE)
```

### **8. COMPROBANTES_PAGO**
```
COMPROBANTES_PAGO
├── id_comprobante (PK, INT, AUTO_INCREMENT)
├── id_pago (FK → pagos.id_pago, NOT NULL)
├── nombre_archivo (VARCHAR(255), NOT NULL)
├── ruta_archivo (VARCHAR(500), NOT NULL)
├── tipo_archivo (VARCHAR(10), DEFAULT 'PDF')
├── tamaño_archivo (INT) -- En bytes
└── fecha_subida (DATETIME, DEFAULT CURRENT_TIMESTAMP)
```

## RELACIONES ENTRE TABLAS

### **Relaciones Principales:**
- **USUARIOS** → **VENTAS** (1:N) - Un vendedor puede realizar múltiples ventas
- **USUARIOS** → **COMPRAS_ONLINE** (1:N) - Un comprador puede realizar múltiples compras
- **VENTAS** → **DETALLE_VENTA** (1:N) - Una venta puede tener múltiples items
- **COMPRAS_ONLINE** → **DETALLE_COMPRA** (1:N) - Una compra puede tener múltiples items
- **LIBROS** → **DETALLE_VENTA** (1:N) - Un libro puede estar en múltiples ventas
- **LIBROS** → **DETALLE_COMPRA** (1:N) - Un libro puede estar en múltiples compras
- **COMPRAS_ONLINE** → **PAGOS** (1:N) - Una compra puede tener múltiples intentos de pago
- **VENTAS** → **PAGOS** (1:N) - Una venta puede tener múltiples formas de pago
- **PAGOS** → **COMPROBANTES_PAGO** (1:N) - Un pago puede tener múltiples comprobantes

### **Restricciones de Integridad:**
- Un pago debe estar asociado a una compra O a una venta (no ambas)
- No se puede eliminar un libro que tenga ventas/compras asociadas
- No se puede eliminar un usuario que tenga ventas/compras asociadas
- La cantidad en stock no puede ser negativa
- Los totales deben ser consistentes entre tablas padre e hijo

## HISTORIAS DE USUARIO Y TABLAS UTILIZADAS

### **MÓDULO VENTA**
| HU | Descripción | Tablas Utilizadas |
|---|---|---|
| **HU001** | Crear Venta | VENTAS, DETALLE_VENTA, LIBROS, PAGOS |
| **HU002** | Actualizar Venta | VENTAS, DETALLE_VENTA, LIBROS |
| **HU003** | Buscar Venta | VENTAS, DETALLE_VENTA, LIBROS |
| **HU004** | Inactivar Venta | VENTAS, LIBROS (restaurar stock) |
| **HU005** | Aplicar Descuentos a Ventas | VENTAS |

### **MÓDULO COMPRA ONLINE**
| HU | Descripción | Tablas Utilizadas |
|---|---|---|
| **HU006** | Crear Compra online | COMPRAS_ONLINE, DETALLE_COMPRA, LIBROS, PAGOS |
| **HU007** | Actualizar Compra online | COMPRAS_ONLINE, DETALLE_COMPRA |
| **HU008** | Buscar Compra online | COMPRAS_ONLINE, DETALLE_COMPRA, LIBROS, USUARIOS |
| **HU009** | Eliminar Compra online | COMPRAS_ONLINE, DETALLE_COMPRA, LIBROS |

### **MÓDULO INVENTARIO**
| HU | Descripción | Tablas Utilizadas |
|---|---|---|
| **HU013** | Agregar Libro al Inventario | LIBROS |
| **HU014** | Actualizar Información de Libro | LIBROS |
| **HU015** | Eliminar Libro del Inventario | LIBROS |
| **HU016** | Consultar Inventario de Libros | LIBROS |
| **HU017** | Visualizar Catálogo de Libros | LIBROS |

### **MÓDULO PAGO**
| HU | Descripción | Tablas Utilizadas |
|---|---|---|
| **HU010** | Habilitar Pago con tarjeta | PAGOS |
| **HU011** | Habilitar Pago Efectivo | PAGOS |
| **HU012** | Guardar Comprobante de Pago | COMPROBANTES_PAGO, PAGOS |

### **MÓDULO USUARIO**
| HU | Descripción | Tablas Utilizadas |
|---|---|---|
| **HU018** | Crear Usuario | USUARIOS |
| **HU019** | Actualizar Usuario | USUARIOS |
| **HU020** | Buscar Usuario | USUARIOS |
| **HU021** | Inactivar Usuario | USUARIOS |

## CÓDIGO SQL PARA POSTGRESQL

### Script Completo para Creación de Base de Datos

```sql
-- =========================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS SIVIL
-- PostgreSQL
-- =========================================

-- Crear base de datos
CREATE DATABASE sivil_db;

--luego de crear la base de datos hacer click derecho sobre la db y darle en create script y pegar lo que sigue de este codigo

-- Crear tipos ENUM personalizados
CREATE TYPE tipo_usuario_enum AS ENUM ('comprador', 'vendedor', 'admin');
CREATE TYPE estado_enum AS ENUM ('activo', 'inactivo');
CREATE TYPE estado_venta_enum AS ENUM ('activa', 'inactiva', 'finalizada');
CREATE TYPE estado_compra_enum AS ENUM ('pendiente', 'procesada', 'enviada', 'entregada');
CREATE TYPE metodo_pago_enum AS ENUM ('tarjeta', 'efectivo');
CREATE TYPE estado_pago_enum AS ENUM ('pendiente', 'completado', 'fallido');

-- =========================================
-- TABLA: USUARIOS
-- =========================================
CREATE TABLE usuarios (
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

-- Trigger para actualizar fecha_ultima_actualizacion
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_ultima_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_usuarios_timestamp
    BEFORE UPDATE ON usuarios
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- =========================================
-- TABLA: LIBROS
-- =========================================
CREATE TABLE libros (
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

CREATE TRIGGER update_libros_timestamp
    BEFORE UPDATE ON libros
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- =========================================
-- TABLA: VENTAS
-- =========================================
CREATE TABLE ventas (
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

CREATE TRIGGER update_ventas_timestamp
    BEFORE UPDATE ON ventas
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- =========================================
-- TABLA: COMPRAS_ONLINE
-- =========================================
CREATE TABLE compras_online (
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

CREATE TRIGGER update_compras_timestamp
    BEFORE UPDATE ON compras_online
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- =========================================
-- TABLA: DETALLE_VENTA
-- =========================================
CREATE TABLE detalle_venta (
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
CREATE TABLE detalle_compra (
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
CREATE TABLE pagos (
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
CREATE TABLE comprobantes_pago (
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

-- Índices para búsquedas frecuentes
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_tipo ON usuarios(tipo_usuario);
CREATE INDEX idx_libros_codigo ON libros(codigo_libro);
CREATE INDEX idx_libros_titulo ON libros(titulo);
CREATE INDEX idx_libros_autor ON libros(autor);
CREATE INDEX idx_libros_estado_stock ON libros(estado, cantidad_stock);
CREATE INDEX idx_ventas_fecha ON ventas(fecha_venta);
CREATE INDEX idx_ventas_vendedor ON ventas(id_vendedor);
CREATE INDEX idx_compras_fecha ON compras_online(fecha_compra);
CREATE INDEX idx_compras_comprador ON compras_online(id_comprador);
CREATE INDEX idx_pagos_referencia ON pagos(referencia_transaccion);

-- =========================================
-- DATOS DE PRUEBA INICIALES
-- =========================================

-- Usuario administrador por defecto
INSERT INTO usuarios (
    nombre_usuario, email, contraseña, nombre_completo, tipo_usuario
) VALUES (
    'admin', 'admin@sivil.com', 'admin123', 'Administrador Sistema', 'admin'
);

-- Usuario vendedor de prueba
INSERT INTO usuarios (
    nombre_usuario, email, contraseña, nombre_completo, tipo_usuario
) VALUES (
    'vendedor1', 'vendedor@sivil.com', 'vend123', 'Juan Perez', 'vendedor'
);

-- Usuario comprador de prueba
INSERT INTO usuarios (
    nombre_usuario, email, contraseña, nombre_completo, tipo_usuario
) VALUES (
    'comprador1', 'comprador@gmail.com', 'comp123', 'Daniel Alvarez', 'comprador'
);

-- Libros de ejemplo
INSERT INTO libros (
    codigo_libro, titulo, autor, año_publicacion, precio, cantidad_stock, categoria
) VALUES 
    ('LIB001', 'El Quijote de la Mancha', 'Miguel de Cervantes', 1905, 25.99, 10, 'Clásicos'),
    ('LIB002', 'Cien Años de Soledad', 'Gabriel García Márquez', 1967, 18.50, 15, 'Literatura'),
    ('LIB003', 'Harry Potter y la Piedra Filosofal', 'J.K. Rowling', 1997, 22.00, 8, 'Fantasía');

-- =========================================
-- COMENTARIOS EN TABLAS
-- =========================================

COMMENT ON TABLE usuarios IS 'Almacena información de todos los usuarios del sistema';
COMMENT ON TABLE libros IS 'Inventario completo de libros disponibles';
COMMENT ON TABLE ventas IS 'Registro de ventas realizadas en tienda física';
COMMENT ON TABLE compras_online IS 'Registro de compras realizadas online';
COMMENT ON TABLE detalle_venta IS 'Items específicos de cada venta';
COMMENT ON TABLE detalle_compra IS 'Items específicos de cada compra online';
COMMENT ON TABLE pagos IS 'Procesamiento de pagos para ventas y compras';
COMMENT ON TABLE comprobantes_pago IS 'Comprobantes y evidencias de pagos realizados';

-- =========================================
-- FIN DEL SCRIPT
-- =========================================
```

## RESUMEN DEL DOCUMENTO

- **Total de Tablas**: 8
- **Total de HUs**: 21
- **Relaciones 1:N**: 9
- **Campos con FK**: 7
- **Campos únicos**: 6
- **Campos con validaciones**: 12

## INSTRUCCIONES DE EJECUCIÓN

1. **Conectar a PostgreSQL** como superusuario
2. **Ejecutar el script completo** en el orden presentado
3. **Verificar la creación** de todas las tablas y relaciones
4. **Comprobar datos de prueba** insertados correctamente



# ADICIONAL: GUÍA TÉCNICA PARA EL EQUIPO DE DESARROLLO
## Base de Datos SIVIL - ENUMs y Triggers Automáticos

---

## TIPOS ENUM - Guía Completa de Uso

Los ENUMs garantizan **integridad de datos** limitando los valores permitidos y mejoran el **rendimiento** al usar menos espacio que VARCHAR.

### **1. `tipo_usuario_enum`**
**Valores:** `'comprador'`, `'vendedor'`, `'admin'`  
**Tabla:** `usuarios`  
**Campo:** `tipo_usuario`

```sql
-- Correcto
INSERT INTO usuarios (..., tipo_usuario) VALUES (..., 'admin');

-- Error - valor no permitido
INSERT INTO usuarios (..., tipo_usuario) VALUES (..., 'supervisor');
```

### **2. `estado_enum`** 
**Valores:** `'activo'`, `'inactivo'`  
**Tablas:** `usuarios` y `libros`  
**Campos:** `estado`

```sql
-- Para desactivar un usuario
UPDATE usuarios SET estado = 'inactivo' WHERE id_usuario = 1;

-- Para activar un libro
UPDATE libros SET estado = 'activo' WHERE id_libro = 5;
```

### **3. `estado_venta_enum`**
**Valores:** `'activa'`, `'inactiva'`, `'finalizada'`  
**Tabla:** `ventas`  
**Campo:** `estado`  
**Flujo:** activa → finalizada (o inactiva para cancelaciones)

```sql
-- Finalizar una venta
UPDATE ventas SET estado = 'finalizada' WHERE id_venta = 10;
```

### **4. `estado_compra_enum`**
**Valores:** `'pendiente'`, `'procesada'`, `'enviada'`, `'entregada'`  
**Tabla:** `compras_online`  
**Campo:** `estado_compra`  
**Flujo:** pendiente → procesada → enviada → entregada

```sql
-- Actualizar estado de compra
UPDATE compras_online SET estado_compra = 'enviada' WHERE id_compra = 25;
```

### **5. `metodo_pago_enum`**
**Valores:** `'tarjeta'`, `'efectivo'`  
**Tablas:** `ventas`, `compras_online`, `pagos`  
**Campos:** `tipo_pago`, `metodo_pago`, `metodo_pago`

```sql
-- Registrar venta en efectivo
INSERT INTO ventas (..., tipo_pago) VALUES (..., 'efectivo');

-- Pago con tarjeta online
INSERT INTO compras_online (..., metodo_pago) VALUES (..., 'tarjeta');
```

### **6. `estado_pago_enum`**
**Valores:** `'pendiente'`, `'completado'`, `'fallido'`  
**Tabla:** `pagos`  
**Campo:** `estado_pago`

```sql
-- Marcar pago como completado
UPDATE pagos SET estado_pago = 'completado' WHERE id_pago = 15;
```

---

## SISTEMA DE TRIGGERS AUTOMÁTICOS

### **Función Central**
```sql
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_ultima_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
```

### **¿Cómo Funciona?**

**Activación Automática:**
- Se ejecuta **BEFORE UPDATE** en cada tabla
- **NO** requiere intervención del desarrollador
- Se aplica a **CADA ROW** modificado

**Tablas con Auto-Update:**
- `usuarios` → `update_usuarios_timestamp`
- `libros` → `update_libros_timestamp`  
- `ventas` → `update_ventas_timestamp`
- `compras_online` → `update_compras_timestamp`

### **Ejemplos Prácticos**

```sql
-- Al hacer este UPDATE:
UPDATE usuarios SET telefono = '555-1234' WHERE id_usuario = 1;

-- PostgreSQL automáticamente también ejecuta:
-- fecha_ultima_actualizacion = CURRENT_TIMESTAMP

-- Resultado: Los dos campos se actualizan
```

### **Importante para el Equipo**

**LO QUE SÍ HACER:**
```sql
-- Update normal - el timestamp se actualiza automáticamente
UPDATE libros SET precio = 29.99 WHERE id_libro = 1;

-- Multiple campos - timestamp automático
UPDATE usuarios SET 
    telefono = '555-9999', 
    direccion = 'Nueva dirección' 
WHERE id_usuario = 5;
```

**LO QUE NO HACER:**
```sql
-- NO intenten actualizar manualmente fecha_ultima_actualizacion
UPDATE usuarios SET 
    telefono = '555-1234',
    fecha_ultima_actualizacion = '2025-01-01'  -- Será sobrescrito
WHERE id_usuario = 1;
```

### **Beneficios del Sistema**

1. **Automático:** Sin código adicional en aplicación
2. **Consistente:** Siempre se actualiza correctamente  
3. **Confiable:** Imposible olvidar actualizar el timestamp
4. **Auditabilidad:** Tracking perfecto de modificaciones
