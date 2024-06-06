package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.SEGMENT_ALIGNMENT;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.actions.ToolTipAction;
import net.mslivo.core.engine.ui_engine.ui.tooltip.*;

import java.util.ArrayList;

public final class APITooltip {
    private final API api;
    private final UIEngineState uiEngineState;
    private final MediaManager mediaManager;
    private final UIConfig uiConfig;
    public final APITooltipSegment segment;

    APITooltip(API api, UIEngineState uiEngineState, MediaManager mediaManager) {
        this.api = api;
        this.uiEngineState = uiEngineState;
        this.mediaManager = mediaManager;
        this.uiConfig = uiEngineState.config;
        this.segment = new APITooltipSegment();
    }

    private ToolTipAction defaultToolTipAction() {
        return new ToolTipAction() {
        };
    }

    public Tooltip create(TooltipSegment[] segments) {
        return create(segments, defaultToolTipAction(), 0,
                uiConfig.tooltip_defaultBorderColor.r, uiConfig.tooltip_defaultBorderColor.g, uiConfig.tooltip_defaultBorderColor.b, uiConfig.tooltip_defaultBorderColor.a);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction) {
        return create(segments, toolTipAction, 0,
                uiConfig.tooltip_defaultBorderColor.r, uiConfig.tooltip_defaultBorderColor.g, uiConfig.tooltip_defaultBorderColor.b, uiConfig.tooltip_defaultBorderColor.a);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction, int minWidth) {
        return create(segments, toolTipAction, minWidth,
                uiConfig.tooltip_defaultBorderColor.r, uiConfig.tooltip_defaultBorderColor.g, uiConfig.tooltip_defaultBorderColor.b, uiConfig.tooltip_defaultBorderColor.a);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction, int minWidth, Color color) {
        return create(segments, toolTipAction, minWidth, color.r, color.g, color.b, color.a);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction, int minWidth, float borderColor_r, float borderColor_g, float borderColor_b, float borderColor_a) {
        Tooltip tooltip = new Tooltip();
        tooltip.segments = new ArrayList<>();
        tooltip.color_border_r = borderColor_r;
        tooltip.color_border_g = borderColor_g;
        tooltip.color_border_b = borderColor_b;
        tooltip.color_border_a = borderColor_a;
        tooltip.minWidth = Math.max(minWidth, 0);
        if (segments != null) {
            for (int i = 0; i < segments.length; i++) {
                if (segments[i].addedToTooltip == null) {
                    tooltip.segments.add(segments[i]);
                    segments[i].addedToTooltip = tooltip;
                }
            }
        }
        tooltip.toolTipAction = toolTipAction;
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
        for (int i = 0; i < toolTip.segments.size(); i++)
            UICommonUtils.toolTip_removeTooltipSegment(toolTip, toolTip.segments.get(i));
    }

    public void setToolTipAction(Tooltip toolTip, ToolTipAction toolTipAction) {
        if (toolTip == null) return;
        toolTip.toolTipAction = toolTipAction;
    }

    public void setMinWidth(Tooltip tooltip, int minWidth) {
        tooltip.minWidth = Math.max(minWidth, 0);
    }

    public final class APITooltipSegment {
        public final APITooltipTextSegment text;
        public final APITooltipImageSegment image;
        public final APITooltipEmptySegment empty;

