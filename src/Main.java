import javax.swing.SwingUtilities;

/**
 * Main 클래스 - 진입점
 * SwingUtilities.invokeLater: Swing UI는 EDT(Event Dispatch Thread)에서 실행
 */
public class Main {
    public static void main(String[] args) {
        // Swing 컴포넌트는 반드시 EDT에서 생성/수정해야 함
        SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {}
            new HangmanUI();
        });
    }
}
