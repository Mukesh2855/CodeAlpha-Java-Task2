import java.io.*;
import java.util.*;

class Stock implements Serializable {
    private String symbol;
    private double price;

    public Stock(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }

    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }

    public void updatePrice() {
        // Random price movement (-5% to +5%)
        double change = (Math.random() * 0.10) - 0.05;
        price = Math.round(price * (1 + change) * 100.0) / 100.0;
    }
}

class Transaction implements Serializable {
    String type;
    String symbol;
    double shares;
    double price;

    public Transaction(String type, String symbol, double shares, double price) {
        this.type = type;
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
    }

    @Override
    public String toString() {
        return type + " " + shares + " of " + symbol + " @ " + price;
    }
}

class Portfolio implements Serializable {
    private double cash;
    private Map<String, Double> holdings = new HashMap<>();
    private List<Transaction> history = new ArrayList<>();

    public Portfolio(double cash) {
        this.cash = cash;
    }

    public double getCash() { return cash; }
    public Map<String, Double> getHoldings() { return holdings; }
    public List<Transaction> getHistory() { return history; }

    public void buy(Stock stock, double shares) {
        double cost = stock.getPrice() * shares;
        if (cash >= cost) {
            cash -= cost;
            holdings.put(stock.getSymbol(),
                    holdings.getOrDefault(stock.getSymbol(), 0.0) + shares);
            history.add(new Transaction("BUY", stock.getSymbol(), shares, stock.getPrice()));
        } else {
            System.out.println("Not enough cash!");
        }
    }

    public void sell(Stock stock, double shares) {
        double owned = holdings.getOrDefault(stock.getSymbol(), 0.0);
        if (owned >= shares) {
            cash += stock.getPrice() * shares;
            holdings.put(stock.getSymbol(), owned - shares);
            history.add(new Transaction("SELL", stock.getSymbol(), shares, stock.getPrice()));
        } else {
            System.out.println("Not enough shares!");
        }
    }

    public double totalValue(Map<String, Stock> market) {
        double value = cash;
        for (String sym : holdings.keySet()) {
            value += holdings.get(sym) * market.get(sym).getPrice();
        }
        return Math.round(value * 100.0) / 100.0;
    }
}

public class StockTradingPlatform {
    private static Map<String, Stock> market = new HashMap<>();
    private static Portfolio portfolio = new Portfolio(10000);

    public static void main(String[] args) {
        market.put("AAPL", new Stock("AAPL", 190));
        market.put("GOOG", new Stock("GOOG", 135));
        market.put("TSLA", new Stock("TSLA", 250));

        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== STOCK TRADING PLATFORM ===");
            System.out.println("1. View Market");
            System.out.println("2. Buy Stock");
            System.out.println("3. Sell Stock");
            System.out.println("4. View Portfolio");
            System.out.println("5. View Transactions");
            System.out.println("6. Next Day (Update Prices)");
            System.out.println("7. Save Portfolio");
            System.out.println("8. Load Portfolio");
            System.out.println("9. Exit");
            System.out.print("Choice: ");

            int choice = sc.nextInt();
            switch (choice) {
                case 1: viewMarket(); break;
                case 2: buyFlow(sc); break;
                case 3: sellFlow(sc); break;
                case 4: viewPortfolio(); break;
                case 5: viewTransactions(); break;
                case 6: updatePrices(); break;
                case 7: savePortfolio(); break;
                case 8: loadPortfolio(); break;
                case 9: running = false; break;
                default: System.out.println("Invalid choice");
            }
        }
        sc.close();
    }

    private static void viewMarket() {
        System.out.println("\nMarket Data:");
        for (Stock s : market.values()) {
            System.out.println(s.getSymbol() + ": $" + s.getPrice());
        }
    }

    private static void buyFlow(Scanner sc) {
        System.out.print("Enter stock symbol: ");
        String sym = sc.next().toUpperCase();
        Stock s = market.get(sym);
        if (s != null) {
            System.out.print("Enter shares: ");
            double sh = sc.nextDouble();
            portfolio.buy(s, sh);
        } else {
            System.out.println("Invalid symbol.");
        }
    }

    private static void sellFlow(Scanner sc) {
        System.out.print("Enter stock symbol: ");
        String sym = sc.next().toUpperCase();
        Stock s = market.get(sym);
        if (s != null) {
            System.out.print("Enter shares: ");
            double sh = sc.nextDouble();
            portfolio.sell(s, sh);
        } else {
            System.out.println("Invalid symbol.");
        }
    }

    private static void viewPortfolio() {
        System.out.println("\nPortfolio:");
        System.out.println("Cash: $" + portfolio.getCash());
        for (var e : portfolio.getHoldings().entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue() + " shares");
        }
        System.out.println("Total Value: $" + portfolio.totalValue(market));
    }

    private static void viewTransactions() {
        System.out.println("\nTransactions:");
        for (Transaction t : portfolio.getHistory()) {
            System.out.println(t);
        }
    }

    private static void updatePrices() {
        for (Stock s : market.values()) {
            s.updatePrice();
        }
        System.out.println("Prices updated.");
    }

    private static void savePortfolio() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("portfolio.dat"))) {
            out.writeObject(portfolio);
            System.out.println("Portfolio saved.");
        } catch (IOException e) {
            System.out.println("Error saving portfolio.");
        }
    }

    private static void loadPortfolio() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("portfolio.dat"))) {
            portfolio = (Portfolio) in.readObject();
            System.out.println("Portfolio loaded.");
        } catch (Exception e) {
            System.out.println("Error loading portfolio.");
        }
    }
}
