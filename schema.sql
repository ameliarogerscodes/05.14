-- schema.sql --
DROP DATABASE IF EXISTS `mysql-520`;
CREATE DATABASE `mysql-520`;
USE `mysql-520`;

CREATE TABLE IF NOT EXISTS councils (
                                        id   INT AUTO_INCREMENT PRIMARY KEY,
                                        name VARCHAR(100) NOT NULL
    );

CREATE TABLE IF NOT EXISTS property_types (
                                              id   INT AUTO_INCREMENT PRIMARY KEY,
                                              type VARCHAR(50) NOT NULL
    );

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

-- **no** IF NOT EXISTS here
CREATE INDEX idx_properties_property_id
    ON properties(property_id);

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
