package com.example.todo.controller;



import com.example.todo.model.Todo;
import com.example.todo.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TodoController {

    @Autowired
    private TodoService todoService;

    // GET /todos – Fetch all todos.
    @GetMapping("/todos")
    public List<Todo> getTodos() {
        return todoService.getAllTodos();
    }

    // POST /todos – Add a new todo.
    // Expected request body: { "text": "Your todo text here" }
    @PostMapping("/todos")
    public ResponseEntity<?> addTodo(@RequestBody Todo todo) {
        if (todo.getText() == null || todo.getText().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Todo text is required.");
        }
        Todo addedTodo = todoService.addTodo(todo.getText());
        return ResponseEntity.ok(addedTodo);
    }

    // DELETE /todos/:id – Delete a todo.
    @DeleteMapping("/todos/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id) {
        boolean removed = todoService.deleteTodo(id);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Todo deleted successfully.");
    }

    // POST /summarize – Summarize todos and send to Slack.
    @PostMapping("/summarize")
    public ResponseEntity<String> summarizeTodos() {
        String result = todoService.sendSummaryToSlack();
        return ResponseEntity.ok(result);
    }
}
