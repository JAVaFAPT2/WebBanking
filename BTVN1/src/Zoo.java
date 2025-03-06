import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Zoo {
    private List<Cage> cages;

    public Zoo() {
        cages = new ArrayList<>();
        // Initialize 10 cages
        for (int i = 1; i <= 10; i++) {
            cages.add(new Cage("CAGE" + i));
        }
    }

    public Animal findAnimalByName(String name) {
        for (Cage cage : cages) {
            for (Animal animal : cage.getAnimals()) {
                if (animal.getName().equalsIgnoreCase(name)) {
                    System.out.println("Found animal: " + animal.getName());
                    System.out.println("Age: " + animal.getAge());
                    System.out.println("Weight: " + animal.getWeight());
                    System.out.println("Cage: " + cage.getCageID());
                    return animal;
                }
            }
        }
        return null;
    }

    public List<Animal> getAllAnimalsSortedByAge() {
        List<Animal> allAnimals = new ArrayList<>();
        for (Cage cage : cages) {
            allAnimals.addAll(cage.getAnimals());
        }
        allAnimals.sort((a1, a2) -> Integer.compare(a1.getAge(), a2.getAge()));
        return allAnimals;
    }
}
