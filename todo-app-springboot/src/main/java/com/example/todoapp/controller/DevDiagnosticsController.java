package com.example.todoapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Development diagnostics controller.
 * Provides detailed diagnostic information for troubleshooting development issues.
 */
@RestController
@RequestMapping("/dev/diagnostics")
@Profile("dev")
public class DevDiagnosticsController {

    private static final Logger logger = LoggerFactory.getLogger(DevDiagnosticsController.class);

    private final Environment environment;
    private final DataSource dataSource;

    public DevDiagnosticsController(Environment environment, DataSource dataSource) {
        this.environment = environment;
        this.dataSource = dataSource;
    }

    /**
     * Get comprehensive environment diagnostics
     */
    @GetMapping("/environment")
    public ResponseEntity<Map<String, Object>> getEnvironmentDiagnostics() {
        logger.debug("Retrieving environment diagnostics");
        
        Map<String, Object> diagnostics = new HashMap<>();
        
        // Active profiles
        diagnostics.put("activeProfiles", Arrays.asList(environment.getActiveProfiles()));
        diagnostics.put("defaultProfiles", Arrays.asList(environment.getDefaultProfiles()));
        
        // System properties
        Map<String, Object> systemProps = new HashMap<>();
        Properties sysProps = System.getProperties();
        systemProps.put("java.version", sysProps.getProperty("java.version"));
        systemProps.put("java.vendor", sysProps.getProperty("java.vendor"));
        systemProps.put("java.home", sysProps.getProperty("java.home"));
        systemProps.put("os.name", sysProps.getProperty("os.name"));
        systemProps.put("os.version", sysProps.getProperty("os.version"));
        systemProps.put("user.dir", sysProps.getProperty("user.dir"));
        systemProps.put("user.timezone", sysProps.getProperty("user.timezone"));
        diagnostics.put("systemProperties", systemProps);
        
        // Environment variables (selected)
        Map<String, Object> envVars = new HashMap<>();
        envVars.put("JAVA_HOME", System.getenv("JAVA_HOME"));
        envVars.put("PATH", System.getenv("PATH"));
        envVars.put("USER", System.getenv("USER"));
        diagnostics.put("environmentVariables", envVars);
        
        // Spring configuration
        Map<String, Object> springConfig = new HashMap<>();
        springConfig.put("server.port", environment.getProperty("server.port"));
        springConfig.put("spring.profiles.active", environment.getProperty("spring.profiles.active"));
        springConfig.put("spring.datasource.url", environment.getProperty("spring.datasource.url"));
        springConfig.put("spring.jpa.hibernate.ddl-auto", environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        springConfig.put("logging.level.root", environment.getProperty("logging.level.root"));
        diagnostics.put("springConfiguration", springConfig);
        
        diagnostics.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(diagnostics);
    }

    /**
     * Get database diagnostics
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> getDatabaseDiagnostics() {
        logger.debug("Retrieving database diagnostics");
        
        Map<String, Object> diagnostics = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            Map<String, Object> dbInfo = new HashMap<>();
            dbInfo.put("productName", metaData.getDatabaseProductName());
            dbInfo.put("productVersion", metaData.getDatabaseProductVersion());
            dbInfo.put("driverName", metaData.getDriverName());
            dbInfo.put("driverVersion", metaData.getDriverVersion());
            dbInfo.put("url", metaData.getURL());
            dbInfo.put("userName", metaData.getUserName());
            dbInfo.put("isReadOnly", connection.isReadOnly());
            dbInfo.put("autoCommit", connection.getAutoCommit());
            dbInfo.put("transactionIsolation", connection.getTransactionIsolation());
            
            diagnostics.put("connection", "SUCCESS");
            diagnostics.put("databaseInfo", dbInfo);
            
        } catch (Exception e) {
            diagnostics.put("connection", "FAILED");
            diagnostics.put("error", e.getMessage());
            logger.error("Database diagnostics failed", e);
        }
        
        diagnostics.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(diagnostics);
    }

    /**
     * Get classpath and dependency information
     */
    @GetMapping("/classpath")
    public ResponseEntity<Map<String, Object>> getClasspathDiagnostics() {
        logger.debug("Retrieving classpath diagnostics");
        
        Map<String, Object> diagnostics = new HashMap<>();
        
        // Class loading information
        Map<String, Object> classLoader = new HashMap<>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        classLoader.put("classLoaderName", cl.getClass().getName());
        classLoader.put("classLoaderString", cl.toString());
        
        // System classpath
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(System.getProperty("path.separator"));
        classLoader.put("classpathEntries", classpathEntries.length);
        classLoader.put("sampleEntries", Arrays.asList(Arrays.copyOfRange(classpathEntries, 0, Math.min(10, classpathEntries.length))));
        
        diagnostics.put("classLoader", classLoader);
        
        // Library path
        diagnostics.put("libraryPath", System.getProperty("java.library.path"));
        
        // Boot classpath (if available)
        String bootClasspath = System.getProperty("sun.boot.class.path");
        if (bootClasspath != null) {
            diagnostics.put("bootClasspath", bootClasspath.split(System.getProperty("path.separator")).length + " entries");
        }
        
        diagnostics.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(diagnostics);
    }

    /**
     * Run system health checks
     */
    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> runHealthCheck() {
        logger.debug("Running comprehensive health check");
        
        Map<String, Object> healthCheck = new HashMap<>();
        
        // Memory check
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("totalMemory", runtime.totalMemory());
        memory.put("freeMemory", runtime.freeMemory());
        memory.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        memory.put("maxMemory", runtime.maxMemory());
        memory.put("memoryUsagePercent", ((double)(runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory()) * 100);
        
        boolean memoryHealthy = ((double)(runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory()) < 0.8;
        memory.put("status", memoryHealthy ? "HEALTHY" : "WARNING");
        healthCheck.put("memory", memory);
        
        // Database connectivity check
        Map<String, Object> database = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            database.put("status", "HEALTHY");
            database.put("connectionValid", connection.isValid(5));
        } catch (Exception e) {
            database.put("status", "UNHEALTHY");
            database.put("error", e.getMessage());
        }
        healthCheck.put("database", database);
        
        // Thread count check
        Map<String, Object> threads = new HashMap<>();
        int threadCount = Thread.activeCount();
        threads.put("activeThreads", threadCount);
        threads.put("status", threadCount < 50 ? "HEALTHY" : "WARNING");
        healthCheck.put("threads", threads);
        
        // Overall status
        boolean overallHealthy = memoryHealthy && 
                                "HEALTHY".equals(database.get("status")) && 
                                threadCount < 50;
        healthCheck.put("overallStatus", overallHealthy ? "HEALTHY" : "WARNING");
        healthCheck.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(healthCheck);
    }

    /**
     * Get configuration troubleshooting information
     */
    @GetMapping("/config-check")
    public ResponseEntity<Map<String, Object>> getConfigurationCheck() {
        logger.debug("Running configuration check");
        
        Map<String, Object> configCheck = new HashMap<>();
        
        // Profile validation
        Map<String, Object> profiles = new HashMap<>();
        String[] activeProfiles = environment.getActiveProfiles();
        profiles.put("active", Arrays.asList(activeProfiles));
        profiles.put("isDevelopment", Arrays.asList(activeProfiles).contains("dev"));
        profiles.put("status", Arrays.asList(activeProfiles).contains("dev") ? "CORRECT" : "WARNING - Dev profile not active");
        configCheck.put("profiles", profiles);
        
        // Critical configuration validation
        Map<String, Object> criticalConfig = new HashMap<>();
        
        // Database configuration
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        criticalConfig.put("datasource.url", datasourceUrl != null ? "CONFIGURED" : "MISSING");
        
        // H2 console
        String h2ConsoleEnabled = environment.getProperty("spring.h2.console.enabled");
        criticalConfig.put("h2.console.enabled", "true".equals(h2ConsoleEnabled) ? "ENABLED" : "DISABLED");
        
        // DevTools
        String devToolsEnabled = environment.getProperty("spring.devtools.restart.enabled");
        criticalConfig.put("devtools.restart.enabled", "true".equals(devToolsEnabled) ? "ENABLED" : "DISABLED");
        
        // Thymeleaf cache
        String thymeleafCache = environment.getProperty("spring.thymeleaf.cache");
        criticalConfig.put("thymeleaf.cache", "false".equals(thymeleafCache) ? "DISABLED (Good for dev)" : "ENABLED (Check if needed)");
        
        configCheck.put("criticalConfiguration", criticalConfig);
        
        // Port configuration
        Map<String, Object> networking = new HashMap<>();
        String serverPort = environment.getProperty("server.port", "8080");
        networking.put("serverPort", serverPort);
        networking.put("actuatorBasePath", environment.getProperty("management.endpoints.web.base-path", "/actuator"));
        configCheck.put("networking", networking);
        
        configCheck.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(configCheck);
    }
}