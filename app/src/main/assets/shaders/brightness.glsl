precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D texture;
uniform float brightness;
void main() {
    vec4 color = texture2D(texture, vTexCoord);
    color.rgb += brightness;
    gl_FragColor = color;
}
