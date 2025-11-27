package com.visilpro.controller;

import com.visilpro.model.ExamSession;
import com.visilpro.model.MalpracticeEvent;
import com.visilpro.repository.ExamSessionRepository;
import com.visilpro.repository.MalpracticeEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
