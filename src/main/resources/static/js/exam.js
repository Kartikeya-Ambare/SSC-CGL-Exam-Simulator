/* =========================================================
   SSC CGL EXAM PORTAL — EXAM JS
   Full exam attempt logic: timer, navigation, palette, API
   ========================================================= */

'use strict';

// ============================================================
// STATE
// ============================================================
const ExamApp = {
    sessionId: null,
    csrfToken: null,
    csrfHeader: null,
    examState: null,          // full ExamStateDto from server
    currentPosition: 0,       // 0-based index into examState.questions
    currentSection: 'GENERAL_AWARENESS',
    timerInterval: null,
    timeRemaining: 0,         // seconds
    syncInterval: null,
    saveDebounce: null,
    submitting: false,
    sections: ['GENERAL_AWARENESS', 'VERBAL_ABILITY', 'LOGICAL_REASONING', 'QUANTITATIVE_APTITUDE'],
    sectionLabels: {
        'GENERAL_AWARENESS': 'General Awareness',
        'VERBAL_ABILITY': 'Verbal Ability',
        'LOGICAL_REASONING': 'Logical Reasoning',
        'QUANTITATIVE_APTITUDE': 'Quantitative Aptitude'
    }
};

// ============================================================
// INIT
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
    // Read data injected by Thymeleaf
    const stateEl = document.getElementById('exam-state-data');
    const sessionEl = document.getElementById('session-id-data');
    const csrfTokenEl = document.getElementById('csrf-token-data');
    const csrfHeaderEl = document.getElementById('csrf-header-data');

    if (!stateEl || !sessionEl) {
        console.error('Exam data elements not found');
        return;
    }

    try {
        ExamApp.examState = JSON.parse(stateEl.textContent);
    } catch (e) {
        console.error('Failed to parse exam state:', e);
        return;
    }

    ExamApp.sessionId = sessionEl.textContent.trim();
    ExamApp.csrfToken = csrfTokenEl ? csrfTokenEl.textContent.trim() : '';
    ExamApp.csrfHeader = csrfHeaderEl ? csrfHeaderEl.textContent.trim() : 'X-CSRF-TOKEN';
    ExamApp.currentPosition = ExamApp.examState.currentPosition || 0;
    ExamApp.timeRemaining = ExamApp.examState.timeRemainingSeconds || 3600;

    // Render initial state
    renderSectionTabs();
    renderPalette();
    renderCurrentQuestion();
    updateSidebarStats();
    startTimer();
    startServerSync();

    // Offline detection
    window.addEventListener('offline', showOfflineBar);
    window.addEventListener('online', hideOfflineBar);

    // Warn before unload
    window.addEventListener('beforeunload', e => {
        if (!ExamApp.submitting) {
            e.preventDefault();
            e.returnValue = 'Your exam is in progress. Are you sure you want to leave?';
        }
    });
});

// ============================================================
// TIMER
// ============================================================
function startTimer() {
    updateTimerDisplay();
    ExamApp.timerInterval = setInterval(() => {
        ExamApp.timeRemaining--;
        updateTimerDisplay();
        if (ExamApp.timeRemaining <= 0) {
            clearInterval(ExamApp.timerInterval);
            handleTimeUp();
        }
    }, 1000);
}

function updateTimerDisplay() {
    const el = document.getElementById('timer-display');
    if (!el) return;
    const t = Math.max(0, ExamApp.timeRemaining);
    const h = Math.floor(t / 3600);
    const m = Math.floor((t % 3600) / 60);
    const s = t % 60;
    el.textContent = `${pad(h)}:${pad(m)}:${pad(s)}`;

    // Visual urgency
    el.className = '';
    if (t <= 60) {
        el.className = 'timer-critical';
    } else if (t <= 300) {
        el.className = 'timer-warn';
    } else if (t <= 600) {
        el.className = 'timer-caution';
    } else {
        el.className = 'timer-normal';
    }
}

function pad(n) { return n.toString().padStart(2, '0'); }

function handleTimeUp() {
    clearInterval(ExamApp.syncInterval);
    showTimeUpOverlay();
    setTimeout(() => autoSubmitExam(), 2000);
}

