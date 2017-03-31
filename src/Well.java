import java.util.LinkedList;
import java.util.PriorityQueue;
public class Well {
	// constants *************************************
	// public static final int STAT_ACTIVE 	= 0;
	// public static final int STAT_ABANDONED 	= 1;
	// public static final int STAT_PLUGGED	= 2;
	// public static final int STAT_NEVER		= 3;
	// public static final int STAT_PERMITTED 	= 4;
	// public static final int STAT_FUTURE 	= 5;
	// public static final int STAT_ORDERED 	= 6;
	// public static final int STAT_BUILDING 	= 7;
	// public static final int STAT_SHUTIN	= 8;
	// public static final int STAT_MINEVENT	= 9;

	public static final String STAT_UNKNOWN 	= "n/a";

	public static final String PROD_UNKNOWN	= "UNKNOWN";
	
	public static final String API_UNKNOWN 	= null;
	public static final String OWN_UNKNOWN	= null;
	public static final String TYPE_UNKNOWN	= null;

	public static final int ELEV_UNKNOWN	= -1;
	public static final int DEPTH_UNKNOWN	= -1;
	public static final int DEV_UNKNOWN		= -1;
	public static final int YEAR_UNKNOWN 	= -1;

	// Instance variables ****************************
	private String api;		// WVDEP api#
	
	private String lat;		// latitude of well
	private String lng;		// longitude of well
	
	private String status;		// WVDEP status
	private String type;	// WVDEP Classification (Horizontal or vertical)
	private String owner;	// WVDEP info
	private int elev;  		// elevation (meters)
	private int tvdep;		// total vertical depth (meters)
	private int thdev;		// total horizontal deviation (meters)
	private int year;		// year built/issued

	private String products; 	// type of production, i.e. Oil, Gas, both
	private PriorityQueue<Layer> layers;

	public Well() {	}
	
	// constructor for a well, only needs these three for now.
	public Well(String api, String status, String lat, String lng) {
		this.api = api;
		this.status = status;
		this.lat = lat;
		this.lng = lng;
	}

	// setters
	public void setAPI(String api) {this.api = api;}
	public void setLat(String lat) {this.lat = lat;}
	public void setLong(String lng) {this.lng = lng;}
	public void setStatus(String status) {this.status = status;}
	public void setType(String type) {this.type = type;}
	public void setOwner(String owner) {this.owner = owner;}
	public void setElev(int elev) {this.elev = elev;}
	public void setTVDep(int depth) {this.tvdep = depth;}
	public void setTHDev(int dev) {this.thdev = dev;}
	public void setYear(int year) {this.year = year;}
	public void setProducts(String products) {this.products = products;}

	// add formation layer BELOW all formation layers this well has recorded
	// use DEPTH_UNKNOWN if necessary.
	public void addLayer(String layer, int depthTop, int depthBot, boolean isDeviated) {
		if (layer == null) throw new NullPointerException("Null layer in addlayer");
		if (layers == null) layers = new PriorityQueue<>();
		Layer nLayer = new Layer(layer, depthTop, depthBot, isDeviated);
		layers.add(nLayer);
	}

