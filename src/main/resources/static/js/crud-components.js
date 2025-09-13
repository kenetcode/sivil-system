/**
 * Funciones JavaScript para manejar los componentes CRUD reutilizables
 * Sistema SIVIL - Fragmentos de tabla y modal
 */

// Variables globales
let currentModal = null;
let currentEntityId = null;
let currentMode = null; // 'add', 'edit', 'view', 'delete'
let baseUrl = '';

/**
 * Inicializa los componentes CRUD
 * @param {string} entityBaseUrl - URL base para las operaciones CRUD de la entidad
 */
function initCrudComponents(entityBaseUrl) {
    baseUrl = entityBaseUrl;
    console.log('Componentes CRUD inicializados para:', entityBaseUrl);
}

/**
 * Abre el modal en el modo especificado
 * @param {string} mode - Modo del modal: 'add', 'edit', 'view'
 * @param {string|null} entityId - ID de la entidad (null para agregar)
 */
function openModal(mode, entityId = null) {
    currentMode = mode;
    currentEntityId = entityId;
    currentModal = new bootstrap.Modal(document.getElementById('crudModal'));
    
    // Actualizar título del modal
    updateModalTitle(mode);
    
    // Mostrar/ocultar botones según el modo
    updateModalButtons(mode);
    
    // Cargar contenido del modal
    if (mode === 'add') {
        loadFormForAdd();
    } else if (mode === 'edit' || mode === 'view') {
        loadFormForEditOrView(entityId, mode);
    }
    
    currentModal.show();
}

/**
 * Actualiza el título del modal según el modo
 * @param {string} mode - Modo actual
 */
function updateModalTitle(mode) {
    const titleElement = document.querySelector('.modal-title span');
    const iconElement = document.querySelector('.modal-title i');
    
    switch(mode) {
        case 'add':
            iconElement.className = 'bi bi-plus-circle me-2';
            titleElement.textContent = 'Agregar Registro';
            break;
        case 'edit':
            iconElement.className = 'bi bi-pencil me-2';
            titleElement.textContent = 'Editar Registro';
            break;
        case 'view':
            iconElement.className = 'bi bi-eye me-2';
            titleElement.textContent = 'Ver Detalles';
            break;
    }
}

/**
 * Actualiza la visibilidad de los botones del modal
 * @param {string} mode - Modo actual
 */
function updateModalButtons(mode) {
    const saveBtn = document.getElementById('saveButton');
    const editBtn = document.getElementById('editButton');
    const deleteBtn = document.getElementById('deleteButton');
    
    // Ocultar todos los botones primero
    saveBtn.style.display = 'none';
    editBtn.style.display = 'none';
    deleteBtn.style.display = 'none';
    
    switch(mode) {
        case 'add':
        case 'edit':
            saveBtn.style.display = 'inline-block';
            break;
        case 'view':
            editBtn.style.display = 'inline-block';
            deleteBtn.style.display = 'inline-block';
            break;
    }
}

/**
 * Carga el formulario para agregar un nuevo registro
 */
function loadFormForAdd() {
    showLoading();
    
    fetch(`${baseUrl}/form/new`)
        .then(response => response.text())
        .then(html => {
            document.getElementById('modalContent').innerHTML = html;
            hideLoading();
        })
        .catch(error => {
            console.error('Error al cargar formulario:', error);
            showError('Error al cargar el formulario');
        });
}

/**
 * Carga el formulario para editar o ver un registro
 * @param {string} entityId - ID de la entidad
 * @param {string} mode - Modo: 'edit' o 'view'
 */
function loadFormForEditOrView(entityId, mode) {
    showLoading();
    
    fetch(`${baseUrl}/${entityId}/${mode}`)
        .then(response => response.text())
        .then(html => {
            document.getElementById('modalContent').innerHTML = html;
            hideLoading();
        })
        .catch(error => {
            console.error('Error al cargar datos:', error);
            showError('Error al cargar los datos');
        });
}

/**
 * Guarda los datos del formulario
 */
function saveData() {
    const form = document.getElementById('crudForm');
    if (!form) {
        showError('No se encontró el formulario');
        return;
    }
    
    // Validar formulario
    if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return;
    }
    
    const formData = new FormData(form);
    const url = currentMode === 'add' ? `${baseUrl}/save` : `${baseUrl}/${currentEntityId}/update`;
    const method = currentMode === 'add' ? 'POST' : 'PUT';
    
    showLoading();
    
    fetch(url, {
        method: method,
        body: formData,
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        }
        throw new Error('Error en la respuesta del servidor');
    })
    .then(data => {
        hideLoading();
        if (data.success) {
            showSuccess(data.message || 'Registro guardado correctamente');
            setTimeout(() => {
                currentModal.hide();
                location.reload(); // Recargar la página para mostrar los cambios
            }, 1500);
        } else {
            showError(data.message || 'Error al guardar el registro');
            if (data.errors) {
                showFormErrors(data.errors);
            }
        }
    })
    .catch(error => {
        hideLoading();
        console.error('Error al guardar:', error);
        showError('Error al guardar el registro');
    });
}

