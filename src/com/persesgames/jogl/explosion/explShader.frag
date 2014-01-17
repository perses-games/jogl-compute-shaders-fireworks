#if __VERSION__ >= 130
  #define varying in
  out vec4 mgl_FragColor;
  #define texture2D texture
  #define gl_FragColor mgl_FragColor
#endif

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 varying_Color;

void main (void) {
    float alpha = 1 - smoothstep(0, 1, 2 * distance(gl_PointCoord.st, vec2(0.5, 0.5)));

    gl_FragColor = vec4(varying_Color.rgb, varying_Color.a * alpha);
}
