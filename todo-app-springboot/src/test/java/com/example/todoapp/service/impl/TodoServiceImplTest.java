package com.example.todoapp.service.impl;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.dto.TodoSearchCriteria;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.exception.TodoNotFoundException;
import com.example.todoapp.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TodoServiceImplのユニットテスト
 * 
 * @author System
 */
@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoServiceImpl todoService;

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

        todoRequest = new TodoRequest();
        todoRequest.setTitle("New Todo");
        todoRequest.setDescription("New Description");
        todoRequest.setStatus(TodoStatus.TODO);
        todoRequest.setPriority(TodoPriority.MEDIUM);
        todoRequest.setDueDate(LocalDate.of(2024, 12, 25));
    }

    @Test
    void testFindAll() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Todo> todoList = Arrays.asList(testTodo);
        Page<Todo> todoPage = new PageImpl<>(todoList, pageable, 1);

        when(todoRepository.findAll(pageable)).thenReturn(todoPage);

        Page<Todo> result = todoService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testTodo.getTitle(), result.getContent().get(0).getTitle());
        verify(todoRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindByIdSuccess() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        Todo result = todoService.findById(1L);

        assertNotNull(result);
        assertEquals(testTodo.getId(), result.getId());
        assertEquals(testTodo.getTitle(), result.getTitle());
        verify(todoRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByIdNotFound() {
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TodoNotFoundException.class, () -> todoService.findById(999L));
        verify(todoRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateWithAllFields() {
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        Todo result = todoService.create(todoRequest);

        assertNotNull(result);
        assertEquals(testTodo.getId(), result.getId());
        assertEquals(testTodo.getTitle(), result.getTitle());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void testCreateWithDefaults() {
        TodoRequest requestWithDefaults = new TodoRequest();
        requestWithDefaults.setTitle("Simple Todo");
        requestWithDefaults.setDescription("Simple Description");
        // status and priority are null, should use defaults

        Todo todoWithDefaults = new Todo();
        todoWithDefaults.setId(2L);
        todoWithDefaults.setTitle("Simple Todo");
        todoWithDefaults.setDescription("Simple Description");
        todoWithDefaults.setStatus(TodoStatus.TODO);
        todoWithDefaults.setPriority(TodoPriority.MEDIUM);

        when(todoRepository.save(any(Todo.class))).thenReturn(todoWithDefaults);

        Todo result = todoService.create(requestWithDefaults);

        assertNotNull(result);
        assertEquals(TodoStatus.TODO, result.getStatus());
        assertEquals(TodoPriority.MEDIUM, result.getPriority());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void testUpdateSuccess() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        Todo result = todoService.update(1L, todoRequest);

        assertNotNull(result);
        verify(todoRepository, times(1)).findById(1L);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void testUpdateNotFound() {
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TodoNotFoundException.class, () -> todoService.update(999L, todoRequest));
        verify(todoRepository, times(1)).findById(999L);
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void testUpdateWithNullFields() {
        TodoRequest requestWithNulls = new TodoRequest();
        requestWithNulls.setTitle("Updated Title");
        requestWithNulls.setDescription("Updated Description");
        // status and priority are null, should keep existing values

        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(testTodo);

        Todo result = todoService.update(1L, requestWithNulls);

        assertNotNull(result);
        verify(todoRepository, times(1)).findById(1L);
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    void testDeleteSuccess() {
        when(todoRepository.existsById(1L)).thenReturn(true);

        todoService.delete(1L);

        verify(todoRepository, times(1)).existsById(1L);
        verify(todoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNotFound() {
        when(todoRepository.existsById(999L)).thenReturn(false);

        assertThrows(TodoNotFoundException.class, () -> todoService.delete(999L));
        verify(todoRepository, times(1)).existsById(999L);
        verify(todoRepository, never()).deleteById(999L);
    }

    @Test
    void testSearchWithKeywordAndStatus() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setKeyword("test");
        criteria.setStatus(TodoStatus.TODO);

        List<Todo> expectedResults = Arrays.asList(testTodo);
        when(todoRepository.findByTitleContainingOrDescriptionContainingAndStatus("test", "test", TodoStatus.TODO))
                .thenReturn(expectedResults);

        List<Todo> result = todoService.search(criteria);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTodo.getTitle(), result.get(0).getTitle());
        verify(todoRepository, times(1))
                .findByTitleContainingOrDescriptionContainingAndStatus("test", "test", TodoStatus.TODO);
    }

    @Test
    void testSearchWithKeywordOnly() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setKeyword("test");

        List<Todo> expectedResults = Arrays.asList(testTodo);
        when(todoRepository.findByTitleContainingOrDescriptionContaining("test", "test"))
                .thenReturn(expectedResults);

        List<Todo> result = todoService.search(criteria);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTodo.getTitle(), result.get(0).getTitle());
        verify(todoRepository, times(1))
                .findByTitleContainingOrDescriptionContaining("test", "test");
    }

    @Test
    void testSearchWithStatusOnly() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setStatus(TodoStatus.TODO);

        List<Todo> expectedResults = Arrays.asList(testTodo);
        when(todoRepository.findByStatus(TodoStatus.TODO))
                .thenReturn(expectedResults);

        List<Todo> result = todoService.search(criteria);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTodo.getTitle(), result.get(0).getTitle());
        verify(todoRepository, times(1)).findByStatus(TodoStatus.TODO);
    }

    @Test
    void testSearchWithNoCriteria() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();

        List<Todo> expectedResults = Arrays.asList(testTodo);
        when(todoRepository.findAll()).thenReturn(expectedResults);

        List<Todo> result = todoService.search(criteria);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTodo.getTitle(), result.get(0).getTitle());
        verify(todoRepository, times(1)).findAll();
    }

    @Test
    void testSearchWithEmptyKeyword() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setKeyword("   "); // whitespace only
        criteria.setStatus(TodoStatus.TODO);

        List<Todo> expectedResults = Arrays.asList(testTodo);
        when(todoRepository.findByStatus(TodoStatus.TODO))
                .thenReturn(expectedResults);

        List<Todo> result = todoService.search(criteria);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(todoRepository, times(1)).findByStatus(TodoStatus.TODO);
        verify(todoRepository, never()).findByTitleContainingOrDescriptionContainingAndStatus(anyString(), anyString(), any());
    }

    @Test
    void testFindByStatus() {
        List<Todo> expectedResults = Arrays.asList(testTodo);
        when(todoRepository.findByStatus(TodoStatus.TODO)).thenReturn(expectedResults);

        List<Todo> result = todoService.findByStatus(TodoStatus.TODO);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTodo.getTitle(), result.get(0).getTitle());
        verify(todoRepository, times(1)).findByStatus(TodoStatus.TODO);
    }

    @Test
    void testFindOverdueTodos() {
        List<Todo> expectedResults = Arrays.asList(testTodo);
        when(todoRepository.findByDueDateBeforeAndStatusNot(any(LocalDate.class), eq(TodoStatus.DONE)))
                .thenReturn(expectedResults);

        List<Todo> result = todoService.findOverdueTodos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTodo.getTitle(), result.get(0).getTitle());
        verify(todoRepository, times(1))
                .findByDueDateBeforeAndStatusNot(any(LocalDate.class), eq(TodoStatus.DONE));
    }
}