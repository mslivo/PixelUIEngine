package org.mslivo.core.example.data;


/**
 * All Data-Objects are create here using Static Methods (create_)
 */
public class ExampleDataGenerator {

    public static ExampleData create_exampleData(){
        ExampleData data = new ExampleData();
        data.variable = 123;
        return data;
    }



}
