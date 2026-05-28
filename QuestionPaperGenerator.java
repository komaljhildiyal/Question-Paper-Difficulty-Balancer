import java.io.*;
import java.util.*;

// =====================================================================
//  DAA Question Paper Generator — Single File Version
//  Features:
//    Teacher Mode  → Knapsack-based exact-marks paper (per topic)
//    Student Mode  → Shuffle-based paper (across all topics)
//    Analytics     → Score/usage stats per topic
//    Add Question  → Append new question to questions.csv at runtime
//    Export Paper  → Save generated paper to a .txt file
//
//  Dataset: Single combined file — questions.csv
// =====================================================================

public class QuestionPaperGenerator {

    // ── Data Model ────────────────────────────────────────────────────
    static class Question {
        int    id, marks, score, timesUsed;
        String text, topic;

        Question(int id, String text, int marks, int score, int timesUsed, String topic) {
            this.id        = id;
            this.text      = text;
            this.marks     = marks;
            this.score     = score;
            this.timesUsed = timesUsed;
            this.topic     = topic;
        }

        /** CSV row representation */
        String toCsv() {
            return id + "," + text + "," + marks + "," + score + "," + timesUsed + "," + topic;
        }
    }

    // ── Constants ─────────────────────────────────────────────────────
    static final String[] TOPICS   = {"array", "string", "bfs", "dfs", "sorting"};
    static final String   CSV_FILE = "questions.csv";
    static final String   HEADER   = "id,question,marks,score,timesUsed,topic";

    // ── CSV Helpers ───────────────────────────────────────────────────

