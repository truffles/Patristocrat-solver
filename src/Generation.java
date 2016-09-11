import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;


public class Generation {
    private List<Chromosome> entities = new ArrayList<Chromosome>(POPULATION_SIZE + CROSSOVER_SIZE);
    private String cipher;
    private int generation = 0;
    
    public static final int POPULATION_SIZE = 100;
    public static final int CROSSOVER_SIZE = 200;
    public static final int MAX_MUTATIONS = 10;
    private Random rand = new Random();
    
    public Generation(String cipher) {
        this.cipher = cipher;
        
        for (int i = 0; i < POPULATION_SIZE; i++) {
            entities.add(new Chromosome().shuffle(rand));
        }
        for (Chromosome entity : entities) {
            entity.updateScore(cipher);
        }
        Collections.sort(entities);
    }
    
    public void evolve() {
        
        do {
            do {
                int rand1;
                int rand2;
                
                do {
                    rand1 = rand.nextInt(rand.nextInt(POPULATION_SIZE) + 1);
                    rand2 = rand.nextInt(rand.nextInt(POPULATION_SIZE) + 1);
                } while (rand1 == rand2);
                
                Chromosome chrom1 = entities.get(rand1);
                Chromosome chrom2 = entities.get(rand2);
                Chromosome child = chrom1.mateWith(chrom2, rand);
                entities.add(child);
                
                rand1 = rand.nextInt(MAX_MUTATIONS);
                for (int j = 0; j <= rand1; j++) {
                    child.mutate(rand);
                }
                
                child.updateScore(cipher);
            } while (entities.size() < POPULATION_SIZE + CROSSOVER_SIZE);
            
            Collections.sort(entities);
            
            double best = Double.NEGATIVE_INFINITY;
            ListIterator<Chromosome> iter = entities.listIterator(entities.size());
            while (iter.hasPrevious()) {
                Chromosome entity = iter.previous();
                if (best < entity.getScore()) {
                    best = entity.getScore();
                } else {
                    iter.remove();
                }
            }
        } while (entities.size() < POPULATION_SIZE);
        
        entities.subList(POPULATION_SIZE, entities.size()).clear();
        generation++;
    }
    
    public Chromosome getBestEntity() {
        return entities.get(0);
    }
    
    public int getGeneration() {
        return generation;
    }
}
