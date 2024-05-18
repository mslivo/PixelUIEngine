package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.media_manager.media.CMediaFont;
import net.mslivo.core.engine.media_manager.media.CMediaSprite;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.SEGMENT_ALIGNMENT;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.ui.actions.ToolTipAction;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;
import net.mslivo.core.engine.ui_engine.ui.tooltip.TooltipImageSegment;
import net.mslivo.core.engine.ui_engine.ui.tooltip.TooltipSegment;
import net.mslivo.core.engine.ui_engine.ui.tooltip.TooltipTextSegment;

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
        return create(segments, defaultToolTipAction());
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction) {
        Tooltip tooltip = new Tooltip();
        tooltip.segments = new ArrayList<>();
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


    public final class APITooltipSegment {
        public final APITooltipTextSegment text;
        public final APITooltipImageSegment image;

        APITooltipSegment() {
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

            public TooltipImageSegment create(CMediaSprite sprite) {
                return create(sprite, 0, false, SEGMENT_ALIGNMENT.LEFT, Color.WHITE);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex) {
                return create(sprite, arrayIndex, false, SEGMENT_ALIGNMENT.LEFT, Color.WHITE);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, boolean border) {
                return create(sprite, arrayIndex, border, SEGMENT_ALIGNMENT.LEFT, Color.WHITE);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, boolean border, SEGMENT_ALIGNMENT alignment) {
                return create(sprite, arrayIndex, border, alignment, Color.WHITE);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, boolean border, SEGMENT_ALIGNMENT alignment, Color color) {
                TooltipImageSegment tooltipImageSegment = new TooltipImageSegment();
                tooltipImageSegment.color_r = color.r;
                tooltipImageSegment.color_g = color.g;
                tooltipImageSegment.color_b = color.b;
                tooltipImageSegment.color_a = color.a;
                tooltipImageSegment.alignment = alignment;
                tooltipImageSegment.image = sprite;
                tooltipImageSegment.arrayIndex = Math.clamp(arrayIndex, 0, Integer.MAX_VALUE);
                tooltipImageSegment.border = border;
                if (sprite != null) {
                    tooltipImageSegment.width = MathUtils.round((mediaManager.getCMediaSpriteWidth(sprite) + UIEngine.TILE_SIZE) / UIEngine.TILE_SIZE_F);
                    tooltipImageSegment.height = MathUtils.round((mediaManager.getCMediaSpriteHeight(sprite) + UIEngine.TILE_SIZE) / UIEngine.TILE_SIZE_F);
                } else {
                    tooltipImageSegment.width = tooltipImageSegment.height = 0;
                }
                return tooltipImageSegment;
            }

            public void setImage(TooltipImageSegment tooltipImageSegment, CMediaSprite image) {
                if (tooltipImageSegment == null) return;
                UICommonUtils.toolTip_setImageSegmentImage(mediaManager, tooltipImageSegment, image);
            }

            public void setArrayIndex(TooltipImageSegment tooltipImageSegment, int arrayIndex) {
                if (tooltipImageSegment == null) return;
                tooltipImageSegment.arrayIndex = Math.clamp(arrayIndex, 0, Integer.MAX_VALUE);
            }


        }

        public final class APITooltipTextSegment {
            APITooltipTextSegment() {
            }

            public TooltipTextSegment create(String text) {
                return create(text, false, SEGMENT_ALIGNMENT.LEFT, Color.WHITE);
            }

            public TooltipTextSegment create(String text, boolean border) {
                return create(text, border, SEGMENT_ALIGNMENT.LEFT, Color.WHITE);
            }

            public TooltipTextSegment create(String text, boolean border, SEGMENT_ALIGNMENT alignment) {
                return create(text, border, alignment, Color.WHITE);
            }

            public TooltipTextSegment create(String text, boolean border, SEGMENT_ALIGNMENT alignment, Color color) {
                TooltipTextSegment tooltipTextSegment = new TooltipTextSegment();
                tooltipTextSegment.text = Tools.Text.validString(text);
                tooltipTextSegment.color_r = color.r;
                tooltipTextSegment.color_g = color.g;
                tooltipTextSegment.color_b = color.b;
                tooltipTextSegment.color_a = color.a;
                tooltipTextSegment.border = border;
                tooltipTextSegment.alignment = alignment;
                tooltipTextSegment.font = uiEngineState.uiEngineConfig.tooltip_defaultFont;
                if (!tooltipTextSegment.text.isEmpty()) {
                    tooltipTextSegment.width = MathUtils.round((mediaManager.getCMediaFontTextWidth(tooltipTextSegment.font, tooltipTextSegment.text) + UIEngine.TILE_SIZE) / UIEngine.TILE_SIZE_F);
                    tooltipTextSegment.height = 1;
                } else {
                    tooltipTextSegment.width = tooltipTextSegment.height = 0;
                }
                return tooltipTextSegment;
            }

            public void setText(TooltipTextSegment tooltipTextSegment, String text) {
                if (tooltipTextSegment == null) return;
                UICommonUtils.toolTip_setTextSegmentText(mediaManager, tooltipTextSegment, text);
            }

            public void setFont(TooltipTextSegment tooltipTextSegment, CMediaFont font) {
                if (tooltipTextSegment == null) return;
                tooltipTextSegment.font = font;
            }

        }

    }


}



