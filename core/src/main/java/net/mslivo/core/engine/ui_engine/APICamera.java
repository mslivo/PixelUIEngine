package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.math.Matrix4;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.components.viewport.AppViewport;

import java.util.ArrayList;

public final class APICamera {
    private final API api;
    private final UIEngineState uiEngineState;
    private final UIConfig uiConfig;
    private final MediaManager mediaManager;
    public final APIAppViewports appViewport;

    APICamera(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.uiConfig = uiEngineState.config;
        this.mediaManager = mediaManager;
        this.appViewport = new APIAppViewports();
    }

    public boolean pointVisible(float x, float y) {
        if (uiEngineState.camera_app.frustum.pointInFrustum(x, y, 0f)) return true;
        return false;
    }

    public boolean rectVisible(float x, float y, float width, float height) {
        if (uiEngineState.camera_app.frustum.boundsInFrustum(x, y, 0f, width, height, 0f)) return true;
        return false;
    }

    public boolean sphereVisible(float x, float y, float radius) {
        if (uiEngineState.camera_app.frustum.sphereInFrustum(x, y, 0f, radius)) return true;
        return false;
    }

    public void setPosition(float x, float y) {
        UICommonUtils.camera_setPosition(uiEngineState.camera_app, x, y);
    }

    public void move(float x, float y) {
        UICommonUtils.camera_setPosition(uiEngineState.camera_app,
                (uiEngineState.camera_app.position.x + x),
                (uiEngineState.camera_app.position.y + y)
        );
    }

    public void setX(float x) {
        UICommonUtils.camera_setPosition(uiEngineState.camera_app,
                x,
                uiEngineState.camera_app.position.y
        );
    }

    public void moveX(float x) {
        UICommonUtils.camera_setPosition(uiEngineState.camera_app,
                (uiEngineState.camera_app.position.x + x),
                uiEngineState.camera_app.position.y
        );
    }

    public void setY(float y) {
        UICommonUtils.camera_setPosition(uiEngineState.camera_app,
                uiEngineState.camera_app.position.x,
                y
        );
    }

    public void moveY(float y) {
        UICommonUtils.camera_setPosition(uiEngineState.camera_app,
                uiEngineState.camera_app.position.x,
                (uiEngineState.camera_app.position.y + y)
        );
    }

    public void setZoom(float zoom) {
        UICommonUtils.camera_setZoom(uiEngineState.camera_app, zoom);
    }

    public float x() {
        return uiEngineState.camera_app.position.x;
    }

    public float y() {
        return uiEngineState.camera_app.position.y;
    }

    public float z() {
        return uiEngineState.camera_app.position.z;
    }

    public float zoom() {
        return uiEngineState.camera_app.zoom;
    }

    public Matrix4 projection() {
        return uiEngineState.camera_app.combined;
    }

    public float viewPortStretchFactorWidth() {
        return uiEngineState.viewport_screen.getWorldWidth() / (float) uiEngineState.viewport_screen.getScreenWidth();
    }

    public float viewPortStretchFactorHeight() {
        return uiEngineState.viewport_screen.getWorldHeight() / (float) uiEngineState.viewport_screen.getScreenHeight();
    }

    public final class APIAppViewports {

        APIAppViewports() {
        }

        public int activeSize() {
            return uiEngineState.appViewPorts.size();
        }

        public AppViewport get(int index) {
            return uiEngineState.appViewPorts.get(index);
        }

        private ArrayList<AppViewport> getAll() {
            return new ArrayList<>(uiEngineState.appViewPorts);
        }

        public boolean pointVisible(AppViewport appViewPort, float x, float y) {
            return appViewPort.camera.frustum.pointInFrustum(x, y, 0f);
        }

        public boolean pointVisibleAny(float x, float y) {
            for (int i = 0; i < uiEngineState.appViewPorts.size(); i++) {
                if (pointVisible(uiEngineState.appViewPorts.get(i), x, y)) return true;
            }
            return false;
        }

        public boolean rectVisible(AppViewport appViewPort, float x, float y, float width, float height) {
            return appViewPort.camera.frustum.boundsInFrustum(x, y, 0f, width, height, 0f);
        }

        public boolean rectVisibleAny(float x, float y, float width, float height) {
            for (int i = 0; i < uiEngineState.appViewPorts.size(); i++) {
                if (rectVisible(uiEngineState.appViewPorts.get(i), x, y, width, height)) return true;
            }
            return false;
        }

        public boolean sphereVisible(AppViewport appViewPort, float x, float y, float radius) {
            return appViewPort.camera.frustum.sphereInFrustum(x, y, 0f, radius);
        }

        public boolean sphereVisibleAny(float x, float y, float width, float radius) {
            for (int i = 0; i < uiEngineState.appViewPorts.size(); i++) {
                if (sphereVisible(uiEngineState.appViewPorts.get(i), x, y, radius)) return true;
            }
            return false;
        }

    }

}

