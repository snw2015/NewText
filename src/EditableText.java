import javax.naming.SizeLimitExceededException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

//TODO: tab and newline
//TODO: some getters will not try to buffer the data

/**
 * If not given, the ASCII sample will be set to the widest characters of all <b> ASCII printable characters<b/>,
 * excluding the extended ASCII codes.
 */
public class EditableText extends PrintableText {
    private String text;

    private PrintableChar bufferedNext = null;

    private final int[] asciiWidths = new int[ASCII_RANGE];

    {
        Arrays.fill(asciiWidths, UNDEFINED);
    }

    private int doubleSize = UNDEFINED;

    public EditableText(String text, int width, CharStyle defaultStyle, int charSpace, int lineSpace, Graphics graphics,
                        boolean fixedAsciiWidth, boolean fixedWidth) {
        super(width, defaultStyle, charSpace, lineSpace, graphics, fixedAsciiWidth, fixedWidth);
        this.text = text;
    }

    public EditableText(String text, int width, CharStyle defaultStyle, int charSpace, int lineSpace, Graphics graphics) {
        this(text, width, defaultStyle, charSpace, lineSpace, graphics, true, false);
    }

    @Override
    protected PrintableChar next(int pos, PrintableChar last) {
        if (bufferedNext == null) hasNext(pos, last, -1);
        PrintableChar pc = bufferedNext;
        bufferedNext = null;

        return pc;
    }

    private int deltaX(PrintableChar last) {
        return last.getX() + getCharWidth(last.getValue()) + charSpace;
    }

    private int deltaY(PrintableChar last) {
        return last.getY() + getLineHeight() + lineSpace;
    }

    @Override
    protected boolean hasNext(int pos, PrintableChar last, int endY) {
        //TODO last == null while pos > 0

        if (bufferedNext != null) return true;
        if (pos >= text.length()) return false;
        char c = text.charAt(pos);
        int[] newPos = nextPos(last, c);

        bufferedNext = new PrintableChar(c, newPos[X], newPos[Y], defaultStyle);
        if (endY >= 0 && newPos[Y] >= endY) return false;
        return true;
    }

    private int[] nextPos(PrintableChar last, char c) {
        if (last == null) return new int[]{0, 0};

        int x, y;
        switch (c) {
            case '\n':
                x = -charSpace - getCharWidth(c);
                y = deltaY(last);
                break;
            default:
                x = deltaX(last);
                if (x + getCharWidth(c) >= width) {
                    x = 0;
                    y = deltaY(last);
                } else
                    y = last.getY();
                break;
        }

        return new int[]{x, y};
    }

    @Override
    protected PrintableChar lastInvisible(int startY, AtomicInteger nextPos) {
        //TODO: straightforward method currently
        //TODO: reuse other method

        int pos = 0;
        int x = 0, y = 0;
        char c = ' ';

        if (y + getLineHeight() >= startY) return null;

        while (y + getLineHeight() < startY) {
            c = text.charAt(pos);
            switch (c) {
                case '\n':
                    x = -lineSpace - getCharWidth(c);
                    y += getLineHeight() + lineSpace;
                    break;
                default:
                    x += getCharWidth(c) + charSpace;
                    if (x + getCharWidth(c) >= width) {
                        x = 0;
                        y += getLineHeight() + lineSpace;
                    }
                    break;
            }

            pos++;
        }

        nextPos.set(pos);
        return new PrintableChar(c, x, y, defaultStyle);
    }

    private int getDoubleSize() {
        if (doubleSize == UNDEFINED)
            doubleSize = defaultStyle.getWidth(nonAsciiSample, graphics);
        return doubleSize;
    }

    private int getAsciiWidth(char charCode) {
        if (fixedAsciiWidth) {
            if (asciiWidth == UNDEFINED) {
                //TODO: readability
                for (char c = 32; c < 127; c++) { // for all ASCII printable characters
                    int w = defaultStyle.getWidth(c, graphics);
                    if (asciiWidth < w) {
                        asciiWidth = w;
                        asciiSample = c;
                    }
                }
            }
            return asciiWidth;
        }

        if (asciiWidths[charCode] == UNDEFINED)
            asciiWidths[charCode] = defaultStyle.getWidth(charCode, graphics);
        return asciiWidths[charCode];
    }

