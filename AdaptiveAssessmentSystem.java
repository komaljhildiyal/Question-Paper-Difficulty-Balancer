import java.io.*;
import java.util.*;

public class AdaptiveAssessmentSystem {

    public static void main(String[] args) {

        String filePath = "questions.csv";

        List<Question> allQuestions =
                QuestionBankManagementModule.loadQuestions(filePath);

        if (allQuestions.isEmpty()) {
            System.out.println("Question bank is empty.");
            return;
        }

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter student name: ");
        String studentName = sc.nextLine();

        System.out.print("Enter total marks for quiz: ");
        int totalMarks = sc.nextInt();
        sc.nextLine();

        List<Question> quizQuestions =
                RealTimeQuizConductModule.selectQuizQuestions(allQuestions, totalMarks);

        StudentResponseTrackingModule tracker = new StudentResponseTrackingModule();

        RealTimeQuizConductModule.conductQuiz(
                studentName,
                quizQuestions,
                tracker,
                sc
        );

        Map<String, TopicPerformance> analysis =
                TopicWisePerformanceAnalysisModule.analyze(
                        tracker.getAllResponses()
                );

        TopicWisePerformanceAnalysisModule.displayAnalysis(analysis);

        List<String> weakTopics =
                WeakTopicIdentificationModule.getWeakTopics(analysis);

        WeakTopicIdentificationModule.displayWeakTopics(weakTopics);

        sc.close();
    }
}

/* ========================= QUESTION ========================= */
class Question {

    int id;
    String text;

    String optionA;
    String optionB;
    String optionC;
    String optionD;

    int marks;
    int score;
    int timesUsed;

    String topic;
    String bloomLevel;
    String correctAnswer;

    public Question(int id, String text,
                    String optionA, String optionB,
                    String optionC, String optionD,
                    int marks, int score, int timesUsed,
                    String topic, String bloomLevel,
                    String correctAnswer) {

        this.id = id;
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.marks = marks;
        this.score = score;
        this.timesUsed = timesUsed;
        this.topic = topic;
        this.bloomLevel = bloomLevel;
        this.correctAnswer = correctAnswer;
    }

    public String display(int serialNo) {
        return "\nQ" + serialNo + ": " + text +
                "\nA. " + optionA +
                "\nB. " + optionB +
                "\nC. " + optionC +
                "\nD. " + optionD +
                "\n(" + topic + ", " + bloomLevel + ", " + marks + " marks)";
    }
}

/* ========================= CSV LOADER ========================= */
class QuestionBankManagementModule {

    public static List<Question> loadQuestions(String filePath) {

        List<Question> questions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {

                String[] d = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                if (d.length < 12) continue;

                questions.add(new Question(
                        Integer.parseInt(d[0].trim()),
                        d[1].replace("\"", "").trim(),
                        d[2].replace("\"", "").trim(),
                        d[3].replace("\"", "").trim(),
                        d[4].replace("\"", "").trim(),
                        d[5].replace("\"", "").trim(),
                        Integer.parseInt(d[6].trim()),
                        Integer.parseInt(d[7].trim()),
                        Integer.parseInt(d[8].trim()),
                        d[9].trim(),
                        d[10].trim(),
                        d[11].trim()
                ));
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return questions;
    }
}

/* ========================= RESPONSE ========================= */
class StudentResponse {

    String studentName;
    int questionId;
    String topic;
    String bloomLevel;
    String givenAnswer;
    String correctAnswer;
    boolean isCorrect;
    long timeTakenSeconds;

    public StudentResponse(String studentName, int questionId,
                           String topic, String bloomLevel,
                           String givenAnswer, String correctAnswer,
                           boolean isCorrect, long timeTakenSeconds) {

        this.studentName = studentName;
        this.questionId = questionId;
        this.topic = topic;
        this.bloomLevel = bloomLevel;
        this.givenAnswer = givenAnswer;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
        this.timeTakenSeconds = timeTakenSeconds;
    }
}

/* ========================= TRACKER ========================= */
class StudentResponseTrackingModule {

    private List<StudentResponse> responses = new ArrayList<>();

    public void addResponse(StudentResponse r) {
        responses.add(r);
    }

