package app;

import java.math.BigDecimal;

public class Property {
    private final int id;
    private final String propertyId;
    private final String address;
    private final String postCode;
    private final Integer councilId;
    private final Integer propertyTypeId;
    private final BigDecimal area;
    private final String areaType;
    private final String zoning;
    private final String strataLotNumber;
    private final String propertyName;

    public Property(int id, String propertyId, String address, String postCode,
                    Integer councilId, Integer propertyTypeId, BigDecimal area,
                    String areaType, String zoning, String strataLotNumber, String propertyName) {
        this.id = id;
        this.propertyId = propertyId;
        this.address = address;
        this.postCode = postCode;
        this.councilId = councilId;
        this.propertyTypeId = propertyTypeId;
        this.area = area;
        this.areaType = areaType;
        this.zoning = zoning;
        this.strataLotNumber = strataLotNumber;
        this.propertyName = propertyName;
    }

    // Getters
    public int id() {
        return id;
    }

    public String propertyId() {
        return propertyId;
    }

    public String address() {
        return address;
    }

    public Integer councilId() {
        return councilId;
    }

    public Integer propertyTypeId() {
        return propertyTypeId;
    }

    public BigDecimal area() {
        return area;
    }

    public String areaType() {
        return areaType;
    }

    public String zoning() {
        return zoning;
    }

    public String strataLotNumber() {
        return strataLotNumber;
    }

    public String propertyName() {
        return propertyName;
    }

    public String postCode() {
        return postCode;
    }
}
