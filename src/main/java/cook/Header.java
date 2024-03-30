package cook;

public record Header(String name, String value) {

    public int intValue() {
        return Integer.valueOf(value);
    }
}
