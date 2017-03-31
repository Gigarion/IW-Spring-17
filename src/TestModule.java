import java.util.LinkedList;
public class TestModule {
     public static void main(String[] args) {
          SQLManager sqlm = SQLManager.init();
          Iterable<Well> wells = sqlm.getWells();
          LinkedList<String> statusList = new LinkedList<>();
          int abandonedCount = 0;
          for (Well well : wells) {
               if (well.getStatus().contains("Plug"))
                    abandonedCount++;
               String status = well.getStatus();
               if (!statusList.contains(status)) {
                    statusList.add(status);
                    System.err.println(status);
               }
          }
          System.out.println("Plug count " + abandonedCount);
     }
}