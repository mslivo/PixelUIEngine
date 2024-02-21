package org.mslivo.core.engine.ui_engine.ui.components.viewport;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.mslivo.core.engine.ui_engine.ui.actions.GameViewPortAction;
import org.mslivo.core.engine.ui_engine.ui.components.Component;
import org.mslivo.core.engine.ui_engine.render.NestedFrameBuffer;

public class GameViewPort extends Component {
    public OrthographicCamera camera;
    public NestedFrameBuffer frameBuffer;
    public TextureRegion textureRegion;
    public GameViewPortAction gameViewPortAction;
    public long updateTimer;
    public int updateTime;

}
