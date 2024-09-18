package src.java.main;

import java.util.*;

public class Shop {
    private final int cashBoxCount;
    private final List<CashBox> cashBoxes;

    public Shop(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count should be greater than 0");
        }

        this.cashBoxCount = count;
        List<CashBox> tempCashBoxes = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            tempCashBoxes.add(new CashBox(i));
        }

        this.cashBoxes = Collections.unmodifiableList(tempCashBoxes);
    }

    public int getCashBoxCount() {
        return cashBoxCount;
    }

    public List<CashBox> getCashBoxes() {
        return cashBoxes;
    }

    private static void validateCashBoxes(List<CashBox> cashBoxes) {
        if (cashBoxes == null || cashBoxes.isEmpty()) {
            throw new IllegalArgumentException("Cash boxes list cannot be null or empty");
        }
    }

    private static int getTotalClientsCount(List<CashBox> cashBoxes) {
        validateCashBoxes(cashBoxes);
        return cashBoxes.stream()
                .filter(box -> box.inState(CashBox.State.ENABLED))
                .mapToInt(CashBox::getClientsCount)
                .sum();
    }

    private static int getEnabledCashBoxCount(List<CashBox> cashBoxes) {
        validateCashBoxes(cashBoxes);
        return (int) cashBoxes.stream()
                .filter(box -> box.inState(CashBox.State.ENABLED))
                .count();
    }

    private static boolean isGreaterThanMax(List<CashBox> cashBoxes) {
        validateCashBoxes(cashBoxes);
        int[] minAndMax = getMinMaxSize(cashBoxes);
        int max = minAndMax[1];

        return cashBoxes.stream()
                .filter(box -> box.inState(CashBox.State.IS_CLOSING))
                .anyMatch(box -> box.getClientsCount() > max);
    }

    private static int recalculatedMax(List<CashBox> cashBoxes) {
        validateCashBoxes(cashBoxes);

        int totalBuyers = cashBoxes.stream()
                .filter(box -> box.notInState(CashBox.State.DISABLED))
                .mapToInt(CashBox::getClientsCount)
                .sum();

        long boxCount = cashBoxes.stream()
                .filter(box -> box.notInState(CashBox.State.DISABLED))
                .count();

        int min = (int) (totalBuyers / boxCount);

        return min + (totalBuyers % boxCount == 0 ? 0 : 1);
    }

    public void addClient(Client client) {
        if (client == null) {
            throw new NullPointerException("Client cannot be null");
        }

        int minBoxIndex = -1;

        for (int i = 0; i < cashBoxes.size(); i++) {
            CashBox box = cashBoxes.get(i);
            if (box.inState(CashBox.State.ENABLED) &&
                    (minBoxIndex == -1 || box.getClientsCount() < cashBoxes.get(minBoxIndex).getClientsCount())) {
                minBoxIndex = i;
            }
        }

        if (minBoxIndex == -1) {
            throw new IllegalStateException("No active cash boxes available");
        }

        cashBoxes.get(minBoxIndex).addLast(client);
    }

    public void tact() {
        cashBoxes.forEach(CashBox::serveClient);

        if (getEnabledCashBoxCount(cashBoxes) <= 1 && !isGreaterThanMax(cashBoxes)) {
            return;
        }

        int max = isGreaterThanMax(cashBoxes) ? recalculatedMax(cashBoxes) : getMinMaxSize(cashBoxes)[1];

        Queue<Client> defectorClients = new ArrayDeque<>();
        cashBoxes.stream()
                .filter(box -> box.notInState(CashBox.State.DISABLED) && box.getClientsCount() > max)
                .forEach(box -> {
                    while (box.getClientsCount() > max) {
                        defectorClients.add(box.removeLast());
                    }
                });

        for (int i = 0; i < cashBoxes.size() && !defectorClients.isEmpty(); i++) {
            CashBox box = cashBoxes.get(i);
            while (box.inState(CashBox.State.ENABLED) && box.getClientsCount() < max && !defectorClients.isEmpty()) {
                box.addLast(defectorClients.poll());
            }
        }
    }

    public static int[] getMinMaxSize(List<CashBox> cashBoxes) {
        validateCashBoxes(cashBoxes);

        int totalBuyers = getTotalClientsCount(cashBoxes);
        int cashBoxCount = getEnabledCashBoxCount(cashBoxes);

        int minSize = totalBuyers / cashBoxCount;
        int maxSize = minSize + (totalBuyers % cashBoxCount == 0 ? 0 : 1);

        return new int[]{minSize, maxSize};
    }

    public void setCashBoxState(int cashBoxNumber, CashBox.State state) {
        if (cashBoxNumber < 0) {
            throw new IllegalArgumentException("Cash box number cannot be negative: " + cashBoxNumber);
        }

        if (state == null) {
            throw new IllegalArgumentException("State cannot be null.");
        }

        CashBox box = cashBoxes.stream()
                .filter(b -> b.getNumber() == cashBoxNumber)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No cash box found with number: " + cashBoxNumber));

        box.setState(state);
    }

    public CashBox getCashBox(int cashBoxNumber) {
        if (cashBoxNumber < 0) {
            throw new IllegalArgumentException("Cash box number cannot be negative.");
        }

        return cashBoxes.stream()
                .filter(box -> box.getNumber() == cashBoxNumber)
                .findFirst()
                .orElse(null);
    }

    public void print() {
        cashBoxes.forEach(System.out::println);
    }
}
