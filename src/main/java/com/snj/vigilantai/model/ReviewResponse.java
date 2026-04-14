package com.snj.vigilantai.model;
import java.util.List;

public record ReviewResponse(
    int finalScore,
    String grade,
    List<RuleViolation> staticViolations,
    AiInsights aiInsights
) {}