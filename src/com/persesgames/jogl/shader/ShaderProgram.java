package com.persesgames.jogl.shader;

import com.persesgames.jogl.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.opengl.GL2ES2;
import java.util.HashMap;
import java.util.Map;

/*-----------------------------------------------------+
 |                      App                             |
 |                    pPPAPPp                           |
 |                   APP  PPa                           |
 |                  APA  pPP  PapA  PapA                |
 |                 PPA   APA pP  P pP  P                |
 |             APPPPPPPA PPp Ap Ap Ap Ap                |
 |             apPPA    aPP  P     P                    |
 |              APA     pPP  p     p                    |
 |             pPP      PPA                             |
 |             PPp      PPPp                            |
 |                                                      |
 | Created by:    App Software                          |
 | Email:         info@appsoftware.nl                   |
 | Web:           http://www.appsoftware.nl/            |
 |                                                      |
 +-----------------------------------------------------*/

public class ShaderProgram {
    private final static Logger logger = LoggerFactory.getLogger(Renderer.class);

    private GL2ES2              gl;

    private int                 shaderProgram;
    private int                 vertShader;
    private int                 fragShader;

    private Map<String, Integer> uniformLocations = new HashMap<>();
    private Map<String, Integer> attribLocations = new HashMap<>();

    public ShaderProgram(GL2ES2 gl, String vertex, String fragment) {
        this.gl = gl;

        if (gl.isGL3core()) {
            logger.info("GL3 core detected: explicit adding #version 130 to shaders");

            vertex = "#version 130\n" + vertex;
            fragment = "#version 130\n" + fragment;
        }

        vertShader = createAndCompileShader(GL2ES2.GL_VERTEX_SHADER, vertex);
        fragShader = createAndCompileShader(GL2ES2.GL_FRAGMENT_SHADER, fragment);

        shaderProgram = gl.glCreateProgram();

        gl.glAttachShader(shaderProgram, vertShader);
        gl.glAttachShader(shaderProgram, fragShader);

        gl.glLinkProgram(shaderProgram);
    }

    public int getUniformLocation(String uniform) {
        Integer result = uniformLocations.get(uniform);

        if (result == null) {
            result = gl.glGetUniformLocation(shaderProgram, uniform);

            uniformLocations.put(uniform, result);
        }

        return result;
    }

    public int getAttribLocation(String attrib) {
        Integer result = attribLocations.get(attrib);

        if (result == null) {
            result = gl.glGetAttribLocation(shaderProgram, attrib);

            attribLocations.put(attrib, result);
        }

        return result;
    }

    public void bindAttributeLocation(int location, String name) {
        gl.glBindAttribLocation(shaderProgram, location, name);
    }

    public void begin() {
        gl.glUseProgram(shaderProgram);
    }

    public void end() {
        gl.glUseProgram(0);
    }

    public void dispose() {
        gl.glDetachShader(shaderProgram, vertShader);
        gl.glDeleteShader(vertShader);
        gl.glDetachShader(shaderProgram, fragShader);
        gl.glDeleteShader(fragShader);
        gl.glDeleteProgram(shaderProgram);
    }

    private int createAndCompileShader(int type, String shaderString) {
        int shader = gl.glCreateShader(type);

        String[] vlines = new String[]{shaderString};
        int[] vlengths = new int[]{vlines[0].length()};

        gl.glShaderSource(shader, vlines.length, vlines, vlengths, 0);
        gl.glCompileShader(shader);

        int[] compiled = new int[1];
        gl.glGetShaderiv(shader, GL2ES2.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            int[] logLength = new int[1];
            gl.glGetShaderiv(shader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(shader, logLength[0], (int[]) null, 0, log, 0);

            throw new IllegalStateException("Error compiling the shader: " + new String(log));
        }

        return shader;
    }

}
