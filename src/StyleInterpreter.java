public class StyleInterpreter {
    public enum COMMAND {
        CHARACTER, STYLE_START, STYLE_END, END
    }

    private final String rawStr;

    public StyleInterpreter(String rawStr) {
        this.rawStr = rawStr;
    }

    public COMMAND nextCommend() {
        //TODO

        return COMMAND.CHARACTER;
    }

    public char getCharacter() {
        //TODO

        return '1';
    }

    public String getStyleInfo() {
        //TODO
        return null;
    }
}
