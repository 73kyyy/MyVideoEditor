precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D texture;
uniform float saturation;
void main() {
    vec4 color = texture2D(texture, vTexCoord);
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(vec3(gray), color.rgb, saturation);
    gl_FragColor = color;
}
