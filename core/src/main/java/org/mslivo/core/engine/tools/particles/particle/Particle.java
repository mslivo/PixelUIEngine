package org.mslivo.core.engine.tools.particles.particle;

import org.mslivo.core.engine.media_manager.media.*;
import org.mslivo.core.engine.tools.particles.ParticleSystem;

public class Particle {
    public ParticleType type;
    public float x, y;
    public float r, g, b, a;
    public float rotation, scaleX, scaleY;
    public int array_index;
    public float origin_x, origin_y;
    public CMediaGFX appearance;

    public CMediaFont font;

    public String text;

    public Particle() {

    }

    public Particle(ParticleType type, float x, float y, float r, float g, float b, float a, float rotation, float scaleX, float scaleY, int array_index, float origin_x, float origin_y, CMediaGFX appearance, CMediaFont font, String text, float animation_offset, boolean visible, Object[] customData) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.rotation = rotation;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.array_index = array_index;
        this.origin_x = origin_x;
        this.origin_y = origin_y;
        this.appearance = appearance;
        this.font = font;
        this.text = text;
        this.animation_offset = animation_offset;
        this.visible = visible;
        this.customData = customData;
    }

    public float animation_offset;
    public boolean visible;
    public Object customData;

    /*
    public Particle(CMediaFont font, String text, float x, float y) {
        this(font, text, x, y, 1, 1, 1, 1, 0, 0, null);
    }

    public Particle(CMediaFont font, String text, float x, float y, float r, float g, float b, float a) {
        this(font, text, x, y, r, g, b, a, 0, 0, null);
    }

    public Particle(CMediaFont font, String text, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, Object[] customData) {
        initValues(x, y, r, g, b, a, origin_x, origin_y, rotation, scaleX, scaleY, customData);
        this.type = ParticleType.TEXT;
        this.font = font;
        this.text = text;
    }

    public Particle(CMediaImage image, float x, float y) {
        this(image, x, y, 1, 1, 1, 1, 0, 0, 0, 1, 1, null);
    }

    public Particle(CMediaImage image, float x, float y, float r, float g, float b, float a) {
        this(image, x, y, r, g, b, a, 0, 0, 0, 1, 1, null);
    }



    public Particle(CMediaImage image, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY, Object[] customData) {
        initValues(x, y, r, g, b, a, origin_x, origin_y, rotation, scaleX, scaleY, customData);
        this.type = ParticleType.IMAGE;
        this.appearance = image;
    }


    public Particle(CMediaAnimation animation, float animation_offset, float x, float y) {
        this(animation, animation_offset, x, y, 1, 1, 1, 1, 0, 0, 0, 1, 1, null);
    }

    public Particle(CMediaAnimation animation, float animation_offset, float x, float y, float r, float g, float b, float a) {
        this(animation, animation_offset, x, y, r, g, b, a, 0, 0, 0, 1, 1, null);
    }

    public Particle(CMediaAnimation animation, float animation_offset, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY, Object[] customData) {
        initValues(x, y, r, g, b, a, origin_x, origin_y, rotation, scaleX, scaleY, customData);
        this.type = ParticleType.ANIMATION;
        this.appearance = animation;
        this.animation_offset = animation_offset;
    }

    public Particle(CMediaArray array, int array_index, float x, float y) {
        this(array, array_index, x, y, 1, 1, 1, 1, 0, 0, 0, 1, 1, null);
    }

    public Particle(CMediaArray array, int array_index, float x, float y, float r, float g, float b, float a) {
        this(array, array_index, x, y, r, g, b, a, 0, 0, 0, 1, 1, null);
    }

    public Particle(CMediaArray array, int array_index, float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY, Object[] customData) {
        initValues(x, y, r, g, b, a, origin_x, origin_y, rotation, scaleX, scaleY, customData);
        this.type = ParticleType.ARRAY;
        this.appearance = array;
        this.array_index = array_index;
    }


    private void initValues(float x, float y, float r, float g, float b, float a, float origin_x, float origin_y, float rotation, float scaleX, float scaleY, Object[] customData) {
        this.appearance = null;
        this.font = null;
        this.text = null;
        this.array_index = 0;
        this.animation_offset = 0;
        this.visible = true;
        this.x = x;
        this.y = y;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.rotation = rotation;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.origin_x = origin_x;
        this.origin_y = origin_y;
        this.customData = customData;
    }

     */
}
