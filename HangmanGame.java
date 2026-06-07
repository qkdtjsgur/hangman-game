import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class HangmanGame extends JFrame {

    // ── 단어 목록: {단어, 주제} ──
    private static final String[][] WORD_LIST = {
        {"사자", "동물"}, {"호랑이", "동물"}, {"코끼리", "동물"}, {"기린", "동물"},
        {"펭귄", "동물"}, {"돌고래", "동물"}, {"치타", "동물"}, {"늑대", "동물"},
        {"독수리", "동물"}, {"토끼", "동물"}, {"여우", "동물"}, {"곰", "동물"},
        {"사과", "과일"}, {"바나나", "과일"}, {"포도", "과일"}, {"수박", "과일"},
        {"딸기", "과일"}, {"복숭아", "과일"}, {"키위", "과일"}, {"망고", "과일"},
        {"하늘", "자연"}, {"바다", "자연"}, {"산", "자연"}, {"강", "자연"},
        {"구름", "자연"}, {"바람", "자연"}, {"폭포", "자연"}, {"숲", "자연"},
        {"서울", "도시"}, {"부산", "도시"}, {"인천", "도시"}, {"대전", "도시"},
        {"축구", "스포츠"}, {"야구", "스포츠"}, {"농구", "스포츠"}, {"수영", "스포츠"},
        {"태권도", "스포츠"}, {"배드민턴", "스포츠"}, {"탁구", "스포츠"},
        {"김치", "음식"}, {"비빔밥", "음식"}, {"삼겹살", "음식"}, {"떡볶이", "음식"},
        {"냉면", "음식"}, {"순대", "음식"}, {"갈비탕", "음식"}, {"된장찌개", "음식"},
        {"피아노", "악기"}, {"기타", "악기"}, {"바이올린", "악기"}, {"드럼", "악기"},
        {"봄", "계절"}, {"여름", "계절"}, {"가을", "계절"}, {"겨울", "계절"},
    };

    private static final int MAX_WRONG = 6;

    // 한글 자모 분해용
    private static final char[] CHO  = {'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ',
                                         'ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'};
    private static final char[] JUNG = {'ㅏ','ㅐ','ㅑ','ㅒ','ㅓ','ㅔ','ㅕ','ㅖ','ㅗ',
                                         'ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ'};
    private static final char[] JONG = {0,'ㄱ','ㄲ','ㄳ','ㄴ','ㄵ','ㄶ','ㄷ','ㄹ','ㄺ',
                                         'ㄻ','ㄼ','ㄽ','ㄾ','ㄿ','ㅀ','ㅁ','ㅂ','ㅄ','ㅅ',
                                         'ㅆ','ㅇ','ㅈ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'};

    // ── 게임 상태 ──
    private String currentWord;
    private String currentTopic;
    private List<Character> decomposed; // 분해된 자모 리스트
    private Set<Character> guessed;     // 맞춘 자모
    private Set<Character> wrongSet;    // 틀린 자모
    private int wrongCount;
    private boolean gameOver;

    // ── UI 컴포넌트 ──
    private JLabel topicLabel;       // "동물, 3글자"
    private JLabel livesLabel;       // "남은 기회: N번"
    private JPanel jamoPanel;        // 자모 칸들
    private JPanel keyboardPanel;    // 자모 키보드
    private JLabel statusLabel;      // 정답/실패 메시지
    private Map<Character, JButton> keyButtons = new HashMap<>();

    // 키보드 배열
    private static final char[][] KB_ROWS = {
        {'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ'},
        {'ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'},
        {'ㅏ','ㅐ','ㅑ','ㅒ','ㅓ','ㅔ','ㅕ','ㅖ','ㅗ'},
        {'ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ'}
    };

    public HangmanGame() {
        setTitle("🎯 한국어 행맨");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initUI();
        newGame();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── 한글 분해 ──
    private List<Character> decomposeWord(String word) {
        List<Character> list = new ArrayList<>();
        for (char c : word.toCharArray()) {
            if (c >= 0xAC00 && c <= 0xD7A3) {
                int off = c - 0xAC00;
                int ci  = off / (21 * 28);
                int vi  = (off % (21 * 28)) / 28;
                int ji  = off % 28;
                list.add(CHO[ci]);
                list.add(JUNG[vi]);
                if (ji != 0) list.add(JONG[ji]);
            } else {
                list.add(c); // 공백 등
            }
        }
        return list;
    }

    // ── UI 초기화 ──
    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(30, 30, 40));

        // ── 상단: 주제 + 남은기회 ──
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(25, 25, 35));
        topPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        topicLabel = new JLabel("", SwingConstants.LEFT);
        topicLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        topicLabel.setForeground(new Color(230, 200, 80));

        livesLabel = new JLabel("", SwingConstants.RIGHT);
        livesLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        livesLabel.setForeground(new Color(100, 220, 160));

        topPanel.add(topicLabel, BorderLayout.WEST);
        topPanel.add(livesLabel, BorderLayout.EAST);

        // ── 중앙: 자모 칸 + 상태 메시지 ──
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setBackground(new Color(30, 30, 40));
        centerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        // 자모 칸 (스크롤 가능한 패널로 감쌈)
        jamoPanel = new JPanel();
        jamoPanel.setBackground(new Color(30, 30, 40));
        jamoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 6));

        JScrollPane jamoScroll = new JScrollPane(jamoPanel,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jamoScroll.setBorder(null);
        jamoScroll.setBackground(new Color(30, 30, 40));
        jamoScroll.setPreferredSize(new Dimension(600, 90));

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        statusLabel.setForeground(new Color(230, 100, 100));

        centerPanel.add(jamoScroll, BorderLayout.CENTER);
        centerPanel.add(statusLabel, BorderLayout.SOUTH);

        // ── 하단: 키보드 ──
        keyboardPanel = new JPanel();
        keyboardPanel.setBackground(new Color(20, 20, 30));
        keyboardPanel.setBorder(new EmptyBorder(10, 16, 16, 16));
        keyboardPanel.setLayout(new BoxLayout(keyboardPanel, BoxLayout.Y_AXIS));

        // 새 게임 버튼
        JButton newGameBtn = new JButton("새 게임");
        newGameBtn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        newGameBtn.setBackground(new Color(80, 60, 160));
        newGameBtn.setForeground(Color.WHITE);
        newGameBtn.setFocusPainted(false);
        newGameBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        newGameBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        newGameBtn.addActionListener(e -> newGame());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(new Color(20, 20, 30));
        btnRow.add(newGameBtn);

        // ── 조립 ──
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(20, 20, 30));
        bottomPanel.add(keyboardPanel, BorderLayout.CENTER);
        bottomPanel.add(btnRow, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ── 새 게임 ──
    private void newGame() {
        // 랜덤 단어 선택
        Random rnd = new Random();
        String[] entry = WORD_LIST[rnd.nextInt(WORD_LIST.length)];
        currentWord  = entry[0];
        currentTopic = entry[1];

        decomposed = decomposeWord(currentWord);
        guessed    = new HashSet<>();
        wrongSet   = new HashSet<>();
        wrongCount = 0;
        gameOver   = false;

        // 주제 + 글자수 라벨
        topicLabel.setText(currentTopic + ",  " + currentWord.length() + "글자");
        updateLivesLabel();

        // 자모 칸 재구성
        rebuildJamoPanel();

        // 키보드 재구성
        rebuildKeyboard();

        statusLabel.setText(" ");
    }

    private void updateLivesLabel() {
        int remaining = MAX_WRONG - wrongCount;
        livesLabel.setText("남은 기회: " + remaining + "번");
        if (remaining <= 2)
            livesLabel.setForeground(new Color(230, 80, 80));
        else if (remaining <= 4)
            livesLabel.setForeground(new Color(230, 180, 60));
        else
            livesLabel.setForeground(new Color(100, 220, 160));
    }

    // ── 자모 칸 패널 ──
    private void rebuildJamoPanel() {
        jamoPanel.removeAll();

        // 단어 글자별로 그룹 생성
        for (char c : currentWord.toCharArray()) {
            List<Character> charJamos = decomposeWord(String.valueOf(c));
            JPanel charGroup = buildCharGroup(c, charJamos);
            jamoPanel.add(charGroup);
        }

        jamoPanel.revalidate();
        jamoPanel.repaint();
    }

    // 한 글자(여러 자모)를 묶는 그룹 패널
    private JPanel buildCharGroup(char fullChar, List<Character> jamos) {
        JPanel group = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        group.setBackground(new Color(30, 30, 40));

        for (char jamo : jamos) {
            JLabel lbl = createJamoLabel(jamo);
            group.add(lbl);
        }
        return group;
    }

    private JLabel createJamoLabel(char jamo) {
        boolean revealed = guessed.contains(jamo);
        JLabel lbl = new JLabel(revealed ? String.valueOf(jamo) : "_", SwingConstants.CENTER);
        lbl.setName("jamo_" + jamo);
        lbl.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        lbl.setForeground(revealed ? new Color(230, 210, 80) : new Color(160, 160, 200));
        lbl.setPreferredSize(new Dimension(38, 52));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(40, 40, 55));
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 100), 2, true),
            new EmptyBorder(2, 2, 2, 2)
        ));
        return lbl;
    }

    // 자모 라벨 일괄 갱신 (패널 재구성 없이)
    private void refreshJamoPanel() {
        for (Component comp : jamoPanel.getComponents()) {
            if (comp instanceof JPanel group) {
                for (Component inner : group.getComponents()) {
                    if (inner instanceof JLabel lbl) {
                        String name = lbl.getName();
                        if (name != null && name.startsWith("jamo_")) {
                            char jamo = name.charAt(5);
                            boolean revealed = guessed.contains(jamo);
                            lbl.setText(revealed ? String.valueOf(jamo) : "_");
                            lbl.setForeground(revealed
                                ? new Color(230, 210, 80)
                                : new Color(160, 160, 200));
                        }
                    }
                }
            }
        }
        jamoPanel.repaint();
    }

    // ── 키보드 패널 ──
    private void rebuildKeyboard() {
        keyboardPanel.removeAll();
        keyButtons.clear();

        for (char[] row : KB_ROWS) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 3));
            rowPanel.setBackground(new Color(20, 20, 30));
            rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            for (char key : row) {
                JButton btn = createKeyButton(key);
                keyButtons.put(key, btn);
                rowPanel.add(btn);
            }
            keyboardPanel.add(rowPanel);
        }

        keyboardPanel.revalidate();
        keyboardPanel.repaint();
    }

    private JButton createKeyButton(char jamo) {
        JButton btn = new JButton(String.valueOf(jamo));
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        btn.setPreferredSize(new Dimension(46, 42));
        btn.setBackground(new Color(55, 55, 75));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 110), 1, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> onGuess(jamo));
        return btn;
    }

    // ── 추측 처리 ──
    private void onGuess(char jamo) {
        if (gameOver) return;
        if (guessed.contains(jamo) || wrongSet.contains(jamo)) return;

        boolean hit = decomposed.contains(jamo);

        if (hit) {
            guessed.add(jamo);
            refreshJamoPanel();
            // 키 초록색
            styleKey(jamo, new Color(40, 160, 100), Color.WHITE);

            if (isWordComplete()) {
                gameOver = true;
                statusLabel.setText("🎉 정답! 단어: " + currentWord);
                statusLabel.setForeground(new Color(80, 220, 130));
                disableAllKeys();
            }
        } else {
            wrongSet.add(jamo);
            wrongCount++;
            updateLivesLabel();
            // 키 빨간색
            styleKey(jamo, new Color(160, 50, 50), new Color(200, 200, 200));

            if (wrongCount >= MAX_WRONG) {
                gameOver = true;
                // 정답 전부 공개
                guessed.addAll(decomposed);
                refreshJamoPanel();
                statusLabel.setText("💀 실패! 정답: " + currentWord);
                statusLabel.setForeground(new Color(230, 80, 80));
                disableAllKeys();
            }
        }
    }

    private void styleKey(char jamo, Color bg, Color fg) {
        JButton btn = keyButtons.get(jamo);
        if (btn != null) {
            btn.setBackground(bg);
            btn.setForeground(fg);
            btn.setEnabled(false);
        }
    }

    private void disableAllKeys() {
        for (JButton btn : keyButtons.values()) btn.setEnabled(false);
    }

    private boolean isWordComplete() {
        for (char jamo : decomposed) {
            if (!guessed.contains(jamo)) return false;
        }
        return true;
    }

    // ── 진입점 ──
    public static void main(String[] args) {
        // 한글 폰트 설정
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(HangmanGame::new);
    }
}
