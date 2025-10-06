-- =========================================
-- DATOS DE PRUEBA INICIALES PARA RENDER
-- =========================================

-- Usuario administrador por defecto
INSERT INTO usuarios (
    nombre_usuario, email, contraseña, nombre_completo, tipo_usuario
) VALUES (
    'admin', 'admin@sivil.com', 'admin$1', 'Administrador', 'admin'
) ON CONFLICT (nombre_usuario) DO NOTHING;

-- Usuario vendedor de prueba
INSERT INTO usuarios (
    nombre_usuario, email, contraseña, nombre_completo, tipo_usuario
) VALUES (
    'vendedor', 'vendedor@sivil.com', 'vendedor$1', 'Carlos Contreras', 'vendedor'
) ON CONFLICT (nombre_usuario) DO NOTHING;

-- Usuario comprador de prueba
INSERT INTO usuarios (
    nombre_usuario, email, contraseña, nombre_completo, tipo_usuario
) VALUES (
    'comprador', 'comprador@gmail.com', 'comprador$1', 'Victor Pardo', 'comprador'
) ON CONFLICT (nombre_usuario) DO NOTHING;

-- Libros de ejemplo
INSERT INTO libros (
    codigo_libro, titulo, autor, año_publicacion, precio, cantidad_stock, categoria
) VALUES 
    ('LIB001', 'El Quijote de la Mancha', 'Miguel de Cervantes', 1905, 25.99, 10, 'Clásicos'),
    ('LIB002', 'Cien Años de Soledad', 'Gabriel García Márquez', 1967, 18.50, 15, 'Literatura'),
    ('LIB003', 'Harry Potter y la Piedra Filosofal', 'J.K. Rowling', 1997, 22.00, 20, 'Fantasía'),
    ('LIB004', 'Code Complete', 'Steve McConnell', 1993, 35.50, 15, 'Programación'),
    ('LIB005', 'The Art of Computer Programming', 'Donald Knuth', 1968, 55.00, 19, 'Programación'),
    ('LIB006', 'Clean Code', 'Robert C. Martin', 2008, 29.99, 10, 'Programación'),
    ('LIB007', 'The Pragmatic Programmer', 'Andrew Hunt and David Thomas', 1999, 32.75, 17, 'Programación'),
    ('LIB008', 'Introduction to Algorithms', 'Thomas H. Cormen', 1990, 45.00, 10, 'Algoritmos'),
    ('LIB009', 'Structure and Interpretation of Computer Programs', 'Harold Abelson and Gerald Jay Sussman', 1985, 40.25, 9, 'Programación'),
    ('LIB010', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma', 1994, 38.99, 8, 'Diseño de Software'),
    ('LIB011', 'The Mythical Man-Month', 'Frederick Brooks', 1975, 28.50, 2, 'Ingeniería de Software'),
    ('LIB012', 'C Programming Language', 'Brian W. Kernighan and Dennis M. Ritchie', 1978, 25.00, 9, 'Programación'),
    ('LIB013', 'Godel, Escher, Bach: an Eternal Golden Braid', 'Douglas Hofstadter', 1979, 33.20, 5, 'Matemáticas'),
    ('LIB014', 'The Design of Everyday Things', 'Don Norman', 1988, 20.00, 12, 'Diseño'),
    ('LIB015', 'Structures: Or Why Things Don''t Fall Down', 'J.E. Gordon', 1978, 22.50, 10, 'Ingeniería'),
    ('LIB016', 'Física universitaria con Física Moderna', 'Hugh D. Young y Roger A. Freedman', 2009, 28, 0, 'Física'),
    ('LIB017', 'Calor y Termodinámica', ' Richard H. Dittman', 1985, 24, 0, 'Termodinámica')
ON CONFLICT (codigo_libro) DO NOTHING;
