package net.mslivo.pixelui.engine;

import net.mslivo.pixelui.engine.constants.SHAPE_ROTATION;
import net.mslivo.pixelui.engine.constants.SHAPE_TYPE;
import net.mslivo.pixelui.engine.actions.ShapeAction;

public non-sealed class Shape extends Component {
    public SHAPE_TYPE shapeType;
    public SHAPE_ROTATION shapeRotation;
    public ShapeAction shapeAction;

    Shape() {
    }
}
