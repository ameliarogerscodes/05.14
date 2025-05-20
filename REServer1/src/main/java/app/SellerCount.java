package app;

public class SellerCount {
    private final String council;
    private final int totalSales;

    public SellerCount(String council, int totalSales) {
        this.council = council;
        this.totalSales = totalSales;
    }

    public String getCouncil() {
        return council;
    }

    public int getTotalSales() {
        return totalSales;
    }
}
