import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class LoadingFrame extends JFrame {

    private static final Color NAVY  = new Color(4, 30, 66);
    private static final Color RED   = new Color(208, 15, 49);
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY  = new Color(156, 163, 175);

    public LoadingFrame() {
        setTitle("Lotte-Link Radar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 280);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(NAVY);

        // 로고
        JPanel top = new JPanel();
        top.setBackground(NAVY);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(40, 0, 20, 0));

        JLabel logo = new JLabel("Lotte-Link Radar");
        logo.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        logo.setForeground(WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("경기 정보를 불러오는 중...");
        sub.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        sub.setForeground(GRAY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        top.add(logo);
        top.add(Box.createVerticalStrut(8));
        top.add(sub);
        add(top, BorderLayout.CENTER);

        // 하단 진행 상태
        JPanel bottom = new JPanel();
        bottom.setBackground(NAVY);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(0, 40, 30, 40));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setForeground(RED);
        progressBar.setBackground(new Color(30, 50, 90));
        progressBar.setBorder(null);
        progressBar.setPreferredSize(new Dimension(0, 6));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel("준비 중...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        statusLabel.setForeground(GRAY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottom.add(progressBar);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(statusLabel);
        add(bottom, BorderLayout.SOUTH);

        setVisible(true);

        WishManager.load();

        // ✅ 홈 경기 + PROGRAM_CD 있는 것만 조회 (원정 제외)
        new Thread(() -> {
            List<String[]> games = GameSchedule.getUpcomingHomeGames(6);

            if (games.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("예매 중인 홈경기 없음");
                    progressBar.setValue(100);
                    try { Thread.sleep(500); } catch (Exception ex) {}
                    dispose();
                    new MainFrame();
                });
                return;
            }

            int total = games.size();
            int[] count = {0};

            for (String[] game : games) {
                String date     = game[0];
                String time     = game[1];
                String programCd = game[3];
                String opponent = game[2];
                String label    = date.substring(4, 6) + "/" + date.substring(6, 8) + " vs " + opponent;

                SwingUtilities.invokeLater(() -> statusLabel.setText(label + " 확인 중..."));

                List<SeatInfo> seats = SeatEngine.fetch(date, time, programCd);

                if (seats != null) {
                    boolean allSoldOut = seats.stream()
                            .filter(s -> !s.getName().contains("휠체어"))
                            .allMatch(s -> s.getRemain() == 0);
                    SeatDataCache.put(date, seats, allSoldOut);
                }

                count[0]++;
                int progress = (count[0] * 100) / total;
                SwingUtilities.invokeLater(() -> progressBar.setValue(progress));

                try { Thread.sleep(500); } catch (Exception ex) {}
            }

            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("완료!");
                progressBar.setValue(100);
                try { Thread.sleep(300); } catch (Exception ex) {}
                dispose();
                new MainFrame();
            });

        }).start();
    }
}
