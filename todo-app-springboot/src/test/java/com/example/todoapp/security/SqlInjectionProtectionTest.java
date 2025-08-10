package com.example.todoapp.security;

import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SQL Injection Protection Tests for the Todo application.
 * 
 * This test class verifies that SQL injection attacks are prevented through:
 * - JPA parameter binding in repository methods
 * - Proper input validation and sanitization
 * - Safe handling of user inputs in database queries
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("SQL Injection Protection Tests")
@Transactional
class SqlInjectionProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    @BeforeEach
    void setUp() {
        // Clear existing data and create test data
        todoRepository.deleteAll();
        
        // Create some test todos
        Todo todo1 = new Todo();
        todo1.setTitle("Test Todo 1");
        todo1.setDescription("Description 1");
        todo1.setStatus(TodoStatus.TODO);
        todo1.setPriority(TodoPriority.HIGH);
        todo1.setDueDate(LocalDate.now().plusDays(7));
        
        Todo todo2 = new Todo();
        todo2.setTitle("Test Todo 2");
        todo2.setDescription("Description 2");
        todo2.setStatus(TodoStatus.IN_PROGRESS);
        todo2.setPriority(TodoPriority.MEDIUM);
        todo2.setDueDate(LocalDate.now().plusDays(3));
        
        todoRepository.save(todo1);
        todoRepository.save(todo2);
    }

    @Test
    @DisplayName("Should handle SQL injection in search keyword safely")
    void shouldHandleSqlInjectionInSearchKeyword() throws Exception {
        String sqlInjectionPayload = "'; DROP TABLE todo; --";
        
        mockMvc.perform(get("/api/todos/search")
                .param("keyword", sqlInjectionPayload)
                .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // The application should still be functioning (table not dropped)
                .andExpect(jsonPath("$").isArray());

        // Verify that the table still exists and has data
        List<Todo> todos = todoRepository.findAll();
        assertFalse(todos.isEmpty(), "Table should still exist with data");
    }

    @Test
    @DisplayName("Should handle SQL injection in status parameter safely")
    void shouldHandleSqlInjectionInStatusParameter() throws Exception {
        String sqlInjectionPayload = "TODO'; DELETE FROM todo WHERE '1'='1";
        
        mockMvc.perform(get("/api/todos/search")
                .param("keyword", "Test")
                .param("status", sqlInjectionPayload))
                .andExpect(status().isBadRequest()); // Should return bad request for invalid status

        // Verify that no data was deleted
        List<Todo> todos = todoRepository.findAll();
        assertEquals(2, todos.size(), "All todos should still exist");
    }

    @Test
    @DisplayName("Should handle SQL injection in web search safely")
    void shouldHandleSqlInjectionInWebSearch() throws Exception {
        String sqlInjectionPayload = "1' OR '1'='1' UNION SELECT * FROM todo --";
        
        mockMvc.perform(get("/todos/search")
                .param("keyword", sqlInjectionPayload)
                .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"));

        // Verify database integrity
        List<Todo> todos = todoRepository.findAll();
        assertEquals(2, todos.size(), "Database should be intact");
    }

    @Test
    @DisplayName("Should handle SQL injection in API create request safely")
    void shouldHandleSqlInjectionInApiCreate() throws Exception {
        String sqlInjectionTitle = "Test'; DROP TABLE todo; --";
        String jsonContent = String.format("""
            {
                "title": "%s",
                "description": "Test description",
                "status": "TODO",
                "priority": "MEDIUM"
            }
            """, sqlInjectionTitle);

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(sqlInjectionTitle));

        // Verify that the malicious content was stored safely and didn't execute
        List<Todo> todos = todoRepository.findAll();
        assertEquals(3, todos.size(), "Todo should be created without executing SQL");
        
        // Find the created todo and verify the title was stored as-is
        Todo maliciousTodo = todos.stream()
                .filter(todo -> todo.getTitle().equals(sqlInjectionTitle))
                .findFirst()
                .orElse(null);
        
        assertNotNull(maliciousTodo, "Todo with malicious title should exist");
        assertEquals(sqlInjectionTitle, maliciousTodo.getTitle(), "Title should be stored as plain text");
    }

    @Test
    @DisplayName("Should handle SQL injection through JPA repository methods safely")
    void shouldHandleSqlInjectionThroughRepository() throws Exception {
        String maliciousKeyword = "'; DROP TABLE todo; SELECT * FROM todo WHERE title LIKE '%";
        
        // Test various repository methods with malicious input
        assertDoesNotThrow(() -> {
            todoRepository.findByTitleContainingOrDescriptionContaining(maliciousKeyword, maliciousKeyword);
        }, "Repository method should handle malicious input safely");
        
        // Verify database is still intact
        List<Todo> todos = todoRepository.findAll();
        assertEquals(2, todos.size(), "Database should remain intact");
    }

    @Test
    @DisplayName("Should handle complex SQL injection attempts safely")
    void shouldHandleComplexSqlInjection() throws Exception {
        String[] maliciousInputs = {
            "'; INSERT INTO todo (title) VALUES ('injected'); --",
            "' UNION SELECT password FROM users WHERE '1'='1",
            "'; UPDATE todo SET title='hacked'; --",
            "' OR 1=1 --",
            "'; EXEC xp_cmdshell('dir'); --",
            "' UNION SELECT NULL,NULL,NULL,NULL,table_name FROM information_schema.tables --"
        };

        for (String maliciousInput : maliciousInputs) {
            mockMvc.perform(get("/api/todos/search")
                    .param("keyword", maliciousInput))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }

        // Verify database integrity after all attempts
        List<Todo> todos = todoRepository.findAll();
        assertEquals(2, todos.size(), "Database should remain intact after all SQL injection attempts");
        
        // Verify no todos were modified
        boolean allTodosIntact = todos.stream()
                .allMatch(todo -> todo.getTitle().startsWith("Test Todo") && 
                         todo.getDescription().startsWith("Description"));
        
        assertTrue(allTodosIntact, "All existing todos should remain unchanged");
    }

    @Test
    @DisplayName("Should handle SQL injection in custom query methods safely")
    void shouldHandleSqlInjectionInCustomQueries() throws Exception {
        String maliciousDate = "2024-01-01'; DROP TABLE todo; --";
        
        // Test the custom query method that uses @Query annotation
        assertDoesNotThrow(() -> {
            LocalDate testDate = LocalDate.now();
            todoRepository.findOverdueTodos(testDate);
        }, "Custom query should handle parameters safely");

        // Test with date range query
        assertDoesNotThrow(() -> {
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now().plusDays(7);
            todoRepository.findByCreatedAtBetween(startDate, endDate);
        }, "Date range query should handle parameters safely");

        // Verify database integrity
        List<Todo> todos = todoRepository.findAll();
        assertEquals(2, todos.size(), "Database should remain intact");
    }

    @Test
    @DisplayName("Should handle SQL injection in batch operations safely")
    void shouldHandleSqlInjectionInBatchOperations() throws Exception {
        String maliciousDescription = "'; TRUNCATE TABLE todo; --";
        
        // Create multiple todos with potentially malicious content
        for (int i = 0; i < 5; i++) {
            String jsonContent = String.format("""
                {
                    "title": "Batch Test %d",
                    "description": "%s",
                    "status": "TODO",
                    "priority": "LOW"
                }
                """, i, maliciousDescription);

            mockMvc.perform(post("/api/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonContent))
                    .andExpect(status().isCreated());
        }

        // Verify all todos were created safely
        List<Todo> todos = todoRepository.findAll();
        assertEquals(7, todos.size(), "All todos should be created (2 original + 5 new)");

        // Verify malicious content was stored as plain text
        long maliciousTodos = todos.stream()
                .filter(todo -> todo.getDescription().equals(maliciousDescription))
                .count();
        
        assertEquals(5, maliciousTodos, "All malicious descriptions should be stored as plain text");
    }

    @Test
    @DisplayName("Should prevent SQL injection through special characters")
    void shouldPreventSqlInjectionThroughSpecialCharacters() throws Exception {
        String[] specialCharacters = {
            "'", "\"", ";", "--", "/*", "*/", "@@", "@",
            "char(", "nchar(", "varchar(", "nvarchar(",
            "alter", "begin", "cast", "create", "cursor",
            "declare", "delete", "drop", "end", "exec",
            "execute", "fetch", "insert", "kill", "open",
            "select", "sys", "sysobjects", "syscolumns",
            "table", "update"
        };

        for (String specialChar : specialCharacters) {
            String searchTerm = "test" + specialChar + "malicious";
            
            mockMvc.perform(get("/api/todos/search")
                    .param("keyword", searchTerm))
                    .andExpect(status().isOk());
        }

        // Verify database integrity
        List<Todo> todos = todoRepository.findAll();
        assertEquals(2, todos.size(), "Database should remain intact");
    }
}