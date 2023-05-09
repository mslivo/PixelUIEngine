package org.mslivo.core.example.data;


/**
 * All Data-Objects are create here using Static Methods (create_)
 */
public class ExampleDataGenerator {

    public static ExampleData create_exampleData(){
        ExampleData exampleData = new ExampleData();
        exampleData.variable = 123;
        return exampleData;
    }



}
