import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class CharStyle {
    private Font font;
    private Color color;

    public CharStyle(Font font, Color color) {
        this.font = font;
        this.color = color;
    }

    public void setFormat(Graphics g) {
        g.setFont(font);
        g.setColor(color);
    }

    public int[] getWidths(Graphics g) {
        FontMetrics fm = g.getFontMetrics(font);
        return fm.getWidths();
    }

    public int getWidth(char c, Graphics g) {
        FontMetrics fm = g.getFontMetrics(font);
        return fm.charWidth(c);
    }

    /**
     * Calculate the size of the specific character in the stored graphics variable and font.
     * Note that this method is relatively slow to execute, thus storing the calculated size is suggested.
     * The FontMetrics object will be constructed each time the method is called since the font is allowed to be changed.
     * <br><br/>
     *
     * @param c The character to be measured.
     * @return The size responding to the char.
     */
    public int[] getSize(char c, Graphics g) {
        int width = 0, height = 0;
        FontMetrics fm = g.getFontMetrics(font);
        width = fm.charWidth(c);
        height = fm.getHeight();

        return new int[]{width, height};
    }
    /**
     * @return True if changed.
     */
    public boolean modifyNyInfo(String info) {
        //TODO

        return true;
    }

    public int getHeight(Graphics g) {
        return g.getFontMetrics(font).getHeight();
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
