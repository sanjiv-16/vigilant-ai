package com.snj.vigilantai.model;
import java.util.List;

public record AiInsights(
    List<String> bugs,
    List<String> securityIssues,
    List<String> performanceTips,
    String overallSummary,
    int qualityScore
) {}
