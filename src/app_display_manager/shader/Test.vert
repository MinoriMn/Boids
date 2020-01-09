//uniform mat4 modelViewProjectionMatrix;
//
//layout(location = 0) in vec2 particle_vert;
//layout(location = 1) in vec3 pos;
//layout(location = 2) in vec4 color;

uniform mat4 projection; //プロジェクション行列
uniform mat4 modelview; //モデルビュー行列
//
//uniform sampler2D vertex;
//uniform vec2 res;
//
//in vec4 position;
//in vec4 color;
in vec2 offset; // is the (2D) displacement from the center point to the edge vertex.
//
//out vec4 vertColor;
//out vec2 texCoord;

layout(location = 0) in int index;

uniform sampler2D vert_texture;
uniform float pointScale;
uniform vec2 resolution;
uniform float minRes;

uniform vec2 mouse;
uniform mat4 transform;

void main() {
//  vec4 pos = modelview * vec4(mouse, 0.0, 1.0); //カメラ座標系変換
//  vec4 clip = projection * pos; //投影変換
//
//  gl_Position = clip + projection * vec4(offset, 0, 0);//clipで得た投影座標にoffset分追加しているようだが？


    vec2 mfix = vec2((mouse.x * 2.0 - resolution.x) / minRes, (-mouse.y * 2.0 + resolution.y) / minRes);
    vec4 m = vec4(mfix, 0.0, 1.0) * transform;

   gl_Position = m;
   gl_PointSize = 20 + pointScale;
}