package ru.futoke.arproject.ar.libgdx;

import android.os.Environment;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.ExternalFileHandleResolver;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

import ru.futoke.arproject.ar.vuforia.VuforiaRenderer;
import ru.futoke.arproject.renderer.Utils;

/**
 * Screen implementation responsible for model loading and calling renderer properly.
 */
public class Display implements Screen {

    public ModelInstance modelInstance;
    public Model model;

    private Renderer mRenderer;

    public Display(VuforiaRenderer vuforiaRenderer) {

        mRenderer = new Renderer(vuforiaRenderer);

        // Find models.
        File dir = new File(
            Environment.getExternalStorageDirectory()
                + "/"
                + Utils.workDirectory
        );
        // If File is not present create directory.
        if (!dir.exists()) {
            dir.mkdir();
        }

        // Find models.
        String[] extensions = new String[] { "g3db" };
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
        String modelFile = files.get(0).getName();

//        UBJsonReader jsonReader = new UBJsonReader();
//        G3dModelLoader modelLoader = new G3dModelLoader(jsonReader);
//        model = modelLoader.loadModel(Gdx.files.absolute(modelFile));
//        modelInstance = new ModelInstance(model,0,0,0);

        AssetManager assets = new AssetManager(new ExternalFileHandleResolver());
        assets.load(Utils.workDirectory + "/" + modelFile, Model.class);
        assets.finishLoading();

        model = assets.get(Utils.workDirectory + "/" + modelFile, Model.class);
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