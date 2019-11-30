/* Must DROP the tables before we create new ones. Must CASCADE CONSTRAINTS so that we dont break any integrity/constraints across data */

DROP TABLE inventory CASCADE CONSTRAINTS;
DROP SEQUENCE inventory_sequence;

CREATE SEQUENCE inventory_sequence START WITH 1 INCREMENT BY 1;
/

CREATE TABLE inventory (
	ProductID integer DEFAULT inventory_sequence.NEXTVAL NOT NULL,
	ProductDesc varchar(30),									
	ProductPrice numeric(8,2) NOT NULL,
	ProductStockAmount integer NOT NULL,
	PRIMARY KEY (ProductID),
	CONSTRAINT positive_stock_check CHECK (ProductStockAmount >= 0)
);

-- Create the constraint that you can't have negative stock

INSERT INTO inventory (ProductDesc, ProductPrice, ProductStockAmount) VALUES ('iPhone X', 699.99, 1000);
INSERT INTO inventory (ProductDesc, ProductPrice, ProductStockAmount) VALUES ('Macbook Air', 1299.99, 1000);
INSERT INTO inventory (ProductDesc, ProductPrice, ProductStockAmount) VALUES ('Macbook Pro', 2499.99, 1000);
INSERT INTO inventory (ProductDesc, ProductPrice, ProductStockAmount) VALUES ('Coca Cola',0.79, 1000);
INSERT INTO inventory (ProductDesc, ProductPrice, ProductStockAmount) VALUES ('Big Mac', 8.55, 1000);


/* ====================[ ORDERS ]=================== */


DROP TABLE orders CASCADE CONSTRAINTS;
DROP SEQUENCE orders_sequence;

CREATE SEQUENCE orders_sequence START WITH 1 INCREMENT BY 1;
/

CREATE TABLE orders (
	OrderID integer DEFAULT orders_sequence.NEXTVAL NOT NULL,
	OrderType varchar(30) NOT NULL,
	OrderCompleted integer NOT NULL,
	OrderPlaced Date NOT NULL,
	PRIMARY KEY (OrderID),
	CONSTRAINT valid_order_type_check CHECK (OrderType IN ('InStore', 'Collection', 'Delivery')),
	CONSTRAINT valid_order_completed_value_check CHECK (OrderCompleted IN ('0', '1'))
);


-- INSERT INTO orders (OrderType, OrderCompleted, OrderPlaced) VALUES ('InStore', '0', '26-NOV-19');
-- INSERT INTO orders (OrderType, OrderCompleted, OrderPlaced) VALUES ('Collection', '0', '24-OCT-19');
-- INSERT INTO orders (OrderType, OrderCompleted, OrderPlaced) VALUES ('Collection', '1', '03-OCT-19');
-- INSERT INTO orders (OrderType, OrderCompleted, OrderPlaced) VALUES ('Delivery', '1', '18-AUG-19');


/* ====================[ ORDER_PRODUCTS ]=================== */


DROP TABLE order_products CASCADE CONSTRAINTS;

CREATE TABLE order_products (
	OrderID integer,
	ProductID integer,
	ProductQuantity integer NOT NULL,
	PRIMARY KEY (OrderID, ProductID),
	FOREIGN KEY (ProductID) REFERENCES inventory(ProductID),
	FOREIGN KEY (OrderID) REFERENCES orders(OrderID) ON DELETE CASCADE,
	CONSTRAINT positive_order_amount_check CHECK (ProductQuantity > 0)
);

-- Special type to define the items in an order.
-- Would look like (productid1, amountordered1, productid2, amountordered2, ....)
CREATE OR REPLACE TYPE order_items AS TABLE OF integer;
/

-- Procedure to order products, made using a procedure so that we can rollback on error.
DROP PROCEDURE create_order;
CREATE OR REPLACE PROCEDURE create_order (order_type IN varchar2, order_placed IN Date, staff_id IN integer, order_lines IN order_items) AS
	too_short_order_lines EXCEPTION;
	invalid_order EXCEPTION;
	last_order_id integer;
	currently_processed integer;
