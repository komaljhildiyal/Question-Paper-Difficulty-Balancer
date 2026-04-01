import java.io.*;
import java.util.*;

class Question {
    int id;
    String questionText;
    int marks;
    String difficulty;
    String topic;
    String bloom;

    public Question(int id, String questionText, int marks,
                    String difficulty, String topic, String bloom) {
        this.id = id;
        this.questionText = questionText;
        this.marks = marks;
        this.difficulty = difficulty;
        this.topic = topic;
        this.bloom = bloom;
    }
}

public class QPDB {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Total Marks (50/80/100): ");
        int totalMarks = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter Difficulty (Easy/Medium/Hard): ");
        String diff = sc.nextLine();

        generatePaper(totalMarks, diff);
        sc.close();
    }

    public static void generatePaper(int totalMarks, String diff) {

        int easyP, medP, hardP;

        if(diff.equalsIgnoreCase("Easy")) {
            easyP=60; medP=30; hardP=10;
        } else if(diff.equalsIgnoreCase("Medium")) {
            easyP=30; medP=50; hardP=20;
        } else {
            easyP=20; medP=40; hardP=40;
        }

        List<Question> allQ = loadQuestions();
        Collections.shuffle(allQ);

        List<Question> easy = new ArrayList<>();
        List<Question> medium = new ArrayList<>();
        List<Question> hard = new ArrayList<>();

        for(Question q: allQ){
            if(q.difficulty.equalsIgnoreCase("Easy")) easy.add(q);
            else if(q.difficulty.equalsIgnoreCase("Medium")) medium.add(q);
            else hard.add(q);
        }

        int easyMarks = totalMarks * easyP /100;
        int medMarks = totalMarks * medP /100;
        int hardMarks = totalMarks * hardP /100;

        List<Question> eSet = knapsack(easy, easyMarks);
        List<Question> mSet = knapsack(medium, medMarks);
        List<Question> hSet = knapsack(hard, hardMarks);

        List<Question> combined = new ArrayList<>();
        combined.addAll(eSet);
        combined.addAll(mSet);
        combined.addAll(hSet);

        List<Question> finalPaper = applyBloomBalancer(combined, totalMarks);

        Map<String,Integer> topicCount = new HashMap<>();
        List<Question> filtered = new ArrayList<>();

        for(Question q: finalPaper){
            int c = topicCount.getOrDefault(q.topic,0);
            if(c < 2){
                filtered.add(q);
                topicCount.put(q.topic,c+1);
            }
        }

        finalPaper = filtered;

        int remaining = totalMarks - getMarks(finalPaper);

        for(Question q: allQ){
            int c = topicCount.getOrDefault(q.topic,0);

            if(!finalPaper.contains(q) && q.marks <= remaining && c < 2){
                finalPaper.add(q);
                remaining -= q.marks;
                topicCount.put(q.topic,c+1);
            }
            if(remaining==0) break;
        }

        makeExact(finalPaper, allQ, totalMarks);
        display(finalPaper, totalMarks);
    }

    public static List<Question> applyBloomBalancer(List<Question> input, int totalMarks){

        int remMarks = totalMarks * 20 / 100;
        int undMarks = totalMarks * 20 / 100;
        int appMarks = totalMarks * 30 / 100;
        int anaMarks = totalMarks * 30 / 100;

        List<Question> remember = new ArrayList<>();
        List<Question> understand = new ArrayList<>();
        List<Question> apply = new ArrayList<>();
        List<Question> analyze = new ArrayList<>();

        for(Question q: input){
            if(q.bloom.equalsIgnoreCase("Remember")) remember.add(q);
            else if(q.bloom.equalsIgnoreCase("Understand")) understand.add(q);
            else if(q.bloom.equalsIgnoreCase("Apply")) apply.add(q);
            else analyze.add(q);
        }

        List<Question> result = new ArrayList<>();

        result.addAll(knapsack(remember, remMarks));
        result.addAll(knapsack(understand, undMarks));
        result.addAll(knapsack(apply, appMarks));
        result.addAll(knapsack(analyze, anaMarks));

        return result;
    }

    public static void makeExact(List<Question> paper, List<Question> all, int total){

        int current = getMarks(paper);
        if(current == total) return;

        for(Question remove: new ArrayList<>(paper)){

            paper.remove(remove);
            int needed = total - (current - remove.marks);

            List<Question> candidates = new ArrayList<>();
            for(Question q: all)
                if(!paper.contains(q)) candidates.add(q);

            List<Question> rep = subsetSumExact(candidates, needed);

            if(rep != null){
                paper.addAll(rep);
                return;
            }

            paper.add(remove);
        }
    }

    public static List<Question> subsetSumExact(List<Question> list, int target){

        int n = list.size();
        boolean[][] dp = new boolean[n+1][target+1];

        for(int i=0;i<=n;i++) dp[i][0]=true;

        for(int i=1;i<=n;i++){
            int m = list.get(i-1).marks;
            for(int j=1;j<=target;j++){
                if(m<=j)
                    dp[i][j]= dp[i-1][j] || dp[i-1][j-m];
                else dp[i][j]=dp[i-1][j];
            }
        }

        if(!dp[n][target]) return null;

        List<Question> res = new ArrayList<>();
        int j=target;

        for(int i=n;i>0 && j>0;i--){
            if(!dp[i-1][j]){
                Question q = list.get(i-1);
                res.add(q);
                j -= q.marks;
            }
        }
        return res;
    }

    public static int getMarks(List<Question> list){
        int sum=0;
        for(Question q:list) sum+=q.marks;
        return sum;
    }

    public static List<Question> loadQuestions(){
        List<Question> list = new ArrayList<>();
        try{
            BufferedReader br = new BufferedReader(new FileReader("questions.csv"));
            br.readLine();
            String line;
            while((line=br.readLine())!=null){
                String[] d = line.split(",");
                list.add(new Question(
                        Integer.parseInt(d[0]), d[1], Integer.parseInt(d[2]),
                        d[3], d[4], d[5]
                ));
            }
            br.close();
        }catch(Exception e){
            System.out.println("CSV Error");
        }
        return list;
    }

    public static List<Question> knapsack(List<Question> q, int max){

        int n=q.size();
        int[][] dp=new int[n+1][max+1];

        for(int i=1;i<=n;i++){
            int m=q.get(i-1).marks;
            for(int w=0;w<=max;w++){
                if(m<=w)
                    dp[i][w]=Math.max(dp[i-1][w], m+dp[i-1][w-m]);
                else dp[i][w]=dp[i-1][w];
            }
        }

        List<Question> res=new ArrayList<>();
        int w=max;

        for(int i=n;i>0;i--){
            if(dp[i][w]!=dp[i-1][w]){
                Question qu=q.get(i-1);
                res.add(qu);
                w-=qu.marks;
            }
        }
        return res;
    }

    public static void display(List<Question> paper, int total){

        String time;
        if (total == 50) time = "1.5 Hours";
        else if (total == 80) time = "2 Hours";
        else time = "3 Hours";

        int totalM=0, qNo=1;

        System.out.println("\nCLASS XII PHYSICS EXAM");
        System.out.println("======================================");
        System.out.println("Time: " + time + "\tMaximum Marks: " + total);
        System.out.println("======================================\n");

        System.out.println("SECTION A (Easy)");
        for(Question q:paper){
            if(q.difficulty.equalsIgnoreCase("Easy")){
                System.out.println("Q"+qNo+++". "+q.questionText);
                System.out.println("   ["+q.marks+" marks]\n");
                totalM+=q.marks;
            }
        }

        System.out.println("\nSECTION B (Medium)");
        for(Question q:paper){
            if(q.difficulty.equalsIgnoreCase("Medium")){
                System.out.println("Q"+qNo+++". "+q.questionText);
                System.out.println("   ["+q.marks+" marks]\n");
                totalM+=q.marks;
            }
        }

        System.out.println("\nSECTION C (Hard)");
        for(Question q:paper){
            if(q.difficulty.equalsIgnoreCase("Hard")){
                System.out.println("Q"+qNo+++". "+q.questionText);
                System.out.println("   ["+q.marks+" marks]\n");
                totalM+=q.marks;
            }
        }

        System.out.println("======================================");
        System.out.println("TOTAL: "+totalM+"/"+total);
    }
}