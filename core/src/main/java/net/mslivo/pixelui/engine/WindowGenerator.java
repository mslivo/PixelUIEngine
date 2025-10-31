package net.mslivo.pixelui.engine;

public final class WindowGenerator {

    public interface P0 {
        Window createWindow(API api);
    }

    public interface P1<P1> {
        Window createWindow(API api, P1 p1);
    }

    public interface P2<P1, P2> {
        Window createWindow(API api, P1 p1, P2 p2);
    }

    public interface P3<P1, P2, P3> {
        Window createWindow(API api, P1 p1, P2 p2, P3 p3);
    }

    public interface P4<P1, P2, P3, P4> {
        Window createWindow(API api, P1 p1, P2 p2, P3 p3, P4 p4);
    }

    public interface P5<P1, P2, P3, P4, P5> {
        Window createWindow(API api, P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

}
