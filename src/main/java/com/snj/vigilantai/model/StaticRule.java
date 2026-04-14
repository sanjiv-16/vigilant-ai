package com.snj.vigilantai.model;

public record StaticRule(
    String id, 
    String description, 
    String pattern, 
    String severity, 
    String suggestion,
    boolean fatal
) {}