package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.TOOLTIP_SEGMENT_ALIGNMENT;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.actions.ToolTipAction;
import net.mslivo.core.engine.ui_engine.ui.tooltip.*;

import java.util.ArrayList;

public final class APITooltip {
    private API api;
    private UIEngineState uiEngineState;
    private MediaManager mediaManager;
    public final APITooltipSegment segment;

    APITooltip(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.segment = new APITooltipSegment();
    }

    private ToolTipAction defaultToolTipAction() {
        return new ToolTipAction() {
        };
    }

    public Tooltip create(TooltipSegment[] segments) {
        return create( segments, defaultToolTipAction(), null, 1, 1);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction) {
        return create( segments, toolTipAction, null, 1, 1);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction, String title) {
        return create( segments, toolTipAction, title, 1, 1);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction, String title, int minWidth, int minHeight) {
        Tooltip tooltip = new Tooltip();
        tooltip.title = title;
        tooltip.segments = new ArrayList<>();
        if (segments != null) {
            for (int i = 0; i < segments.length; i++) {
                if(segments[i].addedToTooltip == null) {
                    tooltip.segments.add(segments[i]);
                    segments[i].addedToTooltip = tooltip;
                }
            }
        }
        tooltip.toolTipAction = toolTipAction;
        tooltip.minWidth = Math.clamp(minWidth, 1, Integer.MAX_VALUE);
        tooltip.minHeight = Math.clamp(minHeight, 1, Integer.MAX_VALUE);
        return tooltip;
    }


    public void addTooltipSegment(Tooltip toolTip, TooltipSegment segment) {
        if (toolTip == null || segment == null) return;
        UICommonUtils.toolTip_addTooltipSegment(toolTip, segment);
    }

    public void removeTooltipSegment(Tooltip toolTip, TooltipSegment segment) {
        if (toolTip == null || segment == null) return;
        UICommonUtils.toolTip_removeTooltipSegment(toolTip, segment);
    }

    public void removeAllTooltipSegment(Tooltip toolTip, TooltipSegment segment) {
        if (toolTip == null || segment == null) return;
        for(int i=0;i<toolTip.segments.size();i++) UICommonUtils.toolTip_removeTooltipSegment(toolTip, toolTip.segments.get(i));
    }

    public void setToolTipAction(Tooltip toolTip, ToolTipAction toolTipAction) {
        if (toolTip == null) return;
        toolTip.toolTipAction = toolTipAction;
    }

    public void setTitle(Tooltip tooltip, String title) {
        if (tooltip == null) return;
        tooltip.title = title;
    }

    public void setSizeMin(Tooltip tooltip, int minWidth, int minHeight) {
        if (tooltip == null) return;
        tooltip.minWidth = Math.clamp(minWidth, 1, Integer.MAX_VALUE);
        tooltip.minHeight = Math.clamp(minHeight, 1, Integer.MAX_VALUE);
    }


    public final class APITooltipSegment {
        public final APITooltipTextSegment text;
        public final APITooltipImageSegment image;

        APITooltipSegment(){
            text = new APITooltipTextSegment();
            image = new APITooltipImageSegment();
        }

        public void setColor(TooltipSegment tooltipSegment, Color color) {
            if (tooltipSegment == null || color == null) return;
            setColor(tooltipSegment, color.r, color.g, color.b, color.a);
        }

        public void setColor(TooltipSegment tooltipSegment, float r, float g, float b, float a) {
            if (tooltipSegment == null) return;
            tooltipSegment.color_r = r;
            tooltipSegment.color_g = g;
            tooltipSegment.color_b = b;
            tooltipSegment.color_a = a;
        }

        public void setFont(TooltipSegment tooltipSegment, CMediaFont font){
            if (tooltipSegment == null) return;
            tooltipSegment.font = font;
        }

        public void setAlignment(TooltipSegment tooltipSegment, TOOLTIP_SEGMENT_ALIGNMENT alignment){
            if(tooltipSegment == null) return;
            tooltipSegment.alignment = alignment;
        }

        public final class APITooltipImageSegment {

            APITooltipImageSegment() {
            }

            public TooltipImageSegment create(CMediaSprite sprite) {
                return create(sprite,TOOLTIP_SEGMENT_ALIGNMENT.LEFT, Color.WHITE);
            }

            public TooltipImageSegment create(CMediaSprite sprite, TOOLTIP_SEGMENT_ALIGNMENT alignment) {
                return create(sprite,alignment, Color.WHITE);
            }

            public TooltipImageSegment create(CMediaSprite sprite, TOOLTIP_SEGMENT_ALIGNMENT alignment, Color color) {
                TooltipImageSegment tooltipImageSegment = new TooltipImageSegment();
                tooltipImageSegment.color_r = color.r;
                tooltipImageSegment.color_g = color.g;
                tooltipImageSegment.color_b = color.b;
                tooltipImageSegment.color_a = color.a;
                tooltipImageSegment.alignment = alignment;
                tooltipImageSegment.font = uiEngineState.uiEngineConfig.tooltip_defaultFont;
                tooltipImageSegment.image = sprite;
                if (sprite != null) {
                    tooltipImageSegment.width = MathUtils.ceil(mediaManager.getCMediaSpriteWidth(sprite) / UIEngine.TILE_SIZE_F);
                    tooltipImageSegment.height = MathUtils.ceil(mediaManager.getCMediaSpriteHeight(sprite) / UIEngine.TILE_SIZE_F);
                }else{
                    tooltipImageSegment.width = tooltipImageSegment.height = 0;
                }
                return tooltipImageSegment;
            }

            public void setImage(TooltipImageSegment tooltipImageSegment, CMediaSprite image) {
                if (tooltipImageSegment == null) return;
                UICommonUtils.toolTip_setImageSegmentImage(mediaManager,tooltipImageSegment, image);
            }
        }

        public final class APITooltipTextSegment {
            APITooltipTextSegment() {
            }

            public TooltipTextSegment create(String text) {
                return create(text, TOOLTIP_SEGMENT_ALIGNMENT.LEFT, Color.WHITE);
            }

            public TooltipTextSegment create(String text, TOOLTIP_SEGMENT_ALIGNMENT alignment) {
                return create(text, alignment, Color.WHITE);
            }

            public TooltipTextSegment create(String text, TOOLTIP_SEGMENT_ALIGNMENT alignment, Color color) {
                TooltipTextSegment tooltipTextSegment = new TooltipTextSegment();
                tooltipTextSegment.text = Tools.Text.validString(text);
                tooltipTextSegment.color_r = color.r;
                tooltipTextSegment.color_g = color.g;
                tooltipTextSegment.color_b = color.b;
                tooltipTextSegment.color_a = color.a;
                tooltipTextSegment.alignment = alignment;
                tooltipTextSegment.font = uiEngineState.uiEngineConfig.tooltip_defaultFont;
                if (!tooltipTextSegment.text.isEmpty()) {
                    tooltipTextSegment.width = MathUtils.ceil(mediaManager.getCMediaFontTextWidth(tooltipTextSegment.font, tooltipTextSegment.text) / UIEngine.TILE_SIZE_F);
                    tooltipTextSegment.height = 1;
                }else{
                    tooltipTextSegment.width = tooltipTextSegment.height = 0;
                }
                return tooltipTextSegment;
            }

            public void setText(TooltipTextSegment tooltipTextSegment, String text) {
                if (tooltipTextSegment == null) return;
                UICommonUtils.toolTip_setTextSegmentText(mediaManager, tooltipTextSegment, text);
            }

        }

    }




}



