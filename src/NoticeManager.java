import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class NoticeManager {

    private static final String FILE = "notices.txt";
    private static final List<Notice> notices = new ArrayList<>();
    private static int nextId = 1;

    // 파일에서 불러오기
    public static void load() {
        File f = new File(FILE);
        if (!f.exists()) {
            addDefault();
            save();
            return;
        }
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
            notices.clear();
            String line;
            while ((line = br.readLine()) != null) {
                Notice n = Notice.fromFileLine(line);
                if (n != null) {
                    notices.add(n);
                    if (n.getId() >= nextId) nextId = n.getId() + 1;
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("공지 로드 실패: " + e.getMessage());
        }
    }

    // 파일에 저장
    public static void save() {
        try {
            PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(FILE), StandardCharsets.UTF_8));
            for (Notice n : notices) pw.println(n.toFileLine());
            pw.close();
        } catch (Exception e) {
            System.out.println("공지 저장 실패: " + e.getMessage());
        }
    }

    // 공지 추가
    public static void add(String tag, String title, String content) {
        String date = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        notices.add(0, new Notice(nextId++, tag, title, content, date));
        save();
    }

    // 공지 삭제
    public static void delete(int id) {
        notices.removeIf(n -> n.getId() == id);
        save();
    }

    // 공지 수정
    public static void update(int id, String tag, String title, String content) {
        for (Notice n : notices) {
            if (n.getId() == id) {
                n.setTag(tag);
                n.setTitle(title);
                n.setContent(content);
                break;
            }
        }
        save();
    }

    // 읽음 처리
    public static void markRead(int id) {
        for (Notice n : notices) {
            if (n.getId() == id) { n.setUnread(false); save(); break; }
        }
    }

    public static List<Notice> getAll()  { return new ArrayList<>(notices); }
    public static int getUnreadCount()   { return (int) notices.stream().filter(Notice::isUnread).count(); }

    // 기본 공지 추가
    private static void addDefault() {
        String today = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        notices.add(new Notice(nextId++, "이벤트",
                "5/24 삼성전 클래식 유니폼 배포",
                "롯데 자이언츠가 5월 24일 삼성 라이온즈와의 홈경기에서 클래식 유니폼 배포 이벤트를 진행합니다.\n선착순 3,000명 대상 · 입장 시 게이트 수령",
                today));
        notices.add(new Notice(nextId++, "예매",
                "5/24 삼성전 예매 경쟁 주의",
                "이벤트 경기 특성상 예매 시작과 동시에 빠르게 마감될 수 있습니다.",
                today));
        notices.add(new Notice(nextId++, "공지",
                "5/26~28 LG전 예매 오픈 안내",
                "5월 13일 오후 2시 예매 오픈 예정입니다.",
                today));
    }
}