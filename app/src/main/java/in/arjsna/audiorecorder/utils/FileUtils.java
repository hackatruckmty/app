package in.arjsna.audiorecorder.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by javier on 11/25/17.
 */

public class FileUtils {
    /**
     * Instances should NOT be constructed in standard programming.
     */
    public FileUtils() { }

    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a gigabyte.
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;



    public static String readFileToString(File file, String encoding) throws IOException {
        InputStream in = new java.io.FileInputStream(file);
        try {
            return IOUtils.toString(in, encoding);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

}