function showTimeUpOverlay() {
    const overlay = document.getElementById('time-up-overlay');
    if (overlay) overlay.classList.add('visible');
}

// ============================================================
// SERVER SYNC
// ============================================================
function startServerSync() {
    ExamApp.syncInterval = setInterval(syncTimerWithServer, 30000);
}

async function syncTimerWithServer() {
    try {
        const resp = await fetch(`/api/exam/${ExamApp.sessionId}/time`, {
            headers: { [ExamApp.csrfHeader]: ExamApp.csrfToken }
        });
        if (!resp.ok) return;
        const data = await resp.json();
        if (data.timeRemaining !== undefined) {
            // Reconcile: if server differs by > 5s, trust server
            const diff = Math.abs(ExamApp.timeRemaining - data.timeRemaining);
            if (diff > 5) {
                ExamApp.timeRemaining = data.timeRemaining;
            }
        }
        if (data.autoSubmitted) {
            handleTimeUp();
        }
    } catch (e) { /* ignore */ }
}

// ============================================================
// QUESTION RENDERING
// ============================================================
function getCurrentQuestion() {
    const questions = ExamApp.examState.questions;
    return questions[ExamApp.currentPosition] || null;
}

function getAnswerForQuestion(examQuestionId) {
    const answers = ExamApp.examState.answers;
    return answers ? answers[String(examQuestionId)] : null;
}

function renderCurrentQuestion() {
    const q = getCurrentQuestion();
    if (!q) return;

    ExamApp.currentSection = q.section;
    updateSectionTabs();

    // Question header
    const posEl = document.getElementById('q-position');
    const topicEl = document.getElementById('q-topic');
    const diffEl = document.getElementById('q-difficulty');
    const sectionEl = document.getElementById('q-section');

    if (posEl) posEl.textContent = `Question ${ExamApp.currentPosition + 1} of ${ExamApp.examState.questions.length}`;
    if (topicEl) topicEl.textContent = q.topic || '';
    if (diffEl) diffEl.textContent = q.difficulty || '';
    if (sectionEl) sectionEl.textContent = ExamApp.sectionLabels[q.section] || q.section;

    // Question text
    const textEl = document.getElementById('question-text');
    if (textEl) textEl.textContent = q.questionText;

    // Options
    const optionsContainer = document.getElementById('options-container');
    if (optionsContainer) {
        optionsContainer.innerHTML = '';
        const answer = getAnswerForQuestion(q.examQuestionId);
        const selectedOpt = answer ? answer.selectedOption : -1;

        (q.options || []).forEach((opt, idx) => {
            const li = document.createElement('li');
            li.className = 'option-item' + (selectedOpt === idx ? ' selected' : '');
            li.dataset.optionIndex = idx;
            li.innerHTML = `
                <span class="option-letter">${String.fromCharCode(65 + idx)}</span>
                <span class="option-text">${escapeHtml(opt)}</span>
            `;
            li.addEventListener('click', () => selectOption(idx));
            optionsContainer.appendChild(li);
        });
    }

    // Update status ribbon
    updateQuestionStatusRibbon();

    // Update palette current
    updatePaletteCurrent();

    // Update nav buttons
    updateNavButtons();
}

function updateQuestionStatusRibbon() {
    const q = getCurrentQuestion();
    if (!q) return;
    const ribbon = document.getElementById('q-status-ribbon');
    if (!ribbon) return;

    const answer = getAnswerForQuestion(q.examQuestionId);
    if (!answer) { ribbon.textContent = 'Not Visited'; ribbon.className = 'q-status-ribbon not-visited'; return; }

    const status = answer.status || 'NOT_VISITED';
    const map = {
        'NOT_VISITED': ['Not Visited', 'not-visited'],
        'NOT_ANSWERED': ['Not Answered', 'not-answered'],
        'ANSWERED': ['Answered', 'answered'],
        'MARKED_FOR_REVIEW': ['Marked for Review', 'marked'],
        'ANSWERED_MARKED': ['Answered & Marked', 'answered']
    };
    const [label, cls] = map[status] || ['Not Visited', 'not-visited'];
    ribbon.textContent = label;
    ribbon.className = `q-status-ribbon ${cls}`;
}

