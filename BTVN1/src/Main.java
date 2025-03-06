import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private final float InputMismatchException;

    public Main(float inputMismatchException) {
        InputMismatchException = inputMismatchException;
    }

    public static void main(String[] args) {
        Main main = new Main(0);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter an integer1: ");
        int a = scanner.nextInt();
        System.out.println("Enter an integer2: ");
        int b = scanner.nextInt();
        System.out.println("Enter an integer3: ");
        int c = scanner.nextInt();
        main.calculate(a,b,c);
        Zoo zoo = new Zoo();
        Animal tiger1 = new Animal("Tiger1", 5, 200, "Tiger");
        Animal tiger2 = new Animal("Tiger2", 3, 180, "Tiger");
        Animal dog1 = new Animal("Dog1", 2, 30, "Dog");
        Animal dog2 = new Animal("Dog2", 4, 35, "Dog");
        Animal cat1 = new Animal("Cat1", 1, 5, "Cat");
        Animal cat2 = new Animal("Cat2", 2, 4, "Cat");
        Animal elephant1 = new Animal("Elephant1", 10, 4000, "Elephant");
        Animal elephant2 = new Animal("Elephant2", 8, 3500, "Elephant");
        Animal lion1 = new Animal("Lion1", 6, 190, "Lion");
        Animal lion2 = new Animal("Lion2", 7, 200, "Lion");

        Animal foundAnimal = zoo.findAnimalByName("Tiger1");


        List<Animal> sortedAnimals = zoo.getAllAnimalsSortedByAge();
    }

    public float calculate(int a, int b, int c) {
        float delta = b * b - 4 * a * c;
        if (a == 0) {
            throw new InputMismatchException("'a' cannot be zero");
        } else if (delta < 0) {
            System.out.println("Phuong trinh vo nghiem");
            return Float.NaN;
        } else if (delta == 0) {
            return -b / (2f * a);
        } else {
            return (float) ((-b + Math.sqrt(delta)) / (2 * a));
        }
    }

//
//    Một sở thú có 10 chuồng nhốt các con vật: Hổ, Chó, Mèo, Voi, Sư tử.
//    Mỗi con vật có các thuộc tính: Tên, tuổi, cân nặng
//
//    Nhốt 10 con vật, mỗi loại 2 con vào các chuồng biết:
//            + Mỗi chuồng có mã chuồng, chứa tối đa 2 con vật
// + Hổ không nhốt chung với Chó và Sư tử
// + Chó không nhốt chung với mèo
//    Viết hàm để tìm ra con vật theo tên, in ra tên, tuổi, cân nặng, mã chuồng
//    Mở
//    rộng mỗi chuồng nhốt được 5 con vật
//    Sắp
//    xếp các con vật theo thứ tự tuổi tăng dần






//    public static void menu(){
//        System.out.println("1. Add");
//        System.out.println("2. Sub");
//        System.out.println("3. Mul");
//        System.out.println("4. Div");
//        System.out.println("5. Exit");
//    }
}