BEGIN 
	-- We create a savepoint here.
	SAVEPOINT before_order;

	--Create the new order in orders table
	--If it's a InStore/Collection order it's completed, if it's a delivery it isnt completed.
	IF order_type IN ('InStore', 'Collection') THEN
		INSERT INTO orders (OrderType, OrderCompleted, OrderPlaced) VALUES (order_type, '0', order_placed);
	ELSE
		INSERT INTO orders (OrderType, OrderCompleted, OrderPlaced) VALUES (order_type, '1', order_placed);
	END IF;
	

	-- Get the ID of the order being inserted.
	SELECT max(OrderID) INTO last_order_id FROM orders;

  	-- Iterate across all of the order_lines and insert them into the order_products table.
  	-- If an error occurs whilst doing this the whole order gets rolledback nicely (will probably error if not in stock)

	currently_processed := order_lines.FIRST;

	-- Check that the order actually contains items...
	-- If it does insert them, if it doesnt throw an error.
	IF order_lines.count / 2 < 1 THEN
		RAISE too_short_order_lines;
	ELSE 
		FOR i in order_lines.FIRST..order_lines.count / 2 LOOP
			INSERT INTO order_products (OrderID, ProductID, ProductQuantity) VALUES (last_order_id, order_lines(currently_processed), order_lines(currently_processed+1));
			currently_processed := currently_processed + 2;
		END LOOP;
	END IF;

	-- Insert into the staff_orders that they sold this product.
	INSERT INTO staff_orders VALUES(staff_id, last_order_id);
EXCEPTION
	WHEN OTHERS THEN
		ROLLBACK TO before_order;
		RAISE invalid_order; 
END;
/


-- Trigger to decrememt the amount of stock for a product following it being added to an order.
-- Don't need to care about decrementing below 0, this can't happen because the procedure above will handle this error.
CREATE OR REPLACE TRIGGER decrement_stock
AFTER INSERT ON order_products
FOR EACH ROW
BEGIN
	UPDATE inventory
	SET ProductStockAmount = ProductStockAmount - :NEW.ProductQuantity
	WHERE inventory.ProductID = :NEW.ProductID;
END;
/


-- INSERT INTO order_products (OrderID, ProductID, ProductQuantity) VALUES (1, 1, 2);



/* ====================[ deliveries ]=================== */

DROP TABLE deliveries CASCADE CONSTRAINTS;
CREATE TABLE deliveries (
	OrderID integer NOT NULL,
	FName varchar(30) NOT NULL,
	LName varchar(30) NOT NULL,
	House varchar(30) NOT NULL,
	Street varchar(30) NOT NULL,
	City varchar(30) NOT NULL,
	DeliveryDate Date NOT NULL,
	PRIMARY KEY (OrderID),
	FOREIGN KEY (OrderID) REFERENCES orders(OrderID) ON DELETE CASCADE
);


/* ====================[ collections ]=================== */

DROP TABLE collections CASCADE CONSTRAINTS;
CREATE TABLE collections (
	OrderID integer NOT NULL,
	FName varchar(30) NOT NULL,
	LName varchar(30) NOT NULL,
	CollectionDate Date NOT NULL,
	PRIMARY KEY (OrderID),
	FOREIGN KEY (OrderID) REFERENCES orders(OrderID) ON DELETE CASCADE
);


/* ====================[ staff ]=================== */

DROP SEQUENCE staff_sequence;
CREATE SEQUENCE staff_sequence START WITH 1 INCREMENT BY 1;
/

DROP TABLE staff CASCADE CONSTRAINTS;
CREATE TABLE staff (
	StaffID integer DEFAULT staff_sequence.NEXTVAL NOT NULL,
	FName varchar(30) NOT NULL,
	LName varchar(30) NOT NULL,
	PRIMARY KEY (StaffID)
);

INSERT INTO staff (FName, LName) VALUES ('Thomas', 'Ludlow');
INSERT INTO staff (FName, LName) VALUES ('Jeff', 'Marks');
INSERT INTO staff (FName, LName) VALUES ('Bill', 'Bob');

/* ====================[ staff_orders ]=================== */

DROP TABLE staff_orders CASCADE CONSTRAINTS;
CREATE TABLE staff_orders (
	StaffID integer NOT NULL,
	OrderID integer NOT NULL,
	PRIMARY KEY (StaffID, OrderID),
	FOREIGN KEY (StaffID) REFERENCES staff(StaffID),
	FOREIGN KEY (OrderID) REFERENCES orders(OrderID) ON DELETE CASCADE
);