function escapeHtml(text) {
    const d = document.createElement('div');
    d.appendChild(document.createTextNode(text));
    return d.innerHTML;
}

// ============================================================
// OPTION SELECTION (local only, no API call yet)
// ============================================================
function selectOption(optionIdx) {
    const q = getCurrentQuestion();
    if (!q) return;

    // Update UI
    document.querySelectorAll('.option-item').forEach((item, i) => {
        item.classList.toggle('selected', i === optionIdx);
        item.querySelector('.option-letter').style.background = i === optionIdx ? '' : '';
    });

    // Update local state
    let answer = getAnswerForQuestion(q.examQuestionId);
    if (!answer) {
        answer = { examQuestionId: q.examQuestionId, selectedOption: -1, status: 'NOT_VISITED', markedForReview: false };
        ExamApp.examState.answers[String(q.examQuestionId)] = answer;
    }
    answer.selectedOption = optionIdx;
    if (answer.status === 'NOT_VISITED' || answer.status === 'NOT_ANSWERED') {
        answer.status = 'ANSWERED';
    }
}

// ============================================================
// ANSWER ACTIONS
// ============================================================
async function saveAndNext() {
    const q = getCurrentQuestion();
    if (!q) return;

    const answer = getAnswerForQuestion(q.examQuestionId);
    const selectedOption = answer ? answer.selectedOption : -1;

    await callSaveAnswer(q.examQuestionId, selectedOption, 'SAVE_NEXT', false);

    // Advance to next question
    if (ExamApp.currentPosition < ExamApp.examState.questions.length - 1) {
        ExamApp.currentPosition++;
        renderCurrentQuestion();
        scrollQuestionToTop();
    }
}

async function markForReview() {
    const q = getCurrentQuestion();
    if (!q) return;
    const answer = getAnswerForQuestion(q.examQuestionId);
    const selectedOption = answer ? answer.selectedOption : -1;
    const hasAnswer = selectedOption >= 0;
    await callSaveAnswer(q.examQuestionId, selectedOption, hasAnswer ? 'MARK_ANSWERED_REVIEW' : 'MARK_REVIEW', true);
    if (ExamApp.currentPosition < ExamApp.examState.questions.length - 1) {
        ExamApp.currentPosition++;
        renderCurrentQuestion();
        scrollQuestionToTop();
    }
}

async function clearResponse() {
    const q = getCurrentQuestion();
    if (!q) return;
    await callSaveAnswer(q.examQuestionId, -1, 'CLEAR', false);
    document.querySelectorAll('.option-item').forEach(item => item.classList.remove('selected'));
    const answer = ExamApp.examState.answers[String(q.examQuestionId)];
    if (answer) {
        answer.selectedOption = -1;
        answer.status = 'NOT_ANSWERED';
    }
    updateQuestionStatusRibbon();
    updatePaletteButton(q.examQuestionId, 'NOT_ANSWERED');
    updateSidebarStats();
}

async function saveCurrentAnswer() {
    const q = getCurrentQuestion();
    if (!q) return;
    const answer = getAnswerForQuestion(q.examQuestionId);
    const selectedOption = answer ? answer.selectedOption : -1;
    if (selectedOption < 0) return;
    await callSaveAnswer(q.examQuestionId, selectedOption, 'SAVE', false);
}

async function callSaveAnswer(examQuestionId, selectedOption, action, markedForReview) {
    showSaveIndicator();
    try {
        const headers = {
            'Content-Type': 'application/json',
            [ExamApp.csrfHeader]: ExamApp.csrfToken
        };
        const body = JSON.stringify({
            examQuestionId,
            selectedOption,
            action,
            markedForReview,
            currentPosition: ExamApp.currentPosition
        });
        const resp = await fetch(`/api/exam/${ExamApp.sessionId}/save-answer`, {
            method: 'POST',
            headers,
            body
        });

        if (!resp.ok) {
            const err = await resp.text();
            console.error('Save answer error:', err);
            return;
        }

        const data = await resp.json();

        // Update local answer state
        if (data.answerStatus) {
            const answer = ExamApp.examState.answers[String(examQuestionId)] || {};
            answer.examQuestionId = examQuestionId;
            answer.selectedOption = selectedOption;
            answer.status = data.answerStatus;
            answer.markedForReview = markedForReview;
            ExamApp.examState.answers[String(examQuestionId)] = answer;
        }

        // Update palette
        const paletteStatus = mapActionToStatus(action, selectedOption);
        updatePaletteButton(examQuestionId, paletteStatus);

        // Update section progress
        if (data.sectionProgress) {
            ExamApp.examState.sectionProgress = data.sectionProgress;
        }

        updateSidebarStats();
        updateQuestionStatusRibbon();

    } catch (e) {
        console.error('Network error saving answer:', e);
    } finally {
        hideSaveIndicator();
    }
}

