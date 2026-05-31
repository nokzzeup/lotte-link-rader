import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class WishCalendarPanel extends JPanel {

    private static final Color NAVY     = new Color(4, 30, 66);
    private static final Color RED      = new Color(208, 15, 49);
    private static final Color WHITE    = Color.WHITE;
    private static final Color BG       = new Color(240, 242, 245);
    private static final Color GRAY     = new Color(156, 163, 175);
    private static final Color LIGHT_BG = new Color(243, 244, 246);

    private static final String EVENT_FILE     = "events.txt";
    private static final String ADMIN_PASSWORD = "admin1234";

    private int currentYear;
    private int currentMonth;
    private JPanel calendarGrid;
    private JLabel monthLabel;
    private JPanel wishListPanel;
    private JLabel wishCountLabel;
    private JLabel statTitle;
    private JLabel[] statLabels = new JLabel[4];

    // 이벤트 경기 날짜 목록 (파일에서 로드)
    private final Set<String> eventDates = new HashSet<>();

    public WishCalendarPanel() {
        Calendar cal = Calendar.getInstance();
        currentYear  = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH);

        loadEventDates();

        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel right = createRightPanel();
        JPanel left  = createLeftPanel();

        add(left, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }

    // ─── 이벤트 날짜 파일 로드/저장 ──────────────────────────
    private void loadEventDates() {
        eventDates.clear();
        eventDates.add("20260524"); // 기본 이벤트 날짜
        File f = new File(EVENT_FILE);
        if (!f.exists()) return;
        try {
            BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) eventDates.add(line);
            }
            br.close();
        } catch (Exception e) {
            System.out.println("이벤트 로드 실패: " + e.getMessage());
        }
    }

    private void saveEventDates() {
        try {
            PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(EVENT_FILE), StandardCharsets.UTF_8));
            for (String d : eventDates) pw.println(d);
            pw.close();
        } catch (Exception e) {
            System.out.println("이벤트 저장 실패: " + e.getMessage());
        }
    }

    private boolean isEventGame(String dateKey) {
        return eventDates.contains(dateKey);
    }

    // ─── 좌측: 캘린더 ─────────────────────────────────────────
    private JPanel createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(BG);
        left.setBorder(new EmptyBorder(0, 0, 0, 12));

        JPanel calCard = new JPanel(new BorderLayout());
        calCard.setBackground(WHITE);
        calCard.setBorder(new LineBorder(new Color(229, 231, 235), 1, true));
        calCard.setPreferredSize(new Dimension(0, 500));

        // 캘린더 헤더
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(12, 18, 12, 18)));

        monthLabel = new JLabel("", SwingConstants.LEFT);
        monthLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        monthLabel.setForeground(new Color(17, 24, 39));
        header.add(monthLabel, BorderLayout.WEST);

        JPanel navBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        navBtns.setBackground(WHITE);

        JButton prevBtn = createNavBtn("‹");
        JButton nextBtn = createNavBtn("›");

        prevBtn.addActionListener(e -> {
            currentMonth--;
            if (currentMonth < 0) { currentMonth = 11; currentYear--; }
            refreshCalendar();
        });
        nextBtn.addActionListener(e -> {
            currentMonth++;
            if (currentMonth > 11) { currentMonth = 0; currentYear++; }
            refreshCalendar();
        });

        navBtns.add(prevBtn);
        navBtns.add(nextBtn);
        header.add(navBtns, BorderLayout.EAST);
        calCard.add(header, BorderLayout.NORTH);

        // 요일 헤더
        JPanel dowPanel = new JPanel(new GridLayout(1, 7));
        dowPanel.setBackground(WHITE);
        dowPanel.setBorder(new EmptyBorder(8, 12, 4, 12));
        String[] dows = {"일", "월", "화", "수", "목", "금", "토"};
        Color[] dowColors = {RED, GRAY, GRAY, GRAY, GRAY, GRAY, new Color(59, 130, 246)};
        for (int i = 0; i < 7; i++) {
            JLabel dl = new JLabel(dows[i], SwingConstants.CENTER);
            dl.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            dl.setForeground(dowColors[i]);
            dowPanel.add(dl);
        }

        calendarGrid = new JPanel(new GridLayout(0, 7, 3, 3));calendarGrid = new JPanel(new GridLayout(0, 7, 3, 3));
        calendarGrid.setBackground(LIGHT_BG);
        calendarGrid.setBorder(new EmptyBorder(4, 12, 12, 12));

        JPanel calBody = new JPanel(new BorderLayout());
        calBody.setBackground(WHITE);
        calBody.add(dowPanel, BorderLayout.NORTH);
        calBody.add(calendarGrid, BorderLayout.CENTER);
        calCard.add(calBody, BorderLayout.CENTER);

        // 범례
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        legend.setBackground(WHITE);
        legend.setBorder(new MatteBorder(1, 0, 0, 0, new Color(243, 244, 246)));
        addLegend(legend, "★ 찜",   RED,                    WHITE);
        addLegend(legend, "홈",     new Color(232, 237, 245), NAVY);
        addLegend(legend, "원정",   new Color(243, 244, 246), GRAY);
        addLegend(legend, "이벤트", new Color(253, 244, 255), new Color(126, 34, 206));
        addLegend(legend, "예매중", new Color(236, 253, 245), new Color(6, 95, 70));
        calCard.add(legend, BorderLayout.SOUTH);

        left.add(calCard, BorderLayout.CENTER);
        refreshCalendar();
        return left;
    }

    // ─── 캘린더 갱신 ──────────────────────────────────────────
    private void refreshCalendar() {
        String[] months = {"1월","2월","3월","4월","5월","6월","7월","8월","9월","10월","11월","12월"};
        monthLabel.setText(currentYear + "년 " + months[currentMonth]);

        calendarGrid.removeAll();

        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth, 1);
        int startDow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int lastDay  = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Map<String, String[]> gameMap = new HashMap<>();
        for (String[] g : GameSchedule.getAllGames()) {
            String ym = g[0].substring(0, 6);
            if (ym.equals(String.format("%04d%02d", currentYear, currentMonth + 1))) {
                gameMap.put(g[0].substring(6, 8), g);
            }
        }

        for (int i = 0; i < startDow; i++) {
            JPanel empty = new JPanel();
            empty.setBackground(LIGHT_BG);
            calendarGrid.add(empty);
        }

        Calendar today = Calendar.getInstance();
        for (int d = 1; d <= lastDay; d++) {
            String dayStr  = String.format("%02d", d);
            String[] game  = gameMap.get(dayStr);
            String dateKey = String.format("%04d%02d%02d", currentYear, currentMonth + 1, d);
            boolean isWished = WishManager.isWished(dateKey);
            boolean isToday  = (currentYear == today.get(Calendar.YEAR) &&
                                currentMonth == today.get(Calendar.MONTH) &&
                                d == today.get(Calendar.DAY_OF_MONTH));
            calendarGrid.add(createDayCell(d, dayStr, game, isWished, isToday, dateKey));
        }

        int total     = startDow + lastDay;
        int remainder = total % 7 == 0 ? 0 : 7 - (total % 7);
        for (int i = 0; i < remainder; i++) {
            JPanel empty = new JPanel();
            empty.setBackground(LIGHT_BG);
            calendarGrid.add(empty);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
        refreshWishList();
        refreshStats();
    }

    // ─── 날짜 셀 ──────────────────────────────────────────────
    private JPanel createDayCell(int day, String dayStr, String[] game, boolean isWished, boolean isToday, String dateKey) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBackground(isToday ? new Color(232, 237, 245) : WHITE);
        cell.setBorder(new EmptyBorder(4, 4, 4, 4));

        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth, day);
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        Color dayColor = dow == 0 ? RED : (dow == 6 ? new Color(59, 130, 246) : new Color(55, 65, 81));
        if (isToday) dayColor = NAVY;

        JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.CENTER);
        dayLabel.setFont(new Font("맑은 고딕", isToday ? Font.BOLD : Font.PLAIN, 12));
        dayLabel.setForeground(dayColor);
        dayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cell.add(dayLabel);

        if (game != null) {
            boolean isAway   = game.length >= 5 && game[4].equals("원정");
            boolean hasCD    = !game[3].isEmpty(); // 예매 가능 여부
            String  opponent = game[2];

            Color  pillBg, pillFg;
            String pillText;

            if (isWished) {
                pillBg   = RED;
                pillFg   = WHITE;
                pillText = "★ " + (isAway ? "" : "홈 ") + "vs " + opponent;
            } else if (isAway) {
                pillBg   = new Color(243, 244, 246);
                pillFg   = GRAY;
                pillText = "원정 vs " + opponent;
            } else {
                pillBg   = new Color(232, 237, 245);
                pillFg   = NAVY;
                pillText = "홈 vs " + opponent;
            }

            JLabel pill = new JLabel(pillText, SwingConstants.CENTER);
            pill.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
            pill.setForeground(pillFg);
            pill.setBackground(pillBg);
            pill.setOpaque(true);
            pill.setBorder(new EmptyBorder(1, 3, 1, 3));
            pill.setAlignmentX(Component.CENTER_ALIGNMENT);
            pill.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
            cell.add(Box.createVerticalStrut(2));
            cell.add(pill);

            // 예매중 태그
            if (!isAway && hasCD) {
                JLabel bookPill = new JLabel("예매중", SwingConstants.CENTER);
                bookPill.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
                bookPill.setForeground(new Color(6, 95, 70));
                bookPill.setBackground(new Color(236, 253, 245));
                bookPill.setOpaque(true);
                bookPill.setBorder(new EmptyBorder(1, 3, 1, 3));
                bookPill.setAlignmentX(Component.CENTER_ALIGNMENT);
                bookPill.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
                cell.add(Box.createVerticalStrut(1));
                cell.add(bookPill);
            }

            // 이벤트 태그
            if (isEventGame(dateKey)) {
                JLabel evPill = new JLabel("이벤트", SwingConstants.CENTER);
                evPill.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
                evPill.setForeground(new Color(126, 34, 206));
                evPill.setBackground(new Color(253, 244, 255));
                evPill.setOpaque(true);
                evPill.setBorder(new EmptyBorder(1, 3, 1, 3));
                evPill.setAlignmentX(Component.CENTER_ALIGNMENT);
                evPill.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
                cell.add(Box.createVerticalStrut(1));
                cell.add(evPill);
            }

            String[] finalGame = game;
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { showGamePopup(cell, finalGame, dateKey); }
                @Override public void mouseEntered(MouseEvent e) { cell.setBackground(new Color(248, 249, 250)); }
                @Override public void mouseExited(MouseEvent e)  { cell.setBackground(isToday ? new Color(232,237,245) : WHITE); }
            });
        }

        return cell;
    }

    // ─── 팝업 ─────────────────────────────────────────────────
    private void showGamePopup(JPanel anchor, String[] game, String dateKey) {
        boolean isWished = WishManager.isWished(dateKey);
        boolean isAway   = game.length >= 5 && game[4].equals("원정");
        boolean hasCD    = !game[3].isEmpty();
        boolean isEvent  = isEventGame(dateKey);

        String month = dateKey.substring(4, 6);
        String day   = dateKey.substring(6, 8);

        // 요일 계산
        String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(dateKey.substring(0,4)),
                Integer.parseInt(dateKey.substring(4,6))-1,
                Integer.parseInt(dateKey.substring(6,8)));
        String dow = days[cal.get(Calendar.DAY_OF_WEEK)-1];

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "", true);
        dialog.setUndecorated(true);
        dialog.setSize(300, isAway ? 200 : (hasCD ? 240 : 210));
        dialog.setLocationRelativeTo(anchor);
        dialog.setLayout(new BorderLayout());

        // ─── 메인 패널 ────────────────────────────────────────
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(WHITE);
        main.setBorder(new CompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(20, 20, 16, 20)));

        // 헤더 (경기 정보 + X 버튼)
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(WHITE);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel header = new JLabel("경기 정보");
        header.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        header.setForeground(GRAY);

        JButton closeBtn = new JButton("X");
        closeBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        closeBtn.setForeground(GRAY);
        closeBtn.setBackground(WHITE);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dialog.dispose());

        headerRow.add(header, BorderLayout.WEST);
        headerRow.add(closeBtn, BorderLayout.EAST);

        // 날짜
        JLabel dateLabel = new JLabel(
                Integer.parseInt(month) + "월 " + Integer.parseInt(day) + "일 (" + dow + ")  " +
                "롯데 vs " + game[2]);
        dateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        dateLabel.setForeground(new Color(17, 24, 39));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 시간 & 장소
        String venue = isAway ? "원정 경기" : "사직야구장";
        JLabel timeLabel = new JLabel(game[1].substring(0,2) + ":" + game[1].substring(2) + "   " + venue);
        timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        timeLabel.setForeground(GRAY);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 이벤트 배지
        if (isEvent) {
            JLabel evBadge = new JLabel("이벤트 경기");
            evBadge.setFont(new Font("맑은 고딕", Font.BOLD, 10));
            evBadge.setForeground(new Color(126, 34, 206));
            evBadge.setBackground(new Color(253, 244, 255));
            evBadge.setOpaque(true);
            evBadge.setBorder(new EmptyBorder(2, 8, 2, 8));
            evBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
            main.add(headerRow);
            main.add(Box.createVerticalStrut(10));
            main.add(dateLabel);
            main.add(Box.createVerticalStrut(4));
            main.add(timeLabel);
            main.add(Box.createVerticalStrut(6));
            main.add(evBadge);
        } else {
            main.add(headerRow);
            main.add(Box.createVerticalStrut(10));
            main.add(dateLabel);
            main.add(Box.createVerticalStrut(4));
            main.add(timeLabel);
        }

        main.add(Box.createVerticalStrut(16));

        // ─── 찜하기 버튼 ─────────────────────────────────────
        JButton wishBtn = new JButton(isWished ? "♥  찜 해제" : "♥  찜하기");
        wishBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        wishBtn.setForeground(WHITE);
        wishBtn.setBackground(isWished ? GRAY : RED);
        wishBtn.setOpaque(true);
        wishBtn.setBorderPainted(false);
        wishBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        wishBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        wishBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        wishBtn.addActionListener(e -> {
            if (isWished) WishManager.remove(dateKey);
            else          WishManager.add(game);
            refreshCalendar();
            dialog.dispose();
        });
        main.add(wishBtn);

        // ─── 예매하기 버튼 ────────────────────────────────────
        if (!isAway && hasCD) {
            main.add(Box.createVerticalStrut(8));
            JButton bookBtn = new JButton("예매하기 →");
            bookBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            bookBtn.setForeground(NAVY);
            bookBtn.setBackground(WHITE);
            bookBtn.setOpaque(true);
            bookBtn.setBorder(new LineBorder(new Color(229, 231, 235)));
            bookBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            bookBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            bookBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            bookBtn.addActionListener(e -> {
                try { Desktop.getDesktop().browse(new java.net.URI("https://ticket.giantsclub.com")); }
                catch (Exception ex) { JOptionPane.showMessageDialog(null, "브라우저를 열 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE); }
                dialog.dispose();
            });
            main.add(bookBtn);
        }

        dialog.add(main, BorderLayout.CENTER);

        // 다이얼로그 바깥 클릭 시 닫기
        dialog.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dialog.dispose(); }
        });

        dialog.setVisible(true);
    }

    // ─── 이벤트 관리 다이얼로그 ──────────────────────────────

    // ─── 우측: 찜 목록 + 통계 ────────────────────────────────
    private JPanel createRightPanel() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(BG);
        right.setPreferredSize(new Dimension(260, 0));

        JPanel wishCard = new JPanel(new BorderLayout());
        wishCard.setBackground(WHITE);
        wishCard.setBorder(new LineBorder(new Color(229, 231, 235), 1, true));
        wishCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JPanel wishHeader = new JPanel(new BorderLayout());
        wishHeader.setBackground(WHITE);
        wishHeader.setBorder(new CompoundBorder(
                new MatteBorder(0,0,1,0,new Color(229,231,235)),
                new EmptyBorder(10,14,10,14)));

        JLabel wishTitleLabel = new JLabel("♥  찜한 경기", SwingConstants.LEFT);
        wishTitleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        wishTitleLabel.setForeground(new Color(55, 65, 81));
        wishHeader.add(wishTitleLabel, BorderLayout.WEST);

        wishCountLabel = new JLabel("0경기", SwingConstants.RIGHT);
        wishCountLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        wishCountLabel.setForeground(GRAY);
        wishHeader.add(wishCountLabel, BorderLayout.EAST);

        wishCard.add(wishHeader, BorderLayout.NORTH);

        wishListPanel = new JPanel();
        wishListPanel.setLayout(new BoxLayout(wishListPanel, BoxLayout.Y_AXIS));
        wishListPanel.setBackground(WHITE);

        JScrollPane wishScroll = new JScrollPane(wishListPanel);
        wishScroll.setBorder(null);
        wishScroll.getVerticalScrollBar().setUnitIncrement(10);
        wishCard.add(wishScroll, BorderLayout.CENTER);

        right.add(wishCard);
        right.add(Box.createVerticalStrut(10));

        JPanel statCard = new JPanel(new BorderLayout());
        statCard.setBackground(WHITE);
        statCard.setBorder(new LineBorder(new Color(229,231,235),1,true));
        statCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JPanel statHeader = new JPanel(new BorderLayout());
        statHeader.setBackground(WHITE);
        statHeader.setBorder(new CompoundBorder(
                new MatteBorder(0,0,1,0,new Color(229,231,235)),
                new EmptyBorder(10,14,10,14)));

        statTitle = new JLabel(currentMonth+1 + "월 요약");
        statTitle.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        statTitle.setForeground(new Color(55,65,81));
        statHeader.add(statTitle, BorderLayout.WEST);
        statCard.add(statHeader, BorderLayout.NORTH);

        JPanel statGrid = new JPanel(new GridLayout(2, 2, 6, 6));
        statGrid.setBackground(WHITE);
        statGrid.setBorder(new EmptyBorder(10, 12, 10, 12));

        String[] statNames  = {"홈경기", "찜한 경기", "이벤트", "원정"};
        Color[]  statColors = {NAVY, RED, new Color(126,34,206), new Color(55,65,81)};
        for (int i = 0; i < 4; i++) {
            JPanel si = new JPanel();
            si.setLayout(new BoxLayout(si, BoxLayout.Y_AXIS));
            si.setBackground(new Color(249,250,251));
            si.setBorder(new EmptyBorder(8, 10, 8, 10));
            JLabel sl = new JLabel(statNames[i]);
            sl.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
            sl.setForeground(GRAY);
            statLabels[i] = new JLabel("0경기");
            statLabels[i].setFont(new Font("맑은 고딕", Font.BOLD, 16));
            statLabels[i].setForeground(statColors[i]);
            si.add(sl);
            si.add(statLabels[i]);
            statGrid.add(si);
        }
        statCard.add(statGrid, BorderLayout.CENTER);
        right.add(statCard);

        return right;
    }

    // ─── 찜 목록 갱신 ─────────────────────────────────────────
    private void refreshWishList() {
        wishListPanel.removeAll();
        List<String[]> wishes = WishManager.getAll();

        if (wishes.isEmpty()) {
            JLabel empty = new JLabel("찜한 경기가 없습니다", SwingConstants.CENTER);
            empty.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            empty.setForeground(GRAY);
            empty.setBorder(new EmptyBorder(20, 0, 20, 0));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            wishListPanel.add(empty);
        } else {
            for (String[] w : wishes) wishListPanel.add(createWishItem(w));
        }

        wishCountLabel.setText(wishes.size() + "경기");
        wishListPanel.revalidate();
        wishListPanel.repaint();
    }

    private JPanel createWishItem(String[] game) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(WHITE);
        item.setBorder(new CompoundBorder(
                new MatteBorder(0,0,1,0,new Color(249,250,251)),
                new EmptyBorder(9,14,9,14)));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JPanel dateBox = new JPanel();
        dateBox.setLayout(new BoxLayout(dateBox, BoxLayout.Y_AXIS));
        dateBox.setBackground(WHITE);
        dateBox.setPreferredSize(new Dimension(36, 0));

        JLabel dayL = new JLabel(game[0].substring(6, 8), SwingConstants.CENTER);
        dayL.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        dayL.setForeground(new Color(17,24,39));
        dayL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel moL = new JLabel(game[0].substring(4,6)+"월", SwingConstants.CENTER);
        moL.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
        moL.setForeground(GRAY);
        moL.setAlignmentX(Component.CENTER_ALIGNMENT);

        dateBox.add(dayL);
        dateBox.add(moL);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(WHITE);
        info.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel team = new JLabel("롯데 vs " + game[2]);
        team.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        team.setForeground(new Color(17,24,39));

        JLabel sub = new JLabel(game[1].substring(0,2)+":"+game[1].substring(2) + " · 사직야구장");
        sub.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        sub.setForeground(GRAY);

        info.add(team);
        info.add(sub);

        JButton delBtn = new JButton("X");
        delBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        delBtn.setForeground(GRAY);
        delBtn.setBackground(WHITE);
        delBtn.setBorderPainted(false);
        delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        delBtn.addActionListener(e -> {
            WishManager.remove(game[0]);
            refreshCalendar();
        });

        item.add(dateBox, BorderLayout.WEST);
        item.add(info, BorderLayout.CENTER);
        item.add(delBtn, BorderLayout.EAST);
        return item;
    }

    // ─── 통계 갱신 ────────────────────────────────────────────
    private void refreshStats() {
        if (statTitle != null) statTitle.setText(currentMonth+1 + "월 요약");

        String ym = String.format("%04d%02d", currentYear, currentMonth + 1);
        int home = 0, wished = 0, event = 0, away = 0;

        for (String[] g : GameSchedule.getAllGames()) {
            if (!g[0].startsWith(ym)) continue;
            boolean isAway = g.length >= 5 && g[4].equals("원정");
            if (isAway) away++;
            else        home++;
            if (WishManager.isWished(g[0])) wished++;
            if (isEventGame(g[0]))          event++;
        }

        statLabels[0].setText(home   + "경기");
        statLabels[1].setText(wished + "경기");
        statLabels[2].setText(event  + "경기");
        statLabels[3].setText(away   + "경기");
    }

    // ─── 유틸 ─────────────────────────────────────────────────
    private JButton createNavBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        btn.setForeground(new Color(107,114,128));
        btn.setBackground(WHITE);
        btn.setBorder(new LineBorder(new Color(229,231,235), 1, true));
        btn.setPreferredSize(new Dimension(28, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void addLegend(JPanel p, String text, Color bg, Color fg) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setBackground(WHITE);
        JLabel pill = new JLabel(text);
        pill.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        pill.setForeground(fg);
        pill.setBackground(bg);
        pill.setOpaque(true);
        pill.setBorder(new EmptyBorder(1, 6, 1, 6));
        item.add(pill);
        p.add(item);
    }
}
