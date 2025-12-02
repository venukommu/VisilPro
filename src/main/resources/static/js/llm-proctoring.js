// Enhanced Gemini AI-powered proctoring client
class GeminiProctoringClient {
    constructor(sessionId) {
        this.sessionId = sessionId;
        this.isMonitoring = false;
        this.mediaRecorder = null;
        this.audioChunks = [];
        this.audioStream = null;
        this.violations = [];
        this.analysisStats = {
            totalAnalyses: 0,
            violationsDetected: 0,
            lastAnalysis: null
        };
    }

    async startMonitoring() {
        this.isMonitoring = true;
        console.log('🚀 Gemini AI-powered proctoring started for session:', this.sessionId);
        
        try {
            // Start periodic image analysis
            await this.startImageAnalysis();
            
            // Start audio monitoring
            await this.startAudioMonitoring();
            
            // Start combined multimodal analysis every 30 seconds
            this.startMultimodalAnalysis();
            
        } catch (error) {
            console.error('Failed to start monitoring:', error);
        }
    }

    stopMonitoring() {
        this.isMonitoring = false;
        console.log('🛑 Gemini AI proctoring stopped');
        
        if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
            this.mediaRecorder.stop();
        }
        
        if (this.audioStream) {
            this.audioStream.getTracks().forEach(track => track.stop());
        }
    }

    async startImageAnalysis() {
        try {
            const video = document.getElementById('video'); // Use existing video element
            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');

            // Capture and analyze every 15 seconds
            setInterval(async () => {
                if (this.isMonitoring && video.videoWidth > 0) {
                    canvas.width = video.videoWidth;
                    canvas.height = video.videoHeight;
                    ctx.drawImage(video, 0, 0);
                    
                    const base64Image = canvas.toDataURL('image/jpeg', 0.8).split(',')[1];
                    await this.sendImageToGemini(base64Image);
                }
            }, 15000);

        } catch (error) {
            console.error('Failed to start image analysis:', error);
        }
    }

    async startAudioMonitoring() {
        try {
            this.audioStream = await navigator.mediaDevices.getUserMedia({ 
                audio: {
                    echoCancellation: true,
                    noiseSuppression: true,
                    autoGainControl: true
                } 
            });
            
            this.mediaRecorder = new MediaRecorder(this.audioStream, {
                mimeType: 'audio/webm'
            });

            this.mediaRecorder.ondataavailable = (event) => {
                if (event.data.size > 0) {
                    this.audioChunks.push(event.data);
                }
            };

            this.mediaRecorder.onstop = async () => {
                if (this.audioChunks.length > 0) {
                    const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
                    await this.sendAudioToGemini(audioBlob);
                    this.audioChunks = [];
                }
            };

            // Record in 20-second chunks
            setInterval(() => {
                if (this.isMonitoring) {
                    if (this.mediaRecorder.state === 'recording') {
                        this.mediaRecorder.stop();
                    }
                    this.mediaRecorder.start();
                    
                    setTimeout(() => {
                        if (this.mediaRecorder.state === 'recording') {
                            this.mediaRecorder.stop();
                        }
                    }, 20000);
                }
            }, 22000);

        } catch (error) {
            console.error('Failed to start audio monitoring:', error);
        }
    }

    startMultimodalAnalysis() {
        // Combined analysis every 45 seconds
        setInterval(async () => {
            if (this.isMonitoring) {
                await this.performMultimodalAnalysis();
            }
        }, 45000);
    }

    async sendImageToGemini(base64Image) {
        try {
            const response = await fetch(`/api/exam/gemini/analyze-image/${this.sessionId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(base64Image)
            });

            const result = await response.json();
            this.handleAnalysisResult(result, 'Image Analysis');
            
        } catch (error) {
            console.error('Gemini image analysis failed:', error);
        }
    }

    async sendAudioToGemini(audioBlob) {
        try {
            const reader = new FileReader();
            reader.onload = async () => {
                const base64Audio = reader.result.split(',')[1];
                
                const response = await fetch(`/api/exam/gemini/analyze-audio/${this.sessionId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(base64Audio)
                });

                const result = await response.json();
                this.handleAnalysisResult(result, 'Audio Analysis');
            };
            reader.readAsDataURL(audioBlob);
            
        } catch (error) {
            console.error('Gemini audio analysis failed:', error);
        }
    }

    async performMultimodalAnalysis() {
        try {
            // Capture current frame
            const video = document.getElementById('video');
            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');
            
            if (video.videoWidth === 0) return;
            
            canvas.width = video.videoWidth;
            canvas.height = video.videoHeight;
            ctx.drawImage(video, 0, 0);
            
            const base64Image = canvas.toDataURL('image/jpeg', 0.8).split(',')[1];
            
            // Get recent audio if available
            if (this.audioChunks.length > 0) {
                const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
                const reader = new FileReader();
                
                reader.onload = async () => {
                    const base64Audio = reader.result.split(',')[1];
                    
                    const response = await fetch(`/api/exam/gemini/analyze-multimodal/${this.sessionId}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            base64Image: base64Image,
                            base64Audio: base64Audio
                        })
                    });

                    const result = await response.json();
                    this.handleAnalysisResult(result, 'Multimodal Analysis');
                };
                
                reader.readAsDataURL(audioBlob);
            }
            
        } catch (error) {
            console.error('Multimodal analysis failed:', error);
        }
    }

    handleAnalysisResult(result, analysisType) {
        this.analysisStats.totalAnalyses++;
        this.analysisStats.lastAnalysis = new Date();
        
        console.log(`📊 ${analysisType} Result:`, result);
        
        if (result.violation) {
            this.analysisStats.violationsDetected++;
            this.violations.push({
                type: analysisType,
                result: result,
                timestamp: new Date()
            });
            
            this.showViolationAlert(analysisType, result);
        }
        
        // Update UI stats if element exists
        this.updateStatsDisplay();
    }

    showViolationAlert(type, result) {
        const alert = document.createElement('div');
        alert.className = 'gemini-violation-alert';
        alert.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: linear-gradient(135deg, #dc2626, #b91c1c);
            color: white;
            padding: 16px 20px;
            border-radius: 12px;
            box-shadow: 0 10px 25px rgba(220, 38, 38, 0.3);
            z-index: 10000;
            font-family: 'Inter', sans-serif;
            font-size: 14px;
            max-width: 400px;
            border-left: 4px solid #fbbf24;
            animation: geminiSlideIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        `;
        
        const confidence = result.confidence || 0;
        const riskLevel = result.riskLevel || 'MEDIUM';
        
        alert.innerHTML = `
            <div style="display: flex; align-items: center; margin-bottom: 8px;">
                <span style="font-size: 18px; margin-right: 8px;">⚠️</span>
                <strong>${type} Alert</strong>
                <span style="margin-left: auto; font-size: 12px; background: rgba(255,255,255,0.2); padding: 2px 8px; border-radius: 12px;">
                    ${confidence}% confident
                </span>
            </div>
            <div style="font-size: 13px; margin-bottom: 8px; color: #fef3c7;">
                Risk Level: <strong>${riskLevel}</strong>
            </div>
            <div style="font-size: 13px; line-height: 1.4;">
                ${result.issues || result.recommendation || 'Suspicious activity detected'}
            </div>
        `;

        document.body.appendChild(alert);

        // Remove alert after 8 seconds
        setTimeout(() => {
            alert.style.animation = 'geminiSlideOut 0.3s ease-in';
            setTimeout(() => {
                if (alert.parentNode) {
                    document.body.removeChild(alert);
                }
            }, 300);
        }, 8000);
    }

    updateStatsDisplay() {
        const statsElement = document.getElementById('gemini-stats');
        if (statsElement) {
            statsElement.innerHTML = `
                <div>Analyses: ${this.analysisStats.totalAnalyses}</div>
                <div>Violations: ${this.analysisStats.violationsDetected}</div>
                <div>Last: ${this.analysisStats.lastAnalysis ? this.analysisStats.lastAnalysis.toLocaleTimeString() : 'N/A'}</div>
            `;
        }
    }

    async getSessionSummary() {
        try {
            const response = await fetch(`/api/exam/gemini/session-summary/${this.sessionId}`);
            const summary = await response.json();
            console.log('📈 Session Summary:', summary);
            return summary;
        } catch (error) {
            console.error('Failed to get session summary:', error);
            return null;
        }
    }
}

// Add enhanced CSS animations and styles for Gemini alerts
const geminiStyles = document.createElement('style');
geminiStyles.textContent = `
    @keyframes geminiSlideIn {
        from {
            transform: translateX(120%) scale(0.8);
            opacity: 0;
        }
        to {
            transform: translateX(0) scale(1);
            opacity: 1;
        }
    }
    
    @keyframes geminiSlideOut {
        from {
            transform: translateX(0) scale(1);
            opacity: 1;
        }
        to {
            transform: translateX(120%) scale(0.8);
            opacity: 0;
        }
    }
    
    .gemini-stats-container {
        position: fixed;
        bottom: 20px;
        right: 20px;
        background: rgba(15, 23, 42, 0.95);
        color: #f8fafc;
        padding: 12px 16px;
        border-radius: 8px;
        font-family: 'Inter', sans-serif;
        font-size: 12px;
        border: 1px solid #334155;
        backdrop-filter: blur(8px);
    }
    
    .gemini-stats-container div {
        margin-bottom: 4px;
    }
    
    .gemini-stats-container div:last-child {
        margin-bottom: 0;
    }
`;
document.head.appendChild(geminiStyles);

// Add Gemini stats display to the page
function addGeminiStatsDisplay() {
    const statsContainer = document.createElement('div');
    statsContainer.className = 'gemini-stats-container';
    statsContainer.innerHTML = `
        <div style="font-weight: 600; margin-bottom: 8px; color: #3b82f6;">🤖 Gemini AI Status</div>
        <div id="gemini-stats">
            <div>Analyses: 0</div>
            <div>Violations: 0</div>
            <div>Last: N/A</div>
        </div>
    `;
    document.body.appendChild(statsContainer);
}

// Global variable for easy access
window.geminiProctoring = null;

// Initialize when exam starts
function startLLMProctoring(sessionId) {
    console.log('🎯 Initializing Gemini AI Proctoring...');
    window.geminiProctoring = new GeminiProctoringClient(sessionId);
    
    // Add stats display
    addGeminiStatsDisplay();
    
    // Start monitoring
    window.geminiProctoring.startMonitoring();
    
    // Show initialization success
    setTimeout(() => {
        const initAlert = document.createElement('div');
        initAlert.style.cssText = `
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: linear-gradient(135deg, #10b981, #059669);
            color: white;
            padding: 12px 20px;
            border-radius: 8px;
            font-family: 'Inter', sans-serif;
            font-size: 14px;
            z-index: 10000;
            animation: geminiSlideIn 0.4s ease-out;
        `;
        initAlert.innerHTML = `
            <span style="margin-right: 8px;">✅</span>
            <strong>Gemini AI Proctoring Active</strong>
        `;
        
        document.body.appendChild(initAlert);
        
        setTimeout(() => {
            initAlert.style.animation = 'geminiSlideOut 0.3s ease-in';
            setTimeout(() => {
                if (initAlert.parentNode) {
                    document.body.removeChild(initAlert);
                }
            }, 300);
        }, 3000);
    }, 1000);
}

function stopLLMProctoring() {
    if (window.geminiProctoring) {
        window.geminiProctoring.stopMonitoring();
        console.log('🛑 Gemini AI Proctoring stopped');
    }
}

// Helper function to show session summary
async function showGeminiSessionSummary() {
    if (window.geminiProctoring) {
        const summary = await window.geminiProctoring.getSessionSummary();
        if (summary) {
            console.table(summary);
            alert(`Session Summary:\n\nTotal Analyses: ${summary.totalAnalyses}\nViolations: ${summary.violationCount}\nOverall Risk: ${summary.overallRisk}\nNeeds Review: ${summary.needsReview ? 'Yes' : 'No'}`);
        }
    }
}