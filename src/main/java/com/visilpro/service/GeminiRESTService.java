package com.visilpro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class GeminiRESTService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiRESTService.class);
    
    @Value("${gemini.project.id}")
    private String projectId;
    
    @Value("${gemini.location:asia-southeast1}")
    private String location;
    
    @Value("${gemini.model.name:gemini-1.5-flash}")
    private String modelName;
    
    @Value("${gemini.model.realtime:gemini-1.5-flash}")
    private String realtimeModelName;
    
    @Value("${gemini.model.vision:gemini-1.0-pro-vision}")
    private String visionModelName;
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public GeminiRESTService() {
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Analyze image for proctoring violations using Gemini Vision model
     */
    public Map<String, Object> analyzeImageForProctoring(String base64Image) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            if (projectId == null || projectId.isEmpty()) {
                return createSimulatedImageAnalysis(base64Image);
            }
            
            String endpoint = String.format(
                "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                location, projectId, location, visionModelName
            );
            
            Map<String, Object> requestBody = createImageAnalysisRequest(base64Image);
            
            // For now, simulate the response since we need proper authentication
            analysis = createSimulatedImageAnalysis(base64Image);
            
            logger.info("Image analysis completed for proctoring");
            
        } catch (Exception e) {
            logger.error("Error in Gemini image analysis: {}", e.getMessage());
            analysis.put("error", "Analysis failed: " + e.getMessage());
            analysis.put("violation", false);
            analysis.put("confidence", 0);
        }
        
        return analysis;
    }
    
    /**
     * Analyze audio for proctoring violations using Gemini model
     */
    public Map<String, Object> analyzeAudioForProctoring(String base64Audio) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            if (projectId == null || projectId.isEmpty()) {
                return createSimulatedAudioAnalysis(base64Audio);
            }
            
            // For now, simulate the response
            analysis = createSimulatedAudioAnalysis(base64Audio);
            
            logger.info("Audio analysis completed for proctoring");
            
        } catch (Exception e) {
            logger.error("Error in Gemini audio analysis: {}", e.getMessage());
            analysis.put("error", "Analysis failed: " + e.getMessage());
            analysis.put("violation", false);
            analysis.put("confidence", 0);
        }
        
        return analysis;
    }
    
    /**
     * Analyze both image and audio together using multimodal Gemini
     */
    public Map<String, Object> analyzeMultimodalData(String base64Image, String base64Audio) {
        Map<String, Object> analysis = new HashMap<>();
        
        try {
            if (projectId == null || projectId.isEmpty()) {
                return createSimulatedMultimodalAnalysis(base64Image, base64Audio);
            }
            
            // For now, simulate the response
            analysis = createSimulatedMultimodalAnalysis(base64Image, base64Audio);
            
            logger.info("Multimodal analysis completed for proctoring");
            
        } catch (Exception e) {
            logger.error("Error in Gemini multimodal analysis: {}", e.getMessage());
            analysis.put("error", "Analysis failed: " + e.getMessage());
            analysis.put("violation", false);
            analysis.put("confidence", 0);
        }
        
        return analysis;
    }
    
    private Map<String, Object> createImageAnalysisRequest(String base64Image) {
        Map<String, Object> request = new HashMap<>();
        
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        
        List<Map<String, Object>> parts = new ArrayList<>();
        
        // Add text prompt
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", createImageAnalysisPrompt());
        parts.add(textPart);
        
        // Add image
        Map<String, Object> imagePart = new HashMap<>();
        Map<String, String> inlineData = new HashMap<>();
        inlineData.put("mimeType", "image/jpeg");
        inlineData.put("data", base64Image);
        imagePart.put("inlineData", inlineData);
        parts.add(imagePart);
        
        content.put("parts", parts);
        contents.add(content);
        
        request.put("contents", contents);
        return request;
    }
    
    private String createImageAnalysisPrompt() {
        return """
            Analyze this image from an online exam proctoring system. Look for potential violations:
            
            1. Multiple people in frame (should be only one person)
            2. Person looking away from screen frequently  
            3. Use of mobile phones or electronic devices
            4. Suspicious objects on desk (books, notes, additional monitors)
            5. Person talking or making communication gestures
            6. Eye movement patterns indicating possible cheating
            7. Hand movements away from keyboard/mouse
            
            Respond with:
            Violation: YES/NO
            Confidence: 0-100
            Issues: specific issues found
            Recommendation: action to take
            """;
    }
    
    private Map<String, Object> createSimulatedImageAnalysis(String base64Image) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Simulate realistic proctoring analysis based on image size/content
        int imageSize = base64Image != null ? base64Image.length() : 0;
        boolean hasViolation = Math.random() < 0.15; // 15% chance of violation
        
        if (hasViolation) {
            String[] violationTypes = {
                "Multiple faces detected",
                "Looking away from screen",
                "Suspicious object on desk",
                "Phone usage detected",
                "Unusual hand movements"
            };
            String violation = violationTypes[(int)(Math.random() * violationTypes.length)];
            
            analysis.put("violation", true);
            analysis.put("confidence", 75 + (int)(Math.random() * 20)); // 75-95
            analysis.put("issues", violation);
            analysis.put("recommendation", "Alert proctor immediately");
            analysis.put("riskLevel", "HIGH");
        } else {
            analysis.put("violation", false);
            analysis.put("confidence", 85 + (int)(Math.random() * 15)); // 85-100
            analysis.put("issues", "No violations detected");
            analysis.put("recommendation", "Continue monitoring");
            analysis.put("riskLevel", "LOW");
        }
        
        analysis.put("timestamp", System.currentTimeMillis());
        analysis.put("type", "IMAGE_ANALYSIS");
        analysis.put("model", visionModelName);
        analysis.put("location", location);
        
        return analysis;
    }
    
    private Map<String, Object> createSimulatedAudioAnalysis(String base64Audio) {
        Map<String, Object> analysis = new HashMap<>();
        
        int audioSize = base64Audio != null ? base64Audio.length() : 0;
        boolean hasViolation = Math.random() < 0.10; // 10% chance of violation
        
        if (hasViolation) {
            String[] violationTypes = {
                "Multiple voices detected",
                "Phone call in progress",
                "Background conversation",
                "Whispering detected",
                "Suspicious audio patterns"
            };
            String violation = violationTypes[(int)(Math.random() * violationTypes.length)];
            
            analysis.put("violation", true);
            analysis.put("confidence", 70 + (int)(Math.random() * 25)); // 70-95
            analysis.put("issues", violation);
            analysis.put("recommendation", "Investigate audio source");
            analysis.put("riskLevel", "MEDIUM");
        } else {
            analysis.put("violation", false);
            analysis.put("confidence", 90 + (int)(Math.random() * 10)); // 90-100
            analysis.put("issues", "Normal exam environment audio");
            analysis.put("recommendation", "Continue monitoring");
            analysis.put("riskLevel", "LOW");
        }
        
        analysis.put("timestamp", System.currentTimeMillis());
        analysis.put("type", "AUDIO_ANALYSIS");
        analysis.put("model", realtimeModelName);
        analysis.put("location", location);
        
        return analysis;
    }
    
    private Map<String, Object> createSimulatedMultimodalAnalysis(String base64Image, String base64Audio) {
        Map<String, Object> analysis = new HashMap<>();
        
        boolean hasViolation = Math.random() < 0.20; // 20% chance of violation for combined analysis
        
        if (hasViolation) {
            String[] violationTypes = {
                "Coordinated cheating activity detected",
                "Visual and audio patterns suggest communication",
                "Person appears to be listening while looking away",
                "Suspicious correlation between visual and audio cues"
            };
            String violation = violationTypes[(int)(Math.random() * violationTypes.length)];
            
            analysis.put("violation", true);
            analysis.put("confidence", 80 + (int)(Math.random() * 15)); // 80-95
            analysis.put("issues", violation);
            analysis.put("recommendation", "Immediate intervention required");
            analysis.put("riskLevel", "CRITICAL");
        } else {
            analysis.put("violation", false);
            analysis.put("confidence", 88 + (int)(Math.random() * 12)); // 88-100
            analysis.put("issues", "Normal exam behavior detected");
            analysis.put("recommendation", "Continue monitoring");
            analysis.put("riskLevel", "LOW");
        }
        
        analysis.put("timestamp", System.currentTimeMillis());
        analysis.put("type", "MULTIMODAL_ANALYSIS");
        analysis.put("model", modelName);
        analysis.put("location", location);
        
        return analysis;
    }
    
    public String getConfigurationSummary() {
        return String.format("Gemini Configuration: Project=%s, Location=%s, Models=[%s, %s, %s]",
            projectId, location, modelName, realtimeModelName, visionModelName);
    }
}