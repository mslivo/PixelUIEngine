package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.actions.ToolTipAction;
import net.mslivo.core.engine.ui_engine.ui.tooltip.TooltipImageSegment;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;
import net.mslivo.core.engine.ui_engine.ui.tooltip.TooltipImage;

import java.util.ArrayList;

public final class APITooltip {
    private API api;
    private UIEngineState uiEngineState;
    private MediaManager mediaManager;
    public final APITooltipImage toolTipImage;
    public final APITooltipTextSegment textSegment;
    public final APITooltipImageSegment imageSegment;

    APITooltip(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.toolTipImage = new APITooltipImage();
        this.textSegment = new APITooltipTextSegment();
        this.imageSegment = new APITooltipImageSegment();
    }

    private ToolTipAction defaultToolTipAction() {
        return new ToolTipAction() {
        };
    }

    public Tooltip create(String[] lines) {
        return create(lines, null, defaultToolTipAction(), true, 1, 1);
    }

    public Tooltip create(String[] lines, TooltipImage[] images) {
        return create(lines, images, defaultToolTipAction(), true, 1, 1);
    }

    public Tooltip create(String[] lines, TooltipImage[] images, ToolTipAction toolTipAction) {
        return create(lines, images, toolTipAction, true, 1, 1);
    }

    public Tooltip create(String[] lines, TooltipImage[] images, ToolTipAction toolTipAction, boolean displayFistLineAsTitle) {
        return create(lines, images, toolTipAction, displayFistLineAsTitle, 1, 1);
    }

    public Tooltip create(String[] lines, TooltipImage[] images, ToolTipAction toolTipAction, boolean displayFistLineAsTitle, int minWidth, int minHeight) {
        Tooltip tooltip = new Tooltip();
        tooltip.lines = Tools.Text.validStringArray(lines);
        tooltip.images = new ArrayList<>();
        if (images != null) {
            for (int i = 0; i < images.length; i++) {
                tooltip.images.add(images[0]);
                images[i].addedToToolTip = tooltip;
            }
        }
        tooltip.toolTipAction = toolTipAction;
        tooltip.displayFistLineAsTitle = displayFistLineAsTitle;
        tooltip.minWidth = Math.clamp(minWidth, 1, Integer.MAX_VALUE);
        tooltip.minHeight = Math.clamp(minHeight, 1, Integer.MAX_VALUE);
        tooltip.font = uiEngineState.uiEngineConfig.tooltip_defaultFont;
        tooltip.color_r = uiEngineState.uiEngineConfig.component_defaultColor.r;
        tooltip.color_g = uiEngineState.uiEngineConfig.component_defaultColor.g;
        tooltip.color_b = uiEngineState.uiEngineConfig.component_defaultColor.b;
        tooltip.color_a = uiEngineState.uiEngineConfig.component_defaultColor.a;
        return tooltip;
    }

    public void addToolTipImage(Tooltip toolTip, TooltipImage toolTipImage) {
        if (toolTip == null || toolTipImage == null) return;
        UICommonUtils.toolTip_addToolTipImage(toolTip, toolTipImage);
    }

    public void addToolTipImages(Tooltip toolTip, TooltipImage[] toolTipImages) {
        if (toolTip == null || toolTipImages == null) return;
        for (int i = 0; i < toolTipImages.length; i++) addToolTipImage(toolTip, toolTipImages[i]);
    }

    public void removeToolTipImage(Tooltip toolTip, TooltipImage toolTipImage) {
        if (toolTip == null || toolTipImage == null) return;
        UICommonUtils.toolTip_removeToolTipImage(toolTip, toolTipImage);
    }

    public void removeToolTipImages(Tooltip toolTip, TooltipImage[] toolTipImages) {
        if (toolTip == null || toolTipImages == null) return;
        for (int i = 0; i < toolTipImages.length; i++) removeToolTipImage(toolTip, toolTipImages[i]);
    }

