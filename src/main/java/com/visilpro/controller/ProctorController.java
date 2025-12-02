package com.visilpro.controller;

import com.visilpro.model.ExamSession;
import com.visilpro.model.MalpracticeEvent;
import com.visilpro.repository.ExamSessionRepository;
import com.visilpro.repository.MalpracticeEventRepository;
import com.visilpro.service.EnhancedProctoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Base64;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/exam")
@CrossOrigin(origins = "*") // Allow all for PoC
public class ProctorController {

    @Autowired
    private ExamSessionRepository sessionRepository;

    @Autowired
    private MalpracticeEventRepository eventRepository;

    @Autowired
    private EnhancedProctoringService enhancedProctoringService;

    @PostMapping("/start")
    public ExamSession startExam(@RequestBody ExamSession session) {
        session.setStartTime(LocalDateTime.now());
        session.setStatus("STARTED");
        ExamSession savedSession = sessionRepository.save(session);
        
        // Session is ready for Gemini AI monitoring
        
        return savedSession;
    }

    @PostMapping("/log")
    public MalpracticeEvent logEvent(@RequestBody MalpracticeEvent event) {
        event.setTimestamp(LocalDateTime.now());

        // Handle Snapshot Saving
        if (event.getSnapshotData() != null && !event.getSnapshotData().isEmpty()) {
            try {
                String base64Image = event.getSnapshotData().split(",")[1]; // Remove header
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                
                String filename = "evidence_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + ".jpg";
                Path path = Paths.get("src/main/resources/static/evidence/" + filename);
                
                // Ensure directory exists
                Files.createDirectories(path.getParent());
                Files.write(path, imageBytes);
                
                event.setSnapshotUrl("/evidence/" + filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return eventRepository.save(event);
    }

    @GetMapping("/events/{sessionId}")
    public List<MalpracticeEvent> getEvents(@PathVariable Long sessionId) {
        return eventRepository.findBySessionId(sessionId);
    }

    @GetMapping("/sessions")
    public List<ExamSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    // Gemini AI-powered analysis endpoints
    
    @PostMapping("/gemini/analyze-image/{sessionId}")
    public CompletableFuture<ResponseEntity<Object>> analyzeImageWithGemini(
            @PathVariable String sessionId,
            @RequestBody String base64Image) {
        
        return enhancedProctoringService.analyzeImageAsync(sessionId, base64Image)
            .thenApply(result -> ResponseEntity.ok(result));
    }

    @PostMapping("/gemini/analyze-audio/{sessionId}")
    public CompletableFuture<ResponseEntity<Object>> analyzeAudioWithGemini(
            @PathVariable String sessionId,
            @RequestBody String base64Audio) {
        
        return enhancedProctoringService.analyzeAudioAsync(sessionId, base64Audio)
            .thenApply(result -> ResponseEntity.ok(result));
    }

    @PostMapping("/gemini/analyze-multimodal/{sessionId}")
    public CompletableFuture<ResponseEntity<Object>> analyzeMultimodalWithGemini(
            @PathVariable String sessionId,
            @RequestBody MultimodalRequest request) {
        
        return enhancedProctoringService.analyzeMultimodalAsync(
            sessionId, 
            request.getBase64Image(), 
            request.getBase64Audio()
        ).thenApply(result -> ResponseEntity.ok(result));
    }
    
    @GetMapping("/gemini/session-summary/{sessionId}")
    public ResponseEntity<Object> getSessionSummary(@PathVariable String sessionId) {
        return ResponseEntity.ok(enhancedProctoringService.getSessionAnalyticsSummary(sessionId));
    }
    
    @GetMapping("/gemini/recent-violations/{sessionId}")
    public ResponseEntity<Object> getRecentViolations(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(enhancedProctoringService.getRecentViolations(sessionId, limit));
    }
    
    @PostMapping("/gemini/clear-session/{sessionId}")
    public ResponseEntity<String> clearSessionData(@PathVariable String sessionId) {
        enhancedProctoringService.clearSessionAnalysis(sessionId);
        return ResponseEntity.ok("Session data cleared successfully");
    }
    
    // Request class for multimodal analysis
    public static class MultimodalRequest {
        private String base64Image;
        private String base64Audio;
        
        public String getBase64Image() { return base64Image; }
        public void setBase64Image(String base64Image) { this.base64Image = base64Image; }
        
        public String getBase64Audio() { return base64Audio; }
        public void setBase64Audio(String base64Audio) { this.base64Audio = base64Audio; }
    }
}
