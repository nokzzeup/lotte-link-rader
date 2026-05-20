import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class SeatEngine {

    private static String JSESSIONID = "nBAXfS3nTnB98j5UzjvThrXltBdvdbQkafzesR5a1RI5fb57t18NHzWNBHuiTriJ.GTICKETWAS6_servlet_engine3";
    private static String USER_ID = "smin1115";

    public static void setSession(String jsessionid, String userId) {
        JSESSIONID = jsessionid;
        USER_ID = userId;
    }

    public static List<SeatInfo> fetch(String playDate, String playTime, String programCd) {
        try {
            String html = fetchPage(playDate, playTime, programCd);
            if (html == null) return null;

            if (html.contains("다시 로그인")) {
                System.out.println("  세션 만료 - 쿠키를 다시 뽑아주세요");
                return null;
            }
            if (html.contains("예매기간이 아닙니다")) {
                System.out.println("  아직 예매 기간이 아닙니다");
                return null;
            }
            if (html.contains("과도한 호출")) {
                System.out.println("  서버 요청 차단됨 - 잠시 후 재시도");
                return null;
            }

            return parse(html);

        } catch (Exception e) {
            System.out.println("  에러: " + e.getMessage());
            return null;
        }
    }

    private static String fetchPage(String playDate, String playTime, String programCd) throws Exception {
        URL url = new URL("https://ticket.giantsclub.com/booking_step2_floor_plan.do");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        conn.setRequestProperty("Referer", "https://ticket.giantsclub.com/booking_step2_floor_plan.do");
        conn.setRequestProperty("Cookie",
                "JSESSIONID=" + JSESSIONID + "; " +
                        "user_id=" + USER_ID + "; " +
                        "perf_dv6Tr4n=1"
        );

        String params = "PLAY_COMPANY_CD=500049"
                + "&PLACE_CD=1001"
                + "&REG_COMPANY_CD=110020"
                + "&PROGRAM_CD=" + programCd
                + "&LANG=K"
                + "&PLAY_SEQ_NM=1%ED%9A%8C%EC%B0%A8"
                + "&PLAY_SEQ_NM_ENC=1%ED%9A%8C%EC%B0%A8"
                + "&PLAY_DATE=" + playDate
                + "&PLAY_SEQ_CD=000001"
                + "&PLAY_ST_TIME=" + playTime
                + "&PHYMAP_NBR=035"
                + "&SEASON_RENEWAL=N"
                + "&MAX_BOOKING_COUNT=8"
                + "&BetweenDateCnt=1";

        byte[] postData = params.getBytes(StandardCharsets.UTF_8);
        conn.getOutputStream().write(postData);

        int status = conn.getResponseCode();
        if (status != 200) {
            System.out.println("  HTTP " + status);
            return null;
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8")
        );
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    private static List<SeatInfo> parse(String html) {
        Pattern namePattern = Pattern.compile(
                "<span style=\"width: \"[\\s\\S]*?>([^<]+)</span>",
                Pattern.DOTALL
        );
        Pattern remainPattern = Pattern.compile(
                "<span style=\"color: [^\"]*\"\\s*>([\\d]+)석</span>",
                Pattern.DOTALL
        );

        Matcher nameMatcher = namePattern.matcher(html);
        Matcher remainMatcher = remainPattern.matcher(html);

        List<String> names = new ArrayList<>();
        List<Integer> remains = new ArrayList<>();

        while (nameMatcher.find()) names.add(nameMatcher.group(1).trim());
        while (remainMatcher.find()) remains.add(Integer.parseInt(remainMatcher.group(1).trim()));

        if (names.isEmpty()) return null;

        List<SeatInfo> result = new ArrayList<>();
        for (int i = 0; i < Math.min(names.size(), remains.size()); i++) {
            result.add(new SeatInfo(names.get(i), remains.get(i)));
        }
        return result;
    }
}