/**
 * Funciones JavaScript principales para Sistema SIVIL
 */

// Configuracion global
const API_BASE_URL = '/api';
const LOAD_DELAY = 300; // ms para simular carga

// Utilitarias generales
function mostrarLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.classList.remove('d-none');
    }
}

function ocultarLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.classList.add('d-none');
    }
}

function mostrarError(mensaje, containerId = 'errorMessage') {
    const errorContainer = document.getElementById(containerId);
    if (errorContainer) {
        errorContainer.textContent = mensaje;
        errorContainer.classList.remove('d-none');
    }
    console.error('Error:', mensaje);
}

function ocultarError(containerId = 'errorMessage') {
    const errorContainer = document.getElementById(containerId);
    if (errorContainer) {
        errorContainer.classList.add('d-none');
    }
}

// Formateo de datos
function formatearPrecio(precio) {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(precio);
}

function formatearFecha(fecha) {
    if (!fecha) return '-';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-CO', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatearEstado(estado) {
    const estados = {
        'activo': { text: 'Activo', class: 'bg-success' },
        'inactivo': { text: 'Inactivo', class: 'bg-danger' }
    };
    
    const config = estados[estado] || { text: estado, class: 'bg-secondary' };
    return `<span class="badge ${config.class}">${config.text}</span>`;
}

// Funciones para estadisticas (index.html)
async function cargarEstadisticas() {
    try {
        mostrarLoading('loading');
        
        // Simular carga de estadisticas desde la API
        await new Promise(resolve => setTimeout(resolve, LOAD_DELAY));
        
        const [libros, activos, disponibles] = await Promise.all([
            fetch(`${API_BASE_URL}/libros`).then(r => r.json()).catch(() => []),
            fetch(`${API_BASE_URL}/libros/activos`).then(r => r.json()).catch(() => []),
            fetch(`${API_BASE_URL}/libros/disponibles`).then(r => r.json()).catch(() => [])
        ]);
        
        // Obtener categorias unicas
        const categorias = new Set(libros.map(l => l.categoria).filter(c => c));
        
        // Actualizar contadores
        document.getElementById('totalLibros').textContent = libros.length;
        document.getElementById('librosActivos').textContent = activos.length;
        document.getElementById('librosDisponibles').textContent = disponibles.length;
        document.getElementById('totalCategorias').textContent = categorias.size;
        
        ocultarLoading('loading');
    } catch (error) {
        console.error('Error cargando estadisticas:', error);
        // Mostrar valores por defecto
        document.getElementById('totalLibros').textContent = '0';
        document.getElementById('librosActivos').textContent = '0';
        document.getElementById('librosDisponibles').textContent = '0';
        document.getElementById('totalCategorias').textContent = '0';
        ocultarLoading('loading');
    }
}

// Funciones para lista de libros (libro/lista.html)
async function cargarLibros(tipo = 'todos') {
    try {
        mostrarLoading('loading');
        ocultarError();
        
        let url = `${API_BASE_URL}/libros`;
        if (tipo === 'activos') url += '/activos';
        else if (tipo === 'disponibles') url += '/disponibles';
        
        const response = await fetch(url);
        if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);
        
        const libros = await response.json();
        mostrarLibrosEnTabla(libros);
        
        // Actualizar estado de botones
        document.querySelectorAll('.btn-group .btn').forEach(btn => {
            btn.classList.remove('active');
        });
        event?.target?.classList.add('active');
        
    } catch (error) {
        mostrarError(`Error cargando libros: ${error.message}`);
        document.getElementById('noData').classList.remove('d-none');
    } finally {
        ocultarLoading('loading');
    }
}

