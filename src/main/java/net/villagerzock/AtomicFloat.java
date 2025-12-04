package net.villagerzock;

public class AtomicFloat {
    private float value;

    public AtomicFloat() {
        value = 0;
    }
    public AtomicFloat(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}

