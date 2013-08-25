package com.adrguides.tts;

/**
 * Created by adrian on 25/08/13.
 */
public class GuidesSingleton {
    private static GuidesSingleton ourInstance = new GuidesSingleton();

    public static GuidesSingleton getInstance() {
        return ourInstance;
    }

    private GuidesSingleton() {
    }



}
