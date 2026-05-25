precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D textureFrom;
uniform sampler2D textureTo;
uniform float progress;
void main() {
    vec2 center = vec2(0.5);
    float scaleFrom = 1.0 + progress * 0.5;
    float scaleTo = 1.5 - progress * 0.5;
    vec2 uv1 = (vTexCoord - center) / scaleFrom + center;
    vec2 uv2 = (vTexCoord - center) / scaleTo + center;
    uv1 = clamp(uv1, 0.0, 1.0);
    uv2 = clamp(uv2, 0.0, 1.0);
    vec4 from = texture2D(textureFrom, uv1);
    vec4 to = texture2D(textureTo, uv2);
    gl_FragColor = mix(from, to, progress);
}
