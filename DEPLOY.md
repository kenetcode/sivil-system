# Comandos de Deploy y Mantenimiento

## Testing Local con Perfil de Producción
```bash
# Compilar
./mvnw clean package -DskipTests

# Ejecutar con perfil prod (usando DB local)
export DATABASE_URL=jdbc:postgresql://10.255.255.254:5432/sivil_db
java -Dspring.profiles.active=prod -jar target/Sistema-digital-de-venta-de-libros-0.0.1-SNAPSHOT.jar
```

## Testing Docker Local
```bash
# Construir imagen
docker build -t sivil-system .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e DATABASE_URL="jdbc:postgresql://10.255.255.254:5432/sivil_db" \
  -e SPRING_PROFILES_ACTIVE=prod \
  sivil-system
```

## Comandos Git para Deploy
```bash
# Agregar cambios
git add .

# Commit
git commit -m "Deploy configuration for Render"

# Push (automáticamente redespliega en Render)
git push origin main
```

## Troubleshooting
- **Logs**: Dashboard de Render → tu service → Logs
- **Variables**: Dashboard → Settings → Environment Variables
- **DB Connection**: Verificar DATABASE_URL en variables