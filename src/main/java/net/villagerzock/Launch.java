package net.villagerzock;

public class Launch {
    public static void main(String[] args) {
        new LaunchInjector().inject(args,new SimpleMain("net.villagerzock.Main","run"),"mods");
    }
}