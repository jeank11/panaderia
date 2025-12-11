# Ventas App - Java + Spring Boot + MySQL

Proyecto ejemplo minimal para registrar usuarios, productos y ventas.
Funcionalidades:
- Registro y login (sesión simple)
- CRUD: crear productos
- Registrar ventas asignadas a usuarios
- Ver historial de compras por usuario

## Requisitos
- Java 17+
- Maven
- MySQL (creá la base `ventasdb` o usá el script `db/create_db.sql`)

## Cómo correr
1. Editar `src/main/resources/application.properties` y colocar tu usuario/clave de MySQL.
2. Ejecutar:
   ```
   mvn spring-boot:run
   ```
3. Abrir http://localhost:8080

## Notas
- La autenticación es simple (no usa Spring Security) para facilitar la prueba rápida.
- Contraseñas se guardan en texto plano en esta demo: **no** usar así en producción.
- Mejoras sugeridas: añadir Spring Security, validación, manejo de stock, páginas de administración, tests, y hashing de contraseñas.
