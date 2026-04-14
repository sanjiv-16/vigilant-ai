package com.snj.vigilantai.model;

public record RuleViolation(
    String fileName,
    String ruleId, 
    String message, 
    String severity,
    boolean isFatal
) {}