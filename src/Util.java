import java.net.*;
import java.util.*;
import java.io.*;
import java.util.regex.*;

public class Util {

	/****************Constants********************************************************\
	\*********************************************************************************/
	// state constants for reading the googleapis return
	private final static String APIKEY = "AIzaSyDCcaRCI9V3av8Cn3zxcpId_H7_tNUYg9c"; // google API key
	private final static double PAD = 0.05;

	/****************Elevation functions**********************************************\
	\*********************************************************************************/
	// get the elevation IN METERS of the given coordinate
	public static double getElev(String lat, String lng) {
		// generate proper request URL
		String url = "https://maps.googleapis.com/maps/api/elevation/json";
		String location = lat + "," + lng;
		String query = String.format("locations=%s&key=%s", location, APIKEY);
		String total = url + "?" + query;

		String charset = "UTF-8";
		try {
			URLConnection connection = new URL(total).openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			InputStream response = connection.getInputStream();
			LinkedList<Double> elevs = parseElevs(response);
			return elevs.getFirst();
		} catch(IOException e) {
			System.err.println(e);
			return -1;
		}
	}

	// returns a LinkedList of @param samples elevation samples between coord1 and coord2,
	// inclusive on both ends.  Samples are evenly spaced along that path.
	public static LinkedList<Double> getElev(String lat1, String lng1, String lat2, String lng2, int samples) {
		
		// slope calc (Probs like the worst thing ever on a sphere)
		Double doublat1 = Double.parseDouble(lat1);
		Double doublng1 = Double.parseDouble(lng1);
		Double doublat2 = Double.parseDouble(lat2);
		Double doublng2 = Double.parseDouble(lng2);

		Double slope = (doublat2 - doublat1) / (doublng2 - doublng1);

		// midpoint, for two sections
		String midlat = String.valueOf((doublat1 + doublat2) / 2.0);
		//((Double) ((doublat1 + doublat2) / 2.0)).toString();
		String midlng = String.valueOf((doublng1 + doublng2) / 2.0);
		//= ((Double) ((doublng1 + doublng2) / 2.0)).toString();

		// build requests**********************************************8
		
		LinkedList<Double> fullElevPath = new LinkedList<Double>();
		
		// generate appropriate urls
		String firstReq = makeFullPathURL(lat1, lng1, midlat, midlng, samples);
		String secondReq = makeFullPathURL(midlat, midlng, lat2, lng2, samples);

		// add those to the full elev path
		fullElevPath.addAll(makePathRequest(firstReq));
		fullElevPath.addAll(makePathRequest(secondReq));

		return fullElevPath;
		//return padElevList(fullElevPath);
	}

	// properly format the URL for a googleapis request
	private static String makeFullPathURL(String lat1, String lng1, String lat2, String lng2, int samples) {
		final String url = "https://maps.googleapis.com/maps/api/elevation/json";
		String locations = lat1 + "," + lng1 + "|" + lat2 + "," + lng2;
		String query = String.format("path=%s&samples=%s&key=%s", locations, samples, APIKEY);
		return url + '?' + query;
	}

	// perform the googleapis request given a url (should format with makeFullPathURL())
	private static LinkedList<Double> makePathRequest(String url) {
		String charset = "UTF-8";
		try {
			URLConnection connection = new URL(url).openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			InputStream response = connection.getInputStream();
			return parseElevs(response);
		} catch (IOException e) {
			System.err.println("makePathRequest " + e.toString());
		}
		return null;
	}

	// pull only the elevations from the googleapis response
	private static LinkedList<Double> parseElevs(InputStream response) throws IOException {
		Scanner s = new Scanner(response);
		LinkedList<Double> toReturn = new LinkedList<>(); // results
		//  run through inputstream
		while (s.hasNextLine()) {
			String str = s.nextLine();
			// grab elevation
			if (Pattern.matches(".*elevation.*", str)) {
				
				// google repsonse specific cleaning here
				StringBuilder clean = new StringBuilder();
				for (int i = 0; i < str.length(); i++) {
					String ch = "" + str.charAt(i);
					if (Pattern.matches("\\.|\\d", ch)) clean.append(ch);
				}
				// if (Math.random() < 0.01)
				// 	System.out.println(str);
				toReturn.add(Double.parseDouble(clean.toString()));
			}
		}
		return toReturn;
	}

	// pseudopad the edges of the linkedList
	private static LinkedList<Double> padElevList(LinkedList<Double> elevs) {
		int padSize = (int) (elevs.size() * PAD);
		System.out.println("Padsize " + padSize);
		double first = elevs.getFirst();
		double last = elevs.getLast();
		for (int i = 0; i < padSize; i++) {
			elevs.addFirst(first);
			elevs.addLast(last);
		}
		return elevs;
	}
	
	//*****************************flags and sourcetypes for reading***************************
		// filters: add to filters string to INCLUDE ONLY WELLS WITH THE ATTRIBUTE in the query,
		//			any filter including FIL_ALL takes all possible wells.

		public static final char FIL_ALL		= ' '; // all wells in source

		// status filters
		public static final char FIL_ACTIVE 	= 'a'; // include active wells
		public static final char FIL_ABAND		= 'b'; // include abandoned wells
		public static final char FIL_PLUG		= 'p'; // include plugged wells
		public static final char FIL_NEVER		= 'n'; // include "never" wells (never drilled, never issued, etc.)
		
		// type filters
		public static final char 	FIL_GAS		= 'g'; // include gas wells
		public static final char 	FIL_OIL		= 'o'; // include oil wells
		public static final char 	FIL_HW		= 'h'; // include horizontal well (NOT HORIZ 6A wells)
		public static final char 	FIL_H6A		= '6'; // include HGA wells
		public static final String 	FIL_ALLHW	= FIL_HW + "" + FIL_H6A; // include horizontal and horizontal 6A wells

		// characteristic filters
		public static final char FIL_DEPTH		= 'd'; // include wells with a depth trait

	//*****************************************************************************************

	public static int feetToMeters(int feet) {
		return (int) (0.3048 * feet);
	}

	// turn given api into the form 47xxxxxxxx
	// @Param api must be either already in the form 47xxxxxxxx
	// or in the form xxx-xxxxx, the TAGIS format
	public static String cleanAPI(String api) {
		//System.out.println(api);
		if (Pattern.matches("47\\d{8}c", api)) return api.substring(0, 10);
		if (Pattern.matches("47\\d{8}c\\d{1}", api)) return api.substring(0, 10);
		if (Pattern.matches("47\\d{8}", api)) return api;
		if (Pattern.matches("\\d{3}-\\d{5}", api)) {
			return "47" + api.substring(0, 3) + api.substring(4, api.length());
		}
		throw new IllegalArgumentException("cleanAPI, invalid api format");
	}
}