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