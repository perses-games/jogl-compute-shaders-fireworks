package com.persesgames.jogl;

import com.jogamp.newt.opengl.GLWindow;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;

/**
 * Date: 10/25/13
 * Time: 7:27 PM
 */
public class TestJogl {

    public static void main(String [] args) {
        TestJogl test = new TestJogl();

        test.run();
    }

    private final Renderer renderer;

    public TestJogl() {
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4ES3));

        caps.setDoubleBuffered(true);

        GLWindow glWindow = GLWindow.create(caps);

        glWindow.setTitle("jogl-triangle");

        glWindow.setSize(800, 800);

        glWindow.setFullscreen(false);
        glWindow.setUndecorated(false);
        glWindow.setPointerVisible(true);
        glWindow.setVisible(true);

        Keyboard keyboard = new Keyboard();
        glWindow.addKeyListener(keyboard);

        renderer = new Renderer(glWindow, keyboard);

        glWindow.addGLEventListener(renderer);
    }

    public void run() {
        renderer.run();
    }

}
