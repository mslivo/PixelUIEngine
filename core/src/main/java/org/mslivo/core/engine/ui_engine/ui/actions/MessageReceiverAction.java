package org.mslivo.core.engine.ui_engine.ui.actions;

public abstract class MessageReceiverAction {

    public final String messageType;

    public MessageReceiverAction(String messageType){
        this.messageType = messageType;
    }

    public abstract void onMessageReceived(Object... p);

}
