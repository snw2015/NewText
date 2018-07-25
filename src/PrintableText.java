import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PrintableText implements Iterable<PrintableChar> {
    public static final int ASCII_RANGE = 256;
    public static final int UNDEFINED = -1;
    public static final int INVALID = -2;
    public static final char DEFAULT_NON_ASCII_SAMPLE = '　';
    public static final char DEFAULT_ASCII_SAMPLE = ' ';
    public static final double DEFAULT_TAB_RATIO = 4F;
    public static final int X = 0;
    public static final int Y = 1;

    protected char nonAsciiSample = DEFAULT_NON_ASCII_SAMPLE;
    protected char asciiSample = DEFAULT_ASCII_SAMPLE;
    protected int asciiWidth = UNDEFINED;
    protected int lineHeight = UNDEFINED;
    protected double tabRatio = DEFAULT_TAB_RATIO;

    protected boolean fixedAsciiWidth;
    protected boolean fixedWidth;

    protected CharStyle defaultStyle = null;
    protected int width;
    protected int charSpace = 0;
    protected boolean fixedCharSpace = false;
    protected int lineSpace = 0;
    protected boolean fixedLineSpace = false;
    protected Graphics graphics = null;

    public PrintableText(int width, CharStyle defaultStyle, int charSpace, int lineSpace, Graphics graphics
            , boolean fixedAsciiWidth, boolean fixedWidth) {
        this.defaultStyle = defaultStyle;
        this.width = width;
        this.charSpace = charSpace;
        this.lineSpace = lineSpace;
        this.graphics = graphics;
        this.fixedAsciiWidth = fixedAsciiWidth;
        this.fixedWidth = fixedWidth;
    }



    public CharStyle getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(CharStyle defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getCharSpace() {
        return charSpace;
    }

    public void setCharSpace(int charSpace) {
        this.charSpace = charSpace;
    }

    public boolean isFixedCharSpace() {
        return fixedCharSpace;
    }

    public void setFixedCharSpace(boolean fixedCharSpace) {
        this.fixedCharSpace = fixedCharSpace;
    }

    public int getLineSpace() {
        return lineSpace;
    }

    public void setLineSpace(int lineSpace) {
        this.lineSpace = lineSpace;
    }

    public boolean isFixedLineSpace() {
        return fixedLineSpace;
    }

    public void setFixedLineSpace(boolean fixedLineSpace) {
        this.fixedLineSpace = fixedLineSpace;
    }

    public boolean isFixedAsciiWidth() {
        return fixedAsciiWidth;
    }

    public void setFixedAsciiWidth(boolean fixedAsciiWidth) {
        this.fixedAsciiWidth = fixedAsciiWidth;
    }

    public boolean isFixedWidth() {
        return fixedWidth;
    }

    public void setFixedWidth(boolean fixedWidth) {
        this.fixedWidth = fixedWidth;
    }

    public char getNonAsciiSample() {
        return nonAsciiSample;
    }

    public void setNonAsciiSample(char nonAsciiSample) {
        this.nonAsciiSample = nonAsciiSample;
    }

    public char getAsciiSample() {
        return asciiSample;
    }

    public void setAsciiSample(char asciiSample) {
        this.asciiSample = asciiSample;
        this.asciiWidth = defaultStyle.getWidth(asciiSample, graphics);
    }

    public int getAsciiWidth() {
        return asciiWidth;
    }

    public void setAsciiWidth(int asciiWidth) {
        this.asciiWidth = asciiWidth;
    }

    private int renderLength = -1;

    public int getRenderLength() {
        return renderLength;
    }

    public void setRenderLength(int renderLength) {
        this.renderLength = renderLength;
    }

    /**
     * Find the next PrintableChar depending on the position in string and the last char.
     *
     * @param pos  The position in string.
     * @param last The char right before this one.
     *             If null, the char should be created only with regard of the pos.
     * @return The required char.
     */
    @NotNull
    protected abstract PrintableChar next(int pos, @Nullable PrintableChar last);

    /**
     * Determine if the next PrintableChar exist.
     *
     * @param pos  The position in string.
     * @param last The char right before this one.
     *             If null, the char should be determined only with regard of the pos.
     * @param endY The end y position of the canvas.
     *             A character is visible when its <b>bottom</b> position is less than the endY.
     * @return True iff has next char and it is inside the range responding to endY.
     */
    protected abstract boolean hasNext(int pos, @Nullable PrintableChar last, int endY);

    /**
     * Find the last invisible character.
     *
     * @param startY  The start y position of the canvas.
     *                A character is visible when its <b>top</b> position is greater than or equals the startY.
     * @param nextPos the position of the first visible character.
     * @return The last invisible char.
     * Null if the first character is visible.
     */
    @Nullable
    protected abstract PrintableChar lastInvisible(int startY, AtomicInteger nextPos);

    private class PrintableTextIterator implements Iterator<PrintableChar> {
        private PrintableChar last;
        private int nextPos;
        private int endY;

        public PrintableTextIterator(int startY, int endY) {
            this.endY = endY;
            AtomicInteger pos = new AtomicInteger(0);
            last = PrintableText.this.lastInvisible(startY, pos);
            nextPos = pos.get();
        }

        @Override
        public boolean hasNext() {
            if (renderLength > 0 && nextPos >= renderLength)
                return false;
            return PrintableText.this.hasNext(nextPos, last, endY);
        }

        @Override
        public PrintableChar next() {
            return last = PrintableText.this.next(nextPos++, last);
        }
    }

    private int startY = 0;
    private int endY = -1;

    @Override
    public PrintableTextIterator iterator() {
        return new PrintableTextIterator(startY, endY);
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public void setEndY(int endY) {
        this.endY = endY;
    }

    public void setPrintRange(int startY, int endY) {
        setStartY(startY);
        setEndY(endY);
    }



    public PrintableTextIterator iterator(int startY, int endY) {
        setPrintRange(startY, endY);
        return iterator();
    }
}
