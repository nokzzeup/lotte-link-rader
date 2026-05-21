import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainFrame extends JFrame {

    // ─── 색상 상수 ───────────────────────────────────────────
    private static final Color NAVY     = new Color(4, 30, 66);
    private static final Color RED      = new Color(208, 15, 49);
    private static final Color BG       = new Color(240, 242, 245);
    private static final Color WHITE    = Color.WHITE;
    private static final Color GRAY     = new Color(156, 163, 175);
    private static final Color LIGHT_BG = new Color(232, 237, 245);
    private static final Color DARK_BG  = new Color(2, 14, 34);

    // ─── 필드 ────────────────────────────────────────────────
    private JScrollPane seatScroll;       // 잔여석 표시 스크롤 영역
    private JLabel lastUpdateLabel;       // 마지막 갱신 시간 표시
    private Timer autoRefreshTimer;       // 선택 경기 15초 자동 갱신 타이머
    private Timer badgeRefreshTimer;      // 전체 경기 배지 15초 갱신 타이머
    private String[] selectedGame = null; // 현재 선택된 경기
    private JPanel selectedItem   = null; // 현재 선택된 경기 아이템 패널
    private JButton wishBtn;              // 찜하기 버튼
    private JPanel bodyPanel;             // 탭 전환 시 교체되는 메인 바디 패널

    // 날짜(key) → 배지 라벨(value) 매핑: 매진/예매중 배지 실시간 업데이트용
    private final Map<String, JLabel> badgeMap = new HashMap<>();

    // ─── 생성자 ──────────────────────────────────────────────
    public MainFrame() {
        setTitle("Lotte-Link Radar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setLocationRelativeTo(null); // 화면 정중앙 배치
        setBackground(BG);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);

        // 탭 전환 시 bodyPanel 내용물만 교체하는 구조
        bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(BG);
        bodyPanel.add(createLeftPanel(), BorderLayout.WEST);
        bodyPanel.add(createRightPanel(), BorderLayout.CENTER);
        add(bodyPanel, BorderLayout.CENTER);

        setVisible(true);
        startBadgeRefresh(); // 앱 시작 시 배지 갱신 시작
    }

    // ─── 배지 갱신 ───────────────────────────────────────────
    // 로딩 시 캐시된 데이터로 즉시 배지 반영 후 15초마다 서버에서 재조회
    private void startBadgeRefresh() {
        // 캐시 데이터로 즉시 반영
        SwingUtilities.invokeLater(() -> {
            for (String[] game : GameSchedule.getUpcomingGames(30)) {
                JLabel badge = badgeMap.get(game[0]);
                if (badge == null || game[3].isEmpty()) continue;
                if (SeatDataCache.hasData(game[0]) && SeatDataCache.isSoldOut(game[0])) {
                    setSoldOut(badge);
                }
            }
        });

        // 15초마다 전체 배지 갱신
        badgeRefreshTimer = new Timer();
        badgeRefreshTimer.schedule(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(() -> refreshAllBadges());
            }
        }, 15000, 15000);
    }

    // 각 경기 잔여석을 별도 Thread로 조회해서 배지 업데이트
    private void refreshAllBadges() {
        for (String[] game : GameSchedule.getUpcomingGames(30)) {
            String date = game[0], time = game[1], cd = game[3];
            JLabel badge = badgeMap.get(date);
            if (badge == null || cd.isEmpty()) continue;

            // 별도 Thread에서 조회 (UI 스레드 블로킹 방지)
            new Thread(() -> {
                List<SeatInfo> seats = SeatEngine.fetch(date, time, cd);
                SwingUtilities.invokeLater(() -> {
                    if (seats == null) return;
                    // 휠체어석 제외 전부 매진이면 매진 배지
                    boolean sold = seats.stream()
                            .filter(s -> !s.getName().contains("휠체어"))
                            .allMatch(s -> s.getRemain() == 0);
                    if (sold) setSoldOut(badge); else setOnSale(badge);
                });
            }).start();
        }
    }

    private void setSoldOut(JLabel b) {
        b.setText("매진");
        b.setBackground(new Color(254, 242, 242));
        b.setForeground(new Color(153, 27, 27));
    }

    private void setOnSale(JLabel b) {
        b.setText("예매중");
        b.setBackground(new Color(236, 253, 245));
        b.setForeground(new Color(6, 95, 70));
    }

    // ─── 헤더 ────────────────────────────────────────────────
    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(NAVY);

        // 로고 + 유저명
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(NAVY);
        topBar.setBorder(new EmptyBorder(11, 24, 11, 24));

        JLabel logo = new JLabel("Lotte-Link Radar");
        logo.setForeground(WHITE);
        logo.setFont(new Font("맑은 고딕", Font.BOLD, 17));
        topBar.add(logo, BorderLayout.WEST);

        JLabel user = new JLabel("smin1115");
        user.setForeground(GRAY);
        user.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        topBar.add(user, BorderLayout.EAST);

        // 공지 티커 (공지 내용을 한 줄로 표시)
        JPanel ticker = new JPanel(new BorderLayout());
        ticker.setBackground(DARK_BG);
        ticker.setBorder(new EmptyBorder(6, 24, 6, 24));

        JLabel tag = new JLabel("  공지  ");
        tag.setBackground(RED);
        tag.setForeground(WHITE);
        tag.setOpaque(true);
        tag.setFont(new Font("맑은 고딕", Font.BOLD, 10));
        ticker.add(tag, BorderLayout.WEST);

        JLabel txt = new JLabel("  5/24 삼성전 — 클래식 유니폼 배포 이벤트 (선착순 3,000명)  |  5/26~28 LG전 예매 5/13 오후 2시 오픈 예정  |  5/12 NC전 3루 내야필드석A 취소표 7석 발생");
        txt.setForeground(new Color(180, 180, 180));
        txt.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        ticker.add(txt, BorderLayout.CENTER);

        // 상단 탭 메뉴 (경기일정 / 내찜목록 / 직관기록)
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        nav.setBackground(NAVY);
        nav.setBorder(new EmptyBorder(0, 20, 0, 0));

        String[] tabs = {"경기 일정", "내 찜 목록", "직관 기록"};
        JLabel[] tabLabels = new JLabel[tabs.length];
        JPanel[] tabWraps  = new JPanel[tabs.length];

        for (int i = 0; i < tabs.length; i++) {
            JLabel tab = new JLabel(tabs[i]);
            tab.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            tab.setBorder(new EmptyBorder(9, 16, 9, 0));
            tab.setForeground(i == 0 ? WHITE : GRAY);
            tabLabels[i] = tab;

            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setBackground(NAVY);
            wrap.add(tab, BorderLayout.CENTER);
            if (i == 0) wrap.setBorder(new MatteBorder(0, 0, 2, 0, RED));
            tabWraps[i] = wrap;
            nav.add(wrap);

            final int idx = i;
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tab.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    // 클릭된 탭 활성화, 나머지 비활성화
                    for (int j = 0; j < tabs.length; j++) {
                        tabLabels[j].setForeground(j == idx ? WHITE : GRAY);
                        tabWraps[j].setBorder(j == idx
                                ? new MatteBorder(0, 0, 2, 0, RED)
                                : new EmptyBorder(0, 0, 2, 0));
                    }

                    // 탭에 따라 bodyPanel 내용 교체
                    bodyPanel.removeAll();
                    if (idx == 0) {
                        // 경기 일정: 좌측 경기목록 + 우측 잔여석
                        bodyPanel.add(createLeftPanel(), BorderLayout.WEST);
                        bodyPanel.add(createRightPanel(), BorderLayout.CENTER);
                        startBadgeRefresh();
                    } else if (idx == 1) {
                        // 내 찜 목록: 캘린더 전체화면
                        bodyPanel.add(new WishCalendarPanel(), BorderLayout.CENTER);
                    } else {
                        // 직관 기록: 준비 중
                        JPanel coming = new JPanel(new GridBagLayout());
                        coming.setBackground(BG);
                        JLabel l = new JLabel("준비 중입니다");
                        l.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
                        l.setForeground(GRAY);
                        coming.add(l);
                        bodyPanel.add(coming, BorderLayout.CENTER);
                    }
                    bodyPanel.revalidate();
                    bodyPanel.repaint();
                }
            });

            if (i < tabs.length - 1) nav.add(new JLabel("   "));
        }

        header.add(topBar);
        header.add(ticker);
        header.add(nav);
        return header;
    }

    // ─── 좌측 패널 ───────────────────────────────────────────
    // 경기일정 탭과 공지사항 탭으로 구성된 좌측 패널
    private JPanel createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(WHITE);
        left.setPreferredSize(new Dimension(280, 0));
        left.setBorder(new MatteBorder(0, 0, 0, 1, new Color(229, 231, 235)));

        // 좌측 내부 탭 (경기일정 / 공지사항)
        JPanel tabBar = new JPanel(new GridLayout(1, 2));
        tabBar.setBackground(WHITE);
        tabBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        JLabel t1 = new JLabel("경기 일정", SwingConstants.CENTER);
        t1.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        t1.setForeground(NAVY);
        t1.setBorder(new MatteBorder(0, 0, 2, 0, NAVY));
        t1.setPreferredSize(new Dimension(0, 42));

        JLabel t2 = new JLabel("공지사항", SwingConstants.CENTER);
        t2.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        t2.setForeground(GRAY);
        t2.setBorder(new EmptyBorder(0, 0, 2, 0)); // 초기 빈 테두리 (빨간 선 방지)

        tabBar.add(t1);
        tabBar.add(t2);
        left.add(tabBar, BorderLayout.NORTH);

        // 경기일정 탭 클릭: 경기 목록으로 복귀
        t1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        t1.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                t1.setBorder(new MatteBorder(0, 0, 2, 0, NAVY));
                t1.setForeground(NAVY);
                t1.setFont(new Font("맑은 고딕", Font.BOLD, 12));
                t2.setBorder(new EmptyBorder(0, 0, 2, 0));
                t2.setForeground(GRAY);
                t2.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

                // 좌측 + 우측 모두 경기일정으로 교체
                bodyPanel.removeAll();
                bodyPanel.add(createLeftPanel(), BorderLayout.WEST);
                bodyPanel.add(createRightPanel(), BorderLayout.CENTER);
                startBadgeRefresh();
                bodyPanel.revalidate();
                bodyPanel.repaint();
            }
        });

        // 공지사항 탭 클릭: 좌측 탭 유지 + 우측을 NoticePanel로 교체
        t2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        t2.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                t1.setBorder(new EmptyBorder(0, 0, 2, 0));
                t1.setForeground(GRAY);
                t1.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                t2.setBorder(new MatteBorder(0, 0, 2, 0, NAVY));
                t2.setForeground(NAVY);
                t2.setFont(new Font("맑은 고딕", Font.BOLD, 12));

                // 좌측 탭 패널은 유지, 우측만 NoticePanel로 교체
                bodyPanel.removeAll();
                bodyPanel.add(createLeftTabOnly(t1, t2), BorderLayout.WEST);
                bodyPanel.add(new NoticePanel(), BorderLayout.CENTER);
                bodyPanel.revalidate();
                bodyPanel.repaint();
            }
        });

        // 경기 목록 (예매중 / 원정 / 오픈예정 분류)
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(WHITE);

        List<String[]> all = GameSchedule.getUpcomingGames(50);
        List<String[]> open = new ArrayList<>(), away = new ArrayList<>(), soon = new ArrayList<>();
        for (String[] g : all) {
            boolean isAway = g.length >= 5 && g[4].equals("원정");
            if (!isAway && !g[3].isEmpty()) open.add(g);
            else if (away.size() + soon.size() < 6) {
                if (isAway) away.add(g);
                else soon.add(g);
            }
        }

        if (!open.isEmpty()) {
            list.add(createSectionLabel("예매 진행중"));
            for (String[] g : open) list.add(createGameItem(g, "예매중"));
        }
        if (!away.isEmpty()) {
            list.add(createDivider());
            list.add(createSectionLabel("원정 경기"));
            for (String[] g : away) list.add(createGameItem(g, "원정"));
        }
        if (!soon.isEmpty()) {
            list.add(createDivider());
            list.add(createSectionLabel("예매 오픈 예정"));
            for (String[] g : soon) list.add(createGameItem(g, "오픈예정"));
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        left.add(scroll, BorderLayout.CENTER);
        return left;
    }

    // 공지사항 탭 선택 시 좌측에 탭만 있는 패널 생성
    // (경기 목록 없이 탭 헤더만 유지)
    private JPanel createLeftTabOnly(JLabel t1, JLabel t2) {
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(WHITE);
        left.setPreferredSize(new Dimension(280, 0));
        left.setBorder(new MatteBorder(0, 0, 0, 1, new Color(229, 231, 235)));

        JPanel tabBar = new JPanel(new GridLayout(1, 2));
        tabBar.setBackground(WHITE);
        tabBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));
        tabBar.add(t1);
        tabBar.add(t2);
        left.add(tabBar, BorderLayout.NORTH);
        return left;
    }

    // ─── 경기 아이템 ─────────────────────────────────────────
    // 경기 하나를 표시하는 패널 생성 (클릭 시 잔여석 조회)
    private JPanel createGameItem(String[] game, String status) {
        String date = game[0], time = game[1], opp = game[2];
        boolean isAway = status.equals("원정");

        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(WHITE);
        item.setBorder(new CompoundBorder(
                new MatteBorder(0, 3, 0, 0, WHITE),
                new EmptyBorder(10, 14, 10, 14)));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        // 날짜 박스 (일, 요일)
        JPanel dateBox = new JPanel();
        dateBox.setLayout(new BoxLayout(dateBox, BoxLayout.Y_AXIS));
        dateBox.setBackground(WHITE);
        dateBox.setPreferredSize(new Dimension(34, 0));

        String[] days = {"일","월","화","수","목","금","토"};
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(date.substring(0,4)),
                Integer.parseInt(date.substring(4,6))-1,
                Integer.parseInt(date.substring(6,8)));
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;

        JLabel dayL = new JLabel(date.substring(6,8), SwingConstants.CENTER);
        dayL.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        dayL.setForeground(isAway ? new Color(196,201,208) : new Color(17,24,39));
        dayL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dowL = new JLabel(days[dow], SwingConstants.CENTER);
        dowL.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        dowL.setForeground(dow==0 ? RED : (dow==6 ? new Color(59,130,246) : GRAY));
        dowL.setAlignmentX(Component.CENTER_ALIGNMENT);

        dateBox.add(dayL);
        dateBox.add(dowL);

        // 경기 정보 (팀명, 시간, 배지)
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(WHITE);
        info.setBorder(new EmptyBorder(0, 12, 0, 0));

        JLabel team = new JLabel("롯데 vs " + opp);
        team.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        team.setForeground(isAway ? new Color(196,201,208) : new Color(17,24,39));

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        badges.setBackground(WHITE);

        JLabel timeL = new JLabel(time.substring(0,2)+":"+time.substring(2));
        timeL.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        timeL.setForeground(GRAY);
        badges.add(timeL);

        if (isAway) {
            badges.add(createBadge("원정", new Color(243,244,246), GRAY));
        } else {
            badges.add(createBadge("홈", new Color(232,237,245), NAVY));
            JLabel sb;
            if (status.equals("예매중")) {
                if (GameSchedule.isOnSiteOnly(date, time)) {
                    // 경기 시작 3시간 전 이후 → 현장예매 배지
                    sb = createBadge("현장예매", new Color(243,232,255), new Color(109,40,217));
                } else {
                    sb = createBadge("예매중", new Color(236,253,245), new Color(6,95,70));
                    badgeMap.put(date, sb); // 배지 갱신을 위해 Map에 저장
                }
            } else {
                sb = createBadge("오픈예정", new Color(255,251,235), new Color(146,64,14));
            }
            badges.add(sb);
        }

        info.add(team);
        info.add(badges);
        item.add(dateBox, BorderLayout.WEST);
        item.add(info, BorderLayout.CENTER);

        // wrapper로 감싸서 구분선 추가
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WHITE);
        wrapper.setBorder(new MatteBorder(0, 0, 1, 0, new Color(243,244,246)));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        wrapper.add(item);

        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (selectedItem != wrapper)
                    setItemBg(wrapper, item, info, badges, dateBox, new Color(249,250,251));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (selectedItem != wrapper)
                    setItemBg(wrapper, item, info, badges, dateBox, WHITE);
            }
            @Override public void mouseClicked(MouseEvent e) {
                // 이전 선택 항목 배경 초기화
                if (selectedItem != null && selectedItem != wrapper)
                    resetItemColor(selectedItem);

                // 현재 항목 선택 표시 (네이비 왼쪽 테두리 + 배경색)
                selectedItem = wrapper;
                wrapper.setBorder(new MatteBorder(0,0,1,0, new Color(229,231,235)));
                item.setBorder(new CompoundBorder(
                        new MatteBorder(0,3,0,0,NAVY), new EmptyBorder(10,14,10,14)));
                setAllBg(wrapper, LIGHT_BG);

                selectedGame = game;
                if (isAway) showAwayMessage(game);
                else if (GameSchedule.isOnSiteOnly(date, time)) showOnSiteMessage(game);
                else loadSeatData(game);
            }
        });

        return wrapper;
    }

    // 특정 패널과 자식 패널들의 배경색 변경
    private void setItemBg(JPanel wrapper, JPanel item, JPanel info, JPanel badges, JPanel dateBox, Color c) {
        wrapper.setBackground(c);
        item.setBackground(c);
        if (info    != null) info.setBackground(c);
        if (badges  != null) badges.setBackground(c);
        if (dateBox != null) dateBox.setBackground(c);
    }

    // 선택 해제 시 항목 배경/테두리 원래대로 복원
    private void resetItemColor(JPanel wrapper) {
        setAllBg(wrapper, WHITE);
        wrapper.setBorder(new MatteBorder(0,0,1,0, new Color(243,244,246)));
        if (wrapper.getComponentCount() > 0 && wrapper.getComponent(0) instanceof JPanel) {
            JPanel inner = (JPanel) wrapper.getComponent(0);
            inner.setBorder(new CompoundBorder(
                    new MatteBorder(0,3,0,0,WHITE), new EmptyBorder(10,14,10,14)));
        }
    }

    // 컨테이너와 모든 하위 컴포넌트 배경색 일괄 변경 (재귀)
    private void setAllBg(Container c, Color color) {
        c.setBackground(color);
        for (Component comp : c.getComponents()) {
            if (comp instanceof Container) setAllBg((Container) comp, color);
        }
    }

    // ─── 우측 패널 ───────────────────────────────────────────
    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG);
        right.setBorder(new EmptyBorder(16, 18, 16, 18));

        // 상단 카드 3개 (오늘경기 / 다음예매오픈 / 다음이벤트)
        JPanel topCards = new JPanel(new GridLayout(1, 3, 10, 0));
        topCards.setBackground(BG);
        topCards.setPreferredSize(new Dimension(0, 92));
        topCards.setBorder(new EmptyBorder(0, 0, 12, 0));
        topCards.add(createTodayCard());
        topCards.add(createCountdownCard("다음 예매 오픈", "D-13", "5/22 vs 삼성", false));
        topCards.add(createCountdownCard("다음 이벤트", "D-15", "5/24 클래식 유니폼", true));
        right.add(topCards, BorderLayout.NORTH);

        // 잔여석 표시 영역 (경기 선택 전 안내 문구)
        JPanel guide = new JPanel(new GridBagLayout());
        guide.setBackground(WHITE);
        JLabel gl = new JLabel("좌측에서 경기를 선택하세요");
        gl.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        gl.setForeground(GRAY);
        guide.add(gl);

        seatScroll = new JScrollPane(guide);
        seatScroll.setBorder(new LineBorder(new Color(229,231,235), 1));
        seatScroll.getVerticalScrollBar().setUnitIncrement(12);
        right.add(seatScroll, BorderLayout.CENTER);

        // 하단 바 (갱신 시간 + 찜하기 버튼)
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(WHITE);
        bottom.setBorder(new CompoundBorder(
                new LineBorder(new Color(229,231,235), 1),
                new EmptyBorder(9, 16, 9, 16)));

        lastUpdateLabel = new JLabel("경기를 선택하면 자동 갱신을 시작합니다");
        lastUpdateLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        lastUpdateLabel.setForeground(GRAY);
        bottom.add(lastUpdateLabel, BorderLayout.WEST);

        wishBtn = new JButton("♥  이 경기 찜하기");
        wishBtn.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        wishBtn.setForeground(NAVY);
        wishBtn.setBackground(WHITE);
        wishBtn.setBorder(new LineBorder(new Color(229,231,235)));
        wishBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 찜하기 버튼 클릭: 찜 추가/해제 토글
        wishBtn.addActionListener(e -> {
            if (selectedGame == null) {
                JOptionPane.showMessageDialog(this, "경기를 먼저 선택해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (WishManager.isWished(selectedGame[0])) {
                WishManager.remove(selectedGame[0]);
                wishBtn.setText("♥  이 경기 찜하기");
                wishBtn.setForeground(NAVY);
            } else {
                WishManager.add(selectedGame);
                wishBtn.setText("♥  찜 완료!");
                wishBtn.setForeground(RED);
            }
        });

        bottom.add(wishBtn, BorderLayout.EAST);
        right.add(bottom, BorderLayout.SOUTH);
        return right;
    }

    // ─── 안내 메시지 패널 ────────────────────────────────────
    // 원정/현장예매 선택 시 중앙에 안내 메시지 표시
    private void showAwayMessage(String[] game) {
        if (autoRefreshTimer != null) { autoRefreshTimer.cancel(); autoRefreshTimer = null; }
        String mo = game[0].substring(4,6), dy = game[0].substring(6,8);
        seatScroll.setViewportView(buildMessagePanel(
                mo+"/"+dy+" vs "+game[2], null,
                "✈", "원정 경기입니다", "원정 경기는 잔여석 정보를 제공하지 않습니다."));
        lastUpdateLabel.setText("원정 경기 · 잔여석 정보 없음");
    }

    private void showOnSiteMessage(String[] game) {
        if (autoRefreshTimer != null) { autoRefreshTimer.cancel(); autoRefreshTimer = null; }
        String mo = game[0].substring(4,6), dy = game[0].substring(6,8);
        seatScroll.setViewportView(buildMessagePanel(
                mo+"/"+dy+" vs "+game[2], null,
                "🎟", "현장 예매만 가능합니다", "경기 시작 3시간 전부터 온라인 예매가 마감됩니다."));
        lastUpdateLabel.setText("현장 예매 전환 · 온라인 예매 마감");
    }

    private JPanel buildMessagePanel(String title, String[] game, String icon, String msg1, String msg2) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(WHITE);
        p.add(createSeatHeader(title, game), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(WHITE);
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(WHITE);

        JLabel ic = new JLabel(icon, SwingConstants.CENTER);
        ic.setFont(new Font("맑은 고딕", Font.PLAIN, 36));
        ic.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l1 = new JLabel(msg1, SwingConstants.CENTER);
        l1.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        l1.setForeground(new Color(55,65,81));
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l2 = new JLabel(msg2, SwingConstants.CENTER);
        l2.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        l2.setForeground(GRAY);
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(ic);
        box.add(Box.createVerticalStrut(12));
        box.add(l1);
        box.add(Box.createVerticalStrut(6));
        box.add(l2);
        center.add(box);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    // ─── 잔여석 로드 & 갱신 ──────────────────────────────────
    // 경기 클릭 시 즉시 조회 후 15초마다 자동 갱신
    private void loadSeatData(String[] game) {
        if (autoRefreshTimer != null) autoRefreshTimer.cancel();
        updateSeatPanel(game);

        // 15초마다 자동 갱신 타이머 설정
        autoRefreshTimer = new Timer();
        autoRefreshTimer.schedule(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(() -> updateSeatPanel(game));
            }
        }, 15000, 15000);

        // 찜 여부에 따라 버튼 텍스트/색상 갱신
        if (WishManager.isWished(game[0])) {
            wishBtn.setText("♥  찜 완료!");
            wishBtn.setForeground(RED);
        } else {
            wishBtn.setText("♥  이 경기 찜하기");
            wishBtn.setForeground(NAVY);
        }
    }

    // 잔여석 패널 갱신 (서버에서 데이터 가져와서 표시)
    private void updateSeatPanel(String[] game) {
        String date=game[0], time=game[1], opp=game[2], cd=game[3];
        String mo=date.substring(4,6), dy=date.substring(6,8);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.add(createSeatHeader(mo+"/"+dy+" vs "+opp+" — 구역별 잔여석", game), BorderLayout.NORTH);

        if (cd.isEmpty()) {
            panel.add(centerMsg("아직 예매 기간이 아닙니다"), BorderLayout.CENTER);
        } else {
            List<SeatInfo> seats = SeatEngine.fetch(date, time, cd);
            if (seats == null || seats.isEmpty()) {
                panel.add(centerMsg("데이터를 불러올 수 없습니다"), BorderLayout.CENTER);
            } else {
                // 휠체어석 제외 전부 0석이면 매진 배지로 변경
                boolean sold = seats.stream()
                        .filter(s -> !s.getName().contains("휠체어"))
                        .allMatch(s -> s.getRemain() == 0);
                JLabel badge = badgeMap.get(date);
                if (badge != null) { if (sold) setSoldOut(badge); else setOnSale(badge); }

                JPanel seatList = new JPanel();
                seatList.setLayout(new BoxLayout(seatList, BoxLayout.Y_AXIS));
                seatList.setBackground(WHITE);
                for (SeatInfo s : seats) seatList.add(createSeatRow(s));
                seatList.add(createLegend());
                panel.add(seatList, BorderLayout.CENTER);
            }
        }

        seatScroll.setViewportView(panel);
        String now = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
        lastUpdateLabel.setText("마지막 갱신 " + now + " · 15초마다 자동 갱신");
    }

    // 잔여석 패널 헤더 (제목 + 새로고침 버튼)
    private JPanel createSeatHeader(String title, String[] game) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(WHITE);
        h.setBorder(new CompoundBorder(
                new MatteBorder(0,0,1,0,new Color(229,231,235)),
                new EmptyBorder(12,18,10,18)));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(WHITE);

        JLabel tl = new JLabel(title);
        tl.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        tl.setForeground(new Color(17,24,39));
        left.add(tl);

        if (game != null) {
            JLabel sub = new JLabel("마지막 갱신 확인 중 · 15초마다 자동 갱신");
            sub.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
            sub.setForeground(GRAY);
            left.add(sub);
        }
        h.add(left, BorderLayout.WEST);

        if (game != null) {
            JButton btn = new JButton("새로고침");
            btn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            btn.setForeground(new Color(107,114,128));
            btn.setBackground(WHITE);
            btn.setBorder(new LineBorder(new Color(229,231,235)));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> updateSeatPanel(game));
            h.add(btn, BorderLayout.EAST);
        }
        return h;
    }

    // 중앙 안내 메시지 패널
    private JPanel centerMsg(String text) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(WHITE);
        JLabel l = new JLabel(text);
        l.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        l.setForeground(GRAY);
        p.add(l);
        return p;
    }

    // ─── 좌석 행 ─────────────────────────────────────────────
    // 구역명, 잔여석 바 차트, 잔여석 숫자로 구성된 행
    private JPanel createSeatRow(SeatInfo seat) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(WHITE);
        row.setBorder(new CompoundBorder(
                new MatteBorder(0,0,1,0,new Color(249,250,251)),
                new EmptyBorder(9,18,9,18)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        // 구역 색상 점
        JPanel dot = new JPanel();
        dot.setBackground(getZoneColor(seat.getName()));
        dot.setPreferredSize(new Dimension(9,9));
        JPanel dotW = new JPanel(new FlowLayout(FlowLayout.LEFT,0,7));
        dotW.setBackground(WHITE);
        dotW.setPreferredSize(new Dimension(22,0));
        dotW.add(dot);

        JLabel name = new JLabel(seat.getName());
        name.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        name.setForeground(new Color(55,65,81));

        // 잔여석 바 차트 + 숫자
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setBackground(WHITE);

        int remain = seat.getRemain();
        Color barColor = remain == 0 ? new Color(229,231,235)
                       : remain < 30 ? new Color(251,191,36)
                       : new Color(52,211,153);

        JPanel barBg = new JPanel(null);
        barBg.setPreferredSize(new Dimension(80, 5));
        barBg.setBackground(new Color(243,244,246));

        // 최대 300석 기준으로 바 너비 계산
        int fillW = remain == 0 ? 0 : Math.min(80, (remain * 80 / 300));
        JPanel fill = new JPanel();
        fill.setBackground(barColor);
        fill.setBounds(0, 0, fillW, 5);
        barBg.add(fill);

        Color rc = remain == 0 ? new Color(209,213,219)
                 : remain < 30 ? new Color(217,119,6)
                 : new Color(5,150,105);
        String rt = remain == 0 ? "매진" : remain+"석";

        JLabel remL = new JLabel(rt);
        remL.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        remL.setForeground(rc);
        remL.setPreferredSize(new Dimension(44, 20));

        right.add(barBg);
        right.add(remL);

        row.add(dotW, BorderLayout.WEST);
        row.add(name, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    // ─── 범례 ────────────────────────────────────────────────
    private JPanel createLegend() {
        JPanel leg = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 7));
        leg.setBackground(WHITE);
        leg.setBorder(new MatteBorder(1,0,0,0,new Color(243,244,246)));
        leg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        addLegendItem(leg, "매진",     new Color(229,231,235));
        addLegendItem(leg, "30석 미만", new Color(251,191,36));
        addLegendItem(leg, "여유",     new Color(52,211,153));
        return leg;
    }

    private void addLegendItem(JPanel p, String text, Color c) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT,4,0));
        item.setBackground(WHITE);
        JPanel dot = new JPanel();
        dot.setBackground(c);
        dot.setPreferredSize(new Dimension(8,8));
        JLabel l = new JLabel(text);
        l.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        l.setForeground(GRAY);
        item.add(dot); item.add(l);
        p.add(item);
    }

    // ─── 상단 카드 ───────────────────────────────────────────
    // 오늘 경기 정보 카드
    private JPanel createTodayCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(NAVY);
        card.setBorder(new CompoundBorder(
                new LineBorder(NAVY,1,true),
                new EmptyBorder(13,16,13,16)));

        JLabel lbl   = new JLabel("● 오늘 경기 · 2026년 5월 12일");
        lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        lbl.setForeground(new Color(150,170,200));

        JLabel title = new JLabel("롯데 자이언츠 vs NC 다이노스");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        title.setForeground(WHITE);

        JLabel meta  = new JLabel("  18:30  ·  사직야구장  ·  맑음 22도  ·  현재 8위");
        meta.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        meta.setForeground(new Color(130,155,185));

        card.add(lbl);
        card.add(Box.createVerticalStrut(5));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(meta);
        return card;
    }

    // D-day 카운트다운 카드
    private JPanel createCountdownCard(String label, String value, String sub, boolean red) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(229,231,235),1,true),
                new EmptyBorder(13,16,13,16)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        lbl.setForeground(GRAY);

        JLabel val = new JLabel(value);
        val.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        val.setForeground(red ? RED : NAVY);

        JLabel s = new JLabel(sub);
        s.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        s.setForeground(GRAY);

        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(val);
        card.add(Box.createVerticalStrut(2));
        card.add(s);
        return card;
    }

    // ─── 유틸 메서드 ─────────────────────────────────────────
    private JLabel createSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        l.setForeground(GRAY);
        l.setBorder(new EmptyBorder(8,16,3,16));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        return l;
    }

    private JPanel createDivider() {
        JPanel d = new JPanel();
        d.setBackground(new Color(243,244,246));
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return d;
    }

    private JLabel createBadge(String text, Color bg, Color fg) {
        JLabel b = new JLabel(text);
        b.setFont(new Font("맑은 고딕", Font.BOLD, 9));
        b.setForeground(fg);
        b.setBackground(bg);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(1,5,1,5));
        return b;
    }

    // 구역명으로 색상 반환 (사직야구장 좌석 배치도 기준)
    private Color getZoneColor(String n) {
        if (n.contains("에비뉴엘"))      return new Color(75,75,190);
        if (n.contains("중앙탁자"))      return new Color(216,84,107);
        if (n.contains("휠체어"))        return new Color(255,0,128);
        if (n.contains("네이버"))        return new Color(238,182,71);
        if (n.contains("메디힐"))        return new Color(79,186,196);
        if (n.contains("중앙상단"))      return new Color(195,0,11);
        if (n.contains("SKY"))           return new Color(63,155,57);
        if (n.contains("1루 내야탁자"))  return new Color(195,217,50);
        if (n.contains("3루 내야탁자"))  return new Color(150,150,200);
        if (n.contains("1루 내야필드"))  return new Color(58,107,156);
        if (n.contains("1루 내야상단"))  return new Color(15,84,126);
        if (n.contains("3루 내야필드A")) return new Color(195,217,50);
        if (n.contains("3루 내야필드B")) return new Color(243,113,32);
        if (n.contains("3루 내야상단A")) return new Color(121,185,26);
        if (n.contains("3루 내야상단B")) return new Color(63,155,57);
        if (n.contains("정관장"))        return new Color(149,255,199);
        if (n.contains("르노"))          return new Color(195,167,192);
        if (n.contains("1루 외야"))      return new Color(101,120,40);
        if (n.contains("3루 외야"))      return new Color(77,185,72);
        return new Color(136,135,128);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