function mapActionToStatus(action, selectedOption) {
    if (action === 'CLEAR') return 'NOT_ANSWERED';
    if (action === 'MARK_REVIEW') return 'MARKED_FOR_REVIEW';
    if (action === 'MARK_ANSWERED_REVIEW') return 'ANSWERED_MARKED';
    if (action === 'SAVE' || action === 'SAVE_NEXT') {
        return selectedOption >= 0 ? 'ANSWERED' : 'NOT_ANSWERED';
    }
    return 'NOT_ANSWERED';
}

// ============================================================
// NAVIGATION
// ============================================================
function goToPrev() {
    saveCurrentAnswer();
    if (ExamApp.currentPosition > 0) {
        ExamApp.currentPosition--;
        renderCurrentQuestion();
        scrollQuestionToTop();
    }
}

function goToNext() {
    saveCurrentAnswer();
    if (ExamApp.currentPosition < ExamApp.examState.questions.length - 1) {
        ExamApp.currentPosition++;
        renderCurrentQuestion();
        scrollQuestionToTop();
    }
}

function jumpToQuestion(position) {
    saveCurrentAnswer();
    if (position >= 0 && position < ExamApp.examState.questions.length) {
        ExamApp.currentPosition = position;
        renderCurrentQuestion();
        scrollQuestionToTop();
    }
}

function jumpToSection(sectionName) {
    const idx = ExamApp.examState.questions.findIndex(q => q.section === sectionName);
    if (idx >= 0) jumpToQuestion(idx);
}

function scrollQuestionToTop() {
    const main = document.querySelector('.exam-main');
    if (main) main.scrollTo({ top: 0, behavior: 'smooth' });
    else window.scrollTo({ top: 0, behavior: 'smooth' });
}

function updateNavButtons() {
    const prevBtn = document.getElementById('btn-prev');
    const nextBtn = document.getElementById('btn-next-q');
    if (prevBtn) prevBtn.disabled = ExamApp.currentPosition === 0;
    if (nextBtn) nextBtn.disabled = ExamApp.currentPosition === ExamApp.examState.questions.length - 1;
}

// ============================================================
// SECTION TABS
// ============================================================
function renderSectionTabs() {
    const tabs = document.querySelectorAll('.section-tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', function () {
            const sec = this.dataset.section;
            if (sec) jumpToSection(sec);
        });
    });
}

function updateSectionTabs() {
    document.querySelectorAll('.section-tab').forEach(tab => {
        tab.classList.toggle('active', tab.dataset.section === ExamApp.currentSection);
    });
}

// ============================================================
// PALETTE
// ============================================================
function renderPalette() {
    ExamApp.sections.forEach(section => {
        const container = document.getElementById(`palette-${section}`);
        if (!container) return;
        container.innerHTML = '';

        const sectionQuestions = ExamApp.examState.questions.filter(q => q.section === section);
        sectionQuestions.forEach(q => {
            const answer = getAnswerForQuestion(q.examQuestionId);
            const status = answer ? answer.status : 'NOT_VISITED';
            const btn = document.createElement('button');
            btn.className = `palette-btn ${statusToPaletteClass(status)}`;
            btn.id = `palette-q-${q.examQuestionId}`;
            btn.dataset.position = q.position !== undefined ? q.position : ExamApp.examState.questions.indexOf(q);
            btn.textContent = q.sectionPosition || (sectionQuestions.indexOf(q) + 1);
            btn.title = `Question ${q.sectionPosition || ''} - ${statusToLabel(status)}`;
            btn.addEventListener('click', () => jumpToQuestion(parseInt(btn.dataset.position)));
            container.appendChild(btn);
        });
    });
}

