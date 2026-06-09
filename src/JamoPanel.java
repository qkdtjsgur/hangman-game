import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * JamoPanel 클래스 - 자모 칸 표시 패널
 * 상속: JPanel extends
 * 현재 단어의 자모를 칸으로 표시, 맞춘 자모는 공개
 */
public class JamoPanel extends JPanel {

    // 색상 상수
    private static final Color BG          = new Color(30, 30, 45);
    private static final Color TILE_BG     = new Color(40, 40, 60);
    private static final Color TILE_BORDER = new Color(70, 70, 100);
    private static final Color HIDDEN_FG   = new Color(130, 130, 170);
    private static final Color REVEAL_FG   = new Color(245, 200, 66);
    private static final Color REVEAL_BOR  = new Color(245, 200, 66);

    public JamoPanel() {
        setBackground(BG);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    /**
     * 단어와 맞춘 자모 세트를 받아 칸 재구성
     * @param charJamos 글자별 자모 리스트
     * @param guessed   이미 맞춘 자모 집합
     */
    public void update(List<List<Character>> charJamos, Set<Character> guessed) {
        removeAll();

        for (int i = 0; i < charJamos.size(); i++) {
            List<Character> jamos = charJamos.get(i);

            // 한 글자를 감싸는 그룹 패널
            JPanel group = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
            group.setBackground(BG);

            for (char jamo : jamos) {
                group.add(buildTile(jamo, guessed.contains(jamo)));
            }
            add(group);

            // 글자 사이 구분 여백
            if (i < charJamos.size() - 1) {
                JLabel sep = new JLabel("  ");
                sep.setPreferredSize(new Dimension(8, 52));
                add(sep);
            }
        }

        revalidate();
        repaint();
    }

    /** 자모 타일 하나 생성 */
    private JLabel buildTile(char jamo, boolean revealed) {
        JLabel tile = new JLabel(revealed ? String.valueOf(jamo) : "_", SwingConstants.CENTER);
        tile.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        tile.setPreferredSize(new Dimension(42, 52));
        tile.setOpaque(true);
        tile.setBackground(TILE_BG);

        if (revealed) {
            tile.setForeground(REVEAL_FG);
            tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(REVEAL_BOR, 2, true),
                new EmptyBorder(2, 2, 2, 2)
            ));
        } else {
            tile.setForeground(HIDDEN_FG);
            tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TILE_BORDER, 2, true),
                new EmptyBorder(2, 2, 2, 2)
            ));
        }
        return tile;
    }
}
