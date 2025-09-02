package net.mslivo.pixelui.engine;

import net.mslivo.pixelui.rendering.NestedFrameBuffer;
import net.mslivo.pixelui.engine.actions.FrameBufferViewportAction;

public class FrameBufferViewport extends Component {
    public NestedFrameBuffer frameBuffer;
    public FrameBufferViewportAction frameBufferViewportAction;

    FrameBufferViewport() {
    }
}
