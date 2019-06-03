package com.mg.agenda;


import android.app.Application;

import com.mg.agenda.engine.Calman;
import com.mg.agenda.engine.ItemDepot;

/**
 * The application class. Initializes the components which have life cycle tied to it.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ItemDepot.get().initialize(getApplicationContext());
        Calman.get().initialize(getApplicationContext());
    }

}
