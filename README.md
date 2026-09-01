# Booking Engine Backend

A high-quality, scalable booking engine application built with Spring Boot, designed to handle complex booking operations with microservices architecture, comprehensive API endpoints, and cloud-native deployment.

## 🏗️ Architecture Overview

```
┌─────────────┐
│   Frontend  │ (Angular/React/Vue)
└──────┬──────┘
       │ HTTP/REST
┌──────▼──────────────────────────┐
│   API Gateway / Load Balancer   │
└──────┬──────────────────────────┘
       │
┌──────▼──────────────────────────────┐
│   Spring Boot Microservices         │
│  ├─ User Service                    │
│  ├─ Booking Service                 │
│  ├─ Payment Service                 │
│  └─ Notification Service            │
└──────┬──────────────────────────────┘
       │
   ┌───┴──────┬────────┬────────┐
   │          │        │        │
┌──▼──┐  ┌───▼──┐ ┌──▼──┐  ┌──▼────┐
│MySQL│  │Mongo │ │Redis│  │DynamoDB
└─────┘  └──────┘ └─────┘  └────────
```

## 🚀 Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**:
  - SQL: MySQL 8.0, PostgreSQL 15
  - NoSQL: MongoDB 7.0
  - Cache: Redis 7.0
- **Cloud**: AWS (EC2, S3, Lambda, RDS, DynamoDB, EKS, SQS, SNS)

### DevOps & CI/CD
- **Containerization**: Docker & Docker Compose
- **Orchestration**: Kubernetes (AWS EKS)
- **CI/CD**: GitHub Actions, Jenkins
- **Monitoring**: Prometheus, Grafana, CloudWatch
- **Security**: SonarQube, Trivy, OWASP Dependency Check

### Testing & Quality
- **Unit Testing**: JUnit 5, Mockito
- **Code Coverage**: JaCoCo
- **Code Quality**: SonarQube

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.9.0 or higher
- Docker & Docker Compose 20.10+
- Git
- AWS Account (for AWS services)

## 🛠️ Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd BookingEngineBackend
```

### 2. Local Development with Docker Compose

```bash
# Start all services (MySQL, MongoDB, Redis, PostgreSQL)
docker-compose up -d

# Verify services are running
docker-compose ps
```

Services will be available at:
- MySQL: localhost:3306
- MongoDB: localhost:27017
- Redis: localhost:6379
- PostgreSQL: localhost:5432
- Application: http://localhost:8080

### 3. Build the Application

```bash
# Build with Maven
mvn clean package

# Run the application
mvn spring-boot:run
```

### 4. Environment Configuration

Create `application.properties` or `application.yml` in `src/main/resources`:

```yaml
spring:
  profiles:
    active: dev
  
  datasource:
    url: jdbc:mysql://localhost:3306/booking_engine
    username: root
    password: root
  
  data:
    mongodb:
      uri: mongodb://localhost:27017/booking_engine
  
  jpa:
    hibernate:
      ddl-auto: update
```

## 📦 Project Structure

```
src/
├── main/
│   ├── java/com/booking/engine/
│   │   ├── config/                 # Spring configurations
│   │   ├── controller/             # REST controllers
│   │   ├── dto/                    # Data Transfer Objects
│   │   ├── entity/                 # JPA entities
│   │   ├── exception/              # Custom exceptions
│   │   ├── repository/
│   │   │   ├── sql/               # SQL repositories
│   │   │   └── mongodb/           # MongoDB repositories
│   │   ├── service/               # Business logic
│   │   │   └── impl/              # Service implementations
│   │   └── util/                  # Utility classes
│   └── resources/
│       ├── application.yml        # Application configuration
│       ├── application-dev.yml    # Development profile
│       ├── application-prod.yml   # Production profile
│       └── application-test.yml   # Test profile
├── test/
│   └── java/com/booking/engine/
│       ├── controller/            # Controller tests
│       ├── service/               # Service tests
│       └── repository/            # Repository tests
├── pom.xml                        # Maven configuration
├── Dockerfile                     # Docker image configuration
└── docker-compose.yml             # Docker Compose configuration
```

## 🔧 API Endpoints

### User Management

```
POST   /api/users                      # Create user
GET    /api/users/{id}                 # Get user by ID
GET    /api/users/email/{email}        # Get user by email
GET    /api/users                      # Get all users (paginated)
GET    /api/users/role/{role}          # Get users by role
PUT    /api/users/{id}                 # Update user
DELETE /api/users/{id}                 # Delete user
```

### Booking Management

```
POST   /api/bookings                   # Create booking
GET    /api/bookings/{id}              # Get booking by ID
GET    /api/bookings/number/{number}   # Get booking by number
GET    /api/bookings/user/{userId}     # Get user bookings
GET    /api/bookings/status/{status}   # Get bookings by status
GET    /api/bookings/date-range        # Get bookings by date range
PUT    /api/bookings/{id}              # Update booking
PATCH  /api/bookings/{id}/status       # Update booking status
POST   /api/bookings/{id}/cancel       # Cancel booking
DELETE /api/bookings/{id}              # Delete booking
```

### API Documentation

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=UserServiceTest
```

