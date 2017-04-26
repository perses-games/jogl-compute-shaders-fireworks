package com.persesgames.jogl.shader;

import javax.media.opengl.GL4;

public class ComputeProgram extends Program {

    private GL4                 gl4;
    private int                 computeShader;

    public ComputeProgram(GL4 gl, String compute) {
        super(gl);
        this.gl4 = gl;

        computeShader = createAndCompileShader(GL4.GL_COMPUTE_SHADER, compute);

        program = gl.glCreateProgram();

        gl.glAttachShader(program, computeShader);

        gl.glLinkProgram(program);
    }

    public void compute(int x, int y, int z) {
        gl4.glDispatchCompute(x, y, z);
    }

    public void dispose() {
        gl.glDetachShader(program, computeShader);
        gl.glDeleteShader(computeShader);

        super.dispose();
    }
}
