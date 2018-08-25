package com.gbjam6.city.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.gbjam6.city.GBJam6;

import javax.swing.ImageIcon;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.addIcon("sprites/gui/ICON.png", Files.FileType.Internal);
        config.width = 640;
        config.height = 576;
        config.title = "Campagne - GBJAM6";
        config.resizable = false;
        new LwjglApplication(new GBJam6(), config);
    }
}
