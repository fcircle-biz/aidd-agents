package com.example.todoapp.controller;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.exception.TodoNotFoundException;
import com.example.todoapp.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TodoRestControllerのユニットテスト
 * 
 * @author System
 */
@WebMvcTest(TodoRestController.class)
@ActiveProfiles("test")
class TodoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Todo testTodo;
    private TodoRequest todoRequest;

    @BeforeEach
    void setUp() {
        testTodo = new Todo();
        testTodo.setId(1L);
        testTodo.setTitle("Test Todo");
        testTodo.setDescription("Test Description");
        testTodo.setStatus(TodoStatus.TODO);
        testTodo.setPriority(TodoPriority.HIGH);
        testTodo.setDueDate(LocalDate.of(2024, 12, 31));
        testTodo.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        testTodo.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));

        todoRequest = new TodoRequest();
        todoRequest.setTitle("New Todo");
        todoRequest.setDescription("New Description");
        todoRequest.setStatus(TodoStatus.TODO);
        todoRequest.setPriority(TodoPriority.MEDIUM);
        todoRequest.setDueDate(LocalDate.of(2024, 12, 25));
    }

    @Test
    void testGetAllTodos() throws Exception {
        List<Todo> todoList = Arrays.asList(testTodo);
        Page<Todo> todoPage = new PageImpl<>(todoList, PageRequest.of(0, 10), 1);

        when(todoService.findAll(any(Pageable.class))).thenReturn(todoPage);

        mockMvc.perform(get("/api/todos")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Todo"))
                .andExpect(jsonPath("$.content[0].description").value("Test Description"))
                .andExpect(jsonPath("$.content[0].status").value("TODO"))
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(todoService, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testGetTodoById() throws Exception {
        when(todoService.findById(1L)).thenReturn(testTodo);

        mockMvc.perform(get("/api/todos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Todo"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        verify(todoService, times(1)).findById(1L);
    }

    @Test
    void testGetTodoByIdNotFound() throws Exception {
        when(todoService.findById(999L)).thenThrow(new TodoNotFoundException(999L));

        mockMvc.perform(get("/api/todos/999"))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).findById(999L);
    }

    @Test
    void testCreateTodo() throws Exception {
        when(todoService.create(any(TodoRequest.class))).thenReturn(testTodo);

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(todoRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "/api/todos/1"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Todo"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(todoService, times(1)).create(any(TodoRequest.class));
    }

    @Test
    void testCreateTodoWithInvalidData() throws Exception {
        TodoRequest invalidRequest = new TodoRequest();
        // title is required but not set

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(todoService, never()).create(any(TodoRequest.class));
    }

    @Test
    void testUpdateTodo() throws Exception {
        when(todoService.update(eq(1L), any(TodoRequest.class))).thenReturn(testTodo);

        mockMvc.perform(put("/api/todos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(todoRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Todo"));

        verify(todoService, times(1)).update(eq(1L), any(TodoRequest.class));
    }

    @Test
    void testUpdateTodoNotFound() throws Exception {
        when(todoService.update(eq(999L), any(TodoRequest.class)))
                .thenThrow(new TodoNotFoundException(999L));

        mockMvc.perform(put("/api/todos/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(todoRequest)))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).update(eq(999L), any(TodoRequest.class));
    }

    @Test
    void testDeleteTodo() throws Exception {
        doNothing().when(todoService).delete(1L);

        mockMvc.perform(delete("/api/todos/1"))
                .andExpect(status().isNoContent());

        verify(todoService, times(1)).delete(1L);
    }

    @Test
    void testDeleteTodoNotFound() throws Exception {
        doThrow(new TodoNotFoundException(999L)).when(todoService).delete(999L);

        mockMvc.perform(delete("/api/todos/999"))
                .andExpect(status().isNotFound());

        verify(todoService, times(1)).delete(999L);
    }

    @Test
    void testSearchTodosWithAllParameters() throws Exception {
        List<Todo> searchResults = Arrays.asList(testTodo);
        when(todoService.search(any())).thenReturn(searchResults);

        mockMvc.perform(get("/api/todos/search")
                .param("keyword", "test")
                .param("status", "TODO")
                .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Todo"));

        verify(todoService, times(1)).search(any());
    }

    @Test
    void testSearchTodosWithKeywordOnly() throws Exception {
        List<Todo> searchResults = Arrays.asList(testTodo);
        when(todoService.search(any())).thenReturn(searchResults);

        mockMvc.perform(get("/api/todos/search")
                .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Todo"));

        verify(todoService, times(1)).search(any());
    }

    @Test
    void testSearchTodosWithInvalidStatus() throws Exception {
        List<Todo> searchResults = Arrays.asList(testTodo);
        when(todoService.search(any())).thenReturn(searchResults);

        mockMvc.perform(get("/api/todos/search")
                .param("keyword", "test")
                .param("status", "INVALID_STATUS"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(todoService, times(1)).search(any());
    }

    @Test
    void testSearchTodosWithInvalidPriority() throws Exception {
        List<Todo> searchResults = Arrays.asList(testTodo);
        when(todoService.search(any())).thenReturn(searchResults);

        mockMvc.perform(get("/api/todos/search")
                .param("keyword", "test")
                .param("priority", "INVALID_PRIORITY"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(todoService, times(1)).search(any());
    }

    @Test
    void testSearchTodosWithNoParameters() throws Exception {
        List<Todo> searchResults = Arrays.asList(testTodo);
        when(todoService.search(any())).thenReturn(searchResults);

        mockMvc.perform(get("/api/todos/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(todoService, times(1)).search(any());
    }
}