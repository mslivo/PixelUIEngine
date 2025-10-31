package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.pixelui.rendering.NestedFrameBuffer;
import net.mslivo.pixelui.engine.actions.AppViewPortAction;

public final class AppViewport extends Component {
    public OrthographicCamera camera;
    public NestedFrameBuffer frameBuffer;
    public TextureRegion textureRegion;
    public AppViewPortAction appViewPortAction;
    public long updateTimer;
    public int updateTime;

    AppViewport() {
    }
}
