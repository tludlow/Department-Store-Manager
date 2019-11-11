/* Find Oracle DB version so i can create auto increment sequence! */

DROP TABLE inventory CASCADE CONSTRAINTS;
CREATE TABLE inventory (
	ProductID integer,
	ProductDesc varchar(30),									
	ProductPrice numeric(8,2) NOT NULL,
	ProductStockAmount integer NOT NULL,
	PRIMARY KEY (ProductID),
	CHECK (ProductStockAmount >= 0)
);

CREATE SEQUENCE inventory_sequence
  START WITH 1
  INCREMENT BY 1
  CACHE 100;

/* Every product should have a price, therefore NOT NULL constraint, Every product should also have stock, therefore we check that its stock amount is > -1 and NOT NULL*/
/* Can't realistically have negative stock in our implementation therefore we have a check that the stock is not less than 0.*/

DROP TABLE orders CASCADE CONSTRAINTS;
CREATE TABLE orders (
	OrderID integer,
	OrderType varchar(30) not NULL,
	OrderCompleted integer not NULL,
	OrderPlaced Date not NULL,
	PRIMARY KEY (OrderID),
	CHECK (OrderType IN ('InStore', 'Collection', 'Delivery')),
	CHECK (OrderCompleted IN ('0', '1'))
);
Added not NULL constraints as every order must have a type (so we know if we need to use the deliveries or collections table) and every order should have if it is complete or not.
CHECK constraints used for OrderType and OrderComplete because they have a limited domain.
DROP TABLE order_products CASCADE CONSTRAINTS;
CREATE TABLE order_products (
	OrderID integer,
	ProductID integer,
	ProductQuantity integer not NULL,
	PRIMARY KEY (OrderID, ProductID),
	FOREIGN KEY (ProductID) REFERENCES inventory(ProductID),
	FOREIGN KEY (OrderID) REFERENCES orders(OrderID) ON DELETE CASCADE
);
//Added not NULL constraint to ProductQuantity as every order should say how much of a product is required.
DROP TABLE deliveries CASCADE CONSTRAINTS;
CREATE TABLE deliveries (
	OrderID integer,
	FName varchar(30) not NULL,
	LName varchar(30) not NULL,
	House varchar(30) not NULL,
	Street varchar(30) not NULL,
	City varchar(30) not NULL,
	DeliveryDate Date not NULL,
	PRIMARY KEY (OrderID),
	FOREIGN KEY (OrderID) REFERENCES orders(OrderID) ON DELETE CASCADE
);
//Added not NULLs as FName/LName/House/Street/City/DeliveryDate as all required for a successful delivery.
DROP TABLE collections CASCADE CONSTRAINTS;
CREATE TABLE collections (
	OrderID integer,
	FName varchar(30) not NULL,
	LName varchar(30) not NULL,
	CollectionDate Date not NULL,
	PRIMARY KEY (OrderID),
	FOREIGN KEY (OrderID) REFERENCES orders(OrderID) ON DELETE CASCADE
);
//Added not NULL constraints as we need to know who is doing the collecting (hence both names) + the expected collection day needs to be known.
DROP TABLE staff CASCADE CONSTRAINTS;
CREATE TABLE staff (
	StaffID integer,
	FName varchar(30) not NULL,
	LName varchar(30) not NULL,
	PRIMARY KEY (StaffID)
);
//Added not NULL constraints as we need to know the names of each staff member.
DROP TABLE staff_orders CASCADE CONSTRAINTS;
CREATE TABLE staff_orders (
	StaffID integer,
	OrderID integer,
	PRIMARY KEY (StaffID, OrderID),
	FOREIGN KEY (StaffID) REFERENCES staff(StaffID),
	FOREIGN KEY (OrderID) REFERENCES orders(OrderID) ON DELETE CASCADE
);


DESIGN CHOICES:
-Have a customer table, with a customer ID, and then name and address info so that in deliveries and collections you could have a column for customer IDs so that there is no
need to store their name and addresses each time. When a single customer has more than one order for delivery or collection this will save space.
-Remove the staff_orders table, and instead in orders have a column for the staffID of the staff member that served the customer. This reduces the need to do an additional join
when wanting to get information about which staff members have sold which products (i.e only one join required (between order_products and orders), rather than three (between order_products, orders, and staff_orders).
This will reduce the complexity from some queries.