function updatePaletteButton(examQuestionId, status) {
    const btn = document.getElementById(`palette-q-${examQuestionId}`);
    if (!btn) return;
    // Remove all state classes
    btn.classList.remove('not-visited', 'not-answered', 'answered', 'marked-review', 'answered-marked');
    btn.classList.add(statusToPaletteClass(status));
    btn.title = `Question - ${statusToLabel(status)}`;
}

function updatePaletteCurrent() {
    document.querySelectorAll('.palette-btn.current').forEach(b => b.classList.remove('current'));
    const q = getCurrentQuestion();
    if (q) {
        const btn = document.getElementById(`palette-q-${q.examQuestionId}`);
        if (btn) btn.classList.add('current');
    }
}

function statusToPaletteClass(status) {
    const map = {
        'NOT_VISITED': 'not-visited',
        'NOT_ANSWERED': 'not-answered',
        'ANSWERED': 'answered',
        'MARKED_FOR_REVIEW': 'marked-review',
        'ANSWERED_MARKED': 'answered-marked'
    };
    return map[status] || 'not-visited';
}

function statusToLabel(status) {
    const map = {
        'NOT_VISITED': 'Not Visited',
        'NOT_ANSWERED': 'Not Answered',
        'ANSWERED': 'Answered',
        'MARKED_FOR_REVIEW': 'Marked for Review',
        'ANSWERED_MARKED': 'Answered & Marked'
    };
    return map[status] || 'Not Visited';
}

// ============================================================
// SIDEBAR STATS
// ============================================================
function updateSidebarStats() {
    const answers = ExamApp.examState.answers || {};
    const questions = ExamApp.examState.questions || [];
    const total = questions.length;

    let answered = 0, notAnswered = 0, notVisited = 0, markedReview = 0, answeredMarked = 0;
    questions.forEach(q => {
        const a = answers[String(q.examQuestionId)];
        const st = a ? a.status : 'NOT_VISITED';
        if (st === 'ANSWERED') answered++;
        else if (st === 'NOT_ANSWERED') notAnswered++;
        else if (st === 'NOT_VISITED') notVisited++;
        else if (st === 'MARKED_FOR_REVIEW') markedReview++;
        else if (st === 'ANSWERED_MARKED') { answeredMarked++; answered++; }
    });

    setText('stat-total', total);
    setText('stat-answered', answered);
    setText('stat-not-answered', notAnswered);
    setText('stat-not-visited', notVisited);
    setText('stat-marked', markedReview + answeredMarked);

    // Overall progress
    const pct = total > 0 ? Math.round((answered / total) * 100) : 0;
    const progressBar = document.getElementById('overall-progress-bar');
    if (progressBar) {
        progressBar.style.width = pct + '%';
        progressBar.textContent = pct + '%';
    }

    // Section progress
    updateSectionProgressBadges();
}

function updateSectionProgressBadges() {
    ExamApp.sections.forEach(sec => {
        const secQs = ExamApp.examState.questions.filter(q => q.section === sec);
        let ans = 0;
        secQs.forEach(q => {
            const a = ExamApp.examState.answers[String(q.examQuestionId)];
            if (a && (a.status === 'ANSWERED' || a.status === 'ANSWERED_MARKED')) ans++;
        });
        const badge = document.getElementById(`sec-count-${sec}`);
        if (badge) badge.textContent = `${ans}/${secQs.length}`;
        const fill = document.getElementById(`sec-fill-${sec}`);
        if (fill && secQs.length > 0) fill.style.width = `${Math.round((ans / secQs.length) * 100)}%`;
    });
}

function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
}

