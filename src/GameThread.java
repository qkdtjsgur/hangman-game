/**
 * GameThread 클래스 - 쓰레드 활용
 * Thread 상속: 결과 표시 후 일정 시간 뒤 자동 새 게임 딜레이 처리
 * UI를 블로킹하지 않고 백그라운드에서 대기
 */
public class GameThread extends Thread {

    public interface Callback {
        void run();
    }

    private final int      delayMs;   // 대기 시간 (밀리초)
    private final Callback callback;  // 대기 후 실행할 작업
    private volatile boolean cancelled = false;

    public GameThread(int delayMs, Callback callback) {
        this.delayMs  = delayMs;
        this.callback = callback;
        setDaemon(true); // 메인 창 닫히면 같이 종료
    }

    @Override
    public void run() {
        try {
            Thread.sleep(delayMs);
            if (!cancelled) {
                // Swing UI는 EDT에서 실행해야 함
                javax.swing.SwingUtilities.invokeLater(callback::run);
            }
        } catch (InterruptedException e) {
            // 취소됨
        }
    }

    /** 쓰레드 취소 */
    public void cancel() {
        cancelled = true;
        interrupt();
    }
}
