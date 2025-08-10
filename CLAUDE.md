# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an AIDD (AI-Driven Development) Agents project containing:

1. **AIDD Agent System**: Six specialized agents for software development workflow
2. **Todo Management Application**: A production-ready Spring Boot application created through the AIDD process
3. **Specification Documents**: Complete development documentation (requirements, design, tasks) 

## Core Architecture

### AIDD Agent System

The project demonstrates a 6-stage AI-driven development workflow:

- `aidd-step01-requirements`: Initial specifications → formal requirements definition
- `aidd-step02-design`: Requirements → comprehensive system design  
- `aidd-step03-task-plan`: Design → detailed implementation tasks
- `aidd-step04-implementation`: Specifications → working code
- `aidd-step05-issue-management`: GitHub issue creation/management (Japanese)
- `aidd-step06-pr-workflow`: Commit, push, and pull request creation (Japanese)

### Todo Application Architecture

Located in `/todo-app-springboot/`:
- **Pattern**: Layered architecture (Controller → Service → Repository → Entity)
- **Technology**: Spring Boot 3.2.1, Java 17, H2 Database, Maven
- **Features**: CRUD operations, validation, error handling, REST API, Thymeleaf web UI
- **Monitoring**: Actuator, Prometheus metrics, Grafana dashboards
- **Profiles**: dev, prod, test configurations

## Development Commands

### Todo Application Development

Navigate to `todo-app-springboot/` first:

```bash
cd todo-app-springboot
```

**Build and Run:**
```bash
# Development mode with debugging features
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Standard mode  
mvn spring-boot:run

# Build JAR and run
mvn clean package
java -jar target/todo-app-1.0.0.jar
```

**Testing:**
```bash
# All tests
mvn test

# Integration tests only
mvn test -Dtest="*IntegrationTest"  

# End-to-end tests only
mvn test -Dtest="CompleteEndToEndIntegrationTest"

# Single test class
mvn test -Dtest="TodoServiceImplTest"

# Single test method
mvn test -Dtest="TodoServiceImplTest#testCreateTodo"
```

**Production Deployment:**
```bash
# Docker deployment
docker-compose up -d

# Monitor logs
docker-compose logs -f todo-app

# Health check
curl http://localhost:8080/actuator/health/production
```

### Application Access URLs

**Development Profile (dev):**
- Web UI: http://localhost:8080/todos
- REST API: http://localhost:8080/api/todos
- H2 Console: http://localhost:8080/h2-console
- Dev Tools: http://localhost:8080/dev/
- Actuator: http://localhost:8080/dev/actuator

**Production Profile:**
- Web UI: http://localhost:8080/todos
- Health Check: http://localhost:8080/actuator/health/production
- Grafana: http://localhost:3000 (admin/admin123)
- Prometheus: http://localhost:9090

## AIDD Agent Usage

Use the Task tool to invoke specialized agents:

```bash
# Requirements definition
Task: aidd-step01-requirements
Input: Initial specifications, system overview
Output: docs/specs/{system-name}/requirements.md

# System design  
Task: aidd-step02-design
Input: requirements.md file path
Output: docs/specs/{system-name}/design.md

# Implementation planning
Task: aidd-step03-task-plan
Input: design.md file path  
Output: docs/specs/{system-name}/tasks.md

# Code implementation
Task: aidd-step04-implementation
Input: specification files, specific task
Output: Working implementation code

# GitHub issue management (Japanese)
Task: aidd-step05-issue-management
Input: Bug reports, feature requests
Output: Formatted GitHub issues

# PR workflow (Japanese)  
Task: aidd-step06-pr-workflow
Input: Completed work
Output: Commits, pushes, pull requests
```

## Key Implementation Patterns

### Spring Boot Application Structure

**Entity Layer:**
- Use JPA entities with validation annotations
- Enum types for controlled values (TodoStatus, TodoPriority)
- Audit fields with automatic timestamps

**Service Layer:**
- Interface-based services with implementation classes  
- `@Transactional` for database operations
- Custom business exceptions

**Controller Layer:**
- Separate REST (`TodoRestController`) and Web (`TodoWebController`) controllers
- DTO pattern for API communication
- Comprehensive validation with custom validators

**Error Handling:**
- Global exception handler (`GlobalExceptionHandler`)
- Structured error responses with correlation IDs
- User-friendly error messages

**Testing Strategy:**
- Unit tests for each layer (Repository, Service, Controller)
- Integration tests for cross-layer functionality
- End-to-end tests with actual HTTP server
- Performance and security test suites

### Development Profiles

**dev profile:**
- H2 console access
- Development-specific endpoints under `/dev/`
- Enhanced logging and debugging features
- Sample data loading

**prod profile:**  
- Production security settings
- Monitoring and metrics enabled
- Performance optimizations
- Health checks for production readiness

**test profile:**
- In-memory database configuration
- Test-specific property overrides
- Isolated test environment

## Architecture Insights

### AIDD Workflow Pattern

The complete specification chain demonstrates AI-driven development:
1. `docs/specs/todo-app-springboot/requirements.md` - Formal requirements
2. `docs/specs/todo-app-springboot/design.md` - System architecture  
3. `docs/specs/todo-app-springboot/tasks.md` - Implementation roadmap
4. `todo-app-springboot/` - Final implementation

### Spring Boot Features Utilized

**Core Spring Boot:**
- Auto-configuration for rapid setup
- Profile-based configuration management
- Embedded server (Tomcat) for easy deployment

**Data Layer:**
- Spring Data JPA for repository pattern
- H2 embedded database for development
- HikariCP connection pooling

**Web Layer:**
- Spring MVC for REST APIs
- Thymeleaf for server-side rendering
- Static resource handling

**Operations:**
- Spring Boot Actuator for monitoring
- Micrometer metrics with Prometheus
- Custom health indicators

**Security:**
- Spring Security for web security
- CSRF protection
- SQL injection and XSS protection

## Important Notes

- All development commands assume working directory is `todo-app-springboot/`
- The Todo app serves as a complete example of AIDD agent-generated code
- AIDD agents work in Japanese for GitHub workflows (issues, PRs)
- Use development profile for debugging and testing features
- Production deployment includes comprehensive monitoring stack