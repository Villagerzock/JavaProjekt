package net.villagerzock;

public class Launch {
    public static void main(String[] args) {
        new LaunchInjector().inject(args,(a)->{
            return Main.run(args);
        },"mods");
    }
}
