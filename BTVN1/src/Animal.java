public class Animal {
    private String name;
    private int age;
    private float weight;
    private String cageCode;
    public Animal(String name, int age, float weight, String cageCode) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.cageCode = cageCode;
    }

    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }

    public float getWeight() {
        return weight;
    }

    public String getCageCode() {
        return cageCode;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }
    public void setCageCode(String cageCode) {
        this.cageCode = cageCode;
    }
}
