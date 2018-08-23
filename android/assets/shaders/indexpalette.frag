uniform sampler2D texture;
uniform sampler2D colorTable;
uniform float paletteIndex;
varying vec2 v_texCoords;

void main() {
    vec4 color = texture2D(texture, v_texCoords);
    vec2 index = vec2(color.r, paletteIndex);
    vec4 indexedColor = texture2D(colorTable, index);
    gl_FragColor = vec4(indexedColor.rgb, color.a); // This way we'll preserve alpha
}