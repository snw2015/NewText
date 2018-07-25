import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedText extends PrintableText {
    private String rawText;
    private final String text;

    private ArrayList<Integer> stylePositions = new ArrayList<>();
    private ArrayList<CharStyle> styleList = new ArrayList<>();

    public FixedText(String text, int width, CharStyle defaultStyle, int charSpace, int lineSpace, Graphics graphics, boolean fixedAsciiWidth, boolean fixedWidth) {
        super(width, defaultStyle, charSpace, lineSpace, graphics, fixedAsciiWidth, fixedWidth);
        this.text = text;
    }

    @Override
    protected @NotNull PrintableChar next(int pos, @Nullable PrintableChar last) {
        //TODO
        return null;
    }

    @Override
    protected boolean hasNext(int pos, @Nullable PrintableChar last, int endY) {
        //TODO
        return false;
    }

    @Override
    protected @Nullable PrintableChar lastInvisible(int startY, AtomicInteger nextPos) {
        //TODO
        return null;
    }

    public void prepare() {
        //TODO
    }

    public String getText() {
        return text;
    }

    public String getRawText() {
        return rawText;
    }
}
