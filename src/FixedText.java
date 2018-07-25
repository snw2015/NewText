import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedText extends PrintableText {
    private final String rawText;
    private String text;
    private ArrayList<PrintableChar> chars = new ArrayList<>();
    private boolean isPrepared;

    private final ArrayList<CharStyle> styleList = new ArrayList<>();

    public FixedText(String rawText, int width, CharStyle defaultStyle, int charSpace, int lineSpace, Graphics graphics, boolean fixedAsciiWidth, boolean fixedWidth) {
        super(width, defaultStyle, charSpace, lineSpace, graphics, fixedAsciiWidth, fixedWidth);
        this.rawText = rawText;
        isPrepared = false;
    }

    @Override
    protected @NotNull PrintableChar next(int pos, @Nullable PrintableChar last) {
        //TODO
        if (!isPrepared) prepare();

        return null;
    }

    @Override
    protected boolean hasNext(int pos, @Nullable PrintableChar last, int endY) {
        //TODO
        if (!isPrepared) prepare();


        return false;
    }

    @Override
    protected @Nullable PrintableChar lastInvisible(int startY, AtomicInteger nextPos) {
        //TODO
        if (!isPrepared) prepare();

        return null;
    }

    public void prepare() {
        //TODO
        StyleInterpreter si = new StyleInterpreter(rawText);
        StyleInterpreter.COMMAND command = si.nextCommand();
        StringBuffer sb = new StringBuffer();
        Stack<CharStyle> styleStack = new Stack<>();

        styleList.add(defaultStyle);
        styleStack.add(defaultStyle);
        CharStyle style;

        int[] pos = new int[] {0, 0};

        while (command != StyleInterpreter.COMMAND.END) {
            switch (command) {
                case CHARACTER:
                    sb.append(si.getCharacter());
                    PrintableChar c = new PrintableChar(si.getCharacter(), pos[X], pos[Y], styleStack.peek());
                    chars.add(c);
                    pos = putNewChar(pos, c);
                    break;
                case STYLE_START:
                    style = styleStack.peek().modifyByInfo(si.getStyleInfo());
                    int index;
                    index = styleList.indexOf(style);
                    if (index < 0) {
                        styleList.add(style);
                        index = styleList.size() - 1;
                    }
                    styleStack.push(styleList.get(index));
                    break;
                case STYLE_END:
                    styleStack.pop();
                    style = styleStack.peek();
                    break;
            }
        }

        text = sb.toString();

        isPrepared = true;
    }

    private int[] putNewChar(int[] pos, PrintableChar c) {
        int rightBound = pos[X] + c.getWidth(graphics);
        int[] newPos = new int[2];

        if(rightBound >= width) {
            newPos[X] = 0;
            //TODO
            newPos[Y] = pos[Y]+lineSpace;
        }

        return newPos;
    }

    public String getText() {
        return text;
    }

    public String getRawText() {
        return rawText;
    }
}
