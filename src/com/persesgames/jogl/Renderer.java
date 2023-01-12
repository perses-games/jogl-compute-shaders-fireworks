package com.persesgames.jogl;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.media.opengl.DebugGL4;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.persesgames.jogl.explosion.ExplosionComputeHandler;

/**
 * Date: 10/25/13
 * Time: 7:42 PM
 */
public class Renderer implements GLEventListener  {
    private final static Logger logger = LoggerFactory.getLogger(Renderer.class);

    private final static int MAX_ENTITIES_PER_COLOR     = 2000000;

    private final Random random         = new Random(System.nanoTime());

    private volatile boolean stopped    = false;
    private volatile boolean dirty      = true;

    private final GLWindow glWindow;

    private int                     width = 100, height = 100;

    private Keyboard                keyboard;

    private boolean                 checkError = false;

    private long                    lastLog = System.nanoTime();
    private long                    start = System.currentTimeMillis();
    private Timer                   timer = new Timer(TimeUnit.SECONDS, 1);

    private ExplosionComputeHandler explosionComputeHandler;

    public Renderer(GLWindow glWindow, Keyboard keyboard) {
        this.glWindow = glWindow;
        this.keyboard = keyboard;
    }

    public void stop() {
        stopped = true;
    }

    public void redraw() {
        dirty = true;
    }

    public void run() {
        Renderer.this.glWindow.display();

        while(!stopped) {
            if (dirty) {
                //logger.info("rendering+" + System.currentTimeMillis());
                Renderer.this.glWindow.display();
                //Renderer.this.glWindow.swapBuffers();
                dirty = true;
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    logger.warn(e.getMessage(), e);
                }
            }

            stopped |= keyboard.isPressed(KeyEvent.VK_ESCAPE);
        }

        Renderer.this.glWindow.destroy();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        timer.start("init");

        GL4 gl = drawable.getGL().getGL4();

        gl.setSwapInterval(0);

        // debug init
        //gl = new DebugGL4(gl);

        logger.info("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        logger.info("INIT GL IS: " + gl.getClass().getName());
        logger.info("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
        logger.info("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
        logger.info("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

        int [] result = new int[3];
        gl.glGetIntegerv(GL2.GL_MAX_VERTEX_ATTRIBS, result, 0);
        logger.info("GL_MAX_VERTEX_ATTRIBS=" + result[0]);

        glGetIntegerIndexed(gl, GL4.GL_MAX_COMPUTE_WORK_GROUP_SIZE, result);
        logger.info("GL_MAX_COMPUTE_WORK_GROUP_SIZE= {},{},{}", result[0], result[1], result[2]);

        glGetIntegerIndexed(gl, GL4.GL_MAX_COMPUTE_WORK_GROUP_COUNT, result);
        logger.info("GL_MAX_COMPUTE_WORK_GROUP_COUNT= {},{},{}", result[0], result[1], result[2]);

        explosionComputeHandler = new ExplosionComputeHandler(gl);
        explosionComputeHandler.init();
        explosionComputeHandler.createNewExplosionData();
        explosionComputeHandler.updateGpu();

        timer.stop("init");
    }

    // https://stackoverflow.com/questions/39004898/get-maximum-workgroup-size-for-compute-shaders
    private static void glGetIntegerIndexed(GL4 gl, int target, int[] data) {
        for(int i = 0; i < data.length; i++) {
        	gl.glGetIntegeri_v(target, i, data, i);
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    	stop();
        explosionComputeHandler.dispose();
    }

    private long lastDelta = System.nanoTime();
    private float delta = 0f;
    private void calculateCurrentDelta() {
        long nanoDelta = System.nanoTime() - lastDelta;

        delta = (nanoDelta / 1000000000f);

        if (keyboard.isPressed(KeyEvent.VK_CONTROL)) {
            delta = delta / 10f;
        }

        lastDelta = System.nanoTime();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        calculateCurrentDelta();

        GL4 gl = drawable.getGL().getGL4();

        if (checkError) {
            // debug
            gl.glGetError();
            gl = new DebugGL4(gl);
        }

        if (keyboard.isPressed(KeyEvent.VK_SHIFT)) {
            explosionComputeHandler.createNewExplosionData();
            explosionComputeHandler.updateGpu();
        }

        if (keyboard.isReleased(KeyEvent.VK_SPACE)) {
            explosionComputeHandler.createNewExplosionData();
            explosionComputeHandler.updateGpu();
        }

        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_DST_ALPHA);

        gl.glViewport(0, 0, width, height);

        // Clear screen
        gl.glClearColor(0.1f, 0.0f, 0.1f, 1f);
        gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);

        timer.start("compute");

        explosionComputeHandler.execute(delta);
        explosionComputeHandler.cleanUp();

        timer.stop("compute");

        timer.start("getGpuData");

        explosionComputeHandler.getGpuData();

        timer.stop("getGpuData");

        timer.start("draw");

        explosionComputeHandler.render();

        timer.stop("draw");

        timer.log();

        if (lastLog < System.nanoTime() - TimeUnit.SECONDS.toNanos(1)) {
            lastLog = System.nanoTime();

            logger.info("Explosion particles: {}", explosionComputeHandler.getParticleCount());
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        logger.info("reshape+" + System.currentTimeMillis());

        this.width = w;
        this.height = h;
    }

}
