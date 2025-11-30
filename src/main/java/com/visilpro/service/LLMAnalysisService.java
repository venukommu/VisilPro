package com.visilpro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class LLMAnalysisService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;
    
    @Value("${llm.provider:openai}")
    private String llmProvider;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public LLMAnalysisService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Analyze image for proctoring violations using LLM vision capabilities
     */
    public AnalysisResult analyzeImage(byte[] imageData, String context) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4-vision-preview");
            requestBody.put("max_tokens", 300);
            
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            
            List<Map<String, Object>> content = new ArrayList<>();
            
            // Text content
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", buildImageAnalysisPrompt(context));
            content.add(textContent);
            
            // Image content
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            Map<String, String> imageUrl = new HashMap<>();
            imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
            imageContent.put("image_url", imageUrl);
            content.add(imageContent);
            
            message.put("content", content);
            messages.add(message);
            requestBody.put("messages", messages);

            String response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            return parseImageAnalysisResponse(response);
            
        } catch (Exception e) {
            return new AnalysisResult(false, 0.0, "Error analyzing image: " + e.getMessage(), "ANALYSIS_ERROR");
        }
    }

    /**
     * Analyze audio transcript for cheating indicators
     */
    public AnalysisResult analyzeAudioTranscript(String transcript, String context) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");
            requestBody.put("max_tokens", 200);
            
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", buildAudioAnalysisPrompt(transcript, context));
            messages.add(message);
            requestBody.put("messages", messages);

            String response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            return parseAudioAnalysisResponse(response);
            
        } catch (Exception e) {
            return new AnalysisResult(false, 0.0, "Error analyzing audio: " + e.getMessage(), "ANALYSIS_ERROR");
        }
    }

    /**
     * Real-time behavioral analysis combining multiple inputs
     */
    public AnalysisResult analyzeBehavior(Map<String, Object> behaviorData) {
        try {
            String prompt = buildBehaviorAnalysisPrompt(behaviorData);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");
            requestBody.put("max_tokens", 250);
            
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);

            String response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(String.class)
                .block();

            return parseBehaviorAnalysisResponse(response);
            
        } catch (Exception e) {
            return new AnalysisResult(false, 0.0, "Error analyzing behavior: " + e.getMessage(), "ANALYSIS_ERROR");
        }
    }

    private String buildImageAnalysisPrompt(String context) {
        return """
            You are an AI proctoring system analyzing exam footage. Examine this image for potential academic dishonesty indicators.
            
            Context: %s
            
            Look for:
            1. Multiple faces or people in frame
            2. Student looking away from screen frequently
            3. Use of unauthorized materials (books, notes, phones)
            4. Suspicious hand movements or gestures
            5. Environmental issues (poor lighting, obstructed view)
            
            Respond in JSON format:
            {
                "violation": true/false,
                "confidence": 0.0-1.0,
                "type": "MULTIPLE_FACES|LOOKING_AWAY|UNAUTHORIZED_MATERIALS|SUSPICIOUS_BEHAVIOR|ENVIRONMENTAL_ISSUE",
                "description": "Brief description of what was detected"
            }
            """.formatted(context);
    }

    private String buildAudioAnalysisPrompt(String transcript, String context) {
        return """
            You are analyzing audio from an exam session for potential cheating indicators.
            
            Context: %s
            Audio Transcript: "%s"
            
            Look for:
            1. Multiple voices or conversations
            2. Requests for help or answers
            3. Reading questions aloud (potential sharing)
            4. Phone calls or video call sounds
            5. Suspicious background noises
            
            Respond in JSON format:
            {
                "violation": true/false,
                "confidence": 0.0-1.0,
                "type": "MULTIPLE_VOICES|SEEKING_HELP|QUESTION_SHARING|PHONE_CALL|SUSPICIOUS_AUDIO",
                "description": "Brief description of audio violation"
            }
            """.formatted(context, transcript);
    }

    private String buildBehaviorAnalysisPrompt(Map<String, Object> behaviorData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this real-time exam behavior data for academic dishonesty patterns:\n\n");
        
        behaviorData.forEach((key, value) -> {
            prompt.append(key).append(": ").append(value).append("\n");
        });
        
        prompt.append("""
            
            Consider patterns like:
            1. Frequent tab switching or window changes
            2. Extended periods of inactivity
            3. Rapid mouse movements suggesting copy-paste
            4. Typing patterns inconsistent with question difficulty
            5. Time spent on questions vs. complexity
            
            Respond in JSON format:
            {
                "violation": true/false,
                "confidence": 0.0-1.0,
                "type": "TAB_SWITCHING|COPY_PASTE|SUSPICIOUS_TIMING|INACTIVITY|PATTERN_ANOMALY",
                "description": "Brief description of behavioral concern"
            }
            """);
        
        return prompt.toString();
    }

    private AnalysisResult parseImageAnalysisResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").get(0).path("message").path("content");
            JsonNode analysis = objectMapper.readTree(content.asText());
            
            return new AnalysisResult(
                analysis.path("violation").asBoolean(),
                analysis.path("confidence").asDouble(),
                analysis.path("description").asText(),
                analysis.path("type").asText()
            );
        } catch (Exception e) {
            return new AnalysisResult(false, 0.0, "Failed to parse LLM response", "PARSE_ERROR");
        }
    }

    private AnalysisResult parseAudioAnalysisResponse(String response) {
        return parseImageAnalysisResponse(response); // Same JSON structure
    }

    private AnalysisResult parseBehaviorAnalysisResponse(String response) {
        return parseImageAnalysisResponse(response); // Same JSON structure
    }

    /**
     * Result class for LLM analysis
     */
    public static class AnalysisResult {
        private final boolean violationDetected;
        private final double confidence;
        private final String description;
        private final String violationType;

        public AnalysisResult(boolean violationDetected, double confidence, String description, String violationType) {
            this.violationDetected = violationDetected;
            this.confidence = confidence;
            this.description = description;
            this.violationType = violationType;
        }

        public boolean isViolationDetected() { return violationDetected; }
        public double getConfidence() { return confidence; }
        public String getDescription() { return description; }
        public String getViolationType() { return violationType; }
    }
}