    /**
     * If FontMetrics.charWidth works properly (it depends on the Graphics' implementation),
     * this function will not return UNDEFINED.
     *
     * @param charCode
     * @return
     */
    private int getCharWidth(char charCode) {
        if (charCode == '\t') {
            if (fixedWidth)
                return (int) (getDoubleSize() * tabRatio);
            else
                return (int) ((getAsciiWidth(asciiSample) + charSpace) * tabRatio);
        }

        if (fixedWidth) {
            return getDoubleSize();
        } else if (charCode < ASCII_RANGE) {
            if (!fixedAsciiWidth)
                return getAsciiWidth(charCode);
            else
                return getAsciiWidth(asciiSample);
        } else {
            return getDoubleSize();
        }
    }

    public void prepareSizes() {
        for (char i = 0; i < ASCII_RANGE; i++) {
            getAsciiWidth(i);
        }
        getDoubleSize();
        getLineHeight();
    }

    public void insert(int pos, String str) {
        if (pos <= 0)
            text = str + text;
        else if (pos >= text.length())
            text = text + str;
        else
            text = text.substring(0, pos) + str + text.substring(pos);
    }

    public void insert(int pos, char charCode) {
        insert(pos, "" + charCode);
    }

    public void append(String str) {
        insert(text.length(), str);
    }

    public void append(char charCode) {
        insert(text.length(), charCode);
    }

    public void remove(int start, int end) {
        if (start < end && start >= 0 && end > text.length()) try {
            throw new SizeLimitExceededException("Try to remove the characters outside the text.");
        } catch (SizeLimitExceededException e) {
            e.printStackTrace();
        }
        text = text.substring(0, start) + text.substring(end);
    }

    public void remove(int pos) {
        remove(pos, pos + 1);
    }

    public void remove() {
        remove(text.length() - 1);
    }

    public String getText() {
        return new String(text);
    }


    public int[] getAsciiWidths() {
        return asciiWidths;
    }

    public int getLineHeight() {
        if (lineHeight == UNDEFINED) {
            lineHeight = defaultStyle.getHeight(graphics);
        }
        return lineHeight;
    }


    public double getTabRatio() {
        return tabRatio;
    }

    public void setTabRatio(double tabRatio) {
        this.tabRatio = tabRatio;
    }

    public int length() {
        return text.length();
    }

    @Override
    public String toString() {
        return "EditableText[" + text + "]";
    }

    public static void main(String[] args) {
        class TFrame extends JFrame {
            EditableText t;
            JPanel j;
            int cursor_pos;

            public TFrame() {
                setBounds(300, 200, 400, 200);
                setDefaultCloseOperation(EXIT_ON_CLOSE);
                setVisible(true);

                t = new EditableText("abcde", 300,
                        new CharStyle(new Font("Consola", Font.BOLD, 20), Color.black),
                        0, 0, getGraphics(), true, false);
                cursor_pos = t.length();
                t.prepareSizes();
                setFocusTraversalKeysEnabled(false);

                addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        char keyChar = e.getKeyChar();
                        if (keyChar == KeyEvent.VK_BACK_SPACE) {
                            if (t.length() > 0 && cursor_pos > 0) t.remove(--cursor_pos);
                        } else if ((keyChar >= 32 && keyChar < 127) || keyChar == '\t' || keyChar == '\n' || keyChar > 127) {
                            t.insert(cursor_pos, keyChar);
                            cursor_pos++;
                        }
                        repaint();
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        int keyCode = e.getKeyCode();
                        if (keyCode == KeyEvent.VK_LEFT) {
                            cursor_pos--;
                            if (cursor_pos < 0) cursor_pos = 0;
                        } else if (keyCode == KeyEvent.VK_RIGHT) {
                            cursor_pos++;
                            if (cursor_pos > t.length()) cursor_pos = t.length();
                        }
                        repaint();
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {

                    }
                });
            }

            @Override
            public void paint(Graphics g) {
                super.paint(g);

                int i = 0;
                PrintableChar last = null;
                if (cursor_pos == 0) {
                    g.setFont(new Font("Arial", Font.PLAIN, 20));
                    g.drawString("|", 20 - 2, 100);
                }
                for (PrintableChar pc : t) {
                    pc.printChar(g, AffineTransform.getTranslateInstance(20, 100));
                    if (i++ == cursor_pos - 1) {
                        g.setFont(new Font("Arial", Font.PLAIN, 20));
                        int[] new_pos = t.nextPos(pc, '\03');
                        g.drawString("|", new_pos[X] + 20 - 2, new_pos[Y] + 100);
                    }
                }
            }
        }

        TFrame t = new TFrame();
    }
}
