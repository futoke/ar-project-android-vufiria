package ru.futoke.arproject.ar.libgdx;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

import java.io.File;

import ru.futoke.arproject.Utils;
import ru.futoke.arproject.ar.vuforia.VuforiaRenderer;

/**
 * Screen implementation responsible for model loading and calling renderer properly.
 */
public class Display implements Screen {

    public ModelInstance modelInstance;
    public Model model;

    private Renderer mRenderer;

    public Display(VuforiaRenderer vuforiaRenderer, File modelPath) {

        mRenderer = new Renderer(vuforiaRenderer);

        String modelFile = Utils.getFileByExtension("g3db", modelPath);
        String filePath = Utils.workDir
            + "/"
            + modelPath.getName()
            + "/"
            + modelFile;
        AssetManager assets = new AssetManager(new ExternalFileHandleResolver());
        assets.load(filePath, Model.class);
        assets.finishLoading();

        model = assets.get(filePath, Model.class);
        modelInstance = new ModelInstance(model);
    }

    @Override
    public void render(float delta) {
        mRenderer.render(this, delta);
    }

    @Override
    public void dispose() {
        mRenderer.dispose();
    }


    @Override
    public void resize(int i, int i2) {

    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }
}