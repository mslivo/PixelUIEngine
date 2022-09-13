package org.vnna.core.example.data;

import org.vnna.core.example.data.game.ExampleObject;

/**
 * All GameObjects are create here using Static Methods
 */
public class ExampleDataGenerator {

    public static ExampleObject create_ExampleObject(){
        ExampleObject object = new ExampleObject();
        object.someVariable = 1;
        return object;
    }



}
