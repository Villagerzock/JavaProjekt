package net.villagerzock;

public class ShaderLinkException extends RuntimeException {
    public ShaderLinkException(String name, String log) {
        super("PROGRAM LINK ERROR in " + name + ":\n" + log);
    }
}
