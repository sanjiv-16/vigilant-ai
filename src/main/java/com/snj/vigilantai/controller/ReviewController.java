package com.snj.vigilantai.controller;

import com.snj.vigilantai.model.AiInsights;
import com.snj.vigilantai.model.FileEntry;
import com.snj.vigilantai.model.ReviewRequest;
import com.snj.vigilantai.model.ReviewResponse;
import com.snj.vigilantai.model.RuleViolation;
import com.snj.vigilantai.service.AiReviewService;
import com.snj.vigilantai.service.StaticRuleEngine;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/v1/review")
public class ReviewController {

    private final StaticRuleEngine ruleEngine;
    private final AiReviewService aiReviewService;

    public ReviewController(StaticRuleEngine ruleEngine, AiReviewService aiReviewService) {
        this.ruleEngine = ruleEngine;
        this.aiReviewService = aiReviewService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ReviewResponse reviewCodeJson(@RequestBody ReviewRequest request) {
        return processReview(request.files());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReviewResponse reviewCodeZip(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".zip")) {
            throw new IllegalArgumentException("Please upload a valid .zip file containing the source code.");
        }

        List<FileEntry> files = extractZipInMemory(file);
        return processReview(files);
    }

    private ReviewResponse processReview(List<FileEntry> files) {
        List<RuleViolation> violations = ruleEngine.scan(files);
        boolean hasFatalErrors = violations.stream().anyMatch(RuleViolation::isFatal);

        if (hasFatalErrors) {
            return new ReviewResponse(
                0, 
                "F (Fatal Basic Rule Violated)", 
                violations, 
                new AiInsights(List.of(), List.of(), List.of(), "AI Analysis skipped due to fatal static violations.", 0)
            );
        }

        AiInsights aiInsights = aiReviewService.analyzeProject(files, violations);
        int finalScore = calculateFinalScore(violations, aiInsights.qualityScore());
        
        return new ReviewResponse(finalScore, assignGrade(finalScore), violations, aiInsights);
    }

    private List<FileEntry> extractZipInMemory(MultipartFile file) {
        List<FileEntry> extractedFiles = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {
                if (!zipEntry.isDirectory() && isSourceCodeFile(zipEntry.getName())) {
                    String content = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));
                    
                    extractedFiles.add(new FileEntry(zipEntry.getName(), content));
                }
                zipEntry = zis.getNextEntry();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process the zip file", e);
        }

        return extractedFiles;
    }

    private boolean isSourceCodeFile(String fileName) {
        return fileName.endsWith(".java") || 
               fileName.endsWith(".xml") || 
               fileName.endsWith(".yml") || 
               fileName.endsWith(".properties");
    }

    private int calculateFinalScore(List<RuleViolation> violations, int aiScore) {
        int penalty = 0;
        for (RuleViolation v : violations) {
            penalty += v.severity().equals("HIGH") ? 15 : 5;
        }
        return Math.max(0, aiScore - penalty);
    }

    private String assignGrade(int score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        return "F";
    }
}