#version 120
uniform vec2 Size;
varying vec4 texProj0;
void main() {
    vec2 position = gl_Vertex.xy / Size;
    gl_Position = vec4(position.x * 2.0 - 1.0, 1.0 - position.y * 2.0, 0.0, 1.0);
    texProj0 = vec4(position.x, 1.0 - position.y, 0.0, 1.0);
}
