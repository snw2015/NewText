public class StyleInterpreter {
    public enum COMMAND {
        CHARACTER, STYLE_START, STYLE_END, END
    }

    private final String rawStr;
    private char lastChar = '\0';
    private String lastInfo = null;

    private int pos = 0;

    public StyleInterpreter(String rawStr) {
        this.rawStr = rawStr;
    }

    private COMMAND analyseEscape() {
        char c = rawStr.charAt(pos);
        switch (c) {
            case '\\':
            case '{':
            case '}':
                lastChar = c;
                pos++;
                return COMMAND.CHARACTER;
        }

        int end_pos = rawStr.indexOf('{', pos);
        lastInfo = rawStr.substring(pos, end_pos);
        pos = end_pos + 1;

        return COMMAND.STYLE_START;
    }

    public COMMAND nextCommand() {
        if (pos >= rawStr.length()) return COMMAND.END;
        char c;
        switch (c = rawStr.charAt(pos++)) {
            case '\\':
                return analyseEscape();
            case '}':
                return COMMAND.STYLE_END;
            default:
                lastChar = c;
                return COMMAND.CHARACTER;
        }
    }

    public char getCharacter() {
        return lastChar;
    }

    public String getStyleInfo() {
        return lastInfo;
    }
}
