package com.persesgames.jogl.shader;

import com.persesgames.jogl.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.opengl.GL2ES2;

public class ShaderProgram extends Program {
    private final static Logger logger = LoggerFactory.getLogger(Renderer.class);

    private int                 vertShader;
    private int                 fragShader;

    public ShaderProgram(GL2ES2 gl, String vertex, String fragment) {
        super(gl);

        if (gl.isGL3core()) {
            logger.info("GL3 core detected: explicit adding #version 130 to shaders");

            vertex = "#version 130\n" + vertex;
            fragment = "#version 130\n" + fragment;
        }

        vertShader = createAndCompileShader(GL2ES2.GL_VERTEX_SHADER, vertex);
        fragShader = createAndCompileShader(GL2ES2.GL_FRAGMENT_SHADER, fragment);

        program = gl.glCreateProgram();

        gl.glAttachShader(program, vertShader);
        gl.glAttachShader(program, fragShader);

        gl.glLinkProgram(program);
    }

    public void dispose() {
        gl.glDetachShader(program, vertShader);
        gl.glDeleteShader(vertShader);
        gl.glDetachShader(program, fragShader);
        gl.glDeleteShader(fragShader);

        super.dispose();
    }

}
