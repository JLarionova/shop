package src.java.main;

public final class Client {

    private static char nextName = 'A';

    static {
        resetNames();
    }

    public static void resetNames() {
        nextName = 'A';
    }


    public static Client nextClient() {
        return new Client(nextName++);
    }

    private final char name;


    private Client(char name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return Character.toString(name);
    }
}
