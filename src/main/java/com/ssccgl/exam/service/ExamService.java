package com.ssccgl.exam.service;

import com.ssccgl.exam.dto.*;
import com.ssccgl.exam.entity.*;
import com.ssccgl.exam.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private static final Logger logger = LoggerFactory.getLogger(ExamService.class);
    private static final int QUESTIONS_PER_SECTION = 25;
    private static final int EXAM_DURATION_SECONDS = 3600;
    private static final double MARKS_CORRECT = 2.0;
    private static final double MARKS_WRONG = -0.33;

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private ExamQuestionRepository examQuestionRepository;

    @Autowired
    private CandidateAnswerRepository candidateAnswerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ExamSession startNewExam(User user, String ipAddress) {
        // Check for existing in-progress session
        Optional<ExamSession> existing = examSessionRepository.findByUserAndStatus(user, ExamSession.Status.IN_PROGRESS);
        if (existing.isPresent()) {
            ExamSession session = existing.get();
            // Recalculate remaining time
            long elapsed = ChronoUnit.SECONDS.between(session.getStartTime(), LocalDateTime.now());
            int remaining = (int) Math.max(0, EXAM_DURATION_SECONDS - elapsed);
            session.setTimeRemainingSeconds(remaining);
            if (remaining <= 0) {
                autoSubmitExam(session);
                return null;
            }
            return session;
        }

        Integer maxAttempt = examSessionRepository.findMaxAttemptNumberByUser(user);
        int attemptNumber = (maxAttempt == null ? 0 : maxAttempt) + 1;

        ExamSession session = new ExamSession();
        session.setUser(user);
        session.setStartTime(LocalDateTime.now());
        session.setDurationSeconds(EXAM_DURATION_SECONDS);
        session.setTimeRemainingSeconds(EXAM_DURATION_SECONDS);
        session.setStatus(ExamSession.Status.IN_PROGRESS);
        session.setIpAddress(ipAddress);
        session.setAttemptNumber(attemptNumber);
        session = examSessionRepository.save(session);

        // Generate question paper
        generateQuestionPaper(session);

        // Initialize candidate answers
        initializeCandidateAnswers(session);

        logger.info("New exam session started for user: {}, session: {}", user.getUsername(), session.getId());
        return session;
    }

    private void generateQuestionPaper(ExamSession session) {
        Question.Section[] sections = {
            Question.Section.GENERAL_AWARENESS,
            Question.Section.VERBAL_ABILITY,
            Question.Section.LOGICAL_REASONING,
            Question.Section.QUANTITATIVE_APTITUDE
        };

        int globalPosition = 0;
        List<ExamQuestion> examQuestions = new ArrayList<>();

        for (Question.Section section : sections) {
            List<Question> sectionQuestions = questionRepository.findRandomBySection(section, QUESTIONS_PER_SECTION);

            if (sectionQuestions.size() < QUESTIONS_PER_SECTION) {
                logger.warn("Not enough questions for section {}. Found: {}", section, sectionQuestions.size());
                List<Question> allSectionQ = questionRepository.findBySection(section);
                Collections.shuffle(allSectionQ);
                sectionQuestions = allSectionQ.stream().limit(QUESTIONS_PER_SECTION).collect(Collectors.toList());
            }

            for (int i = 0; i < sectionQuestions.size(); i++) {
                Question q = sectionQuestions.get(i);
                ExamQuestion eq = new ExamQuestion();
                eq.setSession(session);
                eq.setQuestion(q);
                eq.setPosition(globalPosition++);
                eq.setSection(section);
                eq.setSectionPosition(i);

                // Shuffle options
                int[] originalIndices = {0, 1, 2, 3};
                List<Integer> idxList = new ArrayList<>();
                for (int idx : originalIndices) idxList.add(idx);
                Collections.shuffle(idxList);

                StringBuilder shuffleMap = new StringBuilder();
                int shuffledCorrect = 0;
                for (int k = 0; k < idxList.size(); k++) {
                    if (k > 0) shuffleMap.append(",");
                    shuffleMap.append(idxList.get(k));
                    if (idxList.get(k) == q.getCorrectAnswer()) {
                        shuffledCorrect = k;
                    }
                }
                eq.setOptionShuffleMap(shuffleMap.toString());
                eq.setShuffledCorrectAnswer(shuffledCorrect);

                examQuestions.add(eq);
            }
        }

        examQuestionRepository.saveAll(examQuestions);
        logger.info("Generated {} questions for session {}", examQuestions.size(), session.getId());
    }

    private void initializeCandidateAnswers(ExamSession session) {
        List<ExamQuestion> examQuestions = examQuestionRepository.findBySessionOrderByPosition(session);
        List<CandidateAnswer> answers = new ArrayList<>();

        for (ExamQuestion eq : examQuestions) {
            CandidateAnswer ans = new CandidateAnswer();
            ans.setSession(session);
            ans.setExamQuestion(eq);
            ans.setAnswerStatus(CandidateAnswer.AnswerStatus.NOT_VISITED);
            answers.add(ans);
        }

        candidateAnswerRepository.saveAll(answers);
    }

    @Transactional(readOnly = true)
    public ExamStateDto getExamState(ExamSession session) {
        List<ExamQuestion> examQuestions = examQuestionRepository.findBySessionWithQuestions(session);
        List<CandidateAnswer> candidateAnswers = candidateAnswerRepository.findBySession(session);

        Map<Long, CandidateAnswer> answerMap = new HashMap<>();
        for (CandidateAnswer ca : candidateAnswers) {
            answerMap.put(ca.getExamQuestion().getId(), ca);
        }

        ExamStateDto state = new ExamStateDto();
        state.setSessionId(session.getId());
        state.setCurrentPosition(session.getCurrentQuestionIndex());

        // Recalculate remaining time server-side
        long elapsed = ChronoUnit.SECONDS.between(session.getStartTime(), LocalDateTime.now());
        int remaining = (int) Math.max(0, EXAM_DURATION_SECONDS - elapsed);
        state.setTimeRemainingSeconds(remaining);

        List<QuestionDto> questionDtos = new ArrayList<>();
        Map<Integer, AnswerDto> answers = new HashMap<>();

        int totalAnswered = 0, totalNotAnswered = 0, totalNotVisited = 0, totalMarked = 0, totalAnsweredMarked = 0;

        for (ExamQuestion eq : examQuestions) {
            QuestionDto qDto = new QuestionDto();
            qDto.setExamQuestionId(eq.getId());
            qDto.setPosition(eq.getPosition());
            qDto.setSection(eq.getSection().name());
            qDto.setSectionPosition(eq.getSectionPosition());
            qDto.setQuestionText(eq.getQuestion().getQuestionText());
            qDto.setTopic(eq.getQuestion().getTopic());
            qDto.setDifficulty(eq.getQuestion().getDifficulty().name());

            // Build shuffled options list
            int[] shuffleMap = eq.getShuffleMapArray();
            List<String> options = new ArrayList<>();
            for (int idx : shuffleMap) {
                options.add(eq.getQuestion().getOption(idx));
            }
            qDto.setOptions(options);
            questionDtos.add(qDto);

            CandidateAnswer ca = answerMap.get(eq.getId());
            if (ca != null) {
                AnswerDto aDto = new AnswerDto();
                aDto.setExamQuestionId(eq.getId());
                aDto.setSelectedOption(ca.getSelectedOption());
                aDto.setStatus(ca.getAnswerStatus().name());
                aDto.setMarkedForReview(ca.isMarkedForReview());
                answers.put(eq.getPosition(), aDto);

                switch (ca.getAnswerStatus()) {
                    case ANSWERED -> totalAnswered++;
                    case NOT_ANSWERED -> totalNotAnswered++;
                    case NOT_VISITED -> totalNotVisited++;
                    case MARKED_FOR_REVIEW -> totalMarked++;
                    case ANSWERED_MARKED -> totalAnsweredMarked++;
                }
            }
        }

        state.setQuestions(questionDtos);
        state.setAnswers(answers);
        state.setTotalAnswered(totalAnswered);
        state.setTotalNotAnswered(totalNotAnswered);
        state.setTotalNotVisited(totalNotVisited);
        state.setTotalMarkedForReview(totalMarked);
        state.setTotalAnsweredMarked(totalAnsweredMarked);

        // Section progress
        Map<String, SectionProgressDto> sectionProgress = new LinkedHashMap<>();
        Question.Section[] sections = Question.Section.values();
        int sectionStart = 0;
        for (Question.Section sec : sections) {
            SectionProgressDto spd = new SectionProgressDto();
            spd.setSectionName(sec.name());
            spd.setDisplayName(getSectionDisplayName(sec.name()));
            spd.setTotal(QUESTIONS_PER_SECTION);
            spd.setStartPosition(sectionStart);

            int secAnswered = 0, secNotAnswered = 0, secNotVisited = 0, secMarked = 0, secAnsweredMarked = 0;
            for (ExamQuestion eq : examQuestions) {
                if (eq.getSection() == sec) {
                    CandidateAnswer ca = answerMap.get(eq.getId());
                    if (ca != null) {
                        switch (ca.getAnswerStatus()) {
                            case ANSWERED -> secAnswered++;
                            case NOT_ANSWERED -> secNotAnswered++;
                            case NOT_VISITED -> secNotVisited++;
                            case MARKED_FOR_REVIEW -> secMarked++;
                            case ANSWERED_MARKED -> secAnsweredMarked++;
                        }
                    }
                }
            }
            spd.setAnswered(secAnswered);
            spd.setNotAnswered(secNotAnswered);
            spd.setNotVisited(secNotVisited);
            spd.setMarkedForReview(secMarked);
            spd.setAnsweredMarked(secAnsweredMarked);
            sectionProgress.put(sec.name(), spd);
            sectionStart += QUESTIONS_PER_SECTION;
        }
        state.setSectionProgress(sectionProgress);

        return state;
    }

    @Transactional
    public void saveAnswer(ExamSession session, Long examQuestionId, Integer selectedOption,
                          String action, Long timeTaken) {
        ExamQuestion eq = examQuestionRepository.findById(examQuestionId)
            .orElseThrow(() -> new IllegalArgumentException("ExamQuestion not found: " + examQuestionId));

        if (!eq.getSession().getId().equals(session.getId())) {
            throw new SecurityException("Question does not belong to this session");
        }

        CandidateAnswer answer = candidateAnswerRepository.findBySessionAndExamQuestion(session, eq)
            .orElseGet(() -> {
                CandidateAnswer ca = new CandidateAnswer();
                ca.setSession(session);
                ca.setExamQuestion(eq);
                return ca;
            });

        if (answer.getVisitedAt() == null) {
            answer.setVisitedAt(LocalDateTime.now());
        }

        answer.setTimeTakenSeconds(timeTaken != null ? timeTaken.intValue() : null);

        switch (action) {
            case "SAVE_NEXT", "SAVE" -> {
                if (selectedOption != null) {
                    answer.setSelectedOption(selectedOption);
                    answer.setAnswerStatus(answer.isMarkedForReview()
                        ? CandidateAnswer.AnswerStatus.ANSWERED_MARKED
                        : CandidateAnswer.AnswerStatus.ANSWERED);
                    answer.setAnsweredAt(LocalDateTime.now());
                } else {
                    answer.setAnswerStatus(CandidateAnswer.AnswerStatus.NOT_ANSWERED);
                }
            }
            case "MARK_REVIEW" -> {
                if (selectedOption != null) {
                    answer.setSelectedOption(selectedOption);
                    answer.setAnswerStatus(CandidateAnswer.AnswerStatus.ANSWERED_MARKED);
                    answer.setMarkedForReview(true);
                    answer.setAnsweredAt(LocalDateTime.now());
                } else {
                    answer.setAnswerStatus(CandidateAnswer.AnswerStatus.MARKED_FOR_REVIEW);
                    answer.setMarkedForReview(true);
                }
            }
            case "CLEAR" -> {
                answer.setSelectedOption(null);
                answer.setMarkedForReview(false);
                answer.setAnswerStatus(CandidateAnswer.AnswerStatus.NOT_ANSWERED);
            }
            case "VISIT" -> {
                if (answer.getAnswerStatus() == CandidateAnswer.AnswerStatus.NOT_VISITED) {
                    answer.setAnswerStatus(CandidateAnswer.AnswerStatus.NOT_ANSWERED);
                }
            }
        }

        candidateAnswerRepository.save(answer);

        // Update session position
        examSessionRepository.updateTimeRemaining(session.getId(),
            (int) Math.max(0, EXAM_DURATION_SECONDS - ChronoUnit.SECONDS.between(session.getStartTime(), LocalDateTime.now())));
    }

    @Transactional
    public ResultDto submitExam(ExamSession session, boolean autoSubmit) {
        if (session.getStatus() != ExamSession.Status.IN_PROGRESS) {
            // Already submitted - return existing result
            Optional<Result> existingResult = resultRepository.findBySession(session);
            if (existingResult.isPresent()) {
                return buildResultDto(session, existingResult.get(),
                    candidateAnswerRepository.findBySessionWithDetails(session));
            }
        }

        session.setStatus(autoSubmit ? ExamSession.Status.TIMED_OUT : ExamSession.Status.SUBMITTED);
        session.setSubmittedAt(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now());
        session.setAutoSubmitted(autoSubmit);

        long elapsed = ChronoUnit.SECONDS.between(session.getStartTime(), session.getSubmittedAt());
        session.setTimeRemainingSeconds((int) Math.max(0, EXAM_DURATION_SECONDS - elapsed));
        examSessionRepository.save(session);

        // Calculate results
        List<CandidateAnswer> answers = candidateAnswerRepository.findBySessionWithDetails(session);
        Result result = calculateResult(session, answers);

        // Update user stats
        userRepository.incrementAttempts(session.getUser().getId());
        userRepository.updateBestScore(session.getUser().getId(), result.getTotalScore());

        logger.info("Exam submitted for session: {}, score: {}", session.getId(), result.getTotalScore());
        return buildResultDto(session, result, answers);
    }

    @Transactional
    public void autoSubmitExam(ExamSession session) {
        if (session.getStatus() == ExamSession.Status.IN_PROGRESS) {
            submitExam(session, true);
        }
    }

    private Result calculateResult(ExamSession session, List<CandidateAnswer> answers) {
        Map<String, int[]> sectionStats = new LinkedHashMap<>();
        String[] sections = {"GENERAL_AWARENESS", "VERBAL_ABILITY", "LOGICAL_REASONING", "QUANTITATIVE_APTITUDE"};
        for (String s : sections) {
            sectionStats.put(s, new int[]{0, 0, 0}); // [correct, incorrect, unattempted]
        }

        int totalCorrect = 0, totalIncorrect = 0, totalUnattempted = 0;
        double totalScore = 0;

        for (CandidateAnswer ca : answers) {
            ExamQuestion eq = ca.getExamQuestion();
            String sectionName = eq.getSection().name();
            int[] stats = sectionStats.get(sectionName);

            boolean attempted = ca.getSelectedOption() != null
                && ca.getAnswerStatus() != CandidateAnswer.AnswerStatus.NOT_VISITED
                && ca.getAnswerStatus() != CandidateAnswer.AnswerStatus.NOT_ANSWERED
                && ca.getAnswerStatus() != CandidateAnswer.AnswerStatus.MARKED_FOR_REVIEW;

            if (!attempted) {
                stats[2]++;
                totalUnattempted++;
                ca.setMarksObtained(0.0);
                ca.setCorrect(null);
            } else {
                if (ca.getSelectedOption() == eq.getShuffledCorrectAnswer()) {
                    stats[0]++;
                    totalCorrect++;
                    totalScore += MARKS_CORRECT;
                    ca.setMarksObtained(MARKS_CORRECT);
                    ca.setCorrect(true);
                } else {
                    stats[1]++;
                    totalIncorrect++;
                    totalScore += MARKS_WRONG;
                    ca.setMarksObtained(MARKS_WRONG);
                    ca.setCorrect(false);
                }
            }
        }

        candidateAnswerRepository.saveAll(answers);

        long elapsed = ChronoUnit.SECONDS.between(session.getStartTime(), session.getSubmittedAt());

        Result result = new Result();
        result.setSession(session);
        result.setUser(session.getUser());
        result.setTotalScore(Math.max(0, totalScore));
        result.setMaxMarks(200.0);
        result.setTotalCorrect(totalCorrect);
        result.setTotalIncorrect(totalIncorrect);
        result.setTotalUnattempted(totalUnattempted);
        result.setTotalAttempted(totalCorrect + totalIncorrect);
        result.setAttemptNumber(session.getAttemptNumber());
        result.setTimeTakenSeconds((int) Math.min(elapsed, EXAM_DURATION_SECONDS));

        int totalAttempted = totalCorrect + totalIncorrect;
        result.setAccuracyPercentage(totalAttempted > 0 ? (double) totalCorrect / totalAttempted * 100 : 0);
        result.setAttemptPercentage((double) totalAttempted / 100 * 100);

        // Calculate percentile
        long betterCount = resultRepository.countBetterScores(result.getTotalScore());
        long totalCount = resultRepository.countTotal();
        double percentile = totalCount > 0 ? (1.0 - (double) betterCount / totalCount) * 100 : 100.0;
        result.setPercentile(Math.min(100.0, percentile));

        // Build section scores JSON
        StringBuilder sectionJson = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, int[]> entry : sectionStats.entrySet()) {
            if (!first) sectionJson.append(",");
            int[] s = entry.getValue();
            double sectionScore = s[0] * MARKS_CORRECT + s[1] * MARKS_WRONG;
            sectionScore = Math.max(0, sectionScore);
            sectionJson.append(String.format("\"%s\":{\"correct\":%d,\"incorrect\":%d,\"unattempted\":%d,\"score\":%.2f}",
                entry.getKey(), s[0], s[1], s[2], sectionScore));
            first = false;
        }
        sectionJson.append("}");
        result.setSectionScoresJson(sectionJson.toString());

        return resultRepository.save(result);
    }

    private ResultDto buildResultDto(ExamSession session, Result result, List<CandidateAnswer> answers) {
        ResultDto dto = new ResultDto();
        dto.setSessionId(session.getId());
        dto.setResultId(result.getId());
        dto.setCandidateName(session.getUser().getFullName());
        dto.setTotalScore(result.getTotalScore());
        dto.setMaxMarks(result.getMaxMarks());
        dto.setTotalCorrect(result.getTotalCorrect());
        dto.setTotalIncorrect(result.getTotalIncorrect());
        dto.setTotalUnattempted(result.getTotalUnattempted());
        dto.setTotalAttempted(result.getTotalAttempted());
        dto.setAccuracyPercentage(result.getAccuracyPercentage());
        dto.setAttemptPercentage(result.getAttemptPercentage());
        dto.setPercentile(result.getPercentile());
        dto.setTimeTakenSeconds(result.getTimeTakenSeconds());
        dto.setAttemptNumber(result.getAttemptNumber());
        dto.setSubmittedAt(session.getSubmittedAt());
        dto.setAutoSubmitted(session.isAutoSubmitted());

        // Parse section scores
        Map<String, SectionScoreDto> sectionScores = new LinkedHashMap<>();
        String json = result.getSectionScoresJson();
        if (json != null && !json.isEmpty()) {
            String[] sections = {"GENERAL_AWARENESS", "VERBAL_ABILITY", "LOGICAL_REASONING", "QUANTITATIVE_APTITUDE"};
            for (String sec : sections) {
                SectionScoreDto ssd = parseSectionScore(json, sec);
                ssd.setDisplayName(getSectionDisplayName(sec));
                sectionScores.put(sec, ssd);
            }
        }
        dto.setSectionScores(sectionScores);

        // Build question reviews
        List<QuestionReviewDto> reviews = new ArrayList<>();
        Map<Long, CandidateAnswer> answerMap = new HashMap<>();
        for (CandidateAnswer ca : answers) {
            answerMap.put(ca.getExamQuestion().getId(), ca);
        }

        List<ExamQuestion> examQuestions = examQuestionRepository.findBySessionWithQuestions(session);
        for (ExamQuestion eq : examQuestions) {
            QuestionReviewDto review = new QuestionReviewDto();
            review.setPosition(eq.getPosition() + 1);
            review.setSection(eq.getSection().name());
            review.setSectionDisplayName(getSectionDisplayName(eq.getSection().name()));
            review.setQuestionText(eq.getQuestion().getQuestionText());
            review.setExplanation(eq.getQuestion().getExplanation());
            review.setTopic(eq.getQuestion().getTopic());

            int[] shuffleMap = eq.getShuffleMapArray();
            List<String> options = new ArrayList<>();
            for (int idx : shuffleMap) options.add(eq.getQuestion().getOption(idx));
            review.setOptions(options);
            review.setCorrectOption(eq.getShuffledCorrectAnswer());

            CandidateAnswer ca = answerMap.get(eq.getId());
            if (ca != null) {
                review.setSelectedOption(ca.getSelectedOption());
                review.setStatus(ca.getAnswerStatus().name());
                review.setMarksObtained(ca.getMarksObtained() != null ? ca.getMarksObtained() : 0.0);

                boolean attempted = ca.getSelectedOption() != null
                    && ca.getAnswerStatus() != CandidateAnswer.AnswerStatus.NOT_VISITED
                    && ca.getAnswerStatus() != CandidateAnswer.AnswerStatus.NOT_ANSWERED
                    && ca.getAnswerStatus() != CandidateAnswer.AnswerStatus.MARKED_FOR_REVIEW;
                review.setAttempted(attempted);
                review.setCorrect(attempted && ca.getSelectedOption() == eq.getShuffledCorrectAnswer());
            }
            reviews.add(review);
        }
        dto.setQuestionReviews(reviews);
        return dto;
    }

    private SectionScoreDto parseSectionScore(String json, String section) {
        SectionScoreDto ssd = new SectionScoreDto();
        ssd.setSectionName(section);
        ssd.setMaxScore(50.0);
        try {
            int startIdx = json.indexOf("\"" + section + "\":{");
            if (startIdx >= 0) {
                int contentStart = json.indexOf("{", startIdx + section.length() + 2);
                int contentEnd = json.indexOf("}", contentStart);
                String content = json.substring(contentStart + 1, contentEnd);
                Map<String, String> map = parseSimpleJson(content);
                ssd.setCorrect(Integer.parseInt(map.getOrDefault("correct", "0")));
                ssd.setIncorrect(Integer.parseInt(map.getOrDefault("incorrect", "0")));
                ssd.setUnattempted(Integer.parseInt(map.getOrDefault("unattempted", "0")));
                ssd.setAttempted(ssd.getCorrect() + ssd.getIncorrect());
                ssd.setScore(Double.parseDouble(map.getOrDefault("score", "0")));
                ssd.setAccuracy(ssd.getAttempted() > 0 ? (double) ssd.getCorrect() / ssd.getAttempted() * 100 : 0);
            }
        } catch (Exception e) {
            logger.warn("Error parsing section score for {}: {}", section, e.getMessage());
        }
        return ssd;
    }

    private Map<String, String> parseSimpleJson(String content) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = content.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }

    @Transactional(readOnly = true)
    public ResultDto getResultBySessionId(Long sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId).orElse(null);
        if (session == null) return null;
        Result result = resultRepository.findBySession(session).orElse(null);
        if (result == null) return null;
        List<CandidateAnswer> answers = candidateAnswerRepository.findBySessionWithDetails(session);
        return buildResultDto(session, result, answers);
    }

    @Transactional(readOnly = true)
    public List<ResultDto> getResultHistory(User user) {
        List<ExamSession> sessions = examSessionRepository.findByUserOrderByStartTimeDesc(user);
        List<ResultDto> history = new ArrayList<>();
        for (ExamSession session : sessions) {
            if (session.getStatus() == ExamSession.Status.SUBMITTED ||
                session.getStatus() == ExamSession.Status.TIMED_OUT) {
                Result result = resultRepository.findBySession(session).orElse(null);
                if (result != null) {
                    ResultDto dto = new ResultDto();
                    dto.setSessionId(session.getId());
                    dto.setResultId(result.getId());
                    dto.setTotalScore(result.getTotalScore());
                    dto.setMaxMarks(result.getMaxMarks());
                    dto.setTotalCorrect(result.getTotalCorrect());
                    dto.setTotalIncorrect(result.getTotalIncorrect());
                    dto.setTotalAttempted(result.getTotalAttempted());
                    dto.setAccuracyPercentage(result.getAccuracyPercentage());
                    dto.setAttemptPercentage(result.getAttemptPercentage());
                    dto.setPercentile(result.getPercentile());
                    dto.setTimeTakenSeconds(result.getTimeTakenSeconds());
                    dto.setAttemptNumber(result.getAttemptNumber());
                    dto.setSubmittedAt(session.getSubmittedAt());
                    dto.setAutoSubmitted(session.isAutoSubmitted());
                    history.add(dto);
                }
            }
        }
        return history;
    }

    @Transactional(readOnly = true)
    public ExamSession getActiveSession(User user) {
        return examSessionRepository.findByUserAndStatus(user, ExamSession.Status.IN_PROGRESS).orElse(null);
    }

    @Transactional(readOnly = true)
    public ExamSession getSessionById(Long sessionId) {
        return examSessionRepository.findById(sessionId).orElse(null);
    }

    @Transactional
    public void updateSessionPosition(Long sessionId, int position, String section) {
        examSessionRepository.updateCurrentPosition(sessionId, position, section);
    }

    public String getSectionDisplayName(String section) {
        return switch (section) {
            case "GENERAL_AWARENESS" -> "General Awareness";
            case "VERBAL_ABILITY" -> "English Comprehension";
            case "LOGICAL_REASONING" -> "General Intelligence";
            case "QUANTITATIVE_APTITUDE" -> "Quantitative Aptitude";
            default -> section;
        };
    }
}