    /** Load ALL questions from the single combined CSV. */
    static List<Question> loadAll() {
        List<Question> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] d = line.split(",", 6);
                if (d.length < 6) continue;
                list.add(new Question(
                        Integer.parseInt(d[0].trim()),
                        d[1].trim(),
                        Integer.parseInt(d[2].trim()),
                        Integer.parseInt(d[3].trim()),
                        Integer.parseInt(d[4].trim()),
                        d[5].trim()
                ));
            }
        } catch (Exception e) {
            System.out.println("  [!] Could not read " + CSV_FILE + ": " + e.getMessage());
        }
        return list;
    }

    /** Filter questions by topic. */
    static List<Question> loadByTopic(String topic) {
        List<Question> result = new ArrayList<>();
        for (Question q : loadAll())
            if (q.topic.equalsIgnoreCase(topic)) result.add(q);
        return result;
    }

    /** Append a new question to the combined CSV. */
    static void appendToCsv(Question q) throws IOException {
        File f = new File(CSV_FILE);
        boolean needsHeader = !f.exists() || f.length() == 0;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f, true))) {
            if (needsHeader) pw.println(HEADER);
            pw.println(q.toCsv());
        }
    }

    /** Compute next available global ID. */
    static int nextId() {
        List<Question> existing = loadAll();
        int max = 0;
        for (Question q : existing) if (q.id > max) max = q.id;
        return max + 1;
    }

    // ── Knapsack (Teacher Mode) ───────────────────────────────────────
    /**
     * Boolean 0/1 knapsack: find a subset of questions whose marks sum == W.
     * Returns empty list if impossible.
     */
    static List<Question> knapsack(List<Question> pool, int W) {
        int n = pool.size();
        boolean[][] dp = new boolean[n + 1][W + 1];
        dp[0][0] = true;

        for (int i = 1; i <= n; i++) {
            int m = pool.get(i - 1).marks;
            for (int w = 0; w <= W; w++) {
                dp[i][w] = dp[i - 1][w];
                if (w >= m && dp[i - 1][w - m]) dp[i][w] = true;
            }
        }

        if (!dp[n][W]) return new ArrayList<>();

        List<Question> result = new ArrayList<>();
        int w = W;
        for (int i = n; i > 0; i--) {
            int m = pool.get(i - 1).marks;
            if (w >= m && dp[i - 1][w - m]) {
                result.add(pool.get(i - 1));
                w -= m;
            }
        }
        Collections.shuffle(result);
        return result;
    }

    // ── Paper Printer ─────────────────────────────────────────────────
    static void printPaper(List<Question> paper, String title, PrintStream out) {
        out.println();
        out.println("╔══════════════════════════════════════════════════╗");
        out.printf ("║  %-48s║%n", " " + title);
        out.println("╚══════════════════════════════════════════════════╝");
        out.println();

        int totalMarks = 0, totalTime = 0, qno = 1;
        for (Question q : paper) {
            out.printf("  Q%-3d %s%n", qno++, q.text);
            out.printf("       [%d marks | Topic: %s]%n%n", q.marks, q.topic);
            totalMarks += q.marks;
            totalTime  += q.marks;
        }

        out.println("──────────────────────────────────────────────────");
        out.printf ("  Total Questions : %d%n", paper.size());
        out.printf ("  Total Marks     : %d%n", totalMarks);
        out.printf ("  Suggested Time  : %d minutes%n", totalTime);
        out.println("──────────────────────────────────────────────────");
    }

    // ── Export to File ────────────────────────────────────────────────
    static void exportPaper(List<Question> paper, String title) {
        String filename = title.replaceAll("\\s+", "_") + ".txt";
        try (PrintStream ps = new PrintStream(new FileOutputStream(filename))) {
            printPaper(paper, title, ps);
            System.out.println("\n  [✓] Paper exported to: " + filename);
        } catch (Exception e) {
            System.out.println("  [!] Export failed: " + e.getMessage());
        }
    }

    // ── Analytics ─────────────────────────────────────────────────────
    static void showAnalytics(Scanner sc) {
        System.out.println("\n── Analytics ────────────────────────────────────");
        System.out.println("  Select topic (0 = all topics):");
        for (int i = 0; i < TOPICS.length; i++)
            System.out.printf("  %d. %s%n", i + 1, TOPICS[i]);
        System.out.print("  Choice: ");
        int ch = sc.nextInt();

        List<Question> pool;
        if (ch == 0) {
            pool = loadAll();
        } else if (ch >= 1 && ch <= TOPICS.length) {
            pool = loadByTopic(TOPICS[ch - 1]);
        } else {
            System.out.println("  Invalid choice.");
            return;
        }

        if (pool.isEmpty()) { System.out.println("  No data found."); return; }

        Map<Integer, List<Question>> byMark = new TreeMap<>();
        for (Question q : pool) byMark.computeIfAbsent(q.marks, k -> new ArrayList<>()).add(q);

        System.out.println();
        System.out.printf("  %-8s %-6s %-10s %-10s%n", "Marks", "Count", "Avg Score", "Avg Uses");
        System.out.println("  ─────────────────────────────────────────");
        for (Map.Entry<Integer, List<Question>> e : byMark.entrySet()) {
            List<Question> qs = e.getValue();
            double avgScore = qs.stream().mapToInt(q -> q.score).average().orElse(0);
            double avgUses  = qs.stream().mapToInt(q -> q.timesUsed).average().orElse(0);
            System.out.printf("  %-8d %-6d %-10.1f %-10.1f%n",
                    e.getKey(), qs.size(), avgScore, avgUses);
        }

        System.out.println("\n  Top 3 Most-Used Questions:");
        pool.stream()
            .sorted((a, b) -> b.timesUsed - a.timesUsed)
            .limit(3)
            .forEach(q -> System.out.printf("    • [Used %dx] %s%n", q.timesUsed, q.text));

        System.out.println("\n  Total questions in pool: " + pool.size());
    }

    // ── Add Question ──────────────────────────────────────────────────
    static void addQuestion(Scanner sc) {
        sc.nextLine(); // consume leftover newline
        System.out.println("\n── Add New Question ─────────────────────────────");
        System.out.println("  Topics: array | string | bfs | dfs | sorting");
        System.out.print("  Topic: ");
        String topic = sc.nextLine().trim().toLowerCase();

        boolean validTopic = false;
        for (String t : TOPICS) if (t.equals(topic)) { validTopic = true; break; }
        if (!validTopic) { System.out.println("  Invalid topic."); return; }

        System.out.print("  Question text: ");
        String text = sc.nextLine().trim();
        if (text.isEmpty()) { System.out.println("  Empty text."); return; }

        System.out.print("  Marks (1-5): ");
        int marks;
        try { marks = Integer.parseInt(sc.nextLine().trim()); }
        catch (Exception e) { System.out.println("  Invalid marks."); return; }
        if (marks < 1 || marks > 5) { System.out.println("  Marks out of range."); return; }

        int id = nextId();
        Question q = new Question(id, text, marks, 5, 0, topic);

        try {
            appendToCsv(q);
            System.out.printf("  [✓] Question #%d added to %s%n", id, CSV_FILE);
        } catch (IOException e) {
            System.out.println("  [!] Failed to save: " + e.getMessage());
        }
    }

    // ── Teacher Mode ──────────────────────────────────────────────────
    static void teacherMode(Scanner sc) {
        System.out.println("\n── Teacher Mode (Knapsack) ───────────────────────");
        System.out.println("  Select Topic:");
        for (int i = 0; i < TOPICS.length; i++)
            System.out.printf("  %d. %s%n", i + 1, TOPICS[i]);
        System.out.print("  Choice: ");
        int t = sc.nextInt();

        if (t < 1 || t > TOPICS.length) { System.out.println("  Invalid topic."); return; }
        String topic = TOPICS[t - 1];
        List<Question> pool = loadByTopic(topic);

        if (pool.isEmpty()) {
            System.out.println("  No questions found for topic: " + topic);
            return;
        }

        System.out.print("  Enter total marks: ");
        int W = sc.nextInt();

        Collections.shuffle(pool);
        List<Question> paper = knapsack(pool, W);

        if (paper.isEmpty()) {
            System.out.println("  [!] Cannot form exact " + W + " marks from " + topic + " questions.");
            System.out.println("      Try a different mark total.");
            return;
        }

        printPaper(paper, "Teacher Paper — " + topic.toUpperCase(), System.out);

        System.out.print("\n  Export this paper to a file? (y/n): ");
        sc.nextLine();
        if (sc.nextLine().trim().equalsIgnoreCase("y"))
            exportPaper(paper, "Teacher_Paper_" + topic);
    }

    // ── Student Mode ──────────────────────────────────────────────────
    static void studentMode(Scanner sc) {
        System.out.println("\n── Student Mode (Shuffle) ────────────────────────");
        List<Question> all = loadAll();

        if (all.isEmpty()) {
            System.out.println("  No questions found. Ensure questions.csv is in the same directory.");
            return;
        }

        System.out.print("  2-mark questions: "); int n2 = sc.nextInt();
        System.out.print("  3-mark questions: "); int n3 = sc.nextInt();
        System.out.print("  4-mark questions: "); int n4 = sc.nextInt();
        System.out.print("  5-mark questions: "); int n5 = sc.nextInt();

        List<Question> q2 = new ArrayList<>(), q3 = new ArrayList<>(),
                       q4 = new ArrayList<>(), q5 = new ArrayList<>();
        for (Question q : all) {
            if      (q.marks == 2) q2.add(q);
            else if (q.marks == 3) q3.add(q);
            else if (q.marks == 4) q4.add(q);
            else if (q.marks == 5) q5.add(q);
        }

        Collections.shuffle(q2); Collections.shuffle(q3);
        Collections.shuffle(q4); Collections.shuffle(q5);

        List<Question> paper = new ArrayList<>();
        for (int i = 0; i < Math.min(n2, q2.size()); i++) paper.add(q2.get(i));
        for (int i = 0; i < Math.min(n3, q3.size()); i++) paper.add(q3.get(i));
        for (int i = 0; i < Math.min(n4, q4.size()); i++) paper.add(q4.get(i));
        for (int i = 0; i < Math.min(n5, q5.size()); i++) paper.add(q5.get(i));
        Collections.shuffle(paper);

        printPaper(paper, "Sample Paper — All Topics", System.out);

        System.out.print("\n  Export this paper to a file? (y/n): ");
        sc.nextLine();
        if (sc.nextLine().trim().equalsIgnoreCase("y"))
            exportPaper(paper, "Student_Sample_Paper");
    }

    // ── Main Menu ─────────────────────────────────────────────────────
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║      DAA Question Paper Generator  v2.1          ║");
        System.out.println("║      Dataset: questions.csv (unified)            ║");
        System.out.println("╚══════════════════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("  1. Teacher Mode  (topic-specific, exact marks via Knapsack)");
            System.out.println("  2. Student Mode  (multi-topic shuffle paper)");
            System.out.println("  3. Analytics     (pool statistics & top questions)");
            System.out.println("  4. Add Question  (append to questions.csv)");
            System.out.println("  5. Exit");
            System.out.print("  Choice: ");

            int choice;
            try { choice = sc.nextInt(); }
            catch (InputMismatchException e) { sc.next(); System.out.println("  Enter a number."); continue; }

            switch (choice) {
                case 1 -> teacherMode(sc);
                case 2 -> studentMode(sc);
                case 3 -> showAnalytics(sc);
                case 4 -> addQuestion(sc);
                case 5 -> { running = false; System.out.println("\n  Goodbye!\n"); }
                default -> System.out.println("  Invalid choice.");
            }
        }
        sc.close();
    }
}
