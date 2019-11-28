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


/* ====================[ ORDERS ]=================== */


DROP TABLE orders CASCADE CONSTRAINTS;
DROP SEQUENCE orders_sequence;

CREATE SEQUENCE orders_sequence START WITH 1 INCREMENT BY 1;
CREATE TABLE orders (
	OrderID integer DEFAULT orders_sequence.NEXTVAL NOT NULL,
	OrderType varchar(30) NOT NULL,
	OrderCompleted integer NOT NULL,
	OrderPlaced Date NOT NULL,
	PRIMARY KEY (OrderID),
	CHECK (OrderType IN ('InStore', 'Collection', 'Delivery')),
	CHECK (OrderCompleted IN ('0', '1'))
);

INSERT INTO orders (OrderType, OrderCompleted, OrderPlaced) VALUES ('InStore', '0', '26-NOV-19');
INSERT INTO orders (OrderType, OrderCompleted, OrderPlaced) VALUES ('Collection', '0', '24-OCT-19');
INSERT INTO orders (OrderType, OrderCompleted, OrderPlaced) VALUES ('Collection', '1', '03-OCT-19');
INSERT INTO orders (OrderType, OrderCompleted, OrderPlaced) VALUES ('Delivery', '1', '18-AUG-19');



/* ====================[ ORDER_PRODUCTS ]=================== */


DROP TABLE order_products CASCADE CONSTRAINTS;

CREATE TABLE order_products (
	OrderID integer,
	ProductID integer,
	ProductQuantity integer NOT NULL,
	PRIMARY KEY (OrderID, ProductID),
	FOREIGN KEY (ProductID) REFERENCES inventory(ProductID),
	FOREIGN KEY (OrderID) REFERENCES orders(OrderID) ON DELETE CASCADE,
	CHECK (ProductQuantity >= 0)
);

-- CREATE OR REPLACE TRIGGER order_products_check_quantity 
-- AFTER INSERT OR UPDATE ON ORDERS
-- FOR EACH ROW
-- DECLARE
-- 	item_qty inventory.ProductStockAmount%type;
-- BEGIN
--  SELECT ProductQuantity
--  INTO item_qty
--  FROM inventory
--  WHERE inventory.ProductID = :NEW.ProductID;

-- IF (:NEW.ProductID>=item_qty) THEN
--  RAISE_APPLICATION_ERROR(-20250,'Trying to order more products than are in stock!!');
-- END IF;
-- END;


INSERT INTO order_products (OrderID, ProductID, ProductQuantity) VALUES (1, 1, 2);



/* ======================================= */