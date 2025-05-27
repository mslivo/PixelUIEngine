package net.mslivo.core.engine.ui_engine.ui.components.shape;

import net.mslivo.core.engine.ui_engine.constants.SHAPE_ROTATION;
import net.mslivo.core.engine.ui_engine.constants.SHAPE_TYPE;
import net.mslivo.core.engine.ui_engine.ui.actions.ShapeAction;
import net.mslivo.core.engine.ui_engine.ui.components.Component;

public class Shape extends Component {
    public SHAPE_TYPE shapeType;
    public SHAPE_ROTATION shapeRotation;
    public ShapeAction shapeAction;
}
