# Fragmentos CRUD Reutilizables - Sistema SIVIL

Este documento explica cómo usar los fragmentos reutilizables para tablas y modales CRUD que se han creado para el Sistema SIVIL.

## Archivos Creados

### 1. Fragmentos HTML
- `src/main/resources/templates/fragments/table.html` - Fragmentos para tablas
- `src/main/resources/templates/fragments/modal.html` - Fragmentos para modales
- `src/main/resources/templates/fragments/ejemplo-form.html` - Ejemplo de formulario

### 2. JavaScript
- `src/main/resources/static/js/crud-components.js` - Funciones JavaScript para manejar los modales

### 3. Controlador de Prueba
- `src/main/java/com/sivil/controller/PruebaFragmentsController.java` - Ejemplo de uso
- `src/main/resources/templates/prueba-fragments.html` - Página de demostración

## Cómo Usar los Fragmentos

### 1. Fragment de Tabla CRUD Completa

```html
<div th:replace="~{fragments/table :: crud-table (
    title='Mi Tabla',
    subtitle='Subtítulo opcional',
    items=${miLista},
    columns=${misColumnas},
    actions=${{add: true, view: true, edit: true, delete: true}}
)}"></div>
```

### 2. Fragment de Tabla Simple

```html
<div th:replace="~{fragments/table :: simple-table (
    title='Tabla Simple',
    items=${misDatos},
    headers=${'ID', 'Nombre', 'Estado'}
)}"></div>
```

### 3. Fragment de Modal CRUD

```html
<div th:replace="~{fragments/modal :: crud-modal (
    modalId='miModal',
    title='Gestionar Registro'
)}"></div>
```

### 4. Fragment de Modal de Eliminación

```html
<div th:replace="~{fragments/modal :: delete-modal (
    modalId='deleteModal',
    title='Confirmar Eliminación'
)}"></div>
```

## Configuración en el Controlador

### Ejemplo Básico

```java
@Controller
@RequestMapping("/mi-entidad")
public class MiEntidadController {

    @GetMapping
    public String listar(Model model) {
        // 1. Obtener datos
        List<MiEntidad> entidades = miServicio.findAll();
        model.addAttribute("items", entidades);
        
        // 2. Configurar columnas
        List<Map<String, Object>> columnas = Arrays.asList(
            Map.of("label", "ID", "getter", "getId", "type", "text"),
            Map.of("label", "Nombre", "getter", "getNombre", "type", "strong"),
            Map.of("label", "Email", "getter", "getEmail", "type", "text"),
            Map.of("label", "Estado", "getter", "isActivo", "type", "conditional-badge"),
            Map.of("label", "Precio", "getter", "getPrecio", "type", "price")
        );
        model.addAttribute("columns", columnas);
        
        return "mi-entidad/listar";
    }
}
```

### Tipos de Columnas Disponibles

| Tipo | Descripción | Propiedades Adicionales |
|------|-------------|-------------------------|
| `text` | Texto normal | - |
| `strong` | Texto en negrita | - |
| `code` | Texto con formato de código | - |
| `badge` | Badge con clase personalizada | `badgeClass` |
| `conditional-badge` | Badge verde/rojo según valor > 0 | - |
| `price` | Formato de precio con $ | - |

### Ejemplo de Columna con Badge Personalizado

```java
Map.of(
    "label", "Estado", 
    "getter", "getEstado", 
    "type", "badge", 
    "badgeClass", "bg-success"
)
```

## Configuración de JavaScript

### 1. Incluir el archivo JavaScript en tu plantilla

```html
<script th:src="@{/js/crud-components.js}"></script>
<script>
    // Inicializar con la URL base de tu API
    initCrudComponents('/api/mi-entidad');
</script>
```

### 2. Endpoints requeridos en tu controlador

Tu controlador debe implementar estos endpoints para que el JavaScript funcione:

