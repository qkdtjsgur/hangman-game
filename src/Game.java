import java.util.HashSet;
import java.util.Set;

/**
 * Game 클래스 - 게임 로직 담당
 * 객체지향: 게임 상태 캡슐화, UI와 로직 분리
 */
public class Game {

    public static final int MAX_WRONG = 6;

    // 게임 상태 열거형
    public enum State { PLAYING, WIN, LOSE }

    private final WordBank wordBank;
    private Word    currentWord;
    private Set<Character> guessed;   // 맞춘 자모
    private Set<Character> wrongSet;  // 틀린 자모
    private int     wrongCount;
    private State   state;

    // 게임 이벤트를 UI에 전달하는 리스너 인터페이스
    public interface GameListener {
        void onCorrectGuess(char jamo);
        void onWrongGuess(char jamo, int wrongCount, int remaining);
        void onGameWin(Word word);
        void onGameLose(Word word);
    }

    private GameListener listener;

    public Game() {
        this.wordBank = new WordBank();
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    /** 새 게임 시작 */
    public void start() {
        currentWord = wordBank.getRandomWord();
        guessed     = new HashSet<>();
        wrongSet    = new HashSet<>();
        wrongCount  = 0;
        state       = State.PLAYING;
    }

    /** 자모 추측 */
    public void guess(char jamo) {
        if (state != State.PLAYING) return;
        if (guessed.contains(jamo) || wrongSet.contains(jamo)) return;

        if (currentWord.getAllJamos().contains(jamo)) {
            // 정답
            guessed.add(jamo);
            if (listener != null) listener.onCorrectGuess(jamo);

            if (isComplete()) {
                state = State.WIN;
                if (listener != null) listener.onGameWin(currentWord);
            }
        } else {
            // 오답
            wrongSet.add(jamo);
            wrongCount++;
            int remaining = MAX_WRONG - wrongCount;
            if (listener != null) listener.onWrongGuess(jamo, wrongCount, remaining);

            if (wrongCount >= MAX_WRONG) {
                state = State.LOSE;
                // 정답 전부 공개
                guessed.addAll(currentWord.getAllJamos());
                if (listener != null) listener.onGameLose(currentWord);
            }
        }
    }

    /** 모든 자모를 맞췄는지 확인 */
    private boolean isComplete() {
        return guessed.containsAll(currentWord.getAllJamos());
    }

    // ── Getter ──
    public Word    getCurrentWord() { return currentWord; }
    public Set<Character> getGuessed()  { return guessed; }
    public Set<Character> getWrongSet() { return wrongSet; }
    public int     getWrongCount()  { return wrongCount; }
    public int     getRemaining()   { return MAX_WRONG - wrongCount; }
    public State   getState()       { return state; }
    public boolean isAlreadyGuessed(char jamo) {
        return guessed.contains(jamo) || wrongSet.contains(jamo);
    }
}
