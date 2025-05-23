package com.example.todo.service;



import com.example.todo.model.Todo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TodoService {

    private final List<Todo> todos = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    private final RestTemplate restTemplate = new RestTemplate();

    // Load the Slack webhook URL from application.properties
    @Value("${slack.webhook-url:}")
    private String slackWebhookUrl;

    public List<Todo> getAllTodos() {
        return todos;
    }

    public Todo addTodo(String text) {
        Todo todo = new Todo(counter.incrementAndGet(), text);
        todos.add(todo);
        return todo;
    }

    public boolean deleteTodo(Long id) {
        return todos.removeIf(t -> t.getId().equals(id));
    }

    public String sendSummaryToSlack() {
        if (todos.isEmpty()) {
            return "No todos to summarize.";
        }

        // Build the summary string
        StringBuilder summaryBuilder = new StringBuilder("To-Do Summary:\n");
        todos.forEach(todo -> summaryBuilder.append("- ").append(todo.getText()).append("\n"));
        String message = summaryBuilder.toString();

        // Check if the Slack webhook URL is configured
        if (slackWebhookUrl == null || slackWebhookUrl.isEmpty()) {
            return "Slack webhook URL is not configured.";
        }

        // Slack expects a JSON payload with a text property.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Escape quotes in the message if needed.
        String payload = "{\"text\": \"" + message.replace("\"", "\\\"") + "\"}";
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(slackWebhookUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return "Summary sent to Slack successfully.";
            } else {
                return "Failed to send summary to Slack. HTTP Status: " + response.getStatusCode();
            }
        } catch (Exception e) {
            return "Error sending summary to Slack: " + e.getMessage();
        }
    }
}
