package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.xpenatan.webgpu.WGPUTextureFormat;
import com.monstrous.gdx.webgpu.graphics.utils.WgFrameBuffer;
import com.monstrous.gdx.webgpu.graphics.utils.WgScreenUtils;

public class XWgFrameBuffer extends WgFrameBuffer {

    private TextureRegion textureRegionFlipped;

    public XWgFrameBuffer(int width, int height, boolean hasDepth) {
        super(width, height, hasDepth);
    }

    public XWgFrameBuffer(WGPUTextureFormat format, int width, int height, boolean hasDepth) {
        super(format, width, height, hasDepth);
    }

    public void beginGlClear(){
        this.begin();
        WgScreenUtils.clear(0f,0f,0f,0f);
    }

    public TextureRegion getFlippedTextureRegion(){
        if(this.textureRegionFlipped == null){
            this.textureRegionFlipped = new TextureRegion(this.getColorBufferTexture());
            this.textureRegionFlipped.flip(false,true);
        }
        return this.textureRegionFlipped;
    }
}
