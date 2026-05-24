import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class RecordPanel extends JPanel {

    private static final Color NAVY     = new Color(4, 30, 66);
    private static final Color RED      = new Color(208, 15, 49);
    private static final Color WHITE    = Color.WHITE;
    private static final Color BG       = new Color(240, 242, 245);
    private static final Color GRAY     = new Color(156, 163, 175);
    private static final Color LIGHT_BG = new Color(243, 244, 246);

    private static final String[] ZONES = {
        "에비뉴엘석", "중앙탁자석", "네이버 클럽하우스석", "메디힐 SKY석",
        "중앙상단석", "1루 내야탁자석", "3루 내야탁자석",
        "1루 내야필드석", "1루 내야상단석",
        "3루 내야필드석A", "3루 내야필드석B",
        "3루 내야상단석A", "3루 내야상단석B",
        "정관장 Red석", "르노코리아 블루석",
        "1루 외야석", "3루 외야석", "휠체어석"
    };

    private JPanel recordListPanel;
    private JLabel totalValLabel, recordValLabel, rateValLabel, thankValLabel, thankSubLabel;

    public RecordPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(16, 18, 16, 18));

        RecordManager.load();

        add(createStatCards(), BorderLayout.NORTH);
        add(createListCard(), BorderLayout.CENTER);

        refreshAll();
    }

    // ─── 상단 통계 카드 ───────────────────────────────────────
    private JPanel createStatCards() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBackground(BG);
        panel.setBorder(new EmptyBorder(0, 0, 12, 0));
        panel.setPreferredSize(new Dimension(0, 88));

        JPanel card1 = createCardShell("총 직관");
        totalValLabel = new JLabel("0경기");
        totalValLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        totalValLabel.setForeground(new Color(17, 24, 39));
        card1.add(totalValLabel);
        panel.add(card1);

        JPanel card2 = createCardShell("승 / 무 / 패");
        recordValLabel = new JLabel("0 / 0 / 0");
        recordValLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        recordValLabel.setForeground(new Color(17, 24, 39));
        card2.add(recordValLabel);
        panel.add(card2);

        JPanel card3 = createCardShell("직관 승률");
        rateValLabel = new JLabel("-%");
        rateValLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        rateValLabel.setForeground(new Color(5, 150, 105));
        card3.add(rateValLabel);
        panel.add(card3);

        JPanel card4 = createCardShell("너만 오면 생큐");
        thankValLabel = new JLabel("-");
        thankValLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        thankValLabel.setForeground(RED);
        thankSubLabel = new JLabel("");
        thankSubLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        thankSubLabel.setForeground(GRAY);
        card4.add(thankValLabel);
        card4.add(Box.createVerticalStrut(2));
        card4.add(thankSubLabel);
        panel.add(card4);

        return panel;
    }

    private JPanel createCardShell(String labelText) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(12, 16, 12, 16)));
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        lbl.setForeground(GRAY);
        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        return card;
    }

    // ─── 기록 목록 카드 ───────────────────────────────────────
    private JPanel createListCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(new LineBorder(new Color(229, 231, 235), 1, true));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel title = new JLabel("내 직관 기록");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        title.setForeground(new Color(17, 24, 39));
        header.add(title, BorderLayout.WEST);

        JButton addBtn = new JButton("+ 기록 추가");
        addBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        addBtn.setForeground(NAVY);
        addBtn.setBackground(WHITE);
        addBtn.setBorder(new LineBorder(new Color(229, 231, 235)));
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> showAddDialog());
        header.add(addBtn, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        recordListPanel = new JPanel();
        recordListPanel.setLayout(new BoxLayout(recordListPanel, BoxLayout.Y_AXIS));
        recordListPanel.setBackground(WHITE);

        JScrollPane scroll = new JScrollPane(recordListPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ─── 전체 갱신 ────────────────────────────────────────────
    private void refreshAll() {
        refreshRecordList();
        refreshStats();
    }

    // ─── 통계 갱신 ────────────────────────────────────────────
    private void refreshStats() {
        List<String[]> all = RecordManager.getAll();
        int wins  = RecordManager.getWins();
        int loses = RecordManager.getLoses();
        int draws = RecordManager.getDraws();

        totalValLabel.setText(all.size() + "경기");
        recordValLabel.setText(wins + " / " + draws + " / " + loses);

        if (wins + loses == 0) {
            rateValLabel.setText("-%");
        } else {
            double rate = (double) wins / (wins + loses) * 100;
            rateValLabel.setText(String.format("%.1f%%", rate));
        }

        // 너만 오면 생큐: 팀별 승률 최고 팀
        Map<String, int[]> teamStats = new LinkedHashMap<>();
        for (String[] r : all) {
            teamStats.computeIfAbsent(r[1], k -> new int[2]);
            if (r[2].equals("승")) teamStats.get(r[1])[0]++;
            if (r[2].equals("패")) teamStats.get(r[1])[1]++;
        }

        String bestTeam = null;
        double bestRate = -1;
        int bestWin = 0, bestTotal = 0;

        for (Map.Entry<String, int[]> entry : teamStats.entrySet()) {
            int w = entry.getValue()[0], l = entry.getValue()[1];
            if (w + l == 0) continue;
            double rate = (double) w / (w + l) * 100;
            if (rate > bestRate || (rate == bestRate && w > bestWin)) {
                bestRate = rate; bestTeam = entry.getKey();
                bestWin = w; bestTotal = w + l;
            }
        }

        if (bestTeam != null) {
            thankValLabel.setText("vs " + bestTeam);
            thankSubLabel.setText(String.format("%.0f%% (%d전 %d승)", bestRate, bestTotal, bestWin));
        } else {
            thankValLabel.setText("-");
            thankSubLabel.setText("");
        }
    }

    // ─── 기록 목록 갱신 ───────────────────────────────────────
    private void refreshRecordList() {
        recordListPanel.removeAll();
        List<String[]> all = RecordManager.getAll();

        if (all.isEmpty()) {
            JLabel empty = new JLabel("직관 기록이 없습니다", SwingConstants.CENTER);
            empty.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            empty.setForeground(GRAY);
            empty.setBorder(new EmptyBorder(40, 0, 0, 0));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            recordListPanel.add(empty);
        } else {
            for (int i = 0; i < all.size(); i++) {
                recordListPanel.add(createRecordItem(all.get(i), i));
            }
        }

        recordListPanel.revalidate();
        recordListPanel.repaint();
    }

    // ─── 기록 아이템 (고정 높이) ──────────────────────────────
    private JPanel createRecordItem(String[] record, int index) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(WHITE);
        item.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(10, 16, 10, 16)));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        item.setMinimumSize(new Dimension(0, 68));
        item.setPreferredSize(new Dimension(0, 68));

        // 날짜 박스
        JPanel dateBox = new JPanel();
        dateBox.setLayout(new BoxLayout(dateBox, BoxLayout.Y_AXIS));
        dateBox.setBackground(WHITE);
        dateBox.setPreferredSize(new Dimension(40, 0));

        String date = record[0];
        JLabel dayL = new JLabel(date.substring(6, 8), SwingConstants.CENTER);
        dayL.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        dayL.setForeground(new Color(17, 24, 39));
        dayL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel moL = new JLabel(date.substring(4, 6) + "월", SwingConstants.CENTER);
        moL.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
        moL.setForeground(GRAY);
        moL.setAlignmentX(Component.CENTER_ALIGNMENT);

        dateBox.add(dayL);
        dateBox.add(moL);

        // 경기 정보
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(WHITE);
        info.setBorder(new EmptyBorder(0, 12, 0, 0));

        JLabel team = new JLabel("롯데 vs " + record[1]);
        team.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        team.setForeground(new Color(17, 24, 39));

        // 결과 배지 + 점수
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        badges.setBackground(WHITE);

        String result = record[2];
        Color resultBg = result.equals("승") ? new Color(236, 253, 245)
                       : result.equals("패") ? new Color(254, 242, 242) : LIGHT_BG;
        Color resultFg = result.equals("승") ? new Color(6, 95, 70)
                       : result.equals("패") ? new Color(153, 27, 27) : GRAY;

        JLabel resultBadge = new JLabel(result);
        resultBadge.setFont(new Font("맑은 고딕", Font.BOLD, 10));
        resultBadge.setForeground(resultFg);
        resultBadge.setBackground(resultBg);
        resultBadge.setOpaque(true);
        resultBadge.setBorder(new EmptyBorder(1, 6, 1, 6));
        badges.add(resultBadge);



        info.add(team);
        info.add(badges);

        // 좌석 + 메모 한 줄로 표시
        String seat = (record.length > 4 && !record[4].isEmpty()) ? record[4] : "";
        String memo = (record.length > 5 && !record[5].isEmpty()) ? record[5] : "";
        String subText = "";
        if (!seat.isEmpty() && !memo.isEmpty()) subText = "🪑 " + seat + "  ·  " + memo;
        else if (!seat.isEmpty()) subText = "🪑 " + seat;
        else if (!memo.isEmpty()) subText = memo;

        if (!subText.isEmpty()) {
            if (subText.length() > 40) subText = subText.substring(0, 40) + "...";
            JLabel subLbl = new JLabel(subText);
            subLbl.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            subLbl.setForeground(GRAY);
            info.add(subLbl);
        }

        // 삭제 버튼
        JButton delBtn = new JButton("✕");
        delBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        delBtn.setForeground(GRAY);
        delBtn.setBackground(WHITE);
        delBtn.setBorderPainted(false);
        delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        delBtn.addActionListener(e -> { RecordManager.remove(index); refreshAll(); });

        item.add(dateBox, BorderLayout.WEST);
        item.add(info, BorderLayout.CENTER);
        item.add(delBtn, BorderLayout.EAST);
        return item;
    }

    // ─── 기록 추가 다이얼로그 ─────────────────────────────────
    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "직관 기록 추가", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 경기 선택
        JLabel gameL = new JLabel("경기 선택");
        gameL.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        gameL.setForeground(new Color(55, 65, 81));

        List<String[]> allGames = GameSchedule.getAllGames();
        List<String> gameOptions = new ArrayList<>();
        Map<String, String[]> gameMap = new LinkedHashMap<>();

        for (String[] g : allGames) {
            if (g.length >= 5 && g[4].equals("홈")) {
                String key = g[0].substring(4,6) + "/" + g[0].substring(6,8) + " vs " + g[2];
                gameOptions.add(key);
                gameMap.put(key, g);
            }
        }

        JComboBox<String> gameBox = new JComboBox<>(gameOptions.toArray(new String[0]));
        gameBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        gameBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        // 결과
        JLabel resultL = new JLabel("결과");
        resultL.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        resultL.setForeground(new Color(55, 65, 81));

        JComboBox<String> resultBox = new JComboBox<>(new String[]{"승", "패", "무"});
        resultBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        resultBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        // 좌석
        JLabel seatL = new JLabel("좌석");
        seatL.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        seatL.setForeground(new Color(55, 65, 81));

        JComboBox<String> seatBox = new JComboBox<>(ZONES);
        seatBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        seatBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        // 메모 (선택)
        JLabel memoL = new JLabel("메모 (선택)");
        memoL.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        memoL.setForeground(new Color(55, 65, 81));

        JTextField memoField = new JTextField();
        styleTextField(memoField);

        form.add(gameL);   form.add(Box.createVerticalStrut(4)); form.add(gameBox);
        form.add(Box.createVerticalStrut(12));
        form.add(resultL); form.add(Box.createVerticalStrut(4)); form.add(resultBox);
        form.add(Box.createVerticalStrut(12));
        form.add(seatL);   form.add(Box.createVerticalStrut(4)); form.add(seatBox);
        form.add(Box.createVerticalStrut(12));
        form.add(memoL);   form.add(Box.createVerticalStrut(4)); form.add(memoField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(LIGHT_BG);
        btnPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton cancelBtn = new JButton("취소");
        cancelBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("저장");
        saveBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        saveBtn.setForeground(WHITE);
        saveBtn.setBackground(NAVY);
        saveBtn.setOpaque(true);
        saveBtn.setBorderPainted(false);
        saveBtn.addActionListener(e -> {
            String selectedKey = (String) gameBox.getSelectedItem();
            if (selectedKey == null) return;
            String[] game  = gameMap.get(selectedKey);
            String date    = game[0];
            String opp     = game[2];
            String result  = (String) resultBox.getSelectedItem();
            
            String seat    = (String) seatBox.getSelectedItem();
            String memo    = memoField.getText().trim();

            RecordManager.add(date, opp, result, "", seat, memo);
            refreshAll();
            dialog.dispose();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(6, 8, 6, 8)));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    }
}
