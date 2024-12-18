package net.mslivo.core.engine.ui_engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import net.mslivo.core.engine.media_manager.CMediaSprite;
import net.mslivo.core.engine.media_manager.MediaManager;
import net.mslivo.core.engine.tools.Tools;
import net.mslivo.core.engine.ui_engine.constants.DIRECTION;
import net.mslivo.core.engine.ui_engine.constants.SEGMENT_ALIGNMENT;
import net.mslivo.core.engine.ui_engine.rendering.ColorMap;
import net.mslivo.core.engine.ui_engine.state.UIEngineState;
import net.mslivo.core.engine.ui_engine.state.config.UIConfig;
import net.mslivo.core.engine.ui_engine.ui.actions.ToolTipAction;
import net.mslivo.core.engine.ui_engine.ui.actions.UpdateAction;
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

    public final ToolTipAction DEFAULT_TOOLTIP_ACTION =  new ToolTipAction() {};

    public Tooltip create(String text) {
        return create(new TooltipSegment[]{
                segment.text.create(text)
        }, null);
    }

    public Tooltip create(TooltipSegment[] segments) {
        return create(segments, DEFAULT_TOOLTIP_ACTION, 0, Color.BLACK, Color.BLACK, 2, DIRECTION.RIGHT);
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
        tooltip.lineLength = Math.max(lineLength, 0);
        tooltip.minWidth = Math.max(minWidth, 0);
        if (segments != null) {
            for (int i = 0; i < segments.length; i++) {
                if (segments[i].addedToTooltip == null) {
                    tooltip.segments.add(segments[i]);
                    segments[i].addedToTooltip = tooltip;
                }
            }
        }
        tooltip.updateActions = new ArrayList<>();
        tooltip.toolTipAction = toolTipAction;
        tooltip.direction = direction != null ? direction : DIRECTION.RIGHT;
        return tooltip;
    }

    public void addUpdateAction(Tooltip tooltip, UpdateAction updateAction){
        if(tooltip == null || updateAction == null)
            return;
        tooltip.updateActions.add(updateAction);
    }

    public void addTooltipSegment(Tooltip toolTip, TooltipSegment segment) {
        if (toolTip == null || segment == null) return;
        UICommonUtils.tooltip_addTooltipSegment(toolTip, segment);
    }

    public void removeTooltipSegment(Tooltip toolTip, TooltipSegment segment) {
        if (toolTip == null || segment == null) return;
        UICommonUtils.tooltip_removeTooltipSegment(toolTip, segment);
    }

    public void removeAllTooltipSegment(Tooltip toolTip, TooltipSegment segment) {
        if (toolTip == null || segment == null) return;
        for (int i = 0; i < toolTip.segments.size(); i++)
            UICommonUtils.tooltip_removeTooltipSegment(toolTip, toolTip.segments.get(i));
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
        public final APITooltipCanvasSegment canvas;

        APITooltipSegment() {
            text = new APITooltipTextSegment();
            image = new APITooltipImageSegment();
            canvas = new APITooltipCanvasSegment();
        }

        public void setColor(TooltipSegment tooltipSegment, Color color) {
            if (tooltipSegment == null || color == null) return;
            tooltipSegment.cellColor.set(color);
        }

        public void setAlignment(TooltipSegment tooltipSegment, SEGMENT_ALIGNMENT alignment) {
            if (tooltipSegment == null) return;
            tooltipSegment.alignment = alignment;
        }

        public void setBorder(TooltipSegment tooltipSegment, boolean border) {
            if (tooltipSegment == null) return;
            tooltipSegment.border = border;
        }

        public void resize(TooltipSegment tooltipSegment, int width, int height) {
            if (tooltipSegment == null) return;
            UICommonUtils.tooltip_resizeSegment(uiEngineState, tooltipSegment, width, height);
        }

        private void setSegmentValues(TooltipSegment tooltipSegment, Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment, int width, int height, boolean merge, boolean border, boolean clear) {
            tooltipSegment.addedToTooltip = null;
            tooltipSegment.cellColor = new Color(cellColor);
            tooltipSegment.contentColor = new Color(contentColor);
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
                return create(sprite, 0, false, false, uiConfig.tooltip_defaultCellColor, Color.GRAY, SEGMENT_ALIGNMENT.LEFT, false, false, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex) {
                return create(sprite, arrayIndex, false, false, uiConfig.tooltip_defaultCellColor, Color.GRAY, SEGMENT_ALIGNMENT.LEFT, false, false, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, boolean flipX, boolean flipY) {
                return create(sprite, arrayIndex, flipX, flipY, uiConfig.tooltip_defaultCellColor, Color.GRAY, SEGMENT_ALIGNMENT.LEFT, false, false, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, boolean flipX, boolean flipY, Color cellColor, Color contentColor) {
                return create(sprite, arrayIndex, flipX, flipY, cellColor, contentColor, SEGMENT_ALIGNMENT.LEFT, false, false, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, boolean flipX, boolean flipY, Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment) {
                return create(sprite, arrayIndex, flipX, flipY, cellColor, contentColor, alignment, false, false, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, boolean flipX, boolean flipY, Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment, boolean merge) {
                return create(sprite, arrayIndex, flipX, flipY, cellColor, contentColor, alignment, merge, false, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, boolean flipX, boolean flipY, Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border) {
                return create(sprite, arrayIndex, flipX, flipY, cellColor, contentColor, alignment, merge, border, false);
            }

            public TooltipImageSegment create(CMediaSprite sprite, int arrayIndex, boolean flipX, boolean flipY, Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border, boolean clear) {
                TooltipImageSegment tooltipImageSegment = new TooltipImageSegment();

                int width = 0, height = 0;
                if (sprite != null) {
                    width = MathUtils.round((mediaManager.spriteWidth(sprite) + api.TS()) / api.TSF());
                    height = MathUtils.round((mediaManager.spriteHeight(sprite) + api.TS()) / api.TSF());
                }

                setSegmentValues(tooltipImageSegment, cellColor, contentColor, alignment, width, height, merge, border, clear);
                tooltipImageSegment.image = sprite;
                tooltipImageSegment.arrayIndex = Math.max(arrayIndex, 0);
                tooltipImageSegment.flipX = flipX;
                tooltipImageSegment.flipY = flipY;
                return tooltipImageSegment;
            }

            public void setImage(TooltipImageSegment tooltipImageSegment, CMediaSprite image) {
                if (tooltipImageSegment == null) return;
                UICommonUtils.tooltip_setImageSegmentImage(uiEngineState, mediaManager, tooltipImageSegment, image);
            }

            public void setArrayIndex(TooltipImageSegment tooltipImageSegment, int arrayIndex) {
                if (tooltipImageSegment == null) return;
                tooltipImageSegment.arrayIndex = Math.max(arrayIndex, 0);
            }

            public void setFlipXY(TooltipImageSegment tooltipImageSegment, boolean flipX, boolean flipY) {
                if (tooltipImageSegment == null) return;
                tooltipImageSegment.flipX = flipX;
                tooltipImageSegment.flipY = flipY;
            }

        }

        public final class APITooltipTextSegment {
            APITooltipTextSegment() {
            }

            public TooltipTextSegment create(String text) {
                return create(text, uiConfig.tooltip_defaultCellColor, uiConfig.ui_font_defaultColor, SEGMENT_ALIGNMENT.LEFT, false, false, false);
            }

            public TooltipTextSegment create(String text, Color cellColor, Color contentColor) {
                return create(text, cellColor, contentColor, SEGMENT_ALIGNMENT.LEFT, false, false, false);
            }

            public TooltipTextSegment create(String text, Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment) {
                return create(text, cellColor, contentColor, alignment, false, false, false);
            }

            public TooltipTextSegment create(String text, Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment, boolean merge) {
                return create(text, cellColor, contentColor, alignment, merge, false, false);
            }

            public TooltipTextSegment create(String text, Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border) {
                return create(text, cellColor, contentColor, alignment, merge, border, false);
            }

            public TooltipTextSegment create(String text, Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment, boolean merge, boolean border, boolean clear) {
                TooltipTextSegment tooltipTextSegment = new TooltipTextSegment();
                String segmentText = Tools.Text.validString(text);
                int width = MathUtils.round((mediaManager.fontTextWidth(uiConfig.ui_font, segmentText) + api.TS()) / api.TSF());
                int height = 1;
                setSegmentValues(tooltipTextSegment, cellColor, contentColor, alignment, width, height, merge, border, clear);
                tooltipTextSegment.text = segmentText;
                return tooltipTextSegment;
            }

            public void setText(TooltipTextSegment tooltipTextSegment, String text) {
                if (tooltipTextSegment == null) return;
                UICommonUtils.tooltip_setTextSegmentText(uiEngineState, mediaManager, tooltipTextSegment, text);
            }

        }

        public final class APITooltipCanvasSegment {

            APITooltipCanvasSegment() {
            }


            public TooltipCanvasSegment create() {
                return create(uiConfig.tooltip_defaultCellColor, Color.GRAY, SEGMENT_ALIGNMENT.LEFT, 1, 1, false, false, false);
            }

            public TooltipCanvasSegment create(SEGMENT_ALIGNMENT alignment) {
                return create(uiConfig.tooltip_defaultCellColor, Color.GRAY, alignment, 1, 1, false, false, false);

            }

            public TooltipCanvasSegment create(Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment) {
                return create(cellColor, contentColor, alignment, 1, 1, false, false, false);
            }

            public TooltipCanvasSegment create(Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment, int width, int height) {
                return create(cellColor, contentColor, alignment, width, height, false, false, false);
            }

            public TooltipCanvasSegment create(Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment, int width, int height, boolean merge) {
                return create(cellColor, contentColor, alignment, width, height, merge, false, false);

            }

            public TooltipCanvasSegment create(Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment, int width, int height, boolean merge, boolean border) {
                return create(cellColor, contentColor, alignment, width, height, merge, border, false);
            }

            public TooltipCanvasSegment create(Color cellColor, Color contentColor, SEGMENT_ALIGNMENT alignment, int width, int height, boolean merge, boolean border, boolean clear) {
                TooltipCanvasSegment tooltipCanvasSegment = new TooltipCanvasSegment();
                setSegmentValues(tooltipCanvasSegment, cellColor, contentColor, alignment, width, height, merge, border, clear);
                tooltipCanvasSegment.colorMap = new ColorMap();
                int widthPx = api.TS(width);
                int heightPx = api.TS(height);
                tooltipCanvasSegment.colorMap.width = widthPx;
                tooltipCanvasSegment.colorMap.height = heightPx;
                tooltipCanvasSegment.colorMap.r = new float[widthPx][heightPx];
                tooltipCanvasSegment.colorMap.g = new float[widthPx][heightPx];
                tooltipCanvasSegment.colorMap.b = new float[widthPx][heightPx];
                tooltipCanvasSegment.colorMap.a = new float[widthPx][heightPx];
                return tooltipCanvasSegment;
            }

            /* Draw Functions */

            public void point(TooltipCanvasSegment tooltipCanvasSegment, int x, int y, float r, float g, float b, float a) {
                if (tooltipCanvasSegment == null) return;
                UICommonUtils.colorMap_set(tooltipCanvasSegment.colorMap, x, y, r, g, b, a);
            }

            public void point(TooltipCanvasSegment tooltipCanvasSegment, int x, int y, Color color) {
                point(tooltipCanvasSegment, x, y, color.r, color.g, color.b, color.a);
            }

            public void clear(TooltipCanvasSegment tooltipCanvasSegment, float r, float g, float b, float a) {
                if (tooltipCanvasSegment == null) return;
                UICommonUtils.colorMap_clear(tooltipCanvasSegment.colorMap, r, g, b, a);
            }

            public void clear(TooltipCanvasSegment tooltipCanvasSegment, Color color) {
                clear(tooltipCanvasSegment, color.r, color.g, color.b, color.a);
            }

            public void copy(TooltipCanvasSegment tooltipCanvasSegment, ColorMap colorMap) {
                if (tooltipCanvasSegment == null) return;
                UICommonUtils.colorMap_copy(colorMap, tooltipCanvasSegment.colorMap);
            }

            public Color getColor(TooltipCanvasSegment tooltipCanvasSegment, int x, int y) {
                if (tooltipCanvasSegment == null) return null;
                return UICommonUtils.colorMap_getPointAsColor(tooltipCanvasSegment.colorMap, x, y);
            }

            public float getR(TooltipCanvasSegment tooltipCanvasSegment, int x, int y) {
                if (tooltipCanvasSegment == null) return 0f;
                return UICommonUtils.colorMap_r(tooltipCanvasSegment.colorMap, x, y);
            }

            public float getG(TooltipCanvasSegment tooltipCanvasSegment, int x, int y) {
                if (tooltipCanvasSegment == null) return 0f;
                return UICommonUtils.colorMap_g(tooltipCanvasSegment.colorMap, x, y);
            }

            public float getB(TooltipCanvasSegment tooltipCanvasSegment, int x, int y) {
                if (tooltipCanvasSegment == null) return 0f;
                return UICommonUtils.colorMap_b(tooltipCanvasSegment.colorMap, x, y);
            }

            public float getA(TooltipCanvasSegment tooltipCanvasSegment, int x, int y) {
                if (tooltipCanvasSegment == null) return 0f;
                return UICommonUtils.colorMap_a(tooltipCanvasSegment.colorMap, x, y);
            }

        }

    }


}



