precision mediump float;//精度修飾子

//
//uniform vec2 resolution;
//uniform float minRes;
//uniform vec2 mouse;

void main(void){
//    //vec2 m = vec2(mouse.x * 2.0 - 1.0, -mouse.y * 2.0 + 1.0);
//	vec2 p = (gl_FragCoord.xy * 2.0 - resolution) / minRes;//-1 ～ 1 の範囲に正規, スクリーンの中心を (0.0, 0.0) とする
//
//	// division
//	float t = 0.05 / length(gl_PointCoord - p);
//
//	//gl_FragColor = vec4(vec2(t / 5.0), t , 1.0);
//
//
////    vec3 n;
////	n.xy = gl_PointCoord;  // 座標値
////      n.z = 1.0 - dot(n.xy, n.xy);  // 1 から x と y のそれぞれの二乗和を引く
////      if (n.z < 0.0) discard;  // 結果が負ならフラグメントを捨てる
////
//      gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);

 vec3 n;

  n.xy = gl_PointCoord * 2.0 - 1.0;  // 座標値を [0, 1] → [-1, 1] に変換する
  n.z = 1.0 - dot(n.xy, n.xy);  // 1 から x と y のそれぞれの二乗和を引く
  if (n.z < 0.0) discard;  // 結果が負ならフラグメントを捨てる

  gl_FragColor = vec4(1.0);  // 白色を描くだけ
}