import java.util.*;
import java.io.*;

public class TypeStripper {
     public static void main(String[] args) {
          File f = null;
          Scanner s = null;
          LinkedList<String> unique = new LinkedList<>();
          try {
               f = new File(args[0]);
               s = new Scanner(f);
          } catch(Exception e) {
               e.printStackTrace();
               return;
          }
          while (s.hasNextLine()) {
               String prod = s.nextLine();
               if (!unique.contains(prod)) {
                    unique.add(prod);
                    System.out.println(prod);
               }
          }
     }
}