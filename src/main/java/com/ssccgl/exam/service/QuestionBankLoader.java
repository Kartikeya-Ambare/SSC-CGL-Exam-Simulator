package com.ssccgl.exam.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssccgl.exam.entity.Question;
import com.ssccgl.exam.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionBankLoader implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(QuestionBankLoader.class);

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("=== Starting Question Bank Import ===");

        long totalBefore = questionRepository.count();

        loadSection("classpath:question-bank/general-awareness.json", Question.Section.GENERAL_AWARENESS);
        loadSection("classpath:question-bank/verbal-ability.json", Question.Section.VERBAL_ABILITY);
        loadSection("classpath:question-bank/logical-reasoning.json", Question.Section.LOGICAL_REASONING);
        loadSection("classpath:question-bank/quantitative-aptitude.json", Question.Section.QUANTITATIVE_APTITUDE);

        long totalAfter = questionRepository.count();
        logger.info("=== Question Bank Import Complete. Total: {} questions (added: {}) ===",
            totalAfter, totalAfter - totalBefore);

        // Print section stats
        for (Question.Section sec : Question.Section.values()) {
            long count = questionRepository.countBySection(sec);
            logger.info("  {} : {} questions", sec.name(), count);
        }
    }

    @Transactional
    public void loadSection(String resourcePath, Question.Section section) {
        try {
            Resource resource = resourceLoader.getResource(resourcePath);
            if (!resource.exists()) {
                logger.warn("Question bank file not found: {}", resourcePath);
                return;
            }

            InputStream is = resource.getInputStream();
            JsonNode rootNode = objectMapper.readTree(is);
            JsonNode questionsNode = rootNode.isArray() ? rootNode : rootNode.get("questions");

            if (questionsNode == null || !questionsNode.isArray()) {
                logger.error("Invalid JSON format in {}", resourcePath);
                return;
            }

            int imported = 0, skipped = 0, errors = 0;
            List<Question> batch = new ArrayList<>();

            for (JsonNode node : questionsNode) {
                try {
                    long jsonId = node.get("id").asLong();

                    if (questionRepository.existsByJsonId(jsonId)) {
                        skipped++;
                        continue;
                    }

                    Question q = new Question();
                    q.setJsonId(jsonId);
                    q.setSection(section);
                    q.setTopic(node.has("topic") ? node.get("topic").asText() : "General");
                    q.setQuestionText(node.get("questionText").asText());

                    JsonNode sectionNode = node.get("section");
                    if (sectionNode != null) {
                        try {
                            q.setSection(Question.Section.valueOf(sectionNode.asText()));
                        } catch (Exception ignored) {
                            q.setSection(section);
                        }
                    }

                    String diffStr = node.has("difficulty") ? node.get("difficulty").asText() : "MEDIUM";
                    try {
                        q.setDifficulty(Question.Difficulty.valueOf(diffStr.toUpperCase()));
                    } catch (Exception e) {
                        q.setDifficulty(Question.Difficulty.MEDIUM);
                    }

                    JsonNode optionsNode = node.get("options");
                    if (optionsNode == null || !optionsNode.isArray() || optionsNode.size() < 4) {
                        errors++;
                        continue;
                    }

                    q.setOptionA(optionsNode.get(0).asText());
                    q.setOptionB(optionsNode.get(1).asText());
                    q.setOptionC(optionsNode.get(2).asText());
                    q.setOptionD(optionsNode.get(3).asText());
                    q.setCorrectAnswer(node.get("correctAnswer").asInt());

                    if (node.has("explanation")) {
                        q.setExplanation(node.get("explanation").asText());
                    }

                    batch.add(q);
                    imported++;

                    if (batch.size() >= 100) {
                        questionRepository.saveAll(batch);
                        batch.clear();
                    }
                } catch (Exception e) {
                    errors++;
                    logger.debug("Error importing question: {}", e.getMessage());
                }
            }

            if (!batch.isEmpty()) {
                questionRepository.saveAll(batch);
            }

            logger.info("Section {}: imported={}, skipped={}, errors={}", section.name(), imported, skipped, errors);

        } catch (Exception e) {
            logger.error("Failed to load section {}: {}", section.name(), e.getMessage(), e);
        }
    }
}
