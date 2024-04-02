package net.mslivo.core.engine.ui_engine.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.CMediaModel;

import java.util.HashMap;

public class ModelRenderer {
    private static final String ERROR_CAMERA_NOT_SET = "No camera set";
    private static final float MODEL_Z = -50;
    private final MediaManager mediaManager;
    private final HashMap<CMediaModel, ModelInstance> modelInstances;
    private OrthographicCamera camera;
    private Environment environment;
    private final ModelBatch modelBatch;
    private boolean drawing;
    private Quaternion rotation;

    public ModelRenderer(MediaManager mediaManager, Environment environment) {
        this(mediaManager, environment, null);
    }

    public ModelRenderer(MediaManager mediaManager, Environment environment, ShaderProvider shaderProvider) {
        this.mediaManager = mediaManager;
        this.modelInstances = new HashMap<>();
        this.modelBatch = new ModelBatch(shaderProvider);
        this.environment = environment;
        this.rotation = new Quaternion();
        this.camera = null;
        drawing = false;
    }

    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void begin() {
        if (drawing) throw new IllegalStateException("ModelRenderer.end must be called before begin.");
        if (this.camera == null) throw new RuntimeException(ERROR_CAMERA_NOT_SET);
        this.modelBatch.begin(this.camera);
        this.drawing = true;
    }

    public void end() {
        if (!drawing) throw new IllegalStateException("ModelRenderer.begin must be called before end.");
        this.modelBatch.end();
        this.drawing = false;
    }

    public void drawCMediaModel(CMediaModel cMediaModel, float x, float y) {
        drawCMediaModel(cMediaModel, x, y, 10f, 10f, 10f,0f,0f,0f);
    }

    public void drawCMediaModel(CMediaModel cMediaModel, float x, float y, float scale) {
        drawCMediaModel(cMediaModel, x, y, scale, scale, scale,0f,0f,0f);
    }

    public void drawCMediaModel(CMediaModel cMediaModel, float x, float y, float scaleX, float scaleY, float scaleZ) {
        drawCMediaModel(cMediaModel, x, y, scaleX, scaleY, scaleZ,0f,0f,0f);
    }

    public void drawCMediaModel(CMediaModel cMediaModel, float x, float y, float scaleX, float scaleY, float scaleZ,
                                float pitch, float yaw, float roll) {
        ModelInstance modelInstance = this.modelInstances.get(cMediaModel);
        if (modelInstance == null) {
            modelInstance = new ModelInstance(mediaManager.getCMediaModel(cMediaModel));
            this.modelInstances.put(cMediaModel, modelInstance);
        }

        modelInstance.transform.idt();
        modelInstance.transform.trn(x,y,MODEL_Z);
        modelInstance.transform.scl(scaleX, scaleY, scaleZ);
        rotation.setEulerAngles(pitch,yaw,roll);
        modelInstance.transform.rotate(rotation);
        modelBatch.render(modelInstance, environment);
    }


}
