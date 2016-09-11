import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.InflaterInputStream;


public class Dictionary {
    
    public static final char[] LETTER_FREQ_RANK = {'E','T','A','O','I','N','S','R','H','D','L','U','C',
                                                   'M','F','Y','W','G','P','B','V','K','X','Q','J','Z'};
    public static final int[] LETTER_FREQ_RANK_INT;
    public static final int[] LETTER_RANK_INT;
    private final double unknownScore;
    private static Dictionary instance;
    private final double[] quadgrams;
    private final Map<String, Double> vocabularies;
    
    
    static {
        LETTER_FREQ_RANK_INT = new int[26];
        LETTER_RANK_INT = new int[26];
        
        for (int i = 0; i < 26; i++) {
            LETTER_FREQ_RANK_INT[i] = LETTER_FREQ_RANK[i] - 'A';
            LETTER_RANK_INT[LETTER_FREQ_RANK_INT[i]] = i;
        }
        
        try {
            instance = new Dictionary();
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    }
    /*
    public static void main(String[] args) {
        File inFile = null;
        int type = 0;
        Scanner scan = new Scanner(System.in);
        do {
            System.out.print("Select Type: ");
            type = Integer.valueOf(scan.nextLine());
            System.out.print("To be encoded: ");
            inFile = new File(scan.nextLine());
        } while (inFile == null || !inFile.canRead());
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            DataOutputStream writer = new DataOutputStream(
                    new DeflaterOutputStream(new FileOutputStream(new File(inFile.getName() + ".dic")), new Deflater(9), 512));
            
            switch (type) {
            case 1: {
                int [] quadgrams = new int[26*26*26*26];
                String entry;
                while ( (entry = reader.readLine()) != null ) {
                    String[] pair = entry.split(" ");
                    int index = (pair[0].charAt(0) - 'A') * 26 * 26 * 26
                              + (pair[0].charAt(1) - 'A') * 26 * 26
                              + (pair[0].charAt(2) - 'A') * 26
                              + (pair[0].charAt(3) - 'A');
                    
                    quadgrams[index] = Integer.valueOf(pair[1]);
                }
                
                for (int i = 0; i < 26 * 26 * 26 * 26; i++) {
                    writer.writeInt(quadgrams[i]);
                }
                break; }
            case 2: {
                String entry;
                while ( (entry = reader.readLine()) != null ) {
                    String[] parts = entry.split("\t");
                    
                    writer.writeUTF(parts[0]);
                    writer.writeLong(Long.valueOf(parts[1]));
                }
                break; }
            }
            
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        scan.close();
    }
    */
    private Dictionary() throws NumberFormatException, IOException {
        InputStream inStream = this.getClass().getClassLoader().getResourceAsStream("quadgram_a.dic");
        DataInputStream reader = new DataInputStream(
                new BufferedInputStream(new InflaterInputStream(inStream)));
        
        quadgrams = new double[26*26*26*26];
        long sumOfFreq = 0;
        
        try {
            for (int i = 0; i < 26 * 26 * 26 * 26; i++) {
                int freq = reader.readInt();
                quadgrams[i] = freq;
                sumOfFreq += freq;
            }
        } catch (EOFException e) {
            e.printStackTrace();
        }
        
        reader.close();
        
        for (int i = 0; i < 26 * 26 * 26 * 26; i++) {
            if (quadgrams[i] != 0.0) {
                quadgrams[i] = Math.log10(quadgrams[i] / sumOfFreq);
            } else {
                quadgrams[i] = Math.log10(0.01 / sumOfFreq);
            }
        }
        
        inStream = this.getClass().getClassLoader().getResourceAsStream("count_1w.dic");
        reader = new DataInputStream(
                new BufferedInputStream(new InflaterInputStream(inStream)));
        
        vocabularies = new HashMap<String, Double>(100000);
        sumOfFreq = 0L;
        
        try {
            while (true) {
                String key = reader.readUTF();
                long freq = reader.readLong();
                sumOfFreq += freq;
                vocabularies.put(key, (double) freq);
            }
        } catch (EOFException e) {
        }
        
        reader.close();
        
        Iterator<Entry<String, Double>> iter = vocabularies.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Double> ent = iter.next();
            ent.setValue(Math.log10(ent.getValue() / sumOfFreq));
        }
        
        unknownScore = Math.log10(1.0 / sumOfFreq);
    }
    
    public static Dictionary getInstance() {
        return instance;
    }
    
    public double getScore(char[] deciphered) {
        if (deciphered.length < 4) return 0.0;
        double score = 0.0;
        
        int ca = deciphered[0] - 'A';
        int cb = deciphered[1] - 'A';
        int cc = deciphered[2] - 'A';
        int cd;
        int index = ca * 26 * 26
                  + cb * 26
                  + cc;
        
        for (int i = 3; i < deciphered.length; i++) {
            cd = deciphered[i] - 'A';
            index *= 26;
            index += cd;
            score += quadgrams[index];
            index -= ca * (26 * 26 * 26);
            
            ca = cb;
            cb = cc;
            cc = cd;
        }
        
        return score;
    }
    
    private double getSegmentScore(String text) {
        Double score = vocabularies.get(text);
        if (score == null) {
            return unknownScore - text.length() + 2;
        }
        return score;
    }
    
    public int[] segment(String text) {
        int length = text.length();
        text = text.toLowerCase();
        int[] seg = new int[length];
        double[] score = new double[length+1];
        score[length] = 0.0;
        for (int i = length - 1; i >= 0; i--) {
            score[i] = Double.NEGATIVE_INFINITY;
            for (int j = i+1; j <= length && j <= i + 40; j++) {
                double prob = getSegmentScore(text.substring(i, j)) + score[j];
                if ( prob > score[i] ) {
                    score[i] = prob;
                    seg[i] = j;
                }
            }
        }
        return seg;
    }
}
