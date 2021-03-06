package ru.futoke.arproject.ar.libgdx;

import android.util.Log;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.FPSLogger;

import java.io.File;

import ru.futoke.arproject.ar.vuforia.VuforiaRenderer;

/**
 * Instance of libgdx Game class responsible for rendering 3D content over augmented reality.
 */
public class Engine extends Game {

    private FPSLogger fps;
    private File modelPath;
    private VuforiaRenderer vuforiaRenderer;

    public Engine(VuforiaRenderer vuforiaRenderer, File modelPath) {
        this.vuforiaRenderer = vuforiaRenderer;
        this.modelPath = modelPath;
    }

    @Override
    public void create () {
        Display mDisplay = new Display(vuforiaRenderer, modelPath);
        setScreen(mDisplay);
        vuforiaRenderer.initRendering();
        fps = new FPSLogger();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        Log.d("ENGINE", "Resize: "  +width + "x"  +height);
        vuforiaRenderer.onSurfaceChanged(width, height);
    }

    @Override
    public void render () {
        super.render();
        fps.log();
    }

}
