import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class UnifiedAssessmentSuite extends JFrame {

    // File paths explicitly pointing to their respective datasets
    private static final String FILE_QPDB = "questions QPDB.csv";
    private static final String FILE_GENERATOR = "questions QuestionPaperGenerator.csv";
    private static final String FILE_ADAPTIVE = "questions AdaptiveAssessmentSystem.csv";

    // Visual Theme Colors (Gen Z Dark Mode Aesthetic)
    private static final Color BG_DARK = new Color(18, 18, 24);
    private static final Color BG_CARD = new Color(30, 30, 40);
    private static final Color ACCENT_CYAN = new Color(0, 245, 255);
    private static final Color ACCENT_PURPLE = new Color(188, 19, 254);
    private static final Color TEXT_WHITE = new Color(240, 240, 245);
    private static final Color TEXT_MUTED = new Color(150, 150, 165);

    public UnifiedAssessmentSuite() {
        setTitle("★ NEXUS ASSESSMENT SUITE ★");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        // Core Layout Setup
        JPanel headerPanel = createHeader();
        JTabbedPane tabbedPane = createStyledTabbedPane();

        // Injecting the 3 distinct modules as operational tabs
        tabbedPane.addTab("Exam Paper Builder", createModule1Panel());
        tabbedPane.addTab("DAA Paper Gen (Knapsack)", createModule2Panel());
        tabbedPane.addTab("Adaptive Quiz System", createModule3Panel());

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel title = new JLabel("NEXUS // ASSESSMENT ENGINE");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT_WHITE);

        JLabel subtitle = new JLabel("Three Core Engines. One Unified Interface Engine.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        panel.add(title, BorderLayout.NORTH);
        panel.add(subtitle, BorderLayout.SOUTH);
        return panel;
    }

    private JTabbedPane createStyledTabbedPane() {
        JTabbedPane pane = new JTabbedPane();
        pane.setBackground(BG_CARD);
        pane.setForeground(TEXT_MUTED);
        pane.setFont(new Font("SansSerif", Font.BOLD, 13));
        return pane;
    }

    // Helper UI Builder methods for clean aesthetics
    private JButton createNeonButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(ACCENT_PURPLE);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextArea createDisplayConsole() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(new Color(10, 10, 15));
        area.setForeground(ACCENT_CYAN);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setMargin(new Insets(15, 15, 15, 15));
        return area;
    }

    // =========================================================================
    // MODULE 1: EXAM PAPER BUILDER (QPDB Engine)
    // =========================================================================
    private JPanel createModule1Panel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(new EmptyBorder(20, 25, 25, 25));

        // Control Panel
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        controls.setBackground(BG_DARK);

        JLabel lblMarks = new JLabel("Total Marks:");
        lblMarks.setForeground(TEXT_WHITE);
        JTextField txtMarks = new JTextField("80", 5);

        JLabel lblDiff = new JLabel("Target Difficulty:");
        lblDiff.setForeground(TEXT_WHITE);
        JComboBox<String> cmbDiff = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});

        JButton btnGenerate = createNeonButton("Compile Blueprint");
        controls.add(lblMarks); controls.add(txtMarks);
        controls.add(lblDiff); controls.add(cmbDiff);
        controls.add(btnGenerate);

        JTextArea console = createDisplayConsole();
        mainPanel.add(controls, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(console), BorderLayout.CENTER);

        btnGenerate.addActionListener(e -> {
            try {
                int marks = Integer.parseInt(txtMarks.getText());
                String diff = (String) cmbDiff.getSelectedItem();
                String output = runQPDBEngine(marks, diff);
                console.setText(output);
            } catch (Exception ex) {
                console.setText("Error updating payload: " + ex.getMessage());
            }
        });

        return mainPanel;
    }

    private String runQPDBEngine(int totalMarks, String diff) {
        StringBuilder sb = new StringBuilder();
        List<M1Question> pool = new ArrayList<>();

        // Explicitly reading its dedicated source file
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_QPDB))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length >= 4) {
                    pool.add(new M1Question(
                        parts[1].replace("\"", ""),
                        Integer.parseInt(parts[2]),
                        parts[3].trim()
                    ));
                }
            }
        } catch (Exception e) {
            return "Failed to parse " + FILE_QPDB + ": " + e.getMessage();
        }

        Collections.shuffle(pool);
        int easyP = 30, medP = 40, hardP = 30;
        if (diff.equalsIgnoreCase("Easy")) { easyP = 60; medP = 30; hardP = 10; }
        else if (diff.equalsIgnoreCase("Hard")) { easyP = 10; medP = 30; hardP = 60; }

        int easyTarget = (totalMarks * easyP) / 100;
        int medTarget = (totalMarks * medP) / 100;
        int hardTarget = (totalMarks * hardP) / 100;

        sb.append("⚡ GENERATED BLUEPRINT VIA ENGINE-M1 (").append(diff).append(" Mode) ⚡\n");
        sb.append("================================================================\n\n");

        int currentQ = 1;
        currentQ = appendSection(sb, pool, "Easy", easyTarget, currentQ);
        currentQ = appendSection(sb, pool, "Medium", medTarget, currentQ);
        appendSection(sb, pool, "Hard", hardTarget, currentQ);

        return sb.toString();
    }

    private int appendSection(StringBuilder sb, List<M1Question> pool, String diff, int target, int startNum) {
        sb.append("■ SECTION: ").append(diff.toUpperCase()).append(" (Target: ").append(target).append(" Marks)\n");
        sb.append("────────────────────────────────────────────────────────────────\n");
        int allocated = 0;
        for (M1Question q : pool) {
            if (q.difficulty.equalsIgnoreCase(diff) && (allocated + q.marks <= target)) {
                sb.append("Q").append(startNum++).append(". ").append(q.text)
                  .append(" [").append(q.marks).append(" M]\n\n");
                allocated += q.marks;
            }
        }
        sb.append("-> Allocated Section Weight: ").append(allocated).append(" M\n\n\n");
        return startNum;
    }

    // =========================================================================
    // MODULE 2: DAA QUESTION PAPER GENERATOR (Knapsack Algorithm Focus)
    // =========================================================================
    private JPanel createModule2Panel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(new EmptyBorder(20, 25, 25, 25));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        controls.setBackground(BG_DARK);

        JLabel lblTopic = new JLabel("Topic Domain:");
        lblTopic.setForeground(TEXT_WHITE);
        JComboBox<String> cmbTopic = new JComboBox<>(new String[]{"array", "string", "bfs", "dfs", "sorting"});

        JLabel lblTarget = new JLabel("Exact Target Marks:");
        lblTarget.setForeground(TEXT_WHITE);
        JTextField txtTarget = new JTextField("15", 5);

        JButton btnKnapsack = createNeonButton("Execute Knapsack Engine");
        controls.add(lblTopic); controls.add(cmbTopic);
        controls.add(lblTarget); controls.add(txtTarget);
        controls.add(btnKnapsack);

        JTextArea console = createDisplayConsole();
        mainPanel.add(controls, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(console), BorderLayout.CENTER);

        btnKnapsack.addActionListener(e -> {
            try {
                String topic = (String) cmbTopic.getSelectedItem();
                int target = Integer.parseInt(txtTarget.getText());
                console.setText(runKnapsackEngine(topic, target));
            } catch (Exception ex) {
                console.setText("Invalid constraints parsed.");
            }
        });

        return mainPanel;
    }

    private String runKnapsackEngine(String topic, int targetMarks) {
        List<M2Question> allQuestions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_GENERATOR))) {
            br.readLine(); // headers
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    allQuestions.add(new M2Question(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        parts[5].trim()
                    ));
                }
            }
        } catch (Exception e) {
            return "Failed to load " + FILE_GENERATOR;
        }

        // Filtering metrics relative to target domain selection
        List<M2Question> filtered = new ArrayList<>();
        for (M2Question q : allQuestions) {
            if (q.topic.equalsIgnoreCase(topic)) filtered.add(q);
        }

        int n = filtered.size();
        if (n == 0) return "No data present in the sub-pool matching topic context: " + topic;

        // DP Standard 0/1 Knapsack Execution Strategy maximizing question priority rankings
        int[][] dp = new int[n + 1][targetMarks + 1];
        for (int i = 1; i <= n; i++) {
            M2Question q = filtered.get(i - 1);
            for (int w = 0; w <= targetMarks; w++) {
                if (q.marks <= w) {
                    dp[i][w] = Math.max(filtered.get(i - 1).score + dp[i - 1][w - q.marks], dp[i - 1][w]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        // Backtracking optimization route maps
        int w = targetMarks;
        List<M2Question> selection = new ArrayList<>();
        for (int i = n; i > 0 && w > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                M2Question q = filtered.get(i - 1);
                selection.add(q);
                w -= q.marks;
            }
        }

        if (w != 0) {
            return "Verification Alert: Knapsack optimization was unable to build a combination totaling exactly " + targetMarks + " marks.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("⚡ DAA OPTIMIZED KNAPSACK COMPILE SUCCESSFUL ⚡\n");
        sb.append("Topic Focus: ").append(topic.toUpperCase()).append(" | Total Target: ").append(targetMarks).append(" M\n");
        sb.append("================================================================\n\n");
        int index = 1;
        for (M2Question q : selection) {
            sb.append("Q").append(index++).append(". ").append(q.text)
              .append(" \n   [Marks: ").append(q.marks).append(" | Priority Score: ").append(q.score).append("]\n\n");
        }
        return sb.toString();
    }

    // =========================================================================
    // MODULE 3: ADAPTIVE ASSESSMENT SYSTEM (Interactive Testing Track)
    // =========================================================================
    private List<M3Question> adaptivePool = new ArrayList<>();
    private int currentQuizIndex = 0;
    private int sessionCorrectCount = 0;
    private final Map<String, int[]> topicMetrics = new HashMap<>(); // key -> [correctCount, totalCount]

    private JPanel createModule3Panel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(new EmptyBorder(20, 25, 25, 25));

        JPanel quizScreen = new JPanel(new GridLayout(6, 1, 10, 10));
        quizScreen.setBackground(BG_CARD);
        quizScreen.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblQText = new JLabel("Click below to initialize the Adaptive Engine testing instance.");
        lblQText.setForeground(TEXT_WHITE);
        lblQText.setFont(new Font("SansSerif", Font.BOLD, 14));

        JRadioButton rA = new JRadioButton("A"); JRadioButton rB = new JRadioButton("B");
        JRadioButton rC = new JRadioButton("C"); JRadioButton rD = new JRadioButton("D");
        ButtonGroup group = new ButtonGroup();
        group.add(rA); group.add(rB); group.add(rC); group.add(rD);

        styleRadio(rA); styleRadio(rB); styleRadio(rC); styleRadio(rD);

        JButton btnAction = createNeonButton("Initialize Session Instance");

        quizScreen.add(lblQText);
        quizScreen.add(rA); quizScreen.add(rB); quizScreen.add(rC); quizScreen.add(rD);
        quizScreen.add(btnAction);

        mainPanel.add(quizScreen, BorderLayout.CENTER);

        btnAction.addActionListener(e -> {
            if (btnAction.getText().equals("Initialize Session Instance")) {
                loadAdaptiveDataset();
                if (adaptivePool.isEmpty()) {
                    lblQText.setText("Parsing Error: Assessment configuration data not loaded.");
                    return;
                }
                currentQuizIndex = 0;
                sessionCorrectCount = 0;
                topicMetrics.clear();
                btnAction.setText("Submit Choice Response");
                presentAdaptiveQuestion(lblQText, rA, rB, rC, rD, group);
            } else if (btnAction.getText().equals("Submit Choice Response")) {
                String selected = "";
                if (rA.isSelected()) selected = "A";
                else if (rB.isSelected()) selected = "B";
                else if (rC.isSelected()) selected = "C";
                else if (rD.isSelected()) selected = "D";

                if (selected.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please select an answer response choice framework.");
                    return;
                }

                M3Question currentQ = adaptivePool.get(currentQuizIndex);
                boolean isCorrect = selected.equalsIgnoreCase(currentQ.correctAnswer);
                
                // Track performance data metrics
                topicMetrics.putIfAbsent(currentQ.topic, new int[]{0, 0});
                topicMetrics.get(currentQ.topic)[1]++; // Total increments
                if (isCorrect) {
                    sessionCorrectCount++;
                    topicMetrics.get(currentQ.topic)[0]++; // Correct increments
                }

                currentQuizIndex++;
                if (currentQuizIndex < Math.min(5, adaptivePool.size())) { // Evaluate processing sequence over 5 item variations
                    group.clearSelection();
                    presentAdaptiveQuestion(lblQText, rA, rB, rC, rD, group);
                } else {
                    btnAction.setText("Initialize Session Instance");
                    showPerformanceAnalyticsReport(lblQText, rA, rB, rC, rD, group);
                }
            }
        });

        return mainPanel;
    }

    private void styleRadio(JRadioButton radio) {
        radio.setBackground(BG_CARD);
        radio.setForeground(TEXT_WHITE);
        radio.setFont(new Font("SansSerif", Font.PLAIN, 13));
        radio.setVisible(false);
    }

    private void loadAdaptiveDataset() {
        adaptivePool.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_ADAPTIVE))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length >= 12) {
                    adaptivePool.add(new M3Question(
                        parts[1].replace("\"", ""),
                        parts[2].replace("\"", ""), parts[3].replace("\"", ""),
                        parts[4].replace("\"", ""), parts[5].replace("\"", ""),
                        parts[9].trim(), parts[11].trim()
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Problem updating data stream parsing components.");
        }
        Collections.shuffle(adaptivePool);
    }

    private void presentAdaptiveQuestion(JLabel qTxt, JRadioButton a, JRadioButton b, JRadioButton c, JRadioButton d, ButtonGroup bg) {
        M3Question q = adaptivePool.get(currentQuizIndex);
        qTxt.setText("<html><body><b>[Topic: " + q.topic + "]</b><br/>" + q.text + "</body></html>");
        a.setText("A: " + q.opA); a.setVisible(true);
        b.setText("B: " + q.opB); b.setVisible(true);
        c.setText("C: " + q.opC); c.setVisible(true);
        d.setText("D: " + q.opD); d.setVisible(true);
    }

    private void showPerformanceAnalyticsReport(JLabel qTxt, JRadioButton a, JRadioButton b, JRadioButton c, JRadioButton d, ButtonGroup bg) {
        a.setVisible(false); b.setVisible(false); c.setVisible(false); d.setVisible(false);
        bg.clearSelection();

        StringBuilder report = new StringBuilder("<html><body>📊 <b>SESSION ANALYTICS CONTEXT COMPILE DONE</b><br/>");
        report.append("Total Score: ").append(sessionCorrectCount).append(" out of 5 items correctly processed.<br/><br/>");
        report.append("<b>Topic Framework Breakdown:</b><br/>");

        List<String> weakTopics = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : topicMetrics.entrySet()) {
            double accuracy = (entry.getValue()[0] * 100.0) / entry.getValue()[1];
            report.append("&bull; ").append(entry.getKey()).append(" -> ").append(String.format("%.1f", accuracy)).append("% accuracy<br/>");
            if (accuracy < 50.0) {
                weakTopics.add(entry.getKey());
            }
        }

        if (!weakTopics.isEmpty()) {
            report.append("<br/><font color='#FF4466'>⚠️ <b>System Flagged Weak Core Zones:</b> ").append(weakTopics).append("</font>");
        } else {
            report.append("<br/><font color='#00FFCC'>✨ Minimal validation anomalies. Competency scales within target boundaries.</font>");
        }

        report.append("</body></html>");
        qTxt.setText(report.toString());
    }

    // =========================================================================
    // INTERNAL MODEL ENTITY INJECTIONS
    // =========================================================================
    static class M1Question {
        String text, difficulty; int marks;
        M1Question(String t, int m, String d) { this.text = t; this.marks = m; this.difficulty = d; }
    }

    static class M2Question {
        int id, marks, score; String text, topic;
        M2Question(int id, String text, int marks, int score, String topic) {
            this.id = id; this.text = text; this.marks = marks; this.score = score; this.topic = topic;
        }
    }

    static class M3Question {
        String text, opA, opB, opC, opD, topic, correctAnswer;
        M3Question(String t, String a, String b, String c, String d, String tp, String ca) {
            this.text = t; this.opA = a; this.opB = b; this.opC = c; this.opD = d; this.topic = tp; this.correctAnswer = ca;
        }
    }

    // =========================================================================
    // BOOTSTRAP EXECUTOR ENTRY POINT
    // =========================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UnifiedAssessmentSuite().setVisible(true));
    }
}