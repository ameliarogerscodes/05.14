USE mysql-520;

--
-- 1) Councils lookup
--
CREATE TABLE IF NOT EXISTS councils (
                                        id   INT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(100) NOT NULL
    );

--
-- 2) Property types lookup
--
CREATE TABLE IF NOT EXISTS property_types (
                                              id   INT AUTO_INCREMENT PRIMARY KEY,
                                              type VARCHAR(50) NOT NULL
    );

--
-- 3) Properties master table
--    – property_id is now nullable and no longer UNIQUE
--
CREATE TABLE IF NOT EXISTS properties (
                                          id                  INT AUTO_INCREMENT PRIMARY KEY,
                                          property_id         VARCHAR(20)   NULL,
    address             TEXT          NULL,
    post_code           VARCHAR(10)   NULL,
    council_id          INT           NULL,
    property_type_id    INT           NULL,
    area                DECIMAL(10,2) NULL,
    area_type           CHAR(1)       NULL,
    zoning              VARCHAR(10)   NULL,
    strata_lot_number   VARCHAR(50)   NULL,
    property_name       VARCHAR(100)  NULL,

    FOREIGN KEY (council_id)
    REFERENCES councils(id)
    ON UPDATE CASCADE
    ON DELETE SET NULL,

    FOREIGN KEY (property_type_id)
    REFERENCES property_types(id)
    ON UPDATE CASCADE
    ON DELETE SET NULL
    );

-- Add an index on properties.property_id so it can be referenced by sales
CREATE INDEX idx_properties_property_id
    ON properties(property_id);

--
-- 4) Sales table, linking back to properties.property_id
--    property_id is nullable and can repeat
--
CREATE TABLE IF NOT EXISTS sales (
                                     id                  INT AUTO_INCREMENT PRIMARY KEY,
                                     property_id         VARCHAR(20)   NULL,
    purchase_price      BIGINT        NULL,
    contract_date       DATE          NULL,
    settlement_date     DATE          NULL,
    download_date       DATE          NULL,
    nature_of_property  VARCHAR(10)   NULL,
    primary_purpose     VARCHAR(100)  NULL,
    legal_description   TEXT          NULL,

    FOREIGN KEY (property_id)
    REFERENCES properties(property_id)
    ON UPDATE CASCADE
    ON DELETE SET NULL
    );
