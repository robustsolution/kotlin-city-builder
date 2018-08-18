package com.gbjam6.city.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gbjam6.city.GBJam6;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 640;
        config.height = 576;
        config.title = "GBJAM6 - City Builder";
        new LwjglApplication(new GBJam6(), config);
    }
}
