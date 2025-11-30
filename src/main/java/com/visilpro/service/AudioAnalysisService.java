package com.visilpro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class AudioAnalysisService {

    @Autowired
    private LLMAnalysisService llmAnalysisService;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AudioAnalysisService() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Process real-time audio stream for violations
     */
    public CompletableFuture<LLMAnalysisService.AnalysisResult> processAudioStream(byte[] audioData, String sessionContext) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // First, detect basic audio properties
                AudioProperties properties = analyzeAudioProperties(audioData);
                
                if (properties.hasMultipleVoices() || properties.hasPhoneRinging() || properties.hasKeyboardSounds()) {
                    return new LLMAnalysisService.AnalysisResult(
                        true, 
                        properties.getConfidence(),
                        "Suspicious audio detected: " + properties.getDescription(),
                        properties.getViolationType()
                    );
                }

                // If speech detected, transcribe and analyze with LLM
                if (properties.hasSpeech()) {
                    String transcript = transcribeAudio(audioData);
                    if (!transcript.isEmpty()) {
                        return llmAnalysisService.analyzeAudioTranscript(transcript, sessionContext);
                    }
                }

                return new LLMAnalysisService.AnalysisResult(false, 0.0, "No violations detected", "CLEAR");
                
            } catch (Exception e) {
                return new LLMAnalysisService.AnalysisResult(false, 0.0, "Audio analysis error: " + e.getMessage(), "ERROR");
            }
        });
    }

    /**
     * Transcribe audio to text using OpenAI Whisper API
     */
    public String transcribeAudio(byte[] audioData) {
        try {
            // Create temporary WAV file
            File tempFile = File.createTempFile("audio", ".wav");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(audioData);
            }

            // Note: This is a simplified example. In production, you'd use proper multipart form data
            // For now, we'll simulate transcription results
            return simulateTranscription(audioData);
            
        } catch (Exception e) {
            System.err.println("Transcription error: " + e.getMessage());
            return "";
        }
    }

    /**
     * Analyze basic audio properties without transcription
     */
    private AudioProperties analyzeAudioProperties(byte[] audioData) {
        try {
            // Convert byte array to audio input stream
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bais);
            
            // Analyze audio characteristics
            AudioFormat format = audioStream.getFormat();
            float sampleRate = format.getSampleRate();
            int channels = format.getChannels();
            
            // Simple analysis based on audio characteristics
            boolean multipleVoices = detectMultipleVoices(audioData, format);
            boolean phoneRinging = detectPhoneRinging(audioData, format);
            boolean keyboardSounds = detectKeyboardSounds(audioData, format);
            boolean speech = detectSpeech(audioData, format);
            
            return new AudioProperties(multipleVoices, phoneRinging, keyboardSounds, speech);
            
        } catch (Exception e) {
            return new AudioProperties(false, false, false, false);
        }
    }

    private boolean detectMultipleVoices(byte[] audioData, AudioFormat format) {
        // Simplified detection - in production, use proper audio analysis libraries
        // This would analyze frequency patterns, voice separation, etc.
        return false; // Placeholder
    }

    private boolean detectPhoneRinging(byte[] audioData, AudioFormat format) {
        // Detect phone ringtone patterns
        return false; // Placeholder
    }

    private boolean detectKeyboardSounds(byte[] audioData, AudioFormat format) {
        // Detect rapid clicking/typing sounds
        return false; // Placeholder
    }

    private boolean detectSpeech(byte[] audioData, AudioFormat format) {
        // Basic speech detection
        return audioData.length > 1000; // Simplified check
    }

    private String simulateTranscription(byte[] audioData) {
        // In production, this would call OpenAI Whisper API or another transcription service
        // For demonstration, return empty or simulate based on audio length
        if (audioData.length > 5000) {
            return "Sample transcribed text from audio"; // Placeholder
        }
        return "";
    }

    /**
     * Monitor continuous audio stream for real-time analysis
     */
    public void startRealTimeAudioMonitoring(String sessionId, AudioMonitorCallback callback) {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            
            if (!AudioSystem.isLineSupported(info)) {
                callback.onError("Audio line not supported");
                return;
            }

            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[4096];
            ByteArrayOutputStream audioBuffer = new ByteArrayOutputStream();

            // Monitor in chunks
            while (true) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                audioBuffer.write(buffer, 0, bytesRead);

                // Process every 3 seconds of audio
                if (audioBuffer.size() > 48000) { // ~3 seconds at 16kHz
                    byte[] audioChunk = audioBuffer.toByteArray();
                    processAudioStream(audioChunk, sessionId)
                        .thenAccept(result -> {
                            if (result.isViolationDetected()) {
                                callback.onViolationDetected(result);
                            }
                        });
                    
                    audioBuffer.reset();
                }
            }
        } catch (Exception e) {
            callback.onError("Audio monitoring error: " + e.getMessage());
        }
    }

    /**
     * Audio properties helper class
     */
    private static class AudioProperties {
        private final boolean multipleVoices;
        private final boolean phoneRinging;
        private final boolean keyboardSounds;
        private final boolean speech;

        public AudioProperties(boolean multipleVoices, boolean phoneRinging, boolean keyboardSounds, boolean speech) {
            this.multipleVoices = multipleVoices;
            this.phoneRinging = phoneRinging;
            this.keyboardSounds = keyboardSounds;
            this.speech = speech;
        }

        public boolean hasMultipleVoices() { return multipleVoices; }
        public boolean hasPhoneRinging() { return phoneRinging; }
        public boolean hasKeyboardSounds() { return keyboardSounds; }
        public boolean hasSpeech() { return speech; }

        public double getConfidence() {
            if (multipleVoices) return 0.9;
            if (phoneRinging) return 0.8;
            if (keyboardSounds) return 0.6;
            return 0.5;
        }

        public String getDescription() {
            List<String> issues = new ArrayList<>();
            if (multipleVoices) issues.add("multiple voices");
            if (phoneRinging) issues.add("phone ringing");
            if (keyboardSounds) issues.add("excessive keyboard sounds");
            return String.join(", ", issues);
        }

        public String getViolationType() {
            if (multipleVoices) return "MULTIPLE_VOICES";
            if (phoneRinging) return "PHONE_CALL";
            if (keyboardSounds) return "SUSPICIOUS_AUDIO";
            return "UNKNOWN";
        }
    }

    /**
     * Callback interface for real-time monitoring
     */
    public interface AudioMonitorCallback {
        void onViolationDetected(LLMAnalysisService.AnalysisResult result);
        void onError(String error);
    }
}