#version 330 core

uniform sampler2D font;

in vec2 vUv;
out vec4 COLOR;

void main() {
    float a = texture(font, vUv).r;
    COLOR = vec4(a, a, a, 1.0); // reines Graubild vom Fontatlas
}
