import java.sql.*;
import java.util.Properties;
import java.util.LinkedList;
import java.io.*;
import java.util.Scanner;
public class SQLManager {

	/****************Constants********************************************************\
	\*********************************************************************************/
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost/well_2016_schema";

	private static String USER;
	private static String PASS;


	/****************Static and Instance Variables************************************\
	\*********************************************************************************/
	private static SQLManager sqlm;

	/****************Constrcutor******************************************************\
	\*********************************************************************************/

	// returns an SQLManager, only one instance of this object per JVM Run
	public static SQLManager init() {
		if (sqlm == null)
			sqlm = new SQLManager();
		return sqlm;
	}

	// private constructor
	private SQLManager() {
		try {
               System.out.println(new File(".").getAbsolutePath());
               File credentials = new File("../credentials/mysql.CRED");
               Scanner s = new Scanner(credentials);
               USER = s.nextLine();
               PASS = s.nextLine();
			Class.forName(JDBC_DRIVER);
		} catch (Exception e) {
			System.out.println(" err in constructor: ");
			e.printStackTrace();
		}
	}

	/****************Public Actions***************************************************\
	\*********************************************************************************/

	// return the results of a custom query
	public ResultSet customQuery(String query) {
		if (sqlm == null) throw new NullPointerException("SQLManager not initialized properly");
		Connection conn = connect();
		if (conn == null) throw new NullPointerException("customQuery: connection failed");
		return query(query);
	}

	// given a Well, insert that into
	public boolean addToDatabase(Well well) {
		if (well == null)
			throw new NullPointerException("addToDatabase: null well");
		System.out.println("here");
		StringBuilder update = new StringBuilder("INSERT IGNORE INTO wvwells VALUES ");
		update.append(well.getSQLValue());
		execute(update.toString());
		return true;
	}

	// add all wells in iterable to the database, return true
	// if all adds returned non-null, false otherwise
	public boolean addToDatabase(Iterable<Well> wells) {
		if (wells == null)
			throw new NullPointerException("addToDatabase: null well iterable");
		for (Well well : wells)
			if (!addToDatabase(well))
				return false;
		return true;
	}

	public boolean addToDatabase(String sqlstring, String table) {
		if (sqlstring == null)
			throw new NullPointerException("addToDatabase: null string");
		StringBuilder update = new StringBuilder("INSERT IGNORE INTO " + table + " VALUES ");
		update.append(sqlstring);
		execute(update.toString());
		return true;
	}

	// dear god if you fuck this up
	public boolean deleteTable(String table) {
		if (table.equals("wvwells")) throw new IllegalArgumentException("FUCK YOU");
		if (table.equals("test_table")) {
			execute("DELETE FROM test_table");
		}
		return true;
	}

	public Iterable<Well> getWells() {
		return getWells("wvwells");
	}

