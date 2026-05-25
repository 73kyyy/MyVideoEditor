precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D textureFrom;
uniform sampler2D textureTo;
uniform float progress;
uniform float smoothness;
uniform int transitionType;
void main() {
    vec4 from = texture2D(textureFrom, vTexCoord);
    vec4 to = texture2D(textureTo, vTexCoord);
    float p = smoothstep(0.0, smoothness, progress);
    gl_FragColor = mix(from, to, p);
}
