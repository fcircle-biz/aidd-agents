# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an AI-Driven Development (AIDD) project that contains:

1. **AIDD Agent System**: A collection of specialized AI agents for software development workflow
2. **Todo App Implementation**: A fully-featured Spring Boot todo management application
3. **Development Templates**: Structured templates for requirements, design, and task generation

## Core Architecture

### AIDD Agent System

The project contains specialized agents designed for different phases of AI-driven development:

- **Requirements Analysis** (`aidd-step01-requirements`): Converts initial requirements into formal requirement definitions
- **System Design** (`aidd-step02-design`): Creates comprehensive design documents from requirements
- **Task Planning** (`aidd-step03-task-plan`): Generates detailed implementation task lists
- **Implementation** (`aidd-step04-implementation`): Handles actual code implementation from specifications
- **Issue Management** (`aidd-step05-issue-management`): Creates and manages GitHub issues in Japanese
- **PR Workflow** (`aidd-step06-pr-workflow`): Handles commits, pushes, and pull request creation

### Templates Structure

Located in `/templates/`:
- `requirements-definition-prompt.md`: Template for generating detailed requirement specifications
- `design-generation-prompt.md`: Template for creating system design documents
- `task-generation-prompt.md`: Template for breaking design into implementation tasks

### Todo App Spring Boot Application

Located in `/todo-app-springboot/`:
- **Architecture**: Layered architecture with Spring Boot 3.x, Java 17, H2 database
- **Layers**: Controller → Service → Repository → Entity
- **Features**: CRUD operations, validation, error handling, REST API, Thymeleaf web UI
- **Monitoring**: Actuator endpoints, Prometheus metrics, Grafana dashboards

## Development Commands

### Todo App Development

Navigate to `todo-app-springboot/` directory first:

```bash
cd todo-app-springboot
```

**Build and Run:**
```bash
# Maven development mode
mvn spring-boot:run

# Development profile with additional dev features  
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Build and run JAR
mvn clean package
java -jar target/todo-app-1.0.0.jar
```

**Testing:**
```bash
# Run all tests
mvn test

# Run integration tests only
mvn test -Dtest="*IntegrationTest"

# Run end-to-end tests only
mvn test -Dtest="CompleteEndToEndIntegrationTest"

# Run specific test profile
mvn test -Dspring.profiles.active=test
```

**Docker Deployment:**
```bash
# Production deployment
docker-compose up -d

# View logs
docker-compose logs -f todo-app

# Health check
curl http://localhost:8080/actuator/health/production
```

### Todo App Access URLs

- **Web UI**: http://localhost:8080/todos
- **REST API**: http://localhost:8080/api/todos  
- **H2 Console**: http://localhost:8080/h2-console
- **Dev Tools** (dev profile): http://localhost:8080/dev/
- **Actuator**: http://localhost:8080/dev/actuator (dev profile)
- **Grafana Dashboard**: http://localhost:3000 (admin/admin123)
- **Prometheus**: http://localhost:9090

## Key Implementation Patterns

### AIDD Workflow Pattern

1. **Requirements** → `docs/specs/{system-name}/requirements.md`
2. **Design** → `docs/specs/{system-name}/design.md`  
3. **Tasks** → `docs/specs/{system-name}/tasks.md`
4. **Implementation** → Working code with tests
5. **Issue Management** → GitHub issues in Japanese
6. **PR Workflow** → Commits, pushes, pull requests

### Todo App Patterns

**Entity Design:**
- Use Spring Data JPA entities with validation annotations
- Enum types for status and priority (`TodoStatus`, `TodoPriority`)
- Audit fields (createdAt, updatedAt) with automatic timestamps

**Service Layer:**
- Interface-based services with implementation classes
- Transaction management with `@Transactional`
- Custom exceptions for business logic errors

**Controller Pattern:**
- Separate REST (`TodoRestController`) and Web (`TodoWebController`) controllers
- DTO pattern for API responses (`TodoResponse`, `TodoRequest`)
- Comprehensive validation with custom validators

**Error Handling:**
- Global exception handler (`GlobalExceptionHandler`)
- Custom business exceptions (`BusinessException`, `TodoNotFoundException`)
- Structured error responses with correlation IDs

**Testing Strategy:**
- Unit tests for each layer (Repository, Service, Controller)
- Integration tests for cross-layer functionality
- End-to-end tests with actual HTTP server
- Performance and security tests

## Development Environment Setup

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose (for production deployment)

### Spring Profiles
- **default**: Basic configuration
- **dev**: Development mode with additional debugging features, dev controllers, H2 console access
- **prod**: Production mode with security, monitoring, performance optimizations  
- **test**: Test configuration with in-memory database

## AIDD Agent Usage

When working with AIDD agents, use the Task tool to invoke specific agents:

- Use `aidd-step01-requirements` for converting initial specs to formal requirements
- Use `aidd-step02-design` for creating system design from requirements  
- Use `aidd-step03-task-plan` for generating implementation tasks from design
- Use `aidd-step04-implementation` for implementing specific tasks
- Use `aidd-step05-issue-management` for GitHub issue creation (Japanese)
- Use `aidd-step06-pr-workflow` for committing and creating pull requests (Japanese)

## Important Notes

- All Todo App code uses Spring Boot best practices with comprehensive error handling
- Development profile includes extensive debugging and monitoring tools
- Production deployment includes Grafana dashboards and Prometheus metrics
- AIDD agents work in Japanese for issue management and PR workflows
- Templates follow structured prompts for consistency across projects