precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D textureFrom;
uniform sampler2D textureTo;
uniform float progress;
void main() {
    vec4 color;
    if (vTexCoord.x < progress) {
        color = texture2D(textureTo, vTexCoord);
    } else {
        color = texture2D(textureFrom, vTexCoord);
    }
    gl_FragColor = color;
}
