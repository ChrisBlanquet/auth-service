# Auth Service

Microservicio central de autenticación, autorización y emisión de credenciales criptográficas para arquitectura distribuida de microservicios. Implementado con **Java 17**, **Spring Boot 3** y **Spring Security**.

---

## Características Técnicas & Arquitectura

* **Criptografía Asimétrica (RSA):** Generación y firma de tokens JWT utilizando par de claves pública/privada. La clave privada permanece resguardada en el servidor de autenticación para la firma, mientras que la clave pública es distribuida a los microservicios satélites para validación descentralizada.
* **Ciclo de Vida de Tokens & Cookies HttpOnly:**
  * **Access Token:** Corta duración en memoria/headers para autorización de peticiones.
  * **Refresh Token:** Transmitido y resguardado mediante cookies con banderas `HttpOnly`, `SameSite` y control estricto de rutas para mitigar ataques XSS y CSRF.
* **Control de Sesión Única & Revocación:** Mecanismo de lista negra activa en base de datos para invalidación inmediata de tokens ante cierre de sesión, rotación de credenciales o detección de sesiones concurrentes no autorizadas.
* **Tolerancia a Fallos & Resiliencia:** Integración con **Resilience4j** y Circuit Breakers en clientes **OpenFeign** para gestionar reintentos automáticos y caídas de dependencias externas.
* **Gestión Segura de Secretos:** Integración con **Azure Key Vault** para la inyección dinámica de secretos de base de datos, contraseñas y llaves criptográficas sin almacenar datos sensibles en código fuente.
* **Descubrimiento de Servicios:** Registro dinámico y balanceo de carga mediante **Netflix Eureka Client**.

---

## Stack Tecnológico

* **Lenguaje:** Java 17+
* **Framework:** Spring Boot 3.x (Spring Security, Spring Cloud OpenFeign, Spring Data JPA)
* **Seguridad:** JWT (JSON Web Tokens), Algoritmo RSA (2048-bit), BCrypt
* **Cloud & DevOps:** Docker, Azure Key Vault, Netflix Eureka
* **Persistencia:** MySQL / PostgreSQL / HikariCP
* **Resiliencia:** Resilience4j

---

## ⚙️ Variables de Configuración

El servicio consume sus secretos mediante variables de entorno o proveedor de secretos (Azure Key Vault):

| Variable / Propiedad | Descripción |
| :--- | :--- |
| `AZURE_KEYVAULT_ENDPOINT` | Endpoint del Azure Key Vault para resolución de secretos. |
| `db-url` | URL de conexión JDBC a la base de datos relacional. |
| `db-user` / `db-password` | Credenciales de acceso para HikariCP. |
| `jwt-rsa-private-key` | Clave privada RSA en formato PEM/Base64 para la firma de JWTs. |
| `jwt-rsa-public-key` | Clave pública RSA correspondiente. |
| `azure-public-ip` | IP o host del servidor Eureka Service Discovery. |

---

## Ejecución Local

### Prerrequisitos
* Java 17 o superior
* Maven 3.8+
* MySQL en ejecución (o contenedor Docker)
