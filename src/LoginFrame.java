import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private static final Color NAVY = new Color(4, 30, 66);
    private static final Color RED = new Color(208, 15, 49);
    private static final Color BG = new Color(240, 242, 245);
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY = new Color(156, 163, 175);

    public LoginFrame() {
        setTitle("Lotte-Link Radar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        setVisible(true);
    }


    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(NAVY);
        header.setPreferredSize(new Dimension(0, 80));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel logo = new JLabel("Lotte-Link Radar");
        logo.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        logo.setForeground(WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("롯데 자이언츠 실시간 좌석 레이더");
        sub.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        sub.setForeground(GRAY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);

        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel();
        body.setBackground(BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(30, 40, 30, 40));

        // 안내 카드
        JPanel guideCard = new JPanel();
        guideCard.setLayout(new BoxLayout(guideCard, BoxLayout.Y_AXIS));
        guideCard.setBackground(WHITE);
        guideCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        guideCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JLabel guideTitle = new JLabel("시작 전 준비");
        guideTitle.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        guideTitle.setForeground(NAVY);
        guideTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel step1 = new JLabel("1.  브라우저에서 예매 웹사이트 로그인");
        step1.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        step1.setForeground(new Color(55, 65, 81));
        step1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel step2 = new JLabel("2.  F12 → Application → Cookies 클릭");
        step2.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        step2.setForeground(new Color(55, 65, 81));
        step2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel step3 = new JLabel("3.  JSESSIONID 값 복사");
        step3.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        step3.setForeground(new Color(55, 65, 81));
        step3.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel step4 = new JLabel("4.  아래 입력창에 붙여넣기 후 시작");
        step4.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        step4.setForeground(new Color(55, 65, 81));
        step4.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel siteLink = new JLabel("예매 웹사이트: ticket.giantsclub.com");
        siteLink.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        siteLink.setForeground(new Color(99, 102, 241));
        siteLink.setAlignmentX(Component.LEFT_ALIGNMENT);
        siteLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        siteLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(
                            new java.net.URI("https://ticket.giantsclub.com")
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        guideCard.add(guideTitle);
        guideCard.add(Box.createVerticalStrut(10));
        guideCard.add(step1);
        guideCard.add(Box.createVerticalStrut(5));
        guideCard.add(step2);
        guideCard.add(Box.createVerticalStrut(5));
        guideCard.add(step3);
        guideCard.add(Box.createVerticalStrut(5));
        guideCard.add(step4);
        guideCard.add(Box.createVerticalStrut(10));
        guideCard.add(siteLink);

        // JSESSIONID 입력
        JLabel inputLabel = new JLabel("JSESSIONID");
        inputLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        inputLabel.setForeground(new Color(55, 65, 81));
        inputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField sessionInput = new JTextField();
        sessionInput.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        sessionInput.setBorder(new CompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        sessionInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        sessionInput.setToolTipText("JSESSIONID 값을 붙여넣으세요");

        // 시작 버튼
        JButton startBtn = new JButton("레이더 시작");
        startBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        startBtn.setForeground(WHITE);
        startBtn.setBackground(NAVY);
        startBtn.setOpaque(true);
        startBtn.setBorderPainted(false);
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        startBtn.addActionListener(e -> {
            String sessionId = sessionInput.getText().trim();
            if (sessionId.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "JSESSIONID를 입력해주세요.",
                        "입력 오류",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            SeatEngine.setSession(sessionId, "smin1115");

            // ✅ 로딩 화면으로 전환
            dispose();
            new LoadingFrame();
        });

        // 엔터 키로도 시작 가능
        sessionInput.addActionListener(e -> startBtn.doClick());

        body.add(guideCard);
        body.add(Box.createVerticalStrut(20));
        body.add(inputLabel);
        body.add(Box.createVerticalStrut(6));
        body.add(sessionInput);
        body.add(Box.createVerticalStrut(14));
        body.add(startBtn);

        return body;
    }

}