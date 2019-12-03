SELECT CONCAT(CONCAT(FName, ' '), Lname) AS STAFF_NAME FROM (
WITH MostSoldProducts AS ( 
    SELECT inventory.ProductID AS ProductID
    FROM inventory
        INNER JOIN order_products ON inventory.ProductID = order_products.ProductID
        INNER JOIN orders ON order_products.OrderID = orders.OrderID
    WHERE EXTRACT(YEAR FROM orders.OrderPlaced) = 2019
    HAVING SUM(inventory.ProductPrice * order_products.ProductQuantity) > 20000
    GROUP BY inventory.ProductID
), StaffSold AS (
    SELECT staff.FName AS FName, staff.LName as LName, staff.StaffID AS StaffID, inventory.ProductID AS ProductID,
    SUM(inventory.ProductPrice * order_products.ProductQuantity) AS Revenue
    FROM staff
        INNER JOIN staff_orders ON staff.StaffID = staff_orders.StaffID
        INNER JOIN orders ON staff_orders.OrderID = orders.OrderID
        INNER JOIN order_products ON orders.OrderID = order_products.OrderID
        INNER JOIN inventory ON order_products.ProductID = inventory.ProductID
    WHERE EXTRACT(YEAR FROM orders.OrderPlaced) = 2019
    GROUP BY staff.FName, staff.LName, staff.StaffID, inventory.ProductID
)
SELECT StaffSold.FName AS FName, StaffSold.LName AS LName
FROM StaffSold
    INNER JOIN (
        SELECT StaffSold.StaffID, COUNT(StaffSold.ProductID) AS StaffSalesOfMostSold
        FROM StaffSold
        WHERE StaffSold.ProductID IN (SELECT MostSoldProducts.ProductID FROM MostSoldProducts)
        GROUP BY StaffSold.StaffID 
    ) 
    StaffProductsCount ON StaffSold.StaffID = StaffProductsCount.StaffID
WHERE StaffProductsCount.StaffSalesOfMostSold = (SELECT COUNT(*) FROM MostSoldProducts)
HAVING SUM(StaffSold.Revenue) >= 30000
GROUP BY StaffSold.FName, StaffSold.LName, StaffSold.StaffID
);





WITH BestStaff AS (
    SELECT staff.FName AS FName, staff.LName AS LName, staff.StaffID AS StaffID, inventory.ProductID AS ProductID,
    inventory.ProductPrice AS ProductPrice, SUM(ProductQuantity) AS ProductSoldAmount
    FROM staff
        INNER JOIN staff_orders ON staff.StaffID = staff_orders.StaffID
        INNER JOIN order_products ON staff_orders.OrderID = order_products.OrderID
        INNER JOIN inventory ON order_products.ProductID = inventory.ProductID
    WHERE inventory.ProductID IN ( 
        SELECT inventory.ProductID
        FROM inventory
            INNER JOIN order_products ON inventory.ProductID = order_products.ProductID
        GROUP BY inventory.ProductID
        HAVING SUM(inventory.ProductPrice * order_products.ProductQuantity) > 20000
    ) GROUP BY staff.FName, staff.LName, staff.StaffID, inventory.ProductID, inventory.ProductPrice
)
SELECT BestStaff.FName, BestStaff.LName, BestStaff.StaffID, BestStaff.ProductID, BestStaff.ProductSoldAmount
FROM BestStaff
INNER JOIN (
    SELECT BestStaff.StaffID AS StaffID, SUM(BestStaff.ProductSoldAmount * BestStaff.ProductPrice) AS StaffSoldAmount
    FROM BestStaff
    GROUP BY BestStaff.StaffID
    ) StaffTotalBestSellers ON BestStaff.StaffID = StaffTotalBestSellers.StaffID
ORDER BY StaffTotalBestSellers.StaffSoldAmount DESC;


/** pivot testing **/

-- Products selling over 20k
SELECT inventory.ProductID
FROM inventory
    INNER JOIN order_products ON inventory.ProductID = order_products.ProductID
GROUP BY inventory.ProductID
HAVING SUM(inventory.ProductPrice * order_products.ProductQuantity) > 20000

--Staff who have sold products that are included in the ones selling 20k or more.
SELECT staff.StaffID, staff.Fname, staff.LName, inventory.ProductID, SUM(order_products.ProductQuantity) AS quantity_sold
FROM staff
INNER JOIN staff_orders ON staff.StaffID = staff_orders.StaffID
INNER JOIN order_products ON staff_orders.OrderID = order_products.OrderID
INNER JOIN inventory ON order_products.ProductID = inventory.ProductID
WHERE inventory.ProductID IN (
    SELECT inventory.ProductID
    FROM inventory
        INNER JOIN order_products ON inventory.ProductID = order_products.ProductID
    GROUP BY inventory.ProductID
    HAVING SUM(inventory.ProductPrice * order_products.ProductQuantity) > 20000
);

