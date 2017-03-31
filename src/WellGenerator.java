import java.util.*;
public class WellGenerator {
     public static void main(String[] args) {
          final char GAS = 'g';
          final char OIL = 'o';
          final char OTHER = 'x';
          final char UNPLUG = 'u';
          final char PLUG = 'p';

          Integer count = Integer.parseInt(args[0]);
          int[] counts = new int[6];
          LinkedList<String> strList = new LinkedList<>();
          for (int i = 0; i < count; i++) {
               String id = "";
               Well well = new Well();
               well.setAPI(""+i);
               double decide = Math.random();
               if (decide < 0.33) {
                    well.setProducts("Gas");
                    id += GAS;
               }
               else if (decide < 0.66) {
                    well.setProducts("Oil");
                    id  += OIL;
               }
               else {
                    well.setProducts("Dry");
                    id += OTHER;
               }

               decide = Math.random();
               if (decide < 0.55) {
                    well.setStatus("Plugged");
                    id += PLUG;
               }
               else {
                    well.setStatus("Abandoned");
                    id += UNPLUG;
               }
               System.out.println(well.getSQLValue());
               strList.add(id);
          }
          for (String str : strList) {
               switch(str) {
                    case "gu": counts[0]++; break;
                    case "gp": counts[1]++; break;
                    case "ou": counts[2]++; break;
                    case "op": counts[3]++; break;
                    case "xu": counts[4]++; break;
                    case "xp": counts[5]++; break;
               }
          }
          System.err.println("Gas Unplugged,Gas Plugged,Oil Unplugged,Oil Plugged,Other Unplugged,Other Plugged,Total count");
          for (int i = 0; i < 6; i++) {
               System.err.print(counts[i] + ",");
          }
          System.err.println("=SUM(A2:F2)");
          System.err.println("7.5e4, 5.4e2,3.1e2, 3.6e2,3.1e4, 4.5e2");
          System.err.println("=A2*A3,=B2*B3,=C2*C3,=D2*D3,=E2*E3,=F2*F3,=SUM(A4:F4)");
     }
}