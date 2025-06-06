package net.mslivo.core.engine.ui_engine.ui.notification;

import net.mslivo.core.engine.ui_engine.constants.TOOLTIP_NOTIFICATION_STATE;
import net.mslivo.core.engine.ui_engine.ui.tooltip.Tooltip;

public non-sealed class TooltipNotification extends CommonNotification {
    public int x,y;
    public Tooltip tooltip;
    public TOOLTIP_NOTIFICATION_STATE state;
}
