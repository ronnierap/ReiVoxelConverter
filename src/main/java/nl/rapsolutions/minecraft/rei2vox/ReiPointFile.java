package nl.rapsolutions.minecraft.rei2vox;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Created by ronnie on 21-04-14.
 */
public class ReiPointFile {
    private final String server;
    private final String dimension;

    public ReiPointFile(File pointfile) {
        // 127.0.0.1.DIM0.points

        String basename = FilenameUtils.getBaseName(pointfile.getName());
        server = FilenameUtils.getBaseName(basename);
        dimension = FilenameUtils.getExtension(basename).substring(3);
    }

    public String getServer() {
        return server;
    }

    public String getDimension() {
        return dimension;
    }
}
