package net.villagerzock;

import org.joml.Vector2f;
import org.joml.Vector2i;

public interface ICamera {
    Vector2f position();
    Vector2i size();
    AtomicFloat sizeMultiplier();
    Vector2f getSize();
}
