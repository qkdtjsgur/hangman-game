import java.util.ArrayList;
import java.util.List;

/**
 * Word 클래스 - 단어 하나를 표현하는 객체
 * 객체지향: 캡슐화 (private 필드 + getter)
 */
public class Word {

    private static final char[] CHO = {
        'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ',
        'ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
    };
    private static final char[] JUNG = {
        'ㅏ','ㅐ','ㅑ','ㅒ','ㅓ','ㅔ','ㅕ','ㅖ','ㅗ',
        'ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ'
    };
    private static final char[] JONG = {
        0,'ㄱ','ㄲ','ㄳ','ㄴ','ㄵ','ㄶ','ㄷ','ㄹ','ㄺ',
        'ㄻ','ㄼ','ㄽ','ㄾ','ㄿ','ㅀ','ㅁ','ㅂ','ㅄ','ㅅ',
        'ㅆ','ㅇ','ㅈ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
    };

    private final String text;
    private final String topic;
    private final List<Character> allJamos;
    private final List<List<Character>> charJamos;

    public Word(String text, String topic) {
        this.text      = text;
        this.topic     = topic;
        this.allJamos  = decompose(text);
        this.charJamos = decomposeByChar(text);
    }

    private List<Character> decompose(String word) {
        List<Character> result = new ArrayList<>();
        for (char c : word.toCharArray()) {
            if (c >= 0xAC00 && c <= 0xD7A3) {
                int off = c - 0xAC00;
                result.add(CHO[off / (21 * 28)]);
                result.add(JUNG[(off % (21 * 28)) / 28]);
                int ji = off % 28;
                if (ji != 0) result.add(JONG[ji]);
            } else {
                result.add(c);
            }
        }
        return result;
    }

    private List<List<Character>> decomposeByChar(String word) {
        List<List<Character>> result = new ArrayList<>();
        for (char c : word.toCharArray()) {
            List<Character> jamos = new ArrayList<>();
            if (c >= 0xAC00 && c <= 0xD7A3) {
                int off = c - 0xAC00;
                jamos.add(CHO[off / (21 * 28)]);
                jamos.add(JUNG[(off % (21 * 28)) / 28]);
                int ji = off % 28;
                if (ji != 0) jamos.add(JONG[ji]);
            } else {
                jamos.add(c);
            }
            result.add(jamos);
        }
        return result;
    }

    public String getText()                      { return text; }
    public String getTopic()                     { return topic; }
    public int    getLength()                    { return text.length(); }
    public List<Character> getAllJamos()         { return allJamos; }
    public List<List<Character>> getCharJamos() { return charJamos; }

    @Override
    public String toString() {
        return "Word{text='" + text + "', topic='" + topic + "'}";
    }
}