    public void removeAllToolTipImages(Tooltip toolTip) {
        if (toolTip == null) return;
        removeToolTipImages(toolTip, toolTip.images.toArray(new TooltipImage[]{}));
    }

    public void setToolTipAction(Tooltip toolTip, ToolTipAction toolTipAction) {
        if (toolTip == null) return;
        toolTip.toolTipAction = toolTipAction;
    }

    public void setDisplayFistLineAsTitle(Tooltip tooltip, boolean firstLineIsTitle) {
        if (tooltip == null) return;
        tooltip.displayFistLineAsTitle = firstLineIsTitle;
    }

    public void setLines(Tooltip tooltip, String[] lines) {
        if (tooltip == null) return;
        UICommonUtils.tooltip_setLines(tooltip, lines);
    }

    public void setSizeMin(Tooltip tooltip, int minWidth, int minHeight) {
        if (tooltip == null) return;
        tooltip.minWidth = Math.clamp(minWidth, 1, Integer.MAX_VALUE);
        tooltip.minHeight = Math.clamp(minHeight, 1, Integer.MAX_VALUE);
    }

    public void setColor(Tooltip tooltip, float r, float g, float b, float a) {
        if (tooltip == null) return;
        tooltip.color_r = r;
        tooltip.color_g = g;
        tooltip.color_b = b;
        tooltip.color_a = a;
    }

    public void setColor(Tooltip tooltip, Color color) {
        if (tooltip == null || color == null) return;
        setColor(tooltip, color.r, color.g, color.b, color.a);
    }

    public void setFont(Tooltip tooltip, CMediaFont font) {
        if (tooltip == null) return;
        tooltip.font = font;
    }

    public final class APITooltipTextSegment {
        APITooltipTextSegment() {
        }

    }

    public final class APITooltipImageSegment {

        APITooltipImageSegment(){
        }

        public TooltipImageSegment create(CMediaSprite sprite) {
            return create(sprite, Color.WHITE);
        }

        public TooltipImageSegment create(CMediaSprite sprite, Color color) {
            TooltipImageSegment toolTipImageSegment = new TooltipImageSegment();
            toolTipImageSegment.sprite = sprite;
            toolTipImageSegment.color = new Color(color);
            if (sprite != null) {
                toolTipImageSegment.width = MathUtils.ceil(mediaManager.getCMediaSpriteWidth(sprite));

            }
            return toolTipImageSegment;
        }

    }

    public final class APITooltipImage {

        APITooltipImage(){
        }

        public TooltipImage create(CMediaSprite image) {
            return create(image, 0, 0);
        }

        public TooltipImage create(CMediaSprite image, int x, int y) {
            TooltipImage toolTipImage = new TooltipImage();
            toolTipImage.image = image;
            toolTipImage.x = x;
            toolTipImage.y = y;
            toolTipImage.color_r = Color.WHITE.r;
            toolTipImage.color_g = Color.WHITE.g;
            toolTipImage.color_b = Color.WHITE.b;
            toolTipImage.color_a = Color.WHITE.a;
            return toolTipImage;
        }

        public void setImage(TooltipImage toolTipImage, CMediaSprite image) {
            if (toolTipImage == null) return;
            toolTipImage.image = image;
        }

        public void setPosition(TooltipImage toolTipImage, int x, int y) {
            if (toolTipImage == null) return;
            toolTipImage.x = x;
            toolTipImage.y = y;
        }

        public void setColor(TooltipImage toolTipImage, float r, float g, float b, float a) {
            if (toolTipImage == null) return;
            toolTipImage.color_r = r;
            toolTipImage.color_g = g;
            toolTipImage.color_b = b;
            toolTipImage.color_a = a;
        }

        public void setColor(TooltipImage toolTipImage, Color color) {
            if (toolTipImage == null) return;
            setColor(toolTipImage, color.r, color.g, color.b, color.a);
        }

    }


}



