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
    codigo_libro, titulo, autor, año_publicacion, precio, cantidad_stock, categoria, editorial, descripcion, imagen_url
) VALUES 
    ('LIB001', 'El Quijote de la Mancha', 'Miguel de Cervantes', 1905, 25.99, 10, 'Clásicos', 
     'Penguin Clásicos', 
     'La obra maestra de Cervantes que narra las aventuras de Don Quijote de la Mancha y su fiel escudero Sancho Panza. Una sátira sobre las novelas de caballería que se convirtió en una de las obras más influyentes de la literatura universal.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1546112331i/3836.jpg'),
    
    ('LIB002', 'Cien Años de Soledad', 'Gabriel García Márquez', 1967, 18.50, 15, 'Literatura', 
     'Editorial Sudamericana', 
     'La obra cumbre del realismo mágico que narra la historia de la familia Buendía a lo largo de siete generaciones en el pueblo ficticio de Macondo. Premio Nobel de Literatura 1982.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1327881361i/320.jpg'),
    
    ('LIB003', 'Harry Potter y la Piedra Filosofal', 'J.K. Rowling', 1997, 22.00, 20, 'Fantasía', 
     'Salamandra', 
     'El primer libro de la saga de Harry Potter. Un niño huérfano descubre que es un mago y es admitido en el Colegio Hogwarts de Magia y Hechicería, donde vivirá aventuras inolvidables.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1598823299i/42844155.jpg'),
    
    ('LIB004', 'Code Complete', 'Steve McConnell', 1993, 35.50, 15, 'Programación', 
     'Microsoft Press', 
     'Guía práctica para la construcción de software de calidad. Cubre todos los aspectos del desarrollo de software, desde el diseño hasta las pruebas, con ejemplos prácticos y mejores prácticas.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1396837641i/4845.jpg'),
    
    ('LIB005', 'The Art of Computer Programming', 'Donald Knuth', 1968, 55.00, 19, 'Programación', 
     'Addison-Wesley', 
     'Serie monumental sobre algoritmos y programación considerada la biblia de la ciencia de la computación. Escrita por Donald Knuth, ganador del Premio Turing.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1387741681i/112239.jpg'),
    
    ('LIB006', 'Clean Code', 'Robert C. Martin', 2008, 29.99, 10, 'Programación', 
     'Prentice Hall', 
     'Manual de estilo para el desarrollo ágil de software. Enseña a escribir código limpio, mantenible y profesional, con principios y prácticas que todo desarrollador debe conocer.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1436202607i/3735293.jpg'),
    
    ('LIB007', 'The Pragmatic Programmer', 'Andrew Hunt and David Thomas', 1999, 32.75, 17, 'Programación', 
     'Addison-Wesley', 
     'Guía esencial que cubre temas desde la filosofía personal y el enfoque pragmático hasta técnicas de arquitectura que mantienen el código flexible y fácil de adaptar.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1401432508i/4099.jpg'),
    
    ('LIB008', 'Introduction to Algorithms', 'Thomas H. Cormen', 1990, 45.00, 10, 'Algoritmos', 
     'MIT Press', 
     'Libro de texto exhaustivo sobre algoritmos. Cubre un amplio rango de algoritmos en profundidad, presentando el diseño y análisis de algoritmos de manera rigurosa pero accesible.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1387741681i/108986.jpg'),
    
    ('LIB009', 'Structure and Interpretation of Computer Programs', 'Harold Abelson and Gerald Jay Sussman', 1985, 40.25, 9, 'Programación', 
     'MIT Press', 
     'Texto clásico del MIT sobre los principios fundamentales de la programación. Utiliza Scheme para enseñar conceptos de programación funcional, abstracción y diseño de sistemas.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1391032527i/43713.jpg'),
    
    ('LIB010', 'Design Patterns: Elements of Reusable Object-Oriented Software', 'Erich Gamma', 1994, 38.99, 8, 'Diseño de Software', 
     'Addison-Wesley', 
     'El libro fundamental sobre patrones de diseño escrito por la Gang of Four. Cataloga 23 patrones de diseño que resuelven problemas comunes en el desarrollo de software orientado a objetos.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1348027904i/85009.jpg'),
    
    ('LIB011', 'The Mythical Man-Month', 'Frederick Brooks', 1975, 28.50, 2, 'Ingeniería de Software', 
     'Addison-Wesley', 
     'Ensayos sobre ingeniería de software y gestión de proyectos basados en la experiencia de Brooks en IBM. Incluye la famosa ley de Brooks sobre cómo añadir personas a proyectos retrasados.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1348430425i/13629.jpg'),
    
    ('LIB012', 'C Programming Language', 'Brian W. Kernighan and Dennis M. Ritchie', 1978, 25.00, 9, 'Programación', 
     'Prentice Hall', 
     'El libro definitivo sobre el lenguaje C escrito por los creadores del lenguaje. Conocido como K&R, es la referencia estándar para aprender programación en C.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1391032527i/515601.jpg'),
    
    ('LIB013', 'Godel, Escher, Bach: an Eternal Golden Braid', 'Douglas Hofstadter', 1979, 33.20, 5, 'Matemáticas', 
     'Basic Books', 
     'Exploración de la conciencia, el pensamiento y la inteligencia artificial a través de las obras de Gödel, Escher y Bach. Ganador del Premio Pulitzer de No Ficción.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1528367139i/24113.jpg'),
    
    ('LIB014', 'The Design of Everyday Things', 'Don Norman', 1988, 20.00, 12, 'Diseño', 
     'Basic Books', 
     'Libro fundamental sobre diseño centrado en el usuario. Norman explica los principios psicológicos detrás del buen y mal diseño de objetos cotidianos.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1442460745i/840.jpg'),
    
    ('LIB015', 'Structures: Or Why Things Don''t Fall Down', 'J.E. Gordon', 1978, 22.50, 10, 'Ingeniería', 
     'Penguin Books', 
     'Introducción accesible a la ciencia de estructuras y materiales. Explica por qué las cosas se mantienen en pie y ocasionalmente se caen, desde puentes hasta huesos.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1320562005i/245344.jpg'),
    
    ('LIB016', 'Física universitaria con Física Moderna', 'Hugh D. Young y Roger A. Freedman', 2009, 25, 0, 'Física', 
     'Pearson Educación', 
     'Texto completo de física para cursos universitarios. Cubre mecánica, ondas, termodinámica, electromagnetismo, óptica, física moderna, mecánica cuántica y más.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1442864006i/106034.jpg'),
    
    ('LIB017', 'Calor y Termodinámica', 'Richard H. Dittman', 1985, 25, 0, 'Termodinámica', 
     'McGraw-Hill', 
     'Texto fundamental sobre termodinámica que cubre las leyes de la termodinámica, entropía, máquinas térmicas y aplicaciones prácticas en ingeniería.',
     'https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1328341832i/1142382.jpg')
ON CONFLICT (codigo_libro) DO NOTHING;
