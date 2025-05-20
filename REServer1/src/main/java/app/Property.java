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

    public Property(int id,
                    String propertyId,
                    String address,
                    String postCode,
                    Integer councilId,
                    Integer propertyTypeId,
                    BigDecimal area,
                    String areaType,
                    String zoning,
                    String strataLotNumber,
                    String propertyName) {
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

    // Standard JavaBean getters:
    public int getId() { return id; }
    public String getPropertyId() { return propertyId; }
    public String getAddress() { return address; }
    public String getPostCode() { return postCode; }
    public Integer getCouncilId() { return councilId; }
    public Integer getPropertyTypeId() { return propertyTypeId; }
    public BigDecimal getArea() { return area; }
    public String getAreaType() { return areaType; }
    public String getZoning() { return zoning; }
    public String getStrataLotNumber() { return strataLotNumber; }
    public String getPropertyName() { return propertyName; }
}
