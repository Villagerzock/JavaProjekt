#version 330 core

uniform vec2 viewportSize;
uniform vec2 uCamPos;

layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aUv;
layout(location = 2) in vec4 oUv;
layout(location = 3) in vec4 aColor;
layout(location = 4) in float aLayer;
layout(location = 5) in float aIsFont;

out vec2 vUv;
out vec2 vPos;
out vec2 minUv;
out vec2 vTexSize;
out vec4 vColor;
flat out int vLayer;
flat out int vIsFont;

void main() {
    vUv      = aUv;
    minUv    = oUv.rg;
    vTexSize = oUv.ba - oUv.rg;
    vColor   = aColor;
    vLayer   = int(aLayer);
    vIsFont = int(aIsFont);

    vec2 pos = aPos - vec2(-uCamPos.x,uCamPos.y);

    // Pixel -> 0..1
    vec2 ndc = pos / viewportSize;
    vPos = ndc;
    // 0..1 -> -1..1
    ndc = ndc * 2.0 - 1.0;

    // Y-Achse invertieren (Screen oben = -1 in NDC)
    ndc.y = -ndc.y;

    gl_Position = vec4(ndc,0.0, 1.0);
}