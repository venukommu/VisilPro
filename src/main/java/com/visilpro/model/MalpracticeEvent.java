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
}
