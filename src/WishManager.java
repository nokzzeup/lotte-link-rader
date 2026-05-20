import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WishManager {

    private static final String FILE = "wishes.txt";
    private static final List<String[]> wishes = new ArrayList<>();

    // 파일에서 찜 목록 불러오기
    public static void load() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
            wishes.clear();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) wishes.add(parts);
            }
            br.close();
        } catch (Exception e) {
            System.out.println("찜 목록 로드 실패: " + e.getMessage());
        }
    }

    // 파일에 저장
    public static void save() {
        try {
            PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(FILE), StandardCharsets.UTF_8));
            for (String[] w : wishes) {
                pw.println(String.join(",", w));
            }
            pw.close();
        } catch (Exception e) {
            System.out.println("찜 목록 저장 실패: " + e.getMessage());
        }
    }

    // 찜 추가 {날짜, 시간, 상대팀, PROGRAM_CD, 홈/원정}
    public static void add(String[] game) {
        if (!isWished(game[0])) {
            wishes.add(game);
            save();
        }
    }

    // 찜 삭제
    public static void remove(String date) {
        wishes.removeIf(w -> w[0].equals(date));
        save();
    }

    // 찜 여부 확인
    public static boolean isWished(String date) {
        return wishes.stream().anyMatch(w -> w[0].equals(date));
    }

    // 전체 찜 목록
    public static List<String[]> getAll() {
        return new ArrayList<>(wishes);
    }

    // 특정 월 찜 목록
    public static List<String[]> getByMonth(String yearMonth) {
        List<String[]> result = new ArrayList<>();
        for (String[] w : wishes) {
            if (w[0].startsWith(yearMonth)) result.add(w);
        }
        return result;
    }
}