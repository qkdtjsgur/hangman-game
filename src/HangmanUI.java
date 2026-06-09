import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * HangmanUI 클래스 - 메인 창
 * 상속: JFrame extends
 * Game.GameListener 구현: 게임 이벤트 수신
 */
public class HangmanUI extends JFrame implements Game.GameListener {

    // 색상
    private static final Color BG_DARK    = new Color(20, 20, 32);
    private static final Color BG_CARD    = new Color(28, 28, 44);
    private static final Color BG_HEADER  = new Color(18, 18, 30);
    private static final Color FG_YELLOW  = new Color(245, 200, 66);
    private static final Color FG_GREEN   = new Color(46, 204, 113);
    private static final Color FG_RED     = new Color(231, 76, 60);
    private static final Color FG_MUTED   = new Color(140, 140, 180);
    private static final Color ACCENT     = new Color(127, 90, 240);

    // 게임 객체
    private final Game game;

    // UI 컴포넌트
    private JLabel    topicLabel;
    private JLabel    livesLabel;
    private JamoPanel jamoPanel;
    private KeyboardPanel keyboardPanel;
    private JLabel    statusLabel;
    private JPanel    wrongListPanel;

    // 결과 표시 후 자동 새 게임을 위한 쓰레드
    private GameThread autoNextThread;

    public HangmanUI() {
        super("한국어 행맨");
        this.game = new Game();
        this.game.setListener(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initUI();
        startNewGame();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── UI 초기화 ──
    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildCenter(),  BorderLayout.CENTER);
        root.add(buildKeyboard(), BorderLayout.EAST);

        setContentPane(root);
    }

    /** 헤더: 타이틀 + 새 게임 버튼 */
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_HEADER);
        panel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("한국어 행맨");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        title.setForeground(FG_YELLOW);

        JButton newBtn = new JButton("새 게임");
        newBtn.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        newBtn.setBackground(ACCENT);
        newBtn.setForeground(Color.WHITE);
        newBtn.setFocusPainted(false);
        newBtn.setBorder(new EmptyBorder(7, 18, 7, 18));
        newBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        newBtn.addActionListener(e -> startNewGame());

        panel.add(title,  BorderLayout.WEST);
        panel.add(newBtn, BorderLayout.EAST);
        return panel;
    }

    /** 중앙: 주제/기회 정보 + 자모 칸 + 상태 + 틀린 목록 */
    private JPanel buildCenter() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_DARK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // 정보 카드 (주제 + 남은기회)
        JPanel infoCard = new JPanel(new BorderLayout());
        infoCard.setBackground(BG_CARD);
        infoCard.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90), 1),
            new EmptyBorder(14, 18, 14, 18)
        ));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        topicLabel = new JLabel(" ");
        topicLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        topicLabel.setForeground(FG_YELLOW);

        livesLabel = new JLabel(" ");
        livesLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        livesLabel.setForeground(FG_GREEN);
        livesLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        infoCard.add(topicLabel,  BorderLayout.WEST);
        infoCard.add(livesLabel,  BorderLayout.EAST);

        // 자모 패널
        jamoPanel = new JamoPanel();
        jamoPanel.setPreferredSize(new Dimension(560, 100));
        jamoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // 상태 메시지
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        statusLabel.setForeground(FG_GREEN);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 틀린 자모
        JPanel wrongCard = new JPanel(new BorderLayout(0, 6));
        wrongCard.setBackground(BG_CARD);
        wrongCard.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90), 1),
            new EmptyBorder(10, 14, 10, 14)
        ));
        wrongCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel wrongTitle = new JLabel("틀린 자모");
        wrongTitle.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        wrongTitle.setForeground(FG_MUTED);

        wrongListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        wrongListPanel.setBackground(BG_CARD);

        wrongCard.add(wrongTitle,    BorderLayout.NORTH);
        wrongCard.add(wrongListPanel, BorderLayout.CENTER);

        panel.add(infoCard);
        panel.add(Box.createVerticalStrut(10));
        panel.add(jamoPanel);
        panel.add(Box.createVerticalStrut(6));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(wrongCard);

        return panel;
    }

    /** 오른쪽: 자모 키보드 */
    private KeyboardPanel buildKeyboard() {
        keyboardPanel = new KeyboardPanel();
        keyboardPanel.setPreferredSize(new Dimension(240, 0));
        keyboardPanel.setKeyListener(jamo -> game.guess(jamo));
        return keyboardPanel;
    }

    // ── 게임 제어 ──
    private void startNewGame() {
        // 진행 중인 자동 새 게임 쓰레드 취소
        if (autoNextThread != null) autoNextThread.cancel();

        game.start();
        Word word = game.getCurrentWord();

        topicLabel.setText(word.getTopic() + ",  " + word.getLength() + "글자");
        updateLivesLabel();
        statusLabel.setText(" ");
        statusLabel.setForeground(FG_GREEN);

        jamoPanel.update(word.getCharJamos(), game.getGuessed());
        wrongListPanel.removeAll();
        wrongListPanel.revalidate();
        wrongListPanel.repaint();
        keyboardPanel.reset();
    }

    private void updateLivesLabel() {
        int rem = game.getRemaining();
        livesLabel.setText("남은 기회: " + rem + "번");
        if      (rem <= 2) livesLabel.setForeground(FG_RED);
        else if (rem <= 4) livesLabel.setForeground(new Color(230, 160, 40));
        else               livesLabel.setForeground(FG_GREEN);
    }

    // ── Game.GameListener 구현 ──

    @Override
    public void onCorrectGuess(char jamo) {
        jamoPanel.update(game.getCurrentWord().getCharJamos(), game.getGuessed());
        keyboardPanel.markHit(jamo);
    }

    @Override
    public void onWrongGuess(char jamo, int wrongCount, int remaining) {
        updateLivesLabel();
        keyboardPanel.markMiss(jamo);

        // 틀린 자모 뱃지 추가
        JLabel badge = new JLabel(String.valueOf(jamo));
        badge.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        badge.setForeground(FG_RED);
        badge.setOpaque(true);
        badge.setBackground(new Color(60, 20, 20));
        badge.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(FG_RED, 1),
            new EmptyBorder(2, 8, 2, 8)
        ));
        wrongListPanel.add(badge);
        wrongListPanel.revalidate();
        wrongListPanel.repaint();
    }

    @Override
    public void onGameWin(Word word) {
        jamoPanel.update(word.getCharJamos(), game.getGuessed());
        statusLabel.setText("🎉 정답!  '" + word.getText() + "'");
        statusLabel.setForeground(FG_GREEN);

        // 2초 후 자동 새 게임 (GameThread 활용)
        autoNextThread = new GameThread(2500, this::startNewGame);
        autoNextThread.start();
    }

    @Override
    public void onGameLose(Word word) {
        jamoPanel.update(word.getCharJamos(), game.getGuessed());
        statusLabel.setText("💀 실패!  정답: " + word.getText());
        statusLabel.setForeground(FG_RED);

        autoNextThread = new GameThread(3000, this::startNewGame);
        autoNextThread.start();
    }
}
