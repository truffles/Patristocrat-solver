import java.io.IOException;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) throws NumberFormatException, IOException {
        Scanner scan = new Scanner(System.in);
        
        System.out.print("Cipher: ");
        String str = scan.nextLine();
        str = str.toUpperCase().replaceAll("[^A-Za-z]", "");
        
        if (str.length() < 4) {
            System.out.println("Cannot be solved");
            scan.close();
            return;
        }
        
        Generation bestGen = null;
        
        int nLoop = 100 * 100/(str.length() + 100);
        nLoop = Math.max(nLoop, 1);
        System.out.println("Ctrl-C to stop.\nWarming up " + nLoop + " group(s) for 256 evolutions...");
        
        for (int i = 0; i < nLoop; i++) {
            Generation gen = new Generation(str);
            for (int j = 0; j < 256; j++) {
                gen.evolve();
            }
            
            if (bestGen == null || bestGen.getBestEntity().compareTo(gen.getBestEntity()) > 0) {
                bestGen = gen;
            }
        }
        
        long showTime = 0L;
        
        while (true) {
            if ((bestGen.getGeneration() & 0x3F) == 0 && showTime + 1000L < System.currentTimeMillis()) {
                System.out.print("\nGeneration " + bestGen.getGeneration() + "\nPlainText:");
                String decoded = new String(bestGen.getBestEntity().decode(str));
                int[] seg = Dictionary.getInstance().segment(decoded);
                for (int i = 0; i < seg.length;) {
                    int end = seg[i];
                    System.out.print(" " + decoded.substring(i, end));
                    i = end;
                }
                System.out.println("\nScore: " + bestGen.getBestEntity().getScore() / str.length());
                showTime = System.currentTimeMillis();
            }
            bestGen.evolve();
        }
    }

}
