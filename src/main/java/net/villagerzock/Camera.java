package net.villagerzock;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;

public record Camera(Vector2f position, Vector2i size, AtomicFloat sizeMultiplier) implements ICamera {

    /**
     * Effektive Kameragröße mit Zoom (wird für die Projektion benutzt)
     */
    public Vector2f getSize() {
        float mul = sizeMultiplier.getValue();
        return new Vector2f(size.x * mul, size.y * mul);
    }

    /**
     * View-Matrix: verschiebt die Welt so, dass die Kamera-Mitte im Ursprung liegt.
     * (klassisch: Welt - camPos)
     */
    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .identity()
                .translate(-position.x, -position.y, 0.0f)
                .scale(sizeMultiplier.getValue(), sizeMultiplier.getValue(), sizeMultiplier.getValue()).invert();
    }

    /**
     * Orthografische Projektionsmatrix für 2D:
     * sichtbarer Bereich = [-size.x, size.x] x [-size.y, size.y]
     * (mit Zoom bereits eingerechnet)
     */
    public Matrix4f getProjMatrix() {
        Vector2f s = getSize(); // mit sizeMultiplier

        return new Matrix4f()
                .identity()
                .ortho2D(
                        -s.x, s.x,
                        -s.y, s.y
                );
    }

}
