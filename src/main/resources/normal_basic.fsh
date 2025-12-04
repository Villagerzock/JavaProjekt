#version 330 core

uniform sampler2DArray textures;
uniform sampler2D font;
uniform vec4 shaderColor;

out vec4 COLOR;


in vec2 vUv;
in vec2 vPos;
in vec2 minUv;
in vec4 vColor;
flat in int vLayer;
in vec2 vTexSize;
flat in int vIsFont;

void main(){
    if(vPos.y >= 0.5){
        discard;
    }

    vec4 col;

    if (vLayer >= 0 && vIsFont == 0) {
        // Tiling: falte Tile-UV auf 0..1
        vec2 localUv = mod(vUv, 1.0);
        // Sub-Rect im Atlas
        vec2 atlasUv = minUv + localUv * vTexSize;

        col = texture(textures, vec3(atlasUv, vLayer));
    } else if (vIsFont > 0) {
        // Font-Rendering wie gehabt
        col = vec4(1.0, 1.0, 1.0, texture(font, vUv).r);
    } else {
        col = vec4(1.0);
    }

    COLOR = col * vColor * shaderColor;
}