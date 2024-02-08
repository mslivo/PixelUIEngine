package org.mslivo.core.engine.tools.rendering.pixels;

import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;

public class ImmediatePixelRenderer {

    private ImmediateModeRenderer20 renderer20 = null;

    public ImmediatePixelRenderer(int resolutionWidth,int resolutionHeight){
        renderer20 = new ImmediateModeRenderer20(resolutionWidth*resolutionHeight,false, true,0);
    }



}
