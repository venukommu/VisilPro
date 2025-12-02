package com.visilpro.controller;

import com.visilpro.model.ExamSession;
import com.visilpro.model.MalpracticeEvent;
import com.visilpro.repository.ExamSessionRepository;
import com.visilpro.repository.MalpracticeEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
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

    @PostMapping("/start")
    public ExamSession startExam(@RequestBody ExamSession session) {
        session.setStartTime(LocalDateTime.now());
        session.setStatus("STARTED");
        return sessionRepository.save(session);
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
}
