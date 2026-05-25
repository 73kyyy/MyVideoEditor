precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D textureFrom;
uniform sampler2D textureTo;
uniform float progress;
uniform vec2 direction;
void main() {
    vec2 uv = vTexCoord + direction * progress;
    vec4 color;
    if (uv.x >= 0.0 && uv.x <= 1.0 && uv.y >= 0.0 && uv.y <= 1.0) {
        color = texture2D(textureTo, uv);
    } else {
        uv = vTexCoord - direction * (1.0 - progress);
        color = texture2D(textureFrom, uv);
    }
    gl_FragColor = color;
}
