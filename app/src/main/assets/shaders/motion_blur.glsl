precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D texture;
uniform vec2 direction;
uniform float strength;
void main() {
    vec4 color = vec4(0.0);
    float total = 0.0;
    for (float i = -4.0; i <= 4.0; i += 1.0) {
        float weight = 1.0 - abs(i) / 4.0;
        vec2 uv = vTexCoord + direction * i * strength * 0.01;
        uv = clamp(uv, 0.0, 1.0);
        color += texture2D(texture, uv) * weight;
        total += weight;
    }
    gl_FragColor = color / total;
}
