import java.util.*;
import java.io.*;

public class StatusStripper {
     public static void main(String[] args) {
          Iterable<Well> wells = SQLManager.init().getWells();
          HashMap<String, Integer> unique = new HashMap<>();
          for (Well w : wells) {
               String status = w.getStatus();
               if (!unique.containsKey(status)) {
                    unique.put(status, 0);
               } else {
                    unique.put(status, (unique.get(status) + 1));
               }
          }
          int count = 0;
          for (String key : unique.keySet()) {
               System.out.println(key + " : " + unique.get(key));
               count += unique.get(key);
          }
          System.out.println("Count: " + count);
     }
}