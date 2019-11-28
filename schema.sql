/* Must DROP the tables before we create new ones. Must CASCADE CONSTRAINTS so that we dont break any integrity/constraints across data */

DROP TABLE inventory CASCADE CONSTRAINTS;
DROP SEQUENCE inventory_sequence;

CREATE SEQUENCE inventory_sequence START WITH 1 INCREMENT BY 1;
CREATE TABLE inventory (
	ProductID integer DEFAULT inventory_sequence.NEXTVAL NOT NULL,
	ProductDesc varchar(30),									
	ProductPrice numeric(8,2) NOT NULL,
	ProductStockAmount integer NOT NULL,
	PRIMARY KEY (ProductID),
	CHECK (ProductStockAmount >= 0) /*You can't have a negative amount of stocks. */
);

INSERT INTO inventory (ProductDesc, ProductPrice, ProductStockAmount) VALUES ('iPhone X', 699.99, 100);
INSERT INTO inventory (ProductDesc, ProductPrice, ProductStockAmount) VALUES ('Macbook Air', 1299.99, 60);
INSERT INTO inventory (ProductDesc, ProductPrice, ProductStockAmount) VALUES ('Macbook Pro', 2499.99, 40);
INSERT INTO inventory (ProductDesc, ProductPrice, ProductStockAmount) VALUES ('Coca Cola',0.79, 1253);
INSERT INTO inventory (ProductDesc, ProductPrice, ProductStockAmount) VALUES ('Big Mac', 8.55, 420);



/* ======================================= */