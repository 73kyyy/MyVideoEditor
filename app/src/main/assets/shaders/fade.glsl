precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D textureFrom;
uniform sampler2D textureTo;
uniform float progress;
void main() {
    vec4 from = texture2D(textureFrom, vTexCoord);
    vec4 to = texture2D(textureTo, vTexCoord);
    gl_FragColor = mix(from, to, progress);
}
