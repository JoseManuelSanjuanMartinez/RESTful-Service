# RESTful Banking Service (BanksProject)

Servicio web RESTful desarrollado en **Java** con **Spring Boot** para la gestión de un catálogo de bancos, sus respectivas hipotecas y documentos adjuntos (como archivos PDF con condiciones contractuales).

---

## 🛠️ Tecnologías Utilizadas

- **Java 17**
- **Spring Boot** (Spring WebMVC, Spring Data JDBC, Spring Security)
- **Base de datos:** H2 Database (en memoria / embebida)
- **Seguridad:** Autenticación mediante Clave de API (`ApiKey` header)
- **Gestión de dependencias y construcción:** Maven (`mvnw` / `mvnw.cmd`)

---

## 📂 Estructura del Proyecto

```text
src/
├── main/
│   ├── java/org/example/banksproject/
│   │   ├── controller/      # Controladores REST (Bancos, Hipotecas, Adjuntos)
│   │   ├── models/          # Entidades de dominio (Bank, Mortgage, ApiUser, etc.)
│   │   ├── repositories/    # Repositorios de Spring Data JDBC
│   │   ├── security/        # Configuración y filtros de autenticación API Key
│   │   └── service/         # Servicios de negocio (ej. FileStorageService)
│   └── resources/
│       ├── application.properties # Configuración de la aplicación y H2
│       ├── OpenAPIDocs.yml        # Documentación OpenAPI / Swagger de la API
│       └── schema.sql             # Esquema inicial de la base de datos
└── test/
    └── java/org/example/banksproject/ # Pruebas unitarias e integración
```

---

## 🚀 Puesta en Marcha

### 1. Requisitos Previos
- Tener instalado **Java JDK 17** (o superior).

### 2. Ejecutar la aplicación con Maven Wrapper
En la raíz del proyecto, ejecuta el siguiente comando:

- **En Linux / macOS:**
  ```bash
  ./mvnw spring-boot:run
  ```
- **En Windows (PowerShell / CMD):  **
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```

La aplicación se iniciará por defecto en el puerto `8080`.

---

## 🔒 Autenticación

Los endpoints protegidos de la API requieren el envío de una cabecera HTTP de autenticación por API Key:

```http
ApiKey: tu-api-key
```

---

## 📖 Documentación de la API

La especificación completa de los endpoints (Operaciones CRUD de Bancos, Hipotecas y Adjuntos) se encuentra documentada bajo el estándar **OpenAPI 3.0** en el archivo:
`src/main/resources/OpenAPIDocs.yml`

---

## 🧪 Pruebas

Para ejecutar la suite de pruebas automatizadas:

- **Linux / macOS:**
  ```bash
  ./mvnw test
  ```
- **Windows:**
  ```powershell
  .\mvnw.cmd test
  ```
