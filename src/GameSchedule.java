import java.text.SimpleDateFormat;
import java.util.*;

public class GameSchedule {

    // ─── 팀별 PROGRAM_CD 상수 ────────────────────────────────
    private static final Map<String, String> TEAM_CD = new HashMap<>();
    static {
        TEAM_CD.put("KIA",  "10000012");
        TEAM_CD.put("NC",   "10000025");
        TEAM_CD.put("삼성", "10000015");
        TEAM_CD.put("LG",   "10000010");
        TEAM_CD.put("한화", "10000011");
        TEAM_CD.put("두산", "10000013");
        TEAM_CD.put("SSG",  "");   // 확인 필요
        TEAM_CD.put("KT",   "");   // 확인 필요
        TEAM_CD.put("키움", "");   // 확인 필요
    }

    // ─── 경기 일정 ────────────────────────────────────────────
    // {날짜, 시간, 상대팀, 홈/원정}
    // CD는 런타임에 자동 매핑됨
    private static final String[][] SCHEDULE = {
        {"20260501", "1700", "SSG",  "홈"},
        {"20260502", "1400", "SSG",  "원정"},
        {"20260503", "1400", "SSG",  "원정"},
        {"20260505", "1400", "KT",   "원정"},
        {"20260506", "1830", "KT",   "원정"},
        {"20260507", "1830", "KT",   "원정"},
        {"20260508", "1830", "KIA",  "홈"},
        {"20260509", "1700", "KIA",  "홈"},
        {"20260510", "1400", "KIA",  "홈"},
        {"20260512", "1830", "NC",   "홈"},
        {"20260513", "1830", "NC",   "홈"},
        {"20260514", "1830", "NC",   "홈"},
        {"20260515", "1830", "두산", "원정"},
        {"20260516", "1700", "두산", "원정"},
        {"20260517", "1400", "두산", "원정"},
        {"20260519", "1830", "한화", "원정"},
        {"20260520", "1830", "한화", "원정"},
        {"20260521", "1830", "한화", "원정"},
        {"20260522", "1830", "삼성", "홈"},
        {"20260523", "1700", "삼성", "홈"},
        {"20260524", "1400", "삼성", "홈"},
        {"20260526", "1830", "LG",   "홈"},
        {"20260527", "1830", "LG",   "홈"},
        {"20260528", "1830", "LG",   "홈"},
        {"20260529", "1830", "NC",   "원정"},
        {"20260530", "1700", "NC",   "원정"},
        {"20260531", "1400", "NC",   "원정"},
        {"20260602", "1830", "KIA",  "원정"},
        {"20260603", "1700", "KIA",  "원정"},
        {"20260604", "1830", "KIA",  "원정"},
        {"20260605", "1830", "한화", "홈"},
        {"20260606", "1700", "한화", "홈"},
        {"20260607", "1700", "한화", "홈"},
        {"20260609", "1830", "두산", "홈"},
        {"20260610", "1830", "두산", "홈"},
        {"20260611", "1830", "두산", "홈"},
        {"20260612", "1830", "LG",   "원정"},
        {"20260613", "1700", "LG",   "원정"},
        {"20260614", "1700", "LG",   "원정"},
        {"20260616", "1830", "SSG",  "원정"},
        {"20260617", "1830", "SSG",  "원정"},
        {"20260618", "1830", "SSG",  "원정"},
        {"20260619", "1830", "키움", "원정"},
        {"20260620", "1700", "키움", "원정"},
        {"20260621", "1400", "키움", "원정"},
        {"20260623", "1830", "NC",   "홈"},
        {"20260624", "1830", "NC",   "홈"},
        {"20260625", "1830", "NC",   "홈"},
        {"20260626", "1830", "LG",   "홈"},
        {"20260627", "1700", "LG",   "홈"},
        {"20260628", "1700", "LG",   "홈"},
        {"20260630", "1830", "두산", "원정"},
        {"20260701", "1830", "두산", "원정"},
        {"20260702", "1830", "두산", "원정"},
        {"20260703", "1830", "KT",   "원정"},
        {"20260704", "1800", "KT",   "원정"},
        {"20260705", "1800", "KT",   "원정"},
        {"20260707", "1830", "KIA",  "홈"},
        {"20260708", "1830", "KIA",  "홈"},
        {"20260709", "1830", "KIA",  "홈"},
        {"20260716", "1830", "삼성", "원정"},
        {"20260717", "1830", "삼성", "원정"},
        {"20260718", "1800", "삼성", "원정"},
        {"20260719", "1800", "삼성", "원정"},
        {"20260721", "1830", "SSG",  "홈"},
        {"20260722", "1830", "SSG",  "홈"},
        {"20260723", "1830", "SSG",  "홈"},
        {"20260724", "1830", "KT",   "홈"},
        {"20260725", "1800", "KT",   "홈"},
        {"20260726", "1800", "KT",   "홈"},
        {"20260728", "1830", "한화", "원정"},
        {"20260729", "1830", "한화", "원정"},
        {"20260730", "1830", "한화", "원정"},
        {"20260731", "1830", "삼성", "홈"},
        {"20260801", "1800", "삼성", "홈"},
        {"20260802", "1800", "삼성", "홈"},
        {"20260804", "1830", "키움", "홈"},
        {"20260805", "1830", "키움", "홈"},
        {"20260806", "1830", "키움", "홈"},
        {"20260807", "1830", "KT",   "원정"},
        {"20260808", "1800", "KT",   "원정"},
        {"20260809", "1800", "KT",   "원정"},
        {"20260811", "1830", "SSG",  "원정"},
        {"20260812", "1830", "SSG",  "원정"},
        {"20260813", "1830", "SSG",  "원정"},
        {"20260814", "1830", "NC",   "홈"},
        {"20260815", "1800", "NC",   "홈"},
        {"20260816", "1800", "NC",   "홈"},
        {"20260818", "1830", "키움", "홈"},
        {"20260819", "1830", "키움", "홈"},
        {"20260820", "1830", "키움", "홈"},
        {"20260821", "1830", "두산", "원정"},
        {"20260822", "1800", "두산", "원정"},
        {"20260823", "1800", "두산", "원정"},
        {"20260825", "1830", "KIA",  "원정"},
        {"20260826", "1830", "KIA",  "원정"},
        {"20260827", "1830", "KIA",  "원정"},
        {"20260828", "1830", "LG",   "홈"},
        {"20260829", "1800", "LG",   "홈"},
        {"20260830", "1800", "LG",   "홈"},
        {"20260901", "1830", "삼성", "원정"},
        {"20260902", "1830", "삼성", "원정"},
        {"20260903", "1830", "삼성", "원정"},
        {"20260904", "1830", "한화", "홈"},
        {"20260905", "1700", "한화", "홈"},
        {"20260906", "1700", "한화", "홈"},
    };

