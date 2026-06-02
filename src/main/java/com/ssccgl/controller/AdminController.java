package com.ssccgl.controller;

import com.ssccgl.dto.ExamDtos.QuestionFormDto;
import com.ssccgl.entity.Question;
import com.ssccgl.entity.User;
import com.ssccgl.enums.Difficulty;
import com.ssccgl.enums.Section;
import com.ssccgl.repository.ExamAttemptRepository;
import com.ssccgl.repository.QuestionRepository;
import com.ssccgl.repository.ResultRepository;
import com.ssccgl.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Admin Panel — secured with ROLE_ADMIN only.
 * Routes: /admin/**
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminController.class);

    public AdminController(QuestionRepository questionRepo,
                           UserRepository userRepo,
                           ResultRepository resultRepo,
                           ExamAttemptRepository attemptRepo,
                           com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.questionRepo = questionRepo;
        this.userRepo     = userRepo;
        this.resultRepo   = resultRepo;
        this.attemptRepo  = attemptRepo;
        this.objectMapper = objectMapper;
    }


    private final QuestionRepository  questionRepo;
    private final UserRepository      userRepo;
    private final ResultRepository    resultRepo;
    private final ExamAttemptRepository attemptRepo;
    private final ObjectMapper        objectMapper;

    // ────────────────────────────────────────────────────────────
    //  Dashboard
    // ────────────────────────────────────────────────────────────
    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalQuestions",  questionRepo.count());
        model.addAttribute("totalUsers",      userRepo.count());
        model.addAttribute("totalAttempts",   attemptRepo.count());
        model.addAttribute("sectionCounts",   getSectionCounts());
        return "admin/dashboard";
    }

    // ────────────────────────────────────────────────────────────
    //  Questions List
    // ────────────────────────────────────────────────────────────
    @GetMapping("/questions")
    public String listQuestions(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "")  String section,
            @RequestParam(defaultValue = "")  String difficulty,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        List<Question> questions;

        if (!keyword.isBlank()) {
            questions = questionRepo.searchByKeyword(keyword);
        } else if (!section.isBlank()) {
            Section sec = Section.valueOf(section);
            questions = !difficulty.isBlank()
                ? questionRepo.findBySectionAndDifficultyAndActiveTrue(sec, Difficulty.valueOf(difficulty))
                : questionRepo.findBySectionAndActiveTrue(sec);
        } else {
            questions = questionRepo.findAll(
                    PageRequest.of(page, 30, Sort.by("id").descending()))
                .getContent();
        }

        model.addAttribute("questions",   questions);
        model.addAttribute("sections",    Section.values());
        model.addAttribute("difficulties",Difficulty.values());
        model.addAttribute("keyword",     keyword);
        model.addAttribute("selSection",  section);
        model.addAttribute("selDiff",     difficulty);
        return "admin/questions";
    }

    // ────────────────────────────────────────────────────────────
    //  Add Question (form)
    // ────────────────────────────────────────────────────────────
    @GetMapping("/questions/add")
    public String addQuestionForm(Model model) {
        model.addAttribute("question",   new QuestionFormDto());
        model.addAttribute("sections",   Section.values());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("mode",       "add");
        return "admin/question-form";
    }

    @PostMapping("/questions/add")
    public String saveQuestion(@ModelAttribute QuestionFormDto dto,
                               @RequestParam(required = false) String active) {
        Question q = Question.builder()
            .questionText(dto.getQuestionText())
            .optionA(dto.getOptionA()).optionB(dto.getOptionB())
            .optionC(dto.getOptionC()).optionD(dto.getOptionD())
            .correctAnswer(dto.getCorrectAnswer().toUpperCase())
            .explanation(dto.getExplanation())
            .section(Section.valueOf(dto.getSection()))
            .difficulty(Difficulty.valueOf(dto.getDifficulty()))
            .topic(dto.getTopic())
            .source(dto.getSource())
            .examYear(dto.getExamYear())
            .active("on".equals(active))
            .build();
        questionRepo.save(q);
        return "redirect:/admin/questions?added=true";
    }

    // ────────────────────────────────────────────────────────────
    //  Edit Question
    // ────────────────────────────────────────────────────────────
    @GetMapping("/questions/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Question q = questionRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Question not found: " + id));

        QuestionFormDto dto = new QuestionFormDto();
        dto.setQuestionText(q.getQuestionText());
        dto.setOptionA(q.getOptionA()); dto.setOptionB(q.getOptionB());
        dto.setOptionC(q.getOptionC()); dto.setOptionD(q.getOptionD());
        dto.setCorrectAnswer(q.getCorrectAnswer());
        dto.setExplanation(q.getExplanation());
        dto.setSection(q.getSection().name());
        dto.setDifficulty(q.getDifficulty().name());
        dto.setTopic(q.getTopic()); dto.setSource(q.getSource());
        dto.setExamYear(q.getExamYear());

        model.addAttribute("question",    dto);
        model.addAttribute("questionId",  id);
        model.addAttribute("sections",    Section.values());
        model.addAttribute("difficulties",Difficulty.values());
        model.addAttribute("active",      q.getActive());
        model.addAttribute("mode",        "edit");
        return "admin/question-form";
    }

    @PostMapping("/questions/edit/{id}")
    public String updateQuestion(@PathVariable Long id,
                                 @ModelAttribute QuestionFormDto dto,
                                 @RequestParam(required = false) String active) {
        Question q = questionRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Question not found: " + id));
        q.setQuestionText(dto.getQuestionText());
        q.setOptionA(dto.getOptionA()); q.setOptionB(dto.getOptionB());
        q.setOptionC(dto.getOptionC()); q.setOptionD(dto.getOptionD());
        q.setCorrectAnswer(dto.getCorrectAnswer().toUpperCase());
        q.setExplanation(dto.getExplanation());
        q.setSection(Section.valueOf(dto.getSection()));
        q.setDifficulty(Difficulty.valueOf(dto.getDifficulty()));
        q.setTopic(dto.getTopic()); q.setSource(dto.getSource());
        q.setExamYear(dto.getExamYear());
        q.setActive("on".equals(active));
        questionRepo.save(q);
        return "redirect:/admin/questions?updated=true";
    }

    // ────────────────────────────────────────────────────────────
    //  Delete Question
    // ────────────────────────────────────────────────────────────
    @PostMapping("/questions/delete/{id}")
    public String deleteQuestion(@PathVariable Long id) {
        questionRepo.deleteById(id);
        return "redirect:/admin/questions?deleted=true";
    }

    // ────────────────────────────────────────────────────────────
    //  Bulk Upload — JSON
    // ────────────────────────────────────────────────────────────
    @GetMapping("/questions/bulk-upload")
    public String bulkUploadPage() {
        return "admin/bulk-upload";
    }

    @PostMapping("/questions/bulk-upload/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadJson(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> rows = objectMapper.readValue(
                file.getInputStream(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));

            int saved = 0, skipped = 0;
            for (Map<String, Object> row : rows) {
                try {
                    Question q = mapRowToQuestion(row);
                    questionRepo.save(q);
                    saved++;
                } catch (Exception e) {
                    log.warn("Skipping malformed question row: {}", e.getMessage());
                    skipped++;
                }
            }
            response.put("success", true);
            response.put("saved",   saved);
            response.put("skipped", skipped);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error",   e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Bulk Upload — Excel (.xlsx)
    // ────────────────────────────────────────────────────────────
    @PostMapping("/questions/bulk-upload/excel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            int saved = 0, skipped = 0;

            // Row 0 = header; data starts at row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    Map<String, Object> rowMap = new HashMap<>();
                    rowMap.put("section",       cellStr(row, 0));
                    rowMap.put("topic",         cellStr(row, 1));
                    rowMap.put("difficulty",    cellStr(row, 2));
                    rowMap.put("question",      cellStr(row, 3));
                    rowMap.put("optionA",       cellStr(row, 4));
                    rowMap.put("optionB",       cellStr(row, 5));
                    rowMap.put("optionC",       cellStr(row, 6));
                    rowMap.put("optionD",       cellStr(row, 7));
                    rowMap.put("correctAnswer", cellStr(row, 8));
                    rowMap.put("explanation",   cellStr(row, 9));
                    rowMap.put("source",        cellStr(row, 10));
                    questionRepo.save(mapRowToQuestion(rowMap));
                    saved++;
                } catch (Exception e) {
                    log.warn("Excel row {} skipped: {}", i, e.getMessage());
                    skipped++;
                }
            }
            response.put("success", true);
            response.put("saved",   saved);
            response.put("skipped", skipped);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error",   e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Users
    // ────────────────────────────────────────────────────────────
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepo.findAll(Sort.by("id").descending());
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUserActive(@PathVariable Long id) {
        userRepo.findById(id).ifPresent(u -> {
            u.setActive(!u.isActive());
            userRepo.save(u);
        });
        return "redirect:/admin/users";
    }

    // ────────────────────────────────────────────────────────────
    //  Reports
    // ────────────────────────────────────────────────────────────
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("totalQuestions", questionRepo.count());
        model.addAttribute("totalUsers",     userRepo.count());
        model.addAttribute("totalAttempts",  attemptRepo.count());
        // Top 10 scorers
        model.addAttribute("topResults",
            resultRepo.findAll(
                PageRequest.of(0, 10, Sort.by("totalScore").descending()))
            .getContent());
        return "admin/reports";
    }

    // ────────────────────────────────────────────────────────────
    //  Private helpers
    // ────────────────────────────────────────────────────────────
    private Map<String, Long> getSectionCounts() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Section s : Section.values()) {
            map.put(s.getDisplayName(), questionRepo.countBySectionAndActiveTrue(s));
        }
        return map;
    }

    private Question mapRowToQuestion(Map<String, Object> row) {
        String secStr  = str(row, "section");
        String diffStr = str(row, "difficulty");

        return Question.builder()
            .section(Section.valueOf(secStr.toUpperCase().replace(" ", "_")))
            .topic(str(row, "topic"))
            .difficulty(Difficulty.valueOf(diffStr.toUpperCase()))
            .questionText(str(row, "question"))
            .optionA(str(row, "optionA"))
            .optionB(str(row, "optionB"))
            .optionC(str(row, "optionC"))
            .optionD(str(row, "optionD"))
            .correctAnswer(str(row, "correctAnswer").toUpperCase())
            .explanation(str(row, "explanation"))
            .source(str(row, "source"))
            .active(true)
            .build();
    }

    private String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString().trim() : "";
    }

    private String cellStr(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }
}
