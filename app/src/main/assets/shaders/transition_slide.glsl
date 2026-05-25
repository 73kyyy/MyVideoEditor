precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D textureFrom;
uniform sampler2D textureTo;
uniform float progress;
uniform vec2 direction;
void main() {
    vec2 offset = direction * progress;
    vec2 uvFrom = vTexCoord + offset;
    vec2 uvTo = vTexCoord - direction * (1.0 - progress);
    vec4 color;
    if (uvTo.x >= 0.0 && uvTo.x <= 1.0 && uvTo.y >= 0.0 && uvTo.y <= 1.0 && progress > 0.5) {
        color = texture2D(textureTo, uvTo);
    } else if (uvFrom.x >= 0.0 && uvFrom.x <= 1.0 && uvFrom.y >= 0.0 && uvFrom.y <= 1.0) {
        color = texture2D(textureFrom, uvFrom);
    } else {
        color = mix(texture2D(textureFrom, vTexCoord), texture2D(textureTo, vTexCoord), progress);
    }
    gl_FragColor = color;
}
