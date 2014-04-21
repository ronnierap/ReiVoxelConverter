package nl.rapsolutions.minecraft.rei2vox;

import sun.awt.OSInfo;

import java.io.File;

/**
 * Created by ronnie on 21-04-14.
 */
public class UserDir {
    public static String getApplicationSupport() {
        switch (OSInfo.getOSType()) {
            case MACOSX:
                String applicationSupportDirectory = System.getProperty("user.home") + "/Library/Application Support/minecraft";
                return applicationSupportDirectory;
            case WINDOWS:
                return System.getenv("APPDATA") + File.separatorChar + ".minecraft";
            default:
                return System.getProperty("user.home") + File.separatorChar + ".minecraft";
        }
    }
}
