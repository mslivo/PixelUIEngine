package net.mslivo.core.engine.ui_engine.ui.components.framebuffer;

import net.mslivo.core.engine.ui_engine.rendering.NestedFrameBuffer;
import net.mslivo.core.engine.ui_engine.ui.actions.FrameBufferViewportAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

public class FrameBufferViewport extends Component {
    public NestedFrameBuffer frameBuffer;
    public FrameBufferViewportAction frameBufferViewportAction;
}
