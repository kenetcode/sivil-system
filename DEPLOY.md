# 🚀 Guía Completa de Deploy - Sistema SIVIL en Render.com

## 📋 Tabla de Contenido
1. [Arquitectura del Deploy](#arquitectura-del-deploy)
2. [Archivos de Configuración Completos](#archivos-de-configuración-completos)
3. [Configuración de Variables de Entorno en Render](#configuración-de-variables-de-entorno-en-render)
4. [Procedimiento Completo de Deploy](#procedimiento-completo-de-deploy)
5. [Troubleshooting y Comandos Útiles](#troubleshooting-y-comandos-útiles)

---

## 🏗️ Arquitectura del Deploy

### **Stack Tecnológico:**
- ✅ **Frontend**: Thymeleaf + HTML + CSS (Server-Side Rendering)
- ✅ **Backend**: Spring Boot 3.5.5 + Java 21
- ✅ **Base de Datos**: PostgreSQL 17.6 (Render managed)
- ✅ **Containerización**: Docker
- ✅ **Hosting**: Render.com (Free Tier)

### **Flujo de Deploy:**
```
GitHub Push → Render Webhook → Docker Build → PostgreSQL Connection → App Live
```

---

## 📁 Archivos de Configuración Completos

### **1. application.properties** (Desarrollo Local)
```properties
spring.application.name=Sistema-digital-de-venta-de-libros

# Server port configuration
#server.port=8080

#Database connection

# Perfil por defecto para desarrollo
spring.profiles.active=prod

# Configuracion de hibernate y JPA
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.format_sql=true

# Configuracion adicional para manejar fechas
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=America/El_Salvador

# Configuracion para ver logs detallados de validacion
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.hibernate.tool.hbm2ddl=DEBUG

# Configuracion para generar un reporte de errores de validacion
spring.jpa.properties.hibernate.hbm2ddl.schema-validation.script.append=true
spring.jpa.properties.hibernate.hbm2ddl.schema-validation.script.create=true
spring.jpa.properties.hibernate.hbm2ddl.schema-validation.script=validation-errors.sql

# Configuracion de Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.mode=HTML
spring.thymeleaf.encoding=UTF-8
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
```

### **2. application-prod.properties** (Producción en Render)
```properties
# Configuracion para produccion en Render
# Usar variables separadas para evitar URL parsing issues
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=require
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# Hibernate configuration - Usar nuestro esquema personalizado
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Ejecutar nuestros scripts SQL personalizados
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql
spring.sql.init.continue-on-error=true

# Server configuration - Puerto para Render
#server.port=${PORT:10000}

# Thymeleaf production settings
spring.thymeleaf.cache=true

# Security (usar variables de entorno)
spring.security.user.name=${ADMIN_USER:admin}
spring.security.user.password=${ADMIN_PASSWORD:admin}

# Jackson configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=America/El_Salvador

# JPA optimizations for production
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.format_sql=false
```

### **3. schema.sql** (Estructura de Base de Datos)
```sql
-- =========================================
-- SCRIPT DE CREACIÓN DE BASE DE DATOS SIVIL
-- PostgreSQL - SCHEMA COMPLETO SIN ENUMs
-- =========================================

-- Limpiar tablas existentes si existen
DROP TABLE IF EXISTS comprobantes_pago CASCADE;
DROP TABLE IF EXISTS pagos CASCADE;  
DROP TABLE IF EXISTS detalle_compra CASCADE;
DROP TABLE IF EXISTS detalle_venta CASCADE;
DROP TABLE IF EXISTS compras_online CASCADE;
DROP TABLE IF EXISTS ventas CASCADE;
DROP TABLE IF EXISTS libros CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- Limpiar tipos ENUM existentes si existen
DROP TYPE IF EXISTS tipo_usuario_enum CASCADE;
DROP TYPE IF EXISTS estado_enum CASCADE;
DROP TYPE IF EXISTS estado_venta_enum CASCADE;
DROP TYPE IF EXISTS estado_compra_enum CASCADE;
DROP TYPE IF EXISTS metodo_pago_enum CASCADE;
DROP TYPE IF EXISTS estado_pago_enum CASCADE;

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
CREATE TABLE usuarios (
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
    tipo_pago VARCHAR(20) NOT NULL CHECK (tipo_pago IN ('tarjeta', 'efectivo')),
    estado VARCHAR(20) DEFAULT 'activa' CHECK (estado IN ('activa', 'inactiva', 'finalizada')),
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
CREATE TABLE compras_online (
    id_compra SERIAL PRIMARY KEY,
    numero_orden VARCHAR(50) UNIQUE NOT NULL,
    id_comprador INTEGER NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    impuestos DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    direccion_entrega TEXT,
    estado_compra VARCHAR(20) DEFAULT 'pendiente' CHECK (estado_compra IN ('pendiente', 'procesada', 'enviada', 'entregada')),
    metodo_pago VARCHAR(20) DEFAULT 'tarjeta' CHECK (metodo_pago IN ('tarjeta', 'efectivo')),
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
    metodo_pago VARCHAR(20) NOT NULL CHECK (metodo_pago IN ('tarjeta', 'efectivo')),
    monto DECIMAL(10,2) NOT NULL,
    estado_pago VARCHAR(20) DEFAULT 'pendiente' CHECK (estado_pago IN ('pendiente', 'completado', 'fallido')),
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
```

### **4. data.sql** (Datos de Prueba)
```sql
-- =========================================
-- DATOS DE PRUEBA INICIALES PARA RENDER
-- =========================================

-- Usuario administrador por defecto
INSERT INTO usuarios (
    nombre_usuario, email, contraseña, nombre_completo, tipo_usuario
) VALUES (
    'admin', 'admin@sivil.com', 'admin123', 'Administrador Sistema', 'admin'
) ON CONFLICT (nombre_usuario) DO NOTHING;

-- Usuario vendedor de prueba
INSERT INTO usuarios (
    nombre_usuario, email, contraseña, nombre_completo, tipo_usuario
) VALUES (
    'vendedor1', 'vendedor@sivil.com', 'vend123', 'Juan Perez', 'vendedor'
) ON CONFLICT (nombre_usuario) DO NOTHING;

-- Usuario comprador de prueba
INSERT INTO usuarios (
    nombre_usuario, email, contraseña, nombre_completo, tipo_usuario
) VALUES (
    'comprador1', 'comprador@gmail.com', 'comp123', 'Daniel Alvarez', 'comprador'
) ON CONFLICT (nombre_usuario) DO NOTHING;

-- Libros de ejemplo
INSERT INTO libros (
    codigo_libro, titulo, autor, año_publicacion, precio, cantidad_stock, categoria
) VALUES 
    ('LIB001', 'El Quijote de la Mancha', 'Miguel de Cervantes', 1905, 25.99, 10, 'Clásicos'),
    ('LIB002', 'Cien Años de Soledad', 'Gabriel García Márquez', 1967, 18.50, 15, 'Literatura'),
    ('LIB003', 'Harry Potter y la Piedra Filosofal', 'J.K. Rowling', 1997, 22.00, 8, 'Fantasía')
ON CONFLICT (codigo_libro) DO NOTHING;
```

### **5. Dockerfile**
```dockerfile
# Usar OpenJDK 21 como imagen base
FROM openjdk:21-jdk-slim

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos Maven wrapper
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Dar permisos de ejecución al wrapper
RUN chmod +x ./mvnw

# Descargar dependencias (para cache de Docker)
RUN ./mvnw dependency:resolve

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# Exponer puerto - Render usa PORT dinámico
EXPOSE ${PORT:-10000}

# Comando para ejecutar la aplicación
CMD ["java", "-Dspring.profiles.active=prod", "-jar", "target/Sistema-digital-de-venta-de-libros-0.0.1-SNAPSHOT.jar"]
```

### **6. .dockerignore**
```
target/
.mvn/wrapper/maven-wrapper.jar
.git
.gitignore
README.md
.env
.DS_Store
*.log
```

---

## 🔧 Configuración de Variables de Entorno en Render

### **Ubicación en Render:**
1. Dashboard → Tu servicio → **Settings** → **Environment Variables**

### **Variables Requeridas:**

#### **🔐 Base de Datos (OBLIGATORIAS):**
```
DB_HOST = dpg-d2n477gdl3ps73fupstg-a.oregon-postgres.render.com
DB_PORT = 5432
DB_NAME = sivil_db
DB_USER = sivil_db_user
DB_PASSWORD = 2VSz8VSFDBZhVZOUE8gbTovZXQ2C3cg4
```

#### **🛡️ Configuración de Aplicación:**
```
SPRING_PROFILES_ACTIVE = prod
ADMIN_USER = admin
ADMIN_PASSWORD = tupasswordseguro123
```

#### **⚠️ Variables Opcionales (Render las maneja automáticamente):**
```
PORT = (Render lo asigna dinámicamente)
DATABASE_URL = (Mantener si existe, pero no se usa)
```

### **🔍 Cómo obtener las credenciales de DB:**
1. **Dashboard Render** → **PostgreSQL service** → **Info**
2. Copiar:
   - **Hostname** → DB_HOST
   - **Port** → DB_PORT  
   - **Database** → DB_NAME
   - **Username** → DB_USER
   - **Password** → DB_PASSWORD

---

## 🚀 Procedimiento Completo de Deploy

### **FASE 1: Preparación del Repositorio**

#### **1.1 Verificar archivos de configuración:**
```bash
# Verificar que existen todos los archivos
ls -la src/main/resources/
# Debería mostrar:
# - application.properties
# - application-prod.properties  
# - schema.sql
# - data.sql

ls -la
# Debería mostrar:
# - Dockerfile
# - .dockerignore
```

#### **1.2 Commit y Push:**
```bash
git add .
git commit -m "Complete deployment configuration for Render"
git push origin main
```

### **FASE 2: Configuración en Render.com**

#### **2.1 Crear cuenta y servicios:**
1. **Registrarse**: [render.com](https://render.com) → "Get Started for Free"
2. **Conectar GitHub**: Autorizar acceso a repositorios

#### **2.2 Crear Base de Datos PostgreSQL:**
1. **New** → **PostgreSQL**
2. **Configuración**:
   - Name: `sivil-db`
   - Database: `sivil_db`
   - User: `sivil_db_user`
   - Region: `Oregon (US West)`
   - **Plan**: **Free** 
3. **Create Database**
4. **⚠️ IMPORTANTE**: Guardar credenciales de conexión

#### **2.3 Crear Web Service:**
1. **New** → **Web Service**
2. **Connect Repository**: Seleccionar `sivil-system`
3. **Configuración**:
   - Name: `sivil-system`
   - Region: `Oregon (US West)`
   - Branch: `main`
   - Runtime: **Docker**
   - Plan: **Free**

#### **2.4 Configurar Variables de Entorno:**
Agregar las variables listadas en la sección anterior.

#### **2.5 Deploy:**
1. **Create Web Service**
2. **Esperar**: 5-10 minutos para primer deploy
3. **Monitorear logs** en tiempo real

### **FASE 3: Verificación del Deploy**

#### **3.1 Verificar aplicación:**
- **URL**: `https://sivil-system.onrender.com`
- **Verificar**: 
  - ✅ Página principal carga
  - ✅ CSS se aplica
  - ✅ Libros de prueba aparecen
  - ✅ Navegación funciona

#### **3.2 Verificar base de datos:**
- **Dashboard Render** → **PostgreSQL** → **Connect**
- Verificar que existen las tablas y datos de prueba

#### **3.3 Verificar logs:**
```
✅ Started SistemaDigitalDeVentaDeLibrosApplication
✅ Tomcat started on port 8080
✅ Your service is live 🎉
```

---

## 🔧 Troubleshooting y Comandos Útiles

### **🚨 Problemas Comunes:**

#### **Error: "No open ports detected"**
**Solución**: Descomentar `server.port=${PORT:10000}` en application-prod.properties

#### **Error: "Database connection failed"**
**Solución**: Verificar variables DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD

#### **Error: "operator does not exist: estado_enum = character varying"**
**Solución**: ✅ Ya resuelto - schema usa VARCHAR en lugar de ENUMs

### **🛠️ Comandos de Mantenimiento:**

#### **Testing Local con Perfil Producción:**
```bash
# Compilar
./mvnw clean package -DskipTests

# Ejecutar con perfil prod (usando DB local)
export DATABASE_URL=jdbc:postgresql://10.255.255.254:5432/sivil_db
java -Dspring.profiles.active=prod -jar target/Sistema-digital-de-venta-de-libros-0.0.1-SNAPSHOT.jar
```

#### **Testing Docker Local:**
```bash
# Construir imagen
docker build -t sivil-system .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e DB_HOST="10.255.255.254" \
  -e DB_PORT="5432" \
  -e DB_NAME="sivil_db" \
  -e DB_USER="postgres" \
  -e DB_PASSWORD="Ken 1424. $" \
  -e SPRING_PROFILES_ACTIVE=prod \
  sivil-system
```

#### **Git para Redeploy:**
```bash
# Cualquier push a main triggerea redeploy automático
git add .
git commit -m "Update application"
git push origin main
```

### **📊 Monitoreo:**
- **Logs en tiempo real**: Dashboard Render → Service → Logs
- **Métricas**: Dashboard Render → Service → Metrics  
- **DB Status**: Dashboard Render → PostgreSQL → Metrics

### **🔄 Redeploy Manual:**
Dashboard Render → Service → **Manual Deploy** → **Deploy latest commit**

---

## ✅ Checklist Final

### **Antes del Deploy:**
- [ ] Todos los archivos de configuración están presentes
- [ ] Las credenciales están en variables de entorno (no hardcodeadas)
- [ ] El código compila localmente: `./mvnw clean compile`
- [ ] Git push realizado correctamente

### **Durante el Deploy:**
- [ ] PostgreSQL database creada en Render
- [ ] Variables de entorno configuradas correctamente
- [ ] Web service conectado al repositorio correcto
- [ ] Runtime configurado como Docker

### **Después del Deploy:**
- [ ] Aplicación accesible en la URL asignada
- [ ] Datos de prueba cargados correctamente
- [ ] CSS y recursos estáticos funcionando
- [ ] Logs sin errores críticos

### **🎯 URL Final:**
**Tu aplicación estará disponible en**: `https://sivil-system.onrender.com`

---

**Documentación oficial**: 
- [Render Docs](https://render.com/docs)
- [Spring Boot Docs](https://spring.io/projects/spring-boot)