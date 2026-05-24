import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class NoticeListPanel extends JPanel {

    private static final Color NAVY     = new Color(4, 30, 66);
    private static final Color RED      = new Color(208, 15, 49);
    private static final Color WHITE    = Color.WHITE;
    private static final Color BG       = new Color(240, 242, 245);
    private static final Color GRAY     = new Color(156, 163, 175);
    private static final Color LIGHT_BG = new Color(243, 244, 246);

    private static final String ADMIN_PASSWORD = "admin1234";

    private JPanel noticeListPanel;
    private JPanel detailPanel;        // 우측 상세 패널 (MainFrame에서 가져다 씀)
    private JLabel statusLabel;
    private String selectedFilter = "전체";

    public NoticeListPanel() {
        setLayout(new BorderLayout());
        setBackground(WHITE);
        setPreferredSize(new Dimension(280, 0));
        setBorder(new MatteBorder(0, 0, 0, 1, new Color(229, 231, 235)));

        NoticeManager.load();

        // 상세 패널 미리 생성
        detailPanel = createDetailPanel();

        add(createFilterBar(), BorderLayout.NORTH);
        add(createListArea(), BorderLayout.CENTER);
        add(createBottomBar(), BorderLayout.SOUTH);

        refreshNoticeList();
        fetchNoticesInBackground();
    }

    // 우측 상세 패널 반환 (MainFrame이 CENTER에 배치)
    public JPanel getDetailPanel() { return detailPanel; }

    // ─── 백그라운드 크롤링 ────────────────────────────────────
    private void fetchNoticesInBackground() {
        if (statusLabel != null) statusLabel.setText("공식 공지 불러오는 중...");
        new Thread(() -> {
            List<Notice> fetched = NoticeManager.fetchFromWeb();
            NoticeManager.mergeFromWeb(fetched);
            SwingUtilities.invokeLater(() -> {
                refreshNoticeList();
                if (statusLabel != null)
                    statusLabel.setText("공식 공지 " + fetched.size() + "개 불러옴");
            });
        }).start();
    }

    // ─── 필터 바 ──────────────────────────────────────────────
    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 8));
        bar.setBackground(WHITE);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        String[] filters = {"전체", "이벤트", "예매", "공지", "기타"};
        for (String f : filters) bar.add(createFilterBtn(f, bar));
        return bar;
    }

    private JLabel createFilterBtn(String text, JPanel parent) {
        JLabel btn = new JLabel(text);
        btn.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        btn.setBorder(new EmptyBorder(3, 8, 3, 8));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateFilterStyle(btn, text.equals(selectedFilter));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectedFilter = text;
                refreshNoticeList();
                for (Component c : parent.getComponents()) {
                    if (c instanceof JLabel)
                        updateFilterStyle((JLabel) c, ((JLabel) c).getText().equals(selectedFilter));
                }
            }
        });
        return btn;
    }

    private void updateFilterStyle(JLabel btn, boolean active) {
        String text = btn.getText();
        Color bg, fg;
        switch (text) {
            case "이벤트": bg = new Color(253,244,255); fg = new Color(126,34,206); break;
            case "예매":   bg = new Color(254,242,242); fg = new Color(153,27,27);  break;
            case "공지":   bg = new Color(239,246,255); fg = new Color(29,78,216);  break;
            case "기타":   bg = new Color(243,244,246); fg = new Color(75,85,99);   break;
            default:       bg = new Color(243,244,246); fg = new Color(55,65,81);   break;
        }
        if (active) { btn.setBackground(fg); btn.setForeground(WHITE); }
        else        { btn.setBackground(bg); btn.setForeground(fg); }
        btn.setOpaque(true);
    }

    // ─── 목록 영역 ────────────────────────────────────────────
    private JScrollPane createListArea() {
        noticeListPanel = new JPanel();
        noticeListPanel.setLayout(new BoxLayout(noticeListPanel, BoxLayout.Y_AXIS));
        noticeListPanel.setBackground(WHITE);

        JScrollPane scroll = new JScrollPane(noticeListPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        return scroll;
    }

    // ─── 하단 바 ──────────────────────────────────────────────
    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(LIGHT_BG);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(229, 231, 235)),
                new EmptyBorder(6, 14, 6, 14)));

        statusLabel = new JLabel("공지를 불러오는 중...");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        statusLabel.setForeground(GRAY);
        bar.add(statusLabel, BorderLayout.WEST);

        JButton adminBtn = new JButton("+ 공지 추가");
        adminBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        adminBtn.setForeground(NAVY);
        adminBtn.setBackground(WHITE);
        adminBtn.setBorder(new LineBorder(new Color(229, 231, 235)));
        adminBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        adminBtn.addActionListener(e -> showAdminDialog());
        bar.add(adminBtn, BorderLayout.EAST);
        return bar;
    }

    // ─── 목록 갱신 ────────────────────────────────────────────
    private void refreshNoticeList() {
        noticeListPanel.removeAll();
        List<Notice> all = NoticeManager.getAll();

        for (Notice n : all) {
            if (!selectedFilter.equals("전체") && !n.getTag().equals(selectedFilter)) continue;
            noticeListPanel.add(createNoticeItem(n));
        }

        if (noticeListPanel.getComponentCount() == 0) {
            JLabel empty = new JLabel("공지가 없습니다", SwingConstants.CENTER);
            empty.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
            empty.setForeground(GRAY);
            empty.setBorder(new EmptyBorder(30, 0, 0, 0));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            noticeListPanel.add(empty);
        }

        noticeListPanel.revalidate();
        noticeListPanel.repaint();
    }

    // ─── 공지 아이템 ──────────────────────────────────────────
    private JPanel createNoticeItem(Notice notice) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(WHITE);
        item.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(243, 244, 246)),
                new EmptyBorder(10, 14, 10, 14)));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(WHITE);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        topRow.setBackground(WHITE);
        topRow.add(createTagLabel(notice.getTag()));

        JLabel dateL = new JLabel(notice.getDate());
        dateL.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        dateL.setForeground(GRAY);
        topRow.add(dateL);

        // 제목 JTextArea (긴 제목 2줄 표시)
        JTextArea title = new JTextArea(notice.getTitle());
        title.setFont(new Font("맑은 고딕", notice.isUnread() ? Font.BOLD : Font.PLAIN, 12));
        title.setForeground(notice.isUnread() ? new Color(17,24,39) : new Color(107,114,128));
        title.setBackground(WHITE);
        title.setLineWrap(true);
        title.setWrapStyleWord(true);
        title.setEditable(false);
        title.setOpaque(false);
        title.setBorder(null);
        title.setRows(2);

        left.add(topRow);
        left.add(Box.createVerticalStrut(3));
        left.add(title);

        item.add(left, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                item.setBackground(LIGHT_BG);
                left.setBackground(LIGHT_BG);
                topRow.setBackground(LIGHT_BG);
            }
            @Override public void mouseExited(MouseEvent e) {
                item.setBackground(WHITE);
                left.setBackground(WHITE);
                topRow.setBackground(WHITE);
            }
            @Override public void mouseClicked(MouseEvent e) {
                NoticeManager.markRead(notice.getId());
                refreshNoticeList();
                showDetail(notice);
            }
        });

        return item;
    }

    // ─── 우측 상세 패널 ───────────────────────────────────────
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG);
        JLabel guide = new JLabel("공지를 선택하세요", SwingConstants.CENTER);
        guide.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        guide.setForeground(GRAY);
        panel.add(guide);
        return panel;
    }

    private void showDetail(Notice notice) {
        detailPanel.removeAll();
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBackground(BG);
        detailPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(new LineBorder(new Color(229,231,235), 1, true));

        // 헤더
        JPanel cardHeader = new JPanel();
        cardHeader.setLayout(new BoxLayout(cardHeader, BoxLayout.Y_AXIS));
        cardHeader.setBackground(WHITE);
        cardHeader.setBorder(new CompoundBorder(
                new MatteBorder(0,0,1,0,new Color(229,231,235)),
                new EmptyBorder(14,18,14,18)));

        JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tagRow.setBackground(WHITE);
        tagRow.add(createTagLabel(notice.getTag()));

        JLabel dateL = new JLabel(notice.getDate());
        dateL.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        dateL.setForeground(GRAY);
        tagRow.add(dateL);

        JLabel titleL = new JLabel(notice.getTitle());
        titleL.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleL.setForeground(new Color(17,24,39));

        cardHeader.add(tagRow);
        cardHeader.add(Box.createVerticalStrut(8));
        cardHeader.add(titleL);
        card.add(cardHeader, BorderLayout.NORTH);

        boolean isWeb = notice.getContent().startsWith("http");

        if (isWeb) {
            JPanel loading = new JPanel(new GridBagLayout());
            loading.setBackground(WHITE);
            JLabel lbl = new JLabel("내용 불러오는 중...");
            lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
            lbl.setForeground(GRAY);
            loading.add(lbl);
            card.add(loading, BorderLayout.CENTER);

            String url = notice.getContent();
            new Thread(() -> {
                String content = NoticeManager.fetchDetail(url);
                SwingUtilities.invokeLater(() -> {
                    card.remove(loading);
                    if (content.trim().isEmpty() || content.equals("내용을 불러올 수 없습니다.")) {
                        card.add(createImageNoticePanel(), BorderLayout.CENTER);
                    } else {
                        card.add(createContentPanel(content), BorderLayout.CENTER);
                    }
                    card.revalidate();
                    card.repaint();
                });
            }).start();
        } else {
            card.add(createContentPanel(notice.getContent()), BorderLayout.CENTER);
        }

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnRow.setBackground(LIGHT_BG);
        btnRow.setBorder(new MatteBorder(1,0,0,0,new Color(229,231,235)));

        if (isWeb) {
            JButton linkBtn = new JButton("원문 보기 →");
            linkBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            linkBtn.setForeground(new Color(99,102,241));
            linkBtn.setBackground(WHITE);
            linkBtn.setBorder(new LineBorder(new Color(229,231,235)));
            linkBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            linkBtn.addActionListener(e -> {
                try { Desktop.getDesktop().browse(new java.net.URI(notice.getContent())); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "브라우저를 열 수 없습니다.", "오류", JOptionPane.ERROR_MESSAGE); }
            });
            btnRow.add(linkBtn);
        } else {
            JButton editBtn = new JButton("수정");
            editBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            editBtn.setForeground(NAVY);
            editBtn.setBackground(WHITE);
            editBtn.setBorder(new LineBorder(new Color(229,231,235)));
            editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            editBtn.addActionListener(e -> showEditDialog(notice));

            JButton delBtn = new JButton("삭제");
            delBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
            delBtn.setForeground(RED);
            delBtn.setBackground(WHITE);
            delBtn.setBorder(new LineBorder(new Color(229,231,235)));
            delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            delBtn.addActionListener(e -> {
                String pw = JOptionPane.showInputDialog(this, "관리자 비밀번호:", "삭제 확인", JOptionPane.PLAIN_MESSAGE);
                if (ADMIN_PASSWORD.equals(pw)) {
                    NoticeManager.delete(notice.getId());
                    refreshNoticeList();
                    resetDetail();
                } else if (pw != null) {
                    JOptionPane.showMessageDialog(this, "비밀번호가 틀렸습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            });
            btnRow.add(editBtn);
            btnRow.add(delBtn);
        }

        card.add(btnRow, BorderLayout.SOUTH);
        detailPanel.add(card, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private JScrollPane createContentPanel(String content) {
        JTextArea textArea = new JTextArea(content);
        textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        textArea.setForeground(new Color(55,65,81));
        textArea.setBackground(WHITE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(16, 18, 16, 18));
        textArea.setBorder(null);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        return scroll;
    }

    private JPanel createImageNoticePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(WHITE);
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(WHITE);

        JLabel icon = new JLabel("🖼", SwingConstants.CENTER);
        icon.setFont(new Font("맑은 고딕", Font.PLAIN, 36));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg1 = new JLabel("이미지로 구성된 공지입니다", SwingConstants.CENTER);
        msg1.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        msg1.setForeground(new Color(55,65,81));
        msg1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg2 = new JLabel("아래 원문 보기 버튼을 눌러 확인하세요", SwingConstants.CENTER);
        msg2.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        msg2.setForeground(GRAY);
        msg2.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(icon);
        box.add(Box.createVerticalStrut(12));
        box.add(msg1);
        box.add(Box.createVerticalStrut(6));
        box.add(msg2);
        p.add(box);
        return p;
    }

    private void resetDetail() {
        detailPanel.removeAll();
        detailPanel.setLayout(new GridBagLayout());
        detailPanel.setBackground(BG);
        detailPanel.setBorder(new EmptyBorder(0,0,0,0));
        JLabel guide = new JLabel("공지를 선택하세요", SwingConstants.CENTER);
        guide.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        guide.setForeground(GRAY);
        detailPanel.add(guide);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    // ─── 관리자 ───────────────────────────────────────────────
    private void showAdminDialog() {
        String pw = JOptionPane.showInputDialog(this, "관리자 비밀번호:", "관리자 인증", JOptionPane.PLAIN_MESSAGE);
        if (!ADMIN_PASSWORD.equals(pw)) {
            if (pw != null) JOptionPane.showMessageDialog(this, "비밀번호가 틀렸습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        showNoticeInputDialog(null);
    }

    private void showEditDialog(Notice notice) {
        String pw = JOptionPane.showInputDialog(this, "관리자 비밀번호:", "관리자 인증", JOptionPane.PLAIN_MESSAGE);
        if (!ADMIN_PASSWORD.equals(pw)) {
            if (pw != null) JOptionPane.showMessageDialog(this, "비밀번호가 틀렸습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        showNoticeInputDialog(notice);
    }

    private void showNoticeInputDialog(Notice existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "공지 추가" : "공지 수정", true);
        dialog.setSize(440, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(WHITE);
        form.setBorder(new EmptyBorder(20,20,20,20));

        JLabel tagL = new JLabel("태그");
        tagL.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        tagL.setForeground(new Color(55,65,81));

        String[] tagOptions = {"이벤트", "예매", "취소표", "공지", "기타"};
        JComboBox<String> tagBox = new JComboBox<>(tagOptions);
        tagBox.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        tagBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        if (existing != null) tagBox.setSelectedItem(existing.getTag());

        JLabel titleL = new JLabel("제목");
        titleL.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        titleL.setForeground(new Color(55,65,81));

        JTextField titleField = new JTextField(existing != null ? existing.getTitle() : "");
        titleField.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        titleField.setBorder(new CompoundBorder(new LineBorder(new Color(229,231,235),1), new EmptyBorder(6,8,6,8)));
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel contentL = new JLabel("내용");
        contentL.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        contentL.setForeground(new Color(55,65,81));

        JTextArea contentArea = new JTextArea(existing != null ? existing.getContent() : "");
        contentArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(new EmptyBorder(6,8,6,8));

        JScrollPane contentScroll = new JScrollPane(contentArea);
        contentScroll.setBorder(new LineBorder(new Color(229,231,235),1));
        contentScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        form.add(tagL); form.add(Box.createVerticalStrut(4)); form.add(tagBox);
        form.add(Box.createVerticalStrut(12));
        form.add(titleL); form.add(Box.createVerticalStrut(4)); form.add(titleField);
        form.add(Box.createVerticalStrut(12));
        form.add(contentL); form.add(Box.createVerticalStrut(4)); form.add(contentScroll);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        btnPanel.setBackground(LIGHT_BG);
        btnPanel.setBorder(new MatteBorder(1,0,0,0,new Color(229,231,235)));

        JButton cancelBtn = new JButton("취소");
        cancelBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton(existing == null ? "추가" : "저장");
        saveBtn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        saveBtn.setForeground(WHITE);
        saveBtn.setBackground(NAVY);
        saveBtn.setOpaque(true);
        saveBtn.setBorderPainted(false);
        saveBtn.addActionListener(e -> {
            String tag = (String) tagBox.getSelectedItem();
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            if (title.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "제목과 내용을 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (existing == null) NoticeManager.add(tag, title, content);
            else NoticeManager.update(existing.getId(), tag, title, content);
            refreshNoticeList();
            resetDetail();
            dialog.dispose();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel createTagLabel(String tag) {
        JLabel l = new JLabel(tag);
        l.setFont(new Font("맑은 고딕", Font.BOLD, 9));
        l.setBorder(new EmptyBorder(2,6,2,6));
        l.setOpaque(true);
        switch (tag) {
            case "이벤트": l.setBackground(new Color(253,244,255)); l.setForeground(new Color(126,34,206)); break;
            case "예매":   l.setBackground(new Color(254,242,242)); l.setForeground(new Color(153,27,27));  break;
            case "취소표": l.setBackground(new Color(236,253,245)); l.setForeground(new Color(6,95,70));    break;
            case "기타":   l.setBackground(new Color(243,244,246)); l.setForeground(new Color(75,85,99));   break;
            default:       l.setBackground(new Color(239,246,255)); l.setForeground(new Color(29,78,216));  break;
        }
        return l;
    }
}
