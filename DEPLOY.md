# Guía de Despliegue y Variables de Entorno

Para ejecutar el proyecto de forma segura, el sistema ahora utiliza variables de entorno en lugar de credenciales expuestas en el código fuente.

## 1. Generar un Secreto JWT Seguro

El secreto JWT debe tener al menos 32 bytes de longitud. Puedes generar uno seguro ejecutando en una terminal:

\\\ash
openssl rand -hex 32
\\\

Copia la salida y utilízala como valor para la variable JWT_SECRET.

## 2. Configurar Variables de Entorno

Debes definir las siguientes variables de entorno antes de ejecutar el proyecto. Revisa el archivo .env.example en la carpeta \Backend/backend-sgac\ para ver un ejemplo.

### En PowerShell (Windows)
\\\powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/SGAC-FINAL"
$env:DB_USERNAME="app_user_default"
$env:DB_PASSWORD="tu_contrasena_db"
$env:FLYWAY_USER="administrador_consultas"
$env:FLYWAY_PASSWORD="tu_contrasena_flyway"

$env:JWT_SECRET="tu_secreto_generado"
$env:JWT_EXPIRATION="36000000"
$env:JWT_PRE_AUTH_EXPIRATION="300000"

$env:CLOUDINARY_CLOUD_NAME="dyqeqscmu"
$env:CLOUDINARY_API_KEY="tu_api_key"
$env:CLOUDINARY_API_SECRET="tu_api_secret"

$env:MAIL_USERNAME="equipoti28@gmail.com"
$env:MAIL_PASSWORD="tu_contrasena_de_aplicacion"
\\\

### En Bash (Linux / macOS)
\\\ash
export DB_URL=jdbc:postgresql://localhost:5432/SGAC-FINAL
export DB_USERNAME=app_user_default
export DB_PASSWORD=tu_contrasena_db
export FLYWAY_USER=administrador_consultas
export FLYWAY_PASSWORD=tu_contrasena_flyway

export JWT_SECRET=tu_secreto_generado
export JWT_EXPIRATION=36000000
export JWT_PRE_AUTH_EXPIRATION=300000

export CLOUDINARY_CLOUD_NAME=dyqeqscmu
export CLOUDINARY_API_KEY=tu_api_key
export CLOUDINARY_API_SECRET=tu_api_secret

export MAIL_USERNAME=equipoti28@gmail.com
export MAIL_PASSWORD=tu_contrasena_de_aplicacion
\\\

### En IntelliJ IDEA (Run Configuration)
1. Ve a **Run > Edit Configurations...**
2. Selecciona la configuración de tu aplicación Spring Boot.
3. En la sección **Environment variables**, haz clic en el icono de carpeta y añade las variables una por una (o copia y pega la lista de nombres y valores separados por punto y coma).

