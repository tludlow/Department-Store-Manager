
# CS258 Coursework - u1814232

**Tables**
All the tables have been implemented as described in the Coursework specification. Constraints have been added to the tables so that the data contained is appropriate for the department store. (E.g: the stock of an item in inventory cant go below 0)


**Procedure: create_order**
I have implemented a procedure to create new orders as much of this SQL code was repeated across the first 3 options of the coursework. The final parameter in the create_order procedure, named: *order_lines* is of a custom type I made called *order_items* which is essentially a list of integers like: (1, 5, 2, 3, 5 ,8) which represents for even index items the product id and odd index items the quantity for the (i-1)th quantity provided. I.E: for product 1 you want 5 of them, product 2 you want 3 of them, etc..
This procedure has some basic exception handling built in as an output to help in telling the user of the program the issues that have occurred when creating an order if they so happen to error. The procedure also rolls back upon errors occurring so that nothing transacted within the procedure is committed to the database to preserve integrity of the data.

**Triggers for stock count**
Two triggers have been implemented, namely: *decrement_stock* and *increment_stock* which handle removing stock count from products within the inventory, and adding items to the inventory upon new order_products entities being created. These triggers reduce the amount of sql needed as they handle this mundane task for us. They will never break the constraints imposed upon the table either, meaning they keep the data integrity.

**Views**
I have created three views, namely: *staff_lifetime_success*, *best_products* and *staff_who_sold_best_products* which handle the selection of the data which is from a static input. These reduce the need for using JDBC for simple, naive selections. They are all appropriately commented within the schema.sql file describing their actions/selection output.


**Options**
Data for the options was gotten from the user, in the required way during the menu loop found in *main()*. the data has some basic validation that its right, and if the user manages to bypass these restrictions the database will error too, meaning the user must retry with the correct data. Data output is generally handled through the java formatting method which allows for tabled outputs. This makes it easier to mimic the required outputs as seen in the Coursework specification.

 - Options 1, 2, 3: The first 3 options are all generally implemented in the same way making uyse of the procedure I created in schema.sql. They also insert into the required deliveries and collections table as required for the orders which are of these types. They also output the updated stock through a pretty simple loop across the product id's provided. This could b made better by having a procedure output containing the products and their new amounts to reduce the amount of jdbc required.
 - Option 4: This just selects data from the view called *best_products* and outputs the data as required.
 - Option 5: Uses a fairly simple JDBC selection query where the data provided by the user is dynamic and gets inserted into the query. Makes use of the TO_DATE() oracle function which allows me to select a data which is 8 days or older before the one provided. The option then takes all the product ids provided and deletes them from the orders table, cascading the deletion to the order_products entities and then the inventory stock gets updated automatically by a trigger called decrement_trigger
 - Option 6: Much like option 4, this option just uses a view called staff_lifetime_success and outputs the data found from the views.
 - Option 7 is arguably the most complex option because the data found from the view is not in the required output format. The data from the view could be put into the required output format using a dynamic Oracle PIVOT() which allows you to move the data in rows into the column headers and aggregate the findings. In haven't implemented this but I just store the individual row data into java,util storage methods and then print the data required in the format provided by the coursework specification. The view for this option uses the WITH statement in oracle which allows you to select data, and then make use of it after you have selected it, which is very useful for readability.
 - Option 8 uses a double WITH statement to find the data required and then I inner_join the two views created in the WITH statement. This allows me to find all the staff members who are considered to be employees_of_the_year as required. Inner joins are used almost exclusively in the coursework  because they dont produce spurious output as they only select where both data exists on either side of the join. The SQL  for this option is done in JDBC because the data provided is dynamic to the users input, this could be abstracted out into a procedure in the schema where the data is an input but having some more complex queries within a JDBC query itself is quite nice.


**Improvements**

 - To prevent redundant data being created a customers table could be created to store the information about somebody. E.g their name and possibly address. This could be used within the collections and deliveries tables to reference a customer rather than storing their data for these tables independently. You can have scenarios where the data is duplicated across many rows because many orders have been placed by the same person. This is not the best design there could be for the schema.
- Indexing could be done on the tables so that the DBMS can find the data more quickly in some of the tables. This would allow for more optimal queries. 
- You may also want to consider the possibility of using a database which hasn't got a single point of failure like this one, and running backups on the data and possibly spreading the data across multiple machines to ensure optimal up time for users to access the data
- Caching data in static views such as the ones which make use of the views can be done to prevent queries being ran on the data so often, especially complex queries such as these which will get slow for large amounts of data. This will mean the data can be retrieved from the cache when needed and not the database to reduce database load. The data in the cache should be then updated as required to make sure its up to date.

**Security**
I have not considered the security of the database in the coursework and therefore I don't really use PreparedStatements, usually only Statements. These are fairly easy to change if the coursework was to be more security conscious. The callable statements for options 1,2 3 would also need to be modified to validate the user's input more to prevent injection attacks.
