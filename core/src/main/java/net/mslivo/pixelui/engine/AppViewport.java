package net.mslivo.pixelui.engine;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.monstrous.gdx.webgpu.graphics.utils.WgFrameBuffer;
import net.mslivo.pixelui.engine.actions.AppViewPortAction;
import net.mslivo.pixelui.rendering.XWgFrameBuffer;

public final class AppViewport extends Component {
    public OrthographicCamera camera;
    public XWgFrameBuffer frameBuffer;
    public TextureRegion textureRegion;
    public AppViewPortAction appViewPortAction;
    public long updateTimer;
    public int updateTime;

    AppViewport() {
    }
}
