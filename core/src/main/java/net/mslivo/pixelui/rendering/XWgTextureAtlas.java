package net.mslivo.pixelui.rendering;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.monstrous.gdx.webgpu.graphics.WgTexture;

public class XWgTextureAtlas extends TextureAtlas {

    public XWgTextureAtlas() {
        super();
    }

    public XWgTextureAtlas(FileHandle packFile) {
        super(packFile, packFile.parent());
    }

    /** Adds the textures and regions from the specified texture atlas data. */
    @Override
    public void load(TextureAtlasData data) {
        for (TextureAtlasData.Page page : data.getPages()) {
            if (page.texture == null)
                page.texture = new WgTexture(page.textureFile, page.format, page.useMipMaps);
        }
        super.load(data);
    }

}
