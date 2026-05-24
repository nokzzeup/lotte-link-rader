import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class KboRankPanel extends JPanel {

    private static final Color NAVY     = new Color(4, 30, 66);
    private static final Color RED      = new Color(208, 15, 49);
    private static final Color WHITE    = Color.WHITE;
    private static final Color BG       = new Color(240, 242, 245);
    private static final Color GRAY     = new Color(156, 163, 175);
    private static final Color LIGHT_BG = new Color(243, 244, 246);
    private static final Color LOTTE_BG = new Color(232, 237, 245);

    private JPanel rankTablePanel;
    private JLabel lastUpdateLabel;

    public KboRankPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(new EmptyBorder(16, 18, 16, 18));

        add(createHeader(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

        fetchRankingInBackground();
    }

    // ─── 헤더 ─────────────────────────────────────────────────
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        header.setPreferredSize(new Dimension(0, 40));

        JLabel title = new JLabel("KBO 팀 순위");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        title.setForeground(new Color(17, 24, 39));
        header.add(title, BorderLayout.WEST);

        return header;
    }

    // ─── 순위표 카드 ──────────────────────────────────────────
    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(new LineBorder(new Color(229, 231, 235), 1, true));

        rankTablePanel = new JPanel();
        rankTablePanel.setLayout(new BoxLayout(rankTablePanel, BoxLayout.Y_AXIS));
        rankTablePanel.setBackground(WHITE);

        JPanel loading = new JPanel(new GridBagLayout());
        loading.setBackground(WHITE);
        JLabel lbl = new JLabel("순위 정보를 불러오는 중...");
        lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        lbl.setForeground(GRAY);
        loading.add(lbl);
        rankTablePanel.add(loading);

        JScrollPane scroll = new JScrollPane(rankTablePanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    // ─── 하단 바 ──────────────────────────────────────────────
    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(WHITE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(8, 14, 8, 14)));

        lastUpdateLabel = new JLabel("불러오는 중...");
        lastUpdateLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        lastUpdateLabel.setForeground(GRAY);
        bar.add(lastUpdateLabel, BorderLayout.WEST);

        JLabel source = new JLabel("출처: KBO 공식 홈페이지");
        source.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        source.setForeground(GRAY);
        bar.add(source, BorderLayout.EAST);

        return bar;
    }

    // ─── 백그라운드 크롤링 ────────────────────────────────────
    private void fetchRankingInBackground() {
        new Thread(() -> {
            List<KboService.TeamRank> ranks = KboService.fetchRanking();
            SwingUtilities.invokeLater(() -> {
                rankTablePanel.removeAll();
                if (ranks.isEmpty()) {
                    JPanel err = new JPanel(new GridBagLayout());
                    err.setBackground(WHITE);
                    JLabel lbl = new JLabel("순위 정보를 불러올 수 없습니다");
                    lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
                    lbl.setForeground(GRAY);
                    err.add(lbl);
                    rankTablePanel.add(err);
                } else {
                    rankTablePanel.add(createTableHeader());
                    for (KboService.TeamRank r : ranks) rankTablePanel.add(createRankRow(r));
                }
                rankTablePanel.revalidate();
                rankTablePanel.repaint();

                String now = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
                lastUpdateLabel.setText("마지막 갱신 " + now + "  ·  출처: KBO 공식 홈페이지");
            });
        }).start();
    }

    // ─── 테이블 헤더 ──────────────────────────────────────────
    private JPanel createTableHeader() {
        JPanel header = new JPanel(new GridLayout(1, 9));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        String[] cols = {"순위", "팀", "경기", "승", "패", "승률", "게임차", "최근10경기", "연속"};
        for (String col : cols) {
            JLabel lbl = new JLabel(col, SwingConstants.CENTER);
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 11));
            lbl.setForeground(WHITE);
            header.add(lbl);
        }
        return header;
    }

    // ─── 순위 행 ──────────────────────────────────────────────
    private JPanel createRankRow(KboService.TeamRank rank) {
        boolean isLotte = rank.isLotte();

        JPanel row = new JPanel(new GridLayout(1, 9));
        row.setBackground(isLotte ? LOTTE_BG : WHITE);
        row.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(10, 16, 10, 16)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        Color rankColor = rank.rank == 1 ? new Color(202, 138, 4)
                        : rank.rank == 2 ? new Color(107, 114, 128)
                        : rank.rank == 3 ? new Color(180, 83, 9)
                        : new Color(55, 65, 81);

        JLabel rankLbl = new JLabel(String.valueOf(rank.rank), SwingConstants.CENTER);
        rankLbl.setFont(new Font("맑은 고딕", rank.rank <= 3 ? Font.BOLD : Font.PLAIN, 13));
        rankLbl.setForeground(rankColor);
        row.add(rankLbl);

        JLabel teamLbl = new JLabel(rank.team, SwingConstants.CENTER);
        teamLbl.setFont(new Font("맑은 고딕", isLotte ? Font.BOLD : Font.PLAIN, 13));
        teamLbl.setForeground(isLotte ? RED : new Color(17, 24, 39));
        row.add(teamLbl);

        String[] values = {
            String.valueOf(rank.games),
            String.valueOf(rank.win),
            String.valueOf(rank.lose),
            rank.winRate,
            rank.gameDiff,
            rank.last10,
            rank.streak
        };

        for (String val : values) {
            JLabel lbl = new JLabel(val, SwingConstants.CENTER);
            lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            lbl.setForeground(new Color(55, 65, 81));
            row.add(lbl);
        }
        return row;
    }
}
