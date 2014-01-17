package com.persesgames.jogl.explosion;

import com.jogamp.common.nio.Buffers;
import com.persesgames.jogl.shader.ComputeProgram;
import com.persesgames.jogl.shader.ShaderProgram;
import com.persesgames.jogl.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL4;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

/**
 * Date: 1/15/14
 * Time: 8:23 PM
 */
public class ExplosionComputeHandler {
    private final static Logger logger = LoggerFactory.getLogger(ExplosionComputeHandler.class);

    private final static int EXPLOSION_PARTICLES        = 256;
    private final static int MAX_EXPLOSION_PARTICLES    = 250000;

    private final Random    random                      = new Random(System.nanoTime());

    private GL4             gl;

    private ShaderProgram   explProgram;

    private ComputeProgram  explosionProgram;
    private ComputeProgram  explCleanUpProgram;

    private FloatBuffer     explBuffer                  = Buffers.newDirectFloatBuffer(MAX_EXPLOSION_PARTICLES * 8);
    private IntBuffer       atomicBuffer                = Buffers.newDirectIntBuffer(5);


    private int             newParticleCount            = 0;
    private int             particleCount               = 0;

    // opengl handles
    private int explHandle;
    private int atomicHandle;

    public ExplosionComputeHandler(GL4 gl) {
        this.gl = gl;
    }

    public void createNewExplosionData() {
        float x = random.nextFloat() * 2f - 1f;
        float y = random.nextFloat() * 2f - 1f;

        if ((particleCount + newParticleCount) > (MAX_EXPLOSION_PARTICLES - EXPLOSION_PARTICLES)) {
            return;
        }

        float r = random.nextFloat() * 0.25f;
        float g = random.nextFloat() * 0.25f;
        float b = random.nextFloat() * 0.25f;

        for (int i = newParticleCount; i < newParticleCount + EXPLOSION_PARTICLES; i++) {
            int offset = i * 8;

            float angle = (float) (random.nextFloat() * Math.PI * 2f);
            float velocity = (random.nextFloat() * 0.25f) + (random.nextFloat() * 0.25f);

            explBuffer.put(offset + 0, x);
            explBuffer.put(offset + 1, y);
            explBuffer.put(offset + 2, (float) (Math.sin(angle) * velocity));
            explBuffer.put(offset + 3, (float) (Math.cos(angle) * velocity));

            explBuffer.put(offset + 4, r);
            explBuffer.put(offset + 5, g);
            explBuffer.put(offset + 6, b);
            explBuffer.put(offset + 7, random.nextFloat() * 0.5f + 0.5f);
        }

        newParticleCount = newParticleCount + EXPLOSION_PARTICLES;
    }

    public void init() {
        explosionProgram = new ComputeProgram(gl, Util.loadAsText(getClass(), "explosion.comp"));
        explosionProgram.getUniformLocation("delta");

        explCleanUpProgram = new ComputeProgram(gl, Util.loadAsText(getClass(), "explosionCleanUp.comp"));

        explProgram = new ShaderProgram(gl,
                Util.loadAsText(getClass(), "explShader.vert"),
                Util.loadAsText(getClass(), "explShader.frag"));

        int[] tmpHandle = new int[2];
        gl.glGenBuffers(2, tmpHandle, 0);

        explHandle = tmpHandle[0];
        atomicHandle = tmpHandle[1];

        // Select the VBO, GPU memory data, to use for vertices
        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, explHandle);

        // transfer data to VBO, this perform the copy of data from CPU -> GPU memory
        gl.glBufferData(GL.GL_ARRAY_BUFFER, explBuffer.limit() * 4, explBuffer, GL.GL_DYNAMIC_DRAW);
    }

    public void updateGpu() {
        // Select the VBO, GPU memory data, to use for vertices
        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, explHandle);

        // transfer data to VBO, this perform the copy of data from CPU -> GPU memory
        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, particleCount * 4 * 8, newParticleCount * 4 * 8, explBuffer);

        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, 0);

        gl.glBindBuffer(GL4.GL_ATOMIC_COUNTER_BUFFER, atomicHandle);

        atomicBuffer.put(0, particleCount + newParticleCount);

        // transfer data to VBO, this perform the copy of data from CPU -> GPU memory
        gl.glBufferData(GL4.GL_ATOMIC_COUNTER_BUFFER, 4, atomicBuffer, GL.GL_DYNAMIC_DRAW);

        gl.glBindBuffer(GL4.GL_ATOMIC_COUNTER_BUFFER, 0);

        newParticleCount = 0;
    }

    public void execute(float delta) {
        explosionProgram.begin();

        gl.glUniform1f(explosionProgram.getUniformLocation("delta"), delta);

        gl.glBindBufferBase(GL4.GL_SHADER_STORAGE_BUFFER, 0, explHandle);
        gl.glBindBufferBase(GL4.GL_ATOMIC_COUNTER_BUFFER, 1, atomicHandle);

        explosionProgram.compute((particleCount / 512) + 1, 1, 1);

        gl.glBindBufferBase(GL4.GL_SHADER_STORAGE_BUFFER, 0, 0);
        gl.glBindBufferBase(GL4.GL_ATOMIC_COUNTER_BUFFER, 1, 0);

        explosionProgram.end();
    }

    public void cleanUp() {
        explCleanUpProgram.begin();

        gl.glBindBufferBase(GL4.GL_SHADER_STORAGE_BUFFER, 0, explHandle);
        gl.glBindBufferBase(GL4.GL_ATOMIC_COUNTER_BUFFER, 1, atomicHandle);

        explCleanUpProgram.compute((particleCount / 512) + 1, 1, 1);

        gl.glBindBufferBase(GL4.GL_SHADER_STORAGE_BUFFER, 0, 0);
        gl.glBindBufferBase(GL4.GL_ATOMIC_COUNTER_BUFFER, 1, 0);

        explCleanUpProgram.end();
    }

    public void render() {
        explProgram.begin();

        if (particleCount > 0) {
            gl.glEnableVertexAttribArray(0);
            gl.glEnableVertexAttribArray(1);

            gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, explHandle);

            // Associate Vertex attribute 0 with the last bound VBO
            gl.glVertexAttribPointer(0 /* the vertex attribute */, 2,
                    GL2ES2.GL_FLOAT, false /* normalized? */, 32 /* stride */,
                    0 /* The bound VBO data offset */);

            // Associate Vertex attribute 0 with the last bound VBO
            gl.glVertexAttribPointer(1 /* the vertex attribute */, 4,
                    GL2ES2.GL_FLOAT, false /* normalized? */, 32 /* stride */,
                    16 /* The bound VBO data offset */);

            gl.glDrawArrays(GL2ES2.GL_POINTS, 0, particleCount);

            gl.glDisableVertexAttribArray(0);
            gl.glDisableVertexAttribArray(1);
        }

        explProgram.end();
    }

    public void getGpuData() {
        gl.glBindBuffer(GL4.GL_ATOMIC_COUNTER_BUFFER, atomicHandle);
        gl.glGetBufferSubData(GL4.GL_ATOMIC_COUNTER_BUFFER, 0, 4, atomicBuffer);
        particleCount = atomicBuffer.get(0);
        newParticleCount = 0;
        gl.glBindBuffer(GL4.GL_ATOMIC_COUNTER_BUFFER, 0);
    }

    public int getParticleCount() {
        return particleCount;
    }

    public void dispose() {
        explProgram.dispose();
        explosionProgram.dispose();
        explCleanUpProgram.dispose();
    }
}
