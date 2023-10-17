package org.mslivo.example.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a current Snapshot of the Simulations state and is passed to the Engine.
 * Contents of this class should be modified by the GameEngine and Read by the UIEngine.
 * Guidelines:
 * - Members should to break down into Primitives
 * - Members should be serializable.
 * - Members should be public.
 * - Collections should be initialized on declartion
 */
public class ExampleData implements Serializable {

    public int variable;

    public ArrayList<Integer> list = new ArrayList<>();

}
