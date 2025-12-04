#version 330 core

uniform sampler2DArray textures;
uniform sampler2D font;
uniform vec4 shaderColor;

out vec4 COLOR;


in vec2 vUv;
in vec2 minUv;
in vec2 vPos;
in vec4 vColor;
flat in int vLayer;
in vec2 vTexSize;
flat in int vIsFont;

void main(){
    if(vPos.y <= 0.5){
        discard;
    }

    vec4 col;

    if (vLayer >= 0 && vIsFont == 0) {
        // 0..1 innerhalb EINER Tile
        vec2 local = fract(vUv);
        // auf Subrect im Atlas mappen
        vec2 atlasUv = minUv + local * vTexSize;

        col = texture(textures, vec3(atlasUv, vLayer));
    } else if (vIsFont > 0) {
        col = vec4(1.0, 1.0, 1.0, texture(font, vUv).r);
    } else {
        col = vec4(1.0);
    }

    COLOR = col * vColor * shaderColor;
    COLOR.rgb = vec3(1,1,1)-COLOR.rgb;
}