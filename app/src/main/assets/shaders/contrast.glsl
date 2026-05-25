precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D texture;
uniform float contrast;
void main() {
    vec4 color = texture2D(texture, vTexCoord);
    color.rgb = (color.rgb - 0.5) * contrast + 0.5;
    gl_FragColor = color;
}
