package net.mslivo.pixelui.engine;

import com.monstrous.gdx.webgpu.graphics.utils.WgFrameBuffer;
import net.mslivo.pixelui.engine.actions.FrameBufferViewportAction;

public final class FrameBufferViewport extends Component {
    public WgFrameBuffer frameBuffer;
    public FrameBufferViewportAction frameBufferViewportAction;
    public boolean flipX, flipY,stretchToSize;

    FrameBufferViewport() {
    }
}
