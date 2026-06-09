import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * KeyboardPanel 클래스 - 자모 키보드 패널
 * 상속: JPanel extends
 * 자음/모음 버튼 배치, 맞춤/틀림 색상 표시
 */
public class KeyboardPanel extends JPanel {

    // 키보드 배열
    private static final char[][] KB_ROWS = {
        {'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ'},
        {'ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'},
        {'ㅏ','ㅐ','ㅑ','ㅒ','ㅓ','ㅔ','ㅕ','ㅖ','ㅗ'},
        {'ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ'}
    };

    // 색상
    private static final Color BG_PANEL  = new Color(22, 22, 36);
    private static final Color KEY_BG    = new Color(50, 50, 75);
    private static final Color KEY_FG    = new Color(230, 230, 230);
    private static final Color HIT_BG    = new Color(35, 140, 80);
    private static final Color MISS_BG   = new Color(130, 40, 40);
    private static final Color MISS_FG   = new Color(180, 130, 130);
    private static final Color LABEL_FG  = new Color(140, 140, 180);

    private final Map<Character, JButton> keyMap = new HashMap<>();

    // 키 클릭 이벤트 전달용 인터페이스
    public interface KeyListener {
        void onKeyPressed(char jamo);
    }
    private KeyListener keyListener;

    public KeyboardPanel() {
        setBackground(BG_PANEL);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(8, 10, 10, 10));
        buildKeyboard();
    }

    public void setKeyListener(KeyListener listener) {
        this.keyListener = listener;
    }

    private void buildKeyboard() {
        // 자음 섹션
        add(buildSectionLabel("자음"));
        add(buildRow(KB_ROWS[0]));
        add(buildRow(KB_ROWS[1]));
        add(Box.createVerticalStrut(8));
        // 모음 섹션
        add(buildSectionLabel("모음"));
        add(buildRow(KB_ROWS[2]));
        add(buildRow(KB_ROWS[3]));
    }

    private JLabel buildSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        lbl.setForeground(LABEL_FG);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(2, 4, 2, 4));
        return lbl;
    }

    private JPanel buildRow(char[] keys) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 3));
        row.setBackground(BG_PANEL);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        for (char key : keys) {
            JButton btn = createKeyButton(key);
            keyMap.put(key, btn);
            row.add(btn);
        }
        return row;
    }

    private JButton createKeyButton(char jamo) {
        JButton btn = new JButton(String.valueOf(jamo));
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        btn.setPreferredSize(new Dimension(44, 40));
        btn.setBackground(KEY_BG);
        btn.setForeground(KEY_FG);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 120), 1, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (keyListener != null) keyListener.onKeyPressed(jamo);
        });
        return btn;
    }

    /** 맞춘 자모 키 초록색으로 */
    public void markHit(char jamo) {
        JButton btn = keyMap.get(jamo);
        if (btn != null) {
            btn.setBackground(HIT_BG);
            btn.setEnabled(false);
        }
    }

    /** 틀린 자모 키 빨간색으로 */
    public void markMiss(char jamo) {
        JButton btn = keyMap.get(jamo);
        if (btn != null) {
            btn.setBackground(MISS_BG);
            btn.setForeground(MISS_FG);
            btn.setEnabled(false);
        }
    }

    /** 키보드 전체 초기화 */
    public void reset() {
        for (JButton btn : keyMap.values()) {
            btn.setBackground(KEY_BG);
            btn.setForeground(KEY_FG);
            btn.setEnabled(true);
        }
    }
}