    public List<StudentResponse> getAllResponses() {
        return responses;
    }
}

/* ========================= BLOOM CLASSIFIER ========================= */
class BloomDifficultyMapper {

    public static String map(String bloom) {
        if (bloom == null) return "Medium";

        bloom = bloom.toLowerCase();

        switch (bloom) {
            case "remember":
            case "understand":
                return "Easy";

            case "apply":
                return "Medium";

            case "analyze":
                return "Medium";

            case "evaluate":
            case "create":
                return "Hard";

            default:
                return "Medium";
        }
    }
}

/* ========================= QUIZ ENGINE ========================= */
class RealTimeQuizConductModule {

    public static List<Question> selectQuizQuestions(List<Question> all, int totalMarks) {

        List<Question> easy = new ArrayList<>();
        List<Question> medium = new ArrayList<>();
        List<Question> hard = new ArrayList<>();

        for (Question q : all) {
            String level = BloomDifficultyMapper.map(q.bloomLevel);

            if (level.equals("Easy")) easy.add(q);
            else if (level.equals("Hard")) hard.add(q);
            else medium.add(q);
        }

        Collections.shuffle(easy);
        Collections.shuffle(medium);
        Collections.shuffle(hard);

        List<Question> selected = new ArrayList<>();
        int sum = 0;

        List<Question> ordered = new ArrayList<>();
        ordered.addAll(medium);
        ordered.addAll(easy);
        ordered.addAll(hard);

        for (Question q : ordered) {
            if (sum + q.marks <= totalMarks) {
                selected.add(q);
                sum += q.marks;
            }
            if (sum >= totalMarks) break;
        }

        return selected;
    }

    public static void conductQuiz(String studentName,
                                   List<Question> questions,
                                   StudentResponseTrackingModule tracker,
                                   Scanner sc) {

        System.out.println("\n===== MCQ QUIZ STARTED =====");

        int serial = 1;

        for (Question q : questions) {

            System.out.println(q.display(serial));

            System.out.print("Enter Option (A/B/C/D): ");

            long start = System.currentTimeMillis();
            String ans = sc.nextLine().trim().toUpperCase();
            long end = System.currentTimeMillis();

            long time = Math.max(1, (end - start) / 1000);

            boolean correct = ans.equalsIgnoreCase(q.correctAnswer);

            tracker.addResponse(new StudentResponse(
                    studentName,
                    q.id,
                    q.topic,
                    q.bloomLevel,
                    ans,
                    q.correctAnswer,
                    correct,
                    time
            ));

            System.out.println(correct ? "✔ Correct" : "✘ Wrong");

            serial++;
        }

        System.out.println("\n===== QUIZ COMPLETED =====");
    }
}

/* ========================= ANALYTICS ========================= */
class TopicPerformance {

    String topic;
    int total, correct;
    double accuracy;

    public TopicPerformance(String topic) {
        this.topic = topic;
    }

    void update(boolean isCorrect) {
        total++;
        if (isCorrect) correct++;
        accuracy = (correct * 100.0) / total;
    }
}

class TopicWisePerformanceAnalysisModule {

    public static Map<String, TopicPerformance> analyze(List<StudentResponse> r) {

        Map<String, TopicPerformance> map = new HashMap<>();

        for (StudentResponse sr : r) {
            map.putIfAbsent(sr.topic, new TopicPerformance(sr.topic));
            map.get(sr.topic).update(sr.isCorrect);
        }

        return map;
    }

    public static void displayAnalysis(Map<String, TopicPerformance> m) {

        System.out.println("\n===== ANALYSIS =====");

        for (TopicPerformance t : m.values()) {
            System.out.println(t.topic + " -> " + t.accuracy + "%");
        }
    }
}

/* ========================= WEAK TOPICS ========================= */
class WeakTopicIdentificationModule {

    public static List<String> getWeakTopics(Map<String, TopicPerformance> m) {

        List<String> weak = new ArrayList<>();

        for (TopicPerformance t : m.values()) {
            if (t.accuracy < 40)
                weak.add(t.topic);
        }

        return weak;
    }

    public static void displayWeakTopics(List<String> w) {

        System.out.println("\n===== WEAK TOPICS =====");

        if (w.isEmpty()) {
            System.out.println("None");
        }

        for (String s : w) {
            System.out.println(s);
        }
    }
}
