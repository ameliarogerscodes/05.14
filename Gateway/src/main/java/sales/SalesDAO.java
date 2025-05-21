package sales;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SalesDAO {

    // List to hold test data
    private  List<HomeSale> sales = new ArrayList<>();


    public SalesDAO() {
        // create some test data
        sales.add(new HomeSale("0", "2257", "2000000"));
        sales.add(new HomeSale("1", "2262", "1300000"));
        sales.add(new HomeSale("2", "2000", "4000000"));
        sales.add(new HomeSale("3", "2000", "1000000"));
      }

    public boolean newSale (HomeSale homeSale){
            sales.add(homeSale);
            return true;
    }

    // returns Optional wrapping a HomeSale if id is found, empty Optional otherwise
    public Optional<HomeSale> getSaleById(String saleID) {

        for (HomeSale u : sales) {
            if (u.saleID.equals(saleID)) {
                System.out.println("id found ");
                return Optional.of(u);
             }
        }
        return Optional.empty();
    }

    // returns a List of homesales  in a given postCode
    public List<HomeSale> getSalesByPostCode(String postCode) {
        System.out.println("postcode is: " + postCode);
        List<HomeSale> tmp = new ArrayList<>();
         for (HomeSale u : sales) {
            if (u.postcode.equals(postCode)) {
                tmp.add(u);
                System.out.println("postcode found ");
            }
        }
        return tmp == null ? Collections.emptyList() : tmp;
    }

    // returns the individual prices for all sales. Potentially large
    public List<String> getAllSalePrices() {
        return   sales.stream()
                .map(e -> e.salePrice)
                .collect(Collectors.toList());
    }

    // returns all home sales. Potentially large
    public List<HomeSale> getAllSales() {
        return   sales.stream().collect(Collectors.toList());
    }

}
