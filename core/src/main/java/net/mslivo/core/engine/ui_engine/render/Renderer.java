package net.mslivo.core.engine.ui_engine.render;

public class Renderer {
    public final SpriteRenderer sprite;
    public final ImmediateRenderer immediate;
    public final ModelRenderer modelRenderer;

    public Renderer(SpriteRenderer sprite, ImmediateRenderer immediate, ModelRenderer modelRenderer) {
        this.sprite = sprite;
        this.immediate = immediate;
        this.modelRenderer = modelRenderer;
    }

}