### Generate Coverage Report

```bash
mvn test jacoco:report
# Coverage report available at: target/site/jacoco/index.html
```

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t booking-engine:1.0.0 .
```

### Run Container

```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:mysql://mysql:3306/booking_engine \
  -e DB_USER=root \
  -e DB_PASSWORD=password \
  --name booking-engine \
  booking-engine:1.0.0
```

### Docker Compose with Full Stack

```bash
docker-compose up -d
docker-compose logs -f app
```

## ☸️ Kubernetes Deployment

### Prerequisites

```bash
# Configure kubectl
aws eks update-kubeconfig --region us-east-1 --name booking-engine-cluster

# Check cluster
kubectl cluster-info
```

### Deploy Application

```bash
# Apply manifests
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/secret.yml

# Verify deployment
kubectl get pods -n production
kubectl get svc -n production
```

## 🔄 CI/CD Pipelines

### GitHub Actions

Workflow files in `.github/workflows/`:
- `build.yml`: Build, test, and quality checks
- `deploy.yml`: Build Docker image and deploy to AWS EKS

### Jenkins

Pipeline defined in `Jenkinsfile`:
- Checkout code
- Build with Maven
- Run unit tests
- Code quality analysis (SonarQube)
- Security scanning
- Build Docker image
- Push to ECR
- Deploy to EKS

### Trigger Deployments

```bash
# GitHub - Push to main branch
git push origin main

# Jenkins - Manual trigger or webhook
# Configure webhook in GitHub repository settings
```

## 📊 Monitoring & Logging

### Actuator Endpoints

```
GET  /api/actuator/health              # Health check
GET  /api/actuator/info                # Application info
GET  /api/actuator/metrics             # Metrics
GET  /api/actuator/prometheus          # Prometheus metrics
```

### CloudWatch

```bash
# View logs in CloudWatch
aws logs describe-log-groups --region us-east-1

# Stream logs
aws logs tail /aws/eks/booking-engine --follow
```

### Local Monitoring Stack (Optional)

```bash
# Add to docker-compose.yml
docker-compose up -d prometheus grafana
```

## 🔐 Security

### Environment Variables

Store sensitive data in environment variables:

```bash
export JWT_SECRET=your-secret-key
export DB_PASSWORD=your-password
export AWS_ACCESS_KEY_ID=your-key
export AWS_SECRET_ACCESS_KEY=your-secret
```

### HTTPS Configuration

```yaml
server:
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### Authentication

The application uses JWT (JSON Web Tokens):

```
Authorization: Bearer <token>
```

## 📝 Configuration Profiles

### Development

```bash
export SPRING_PROFILES_ACTIVE=dev
```

### Production

```bash
export SPRING_PROFILES_ACTIVE=prod
```

### Testing

```bash
export SPRING_PROFILES_ACTIVE=test
```

## 🚨 Troubleshooting

### Database Connection Issues

```bash
# Check MySQL connectivity
docker exec booking-engine-mysql mysql -u root -proot -e "SELECT 1"

# Check MongoDB connectivity
docker exec booking-engine-mongodb mongosh --eval "db.adminCommand('ping')"
```

### Application Won't Start

```bash
# Check logs
docker-compose logs app

# Restart services
docker-compose restart
```

### Port Already in Use

```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Security](https://spring.io/projects/spring-security)
- [AWS Java SDK](https://docs.aws.amazon.com/sdk-for-java/)
- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)

## 🤝 Contributing

1. Create a feature branch: `git checkout -b feature/new-feature`
2. Commit changes: `git commit -am 'Add new feature'`
3. Push to branch: `git push origin feature/new-feature`
4. Create Pull Request

## 📄 License

This project is licensed under the MIT License - see LICENSE.md for details

## 👨‍💼 Support

For issues, questions, or suggestions:
- Create an issue on GitHub
- Contact: info@miim.in
- Documentation: [Wiki](https://github.com/miim-in/BookingEngineBackend/wiki)

---

**Last Updated**: September 2026
**Version**: 1.0.0-SNAPSHOT
