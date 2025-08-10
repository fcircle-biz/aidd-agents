package com.example.todoapp.security;

import com.example.todoapp.dto.TodoRequest;
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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * XSS Protection Tests for the Todo application.
 * 
 * This test class specifically verifies that XSS attacks are prevented through:
 * - Thymeleaf's automatic HTML escaping
 * - Proper content type handling
 * - Security headers
 * - Input sanitization
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("XSS Protection Tests")
@Transactional
class XssProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    private Todo testTodoWithXss;

    @BeforeEach
    void setUp() {
        // Clear existing data and create test data with potential XSS content
        todoRepository.deleteAll();
        
        testTodoWithXss = new Todo();
        testTodoWithXss.setTitle("<script>alert('XSS in title')</script>");
        testTodoWithXss.setDescription("<img src=x onerror=alert('XSS in description')>");
        testTodoWithXss.setStatus(TodoStatus.TODO);
        testTodoWithXss.setPriority(TodoPriority.HIGH);
        testTodoWithXss = todoRepository.save(testTodoWithXss);
    }

    @Test
    @DisplayName("Should escape XSS content in todo list view")
    void shouldEscapeXssInListView() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                // Check that script tags are escaped and not executable
                .andExpect(content().string(not(containsString("<script>alert('XSS in title')</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;alert(&#39;XSS in title&#39;)&lt;/script&gt;")))
                // Check that img tags with onerror are escaped
                .andExpect(content().string(not(containsString("<img src=x onerror=alert('XSS in description')>"))))
                .andExpect(content().string(containsString("&lt;img src=x onerror=alert(&#39;XSS in description&#39;)&gt;")));
    }

    @Test
    @DisplayName("Should escape XSS content in todo detail view")
    void shouldEscapeXssInDetailView() throws Exception {
        mockMvc.perform(get("/todos/" + testTodoWithXss.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                // Verify XSS content is properly escaped in detail view
                .andExpect(content().string(not(containsString("<script>alert('XSS in title')</script>"))))
                .andExpect(content().string(not(containsString("<img src=x onerror=alert('XSS in description')>"))))
                // Check that content is escaped but still displayed
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(containsString("&lt;img")));
    }

    @Test
    @DisplayName("Should escape XSS content in edit form")
    void shouldEscapeXssInEditForm() throws Exception {
        mockMvc.perform(get("/todos/" + testTodoWithXss.getId() + "/edit"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                // Form inputs should have escaped values
                .andExpect(content().string(containsString("value=\"&lt;script&gt;alert(&#39;XSS in title&#39;)&lt;/script&gt;\"")))
                .andExpect(content().string(not(containsString("value=\"<script>alert('XSS in title')</script>\""))));
    }

    @Test
    @DisplayName("Should handle XSS in search results")
    void shouldHandleXssInSearchResults() throws Exception {
        mockMvc.perform(get("/todos/search")
                .param("keyword", "<script>alert('search XSS')</script>")
                .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                // Search keyword should be escaped in the response
                .andExpect(content().string(not(containsString("<script>alert('search XSS')</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    @DisplayName("Should handle XSS in flash messages")
    void shouldHandleXssInFlashMessages() throws Exception {
        // This test verifies that flash messages are also properly escaped
        // Flash messages are typically set by controllers after operations
        
        String xssTitle = "<script>alert('flash XSS')</script>Test Todo";
        
        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                    {
                        "title": "%s",
                        "description": "Test description",
                        "status": "TODO",
                        "priority": "MEDIUM"
                    }
                    """, xssTitle)))
                .andExpect(status().isCreated());

        // Now check that the created todo is properly escaped in the list view
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert('flash XSS')</script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    @DisplayName("Should prevent XSS through HTTP headers")
    void shouldPreventXssthroughHeaders() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                // X-Content-Type-Options prevents MIME type sniffing
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                // Content-Security-Policy helps prevent XSS
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().string("Content-Security-Policy", 
                    containsString("script-src 'self' 'unsafe-inline'")))
                // X-Frame-Options prevents clickjacking (related to XSS)
                .andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
    }

    @Test
    @DisplayName("Should handle multiple XSS vectors safely")
    void shouldHandleMultipleXssVectors() throws Exception {
        // Test various XSS payloads
        String[] xssPayloads = {
            "<script>alert('XSS1')</script>",
            "javascript:alert('XSS2')",
            "<img src=x onerror=alert('XSS3')>",
            "<svg onload=alert('XSS4')>",
            "\"onclick=alert('XSS5')",
            "<iframe src=javascript:alert('XSS6')>"
        };

        for (String payload : xssPayloads) {
            Todo xssTodo = new Todo();
            xssTodo.setTitle("Safe Title");
            xssTodo.setDescription(payload);
            xssTodo.setStatus(TodoStatus.TODO);
            xssTodo.setPriority(TodoPriority.LOW);
            xssTodo = todoRepository.save(xssTodo);

            mockMvc.perform(get("/todos/" + xssTodo.getId()))
                    .andExpect(status().isOk())
                    // Should not contain the original payload
                    .andExpect(content().string(not(containsString(payload))))
                    // Should contain escaped version (at least the opening bracket should be escaped)
                    .andExpect(content().string(containsString("&lt;")));
        }
    }

    @Test
    @DisplayName("Should handle XSS in URL parameters")
    void shouldHandleXssInUrlParameters() throws Exception {
        String xssParam = "<script>alert('URL XSS')</script>";
        
        mockMvc.perform(get("/todos/search")
                .param("keyword", xssParam))
                .andExpect(status().isOk())
                // URL parameters used in page should be escaped
                .andExpect(content().string(not(containsString(xssParam))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test
    @DisplayName("Should maintain content integrity while escaping XSS")
    void shouldMaintainContentIntegrityWhileEscaping() throws Exception {
        // Test that legitimate HTML-like content in descriptions is preserved but escaped
        Todo htmlContentTodo = new Todo();
        htmlContentTodo.setTitle("HTML Content Test");
        htmlContentTodo.setDescription("This description contains <b>bold text</b> and <i>italic text</i>");
        htmlContentTodo.setStatus(TodoStatus.TODO);
        htmlContentTodo.setPriority(TodoPriority.MEDIUM);
        htmlContentTodo = todoRepository.save(htmlContentTodo);

        mockMvc.perform(get("/todos/" + htmlContentTodo.getId()))
                .andExpect(status().isOk())
                // HTML tags should be escaped (not rendered as HTML)
                .andExpect(content().string(not(containsString("<b>bold text</b>"))))
                .andExpect(content().string(not(containsString("<i>italic text</i>"))))
                // But the content should still be visible as text
                .andExpect(content().string(containsString("&lt;b&gt;bold text&lt;/b&gt;")))
                .andExpect(content().string(containsString("&lt;i&gt;italic text&lt;/i&gt;")));
    }
}