/**
 * Habilita la edición desde el modo vista
 */
function enableEdit() {
    currentMode = 'edit';
    updateModalTitle('edit');
    updateModalButtons('edit');
    
    // Habilitar campos del formulario
    const form = document.getElementById('crudForm');
    if (form) {
        const inputs = form.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            input.readOnly = false;
            input.disabled = false;
        });
    }
}

/**
 * Confirma la eliminación de un registro
 * @param {string} entityId - ID de la entidad a eliminar
 */
function confirmDelete(entityId = null) {
    const id = entityId || currentEntityId;
    if (!id) {
        showError('No se especificó qué registro eliminar');
        return;
    }
    
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
    
    // Cargar detalles del registro a eliminar
    fetch(`${baseUrl}/${id}/details`)
        .then(response => response.text())
        .then(html => {
            document.getElementById('deleteDetails').innerHTML = html;
        })
        .catch(error => {
            console.error('Error al cargar detalles:', error);
            document.getElementById('deleteDetails').innerHTML = `<p>ID: ${id}</p>`;
        });
    
    // Configurar botón de confirmación
    document.getElementById('confirmDeleteButton').onclick = () => executeDelete(id, deleteModal);
    
    deleteModal.show();
}

/**
 * Ejecuta la eliminación del registro
 * @param {string} entityId - ID de la entidad
 * @param {bootstrap.Modal} deleteModal - Modal de confirmación
 */
function executeDelete(entityId, deleteModal) {
    fetch(`${baseUrl}/${entityId}/delete`, {
        method: 'DELETE',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            deleteModal.hide();
            if (currentModal) currentModal.hide();
            showSuccess(data.message || 'Registro eliminado correctamente');
            setTimeout(() => {
                location.reload();
            }, 1500);
        } else {
            showError(data.message || 'Error al eliminar el registro');
        }
    })
    .catch(error => {
        console.error('Error al eliminar:', error);
        showError('Error al eliminar el registro');
    });
}

/**
 * Muestra los detalles de un registro (al hacer clic en una fila)
 * @param {Object} item - Datos del registro
 */
function showDetails(item) {
    // Esta función se puede personalizar según las necesidades
    console.log('Mostrando detalles de:', item);
    
    // Por defecto, abrir en modo vista
    if (typeof item === 'object' && item.id) {
        openModal('view', item.id);
    }
}

/**
 * Acción personalizada para registros
 * @param {string} entityId - ID de la entidad
 */
function customAction(entityId) {
    console.log('Acción personalizada para:', entityId);
    // Implementar según las necesidades específicas
}

/**
 * Funciones de utilidad para UI
 */
function showLoading() {
    document.getElementById('modalContent').innerHTML = `
        <div class="text-center">
            <div class="spinner-border" role="status">
                <span class="visually-hidden">Cargando...</span>
            </div>
            <p class="mt-2">Cargando...</p>
        </div>
    `;
}

function hideLoading() {
    // El loading se oculta automáticamente al cargar el contenido
}

function showError(message) {
    const errorDiv = document.getElementById('errorMessages');
    const errorList = document.getElementById('errorList');
    
    if (errorDiv && errorList) {
        errorList.innerHTML = `<li>${message}</li>`;
        errorDiv.style.display = 'block';
    } else {
        alert('Error: ' + message);
    }
}

function showSuccess(message) {
    const successDiv = document.getElementById('successMessage');
    const successText = document.getElementById('successText');
    
    if (successDiv && successText) {
        successText.textContent = message;
        successDiv.style.display = 'block';
    } else {
        alert(message);
    }
}

function showFormErrors(errors) {
    const errorList = document.getElementById('errorList');
    if (errorList) {
        errorList.innerHTML = '';
        errors.forEach(error => {
            const li = document.createElement('li');
            li.textContent = error;
            errorList.appendChild(li);
        });
    }
}

// Limpiar mensajes al abrir el modal
document.addEventListener('DOMContentLoaded', function() {
    const modalElement = document.getElementById('crudModal');
    if (modalElement) {
        modalElement.addEventListener('show.bs.modal', function() {
            // Limpiar mensajes de error y éxito
            const errorDiv = document.getElementById('errorMessages');
            const successDiv = document.getElementById('successMessage');
            
            if (errorDiv) errorDiv.style.display = 'none';
            if (successDiv) successDiv.style.display = 'none';
        });
    }
});