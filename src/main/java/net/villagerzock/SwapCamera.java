package net.villagerzock;

import lombok.Getter;
import org.joml.Vector2f;
import org.joml.Vector2i;

public class SwapCamera implements ICamera {
    private final Camera[] cameras;
    @Getter
    private int camera = 0;

    public SwapCamera(Camera... cameras) {
        this.cameras = cameras;
    }


    @Override
    public Vector2f position() {
        return cameras[getCamera()].position();
    }

    @Override
    public Vector2i size() {
        return cameras[getCamera()].size();
    }

    @Override
    public AtomicFloat sizeMultiplier() {
        return cameras[getCamera()].sizeMultiplier();
    }

    @Override
    public Vector2f getSize() {
        return cameras[getCamera()].getSize();
    }
}
