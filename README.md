# Project Task Tracker 📋

A Java Spring Boot web application designed for tracking tasks, task history, comments, file attachments (supporting large files), status management, and reporting.

---

## 🚀 Quick Start - Running the Application with Maven

### Prerequisite: PostgreSQL Setup
Ensure PostgreSQL is running locally and create the database:
```sql
CREATE DATABASE projecttasktracker;
```
*(Update `src/main/resources/application.properties` with your PostgreSQL username & password if different).*

### Run Commands

#### Option 1: Using Installed Maven
```bash
mvn spring-boot:run
```

#### Option 2: Using Maven Wrapper (Windows PowerShell / CMD)
```cmd
.\mvnw.cmd spring-boot:run
```

#### Option 3: Using Maven Wrapper (Bash / Git Bash / Linux / macOS)
```bash
./mvnw spring-boot:run
```

Once started, open your browser and navigate to:
👉 **[http://localhost:8080](http://localhost:8080)**

---

## 🛠️ Complete Guide: Step-by-Step Project Creation

Below are the exact commands and steps to recreate this project structure and configuration from scratch.

### Step 1: Create the Project Directory Structure

#### On Windows (PowerShell / Command Prompt):
```cmd
mkdir ProjectTaskTracker
cd ProjectTaskTracker

:: Main Java Package Folders
mkdir src\main\java\com\tasktracker\ProjectTaskTracker

:: Main Resources & Web Folders
mkdir src\main\resources\static
mkdir src\main\resources\templates

:: Test Directory Folders
mkdir src\test\java\com\tasktracker\ProjectTaskTracker

:: File Upload Storage Directory
mkdir uploads
```

#### On Linux / macOS / Bash:
```bash
mkdir -p ProjectTaskTracker/src/main/java/com/tasktracker/ProjectTaskTracker
mkdir -p ProjectTaskTracker/src/main/resources/static
mkdir -p ProjectTaskTracker/src/main/resources/templates
mkdir -p ProjectTaskTracker/src/test/java/com/tasktracker/ProjectTaskTracker
mkdir -p ProjectTaskTracker/uploads
cd ProjectTaskTracker
```

---

### Step 2: Project Directory Layout

```
ProjectTaskTracker/
├── pom.xml
├── README.md
├── HELP.md
├── mvnw
├── mvnw.cmd
├── .mvn/
├── uploads/
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── tasktracker/
    │   │           └── ProjectTaskTracker/
    │   │               ├── TaskTrackerApplication.java
    │   │               ├── Task.java
    │   │               ├── TaskComment.java
    │   │               ├── TaskHistory.java
    │   │               ├── TaskRepository.java
    │   │               ├── TaskCommentRepository.java
    │   │               ├── TaskHistoryRepository.java
    │   │               ├── TaskController.java
    │   │               ├── UIController.java
    │   │               ├── ReportController.java
    │   │               ├── StatusController.java
    │   │               └── UploadController.java
    │   └── resources/
    │       ├── application.properties
    │       ├── static/
    │       └── templates/
    └── test/
        └── java/
            └── com/
                └── tasktracker/
                    └── ProjectTaskTracker/
```

---

### Step 3: Create Maven `pom.xml` Configuration

Create `pom.xml` in the root directory:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.tasktracker</groupId>
    <artifactId>ProjectTaskTracker</artifactId>
    <version>1.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.2</version>
        <relativePath/>
    </parent>

    <dependencies>
        <!-- Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- UI Templating -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- JPA Data Store -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- PostgreSQL Database Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### Step 4: Configure `application.properties`

Create `src/main/resources/application.properties`:

```properties
spring.application.name=ProjectTaskTracker

# PostgreSQL DB Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/projecttasktracker
spring.datasource.username=postgres
spring.datasource.password=root1234

# JPA / Hibernate Settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Upload Limits - Support for large files
spring.servlet.multipart.max-file-size=10GB
spring.servlet.multipart.max-request-size=10GB
spring.servlet.multipart.enabled=true
spring.servlet.multipart.file-size-threshold=50MB
spring.servlet.resolve-lazily=true

# Tomcat Upload Limits
server.tomcat.max-http-form-post-size=10GB
server.tomcat.max-swallow-size=10GB
```

---

### Step 5: Essential Maven Commands Reference

| Action | Command (Maven Installed) | Command (Maven Wrapper - Windows) | Command (Maven Wrapper - Bash/Linux) |
| :--- | :--- | :--- | :--- |
| **Run Application** | `mvn spring-boot:run` | `.\mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` |
| **Clean Project** | `mvn clean` | `.\mvnw.cmd clean` | `./mvnw clean` |
| **Compile Code** | `mvn compile` | `.\mvnw.cmd compile` | `./mvnw compile` |
| **Run Unit Tests** | `mvn test` | `.\mvnw.cmd test` | `./mvnw test` |
| **Build Executable JAR** | `mvn clean package` | `.\mvnw.cmd clean package` | `./mvnw clean package` |
| **Run Executable JAR** | `java -jar target/ProjectTaskTracker-1.0.jar` | `java -jar target/ProjectTaskTracker-1.0.jar` | `java -jar target/ProjectTaskTracker-1.0.jar` |
| **Force Re-download Dependencies** | `mvn clean compile -U` | `.\mvnw.cmd clean compile -U` | `./mvnw clean compile -U` |

---

## ⚡ Tech Stack

- **Java Version:** 17+
- **Framework:** Spring Boot 3.2.2
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA / Hibernate
- **Template Engine:** Thymeleaf
- **Build Tool:** Apache Maven
