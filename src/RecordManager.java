import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RecordManager {

    private static final String FILE = "records.txt";
    private static final List<String[]> records = new ArrayList<>();

    // {날짜, 상대팀, 결과(승/패/무), 점수(롯데:상대), 좌석, 메모}
    // 예: "20260512,NC,승,5:3,3루 내야필드석A,너무 재밌었다"

    // ─── 파일에서 불러오기 ────────────────────────────────────
    public static void load() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try {
            BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
            records.clear();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 6);
                if (parts.length >= 3) records.add(parts);
            }
            br.close();
        } catch (Exception e) {
            System.out.println("직관 기록 로드 실패: " + e.getMessage());
        }
    }

    // ─── 파일에 저장 ──────────────────────────────────────────
    public static void save() {
        try {
            PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(FILE), StandardCharsets.UTF_8));
            for (String[] r : records) pw.println(String.join(",", r));
            pw.close();
        } catch (Exception e) {
            System.out.println("직관 기록 저장 실패: " + e.getMessage());
        }
    }

    // ─── 기록 추가 ────────────────────────────────────────────
    public static void add(String date, String opponent, String result,
                           String score, String seat, String memo) {
        records.add(0, new String[]{
            date,
            opponent,
            result,
            score  == null ? "" : score,
            seat   == null ? "" : seat,
            memo   == null ? "" : memo
        });
        save();
    }

    // ─── 기록 삭제 ────────────────────────────────────────────
    public static void remove(int index) {
        if (index >= 0 && index < records.size()) {
            records.remove(index);
            save();
        }
    }

    // ─── 전체 목록 ────────────────────────────────────────────
    public static List<String[]> getAll() { return new ArrayList<>(records); }

    // ─── 통계 ─────────────────────────────────────────────────
    public static int getTotalGames() { return records.size(); }

    public static int getWins() {
        return (int) records.stream().filter(r -> r[2].equals("승")).count();
    }

    public static int getLoses() {
        return (int) records.stream().filter(r -> r[2].equals("패")).count();
    }

    public static int getDraws() {
        return (int) records.stream().filter(r -> r[2].equals("무")).count();
    }
}
