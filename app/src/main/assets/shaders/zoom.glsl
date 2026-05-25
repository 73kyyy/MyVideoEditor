precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D textureFrom;
uniform sampler2D textureTo;
uniform float progress;
void main() {
    vec2 center = vec2(0.5);
    vec2 uv1 = (vTexCoord - center) * (1.0 + progress * 0.5) + center;
    vec2 uv2 = (vTexCoord - center) * (1.5 - progress * 0.5) + center;
    vec4 from = texture2D(textureFrom, uv1);
    vec4 to = texture2D(textureTo, uv2);
    gl_FragColor = mix(from, to, progress);
}