	// getters
	public String getAPI() {return this.api;}
	public String getLat() {return this.lat;}
	public String getLng() {return this.lng;}
	public String getStatus() {return this.status;}
	public String getType() {return this.type;}
	public String getOwner() {return this.owner;}
	public int    getElev() {return this.elev;}
	public int    getTVDep() {return this.tvdep;}
	public int    getTHDev() {return this.thdev;}
	public int    getYear() {return this.year;}
	public String getProducts() {return this.products;}
	public Iterable<Layer> getLayers() {
		if (layers == null) return null;
		PriorityQueue<Layer> dataSave = new PriorityQueue<>(layers);
		LinkedList<Layer> sorted = new LinkedList<>();
		Layer l = dataSave.poll();
		while (l != null) {
			sorted.add(l);
			l = dataSave.poll();
		}

		return sorted;
	}
	public String getCSLayers() {
		if (getLayers() == null) return "n/a";
		StringBuilder layString = new StringBuilder();
		for (Layer layer : getLayers()) {
			layString.append(layer.type);
			layString.append(',');
		}
		layString.deleteCharAt(layString.length() -1);
		return layString.toString();
	}
	public String getCSLayerTops() {
		if (getLayers() == null) return "n/a";
		StringBuilder layString = new StringBuilder();
		for (Layer layer : getLayers()) {
			layString.append(layer.depthTop);
			layString.append(',');
		}
		layString.deleteCharAt(layString.length() -1);
		return layString.toString();
	}
	public String getCSLayerBots() {
		if (getLayers() == null) return "n/a";
		StringBuilder layString = new StringBuilder();
		for (Layer layer : getLayers()) {
			layString.append(layer.depthBot);
			layString.append(',');
		}
		layString.deleteCharAt(layString.length() -1);
		return layString.toString();
	}
	public String getCSLayerDevs() {
		if (getLayers() == null) return "n/a";
		StringBuilder layString = new StringBuilder();
		for (Layer layer : getLayers()) {
			layString.append(layer.isDeviated);
			layString.append(',');
		}
		layString.deleteCharAt(layString.length() - 1);
		return layString.toString();
	}
	public String toString() {
		return "API: " + api + " Lat: " + lat + " Long: " + lng +" Status: " + status;
	}

	// return a SQL formatted value for this well 
	public String getSQLValue() {
		StringBuilder wellValue = new StringBuilder("(");
		wellValue.append(strSQLForm(api));
		wellValue.delete(1, 3);
		wellValue.append(strSQLForm(lat));
		wellValue.append(strSQLForm(lng));
		
		wellValue.append(strSQLForm(status));
		wellValue.append(strSQLForm(type));
		wellValue.append(strSQLForm(owner));
		wellValue.append(intSQLForm(elev));
		wellValue.append(intSQLForm(tvdep));
		wellValue.append(intSQLForm(thdev));
		wellValue.append(intSQLForm(year));
		
		wellValue.append(strSQLForm(products));
		wellValue.append(strSQLForm(getCSLayers()));
		wellValue.append(strSQLForm(getCSLayerTops()));
		wellValue.append(strSQLForm(getCSLayerBots()));
		wellValue.append(strSQLForm(getCSLayerDevs()));
		
		wellValue.append(")");

		return wellValue.toString();
	}

	/*public static Well wellFromSQLValue(String sqlValue) {
		if (sqlValue == null) throw new NullPointerException("null arg, wellfromsqlvalue");
		String[] values = sqlValue.split(",");
	}*/

	//********getSQL Helpers******************************
	private String strSQLForm(String val) {
		if (val == null) return ", NULL";
		return ", '" + val + "'";
	}

	private String intSQLForm(int val) {
		if (val < 0) return ", NULL";
		return ", " + val;
	}

	//************Test Main******************************
	public static void main(String[] args) {
		Well well = new Well("4703305502", "Active Well", "39.265358", "-80.459071");
		well.setType("horizontal");
		well.setOwner("Antero Resources Appalachian Corp.");
		well.setElev(Util.feetToMeters(1166));
		well.setTVDep(Util.feetToMeters(7025));
		well.setTHDev(Util.feetToMeters(13598-7025));
		well.setYear(2011);
		well.setProducts("Gas");
		well.addLayer("Water", DEPTH_UNKNOWN, 1484, false);
		well.addLayer("Marcellus Sh", 6998, 7025, false);
		well.addLayer("Water", DEPTH_UNKNOWN, 2384, false);

		System.out.println(well.getSQLValue());

		Iterable<Layer> pq = well.getLayers();
		for (Layer l : pq) 
			System.out.println(l);
	}
}