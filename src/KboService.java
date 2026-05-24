import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class KboService {

    private static final String RANK_URL     = "https://www.koreabaseball.com/Record/TeamRank/TeamRankDaily.aspx";
    private static final String SCHEDULE_URL = "https://www.koreabaseball.com/Schedule/Schedule.aspx";

    // ─── 팀 순위 데이터 ──────────────────────────────────────
    public static class TeamRank {
        public final int    rank;
        public final String team;
        public final int    games;
        public final int    win;
        public final int    lose;
        public final int    draw;
        public final String winRate;
        public final String gameDiff;
        public final String last10;
        public final String streak;

        public TeamRank(int rank, String team, int games, int win, int lose, int draw,
                        String winRate, String gameDiff, String last10, String streak) {
            this.rank = rank; this.team = team; this.games = games;
            this.win = win; this.lose = lose; this.draw = draw;
            this.winRate = winRate; this.gameDiff = gameDiff;
            this.last10 = last10; this.streak = streak;
        }

        public boolean isLotte() { return team.contains("롯데"); }
    }

    // ─── 경기 결과 데이터 ─────────────────────────────────────
    public static class GameResult {
        public final String date;
        public final String opponent;
        public final String result;  // 승/패/무
        public final String score;   // 롯데점수:상대점수

        public GameResult(String date, String opponent, String result, String score) {
            this.date = date; this.opponent = opponent;
            this.result = result; this.score = score;
        }
    }


    // ─── KBO 순위 크롤링 ─────────────────────────────────────
    public static List<TeamRank> fetchRanking() {
        List<TeamRank> result = new ArrayList<>();
        try {
            String html = httpGet(RANK_URL);
            if (html == null) return result;

            Pattern trPattern  = Pattern.compile("<tbody>[\\s\\S]*?</tbody>", Pattern.DOTALL);
            Pattern rowPattern = Pattern.compile("<tr>[\\s\\S]*?</tr>", Pattern.DOTALL);
            Pattern tdPattern  = Pattern.compile("<td[^>]*>([^<]*)</td>");

            Matcher trM = trPattern.matcher(html);
            if (!trM.find()) return result;

            Matcher rowM = rowPattern.matcher(trM.group());
            while (rowM.find()) {
                Matcher tdM = tdPattern.matcher(rowM.group());
                List<String> cols = new ArrayList<>();
                while (tdM.find()) cols.add(tdM.group(1).trim());
                if (cols.size() < 10) continue;
                try {
                    result.add(new TeamRank(
                        Integer.parseInt(cols.get(0)), cols.get(1),
                        Integer.parseInt(cols.get(2)), Integer.parseInt(cols.get(3)),
                        Integer.parseInt(cols.get(4)), Integer.parseInt(cols.get(5)),
                        cols.get(6), cols.get(7), cols.get(8), cols.get(9)
                    ));
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            System.out.println("KBO 순위 크롤링 실패: " + e.getMessage());
        }
        return result;
    }

    // ─── 롯데 경기 결과 크롤링 ───────────────────────────────
    // yearMonth: "202605" 형태
    public static List<GameResult> fetchLotteResults(String yearMonth) {
        List<GameResult> results = new ArrayList<>();
        try {
            String url = SCHEDULE_URL + "?seriesId=0&gameDate=" + yearMonth + "01";
            String html = httpGet(url);
            if (html == null) return results;

            // gameDate 포함된 tr 단위로 파싱
            // <tr><td class="day"...>날짜</td>...<td class="play">...</td></tr>
            // 날짜는 rowspan이라 첫 행에만 있음 → 현재 날짜 추적

            Pattern rowPattern = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL);
            // 날짜 패턴: "05.08(금)" 형태
            Pattern datePattern = Pattern.compile("<td[^>]*class=\"day\"[^>]*>\\s*(\\d+)\\.(\\d+)");
            // 경기 결과 패턴
            Pattern playPattern = Pattern.compile(
                "<span>([^<]+)</span><em>" +
                "<span class=\"(win|lose|same)\">(\\d+)</span>" +
                "<span>vs</span>" +
                "<span class=\"(win|lose|same)\">(\\d+)</span>" +
                "</em><span>([^<]+)</span>"
            );

            Matcher rowM = rowPattern.matcher(html);
            String currentDate = "";

            while (rowM.find()) {
                String row = rowM.group(1);

                // 날짜 업데이트 (day 클래스 있는 경우)
                Matcher dateM = datePattern.matcher(row);
                if (dateM.find()) {
                    String month = dateM.group(1); // "05"
                    String day   = dateM.group(2); // "08"
                    currentDate  = yearMonth.substring(0, 4) + month + day;
                }

                if (currentDate.isEmpty() || !currentDate.startsWith(yearMonth)) continue;

                // 경기 결과 파싱
                Matcher playM = playPattern.matcher(row);
                while (playM.find()) {
                    String team1   = playM.group(1).trim();
                    String cls1    = playM.group(2); // win/lose/same
                    int    score1  = Integer.parseInt(playM.group(3));
                    String cls2    = playM.group(4);
                    int    score2  = Integer.parseInt(playM.group(5));
                    String team2   = playM.group(6).trim();

                    if (team1.equals("롯데")) {
                        // 롯데가 왼쪽
                        String res   = cls1.equals("win") ? "승" : cls1.equals("lose") ? "패" : "무";
                        String score = score1 + ":" + score2;
                        results.add(new GameResult(currentDate, team2, res, score));

                    } else if (team2.equals("롯데")) {
                        // 롯데가 오른쪽
                        String res   = cls2.equals("win") ? "승" : cls2.equals("lose") ? "패" : "무";
                        String score = score2 + ":" + score1;
                        results.add(new GameResult(currentDate, team1, res, score));
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("경기 결과 크롤링 실패: " + e.getMessage());
        }
        return results;
    }

    // ─── HTTP GET ─────────────────────────────────────────────
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

    public static void main(String[] args) throws Exception {
        URL url = new URL("https://www.koreabaseball.com/Schedule/Schedule.aspx?seriesId=0&gameDate=20260501");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        br.close();

        String html = sb.toString();

        // 롯데 관련 부분만 출력
        int idx = html.indexOf("롯데");
        if (idx >= 0) {
            System.out.println("롯데 발견! 주변 텍스트:");
            System.out.println(html.substring(Math.max(0, idx-100), Math.min(html.length(), idx+200)));
        } else {
            System.out.println("롯데 없음");
        }
    }
}
