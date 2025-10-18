package com.sivil.systeam.controller;

import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.enums.TipoUsuario;
import com.sivil.systeam.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(value = "error", required = false) String error, Model model) {
        if ("credenciales".equals(error)) {
            model.addAttribute("error", "Email o contraseña incorrectos");
        } else if ("sistema".equals(error)) {
            model.addAttribute("error", "Error en el sistema");
        }
        return "login/login";
    }
    
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        // Solo permitir comprador y vendedor en el registro público
        List<TipoUsuario> tiposPermitidos = Arrays.asList(TipoUsuario.comprador, TipoUsuario.vendedor);
        model.addAttribute("tiposUsuario", tiposPermitidos);
        return "usuario/registro";
    }
    
    @GetMapping("/validar-nombre-usuario")
    @ResponseBody
    public boolean validarNombreUsuario(@RequestParam("nombre_usuario") String nombre_usuario) {
        return !usuarioService.existeNombreUsuario(nombre_usuario);
    }
    
    @GetMapping("/validar-email")
    @ResponseBody
    public boolean validarEmail(@RequestParam("email") String email) {
        return !usuarioService.existeEmail(email);
    }
    
    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario, 
                                   RedirectAttributes redirectAttributes) {
        try {
            usuarioService.crearUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario registrado exitosamente");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro";
        }
    }
    
    @GetMapping("/usuarios")
    public String mostrarUsuarios(
            @RequestParam(value = "criterio", required = false) String criterio,
            @RequestParam(value = "tipoBusqueda", required = false) String tipoBusqueda,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        List<Usuario> usuarios;
        
        // Si hay criterio de búsqueda, buscar; de lo contrario, mostrar todos
        if (criterio != null && !criterio.trim().isEmpty() && tipoBusqueda != null && !tipoBusqueda.trim().isEmpty()) {
            try {
                usuarios = usuarioService.buscarUsuarios(criterio, tipoBusqueda);
                model.addAttribute("criterio", criterio);
                model.addAttribute("tipoBusqueda", tipoBusqueda);
                
                // Si no hay resultados, mostrar mensaje
                if (usuarios.isEmpty()) {
                    model.addAttribute("sinResultados", true);
                    model.addAttribute("mensajeBusqueda", 
                        String.format("No se encontraron usuarios que coincidan con '%s' en %s", 
                        criterio, 
                        tipoBusqueda.equals("todos") ? "todos los campos" : tipoBusqueda));
                }
            } catch (RuntimeException e) {
                // En caso de error en la búsqueda, mostrar todos y mensaje de error
                usuarios = usuarioService.obtenerTodosLosUsuarios();
                model.addAttribute("error", e.getMessage());
            }
        } else {
            usuarios = usuarioService.obtenerTodosLosUsuarios();
        }
        
        model.addAttribute("usuarios", usuarios);
        
        // Configurar columnas para la tabla
        List<Object> columnas = Arrays.asList(
            Map.of("label", "ID", "getter", "id_usuario", "type", "text"),
            Map.of("label", "Usuario", "getter", "nombre_usuario", "type", "strong"),
            Map.of("label", "Email", "getter", "email", "type", "text"),
            Map.of("label", "Nombre Completo", "getter", "nombre_completo", "type", "text"),
            Map.of("label", "Tipo", "getter", "tipo_usuario", "type", "badge", "badgeClass", "bg-primary"),
            Map.of("label", "Estado", "getter", "estado", "type", "conditional-badge"),
            Map.of("label", "Teléfono", "getter", "telefono", "type", "text")
        );
        model.addAttribute("columnas", columnas);
        
        return "usuario/administrar-usuarios";
    }
    
    @GetMapping("/usuarios/nuevo")
    public String mostrarRegistroInterno(Model model) {
        model.addAttribute("usuario", new Usuario());
        // Para registro interno, permitir todos los tipos
        model.addAttribute("tiposUsuario", TipoUsuario.values());
        return "usuario/registro-interno-sistema";
    }
    
    @PostMapping("/usuarios/nuevo")
    public String procesarRegistroInterno(@ModelAttribute Usuario usuario, 
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        try {
            usuarioService.crearUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado exitosamente");
            return "redirect:/usuarios";
        } catch (RuntimeException e) {
            // En lugar de redirigir, volver a mostrar el formulario con los datos
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuario); // Preservar los datos
            model.addAttribute("tiposUsuario", TipoUsuario.values());
            return "usuario/registro-interno-sistema"; // Volver al formulario sin redirect
        }
    }
    
    @GetMapping("/usuarios/{id}/editar")
    public String mostrarEditarUsuario(@PathVariable("id") Integer id, Model model) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        if (usuario == null) {
            model.addAttribute("error", "Usuario no encontrado");
            return "redirect:/usuarios";
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("tiposUsuario", TipoUsuario.values());
        return "usuario/editar-usuario";
    }
    
    @PostMapping("/usuarios/{id}/editar")
    public String procesarEditarUsuario(@PathVariable("id") Integer id,
                                       @ModelAttribute Usuario usuario,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        try {
            usuario.setId_usuario(id); // Asegurar que el ID sea correcto
            usuarioService.actualizarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
            return "redirect:/usuarios";
        } catch (RuntimeException e) {
            // En caso de error, volver a mostrar el formulario con los datos
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("tiposUsuario", TipoUsuario.values());
            return "usuario/editar-usuario";
        }
    }
}