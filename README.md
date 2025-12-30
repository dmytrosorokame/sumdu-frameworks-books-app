# Books Catalog Application

A Spring Boot web application for managing a books catalog with user comments, authentication, and email notifications.

**Live Demo:** [https://mpf-labs-web.onrender.com](https://mpf-labs-web.onrender.com)

## Project Structure

The application follows a layered architecture with clear separation of concerns:

```
src/main/java/sumdu/edu/ua/
├── core/                    # Business logic layer
│   ├── domain/              # Domain models (Book, Comment, Page, PageRequest)
│   ├── exception/           # Custom exceptions (BookNotFoundException, etc.)
│   ├── port/                # Repository interfaces (ports for hexagonal architecture)
│   └── service/             # Business services (BookService, CommentService, etc.)
├── persistence/             # Data access layer
│   ├── entity/              # JPA entities (BookEntity, CommentEntity, UserEntity)
│   ├── jpa/                 # JPA repository implementations
│   └── repository/          # Spring Data JPA repositories
├── web/                     # Presentation layer
│   ├── aop/                 # Aspect-oriented programming (logging aspects)
│   ├── controller/          # MVC and REST controllers
│   ├── http/                # Request/Response DTOs
│   └── service/             # Web-specific services (EmailTemplateProcessor)
└── config/                  # Spring configuration classes
```

### Layer Responsibilities

| Layer           | Package       | Responsibility                                                      |
| --------------- | ------------- | ------------------------------------------------------------------- |
| **Core**        | `core`        | Domain models, business logic, validation rules, custom exceptions  |
| **Persistence** | `persistence` | Database entities, JPA repositories, data access                    |
| **Web**         | `web`         | HTTP controllers, REST API, Thymeleaf templates, exception handlers |
| **Config**      | `config`      | Spring Security, email, web configuration, data initialization      |

## Features

- **Book Management**: CRUD operations for books (admin only)
- **Comments**: Users can add comments to books, admins can delete comments within 24 hours
- **Authentication**: User registration with email confirmation, login/logout
- **Authorization**: Role-based access control (USER, ADMIN roles)
- **Email Notifications**: Registration confirmation, new book notifications
- **Internationalization**: Ukrainian and English language support
- **REST API**: JSON API endpoints for books and comments

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **View Engine**: Thymeleaf
- **Security**: Spring Security 6
- **Database**: PostgreSQL (production), H2 (development)
- **ORM**: Spring Data JPA / Hibernate
- **Migrations**: Flyway
- **Email**: Resend API with FreeMarker templates
- **Monitoring**: Spring Boot Actuator
- **Build**: Maven
- **Container**: Docker

## Configuration

### Application Properties

The application uses environment variables for production configuration:

```properties
# Database Configuration
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:h2:file:./data/guest}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:sa}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate    # Schema validated by Flyway
spring.jpa.show-sql=false                 # Disabled in production

# Flyway Migrations
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# Server
server.port=${PORT:8080}
```

### Environment Variables

| Variable                     | Required | Description                         | Default                 |
| ---------------------------- | -------- | ----------------------------------- | ----------------------- |
| `SPRING_DATASOURCE_URL`      | Yes      | PostgreSQL JDBC URL                 | H2 file database        |
| `SPRING_DATASOURCE_USERNAME` | Yes      | Database username                   | `sa`                    |
| `SPRING_DATASOURCE_PASSWORD` | Yes      | Database password                   | empty                   |
| `APP_ADMIN_EMAIL`            | No       | Admin email (login + notifications) | `admin@example.com`     |
| `APP_ADMIN_PASSWORD`         | No       | Admin password                      | `admin`                 |
| `APP_BASE_URL`               | No       | Application public URL              | `http://localhost:8080` |
| `RESEND_API_KEY`             | No       | Resend.com API key for emails       | disabled                |
| `PORT`                       | No       | Server port (set by Render)         | `8080`                  |

## Docker

### Dockerfile

The application uses a multi-stage Docker build for optimal image size:

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
RUN apk add --no-cache maven
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/spring-boot-books-app-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

### Build Process

1. **Dependency caching**: `pom.xml` is copied first to leverage Docker layer caching
2. **Compile & package**: Maven builds the application JAR
3. **Runtime image**: Only the JAR file is copied to the final lightweight image

### Local Docker Build

```bash
# Build the image
docker build -t mpf-labs-web .

# Run with environment variables
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/db \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=pass \
  mpf-labs-web
```

## Deployment on Render

### PostgreSQL Service

1. Create a new **PostgreSQL** service on Render
2. Note the connection details:
   - **Internal Database URL**: Used by the web service (faster, within Render network)
   - **External Database URL**: For external tools (DBeaver, psql)

### Web Service

1. Create a new **Web Service** on Render
2. Connect your GitHub repository
3. Select **Docker** as the environment
4. Configure environment variables:

| Variable                     | Value                                               |
| ---------------------------- | --------------------------------------------------- |
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://[internal-host]:5432/[database]` |
| `SPRING_DATASOURCE_USERNAME` | From PostgreSQL service                             |
| `SPRING_DATASOURCE_PASSWORD` | From PostgreSQL service                             |
| `APP_ADMIN_EMAIL`            | Your admin email                                    |
| `APP_ADMIN_PASSWORD`         | Secure admin password                               |
| `APP_BASE_URL`               | `https://your-app.onrender.com`                     |

### Health Check

The application exposes health endpoints via Spring Boot Actuator:

```bash
curl https://mpf-labs-web.onrender.com/actuator/health
# Response: {"status":"UP"}
```

## Database Migrations

Flyway manages database schema migrations located in `src/main/resources/db/migration/`:

| Migration                  | Description                                  |
| -------------------------- | -------------------------------------------- |
| `V1__init.sql`             | Creates tables: `books`, `users`, `comments` |
| `V2__initial_data.sql`     | Inserts sample books data                    |
| `V3__test_old_comment.sql` | Test data for comment deletion feature       |

## CI/CD

GitHub Actions workflow (`.github/workflows/ci.yml`):

```yaml
- Run tests: mvn clean test
- Build JAR: mvn clean package -DskipTests
- Upload artifact: spring-boot-books-app-*.jar
```

## API Endpoints

### Books API

| Method | Endpoint          | Description    | Access      |
| ------ | ----------------- | -------------- | ----------- |
| GET    | `/api/books`      | List all books | USER, ADMIN |
| GET    | `/api/books/{id}` | Get book by ID | USER, ADMIN |
| POST   | `/api/books`      | Create book    | ADMIN       |
| PUT    | `/api/books/{id}` | Update book    | ADMIN       |
| DELETE | `/api/books/{id}` | Delete book    | ADMIN       |

### Comments API

| Method | Endpoint    | Description                 | Access        |
| ------ | ----------- | --------------------------- | ------------- |
| POST   | `/comments` | Add comment                 | Authenticated |
| DELETE | `/comments` | Delete comment (within 24h) | ADMIN         |

## Local Development

```bash
# Clone the repository
git clone https://github.com/your-username/sumdu-frameworks-books-app.git
cd sumdu-frameworks-books-app

# Run with Maven (uses H2 database)
mvn spring-boot:run

# Access the application
open http://localhost:8080
```

Default admin credentials (local development):

- Email: `admin@example.com`
- Password: `admin`

## Conclusions

### Deployment Challenges

The most challenging aspects of deployment were:

- Configuring the correct PostgreSQL JDBC URL format for Render's internal network
- Understanding the relationship between Flyway migrations and Hibernate's `ddl-auto=validate`
- Setting up proper security rules for public endpoints (actuator health)

### Why Docker for Java Applications?

- **Consistency**: Same environment across development, testing, and production
- **Isolation**: Application dependencies are bundled within the container
- **Portability**: Runs on any platform that supports Docker
- **Scalability**: Easy to scale horizontally with container orchestration

### Managed PostgreSQL Advantages

- **Zero maintenance**: Automatic backups, updates, and monitoring
- **High availability**: Built-in redundancy and failover
- **Security**: Encrypted connections, managed credentials
- **Scalability**: Easy to upgrade resources as needed

### Role of Environment Variables

- **Security**: Sensitive data (passwords, API keys) never stored in code
- **Flexibility**: Same codebase works in different environments
- **Reproducibility**: Configuration is explicit and version-controlled
- **12-Factor App**: Follows modern cloud-native application principles

## License

This project is part of the SumDU Frameworks course laboratory work.
