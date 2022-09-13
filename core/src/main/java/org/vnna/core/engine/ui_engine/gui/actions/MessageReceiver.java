package org.vnna.core.engine.ui_engine.gui.actions;

public abstract class MessageReceiver {

    public final String message_type;

    public MessageReceiver(String message_type){
        this.message_type = message_type;
    }

    public abstract void onMessageReceived(Object... p);

}
