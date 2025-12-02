package com.visilpro.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class MalpracticeEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId; // Links to ExamSession
    private String type; // FACE_NOT_VISIBLE, MULTIPLE_FACES, LOOKING_AWAY, AUDIO_DETECTED
    private LocalDateTime timestamp;
    private double confidenceScore;
    private String snapshotUrl; // Path to saved image evidence
    
    // Additional fields for Gemini AI analysis
    private String eventType; // AI_DETECTED_VIOLATION, MANUAL_DETECTION, etc.
    private String description; // Detailed description from Gemini
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String analysisType; // IMAGE_ANALYSIS, AUDIO_ANALYSIS, MULTIMODAL_ANALYSIS

    @Transient
    private String snapshotData; // Base64 encoded image data (not saved to DB)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getSnapshotUrl() { return snapshotUrl; }
    public void setSnapshotUrl(String snapshotUrl) { this.snapshotUrl = snapshotUrl; }
    
    // Getters and setters for new fields
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public String getAnalysisType() { return analysisType; }
    public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
    public String getSnapshotData() { return snapshotData; }
    public void setSnapshotData(String snapshotData) { this.snapshotData = snapshotData; }
}
