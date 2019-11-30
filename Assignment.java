import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

class Assignment {

	private static String readEntry(String prompt) {
		try {
			StringBuffer buffer = new StringBuffer();
			System.out.print(prompt);
			System.out.flush();
			int c = System.in.read();
			while(c != '\n' && c != -1) {
				buffer.append((char)c);
				c = System.in.read();
			}
			return buffer.toString().trim();
		} catch (IOException e) {
			return "";
		}
	 }
	 
	
	 /**
	  * Generate a custom string to use in the create_order sql procedure.
	  * @param productIDs The products you want to have
	  * @param quantities The quantities you want to have, matches up against product IDS
	  * @return The string representing the sql to these items in the custom sql type order_items.
	  */
	private static String orderItemsGenerator(int[] productIDs, int[] quantities) {
		String dynamicOrderItems = "order_items(";
		for(int i =0; i < productIDs.length; i++) {
			if (i == 0) {
				dynamicOrderItems = dynamicOrderItems + productIDs[i] + "," + quantities[i];
			} else {
				dynamicOrderItems = dynamicOrderItems + "," + productIDs[i] + "," + quantities[i];
			}
		}
		dynamicOrderItems = dynamicOrderItems + ")";

		return dynamicOrderItems;
	}

	private static int getMaxOrder(Connection conn) throws SQLException {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT max(OrderID) FROM orders");
			if(rs.next()) {
				return rs.getInt("MAX(ORDERID)");
			}
			
			return -1;
		} catch (SQLException e) {
			throw e;
		}
	}
	 

	/**
	* @param conn An open database connection 
	* @param productIDs An array of productIDs associated with an order
    * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID) {
		//Option 1 is implemented as a sql procedure.
		//create_orders(order_type, order_completed, order_date, staff_id, order_items(...variadicProductID, quantitiessold))

		//the following code dynamic generates the order_items type made in my sql code.
		String dynamicOrderItems = orderItemsGenerator(productIDs, quantities);

		//End of the dynamic generate of the order_items type.
		boolean toPrintStock = true;
		try {
			CallableStatement storedProcedure = conn.prepareCall("{ call create_order('InStore', '1', ?, ?, " + dynamicOrderItems + ") }");
			storedProcedure.setString(1, orderDate);
			storedProcedure.setInt(2, staffID);
			storedProcedure.execute();
			storedProcedure.close();
		} catch (SQLException e) {
			System.out.println("\n\nError running option 1.\n\n");
			toPrintStock = false;
			if(e.getMessage().contains(", line 30")) {
				System.out.println("Invalid product ID entered, one of your products doesn't exist!");
			}
			if(e.getMessage().contains(", line 17")) {
				System.out.println("Invalid staff id, this staff member doesn't exist!");
			}
			if(e.getMessage().contains("DECREMENT_STOCK") || e.getMessage().contains("POSITIVE_STOCK_CHECK")) {
				System.out.println("One of the items you are trying to order doesn't have enough stock for your order.");
			}
			if(e.getMessage().contains("OPS$U1814232.CREATE_ORDER")) {
				System.out.println("Error running create_order procedure because of above errors.");
			}
		}

		//Get the updated stock for the items.
		if(toPrintStock) {
			try {
				String query = "SELECT ProductID, ProductStockAmount FROM inventory WHERE ";
				//Dynamicly generate sql string.
				for(int i=0; i<productIDs.length; i++) {
					//Don't add an OR to the final product.. only the ones beforehand.
					if(i == productIDs.length - 1) {
						query = query + "ProductID=" + productIDs[i];
					} else {
						query = query + "ProductID=" + productIDs[i] + " OR ";
					}
				}
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()) {
					System.out.println("Product ID " + rs.getString(1) + " stock is now at " + rs.getString(2) + ".");
				}
				stmt.close();
			} catch (SQLException e) {
				System.out.println("Error getting updated stock for your order.");
			}
		}
	}

	/**
	* @param conn An open database connection 
	* @param productIDs An array of productIDs associated with an order
    * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param collectionDate A string in the form of 'DD-Mon-YY' that represents the date the order will be collected
	* @param fName The first name of the customer who will collect the order
	* @param LName The last name of the customer who will collect the order
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option2(Connection conn, int[] productIDs, int[] quantities, String orderDate, String collectionDate, String fName, String LName, int staffID) {
		//Option 2 is implemented as a sql procedure.
		//create_orders(order_type, order_completed, order_date, staff_id, order_items(...variadicProductID, quantitiessold))

		//the following code dynamic generates the order_items type made in my sql code.
		String dynamicOrderItems = orderItemsGenerator(productIDs, quantities);

		//End of the dynamic generate of the order_items type.
		boolean toPrintStock = true;
		boolean toMakeCollection = true;
		try {
			CallableStatement storedProcedure = conn.prepareCall("{ call create_order('Collection', '0', ?, ?, " + dynamicOrderItems + ") }");
			storedProcedure.setString(1, orderDate);
			storedProcedure.setInt(2, staffID);
			storedProcedure.execute();
			storedProcedure.close();
		} catch (SQLException e) {
			System.out.println("\n\nError running option 2.\n\n");
			toPrintStock = false;
			toMakeCollection = false;
			if(e.getMessage().contains(", line 30")) {
				System.out.println("Invalid product ID entered, one of your products doesn't exist!");
			}
			if(e.getMessage().contains(", line 17")) {
				System.out.println("Invalid staff id, this staff member doesn't exist!");
			}
			if(e.getMessage().contains("DECREMENT_STOCK") || e.getMessage().contains("POSITIVE_STOCK_CHECK")) {
				System.out.println("One of the items you are trying to order doesn't have enough stock for your order.");
			}
			if(e.getMessage().contains("OPS$U1814232.CREATE_ORDER")) {
				System.out.println("Error running create_order procedure because of above errors.");
			}
		}

		//Should only make a collection if no errors creating the order.

		if(toMakeCollection) {
			try {
				//Get the order id we just created.
				int orderID = getMaxOrder(conn);

				Statement stmt2 = conn.createStatement();
				String collectionInsertQuery = "INSERT INTO collections VALUES(" + orderID + ", '" + fName + "', '" + LName + "', '" + collectionDate + "')";
				stmt2.executeUpdate(collectionInsertQuery);

				stmt2.close();
			} catch (SQLException e) {
				toPrintStock = false;
				System.out.println("Error creating collection details!");
			}
		}

		//Get the updated stock for the items.
		if(toPrintStock) {
			try {
				String query = "SELECT ProductID, ProductStockAmount FROM inventory WHERE ";
				//Dynamicly generate sql string.
				for(int i=0; i<productIDs.length; i++) {
					//Don't add an OR to the final product.. only the ones beforehand.
					if(i == productIDs.length - 1) {
						query = query + "ProductID=" + productIDs[i];
					} else {
						query = query + "ProductID=" + productIDs[i] + " OR ";
					}
				}
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()) {
					System.out.println("Product ID " + rs.getString(1) + " stock is now at " + rs.getString(2) + ".");
				}
				stmt.close();
			} catch (SQLException e) {
				System.out.println("Error getting updated stock for your order.");
			}
		}
	}

	/**
	* @param conn An open database connection 
	* @param productIDs An array of productIDs associated with an order
    * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param deliveryDate A string in the form of 'DD-Mon-YY' that represents the date the order will be delivered
	* @param fName The first name of the customer who will receive the order
	* @param LName The last name of the customer who will receive the order
	* @param house The house name or number of the delivery address
	* @param street The street name of the delivery address
	* @param city The city name of the delivery address
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option3(Connection conn, int[] productIDs, int[] quantities, String orderDate, String deliveryDate, String fName, String LName,
				   String house, String street, String city, int staffID) {
		// Incomplete - Code for option 3 goes here
	}

	/**
	* @param conn An open database connection 
	*/
	public static void option4(Connection conn) {
		// Incomplete - Code for option 4 goes here
	}

	/**
	* @param conn An open database connection 
	* @param date The target date to test collection deliveries against
	*/
	public static void option5(Connection conn, String date) {
		// Incomplete - Code for option 5 goes here
	}

	/**
	* @param conn An open database connection 
	*/
	public static void option6(Connection conn) {
		//Select all of the data in our view created in schema.sql. The view is called: 'staff_lifetime_success'
		Statement stmt;
		String query = "SELECT * FROM staff_lifetime_success";

		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			System.out.format("%-20s   %-14s%n", "EmployeeName,", "TotalValueSold");
			while(rs.next()) {
				System.out.format("%-20s   %-14s%n", rs.getString("FNAME") + " " + rs.getString("LNAME") + ",", "£" + rs.getInt("STAFF_AMOUNT_SOLD"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n\nError finding staff life-time success.\n\n");
		}
	}

	/**
	* @param conn An open database connection 
	*/
	public static void option7(Connection conn) {
		// Incomplete - Code for option 7 goes here
	}

	/**
	* @param conn An open database connection 
	* @param year The target year we match employee and product sales against
	*/
	public static void option8(Connection conn, int year) {
		// Incomplete - Code for option 8 goes here
	}

	public static Connection getConnection() {
			// User and password should be left blank. Do not alter!
			String user = "";
        	String passwrd = "";
        	Connection conn;

	        try {
	            Class.forName("oracle.jdbc.driver.OracleDriver");
	        } catch (ClassNotFoundException x) {
	            System.out.println("Driver could not be loaded");
	        }

	        try {
	        	
	            conn = DriverManager.getConnection("jdbc:oracle:thin:@arryn-ora-prod-db-1.warwick.ac.uk:1521:cs2db",user,passwrd);
	        	//conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:7100:daisy",user,passwrd);
	        	
	            return conn;
	        } catch(SQLException e) {
	        	e.printStackTrace();
	            System.out.println("Error retrieving connection");
	            return null;
	        }

	}

	public static SimpleDateFormat dFormat = new SimpleDateFormat("dd-MMM-yy");

	public static void main(String args[]) throws SQLException, IOException {
		// You should only need to fetch the connection details once
		Connection conn = getConnection();
		
		boolean carryOn = true;
		while (carryOn) {
			String choiceMade = "";
			
			System.out.println("\n\n\n(1) In-Store Purchases");
			System.out.println("(2) Collection");
			System.out.println("(3) Delivery");
			System.out.println("(4) Biggest Sellers");
			System.out.println("(5) Reserved Stock");
			System.out.println("(6) Staff Life-Time Success");
			System.out.println("(7) Staff Contribution");
			System.out.println("(8) Employees of the Year");
			System.out.println("(0) Quit");
			choiceMade = readEntry("Enter your choice: ");
			
			
			switch (choiceMade) {
				case "0":
					System.out.println("\n\nExiting department store database.\n\n");
					carryOn = false;
					break;
				case "1":
					System.out.println("\nYou have picked in-store purchases");
					ArrayList<Integer> products = new ArrayList<>();
					ArrayList<Integer> quantities = new ArrayList<>();
					while (true) {
						int newProductID = -1;

						//Get the product ID.
						while (true) {
							try {
								newProductID = Integer.parseInt(readEntry("Enter a product ID: "));
								break;
							} catch (NumberFormatException e) {
								System.out.println("Invalid product ID, must be an integer!");
							}
						}
						products.add(newProductID);

						//Get the quantity.
						int newQuantitySold = -1;
						while (true) {
							try {
								newQuantitySold = Integer.parseInt(readEntry("Enter the quantity sold: "));
								break;
							} catch (NumberFormatException e) {
								System.out.println("Invalid quantity sold, must be an integer!");
							}
						}
						quantities.add(newQuantitySold);

						//See if more products sold, if so restart the loop and get the other products and the quantities.
						String isMore = readEntry("Is there another product in the order?: ");
						if(isMore.equals("Y")) {
							continue;
						}

						//Have all the products sold, now get the dates.
						String dateOrdered = "";
						while (true) {
							dateOrdered = readEntry("Enter the date sold: ");
							try {
								dFormat.parse(dateOrdered);
								break;
							} catch (ParseException e) {
								System.out.println("Not a valid date, must be of the form DD-Mon-YYYY");
							}
						}

						int staffID = -1;
						while (true) {
							try {
								staffID = Integer.parseInt(readEntry("Enter your staff ID: "));
								break;
							} catch (NumberFormatException e) {
								System.out.println("Invalid staff ID, must be an integer!");
							}
						}

						int[] productArray = new int[products.size()];
						int[] quantitiesArray = new int[products.size()];
						for(int i=0; i<products.size(); i++) {
							productArray[i] = products.get(i);
							quantitiesArray[i] = quantities.get(i);
						}

						//Call option 1, we have all the details.
						option1(conn, productArray, quantitiesArray, dateOrdered, staffID);
						break;
					}
					break;
				case "2":
					System.out.println("\nYou have picked, collection");
					ArrayList<Integer> products2 = new ArrayList<>();
					ArrayList<Integer> quantities2 = new ArrayList<>();
					while (true) {
						int newProductID = -1;

						//Get the product ID.
						while (true) {
							try {
								newProductID = Integer.parseInt(readEntry("Enter a product ID: "));
								break;
							} catch (NumberFormatException e) {
								System.out.println("Invalid product ID, must be an integer!");
							}
						}
						products2.add(newProductID);

						//Get the quantity.
						int newQuantitySold = -1;
						while (true) {
							try {
								newQuantitySold = Integer.parseInt(readEntry("Enter the quantity sold: "));
								break;
							} catch (NumberFormatException e) {
								System.out.println("Invalid quantity sold, must be an integer!");
							}
						}
						quantities2.add(newQuantitySold);

						//See if more products sold, if so restart the loop and get the other products and the quantities.
						String isMore = readEntry("Is there another product in the order?: ");
						if(isMore.equals("Y")) {
							continue;
						}

						//Have all the products sold, now get the dates.
						String dateOrdered = "";
						while (true) {
							dateOrdered = readEntry("Enter the date sold: ");
							try {
								dFormat.parse(dateOrdered);
								break;
							} catch (ParseException e) {
								System.out.println("Not a valid date, must be of the form DD-Mon-YYYY");
							}
						}

						String dateCollection = "";
						while (true) {
							dateCollection = readEntry("Enter the date of collection: ");
							try {
								dFormat.parse(dateCollection);
								break;
							} catch (ParseException e) {
								System.out.println("Not a valid date, must be of the form DD-Mon-YYYY");
							}
						}

						String firstName = "";
						while (true) {
							firstName = readEntry("Enter the first name of the collector: ");
							if(!firstName.isEmpty()) {
								break;
							}
						}

						String lastName = "";
						while (true) {
							lastName = readEntry("Enter the last name of the collector: ");
							if(!lastName.isEmpty()) {
								break;
							}
						}

						int staffID = -1;
						while (true) {
							try {
								staffID = Integer.parseInt(readEntry("Enter your staff ID: "));
								break;
							} catch (NumberFormatException e) {
								System.out.println("Invalid staff ID, must be an integer!");
							}
						}

						int[] productArray = new int[products2.size()];
						int[] quantitiesArray = new int[products2.size()];
						for(int i=0; i<products2.size(); i++) {
							productArray[i] = products2.get(i);
							quantitiesArray[i] = quantities2.get(i);
						}

						option2(conn, productArray, quantitiesArray, dateOrdered, dateCollection, firstName, lastName, staffID);
						break;
					}
					break;
				case "3":
					System.out.println("\nYou have picked, Delivery");
					ArrayList<Integer> products3 = new ArrayList<>();
					ArrayList<Integer> quantities3 = new ArrayList<>();
					while (true) {
						int newProductID = -1;

						//Get the product ID.
						while (true) {
							try {
								newProductID = Integer.parseInt(readEntry("Enter a product ID: "));
								break;
							} catch (NumberFormatException e) {
								System.out.println("Invalid product ID, must be an integer!");
							}
						}
						products3.add(newProductID);

						//Get the quantity.
						int newQuantitySold = -1;
						while (true) {
							try {
								newQuantitySold = Integer.parseInt(readEntry("Enter the quantity sold: "));
								break;
							} catch (NumberFormatException e) {
								System.out.println("Invalid quantity sold, must be an integer!");
							}
						}
						quantities3.add(newQuantitySold);

						//See if more products sold, if so restart the loop and get the other products and the quantities.
						String isMore = readEntry("Is there another product in the order?: ");
						if(isMore.equals("Y")) {
							continue;
						}

						//Have all the products sold, now get the dates.
						String dateOrdered = "";
						while (true) {
							dateOrdered = readEntry("Enter the date sold: ");
							try {
								dFormat.parse(dateOrdered);
								break;
							} catch (ParseException e) {
								System.out.println("Not a valid date, must be of the form DD-Mon-YYYY");
							}
						}

						String dateDelivery = "";
						while (true) {
							dateDelivery = readEntry("Enter the date of collection: ");
							try {
								dFormat.parse(dateDelivery);
								break;
							} catch (ParseException e) {
								System.out.println("Not a valid date, must be of the form DD-Mon-YYYY");
							}
						}

						String firstName = "";
						while (true) {
							firstName = readEntry("Enter the first name of the recipitent: ");
							if(!firstName.isEmpty()) {
								break;
							}
						}

						String lastName = "";
						while (true) {
							lastName = readEntry("Enter the last name of the recipitent: ");
							if(!lastName.isEmpty()) {
								break;
							}
						}

						String house = "";
						while (true) {
							house = readEntry("Enter the house name/no: ");
							if(!house.isEmpty()) {
								break;
							}
						}

						String street = "";
						while (true) {
							street = readEntry("Enter the street: ");
							if(!street.isEmpty()) {
								break;
							}
						}

						String city = "";
						while (true) {
							city = readEntry("Enter the City: ");
							if(!city.isEmpty()) {
								break;
							}
						}

						int staffID = -1;
						while (true) {
							try {
								staffID = Integer.parseInt(readEntry("Enter your staff ID: "));
								break;
							} catch (NumberFormatException e) {
								System.out.println("Invalid staff ID, must be an integer!");
							}
						}

						int[] productArray = new int[products3.size()];
						int[] quantitiesArray = new int[products3.size()];
						for(int i=0; i<products3.size(); i++) {
							productArray[i] = products3.get(i);
							quantitiesArray[i] = quantities3.get(i);
						}

						option3(conn, productArray, quantitiesArray, dateOrdered, dateDelivery, firstName, lastName, house, street, city, staffID);
						break;
					}
					break;
				case "4":
					System.out.println("\nYou have picked, Biggest Sellers");
					break;
				case "5":
					System.out.println("\nYou have picked, Reserved Stock");
					break;
				case "6":
					System.out.println("\nYou have picked, Staff Life Time Success");
					option6(conn);
					break;
				case "7":
					System.out.println("\nYou have picked, Staff Contribution");
					break;
				case "8":
					System.out.println("\nYou have picked, Employee of the Year");
					break;
				default:
					System.out.println("\n" + choiceMade + " is not a valid choice, try again");
					break;
			}
		}

		conn.close();
	}
}