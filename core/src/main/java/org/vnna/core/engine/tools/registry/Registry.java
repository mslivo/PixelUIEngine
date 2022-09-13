package org.vnna.core.engine.tools.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
/*
 * Manages Lists of Classes
 */
public class Registry {

    private HashMap<Class, ArrayList> registry;

    public Registry(){
        registry = new HashMap<>();
    }

    public void removeFromRegistry(Object object) {
        getRegistryList(object.getClass()).remove(object);
    }

    public void addToRegistry(Object object) {
        ArrayList list = getRegistryList(object.getClass());
        if(!list.contains(object)) list.add(object);
    }

    public Set<Class> getClasses(){
        return registry.keySet();
    }

    public ArrayList getRegistryList(Class rClass) {
        ArrayList list = registry.get(rClass);
        if(list == null){
            list = new ArrayList();
            registry.put(rClass, list);
            return list;
        }
        return list;
    }

}
