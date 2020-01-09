#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform vec2 center;
uniform float r;
uniform float a;

void main() {
	// division
	float l = length(gl_FragCoord.xy - center);


//    gl_FragColor = gray >= 0.5 ? 1.0 - 2.0 * (1.0 - gray) * (vec4(1.0) - vertColor) : 2.0 * gray * vertColor;
    float gray = min(a / l, 1.0);
    float alpha = l <= r ? 1.0 : 0.0;

    //gl_FragColor = gray >= 0.5 ? 1.0 - 2.0 * (1.0 - gray) * (vec4(1.0) - vertColor) : 2.0 * gray * vertColor;
    gl_FragColor = vec4(vec3(gray), alpha);

}