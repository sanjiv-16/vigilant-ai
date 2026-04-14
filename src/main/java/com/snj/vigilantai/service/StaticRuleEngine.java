package com.snj.vigilantai.service;
import com.snj.vigilantai.model.FileEntry;
import com.snj.vigilantai.model.RuleViolation;
import com.snj.vigilantai.model.StaticRule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class StaticRuleEngine {

    private List<StaticRule> rules = new ArrayList<>();
    private final ObjectMapper objectMapper;

    public StaticRuleEngine(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadRules() throws IOException {
        ClassPathResource resource = new ClassPathResource("rules.json");
        rules = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
        System.out.println("Loaded " + rules.size() + " static rules into Vigilant AI.");
    }

    public List<RuleViolation> scan(List<FileEntry> files) {
        List<RuleViolation> violations = new ArrayList<>();
        
        for (FileEntry file : files) {
            for (StaticRule rule : rules) {
                if (Pattern.compile(rule.pattern()).matcher(file.content()).find()) {
                    violations.add(new RuleViolation(
                        file.fileName(), 
                        rule.id(), 
                        rule.description(), 
                        rule.severity(),
                        rule.fatal()
                    ));
                }
            }
        }
        return violations;
    }
}
