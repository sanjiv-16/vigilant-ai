package com.snj.vigilantai.service;
import com.snj.vigilantai.model.AiInsights;
import com.snj.vigilantai.model.FileEntry;
import com.snj.vigilantai.model.RuleViolation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class AiReviewService {

    private final ChatClient chatClient;

    public AiReviewService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AiInsights analyzeProject(List<FileEntry> files, List<RuleViolation> existingViolations) {
        var outputConverter = new BeanOutputConverter<>(AiInsights.class);
        String format = outputConverter.getFormat();

        StringBuilder projectContext = new StringBuilder();
        for (FileEntry file : files) {
            projectContext.append("\n--- File: ").append(file.fileName()).append(" ---\n");
            projectContext.append(file.content()).append("\n");
        }

        String prompt = """
            You are Vigilant AI, an expert Java backend engineer.
            Review the following multi-file project. Pay attention to how the classes interact and depend on each other.
            
            We have already run static analysis and found these issues: %s
            
            Focus on deep logical bugs across file boundaries, security flaws, and architecture optimizations.
            Give the project a quality score from 0 to 100.
            
            CRITICAL INSTRUCTIONS:
            1. You must return ONLY a raw, valid JSON object.
            2. Do NOT include the "$schema" or property definitions in your output.
            3. Do NOT wrap the response in markdown blocks (e.g., ```json).
            4. Ensure all arrays and objects are properly closed.
            5. Do not output any conversational text before or after the JSON.
            
            Project Files:
            %s
            
            %s
            """.formatted(existingViolations.toString(), projectContext.toString(), format);

        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return outputConverter.convert(aiResponse);
    }
}