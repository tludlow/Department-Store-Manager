import java.io.*;
import java.sql.*;

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
	* @param conn An open database connection 
	* @param productIDs An array of productIDs associated with an order
    * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID) {
		Statement stmt = null;
		String query;
		try {

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("\n\n ERROR INSERTING PRODUCT [OPTION 1] \n\n");
		} finally {
			if (stmt != null) { stmt.close(); }
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
		// Incomplete - Code for option 2 goes here
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
		// Incomplete - Code for option 6 goes here
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

	public static void main(String args[]) throws SQLException, IOException {
		// You should only need to fetch the connection details once
		Connection conn = getConnection();
		
		boolean carryOn = true;
		while (carryOn) {
			String choiceMade = "";
			
			System.out.println("(1) In-Store Purchases");
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
					System.out.println("\nyou have picked in-store purchases");
					break;
				case "2":
					System.out.println("\nYou have picked, collection");
					break;
				default:
					System.out.println("\n" + choiceMade + " is not a valid choice, try again");
			}
		}

		conn.close();
	}
}