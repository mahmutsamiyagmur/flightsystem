# Flight System API

A comprehensive flight management system API for handling transportation routes, locations, and efficient route search with Redis caching.

## 🌟 Features

- **Route Management**: Find optimal routes between locations with support for direct flights and transfers
- **Redis Caching**: Improved performance with Redis-based caching for frequently requested routes
- **JWT Authentication**: Secure API access with token-based authentication
- **Role-Based Access Control**: Different permission levels for administrators and agencies
- **Global Exception Handling**: Consistent error responses across the application
- **Docker Support**: Easy deployment with Docker and Docker Compose

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven
- Docker and Docker Compose (for containerized deployment)

### Local Development Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/flightsystem.git
cd flightsystem
```

2. Create a PostgreSQL database:
```bash
# Using local PostgreSQL installation
createdb flightsystemdb
```

3. Start Redis locally:
```bash
# Install Redis if not already installed
# macOS: brew install redis
# Ubuntu: sudo apt install redis-server

# Start Redis
redis-server
```

4. Run the application:
```bash
mvn spring-boot:run
```

### Docker Setup

1. Build and run with Docker Compose:
```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Redis cache
- Flight System API

### Environment Configuration

The application supports multiple environment profiles:

- **dev**: Local development (default)
- **test**: Testing environment with H2 in-memory database
- **docker**: Containerized environment
- **prod**: Production environment

To switch profiles:
```bash
# Command line
java -jar flightsystem.jar --spring.profiles.active=prod

# Environment variable
export SPRING_PROFILES_ACTIVE=prod
java -jar flightsystem.jar
```

## 🔒 Authentication

The API uses JWT-based authentication:

1. Obtain JWT token:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

2. Use token in subsequent requests:
```bash
curl -X GET http://localhost:8080/api/routes \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### User Roles

- **ADMIN**: Full access to all endpoints, including user, location, and transportation management
- **AGENCY**: Access to routes and read-only access to locations

## 📝 API Documentation

When the application is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

### Key Endpoints

| Endpoint | Method | Description | Access |
|----------|--------|-------------|--------|
| `/auth/login` | POST | Get JWT token | Public |
| `/api/routes` | GET | Find routes between locations | ADMIN, AGENCY |
| `/api/locations` | GET | Get all locations | ADMIN, AGENCY |
| `/api/locations` | POST | Create new location | ADMIN |
| `/api/transportations` | GET | Get all transportation | ADMIN |
| `/api/transportations` | POST | Create new transportation | ADMIN |
| `/api/users` | GET | Get all users | ADMIN |

## 💾 Data Model

The system includes the following main entities:

- **User**: System users with roles (ADMIN, AGENCY)
- **Location**: Airports or terminals with location codes
- **Transportation**: Flights or other transport methods between locations
- **Route**: Combinations of transportation to travel from origin to destination

## 🔄 Caching System

The application utilizes Redis for caching route search results:

- Routes are cached by origin, destination, and date
- Cache is automatically invalidated when transportation data changes
- Configurable time-to-live for cache entries

## 🛠️ Technologies

- **Spring Boot**: Application framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access
- **Spring Data Redis**: Redis caching integration
- **PostgreSQL**: Primary database
- **Redis**: Caching layer
- **JWT**: Token-based authentication
- **Docker**: Containerization
- **Hibernate**: ORM framework
- **Maven**: Dependency management

## ⚙️ Configuration

Environment-specific configuration is located in:

- `application-dev.properties`: Development settings
- `application-test.properties`: Test environment settings
- `application-docker.properties`: Docker environment settings
- `application-prod.properties`: Production environment settings
- `.env`: Environment variables for Docker deployment

## 🧪 Testing

Run tests with:
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RouteServiceTest
```

## 📋 License

[MIT License](LICENSE)

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
