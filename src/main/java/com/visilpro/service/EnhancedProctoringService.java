package com.visilpro.service;

import com.visilpro.model.MalpracticeEvent;
import com.visilpro.repository.MalpracticeEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EnhancedProctoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedProctoringService.class);
    
    @Autowired
    private GeminiRESTService geminiRESTService;
    
    @Autowired
    private MalpracticeEventRepository malpracticeEventRepository;
    
    // Store analysis results temporarily for correlation
    private final Map<String, List<Map<String, Object>>> sessionAnalysis = new ConcurrentHashMap<>();
    
    // Risk thresholds
    private static final int HIGH_RISK_THRESHOLD = 75;
    private static final int MEDIUM_RISK_THRESHOLD = 50;
    private static final int VIOLATION_COUNT_THRESHOLD = 3;

    
    public CompletableFuture<Map<String, Object>> analyzeImageAsync(String sessionId, String base64Image) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> analysis = geminiRESTService.analyzeImageForProctoring(base64Image);
                storeAnalysisResult(sessionId, analysis);
                
                // Check if immediate action is needed
                if (isHighRiskViolation(analysis)) {
                    triggerImmediateAlert(sessionId, analysis);
                }
                
                return analysis;
            } catch (Exception e) {
                logger.error("Error in async image analysis for session {}: {}", sessionId, e.getMessage());
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", e.getMessage());
                errorResult.put("violation", false);
                return errorResult;
            }
        });
    }
    
    public CompletableFuture<Map<String, Object>> analyzeAudioAsync(String sessionId, String base64Audio) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> analysis = geminiRESTService.analyzeAudioForProctoring(base64Audio);
                storeAnalysisResult(sessionId, analysis);
                
                // Check if immediate action is needed
                if (isHighRiskViolation(analysis)) {
                    triggerImmediateAlert(sessionId, analysis);
                }
                
                return analysis;
            } catch (Exception e) {
                logger.error("Error in async audio analysis for session {}: {}", sessionId, e.getMessage());
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", e.getMessage());
                errorResult.put("violation", false);
                return errorResult;
            }
        });
    }
    
    public CompletableFuture<Map<String, Object>> analyzeMultimodalAsync(String sessionId, String base64Image, String base64Audio) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Object> analysis = geminiRESTService.analyzeMultimodalData(base64Image, base64Audio);
                storeAnalysisResult(sessionId, analysis);
                
                // Enhanced risk assessment for multimodal data
                if (isHighRiskViolation(analysis) || isCriticalRisk(analysis)) {
                    triggerImmediateAlert(sessionId, analysis);
                }
                
                return analysis;
            } catch (Exception e) {
                logger.error("Error in async multimodal analysis for session {}: {}", sessionId, e.getMessage());
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", e.getMessage());
                errorResult.put("violation", false);
                return errorResult;
            }
        });
    }
    
    public Map<String, Object> getSessionAnalyticsSummary(String sessionId) {
        List<Map<String, Object>> analyses = sessionAnalysis.getOrDefault(sessionId, new ArrayList<>());
        
        Map<String, Object> summary = new HashMap<>();
        
        int totalAnalyses = analyses.size();
        int violationCount = 0;
        int highRiskCount = 0;
        int mediumRiskCount = 0;
        
        List<String> allIssues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        for (Map<String, Object> analysis : analyses) {
            if (Boolean.TRUE.equals(analysis.get("violation"))) {
                violationCount++;
            }
            
            Integer confidence = (Integer) analysis.get("confidence");
            if (confidence != null) {
                if (confidence >= HIGH_RISK_THRESHOLD) {
                    highRiskCount++;
                } else if (confidence >= MEDIUM_RISK_THRESHOLD) {
                    mediumRiskCount++;
                }
            }
            
            String issues = (String) analysis.get("issues");
            if (issues != null && !issues.isEmpty()) {
                allIssues.add(issues);
            }
            
            String recommendation = (String) analysis.get("recommendation");
            if (recommendation != null && !recommendation.isEmpty()) {
                recommendations.add(recommendation);
            }
        }
        
        // Calculate overall risk score
        double violationRate = totalAnalyses > 0 ? (double) violationCount / totalAnalyses : 0;
        double highRiskRate = totalAnalyses > 0 ? (double) highRiskCount / totalAnalyses : 0;
        
        String overallRisk = "LOW";
        if (violationRate > 0.5 || highRiskRate > 0.3) {
            overallRisk = "HIGH";
        } else if (violationRate > 0.2 || highRiskRate > 0.1) {
            overallRisk = "MEDIUM";
        }
        
        summary.put("sessionId", sessionId);
        summary.put("totalAnalyses", totalAnalyses);
        summary.put("violationCount", violationCount);
        summary.put("violationRate", Math.round(violationRate * 100));
        summary.put("highRiskCount", highRiskCount);
        summary.put("mediumRiskCount", mediumRiskCount);
        summary.put("overallRisk", overallRisk);
        summary.put("allIssues", allIssues);
        summary.put("recommendations", recommendations);
        summary.put("needsReview", violationCount >= VIOLATION_COUNT_THRESHOLD || "HIGH".equals(overallRisk));
        summary.put("generatedAt", LocalDateTime.now());
        
        return summary;
    }
    
    private void storeAnalysisResult(String sessionId, Map<String, Object> analysis) {
        sessionAnalysis.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(analysis);
        
        // Keep only last 50 analyses per session to prevent memory issues
        List<Map<String, Object>> sessionResults = sessionAnalysis.get(sessionId);
        if (sessionResults.size() > 50) {
            sessionResults.remove(0);
        }
    }
    
    private boolean isHighRiskViolation(Map<String, Object> analysis) {
        Boolean violation = (Boolean) analysis.get("violation");
        Integer confidence = (Integer) analysis.get("confidence");
        
        return Boolean.TRUE.equals(violation) && 
               confidence != null && 
               confidence >= HIGH_RISK_THRESHOLD;
    }
    
    private boolean isCriticalRisk(Map<String, Object> analysis) {
        String riskLevel = (String) analysis.get("riskLevel");
        return "CRITICAL".equalsIgnoreCase(riskLevel);
    }
    
    private void triggerImmediateAlert(String sessionId, Map<String, Object> analysis) {
        try {
            // Create malpractice event record
            MalpracticeEvent event = new MalpracticeEvent();
            event.setSessionId(Long.parseLong(sessionId));
            event.setType("AI_DETECTED_VIOLATION");
            event.setTimestamp(LocalDateTime.now());
            event.setConfidenceScore((Integer) analysis.get("confidence"));
            event.setSnapshotUrl("gemini-analysis-" + System.currentTimeMillis());
            
            malpracticeEventRepository.save(event);
            
            logger.warn("High-risk violation detected for session {}: {}", sessionId, analysis.get("issues"));
            
        } catch (Exception e) {
            logger.error("Error creating malpractice event for session {}: {}", sessionId, e.getMessage());
        }
    }
    
    public void clearSessionAnalysis(String sessionId) {
        sessionAnalysis.remove(sessionId);
        logger.info("Cleared analysis data for session: {}", sessionId);
    }
    
    public List<Map<String, Object>> getRecentViolations(String sessionId, int limit) {
        return sessionAnalysis.getOrDefault(sessionId, new ArrayList<>())
                .stream()
                .filter(analysis -> Boolean.TRUE.equals(analysis.get("violation")))
                .sorted((a, b) -> Long.compare((Long) b.get("timestamp"), (Long) a.get("timestamp")))
                .limit(limit)
                .toList();
    }
}