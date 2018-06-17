package cpw.mods.sidedtest;

public class BadClass {
    static {
        System.out.println("BARF?");
        if (!false) {
            throw new RuntimeException("barf");
        }
    }
}
