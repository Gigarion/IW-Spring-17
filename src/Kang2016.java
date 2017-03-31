import java.io.*;
import java.util.Scanner;
import java.util.HashMap;
import java.util.LinkedList;

public class Kang2016 {
     private final static int UNPLUGGED = 0;
     private final static int PLUGGED = 1;
     private final static int REGION_NONE = 0;
     private final static int REGION_COAL = 1;
     private final static int REGION_NONCOAL = 2;
     private static double[][] ALL_WELL_FACTORS = {
          {2.2e4, 11.4e4},
          {1.2e3, 4.3e4},
          {3.1e4, 4.5e2}
     };

     private static double[][] OIL_AND_COMBINED_FACTORS = {
          {1.9e2, 3.3e2},
          {1.1, 1.2e-2},
          {3.1e2, 3.6e2}
     };

     private static double[][] GAS_FACTORS = {
          {6e4, 2.4e4},
          {5.2e3, 4.7e4},
          {7.5e4, 5.4e2}
     };
     private static String MODULE_PATH = "../Kang/";
     private static String PRODUCT_MATCH_PATH = "KangProductMatch.txt";

     private static HashMap<String, String> getProductMatchings() {
          // set up type matching from appropriate file
          File f = null;
          Scanner s = null;
          HashMap<String, String> productMatcher = new HashMap<>();
          try {
               f = new File(MODULE_PATH + PRODUCT_MATCH_PATH);
               s = new Scanner(f);
          } catch (Exception e) {
               e.printStackTrace();
               return null;
          }
          if (f == null || s == null) {
               System.out.println("Error, null file or Scanner");
               return null;
          }

          // get through comment block
          if (s.nextLine() .contains("/*")) {
               while (!s.nextLine().contains("*/")) {
                    continue;
               }
          }

          // fill ProductMatcher
          while (s.hasNextLine()) {
               String[] chunks = s.nextLine().split(",");
               productMatcher.put(chunks[0], chunks[1]);
          }
          return productMatcher;
     }

     private static double getGasEmissions(Iterable<Well> gasWells) {
          double emissionsTotal = 0;
          for (Well well : gasWells) {
               if (well.getStatus().contains("Plug")) {
                    emissionsTotal += GAS_FACTORS[REGION_NONCOAL][PLUGGED];
               }
               else {
                    emissionsTotal += GAS_FACTORS[REGION_NONCOAL][UNPLUGGED];
               }
          }
          return emissionsTotal;
     }

     private static boolean wellAbandoned(Well well) {
          String status = well.getStatus().toUpperCase();
          return status.contains("ABAND") || status.contains("PLUG") || status.contains("SHUTIN") || status.equals("0") || status.equals("");
     }

     private static double getOtherEmissions(Iterable<Well> otherWells) {
          double emissionsTotal = 0;
          for (Well well : otherWells) {
               if (well.getStatus().contains("Plug")) {
                    emissionsTotal += ALL_WELL_FACTORS[REGION_NONCOAL][PLUGGED];
               }
               else {
                    emissionsTotal += ALL_WELL_FACTORS[REGION_NONCOAL][UNPLUGGED];
               }
          }
          return emissionsTotal;
     }

     private static double getOilAndCombinedEmissions(Iterable<Well> oilAndCombinedWells) {
          double emissionsTotal = 0;
          for (Well well : oilAndCombinedWells) {
               if (well.getStatus().contains("Plug")) {
                    emissionsTotal += OIL_AND_COMBINED_FACTORS[REGION_NONCOAL][PLUGGED];
               }
               else {
                    emissionsTotal += OIL_AND_COMBINED_FACTORS[REGION_NONCOAL][UNPLUGGED];
               }
          }
          return emissionsTotal;
     }

     // the kang analysis uses the distribution of the other types of wells 
     private static double getUnknownEmissions(Iterable<Well> otherWells, double gasCut, double oilCut) {
          double total;
          LinkedList<Well> newGas = new LinkedList<>();
          LinkedList<Well> newOil = new LinkedList<>();
          LinkedList<Well> newOther = new LinkedList<>();
          for (Well well : otherWells) {
               double typeSwitch = Math.random();
               if (typeSwitch < gasCut) {
                    newGas.add(well);
                    // this unknown is a gas well
               }
               else if (typeSwitch < oilCut) {
                    newOil.add(well);
                    // this is an oil well
               }
               else {
                    newOther.add(well);
                    // this is an other well
               }
          }
          return getGasEmissions(newGas) + getOilAndCombinedEmissions(newOil) + getOtherEmissions(newOther);
     }

     public static void main(String[] args) {
          SQLManager sqlm = SQLManager.init();

          // check valid table
          String table = args[0];
          if (table == null) {
               System.out.println("Invalid table");
               return;
          }

          // the three arching categories of well products
          LinkedList<Well> gasWells = new LinkedList<>();
          LinkedList<Well> oilAndCombinedWells = new LinkedList<>();
          LinkedList<Well> otherWells = new LinkedList<>();
          LinkedList<Well> unknownWells = new LinkedList<>();

          // get wells
          Iterable<Well> wells = sqlm.getWells(table);
          HashMap<String, String> productMatcher = getProductMatchings();
          if (productMatcher == null) {
               System.out.println("bad matcher");
               return;
          }

          // sort wells by product type
          int count = 0;
          for (Well well : wells) {
               if (wellAbandoned(well)) {
                    if (well.getStatus().contains("Plug"))
                         count++;
                    String product = productMatcher.get(well.getProducts());
                    if (product == null) {
                         System.out.println("Bad Product: " + well.getProducts());
                         continue;
                    }
                    switch(product) {
                         case "Oil": oilAndCombinedWells.add(well); break;
                         case "Gas": gasWells.add(well); break;
                         case "Unknown": unknownWells.add(well); break;
                         case "Other":  otherWells.add(well); break;
                         default: System.out.println("Bad match: " + product);
                    }
               }
          }
          System.out.println("plug count: " + count);

          // get emissions from each type
          // distribute the unknown wells according to the distribution of the other types of wells
          double totalCount = gasWells.size() + oilAndCombinedWells.size() + otherWells.size();
          double gasOilCut = gasWells.size() / totalCount;
          double oilOtherCut = (oilAndCombinedWells.size() / totalCount) + gasOilCut;

          double gasEmissions = getGasEmissions(gasWells);
          double oilAndCombinedEmissions = getOilAndCombinedEmissions(oilAndCombinedWells);
          double otherEmissions = getOtherEmissions(otherWells);
          double unknownEmissions = getUnknownEmissions(unknownWells, gasOilCut, oilOtherCut);

          // print results
          System.out.println("finished, check error file");
          System.out.println("Gas count: " + gasWells.size() + " : " + gasEmissions);
          System.out.println("Oil count: " + oilAndCombinedWells.size() + " : " + oilAndCombinedEmissions);
          System.out.println("Other count: " + otherWells.size() + " : " + otherEmissions);
          System.out.println("Unknown count: " + unknownWells.size() + " : " + unknownEmissions);
          totalCount = gasWells.size() + oilAndCombinedWells.size() + otherWells.size() + unknownWells.size();
          System.out.println("Total count: " + totalCount);
          System.out.println();
          double totalEmissions = gasEmissions + oilAndCombinedEmissions + otherEmissions + unknownEmissions;
          System.out.println("Total emissions: " + totalEmissions + " mg CH4/hr");
     }
}