	public Iterable<Well> getWells(String table) {
		String query = "SELECT * FROM " + table;
		ResultSet results = query(query);
		LinkedList<Well>  wells = new LinkedList<>(); 
		try {
			while (results.next()) {
				wells.add(wellFromResult(results));
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}
		return wells;
	}

	public Iterable<Well> getAbandonedWells() {
		String query = "SELECT * FROM Wvwells WHERE abandoned=";
		ResultSet results = query(query);
		LinkedList<Well>  wells = new LinkedList<>(); 
		try {
			while (results.next()) {
				wells.add(wellFromResult(results));
			}
		} catch (SQLException se) {
			se.printStackTrace();
		}
		return wells;
	}

	/****************Connect and Query helper methods*********************************\
	\*********************************************************************************/

	// establish connection with DB_URL database, returns connection if works, null else
	private static Connection connect() {
		if (sqlm == null) throw new NullPointerException("SQLManager not initialized properly");
		try {
			//System.out.println("Connecting to database... ");
			Properties prop = new Properties();
			prop.setProperty("user", USER);
			prop.setProperty("password", PASS);
			prop.setProperty("useSSL", "false");
			return DriverManager.getConnection(DB_URL, prop);

		} catch (SQLException se) {
			System.out.println("err in connection: ");
			se.printStackTrace();
		} catch (Exception e) {
			System.out.println("err in connection: ");
			e.printStackTrace();
		}
		return null;
	}

	// query connected database given a query string, returns ResultSet if works,
	// null else
	private ResultSet query(String query) {
		if (query == null) throw new NullPointerException("query: null query statement");
		try {
			Connection conn = connect();
			//System.out.println("Creating a statement... ");
			Statement stmt = conn.createStatement();
			return stmt.executeQuery(query);

		} catch (SQLException se) {
			System.out.println("err in query: ");
			se.printStackTrace();
		} catch (Exception e) {
			System.out.println("err in query: ");
			e.printStackTrace();
		}
		return null;
	}

	private void execute(String update) {
		if (update == null) throw new NullPointerException("execute: null update stmt");
		try {
			Connection conn = connect();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(update);
			stmt.close();
			conn.close();
		} catch (SQLException se) {
			System.err.println(se.toString());
			System.err.println();
			System.err.println(update);
			System.err.println();
			//se.printStackTrace();
		}
	}

	private Well wellFromResult(ResultSet rs) {
		try {
			Well well = new Well(rs.getString("api"), rs.getString("status"), rs.getString("lat"), rs.getString("lng"));
			well.setType(rs.getString("type"));
			well.setOwner(rs.getString("owner"));
			well.setElev(rs.getInt("elev"));
			well.setTVDep(rs.getInt("tvdep"));
			well.setTHDev(rs.getInt("thdev"));
			well.setYear(rs.getInt("year"));
			well.setProducts(rs.getString("products"));
			String[] layers = rs.getString("layers").split(",");
			String[] layerTop = rs.getString("layerTop").split(",");
			String[] layerBot = rs.getString("layerBot").split(",");
			String[] layerDev = rs.getString("layerDev").split(",");
			for (int i = 0; i < layers.length; i++) {
				if (layers[i].equals("n/a")) break;
				well.addLayer(layers[i], Integer.parseInt(layerTop[i]), 
									Integer.parseInt(layerBot[i]), Boolean.parseBoolean(layerDev[i]));
			}
			return well;

		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// given a ResultSet, print the top well whose value is in 
	// the current row.
	public static void printWellValue(ResultSet results) {
		try {
			System.out.print("(");
			System.out.print(results.getString("api"));
			System.out.print(", ");
			System.out.print(results.getString("lat"));
			System.out.print(", ");
			System.out.print(results.getString("lng"));
			System.out.print(", ");
			System.out.print(results.getInt("status"));
			System.out.print(", ");
			System.out.print(results.getString("type"));
			System.out.print(", ");
			System.out.print(results.getString("owner"));
			System.out.print(", ");
			System.out.print(results.getInt("elev"));
			System.out.print(", ");
			System.out.print(results.getInt("tvdep"));
			System.out.print(", ");
			System.out.print(results.getInt("thdev"));
			System.out.print(", ");
			System.out.print(results.getInt("year"));
			System.out.print(", ");
			System.out.print(results.getString("products"));
			System.out.print(", ");
			System.out.print(results.getString("layers"));
			System.out.print(", ");
			System.out.print(results.getString("layerTop"));
			System.out.print(", ");
			System.out.print(results.getString("layerBot"));
			System.out.println();
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/****************Test main********************************************************\
	\*********************************************************************************/
	public static void main(String[] args) {
		//testMe();
		SQLManager mManager = SQLManager.init();

		Well well = new Well("4703305502", "Active Well", "39.265358", "-80.459071");
		well.setType("horizontal");
		well.setOwner("Antero Resources Appalachian Corp.");
		well.setElev(Util.feetToMeters(1166));
		well.setTVDep(Util.feetToMeters(7025));
		well.setTHDev(Util.feetToMeters(13598-7025));
		well.setYear(2011);
		well.setProducts("Gas");
		well.addLayer("Water", Well.DEPTH_UNKNOWN, 1484, false);
		well.addLayer("Marcellus Sh", 6998, 7025, false);
		well.addLayer("Water", Well.DEPTH_UNKNOWN, 2384, false);

		mManager.addToDatabase(well);

		Iterable<Well> wells = mManager.getWells();
		for (Well w : wells) {
			System.out.println(w.getSQLValue());
		}
		// String query = "SELECT * FROM wvwells";
		// try {
		// 	ResultSet results = mManager.query(query);
		// 	// Get data from ResultSet
		// 	while (results.next()) {
		// 		printWellValue(results);
		// 	}
		// 	results.close();

		// } catch (SQLException se) {
		// 	System.out.println(se);
		// 	se.printStackTrace();
		// } catch (Exception e) {
		// 	System.out.println(e);
		// 	e.printStackTrace();
		// }
	}
}