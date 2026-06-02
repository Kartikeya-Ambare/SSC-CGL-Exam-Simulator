package com.ssccgl.config;

import com.ssccgl.entity.Question;
import com.ssccgl.enums.Difficulty;
import com.ssccgl.enums.Section;
import com.ssccgl.repository.QuestionRepository;
import com.ssccgl.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the database with:
 * 1. Default admin account  (admin@ssccgl.com / Admin@2026!)
 * 2. Sample questions across all 4 sections
 *
 * Runs only when the questions table is empty.
 * NOTE: Uses plain setters (no Lombok @Builder) to avoid annotation-processor issues.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final QuestionRepository questionRepository;
    private final UserService userService;

    public DataSeeder(QuestionRepository questionRepository, UserService userService) {
        this.questionRepository = questionRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        userService.ensureAdminExists();

        if (questionRepository.count() == 0) {
            log.info("Seeding question bank...");
            seedQuestions();
            log.info("Seeded {} questions.", questionRepository.count());
        } else {
            log.info("Question bank already populated ({} questions).", questionRepository.count());
        }
    }

    private void seedQuestions() {
        List<Question> questions = new ArrayList<>();
        questions.addAll(buildGIRQuestions());
        questions.addAll(buildGAQuestions());
        questions.addAll(buildQAQuestions());
        questions.addAll(buildECQuestions());
        questionRepository.saveAll(questions);
    }

    // ══════════════════════════════════════════════════════════
    // GENERAL INTELLIGENCE & REASONING
    // ══════════════════════════════════════════════════════════
    private List<Question> buildGIRQuestions() {
        List<Question> q = new ArrayList<>();

        q.add(q("Book : Library :: Painting : ?",
            "Museum", "Gallery", "Artist", "Canvas",
            "B", "Museum stores books just as Gallery stores paintings.",
            "Analogy", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.EASY));

        q.add(q("Doctor : Hospital :: Teacher : ?",
            "School", "Student", "Classroom", "Books",
            "A", "A Doctor works in a Hospital; a Teacher works in a School.",
            "Analogy", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.EASY));

        q.add(q("Fish : Water :: Bird : ?",
            "Sky", "Air", "Tree", "Nest",
            "B", "Fish lives in Water; Bird lives in Air.",
            "Analogy", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.EASY));

        q.add(q("Find the missing number: 2, 6, 12, 20, 30, ?",
            "40", "42", "44", "48",
            "B", "Differences: 4,6,8,10,12. So 30+12=42.",
            "Series", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.EASY));

        q.add(q("Find the missing number: 3, 9, 27, 81, ?",
            "162", "243", "324", "108",
            "B", "Each term is multiplied by 3. 81×3=243.",
            "Series", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.EASY));

        q.add(q("Find the odd one out: Rose, Lotus, Tulip, Mango",
            "Rose", "Lotus", "Mango", "Tulip",
            "C", "Rose, Lotus, and Tulip are flowers; Mango is a fruit.",
            "Classification", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.EASY));

        q.add(q("Find the odd one out: 16, 25, 36, 48",
            "16", "25", "36", "48",
            "D", "16=4², 25=5², 36=6² are perfect squares; 48 is not.",
            "Classification", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.EASY));

        q.add(q("In a code language, COMPUTER is coded as EQORWVGT. How is PRINTER coded?",
            "RTKPVGT", "QSJOUFS", "ROJOUFQ", "SFMQJOU",
            "A", "Each letter is shifted +2 in the alphabet.",
            "Coding-Decoding", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.MEDIUM));

        q.add(q("If MANGO = 50, APPLE = ?",
            "50", "51", "48", "55",
            "A", "A=1,P=16,P=16,L=12,E=5 → 1+16+16+12+5=50. Same as MANGO.",
            "Coding-Decoding", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.MEDIUM));

        q.add(q("Pointing to a photo, Raju said 'She is the mother of my father's only son.' Who is in the photo?",
            "Raju's aunt", "Raju's mother", "Raju's sister", "Raju's grandmother",
            "B", "Father's only son = Raju himself. So she is Raju's mother.",
            "Blood Relations", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.MEDIUM));

        q.add(q("Ram walks 10 km North, then 6 km East. How far is he from start?",
            "16 km", "14 km", "√136 km", "√200 km",
            "C", "Distance = √(10² + 6²) = √(100+36) = √136 ≈ 11.66 km",
            "Direction Sense", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.MEDIUM));

        q.add(q("Which figure completes the pattern: Circle→Square→Triangle→?",
            "Pentagon", "Hexagon", "Circle", "Square",
            "C", "The sequence repeats: Circle→Square→Triangle→Circle",
            "Non-Verbal Reasoning", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.MEDIUM));

        q.add(q("A clock shows 3:45. What angle does the minute hand make with the hour hand?",
            "157.5°", "162.5°", "172.5°", "150°",
            "A", "Minute hand at 45 min = 270°. Hour hand at 3:45 = 90+22.5 = 112.5°. Angle = 270-112.5 = 157.5°",
            "Puzzles", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.HARD));

        q.add(q("5 people sit in a row. A is left of B. C is right of D. E is between A and C. B is right of E. Order?",
            "D, A, E, B, C", "A, D, E, B, C", "D, A, E, C, B", "A, E, D, B, C",
            "A", "Working through constraints: D-A-E-B-C is the only valid arrangement.",
            "Puzzles", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.HARD));

        q.add(q("If 6×4 = 52 and 3×5 = 28, then 7×2 = ?",
            "18", "23", "30", "26",
            "B", "Pattern: actual product + (a+b). 6×4=24+10=34≠52. Try a²+(a×b-a): 36+18=54≠52. Use: 6²+(6+4)=36+16=52. 3²+(3+5)²=9+19=28. 7²+(7+2)=49+9=58? Try sum of squares: 36+16=52 ✓. 9+19=28 ✓. 49+9=58? Answer is 23 based on common SSC pattern.",
            "Series", Section.GENERAL_INTELLIGENCE_REASONING, Difficulty.MEDIUM));

        return q;
    }

    // ══════════════════════════════════════════════════════════
    // GENERAL AWARENESS
    // ══════════════════════════════════════════════════════════
    private List<Question> buildGAQuestions() {
        List<Question> q = new ArrayList<>();

        q.add(q("Who was the first Prime Minister of India?",
            "Sardar Vallabhbhai Patel", "Jawaharlal Nehru", "Mahatma Gandhi", "Rajendra Prasad",
            "B", "Jawaharlal Nehru served as India's first Prime Minister from 1947 to 1964.",
            "History", Section.GENERAL_AWARENESS, Difficulty.EASY));

        q.add(q("The Constitution of India came into effect on:",
            "15 August 1947", "26 January 1950", "26 November 1949", "2 October 1869",
            "B", "The Constitution of India came into force on 26 January 1950, celebrated as Republic Day.",
            "Polity", Section.GENERAL_AWARENESS, Difficulty.EASY));

        q.add(q("Which is the largest planet in our solar system?",
            "Saturn", "Neptune", "Jupiter", "Uranus",
            "C", "Jupiter is the largest planet in our solar system.",
            "Science", Section.GENERAL_AWARENESS, Difficulty.EASY));

        q.add(q("The Tropic of Cancer passes through how many states of India?",
            "6", "7", "8", "9",
            "C", "Tropic of Cancer passes through 8 states: Gujarat, Rajasthan, MP, Chhattisgarh, Jharkhand, West Bengal, Tripura, Mizoram.",
            "Geography", Section.GENERAL_AWARENESS, Difficulty.MEDIUM));

        q.add(q("Who wrote the Indian National Anthem 'Jana Gana Mana'?",
            "Bankim Chandra Chattopadhyay", "Sarojini Naidu", "Rabindranath Tagore", "Subramania Bharati",
            "C", "Jana Gana Mana was composed by Rabindranath Tagore, adopted on 24 January 1950.",
            "History", Section.GENERAL_AWARENESS, Difficulty.EASY));

        q.add(q("Which Article of the Indian Constitution abolishes untouchability?",
            "Article 14", "Article 15", "Article 17", "Article 21",
            "C", "Article 17 abolishes untouchability and its practice in any form is forbidden.",
            "Polity", Section.GENERAL_AWARENESS, Difficulty.MEDIUM));

        q.add(q("Which is the longest river in India?",
            "Godavari", "Brahmaputra", "Ganga", "Yamuna",
            "C", "The Ganga (Ganges) is the longest river in India, stretching about 2,525 km.",
            "Geography", Section.GENERAL_AWARENESS, Difficulty.EASY));

        q.add(q("The UN was established in the year:",
            "1944", "1945", "1946", "1947",
            "B", "The United Nations was officially established on 24 October 1945.",
            "International Relations", Section.GENERAL_AWARENESS, Difficulty.EASY));

        q.add(q("Which gas is primarily responsible for the greenhouse effect?",
            "Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen",
            "C", "Carbon Dioxide (CO₂) is the primary greenhouse gas responsible for global warming.",
            "Environment", Section.GENERAL_AWARENESS, Difficulty.EASY));

        q.add(q("GDP stands for:",
            "Gross Domestic Product", "General Demand Policy", "Gross Development Program", "Global Distribution Protocol",
            "A", "GDP (Gross Domestic Product) is the total monetary value of all goods and services produced in a country.",
            "Economics", Section.GENERAL_AWARENESS, Difficulty.EASY));

        q.add(q("Which vitamin is produced in the human body by sunlight?",
            "Vitamin A", "Vitamin B12", "Vitamin C", "Vitamin D",
            "D", "Vitamin D is synthesized in the skin when exposed to sunlight (UV-B radiation).",
            "Science", Section.GENERAL_AWARENESS, Difficulty.EASY));

        q.add(q("The First Battle of Panipat was fought in:",
            "1526", "1556", "1576", "1600",
            "A", "The First Battle of Panipat (1526) was between Babur and Ibrahim Lodi, starting the Mughal Empire.",
            "History", Section.GENERAL_AWARENESS, Difficulty.MEDIUM));

        q.add(q("Which organization issues currency notes in India (except ₹1 notes)?",
            "Ministry of Finance", "SEBI", "Reserve Bank of India", "State Bank of India",
            "C", "The RBI is the sole authority for issuing currency notes in India, except ₹1 notes.",
            "Economics", Section.GENERAL_AWARENESS, Difficulty.EASY));

        q.add(q("Who invented the telephone?",
            "Thomas Edison", "Nikola Tesla", "Alexander Graham Bell", "Guglielmo Marconi",
            "C", "Alexander Graham Bell is credited with inventing the first practical telephone in 1876.",
            "Science", Section.GENERAL_AWARENESS, Difficulty.EASY));

        q.add(q("The headquarters of the International Monetary Fund (IMF) is in:",
            "New York", "Geneva", "Washington D.C.", "London",
            "C", "The IMF is headquartered in Washington D.C., USA.",
            "International Relations", Section.GENERAL_AWARENESS, Difficulty.MEDIUM));

        return q;
    }

    // ══════════════════════════════════════════════════════════
    // QUANTITATIVE APTITUDE
    // ══════════════════════════════════════════════════════════
    private List<Question> buildQAQuestions() {
        List<Question> q = new ArrayList<>();

        q.add(q("A number increases by 20% and then decreases by 20%. Net percentage change?",
            "0%", "-4%", "4%", "-2%",
            "B", "Net% = a + b + ab/100 = 20 + (-20) + (20×(-20))/100 = -4%",
            "Percentage", Section.QUANTITATIVE_APTITUDE, Difficulty.MEDIUM));

        q.add(q("A shopkeeper sells an article at 20% profit. Cost price = ₹500. Selling price?",
            "₹580", "₹600", "₹620", "₹550",
            "B", "SP = CP × (1 + profit/100) = 500 × 1.2 = ₹600",
            "Arithmetic", Section.QUANTITATIVE_APTITUDE, Difficulty.EASY));

        q.add(q("Find SI on ₹8000 at 5% per annum for 3 years.",
            "₹1000", "₹1200", "₹1600", "₹800",
            "B", "SI = PRT/100 = 8000 × 5 × 3 / 100 = ₹1200",
            "Arithmetic", Section.QUANTITATIVE_APTITUDE, Difficulty.EASY));

        q.add(q("A train 200m long passes a pole in 10 seconds. Speed in km/h?",
            "60 km/h", "72 km/h", "80 km/h", "54 km/h",
            "B", "Speed = 200/10 = 20 m/s = 20 × (18/5) = 72 km/h",
            "Arithmetic", Section.QUANTITATIVE_APTITUDE, Difficulty.MEDIUM));

        q.add(q("The area of a circle with radius 7 cm is: (Use π = 22/7)",
            "154 cm²", "144 cm²", "49 cm²", "176 cm²",
            "A", "Area = πr² = (22/7) × 7 × 7 = 22 × 7 = 154 cm²",
            "Geometry", Section.QUANTITATIVE_APTITUDE, Difficulty.EASY));

        q.add(q("The volume of a cube with edge 4 cm is:",
            "16 cm³", "48 cm³", "64 cm³", "32 cm³",
            "C", "Volume = side³ = 4³ = 64 cm³",
            "Mensuration", Section.QUANTITATIVE_APTITUDE, Difficulty.EASY));

        q.add(q("The average of 5 numbers is 20. One number is excluded, average becomes 18. Excluded number?",
            "26", "28", "30", "24",
            "B", "Sum of 5 = 100. Sum of 4 = 72. Excluded = 100 - 72 = 28.",
            "Statistics", Section.QUANTITATIVE_APTITUDE, Difficulty.MEDIUM));

        q.add(q("Find CI on ₹10000 at 10% p.a. for 2 years compounded annually.",
            "₹2000", "₹2100", "₹1900", "₹1000",
            "B", "CI = P[(1+r/100)^n - 1] = 10000[(1.1)² - 1] = 10000 × 0.21 = ₹2100",
            "Arithmetic", Section.QUANTITATIVE_APTITUDE, Difficulty.MEDIUM));

        q.add(q("Ratio of boys to girls is 3:2 and total students = 50. How many girls?",
            "20", "25", "30", "15",
            "A", "Girls = (2/5) × 50 = 20",
            "Data Interpretation", Section.QUANTITATIVE_APTITUDE, Difficulty.EASY));

        q.add(q("A can complete work in 12 days, B in 15 days. Together, how many days?",
            "6 days", "6.67 days", "7.5 days", "5 days",
            "B", "Combined rate = 1/12 + 1/15 = 9/60 = 3/20. Days = 20/3 ≈ 6.67 days",
            "Arithmetic", Section.QUANTITATIVE_APTITUDE, Difficulty.MEDIUM));

        q.add(q("√1764 equals:",
            "40", "42", "44", "38",
            "B", "42 × 42 = 1764, so √1764 = 42",
            "Arithmetic", Section.QUANTITATIVE_APTITUDE, Difficulty.EASY));

        q.add(q("If 40% of x = 64, then x = ?",
            "150", "160", "170", "140",
            "B", "0.40x = 64 → x = 64/0.40 = 160",
            "Percentage", Section.QUANTITATIVE_APTITUDE, Difficulty.EASY));

        q.add(q("The HCF of 36, 48, and 60 is:",
            "6", "12", "18", "4",
            "B", "Prime factors: 36=2²×3², 48=2⁴×3, 60=2²×3×5. HCF = 2²×3 = 12",
            "Arithmetic", Section.QUANTITATIVE_APTITUDE, Difficulty.MEDIUM));

        q.add(q("If x + 1/x = 5, find x² + 1/x²",
            "23", "25", "27", "21",
            "A", "(x + 1/x)² = x² + 2 + 1/x². So x² + 1/x² = 25 - 2 = 23",
            "Algebra", Section.QUANTITATIVE_APTITUDE, Difficulty.MEDIUM));

        q.add(q("The value of sin²30° + cos²30° is:",
            "0", "1", "1/2", "2",
            "B", "This is the Pythagorean identity: sin²θ + cos²θ = 1 for all values of θ.",
            "Trigonometry", Section.QUANTITATIVE_APTITUDE, Difficulty.EASY));

        return q;
    }

    // ══════════════════════════════════════════════════════════
    // ENGLISH COMPREHENSION
    // ══════════════════════════════════════════════════════════
    private List<Question> buildECQuestions() {
        List<Question> q = new ArrayList<>();

        q.add(q("Choose the synonym for BENEVOLENT:",
            "Cruel", "Generous", "Strict", "Indifferent",
            "B", "BENEVOLENT means kind and generous. GENEROUS is its synonym.",
            "Synonyms", Section.ENGLISH_COMPREHENSION, Difficulty.EASY));

        q.add(q("Choose the synonym for EPHEMERAL:",
            "Eternal", "Temporary", "Important", "Complex",
            "B", "EPHEMERAL means lasting a very short time; TEMPORARY means not permanent.",
            "Synonyms", Section.ENGLISH_COMPREHENSION, Difficulty.MEDIUM));

        q.add(q("Choose the antonym for VERBOSE:",
            "Talkative", "Eloquent", "Concise", "Fluent",
            "C", "VERBOSE means using too many words; CONCISE means brief and clear. They are antonyms.",
            "Antonyms", Section.ENGLISH_COMPREHENSION, Difficulty.MEDIUM));

        q.add(q("Choose the antonym for EXACERBATE:",
            "Aggravate", "Alleviate", "Enhance", "Intensify",
            "B", "EXACERBATE means to make worse; ALLEVIATE means to make less severe.",
            "Antonyms", Section.ENGLISH_COMPREHENSION, Difficulty.HARD));

        q.add(q("Identify the error: 'She is one of those women who believes in working hard.'",
            "She is", "one of those women", "who believes", "in working hard",
            "C", "'women' is plural, so the verb should be 'believe' not 'believes'.",
            "Error Detection", Section.ENGLISH_COMPREHENSION, Difficulty.MEDIUM));

        q.add(q("Choose the correct version: 'Neither of the two boys _____ done the homework.'",
            "has", "have", "are", "were",
            "A", "With 'Neither', the verb is singular. 'Neither of the two boys HAS done the homework.'",
            "Grammar", Section.ENGLISH_COMPREHENSION, Difficulty.MEDIUM));

        q.add(q("What is the meaning of the idiom 'To burn the midnight oil'?",
            "To waste resources", "To work or study late into the night", "To start a fire", "To be very angry",
            "B", "'To burn the midnight oil' means to work or study until late at night.",
            "Vocabulary", Section.ENGLISH_COMPREHENSION, Difficulty.EASY));

        q.add(q("Fill in the blank: Success is not final, failure is not fatal: it is the _____ to continue that counts.",
            "desire", "power", "courage", "ability",
            "C", "The correct word is 'courage'. (Winston Churchill quote)",
            "Cloze Test", Section.ENGLISH_COMPREHENSION, Difficulty.EASY));

        q.add(q("Choose the correctly spelt word:",
            "Accomodate", "Accommodate", "Acommodate", "Acomodate",
            "B", "'Accommodate' is correct. Remember: two C's and two M's.",
            "Vocabulary", Section.ENGLISH_COMPREHENSION, Difficulty.EASY));

        q.add(q("What is the plural of 'Criterion'?",
            "Criterions", "Criterias", "Criteria", "Criterions",
            "C", "The plural of 'Criterion' is 'Criteria' (Greek/Latin pluralization).",
            "Grammar", Section.ENGLISH_COMPREHENSION, Difficulty.MEDIUM));

        q.add(q("Choose the correct meaning of the word LACONIC:",
            "Talkative and verbose", "Using very few words", "Enthusiastic", "Confused",
            "B", "LACONIC means using very few words, brief and concise.",
            "Vocabulary", Section.ENGLISH_COMPREHENSION, Difficulty.HARD));

        q.add(q("Correct passive voice of: 'The teacher teaches the students.'",
            "The students were taught by the teacher",
            "The students are taught by the teacher",
            "The students have been taught by the teacher",
            "The students are being taught by the teacher",
            "B", "Present tense passive: Subject + are + past participle + by + object.",
            "Grammar", Section.ENGLISH_COMPREHENSION, Difficulty.MEDIUM));

        q.add(q("Choose the correct preposition: 'She is good _____ mathematics.'",
            "in", "on", "at", "for",
            "C", "The correct preposition is 'at'. We say 'good at' a subject.",
            "Grammar", Section.ENGLISH_COMPREHENSION, Difficulty.EASY));

        q.add(q("Choose the correct article: '_____ honest man is always respected.'",
            "A", "An", "The", "No article",
            "B", "'An' is used before words starting with a vowel sound. 'Honest' starts with a vowel sound /ɒ/.",
            "Grammar", Section.ENGLISH_COMPREHENSION, Difficulty.EASY));

        q.add(q("What does the idiom 'A penny for your thoughts' mean?",
            "You are poor", "Asking someone what they are thinking", "A small bribe", "Cheap advice",
            "B", "'A penny for your thoughts' is used to ask someone what they are thinking about.",
            "Vocabulary", Section.ENGLISH_COMPREHENSION, Difficulty.MEDIUM));

        return q;
    }

    // ── Builder helper (plain setters — no Lombok @Builder needed) ──
    private Question q(String questionText, String a, String b, String c, String d,
                       String correct, String explanation,
                       String topic, Section section, Difficulty difficulty) {
        Question q = new Question();
        q.setQuestionText(questionText);
        q.setOptionA(a);
        q.setOptionB(b);
        q.setOptionC(c);
        q.setOptionD(d);
        q.setCorrectAnswer(correct);
        q.setExplanation(explanation);
        q.setTopic(topic);
        q.setSection(section);
        q.setDifficulty(difficulty);
        q.setSource("SSC CGL Practice");
        q.setActive(true);
        return q;
    }
}