        APITooltipSegment() {
            text = new APITooltipTextSegment();
            image = new APITooltipImageSegment();
            empty = new APITooltipEmptySegment();
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

        public void setAlignment(TooltipSegment tooltipSegment, SEGMENT_ALIGNMENT alignment) {
            if (tooltipSegment == null) return;
            tooltipSegment.alignment = alignment;
        }

        public void setBorder(TooltipSegment tooltipSegment, boolean border) {
            if (tooltipSegment == null) return;
            tooltipSegment.border = border;
        }

        public final class APITooltipImageSegment {

            APITooltipImageSegment() {
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex) {
                return create(sprite, arrayIndex, SEGMENT_ALIGNMENT.LEFT, false, false, 1f, 1f, 1f, 1f);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, SEGMENT_ALIGNMENT alignment) {
                return create(sprite, arrayIndex, alignment, false, false, 1f, 1f, 1f, 1f);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, SEGMENT_ALIGNMENT alignment, boolean merge) {
                return create(sprite, arrayIndex, alignment, merge, false, 1f, 1f, 1f, 1f);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border) {
                return create(sprite, arrayIndex, alignment, merge, border, 1f, 1f, 1f, 1f);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border, Color color) {
                return create(sprite, arrayIndex, alignment, merge, border, color.r, color.g, color.b, color.a);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border, float r, float g, float b, float a) {
                TooltipImageSegment tooltipImageSegment = new TooltipImageSegment();
                tooltipImageSegment.color_r = r;
                tooltipImageSegment.color_g = g;
                tooltipImageSegment.color_b = b;
                tooltipImageSegment.color_a = a;
                tooltipImageSegment.alignment = alignment;
                tooltipImageSegment.image = sprite;
                tooltipImageSegment.merge = merge;
                tooltipImageSegment.arrayIndex = Math.max(arrayIndex, 0);
                tooltipImageSegment.border = border;
                if (sprite != null) {
                    tooltipImageSegment.width = MathUtils.round((mediaManager.getCMediaSpriteWidth(sprite) + api.TS()) / api.TSF());
                    tooltipImageSegment.height = MathUtils.round((mediaManager.getCMediaSpriteHeight(sprite) + api.TS()) / api.TSF());
                } else {
                    tooltipImageSegment.width = tooltipImageSegment.height = 0;
                }
                return tooltipImageSegment;
            }

            public void setImage(TooltipImageSegment tooltipImageSegment, CMediaSprite image) {
                if (tooltipImageSegment == null) return;
                UICommonUtils.toolTip_setImageSegmentImage(uiEngineState,mediaManager, tooltipImageSegment, image);
            }

            public void setArrayIndex(TooltipImageSegment tooltipImageSegment, int arrayIndex) {
                if (tooltipImageSegment == null) return;
                tooltipImageSegment.arrayIndex = Math.max(arrayIndex, 0);
            }


        }

        public final class APITooltipTextSegment {
            APITooltipTextSegment() {
            }

            public TooltipTextSegment create(String text) {
                return create(text, SEGMENT_ALIGNMENT.LEFT, false, false, 1f, 1f, 1f, 1f);
            }

            public TooltipTextSegment create(String text, SEGMENT_ALIGNMENT alignment) {
                return create(text, alignment, false, false, 1f, 1f, 1f, 1f);
            }

            public TooltipTextSegment create(String text, SEGMENT_ALIGNMENT alignment, boolean merge) {
                return create(text, alignment, merge, false, 1f, 1f, 1f, 1f);
            }

            public TooltipTextSegment create(String text, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border) {
                return create(text, alignment, merge, border, 1f, 1f, 1f, 1f);
            }

            public TooltipTextSegment create(String text, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border, Color color) {
                return create(text, alignment, merge, border, color.r, color.g, color.b, color.a);
            }

            public TooltipTextSegment create(String text, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border, float r, float g, float b, float a) {
                TooltipTextSegment tooltipTextSegment = new TooltipTextSegment();
                tooltipTextSegment.text = Tools.Text.validString(text);
                tooltipTextSegment.color_r = r;
                tooltipTextSegment.color_g = g;
                tooltipTextSegment.color_b = b;
                tooltipTextSegment.color_a = a;
                tooltipTextSegment.border = border;
                tooltipTextSegment.alignment = alignment;
                tooltipTextSegment.merge = merge;
                tooltipTextSegment.font = uiEngineState.config.tooltip_defaultFont;
                tooltipTextSegment.width = MathUtils.round((mediaManager.getCMediaFontTextWidth(tooltipTextSegment.font, tooltipTextSegment.text) + api.TS()) / api.TSF());
                tooltipTextSegment.height = 1;
                return tooltipTextSegment;
            }

            public void setText(TooltipTextSegment tooltipTextSegment, String text) {
                if (tooltipTextSegment == null) return;
                UICommonUtils.toolTip_setTextSegmentText(uiEngineState,mediaManager, tooltipTextSegment, text);
            }

            public void setFont(TooltipTextSegment tooltipTextSegment, CMediaFont font) {
                if (tooltipTextSegment == null) return;
                tooltipTextSegment.font = font;
            }

        }

        public final class APITooltipEmptySegment {

            APITooltipEmptySegment() {
            }

            public TooltipEmptySegment create() {
                return create(false, false, Color.WHITE);
            }

            public TooltipEmptySegment create(boolean merge) {
                return create(merge, false, Color.WHITE);
            }

            public TooltipEmptySegment create(boolean merge, boolean border) {
                return create(merge, border, Color.WHITE);
            }

            public TooltipEmptySegment create(boolean merge, boolean border, Color color) {
                return create(merge, border, color.r, color.g, color.b, color.a);
            }

            public TooltipEmptySegment create(boolean merge, boolean border, float r, float g, float b, float a) {
                TooltipEmptySegment tooltipEmptySegment = new TooltipEmptySegment();
                tooltipEmptySegment.color_r = r;
                tooltipEmptySegment.color_g = g;
                tooltipEmptySegment.color_b = b;
                tooltipEmptySegment.color_a = a;
                tooltipEmptySegment.border = border;
                tooltipEmptySegment.merge = merge;
                return tooltipEmptySegment;
            }

        }

    }


}



