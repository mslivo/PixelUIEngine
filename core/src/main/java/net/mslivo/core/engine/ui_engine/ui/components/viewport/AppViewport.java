package net.mslivo.core.engine.ui_engine.ui.components.viewport;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.mslivo.core.engine.ui_engine.rendering.NestedFrameBuffer;
import net.mslivo.core.engine.ui_engine.ui.actions.AppViewPortAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

public class AppViewport extends Component {
    public OrthographicCamera camera;
    public NestedFrameBuffer frameBuffer;
    public TextureRegion textureRegion;
    public AppViewPortAction appViewPortAction;
    public long updateTimer;
    public int updateTime;

}
