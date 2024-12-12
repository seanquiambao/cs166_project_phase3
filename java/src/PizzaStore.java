/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

	// reference to physical database connection.
	private Connection _connection = null;

	// handling the keyboard inputs through a BufferedReader
	// This variable can be global for convenience.
	static BufferedReader in = new BufferedReader(
			new InputStreamReader(System.in));

	/**
	 * Creates a new instance of PizzaStore
	 *
	 * @param hostname the MySQL or PostgreSQL server hostname
	 * @param database the name of the database
	 * @param username the user name used to login to the database
	 * @param password the user login password
	 * @throws java.sql.SQLException when failed to make a connection.
	 */
	public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");

			// obtain a physical connection
			this._connection = DriverManager.getConnection(url, user, passwd);
			System.out.println("Done");
		}catch (Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
			System.out.println("Make sure you started postgres on this machine");
			System.exit(-1);
		}//end catch
	}//end PizzaStore

	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 *
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 */
	public void executeUpdate (String sql) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
		stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 ** obtains the metadata object for the returned result set.  The metadata
		 ** contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		// iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.printf(rsmd.getColumnName(i) + "\t\t");
				}
				System.out.println();
				outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.printf(rs.getString (i) + "\t\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close();
		return rowCount;
	}//end executeQuery

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 *
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 ** obtains the metadata object for the returned result set.  The metadata
		 ** contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		// iterates through the result set and saves the data returned by the query.
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>();
		while (rs.next()){
			List<String> record = new ArrayList<String>();
			for (int i=1; i<=numCol; ++i)
				record.add(rs.getString (i));
			result.add(record);
		}//end while
		stmt.close ();
		return result;
	}//end executeQueryAndReturnResult

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		// iterates through the result set and count nuber of results.
		while (rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current
	 * value of sequence used for autogenerated keys
	 *
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();

		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next())
			return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
			// ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 *
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
					"Usage: " +
					"java [-classpath <classpath>] " +
					PizzaStore.class.getName () +
					" <dbname> <port> <user>");
			return;
		}//end if

		Greeting();
		PizzaStore esql = null;
		try{
			// use postgres JDBC driver.
			Class.forName ("org.postgresql.Driver").newInstance ();
			// instantiate the PizzaStore object and creates a physical
			// connection.
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			esql = new PizzaStore (dbname, dbport, user, "");

			boolean keepon = true;
			while(keepon) {
				// These are sample SQL statements
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Create user");
				System.out.println("2. Log in");
				System.out.println("9. < EXIT");
				String authorisedUser = null;
				switch (readChoice()){
					case 1: CreateUser(esql); break;
					case 2: authorisedUser = LogIn(esql); break;
					case 9: keepon = false; break;
					default : System.out.println("Unrecognized choice!"); break;
				}//end switch
				if (authorisedUser != null) {
					boolean usermenu = true;
					while(usermenu) {
						System.out.println("MAIN MENU");
						System.out.println("---------");
						System.out.println("1. View Profile");
						System.out.println("2. Update Profile");
						System.out.println("3. View Menu");
						System.out.println("4. Place Order"); //make sure user specifies which store
						System.out.println("5. View Full Order ID History");
						System.out.println("6. View Past 5 Order IDs");
						System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
						System.out.println("8. View Stores"); 

						//**the following functionalities should only be able to be used by drivers & managers**
						System.out.println("9. Update Order Status");

						//**the following functionalities should ony be able to be used by managers**
						System.out.println("10. Update Menu");
						System.out.println("11. Update User");

						System.out.println(".........................");
						System.out.println("20. Log out");
						switch (readChoice()){
							case 1: viewProfile(esql, authorisedUser); break;
							case 2: updateProfile(esql, authorisedUser); break;
							case 3: viewMenu(esql); break;
							case 4: placeOrder(esql); break;
							case 5: viewAllOrders(esql); break;
							case 6: viewRecentOrders(esql); break;
							case 7: viewOrderInfo(esql); break;
							case 8: viewStores(esql); break;
							case 9: updateOrderStatus(esql); break;
							case 10: updateMenu(esql, authorisedUser); break;
							case 11: authorisedUser = updateUser(esql, authorisedUser); break;



							case 20: usermenu = false; break;
							default : System.out.println("Unrecognized choice!"); break;
						}
					}
				}
			}//end while
		}catch(Exception e) {
			System.err.println (e.getMessage ());
		}finally{
			// make sure to cleanup the created table and close the connection.
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if
			}catch (Exception e) {
				// ignored.
			}//end try
		}//end try
	}//end main

	public static void Greeting(){
		System.out.println(
				"\n\n*******************************************************\n" +
				"              User Interface      	               \n" +
				"*******************************************************\n");
	}//end Greeting

	/*
	 * Reads the users choice given from the keyboard
	 * @int
	 **/
	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	/*
	 * Creates a new user
	 **/
	public static void CreateUser(PizzaStore esql){
		String username;
		String password;
		String phone;
		try {
			System.out.print("\t Enter username: ");
			username = in.readLine();
			System.out.print("\t Enter password: ");
			password = in.readLine();
			System.out.print("\t Enter phone number: ");
			phone = in.readLine();

			String query = String.format("INSERT INTO Users (login, password, role, phoneNum) VALUES ('%s', '%s', 'Customers', '%s')", username, password, phone);
			esql.executeUpdate(query);

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}//end CreateUser


	/*
	 * Check log in credentials for an existing user
	 * @return User login or null is the user does not exist
	 **/
	public static String LogIn(PizzaStore esql){
		String username;
		String password;
		try {
			System.out.print("Enter username: ");
			username = in.readLine();
			System.out.print("Enter password: ");
			password = in.readLine();

			String query = String.format("SELECT * FROM Users WHERE login = '%s' AND password = '%s';", username, password);
			int count = esql.executeQuery(query);
			if (count != 0) return username;	
			System.out.println("Username/Password is wrong, try again.");
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return null;
	}//end

	// Rest of the functions definition go in here

	public static void viewProfile(PizzaStore esql, String authorisedUser) {
		System.out.println("PROFILE");
		System.out.println("----------");
		String user = authorisedUser;
		try {

			String query = String.format("SELECT * FROM Users WHERE login = '%s'", user);
			List<List<String>> result = esql.executeQueryAndReturnResult(query);
			if(result.isEmpty()) {
				throw new Exception("Error! User not found!");
			}
			List<String> profile = result.get(0);
			System.out.println("Username:\t\t" + profile.get(0));	
			System.out.println("Password:\t\t" + profile.get(1));	
			System.out.println("User Role:\t\t" + profile.get(2));	
			System.out.println("Favorite Item:\t\t" + profile.get(3));	
			System.out.println("Phone Number:\t\t" + profile.get(4));	
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void updateProfile(PizzaStore esql, String authorisedUser) {
		System.out.println("CHANGE PROFILE MENU");
		System.out.println("------------------");
		String user = authorisedUser;
		try {

			String query = String.format("SELECT  FROM Users WHERE login = '%s'", user);
			List<List<String>> result = esql.executeQueryAndReturnResult(query);
			if(result.isEmpty()) {
				throw new Exception("Error! User not found!");
			}
			List<String> profile = result.get(0);
			System.out.println("Username:\t\t" + profile.get(0));	
			System.out.println("Password:\t\t" + profile.get(1));	
			System.out.println("User Role:\t\t" + profile.get(2));	
			System.out.println("Favorite Item:\t\t" + profile.get(3));	
			System.out.println("Phone Number:\t\t" + profile.get(4));	
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		System.out.println("1. Change Favorite Item"); 
		System.out.println("2. Change Phone Number"); 
		System.out.println("3. Change Password"); 
		System.out.println("4. Return Home"); 
		switch(readChoice()) {
			case 1: editProfile(esql, user, "favoriteItems"); break;
			case 2: editProfile(esql, user, "phoneNum"); break;
			case 3: editProfile(esql, user, "password"); break;
			case 4: return; 
			default: System.out.println("Unrecognizable choice!"); break;
		}
	}
	public static void viewMenu(PizzaStore esql) {

		System.out.println("1. Search by type");
		System.out.println("2. Search by price");
		System.out.println("3. Search all items");

		String select = "SELECT * FROM Items i";
		String condition = "";
		String query = "";
		switch(readChoice()) {
			case 1: condition = viewByTypes(esql); break;
			case 2: condition = viewByCost(esql); break;
			case 3: break;
			default: return; 
		}

		int choice = 3;
		while(choice <= 3) {
			try {
				query = select + " " + condition;
				if(choice == 1) query += " ORDER BY price DESC";	
				else if(choice ==  2) query += " ORDER BY price ASC";	
				System.out.println(query);
				List<List<String>> results = esql.executeQueryAndReturnResult(query);
				for(List<String> result : results) {
					System.out.println(String.format("Name: \t\t\t%s", result.get(0)));
					System.out.println(String.format("Ingredients: \t\t%s", result.get(1)));
					System.out.println(String.format("Type: \t\t\t%s", result.get(2)));
					System.out.println(String.format("Cost: \t\t\t%s", result.get(3)));
					System.out.println(String.format("Description: \t\t%s\n", result.get(4)));
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());	
			}

			System.out.println("1. View by highest to lowest");
			System.out.println("2. View by lowest to highest");
			System.out.println("3. View unsorted");
			System.out.println("4. Exit");
			choice = readChoice();
		}

	}

	public static String viewByTypes(PizzaStore esql) {
		System.out.println("1. Entree");
		System.out.println("2. Sides");
		System.out.println("3. Drinks");

		String condition = "";
		switch(readChoice()) {
			case 1: condition = " entree"; break;
			case 2: condition = " sides"; break;
			default: condition = " drinks"; break;
		}

		return String.format("WHERE i.typeOfItem = '%s'", condition);
	}
	public static String viewByCost(PizzaStore esql) {
		String cost = input("the maximum cost (price under...)", "numeric");	
		return String.format("WHERE price BETWEEN 0 AND %s", cost); 
	}
	public static void placeOrder(PizzaStore esql) {
		Scanner readInput = new Scanner(system.in);

        
		viewStores(esql);
		System.out.print("Enter the store id of the store you would like to place an order at: ");
		int storeID = readInput.nextInt();
		readInput.nextLine();

		String getItemsQuery = "SELECT * FROM Items";
		List<List<String>> storeItems = esql.executeQueryAndReturnResult(getItemsQuery);
		System.out.println("Menu:");
        for (List<String> item : menu) {
            System.out.printf("Item: %s, Price: $%s, Description: %s\n",
                              item.get(0), item.get(3), item.get(4));
        }

		List<String> userOrder = new ArrayList<>();
        List<Integer> orderQuantities = new ArrayList<>();
        boolean keepAdding = true;

		while (keepAdding) {
            System.out.print("Enter the item that you want to add to your order: ");
            String itemName = readInput.nextLine();

            System.out.print("Enter the quantity that you want of this item: ");
            int quantity = readInput.nextInt();
            readInput.nextLine();

            userOrder.add(itemName);
            userQuantities.add(quantity);

            System.out.print("Do you want to order more items? (yes/no): ");
            String userResponse = readInput.nextLine();
            keepGoing = userResponse.equals("yes");
        }

		double orderPrice = 0.0;
		for (int iter = 0; iter < userOrder.size(); iter++) {
            String getPriceQuery = String.format("SELECT price FROM Items WHERE itemName = '%s'", userOrder.get(iter));
            List<List<String>> priceList = esql.executeQueryAndReturnResult(getPriceQuery);

            if (!priceResult.isEmpty()) {
              double price = Double.parseDouble(priceResult.get(0).get(0));
              orderPrice += price * quantities.get(iter);
            }
        }

		String placeOrderQuery = String.format("INSERT INTO FoodOrder (login, storeID, totalPrice, orderTimestamp, orderStatus) VALUES ('%s', %d, %.2f, CURRENT_TIMESTAMP, 'incomplete') RETURNING orderID",
                              authorisedUser, storeID, totalPrice);
        List<List<String>> placeOrder = esql.executeQueryAndReturnResult(placeOrderQuery);
        int orderID = Integer.parseInt(orderResult.get(0).get(0));

		for (int iter = 0; iter < userOrder.size(); iter++) {
            String insertOrderQuery = String.format("INSERT INTO ItemsInOrder (orderID, itemName, quantity) VALUES (%d, '%s', %d)",
                                  orderID, orderItems.get(iter), quantities.get(iter));
            esql.executeUpdate(insertOrderQuery);
        }

		System.out.printf("Your order has been placed successfully! Order ID: %d, Total Price: $%.2f\n", orderID, orderPrice);
	}
	public static void viewAllOrders(PizzaStore esql) {
		String getUsersOrdersQuery = String.format("SELECT * FROM FoodOrder WHERE login = '%s'", authorisedUser);
		List<List<String>> currUsersOrders = esql.executeQueryAndReturnResult(getUsersOrdersQuery);

		if (currUsersOrders.isEmpty()) {
            System.out.println("You have no order history.");
            return;
        }

		System.out.println("These are all of the orders you have ever made: ");
        for (List<String> order : orders) {
            System.out.printf("Order ID: %s, Store ID: %s, Total Price: $%s, Timestamp: %s, Status: %s\n",
                              order.get(0), order.get(2), order.get(3), order.get(4), order.get(5));
        }
	}
	public static void viewRecentOrders(PizzaStore esql) {
		String getUsersOrdersQuery = String.format(
            "SELECT * FROM FoodOrder WHERE login = '%s' ORDER BY orderTimestamp DESC LIMIT 5",
            authorisedUser
        );
        List<List<String>> currUsersOrders = esql.executeQueryAndReturnResult(query);

        if (currUsersOrders.isEmpty()) {
            System.out.println("You have no order history.");
            return;
        }

        System.out.println("Your 5 Most Recent Orders:");
        for (List<String> order : orders) {
            System.out.printf("Order ID: %s, Store ID: %s, Total Price: $%s, Timestamp: %s, Status: %s\n",
                              order.get(0), order.get(2), order.get(3), order.get(4), order.get(5));
        }
	}
	public static void viewOrderInfo(PizzaStore esql) {
		Scanner readInput = new Scanner(system.in);
		System.out.print("Enter the Order ID of the order you want to view: ");
		int orderID = scanner.nextInt();

        String viewOrderQuery;
        if (role.equalsIgnoreCase("manager") || role.equalsIgnoreCase("driver")) {
            viewOrderQuery = String.format("SELECT * FROM FoodOrder WHERE orderID = %d", orderID);
        } else {
            viewOrderQuery = String.format("SELECT * FROM FoodOrder WHERE orderID = %d AND login = '%s'", orderID, authorisedUser);
        }

        List<List<String>> userOrders = esql.executeQueryAndReturnResult(viewOrderQuery);

        if (userOrders.isEmpty()) {
            System.out.println("No order found with the given ID or you do not have permission to view it.");
            return;
        }

        List<String> order = userOrders.get(0);
        System.out.printf("Order ID: %s\nOrder Timestamp: %s\nTotal Price: $%s\nOrder Status: %s\n",
                          order.get(0), order.get(4), order.get(3), order.get(5));

        viewOrderQuery = String.format("SELECT itemName, quantity FROM ItemsInOrder WHERE orderID = %d", orderID);
        List<List<String>> items = esql.executeQueryAndReturnResult(viewOrderQuery);

        System.out.println("Items in this Order:");
        for (List<String> item : items) {
            System.out.printf("- %s (Quantity: %s)\n", item.get(0), item.get(1));
        }
	}
	public static void viewStores(PizzaStore esql) {
		System.out.println("These are the open stores that you can place an order at: ");
		Scanner readInput = new Scanner(system.in);
		String getStoresQuery = "SELECT * FROM Store WHERE isOpen = 'yes'";
		List<List<String>> availableStores = esql.executeQueryAndReturnResult(getStoresQuery);
		for (List<String> store : stores) {
            System.out.printf("Store ID: %s, Address: %s, City: %s, State: %s, Review Score: %s\n",
                              store.get(0), store.get(1), store.get(2), store.get(3), store.get(5));
        }
	}
	public static void updateOrderStatus(PizzaStore esql) {
		
	}
	public static void updateMenu(PizzaStore esql, String authorisedUser) {
		String[] roles = {"manager"};	
		String query = "";
		boolean loop = true;
		if(!authorise(esql, authorisedUser, roles)) {
			System.out.println("You do not have permission to view this");
			return;
		}	


		System.out.println("1. Update Item"); 
		System.out.println("2. Remove Item"); 
		System.out.println("3. Add Item"); 
		System.out.println("4. Return Home"); 

		switch(readChoice()) {
			case 1: query = "UPDATE"; break;
			case 2: query = "DELETE"; break;
			case 3: query = "INSERT"; break;
			case 4: return; 
			default: System.out.println("Unrecognizable choice!"); break;
		}


		if(query.equals("UPDATE")){

			try {
				String name;
				String check;
				String update = "";
				String attribute = "";
				String value = "";
				while(true) {

					name = input("item name ('q' to quit)", "not null");
					if(name.equals("q")) return;
					check = String.format("SELECT * FROM Items WHERE itemName = '%s'", name);
					int count = esql.executeQuery(check);
					if(count > 0) break;
					System.out.println("Item name doesn't exist.");
				}

				while(true) {

					update = "UPDATE Items";
					List<List<String>> result = esql.executeQueryAndReturnResult(String.format("SELECT * FROM Items WHERE itemName = '%s'", name));
					List<String> item = result.get(0);
					System.out.println(String.format("Name: %s", item.get(0))); 
					System.out.println(String.format("Ingredients: %s", item.get(1))); 
					System.out.println(String.format("Type: %s", item.get(2))); 
					System.out.println(String.format("Price: %s", item.get(3))); 
					System.out.println(String.format("Description: %s\n", item.get(4))); 


					System.out.println("1. Edit Ingredients"); 
					System.out.println("2. Edit Type"); 
					System.out.println("3. Edit Price"); 
					System.out.println("4. Edit Description"); 
					System.out.println("5. Exit"); 

					switch(readChoice()) {
						case 1: attribute = "ingredients"; break;
						case 2: attribute = "typeOfItem"; break;
						case 3: attribute = "price"; break;
						case 4: attribute = "description"; break;
						default: return; 
					}
					if(attribute.equals("itemName")) {
						value = input("new item name", "not null");
						update += String.format(" SET %s = '%s' WHERE itemName = '%s'", attribute, value, name);
						name = value;
					}
					else if(attribute.equals("price")) {
						value = input("new price:", "numeric");
						update += String.format(" SET %s = '%s' WHERE itemName = '%s'", attribute, value, name);
					}
					else { 
						value = input(String.format("new %s:", attribute), "not null");
						update += String.format(" SET %s = '%s' WHERE itemName = '%s'", attribute, value, name);
					} 
					System.out.println(update);
					esql.executeUpdate(update);
				}

			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		else if (query.equals("DELETE")) {
			try {

				String name;
				System.out.print("Enter item name: ");
				name = in.readLine();
				query = String.format("DELETE FROM Items WHERE itemName = '%s'", name);	
				esql.executeUpdate(query);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
		else {

			try {
				String name;
				String ingredients;
				String type;
				String price;
				String description;

				name = input("item name", "not null"); 
				ingredients = input("ingredients", "not null"); 
				type = input("item type", "not null"); 
				price = input("price", "numeric"); 
				description = input("description", "na"); 

				query = String.format("INSERT INTO Items(itemName, ingredients, typeOfItem, price, description) VALUES('%s', '%s', '%s', %s, '%s')", name, ingredients, type, price, description);

				esql.executeUpdate(query); 
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

		}

	}

	public static String updateUser(PizzaStore esql, String authorisedUser) {
		String[] roles = {"manager"};
		String user = authorisedUser;
		boolean loop = true;
		if(!authorise(esql, authorisedUser, roles)) {
			System.out.println("You do not have permission");
			return authorisedUser;
		}

		while(loop) {
			int check = 0;	
			try {
				System.out.print("Enter user: ");
				user = in.readLine();
				String query = String.format("SELECT * FROM Users WHERE login = '%s'", user);
				check = esql.executeQuery(query);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

			if(check <= 0) {
				System.out.println("Invalid User!");
				continue;
			}
			loop = false;
		}

		System.out.println("1. Change Favorite Item"); 
		System.out.println("2. Change Phone Number"); 
		System.out.println("3. Change Password"); 
		System.out.println("4. Change Login"); 
		System.out.println("5. Change Roles"); 
		System.out.println("6. Return Home"); 
		switch(readChoice()) {
			case 1: editProfile(esql, user, "favoriteItems"); break;
			case 2: editProfile(esql, user, "phoneNum"); break;
			case 3: editProfile(esql, user, "password"); break;
			case 4: authorisedUser = editUser(esql, authorisedUser, user, "login"); break;
			case 5: authorisedUser = editUser(esql, authorisedUser, user, "role"); break;
			case 6: return authorisedUser;
			default: System.out.println("Unrecognizable choice!"); break;
		}

		return authorisedUser;
	}

	public static void editProfile(PizzaStore esql, String user, String attribute) {
		String text;
		try {
			System.out.print(String.format("Enter %s: ", attribute));
			text = in.readLine(); 
			String query = String.format("UPDATE Users SET %s = '%s' WHERE login = '%s'", attribute, text, user); 			
			esql.executeUpdate(query);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static String editUser(PizzaStore esql, String authorisedUser, String user, String attribute) {
		String text;
		try {
			System.out.print(String.format("Enter new %s: ", attribute));
			text = in.readLine(); 
			String query = String.format("UPDATE Users SET %s = '%s' WHERE login = '%s'", attribute, text, user); 			
			esql.executeUpdate(query);

			if(authorisedUser.equals(user) && attribute.equals("login")) { 
				return text; 
			}	
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return authorisedUser;
	}

	public static boolean authorise(PizzaStore esql, String authorisedUser, String[] roles) {
		try {
			for (String role : roles) {

				String query = String.format("SELECT * FROM Users WHERE login = '%s' AND role = '%s'", authorisedUser, role);
				int check = esql.executeQuery(query);
				if (check > 0) return true;
			}

		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
		return false;
	}

	public static String input(String title, String type) {

		if(type.equals("numeric")) {
			while(true) {
				try {
					System.out.print(String.format("Enter %s: ", title));
					String text = in.readLine();
					if(text.matches("^\\d+(\\.\\d{1,2})?$")) {
						return text;
					}
					System.out.println("Invalid Input. Try again.");
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		} 
		else if(type.equals("not null")) {
			while(true) {
				try {
					System.out.print(String.format("Enter %s: ", title));
					String text = in.readLine();
					if(!text.isEmpty()) {
						return text;
					}
					System.out.println("Invalid Input");
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
		else {
			while(true) {
				try {
					System.out.print(String.format("Enter %s: ", title));
					String text = in.readLine();
					return text;
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
	}

}//end PizzaStore

