precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D textureFrom;
uniform sampler2D textureTo;
uniform float progress;
void main() {
    vec2 center = vec2(0.5);
    float angle = progress * 3.14159;
    float s = sin(angle); float c = cos(angle);
    vec2 uv = vTexCoord - center;
    uv = vec2(uv.x * c - uv.y * s, uv.x * s + uv.y * c) + center;
    uv = clamp(uv, 0.0, 1.0);
    vec4 from = texture2D(textureFrom, uv);
    vec4 to = texture2D(textureTo, vTexCoord);
    gl_FragColor = mix(from, to, progress);
}
