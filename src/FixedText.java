import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedText extends PrintableText {
    public enum ALIGN {
        TOP, BOTTOM, CENTER
    }

    private final String rawText;
    private String text;
    private ArrayList<PrintableChar> chars = new ArrayList<>();
    private boolean isPrepared;
    private ALIGN alignment;

    private final ArrayList<CharStyle> styleList = new ArrayList<>();

    public FixedText(String rawText, int width, CharStyle defaultStyle, int charSpace, int lineSpace, Graphics graphics, ALIGN alignment, boolean fixedAsciiWidth, boolean fixedWidth) {
        super(width, defaultStyle, charSpace, lineSpace, graphics, fixedAsciiWidth, fixedWidth);
        this.rawText = rawText;
        this.alignment = alignment;
        isPrepared = false;
    }

    @Override
    protected @NotNull PrintableChar next(int pos, @Nullable PrintableChar last) {
        if (!isPrepared) prepare();
        return chars.get(pos);
    }

    @Override
    protected boolean hasNext(int pos, @Nullable PrintableChar last, int endY) {
        if (!isPrepared) prepare();
        return pos < chars.size() && (endY < 0 || chars.get(pos).getY() < endY);
    }

    private int lastInvisible(int startY, int l, int r) {
        if (!isPrepared) prepare();

        if (r - l == 1) {
            if (chars.get(l).getY() + chars.get(l).getHeight(graphics) >= startY)
                return l - 1;
            else
                return l;
        }

        int m = (l + r) / 2;
        if (chars.get(m).getY() + chars.get(m).getHeight(graphics) >= startY)
            return lastInvisible(startY, l, m);
        else
            return lastInvisible(startY, m, r);
    }

    @Override
    protected @Nullable PrintableChar lastInvisible(int startY, AtomicInteger nextPos) {
        if (!isPrepared) prepare();
        int pos = lastInvisible(startY, 0, chars.size());
        if (pos < 0) {
            nextPos.set(0);
            return null;
        }

        nextPos.set(pos + 1);
        return chars.get(pos);
    }

    public void prepare() {
        StyleInterpreter si = new StyleInterpreter(rawText);
        StyleInterpreter.COMMAND command = si.nextCommand();
        StringBuffer sb = new StringBuffer();
        Stack<CharStyle> styleStack = new Stack<>();

        styleList.add(defaultStyle);
        styleStack.add(defaultStyle);
        CharStyle style;


        while (command != StyleInterpreter.COMMAND.END) {
            switch (command) {
                case CHARACTER:
                    sb.append(si.getCharacter());
                    PrintableChar c = new PrintableChar(si.getCharacter(), styleStack.peek());
                    chars.add(c);
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
                    break;
            }
            command = si.nextCommand();
        }

        text = sb.toString();

        preparePositions();

        isPrepared = true;
    }

    private void preparePositions() {
        PrintableChar c = chars.get(1), cLast;
        int lastNewline = 0;
        int lastY = 0;
        int maxHeight = 0;

        for (int i = 1; i < chars.size(); i++) {
            cLast = c;
            c = chars.get(i);

            int lastWidth = cLast.getWidth(graphics);
            int curWidth = c.getWidth(graphics);
            if (cLast.getX() + lastWidth + charSpace + curWidth >= width) {
                for (int j = lastNewline; j < i; j++) {
                    setCharY(chars.get(j), lastY, maxHeight);
                }

                c.setX(0);
                lastNewline = i;
                lastY = cLast.getY() + cLast.getHeight(graphics) + lineSpace;
                maxHeight = 0;
            } else {
                int height = c.getHeight(graphics);
                if (height > maxHeight) maxHeight = height;
                c.setX(cLast.getX() + lastWidth + charSpace);
            }
        }

        for (int j = lastNewline; j < chars.size(); j++) {
            setCharY(chars.get(j), lastY, maxHeight);
        }
    }

    private void setCharY(PrintableChar c, int lastY, int maxHeight) {
        switch (alignment) {
            case TOP:
                c.setY(lastY);
                break;
            case CENTER:
                c.setY(lastY + (maxHeight - c.getHeight(graphics)) / 2);
                break;
            case BOTTOM:
                c.setY(lastY + maxHeight);
                break;
            default:
                break;
        }
    }

    public String getText() {
        return text;
    }

    public String getRawText() {
        return rawText;
    }

    public ALIGN getAlignment() {
        return alignment;
    }

    public void setAlignment(ALIGN alignment) {
        this.alignment = alignment;
    }

    public static void main(String[] args) {
        class TFrame extends JFrame {
            PrintableText t;
            JPanel j;

            public TFrame() {
                setBounds(300, 200, 400, 200);
                setDefaultCloseOperation(EXIT_ON_CLOSE);
                setVisible(true);

                t = new FixedText("abcde", 300,
                        new CharStyle(new Font("Consola", Font.BOLD, 20), Color.black),
                        0, 0, getGraphics(), ALIGN.BOTTOM, false, false);
                setFocusTraversalKeysEnabled(false);

            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);

                if (t != null)
                    for (PrintableChar pc : t) {
                        pc.printChar(g, AffineTransform.getTranslateInstance(20, 100));
                    }
            }
        }

        TFrame t = new TFrame();
    }
}