function mostrarLibrosEnTabla(libros) {
    const tbody = document.getElementById('tablaLibros');
    const noDataDiv = document.getElementById('noData');
    
    if (!libros || libros.length === 0) {
        tbody.innerHTML = '';
        noDataDiv.classList.remove('d-none');
        return;
    }
    
    noDataDiv.classList.add('d-none');
    
    tbody.innerHTML = libros.map(libro => `
        <tr>
            <td><code>${libro.codigo_libro || '-'}</code></td>
            <td>
                <strong>${libro.titulo || '-'}</strong>
                ${libro.descripcion ? `<br><small class="text-muted">${libro.descripcion.substring(0, 50)}...</small>` : ''}
            </td>
            <td>${libro.autor || '-'}</td>
            <td>${libro.anio_publicacion || '-'}</td>
            <td>${libro.precio ? formatearPrecio(libro.precio) : '-'}</td>
            <td>
                <span class="badge ${libro.cantidad_stock > 0 ? 'bg-success' : 'bg-danger'}">
                    ${libro.cantidad_stock || 0}
                </span>
            </td>
            <td>
                <span class="badge bg-info">${libro.categoria || '-'}</span>
            </td>
            <td>${formatearEstado(libro.estado)}</td>
            <td>
                <div class="btn-group btn-group-sm">
                    <a href="/libros/detalle?id=${libro.id_libro}" class="btn btn-outline-primary" title="Ver detalle">
                        <i class="bi bi-eye"></i>
                    </a>
                    <button type="button" class="btn btn-outline-success" onclick="agregarAlCarrito(${libro.id_libro})" title="Agregar al carrito">
                        <i class="bi bi-cart-plus"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Funciones para detalle de libro (libro/detalle.html)
async function cargarDetalleLibro(id) {
    try {
        mostrarLoading('loading');
        ocultarError();
        
        const response = await fetch(`${API_BASE_URL}/libros/${id}`);
        if (!response.ok) throw new Error(`Libro no encontrado (${response.status})`);
        
        const libro = await response.json();
        mostrarDetalleLibro(libro);
        
        document.getElementById('libroDetalle').classList.remove('d-none');
        
    } catch (error) {
        mostrarError(`Error cargando detalle: ${error.message}`);
        document.getElementById('errorMessage').classList.remove('d-none');
    } finally {
        ocultarLoading('loading');
    }
}

function mostrarDetalleLibro(libro) {
    // Imagen (placeholder si no tiene)
    const imgElement = document.getElementById('imagenLibro');
    imgElement.src = libro.imagen_url || 'https://via.placeholder.com/300x400/6c757d/white?text=Sin+Imagen';
    imgElement.alt = `Portada de ${libro.titulo}`;
    
    // Informacion basica
    document.getElementById('tituloLibro').textContent = libro.titulo || 'Titulo no disponible';
    document.getElementById('codigoLibro').textContent = libro.codigo_libro || '-';
    document.getElementById('autorLibro').textContent = libro.autor || '-';
    document.getElementById('añoLibro').textContent = libro.anio_publicacion || '-';
    document.getElementById('editorialLibro').textContent = libro.editorial || '-';
    document.getElementById('categoriaLibro').innerHTML = `<span class="badge bg-info">${libro.categoria || '-'}</span>`;
    document.getElementById('precioLibro').textContent = libro.precio ? formatearPrecio(libro.precio) : '-';
    document.getElementById('stockLibro').textContent = libro.cantidad_stock || 0;
    document.getElementById('descripcionLibro').textContent = libro.descripcion || 'Sin descripcion disponible';
    
    // Estado
    const estadoElement = document.getElementById('estadoLibro');
    estadoElement.innerHTML = formatearEstado(libro.estado);
    
    // Fechas
    document.getElementById('fechaCreacion').textContent = formatearFecha(libro.fecha_creacion);
    document.getElementById('fechaActualizacion').textContent = formatearFecha(libro.fecha_actualizacion);
}

// Funciones para busqueda (libro/buscar.html)
async function realizarBusqueda() {
    const criterios = obtenerCriteriosBusqueda();
    if (!tieneCriteriosValidos(criterios)) {
        mostrarError('Ingrese al menos un criterio de busqueda');
        return;
    }
    
    try {
        mostrarLoading('loadingBusqueda');
        ocultarError();
        document.getElementById('resultadosBusqueda').classList.add('d-none');
        document.getElementById('sinResultados').classList.add('d-none');
        
        const resultados = await buscarLibros(criterios);
        mostrarResultadosBusqueda(resultados);
        
    } catch (error) {
        mostrarError(`Error en la busqueda: ${error.message}`);
    } finally {
        ocultarLoading('loadingBusqueda');
    }
}

function obtenerCriteriosBusqueda() {
    return {
        titulo: document.getElementById('buscarTitulo').value.trim(),
        autor: document.getElementById('buscarAutor').value.trim(),
        categoria: document.getElementById('buscarCategoria').value,
        añoMin: document.getElementById('añoMin').value,
        añoMax: document.getElementById('añoMax').value,
        precioMin: document.getElementById('precioMin').value,
        precioMax: document.getElementById('precioMax').value,
        soloDisponibles: document.getElementById('soloDisponibles').checked
    };
}

function tieneCriteriosValidos(criterios) {
    return criterios.titulo || criterios.autor || criterios.categoria || 
           criterios.añoMin || criterios.añoMax || criterios.precioMin || criterios.precioMax;
}

async function buscarLibros(criterios) {
    // Construir URL con parametros de busqueda
    const params = new URLSearchParams();
    if (criterios.titulo) params.append('titulo', criterios.titulo);
    if (criterios.autor) params.append('autor', criterios.autor);
    if (criterios.categoria) params.append('categoria', criterios.categoria);
    if (criterios.añoMin) params.append('añoMin', criterios.añoMin);
    if (criterios.añoMax) params.append('añoMax', criterios.añoMax);
    if (criterios.precioMin) params.append('precioMin', criterios.precioMin);
    if (criterios.precioMax) params.append('precioMax', criterios.precioMax);
    if (criterios.soloDisponibles) params.append('disponibles', 'true');
    
    const response = await fetch(`${API_BASE_URL}/libros/buscar?${params}`);
    if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);
    
    return response.json();
}

function mostrarResultadosBusqueda(resultados) {
    if (!resultados || resultados.length === 0) {
        document.getElementById('sinResultados').classList.remove('d-none');
        return;
    }
    
    document.getElementById('cantidadResultados').textContent = resultados.length;
    
    const container = document.getElementById('listaResultados');
    container.innerHTML = resultados.map(libro => `
        <div class="col-md-6 col-lg-4 mb-3">
            <div class="card h-100">
                <div class="card-body">
                    <h6 class="card-title text-truncate">${libro.titulo}</h6>
                    <p class="card-text text-muted small mb-2">${libro.autor || 'Autor desconocido'}</p>
                    <p class="card-text small text-truncate-2">${libro.descripcion || 'Sin descripcion'}</p>
                    <div class="d-flex justify-content-between align-items-center">
                        <span class="h6 text-success mb-0">${libro.precio ? formatearPrecio(libro.precio) : 'N/A'}</span>
                        <span class="badge ${libro.cantidad_stock > 0 ? 'bg-success' : 'bg-danger'}">
                            Stock: ${libro.cantidad_stock || 0}
                        </span>
                    </div>
                </div>
                <div class="card-footer">
                    <div class="btn-group w-100">
                        <a href="/libros/detalle?id=${libro.id_libro}" class="btn btn-outline-primary btn-sm">
                            <i class="bi bi-eye"></i> Ver
                        </a>
                        <button type="button" class="btn btn-success btn-sm" onclick="agregarAlCarrito(${libro.id_libro})">
                            <i class="bi bi-cart-plus"></i> Agregar
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
    
    document.getElementById('resultadosBusqueda').classList.remove('d-none');
}

function limpiarFormulario() {
    document.getElementById('formBusqueda').reset();
    document.getElementById('resultadosBusqueda').classList.add('d-none');
    document.getElementById('sinResultados').classList.add('d-none');
}

function realizarBusquedaAutomatica() {
    const titulo = document.getElementById('buscarTitulo').value.trim();
    const autor = document.getElementById('buscarAutor').value.trim();
    
    if (titulo.length >= 3 || autor.length >= 3) {
        realizarBusqueda();
    }
}

// Funciones de filtrado (libro/lista.html)
function aplicarFiltros() {
    const titulo = document.getElementById('filtroTitulo').value.trim();
    const autor = document.getElementById('filtroAutor').value.trim();
    const categoria = document.getElementById('filtroCategoria').value;
    
    // Implementar filtrado local o hacer nueva peticion a la API
    console.log('Aplicando filtros:', { titulo, autor, categoria });
}

function limpiarFiltros() {
    document.getElementById('filtroTitulo').value = '';
    document.getElementById('filtroAutor').value = '';
    document.getElementById('filtroCategoria').value = '';
    cargarLibros('todos');
}

// Funciones de carrito (placeholder)
function agregarAlCarrito(libroId) {
    console.log('Agregando libro al carrito:', libroId);
    
    // Mostrar mensaje de confirmacion
    const toast = document.createElement('div');
    toast.className = 'toast position-fixed bottom-0 end-0 m-3';
    toast.innerHTML = `
        <div class="toast-header">
            <i class="bi bi-check-circle text-success me-2"></i>
            <strong class="me-auto">Exito</strong>
        </div>
        <div class="toast-body">
            Libro agregado al carrito correctamente
        </div>
    `;
    
    document.body.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
    
    // Remover el toast despues de que se oculte
    toast.addEventListener('hidden.bs.toast', () => {
        toast.remove();
    });
}

// Inicializacion global
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar tooltips de Bootstrap
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Animaciones de entrada
    document.querySelectorAll('.card').forEach((card, index) => {
        card.style.animationDelay = `${index * 0.1}s`;
        card.classList.add('fade-in');
    });
});