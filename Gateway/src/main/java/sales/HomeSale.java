package sales;

// Simple class to provide test data in SalesDAO

public class HomeSale {
    public  String saleID;
    public  String postcode;
    public  String salePrice;

    public HomeSale(String saleID, String postcode, String salePrice) {
        this.saleID = saleID;
        this.postcode = postcode;
        this.salePrice = salePrice;
    }

    // needed for JSON conversion
    public HomeSale() {}


}
