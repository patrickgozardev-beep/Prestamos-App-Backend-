# PrestamosApp

Aplicación backend para gestión de préstamos, cronogramas de pago y registro de pagos parciales o completos.

---

## 🛠 Requisitos

- **Java:** 21  
- **Spring Boot:** 3.5.10  
- **Lombok:** 1.18.x (via Maven)  
- **Base de datos:** MySQL  
- **Maven:** 3.x  

> Se recomienda un IDE con soporte para Lombok (IntelliJ IDEA, Eclipse o VS Code).

---

## 📁 Estructura del proyecto

```plaintext
prestamosapp/
├─ src/
│  ├─ main/
│  │  ├─ java/com/prestamos/prestamosapp/
│  │  │  ├─ config/
│  │  │  ├─ security/
│  │  │  ├─ controller/
│  │  │  ├─ model/
│  │  │  ├─ dto/
│  │  │  ├─ repository/
│  │  │  └─ service/
│  │  └─ resources/
│  │     └─ application.properties
└─ pom.xml
```
---

## ⚙ Configuración de la base de datos

En `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/prestamos_app?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=mysql
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```


## Notas importantes:

ddl-auto=validate evita que Hibernate modifique la base de datos automáticamente.
Cambia username y password según tu configuración de MySQL.

## 🚀 Cómo ejecutar el proyecto

Clonar el repositorio:
git clone https://github.com/tu-usuario/prestamosapp.git
cd prestamosapp

Construir el proyecto con Maven:
mvn clean install

Ejecutar la aplicación:
mvn spring-boot:run

La API estará disponible en:
http://localhost:8080

## 📦 Dependencias principales

Spring Boot Starter Web: para APIs REST
Spring Boot Starter Data JPA: para persistencia con Hibernate
Spring Boot Starter Validation: para validaciones de DTOs
MySQL Connector/J: driver JDBC para MySQL
Lombok: para generar getters, setters, constructores y builders automáticamente

## 📝 Funcionalidades

Crear préstamos diarios o semanales
Generar automáticamente cronogramas de pago
Registrar pagos completos o parciales, incluyendo excedentes
Actualizar estado del cronograma: PENDIENTE, PARCIAL, PAGADO
Reprogramar préstamos con intereses adicionales

## 🤝 Contribuciones

Hacer un fork del proyecto
Crear una rama nueva:
git checkout -b feature/nombre-feature
Hacer commit de los cambios:
git commit -m "Agrega nueva funcionalidad"

Subir la rama:
git push origin feature/nombre-feature
Abrir un Pull Request

## 📌 Notas

Lombok requiere plugin en tu IDE para funcionar correctamente
Cronogramas no cuentan domingos como fecha de pago
Se recomienda tener MySQL corriendo antes de iniciar la aplicación

---