// ============================================================
// SUBMIT
// ============================================================
function showSubmitModal() {
    // Update submit modal stats
    const answers = ExamApp.examState.answers || {};
    const questions = ExamApp.examState.questions || [];
    let answered = 0, notAnswered = 0, notVisited = 0, markedReview = 0;

    questions.forEach(q => {
        const a = answers[String(q.examQuestionId)];
        const st = a ? a.status : 'NOT_VISITED';
        if (st === 'ANSWERED' || st === 'ANSWERED_MARKED') answered++;
        else if (st === 'NOT_ANSWERED') notAnswered++;
        else if (st === 'NOT_VISITED') notVisited++;
        else if (st === 'MARKED_FOR_REVIEW') markedReview++;
    });

    setText('modal-answered', answered);
    setText('modal-not-answered', notAnswered);
    setText('modal-not-visited', notVisited);
    setText('modal-marked', markedReview);

    const modal = new bootstrap.Modal(document.getElementById('submitModal'));
    modal.show();
}

async function confirmSubmit() {
    if (ExamApp.submitting) return;
    ExamApp.submitting = true;
    clearInterval(ExamApp.timerInterval);
    clearInterval(ExamApp.syncInterval);

    const submitBtn = document.getElementById('confirm-submit-btn');
    if (submitBtn) { submitBtn.disabled = true; submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Submitting...'; }

    try {
        const resp = await fetch(`/exam/${ExamApp.sessionId}/submit`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [ExamApp.csrfHeader]: ExamApp.csrfToken
            }
        });

        if (resp.redirected) {
            window.location.href = resp.url;
            return;
        }

        if (resp.ok) {
            window.location.href = `/exam/${ExamApp.sessionId}/result`;
        } else {
            console.error('Submit failed:', resp.status);
            ExamApp.submitting = false;
            if (submitBtn) { submitBtn.disabled = false; submitBtn.innerHTML = 'Submit Test'; }
        }
    } catch (e) {
        console.error('Submit error:', e);
        ExamApp.submitting = false;
        if (submitBtn) { submitBtn.disabled = false; submitBtn.innerHTML = 'Submit Test'; }
    }
}

async function autoSubmitExam() {
    ExamApp.submitting = true;
    clearInterval(ExamApp.timerInterval);
    clearInterval(ExamApp.syncInterval);
    try {
        const resp = await fetch(`/api/exam/${ExamApp.sessionId}/auto-submit`, {
            method: 'POST',
            headers: { [ExamApp.csrfHeader]: ExamApp.csrfToken }
        });
        const data = await resp.json();
        if (data.redirectUrl) {
            setTimeout(() => { window.location.href = data.redirectUrl; }, 1500);
        } else {
            setTimeout(() => { window.location.href = `/exam/${ExamApp.sessionId}/result`; }, 1500);
        }
    } catch (e) {
        setTimeout(() => { window.location.href = `/exam/${ExamApp.sessionId}/result`; }, 2000);
    }
}

// ============================================================
// REVIEW
// ============================================================
function goToReview() {
    clearInterval(ExamApp.syncInterval);
    window.location.href = `/exam/${ExamApp.sessionId}/review`;
}

// ============================================================
// INDICATORS
// ============================================================
let saveIndicatorTimer = null;

function showSaveIndicator() {
    const el = document.getElementById('save-indicator');
    if (el) {
        el.classList.add('visible');
        clearTimeout(saveIndicatorTimer);
        saveIndicatorTimer = setTimeout(() => el.classList.remove('visible'), 1500);
    }
}

function hideSaveIndicator() {
    // Handled by timer above
}

function showOfflineBar() {
    const bar = document.getElementById('offline-bar');
    if (bar) bar.classList.add('visible');
}

function hideOfflineBar() {
    const bar = document.getElementById('offline-bar');
    if (bar) bar.classList.remove('visible');
}

// ============================================================
// KEYBOARD SHORTCUTS
// ============================================================
document.addEventListener('keydown', e => {
    // Prevent shortcuts when typing in input
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;

    if (e.key === 'ArrowRight' || e.key === 'ArrowDown') { e.preventDefault(); goToNext(); }
    else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') { e.preventDefault(); goToPrev(); }
    else if (e.key >= '1' && e.key <= '4') {
        const idx = parseInt(e.key) - 1;
        selectOption(idx);
    }
    else if (e.key === 'Enter') { saveAndNext(); }
    else if (e.key === 'Delete' || e.key === 'Backspace') { e.preventDefault(); clearResponse(); }
});
