import java.util.Random;


public class Chromosome implements Comparable<Chromosome> {
    private static final char[] defaultChromosome;
    private char[] toCipher = new char[26];
    private char[] toPlain = new char[26];
    private double score;
    
    static {
        defaultChromosome = new char[26];
        for (int i = 0; i < 26; i++) {
            defaultChromosome[i] = (char) (i + 'A');
        }
    }
    
    public Chromosome() {
        this(true);
    }
    
    private Chromosome(boolean init) {
        if (init) {
            init();
        }
    }
    
    public Chromosome init() {
        System.arraycopy(defaultChromosome, 0, toCipher, 0, 26);
        System.arraycopy(defaultChromosome, 0, toPlain, 0, 26);
        return this;
    }
    
    public Chromosome shuffle(Random rand) {
        for (int i = 0; i < 26; i++) {
            swapCipher(i, rand.nextInt(26));
        }
        
        return this;
    }
    
    public Chromosome mutate(Random rand) {
        swapCipher(rand.nextInt(26), rand.nextInt(26));
        return this;
    }
    
    public Chromosome mateWith(Chromosome another, Random rand) {
        Chromosome newChrom = new Chromosome(false);
        boolean[] hasBeenMapped = new boolean[26];
        
        for (int i = 0; i < 26; i++) {
            char plain = Dictionary.LETTER_FREQ_RANK[i];
            int plainIdx = Dictionary.LETTER_FREQ_RANK_INT[i];
            char cipher1 = this.toCipher[plainIdx];
            char cipher2 = another.toCipher[plainIdx];
            int rank1 = Dictionary.LETTER_RANK_INT[cipher1 - 'A'];
            int rank2 = Dictionary.LETTER_RANK_INT[cipher2 - 'A'];
            char cipher;
            
            if (rank1 <= rank2) {
                cipher = cipher1;
                if (hasBeenMapped[cipher - 'A']) {
                    cipher = cipher2;
                }
            } else {
                cipher = cipher2;
            }
            
            while (hasBeenMapped[cipher - 'A']) {
                cipher = (char) (rand.nextInt(26) + 'A');
            }
            
            newChrom.toCipher[plainIdx] = cipher;
            newChrom.toPlain[cipher - 'A'] = plain;
            hasBeenMapped[cipher - 'A'] = true;
        }
        
        return newChrom;
    }
    
    public Chromosome swapCipher(int i1, int i2) {
        char plain1 = toPlain[i1];
        char plain2 = toPlain[i2];
        toPlain[i1] = plain2;
        toPlain[i2] = plain1;
        toCipher[plain1 - 'A'] = (char) (i2 + 'A');
        toCipher[plain2 - 'A'] = (char) (i1 + 'A');
        
        return this;
    }
    
    public Chromosome swapCipher(char ch1, char ch2) {
        int i1 = ch1 - 'A';
        int i2 = ch2 - 'A';
        char plain1 = toPlain[i1];
        char plain2 = toPlain[i2];
        toPlain[i1] = plain2;
        toPlain[i2] = plain1;
        toCipher[plain1 - 'A'] = ch2;
        toCipher[plain2 - 'A'] = ch1;
        
        return this;
    }
    
    public char mapToCipher(int i) {
        return toCipher[i];
    }
    
    public char mapToCipher(char ch) {
        return toCipher[ch - 'A'];
    }
    
    public char mapToPlain(int i) {
        return toPlain[i];
    }
    
    public char mapToPlain(char ch) {
        return toPlain[ch - 'A'];
    }
    
    public char[] encode(String plain) {
        char[] chArr = plain.toCharArray();
        
        for (int i = 0; i < chArr.length; i++) {
            chArr[i] = mapToCipher(chArr[i]);
        }
        
        return chArr;
    }
    
    public char[] decode(String cipher) {
        char[] chArr = cipher.toCharArray();
        
        for (int i = 0; i < chArr.length; i++) {
            chArr[i] = mapToPlain(chArr[i]);
        }
        
        return chArr;
    }
    
    public double updateScore(String cipher) {
        score = Dictionary.getInstance().getScore(this.decode(cipher));
        return score;
    }
    
    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(Chromosome o) {
        if (this.score < o.score) return 1;
        if (this.score == o.score) return 0;
        return -1;
    }
}
