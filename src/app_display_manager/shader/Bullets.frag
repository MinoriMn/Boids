#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif


//uniform float sharpness;

in vec4 vertColor;
in vec2 center;
in vec2 pos;

void main() {
  float len = w/2.0 - length(pos); //半径からの距離
  float len2 = length(pos); //半径からの距離
  float t = 5 / len2;
  vec4 color = vec4(1.0, 1.0, 1.0, t) * vertColor;
  color = mix(vec4(0.0), color, sharpness); //x(1-a)+y*aを返す（つまり線形補間）
  color = clamp(color, 0.0, 1.0); //min(max(x, a), b)
  gl_FragColor = color;


}