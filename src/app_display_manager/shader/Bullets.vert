uniform mat4 projection;
uniform mat4 transform;

in vec4 position;
in vec4 color;
in vec2 offset;

out vec4 vertColor;
out vec2 center;
out vec2 pos;

void main() {
  vec4 clip = transform * position;//poz = 0 -> position.z = -310
  gl_Position = clip + projection * vec4(offset, 0, 0);

  vertColor = color;
  center = clip.xy;
  pos = offset;
}