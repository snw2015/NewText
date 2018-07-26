import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class PrintableChar {
    private char value;
    private int x;
    private int y;
    private CharStyle style;

    public PrintableChar(char value, int x, int y, CharStyle style) {
        this.value = value;
        this.x = x;
        this.y = y;
        this.style = style;
    }

    public PrintableChar(char value, CharStyle style) {
        this(value, 0, 0, style);
    }

    public PrintableChar(char value, int x, int y) {
        this(value, x, y, null);
    }

    public PrintableChar(char value) {
        this(value, 0, 0);
    }

    public PrintableChar() {
        this((char) 0);
    }


    public void printChar(Graphics g, AffineTransform transform) {
        if (style != null) style.setFormat(g);
        if (transform != null) {
            Point2D.Float src = new Point2D.Float(x, y);
            Point2D.Float dst = new Point2D.Float();
            transform.transform(src, dst);
            g.drawString("" + value, (int) dst.x, (int) dst.y);
        } else
            g.drawString("" + value, x, y);
    }

    public void printChar(Graphics g) {
        printChar(g, null);
    }

    public char getValue() {
        return value;
    }

    public void setValue(char value) {
        this.value = value;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public CharStyle getStyle() {
        return style;
    }

    public void setStyle(CharStyle style) {
        this.style = style;
    }

    public int getWidth(Graphics g) {
        return style.getWidth(value, g);
    }

    public int getHeight(Graphics g) {
        return style.getHeight(g);
    }

    @Override
    public String toString() {
        return ("PrintableChar[" + value + "]: { x = " + x + ", y = " + y + ", style = " + style + " }");
    }
}
