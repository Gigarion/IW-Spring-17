import java.io.*;
import java.util.*;
public class TestAdder {
     public static void main(String[] args) throws IOException {
          SQLManager sqlm = SQLManager.init();
          sqlm.deleteTable("test_table");
          Scanner s = new Scanner(new File(args[0]));
          while (s.hasNextLine()) {
               sqlm.addToDatabase(s.nextLine(), "test_table");
          }
     }
}