    // ─── ALL_GAMES 빌드 ───────────────────────────────────────
    // {날짜, 시간, 상대팀, PROGRAM_CD, 홈/원정}
    // 오늘 기준 가까운 홈경기 6개만 CD 매핑 → 나머지는 빈값
    private static final String[][] ALL_GAMES;
    static {
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // 1. 오늘 이후 홈경기 날짜 6개 추출
        Set<String> openDates = new HashSet<>();
        int count = 0;
        for (String[] s : SCHEDULE) {
            if (s[0].compareTo(today) >= 0 && s[3].equals("홈") && count < 6) {
                openDates.add(s[0]);
                count++;
            }
        }

        // 2. ALL_GAMES 빌드
        ALL_GAMES = new String[SCHEDULE.length][5];
        for (int i = 0; i < SCHEDULE.length; i++) {
            String date = SCHEDULE[i][0];
            String time = SCHEDULE[i][1];
            String team = SCHEDULE[i][2];
            String type = SCHEDULE[i][3];
            // 오늘 이후 홈경기 6개만 CD 매핑
            String cd = (type.equals("홈") && openDates.contains(date))
                        ? TEAM_CD.getOrDefault(team, "") : "";
            ALL_GAMES[i] = new String[]{date, time, team, cd, type};
        }
    }

    // ─── 조회 메서드 ─────────────────────────────────────────

    // 오늘 이후 경기
    public static List<String[]> getUpcomingGames(int count) {
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        List<String[]> result = new ArrayList<>();
        for (String[] game : ALL_GAMES) {
            if (game[0].compareTo(today) >= 0) {
                result.add(game);
                if (result.size() >= count) break;
            }
        }
        return result;
    }

    // 홈 경기 중 CD 있는 것만 (로딩 화면용)
    public static List<String[]> getUpcomingHomeGames(int count) {
        String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
        List<String[]> result = new ArrayList<>();
        for (String[] game : ALL_GAMES) {
            if (game[0].compareTo(today) >= 0 && game[4].equals("홈") && !game[3].isEmpty()) {
                result.add(game);
                if (result.size() >= count) break;
            }
        }
        return result;
    }

    // 전체 경기 (캘린더용)
    public static List<String[]> getAllGames() {
        return Arrays.asList(ALL_GAMES);
    }

    // 팀별 CD 업데이트
    public static void updateTeamCd(String team, String cd) {
        TEAM_CD.put(team, cd);
        for (String[] game : ALL_GAMES) {
            if (game[2].equals(team) && game[4].equals("홈") && !game[3].isEmpty()) {
                game[3] = cd;
            }
        }
    }

    // 팀별 CD 조회
    public static String getCd(String team) {
        return TEAM_CD.getOrDefault(team, "");
    }

    // 현장예매 여부
    public static boolean isOnSiteOnly(String date, String time) {
        try {
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
            if (!date.equals(today)) return false;
            int gameHour = Integer.parseInt(time.substring(0, 2));
            int gameMin  = Integer.parseInt(time.substring(2, 4));
            Calendar gameCal = Calendar.getInstance();
            gameCal.set(Calendar.HOUR_OF_DAY, gameHour);
            gameCal.set(Calendar.MINUTE, gameMin);
            gameCal.set(Calendar.SECOND, 0);
            gameCal.add(Calendar.HOUR_OF_DAY, -3);
            return new Date().after(gameCal.getTime());
        } catch (Exception e) {
            return false;
        }
    }
}
