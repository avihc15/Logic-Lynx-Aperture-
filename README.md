# Logic Lynx — Aperture (Full-Stack: Android + Web Tomcat + Spring Boot + MySQL en Azure)

## 1. Base de datos MySQL (`database/APERTURE_ENTREGA_COMPLETA.sql`)
1. En el servidor de Azure (MySQL 8.0+), ejecutar el script completo:
   ```bash
   mysql -u root -p < database/APERTURE_ENTREGA_COMPLETA.sql
   ```
   Esto crea la base de datos `aperture_entrega` con sus **27 tablas**, **3 vistas**, **10 triggers** y datos iniciales.

## 2. Backend REST en Spring Boot (`backend-web/`)
1. Configurar las variables de entorno de MySQL en el servidor de Azure (no guardar contraseñas en el código):
   ```bash
   export DB_HOST=localhost
   export DB_PORT=3306
   export DB_NAME=aperture_entrega
   export DB_USER=root
   export DB_PASSWORD=TU_PASSWORD_MYSQL
   ```
2. Compilar y arrancar Spring Boot en el puerto `8080`:
   ```bash
   cd backend-web
   ./mvnw clean package -DskipTests
   java -jar target/aperture-0.0.1-SNAPSHOT.jar
   ```
3. Endpoints REST disponibles (`/api/...`):
   - `GET / POST / PUT / DELETE` `/api/peliculas`
   - `GET / POST / PUT / DELETE` `/api/salas`
   - `GET / POST / PUT / DELETE` `/api/funciones`
   - `GET / POST / PUT / DELETE` `/api/promociones`
   - `GET / POST / PUT / DELETE` `/api/productos`
   - `GET` `/api/ventas`, `POST /api/ventas/taquilla`, `POST /api/ventas/dulceria`

## 3. Frontend Web en Apache Tomcat (`web/`)
1. Copiar el contenido de `web/` (`index.html`, `styles.css`, `app.js`) dentro de `webapps/aperture/` en tu servidor Apache Tomcat.
2. En la sección **Admin Catálogos (ABC)** de `index.html`, colocar la IP pública del servidor Azure (`http://TU_IP_AZURE:8080/api`) y hacer clic en **Conectar**.

## 4. Aplicación Móvil Android (`android/`)
1. Abrir la carpeta `android/` directamente en Android Studio.
2. En `app/src/main/java/com/example/aperture/network/ApiSyncClient.java`, cambiar `BASE_URL` por la IP pública del servidor Azure (`http://TU_IP_AZURE:8080/api`).
3. Cada venta en **Taquilla** o **Dulcería** y cada alta en **Funciones** o **Promociones** se guarda localmente en SQLite y se sincroniza automáticamente por HTTP POST con MySQL en Azure.
   
