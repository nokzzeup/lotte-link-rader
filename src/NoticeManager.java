import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

public class NoticeManager {

    private static final String FILE       = "notices.txt";
    private static final String LIST_URL   = "https://www.giantsclub.com/html/?pcode=783&bcIdx=2&P=1&PC=20";
    private static final String DETAIL_URL = "https://www.giantsclub.com/html/?pcode=783&bcIdx=2&MODE=V&bidx=";

    private static final List<Notice> notices = new ArrayList<>();
    private static int nextId = 1;

    // ─── 파일에서 불러오기 ────────────────────────────────────
    public static void load() {
        File f = new File(FILE);
        if (!f.exists()) { save(); return; }
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

    // ─── 파일에 저장 ──────────────────────────────────────────
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

    // ─── 롯데 공식 공지 목록 크롤링 ──────────────────────────
    public static List<Notice> fetchFromWeb() {
        List<Notice> fetched = new ArrayList<>();
        try {
            String html = httpGet(LIST_URL);
            if (html == null) return fetched;

            Pattern bidxPattern = Pattern.compile(
                "bidx=(\\d+)[^\"]*\"><strong>([^<]+?)(?:&nbsp;)?(?:<img[^>]*>)?</strong>"
            );
            Pattern catPattern = Pattern.compile(
                "<p class=\"new_col\\d+\">([^<]+)</p>"
            );
            Pattern dayPattern = Pattern.compile(
                "<p class=\"news-list-date-day\">(\\d+)</p>[\\s\\S]*?" +
                "<p class=\"news-list-date-month\">([\\d.]+)</p>",
                Pattern.DOTALL
            );

            Matcher bidxM = bidxPattern.matcher(html);
            Matcher catM  = catPattern.matcher(html);
            Matcher dayM  = dayPattern.matcher(html);

            List<String> bidxList  = new ArrayList<>();
            List<String> titleList = new ArrayList<>();
            List<String> catList   = new ArrayList<>();
            List<String> dateList  = new ArrayList<>();

            while (bidxM.find()) {
                bidxList.add(bidxM.group(1));
                titleList.add(bidxM.group(2).trim());
            }
            while (catM.find()) catList.add(catM.group(1).trim());
            while (dayM.find()) dateList.add(dayM.group(2) + "." + dayM.group(1));

            for (int i = 0; i < bidxList.size(); i++) {
                String bidx  = bidxList.get(i);
                String title = titleList.get(i);
                String cat   = i < catList.size()  ? catList.get(i)  : "공지";
                String date  = i < dateList.size() ? dateList.get(i) : "";
                String link  = DETAIL_URL + bidx;
                String tag   = mapTag(cat);
                fetched.add(new Notice(nextId++, tag, title, link, date));
            }

        } catch (Exception e) {
            System.out.println("크롤링 실패: " + e.getMessage());
        }
        return fetched;
    }

    // ─── 공식 공지 상세 본문 크롤링 ──────────────────────────
    public static String fetchDetail(String url) {
        try {
            String html = httpGet(url);
            if (html == null) return "내용을 불러올 수 없습니다.";

            Pattern contPattern = Pattern.compile(
                "<td class=\"board-view-cont\"[^>]*>([\\s\\S]*?)</td>",
                Pattern.DOTALL
            );
            Matcher m = contPattern.matcher(html);
            if (m.find()) {
                String raw = m.group(1);

                // 블록 태그 끝에 줄바꿈 먼저 삽입
                raw = raw.replaceAll("</p>",  "\n");
                raw = raw.replaceAll("</tr>", "\n");
                raw = raw.replaceAll("</li>", "\n");
                raw = raw.replaceAll("</div>","\n");
                raw = raw.replaceAll("<br\\s*/?>", "\n");

                // 표 셀 사이 공백
                raw = raw.replaceAll("</th>", "  ");
                raw = raw.replaceAll("</td>", "  ");

                // 이미지 완전 제거
                raw = raw.replaceAll("<img[^>]*>", "");

                // 나머지 HTML 태그 제거
                raw = raw.replaceAll("<[^>]+>", "");

                // HTML 특수문자 변환
                raw = raw.replaceAll("&nbsp;",   " ");
                raw = raw.replaceAll("&amp;",    "&");
                raw = raw.replaceAll("&lt;",     "<");
                raw = raw.replaceAll("&gt;",     ">");
                raw = raw.replaceAll("&lsquo;",  "'");
                raw = raw.replaceAll("&rsquo;",  "'");
                raw = raw.replaceAll("&ldquo;",  "\"");
                raw = raw.replaceAll("&rdquo;",  "\"");
                raw = raw.replaceAll("&middot;", "·");
                raw = raw.replaceAll("&bull;",   "•");
                raw = raw.replaceAll("&#[0-9]+;","");
                raw = raw.replaceAll("&[a-z]+;", "");

                // 각 줄 앞뒤 공백 정리
                StringBuilder sb = new StringBuilder();
                for (String line : raw.split("\n")) {
                    sb.append(line.stripTrailing()).append("\n");
                }
                raw = sb.toString();

                // 3줄 이상 빈줄 → 2줄로 정리
                raw = raw.replaceAll("\n{3,}", "\n\n");

                return raw.trim();
            }
        } catch (Exception e) {
            System.out.println("상세 크롤링 실패: " + e.getMessage());
        }
        return "내용을 불러올 수 없습니다.";
    }

    // ─── 웹 공지 병합 (중복 제거) ─────────────────────────────
    public static void mergeFromWeb(List<Notice> fetched) {
        Set<String> existing = new HashSet<>();
        for (Notice n : notices) existing.add(n.getTitle());
        for (Notice n : fetched) {
            if (!existing.contains(n.getTitle())) notices.add(0, n);
        }
        save();
    }

    // ─── 태그 매핑 (롯데 카테고리 → 우리 태그) ───────────────
    private static String mapTag(String category) {
        switch (category) {
            case "티켓":     return "예매";
            case "이벤트":   return "이벤트";
            case "보도기사": return "기타";
            case "공지사항": return "공지";
            default:         return "기타";
        }
    }

    // ─── HTTP GET 요청 ────────────────────────────────────────
    private static String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        if (conn.getResponseCode() != 200) return null;
        BufferedReader br = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        br.close();
        return sb.toString();
    }

    // ─── 공지 CRUD ────────────────────────────────────────────
    public static void add(String tag, String title, String content) {
        String date = new SimpleDateFormat("yyyy.MM.dd").format(new Date());
        notices.add(0, new Notice(nextId++, tag, title, content, date));
        save();
    }

    public static void delete(int id) {
        notices.removeIf(n -> n.getId() == id);
        save();
    }

    public static void update(int id, String tag, String title, String content) {
        for (Notice n : notices) {
            if (n.getId() == id) {
                n.setTag(tag); n.setTitle(title); n.setContent(content);
                break;
            }
        }
        save();
    }

    public static void markRead(int id) {
        for (Notice n : notices) {
            if (n.getId() == id) { n.setUnread(false); save(); break; }
        }
    }

    public static List<Notice> getAll()  { return new ArrayList<>(notices); }
    public static int getUnreadCount()   { return (int) notices.stream().filter(Notice::isUnread).count(); }
}
