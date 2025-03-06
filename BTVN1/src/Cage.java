import java.util.List;

public class Cage {
    private String cageID;
    private List<Animal> animals;
    private final int maxAnimals = 5;

    public Cage(String cageID) {
        this.cageID = cageID;
        this.animals = animals;
    }
    public boolean addAnimal(Animal animal) {
        if (animals.size() >= maxAnimals) {
            return false;
        }
        for (Animal existingAnimal : animals) {
            if (!areCompatible(existingAnimal, animal)) {
                return false;
            }
        }

        animals.add(animal);
        return true;
    }
    public String getCageID() { return cageID; }
    public List<Animal> getAnimals() { return animals; }
    private boolean areCompatible(Animal a1, Animal a2) {
        // Tiger cannot be with Dog or Lion
        if (a1.getCageCode().equals("Tiger") && (a2.getCageCode().equals("Dog") || a2.getCageCode().equals("Lion"))) {
            return false;
        }
        if (a2.getCageCode().equals("Tiger") && (a1.getCageCode().equals("Dog") || a1.getCageCode().equals("Lion"))) {
            return false;
        }
        // Dog cannot be with Cat
        if (a1.getCageCode().equals("Dog") && a2.getCageCode().equals("Cat")) {
            return false;
        }
        if (a2.getCageCode().equals("Dog") && a1.getCageCode().equals("Cat")) {
            return false;
        }
        return true;
    }

}
