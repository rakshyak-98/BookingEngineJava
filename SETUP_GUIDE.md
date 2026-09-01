# Booking Engine Backend - Setup Guide

## Quick Start (5 minutes)

### Prerequisites
- Java 17+ installed
- Maven 3.9.0+
- Docker & Docker Compose installed
- Git installed

### Step 1: Clone and Navigate
```bash
cd BookingEngineBackend
```

### Step 2: Start Infrastructure
```bash
docker-compose up -d
```

### Step 3: Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### Step 4: Access Application
- **API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **Health Check**: http://localhost:8080/api/actuator/health

---

## Detailed Setup Instructions

### 1. Development Environment Setup

#### Windows/Mac/Linux

1. **Install Java 17**
   ```bash
   # Using SDKMAN (recommended)
   curl -s "https://get.sdkman.io" | bash
   sdk install java 17.0.5-tem
   ```

2. **Install Maven**
   ```bash
   # Using SDKMAN
   sdk install maven 3.9.0
   
   # Or download from https://maven.apache.org/download.cgi
   ```

3. **Verify Installation**
   ```bash
   java -version
   mvn -version
   ```

### 2. Database Setup

#### Option A: Docker Compose (Recommended)
```bash
cd BookingEngineBackend
docker-compose up -d
```

This starts:
- MySQL 8.0 (port 3306)
- MongoDB 7.0 (port 27017)
- PostgreSQL 15 (port 5432)
- Redis 7.0 (port 6379)

#### Option B: Local Installation

**MySQL**
```bash
# macOS
brew install mysql
brew services start mysql

# Linux
sudo apt-get install mysql-server
sudo systemctl start mysql

# Windows: Download from https://dev.mysql.com/downloads/
```

**MongoDB**
```bash
# macOS
brew install mongodb-community
brew services start mongodb-community

# Linux
sudo apt-get install mongodb
sudo systemctl start mongod

# Windows: Download from https://www.mongodb.com/try/download/community
```

### 3. Project Configuration

#### Create `.env` file in project root
```properties
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=booking_engine_dev
DB_USER=root
DB_PASSWORD=root

# MongoDB
MONGO_URI=mongodb://localhost:27017/booking_engine

# AWS (optional)
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-key
AWS_SECRET_ACCESS_KEY=your-secret

# JWT
JWT_SECRET=your-secret-key-change-in-production
```

#### Configure IDE

**IntelliJ IDEA**
1. Open project
2. File → Project Structure → SDKs
3. Select Java 17
4. Run → Edit Configurations
5. Add Spring Boot application
6. Set active profiles: `dev`

**VS Code**
1. Install "Extension Pack for Java"
2. Install "REST Client" (optional, for testing APIs)
3. Create `.vscode/settings.json`:
```json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/path/to/java17"
    }
  ]
}
```

### 4. IDE Configuration

#### IntelliJ IDEA
- Settings → Plugins → Install "Lombok"
- Enable annotation processing
- Configure code style

#### VS Code
- Install recommended extensions
- Install Java Extension Pack
- Install Spring Boot Extension Pack

### 5. Running the Application

#### Option A: IDE
- Open `BookingEngineBackendApplication.java`
- Click Run (or Shift+F10 on Windows/Linux, Ctrl+R on Mac)

#### Option B: Maven
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

#### Option C: Compiled JAR
```bash
mvn clean package
java -jar target/booking-engine-backend-1.0.0-SNAPSHOT.jar
```

### 6. Verify Installation

**Test API Endpoint**
```bash
curl http://localhost:8080/api/actuator/health
```

**Access Swagger UI**
Open in browser: http://localhost:8080/api/swagger-ui.html

**Check Database Connection**
```bash
# MySQL
mysql -h localhost -u root -proot -e "SELECT 1"

# MongoDB
mongosh --eval "db.adminCommand('ping')"
```

---

## Common Issues & Troubleshooting

### Port Already in Use
```bash
# Find and kill process on port 8080
# Linux/Mac
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Database Connection Failed
```bash
# Check MySQL is running
docker-compose ps

# Restart services
docker-compose restart mysql

# Check logs
docker-compose logs mysql
```

### Build Fails
```bash
# Clear cache
mvn clean

# Update dependencies
mvn dependency:resolve

# Full rebuild
mvn clean install -U
```

### Port Mappings
If ports conflict, edit `docker-compose.yml`:
```yaml
services:
  mysql:
    ports:
      - "3307:3306"  # Changed from 3306 to 3307
```

Then update `application.yml`:
```yaml
datasource:
  url: jdbc:mysql://localhost:3307/booking_engine_dev
```

---

## Development Workflow

### 1. Daily Development
```bash
# Start containers
docker-compose up -d

# Pull latest code
git pull

# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Access at http://localhost:8080/api
```

### 2. Running Tests
```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=UserServiceTest

# With coverage
mvn clean test jacoco:report
```

### 3. Code Quality Checks
```bash
# Format code
mvn spotless:apply

# Check code style
mvn checkstyle:check

# SonarQube analysis
mvn clean verify sonar:sonar
```

### 4. Git Workflow
```bash
# Create feature branch
git checkout -b feature/your-feature

# Make changes and commit
git add .
git commit -m "Add new feature"

# Push to remote
git push origin feature/your-feature

# Create Pull Request on GitHub
```

---

## IDE Extensions & Plugins

### IntelliJ IDEA
- Lombok
- Spring Boot
- SonarLint
- Database Tools and SQL
- .env files support

### VS Code
- Extension Pack for Java
- Spring Boot Extension Pack
- Lombok Annotations Support
- REST Client
- Thunder Client
- Docker

### Install Commands
```bash
# VS Code
code --install-extension vscjava.extension-pack-for-java
code --install-extension vscjava.vscode-spring-boot-dashboard
code --install-extension gabrielbb.vscode-lombok
```

---

## Next Steps

1. Read the main [README.md](README.md)
2. Review [Architecture Overview](#architecture)
3. Check [API Documentation](http://localhost:8080/api/swagger-ui.html)
4. Explore [Database Schema](scripts/init.sql)
5. Review [CI/CD Pipelines](.github/workflows/)

---

## Support & Help

- **Issues**: Create GitHub issue
- **Questions**: Check FAQ or ask on discussions
- **Documentation**: See [README.md](README.md)
- **Email**: info@miim.in

---

**Last Updated**: September 2026
**Version**: 1.0.0
