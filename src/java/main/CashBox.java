package src.java.main;

import java.util.ArrayDeque;
import java.util.Deque;

public class CashBox {
    private final int number;
    private final Deque<Client> clients;
    private State state;

    public enum State {
        ENABLED,
        DISABLED,
        IS_CLOSING
    }

    public CashBox(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("Cash box number cannot be negative.");
        }
        this.number = number;
        this.clients = new ArrayDeque<>();
        this.state = State.DISABLED;
    }

    public Deque<Client> getQueue() {
        return new ArrayDeque<>(clients);
    }

    public void serveClient() {
        if (clients.isEmpty() || state == State.DISABLED) {
            return;
        }

        clients.pollFirst();

        if (clients.isEmpty() && state == State.IS_CLOSING) {
            state = State.DISABLED;
        }
    }

    public boolean inState(State state) {
        return this.state == state;
    }

    public boolean notInState(State state) {
        return !inState(state);
    }

    public void setState(State state) {
        if (state == null) {
            throw new NullPointerException("State cannot be null.");
        }
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void addLast(Client client) {
        if (client == null) {
            throw new NullPointerException("Client cannot be null.");
        }

        if (state == State.ENABLED) {
            clients.addLast(client);
        }
    }

    public Client removeLast() {
        return clients.pollLast();
    }

    public int getNumber() {
        return number;
    }

    public int getClientsCount() {
        return clients.size();
    }

    @Override
    public String toString() {
        char sign = switch (state) {
            case ENABLED -> '+';
            case IS_CLOSING -> '|';
            default -> '-';
        };

        StringBuilder str = new StringBuilder();
        for (Client client : clients) {
            str.append(client);
        }

        return "#" + number + "[" + sign + "]" + str;
    }
}