```java
// Formulario para nuevo registro
@GetMapping("/form/new")
public String nuevoForm(Model model) {
    return "mi-entidad/form";
}

// Formulario para editar/ver
@GetMapping("/{id}/{mode}")
public String editarVer(@PathVariable Long id, @PathVariable String mode, Model model) {
    MiEntidad entidad = miServicio.findById(id);
    model.addAttribute("entity", entidad);
    model.addAttribute("mode", mode); // 'edit' o 'view'
    return "mi-entidad/form";
}

// Guardar nuevo registro
@PostMapping("/save")
@ResponseBody
public ResponseEntity<?> guardar(@ModelAttribute MiEntidad entidad) {
    try {
        miServicio.save(entidad);
        return ResponseEntity.ok(Map.of("success", true, "message", "Guardado correctamente"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}

// Actualizar registro
@PutMapping("/{id}/update")
@ResponseBody
public ResponseEntity<?> actualizar(@PathVariable Long id, @ModelAttribute MiEntidad entidad) {
    try {
        entidad.setId(id);
        miServicio.save(entidad);
        return ResponseEntity.ok(Map.of("success", true, "message", "Actualizado correctamente"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}

// Eliminar registro
@DeleteMapping("/{id}/delete")
@ResponseBody
public ResponseEntity<?> eliminar(@PathVariable Long id) {
    try {
        miServicio.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Eliminado correctamente"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}

// Detalles para confirmación de eliminación
@GetMapping("/{id}/details")
public String detalles(@PathVariable Long id, Model model) {
    MiEntidad entidad = miServicio.findById(id);
    model.addAttribute("entity", entidad);
    return "mi-entidad/detalles";
}
```

## Estructura del Formulario

Tu formulario debe seguir esta estructura básica:

```html
<form id="crudForm" th:object="${entity}">
    <input type="hidden" th:if="${entity?.id}" th:name="id" th:value="${entity?.id}">
    
    <!-- Tus campos aquí -->
    <div class="mb-3">
        <label for="nombre" class="form-label">Nombre</label>
        <input type="text" 
               class="form-control" 
               id="nombre"
               name="nombre"
               th:value="${entity?.nombre}"
               th:readonly="${mode == 'view'}"
               required>
    </div>
    
    <!-- Mensajes de error (obligatorio) -->
    <div id="errorMessages" class="alert alert-danger" style="display: none;">
        <ul id="errorList"></ul>
    </div>

    <!-- Mensajes de éxito (obligatorio) -->
    <div id="successMessage" class="alert alert-success" style="display: none;">
        <i class="bi bi-check-circle"></i> <span id="successText"></span>
    </div>
</form>
```

## Probar los Fragmentos

1. **Ejecutar la aplicación**:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Visitar la página de prueba**:
   ```
   http://localhost:8080/prueba/fragments
   ```

Esta página muestra ejemplos completos de cómo usar todos los fragmentos.

## Personalización

### Estilos CSS Adicionales

Los fragmentos usan clases de Bootstrap 5, pero puedes añadir estilos personalizados en tu `main.css`:

```css
/* Estilos para las tablas CRUD */
.table-responsive {
    border-radius: 0.375rem;
}

.btn-group-sm > .btn {
    padding: 0.25rem 0.5rem;
}

/* Estilos para modales */
.modal-header.bg-primary {
    border-bottom: 1px solid #0d6efd;
}
```

### Funciones JavaScript Personalizadas

Puedes sobrescribir las funciones del archivo `crud-components.js` para personalizar el comportamiento:

```javascript
// Función personalizada para mostrar detalles
function showDetails(item) {
    // Tu lógica personalizada aquí
    console.log('Mostrando detalles personalizados:', item);
}

// Función para acción personalizada
function customAction(entityId) {
    // Tu lógica personalizada aquí
    alert('Acción personalizada para ID: ' + entityId);
}
```

## Notas Importantes

1. **Nombres de métodos getter**: Los nombres en la configuración de columnas deben coincidir con los getters de tu entidad (sin el prefijo "get").

2. **IDs obligatorios**: Tus formularios deben tener `id="crudForm"` y los elementos de mensajes deben tener los IDs especificados.

3. **Endpoints REST**: Para el funcionamiento completo, necesitas implementar todos los endpoints mencionados en tu controlador.

4. **Manejo de errores**: El JavaScript espera respuestas JSON con formato `{success: boolean, message: string}`.

5. **Bootstrap**: Los fragmentos requieren Bootstrap 5 y Bootstrap Icons.