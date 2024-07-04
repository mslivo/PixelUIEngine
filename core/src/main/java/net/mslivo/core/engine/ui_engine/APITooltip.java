package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.media_manager.CMediaFont;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.DIRECTION;
import net.mslivo.core.engine.ui_engine.constants.SEGMENT_ALIGNMENT;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
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
        return create(segments, defaultToolTipAction(), 0, Color.BLACK, Color.BLACK, 2, DIRECTION.RIGHT);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction) {
        return create(segments, toolTipAction, 0, Color.BLACK, Color.BLACK, 2, DIRECTION.RIGHT);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction, int minWidth) {
        return create(segments, toolTipAction, minWidth, Color.BLACK, Color.BLACK, 2, DIRECTION.RIGHT);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction, int minWidth, Color borderColor) {
        return create(segments, toolTipAction, minWidth, borderColor, Color.BLACK, 2, DIRECTION.RIGHT);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction, int minWidth, Color borderColor, Color lineColor) {
        return create(segments, toolTipAction, minWidth, borderColor, lineColor, 2, DIRECTION.RIGHT);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction, int minWidth, Color borderColor, Color lineColor, int lineLength) {
        return create(segments, toolTipAction, minWidth, borderColor, lineColor, lineLength, DIRECTION.RIGHT);
    }

    public Tooltip create(TooltipSegment[] segments, ToolTipAction toolTipAction, int minWidth, Color borderColor, Color lineColor, int lineLength, DIRECTION direction) {
        Tooltip tooltip = new Tooltip();
        tooltip.segments = new ArrayList<>();
        tooltip.color_border = new Color(borderColor);
        tooltip.color_line = new Color(lineColor);
        tooltip.lineLength = Math.max(lineLength,0);
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
        tooltip.direction = direction != null ? direction : DIRECTION.RIGHT;
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
            tooltipSegment.color.set(color);
        }

        public void setAlignment(TooltipSegment tooltipSegment, SEGMENT_ALIGNMENT alignment) {
            if (tooltipSegment == null) return;
            tooltipSegment.alignment = alignment;
        }

        public void setBorder(TooltipSegment tooltipSegment, boolean border) {
            if (tooltipSegment == null) return;
            tooltipSegment.border = border;
        }

        private void setSegmentValues(TooltipSegment tooltipSegment, Color color, SEGMENT_ALIGNMENT alignment, int width, int height, boolean merge, boolean border, boolean clear) {
            tooltipSegment.addedToTooltip = null;
            tooltipSegment.color = new Color(color);
            tooltipSegment.alignment = alignment;
            tooltipSegment.width = width;
            tooltipSegment.height = height;
            tooltipSegment.border = border;
            tooltipSegment.merge = merge;
            tooltipSegment.clear = clear;
        }


        public final class APITooltipImageSegment {

            APITooltipImageSegment() {
            }

            public TooltipImageSegment create(CMediaSprite sprite) {
                return create(sprite, 0, Color.WHITE, SEGMENT_ALIGNMENT.LEFT, false, false, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex) {
                return create(sprite, arrayIndex, Color.WHITE, SEGMENT_ALIGNMENT.LEFT, false, false, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, Color color) {
                return create(sprite, arrayIndex, color, SEGMENT_ALIGNMENT.LEFT, false, false, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, Color color, SEGMENT_ALIGNMENT alignment) {
                return create(sprite, arrayIndex, color, alignment, false, false, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, Color color, SEGMENT_ALIGNMENT alignment, boolean merge) {
                return create(sprite, arrayIndex, color, alignment, merge, false, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, Color color, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border) {
                return create(sprite, arrayIndex, color, alignment, merge, border, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, Color color, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border, boolean clear) {
                TooltipImageSegment tooltipImageSegment = new TooltipImageSegment();

                int width = 0, height = 0;
                if (sprite != null) {
                    width = MathUtils.round((mediaManager.getCMediaSpriteWidth(sprite) + api.TS()) / api.TSF());
                    height = MathUtils.round((mediaManager.getCMediaSpriteHeight(sprite) + api.TS()) / api.TSF());
                }

                setSegmentValues(tooltipImageSegment, color, alignment, width, height, merge, border, clear);
                tooltipImageSegment.image = sprite;
                tooltipImageSegment.arrayIndex = Math.max(arrayIndex, 0);
                return tooltipImageSegment;
            }

            public void setImage(TooltipImageSegment tooltipImageSegment, CMediaSprite image) {
                if (tooltipImageSegment == null) return;
                UICommonUtils.toolTip_setImageSegmentImage(uiEngineState, mediaManager, tooltipImageSegment, image);
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
                return create(text, Color.WHITE, SEGMENT_ALIGNMENT.LEFT, false, false, false);
            }

            public TooltipTextSegment create(String text, Color color) {
                return create(text, color, SEGMENT_ALIGNMENT.LEFT, false, false, false);
            }

            public TooltipTextSegment create(String text, Color color, SEGMENT_ALIGNMENT alignment) {
                return create(text, color, alignment, false, false, false);
            }

            public TooltipTextSegment create(String text, Color color, SEGMENT_ALIGNMENT alignment, boolean merge) {
                return create(text, color, alignment, merge, false, false);
            }

            public TooltipTextSegment create(String text, Color color, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border) {
                return create(text, color, alignment, merge, border, false);
            }

            public TooltipTextSegment create(String text, Color color, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border, boolean clear) {
                TooltipTextSegment tooltipTextSegment = new TooltipTextSegment();
                CMediaFont segmentFont = uiEngineState.config.tooltip_defaultFont;
                String segmentText = Tools.Text.validString(text);
                int width = MathUtils.round((mediaManager.getCMediaFontTextWidth(segmentFont, segmentText) + api.TS()) / api.TSF());
                int height = 1;
                setSegmentValues(tooltipTextSegment, color, alignment, width, height, merge, border, clear);
                tooltipTextSegment.font = segmentFont;
                tooltipTextSegment.text = segmentText;
                return tooltipTextSegment;
            }

            public void setText(TooltipTextSegment tooltipTextSegment, String text) {
                if (tooltipTextSegment == null) return;
                UICommonUtils.toolTip_setTextSegmentText(uiEngineState, mediaManager, tooltipTextSegment, text);
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
                return create(Color.WHITE, false, false, false);
            }

            public TooltipEmptySegment create(Color color) {
                return create(color, false, false, false);
            }

            public TooltipEmptySegment create(Color color, boolean merge) {
                return create(color, merge, false, false);
            }

            public TooltipEmptySegment create(Color color, boolean merge, boolean border) {
                return create(color, merge, border, false);
            }

            public TooltipEmptySegment create(Color color, boolean merge, boolean border, boolean clear) {
                TooltipEmptySegment tooltipEmptySegment = new TooltipEmptySegment();
                setSegmentValues(tooltipEmptySegment, color, SEGMENT_ALIGNMENT.LEFT, 0, 0, merge, border, clear);
                return tooltipEmptySegment;
            }

        }

    }


}



