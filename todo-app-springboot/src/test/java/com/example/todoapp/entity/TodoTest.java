package com.example.todoapp.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Todoエンティティのユニットテスト
 * 
 * @author System
 */
class TodoTest {

    private Todo todo;

    @BeforeEach
    void setUp() {
        todo = new Todo();
        todo.setId(1L);
        todo.setTitle("Test Todo");
        todo.setDescription("Test Description");
        todo.setStatus(TodoStatus.TODO);
        todo.setPriority(TodoPriority.HIGH);
        todo.setDueDate(LocalDate.of(2024, 12, 31));
        todo.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
        todo.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
    }

    @Test
    void testDefaultConstructor() {
        Todo newTodo = new Todo();
        assertNull(newTodo.getId());
        assertNull(newTodo.getTitle());
        assertNull(newTodo.getDescription());
        assertEquals(TodoStatus.TODO, newTodo.getStatus());
        assertEquals(TodoPriority.MEDIUM, newTodo.getPriority());
        assertNull(newTodo.getDueDate());
        assertNull(newTodo.getCreatedAt());
        assertNull(newTodo.getUpdatedAt());
    }

    @Test
    void testConstructorWithTitleAndDescription() {
        Todo newTodo = new Todo("Sample Title", "Sample Description");
        assertEquals("Sample Title", newTodo.getTitle());
        assertEquals("Sample Description", newTodo.getDescription());
        assertEquals(TodoStatus.TODO, newTodo.getStatus());
        assertEquals(TodoPriority.MEDIUM, newTodo.getPriority());
    }

    @Test
    void testConstructorWithAllParameters() {
        Todo newTodo = new Todo("Sample Title", "Sample Description", TodoStatus.IN_PROGRESS, TodoPriority.HIGH);
        assertEquals("Sample Title", newTodo.getTitle());
        assertEquals("Sample Description", newTodo.getDescription());
        assertEquals(TodoStatus.IN_PROGRESS, newTodo.getStatus());
        assertEquals(TodoPriority.HIGH, newTodo.getPriority());
    }

    @Test
    void testGettersAndSetters() {
        // Test ID
        todo.setId(2L);
        assertEquals(2L, todo.getId());

        // Test Title
        todo.setTitle("Updated Title");
        assertEquals("Updated Title", todo.getTitle());

        // Test Description
        todo.setDescription("Updated Description");
        assertEquals("Updated Description", todo.getDescription());

        // Test Status
        todo.setStatus(TodoStatus.DONE);
        assertEquals(TodoStatus.DONE, todo.getStatus());

        // Test Priority
        todo.setPriority(TodoPriority.LOW);
        assertEquals(TodoPriority.LOW, todo.getPriority());

        // Test Due Date
        LocalDate newDueDate = LocalDate.of(2025, 6, 15);
        todo.setDueDate(newDueDate);
        assertEquals(newDueDate, todo.getDueDate());

        // Test Created At
        LocalDateTime newCreatedAt = LocalDateTime.of(2024, 6, 1, 12, 30, 0);
        todo.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, todo.getCreatedAt());

        // Test Updated At
        LocalDateTime newUpdatedAt = LocalDateTime.of(2024, 6, 2, 14, 45, 0);
        todo.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, todo.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Todo todo1 = new Todo();
        todo1.setId(1L);
        todo1.setTitle("Test Todo");

        Todo todo2 = new Todo();
        todo2.setId(1L);
        todo2.setTitle("Different Title");

        Todo todo3 = new Todo();
        todo3.setId(2L);
        todo3.setTitle("Test Todo");

        // Same object should be equal
        assertEquals(todo1, todo1);
        
        // Objects with same ID should be equal
        assertEquals(todo1, todo2);
        assertEquals(todo1.hashCode(), todo2.hashCode());
        
        // Objects with different ID should not be equal
        assertNotEquals(todo1, todo3);
        
        // Null comparison
        assertNotEquals(todo1, null);
        
        // Different class comparison
        assertNotEquals(todo1, "Not a Todo");
        
        // Both objects with null ID should be equal (as per equals implementation using Objects.equals)
        Todo todoWithNullId1 = new Todo();
        Todo todoWithNullId2 = new Todo();
        assertEquals(todoWithNullId1, todoWithNullId1); // Same instance
        assertEquals(todoWithNullId1, todoWithNullId2); // Different instances with null ID are equal
        assertEquals(todoWithNullId1.hashCode(), todoWithNullId2.hashCode()); // Hash codes should also be equal
    }

    @Test
    void testToString() {
        String toStringResult = todo.toString();
        
        // Check that toString contains all key information
        assertTrue(toStringResult.contains("Todo{"));
        assertTrue(toStringResult.contains("id=1"));
        assertTrue(toStringResult.contains("title='Test Todo'"));
        assertTrue(toStringResult.contains("description='Test Description'"));
        assertTrue(toStringResult.contains("status=TODO"));
        assertTrue(toStringResult.contains("priority=HIGH"));
        assertTrue(toStringResult.contains("dueDate=2024-12-31"));
        assertTrue(toStringResult.contains("createdAt=2024-01-01T10:00"));
        assertTrue(toStringResult.contains("updatedAt=2024-01-01T10:00"));
    }

    @Test
    void testNullValues() {
        Todo todoWithNulls = new Todo();
        todoWithNulls.setId(null);
        todoWithNulls.setTitle(null);
        todoWithNulls.setDescription(null);
        todoWithNulls.setDueDate(null);
        todoWithNulls.setCreatedAt(null);
        todoWithNulls.setUpdatedAt(null);
        
        assertNull(todoWithNulls.getId());
        assertNull(todoWithNulls.getTitle());
        assertNull(todoWithNulls.getDescription());
        assertNull(todoWithNulls.getDueDate());
        assertNull(todoWithNulls.getCreatedAt());
        assertNull(todoWithNulls.getUpdatedAt());
        
        // Status and Priority should have defaults
        assertEquals(TodoStatus.TODO, todoWithNulls.getStatus());
        assertEquals(TodoPriority.MEDIUM, todoWithNulls.getPriority());
        
        // toString should handle nulls gracefully
        String toStringResult = todoWithNulls.toString();
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("Todo{"));
    }
}