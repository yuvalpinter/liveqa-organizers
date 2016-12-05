// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.haifa_utils.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/*
 * This loader used to load data/model/conf files from the classpath (inside the jar). All methods are static and use
 * paths relative to a home directory "src/[main,test]/java". Meaning: if you have a conf file:
 * src/main/java/confs/conf.txt, you pass "/confs/conf.txt" as path (notice the leading '/').
 */
public class ResourceDataLoader {

    /**
     * Returns an reader of the resource
     * 
     * @param path to the resource.
     * @return <code>Reader</code> of the specified file.
     */
    public static Reader getReader(String path) {
        return new InputStreamReader(getResource(path), StandardCharsets.UTF_8);
    }

    /**
     * Returns an input stream to the resource
     * 
     * @param path to the resource.
     * @return <code>InputStream</code> of the specified file.
     */
    public static InputStream getResource(String path) {
        InputStream resourceStream = null;
        try {
            if (!resourceExists(path))
                return null;
            resourceStream = ResourceDataLoader.class.getResourceAsStream(path);
            return FileUtils.wrapInputStream(resourceStream, path);
        } catch (IOException e) {
            e.printStackTrace();
            if (null != resourceStream) {
                try {
                    resourceStream.close();
                } catch (IOException e1) {
                }
            }
            return null;
        }
    }

    /**
     * Returns the content of the resource file in a <code>String</code>.
     * 
     * @param path to the resource.
     * @return The content of the file.
     * @throws Exception
     */
    public static String getResourceString(String path) throws Exception {
        InputStream is = getResource(path);
        return inputStreamToString(is);
    }

    /**
     * Returns a reference to a new temporary copy of the requested resources file.
     * 
     * @param path to the resource.
     * @return Reference to the new temporary file.
     * @throws IOException
     */
    public static File getFile(String path) throws IOException {
        // if path is a local file, return it instead of searching for the resource
        File f = new File(path);
        if (f.exists() && f.isFile())
            return f;


        // Result.
        File tempFile = null;

        // Open the stream if the resource exists.
        try (InputStream originalInputStream = getResource(path)) {
            if (originalInputStream == null) {
                return null;
            }

            // Get the filename and replace the leading underscores (good for hadoop file system).
            String fileName = new File(path).getName().replaceFirst("_*", "");

            // Get an extension if exists.
            String extension = "";
            if (fileName.contains(".")) {
                extension = fileName.substring(fileName.lastIndexOf("."));
            }

            // Copy the resource to a temporary file (with the same extension).
            tempFile = File.createTempFile(fileName, extension);

            // Delete the file on exit since it is a temporary file.
            tempFile.deleteOnExit();

            try (BufferedOutputStream fos = FileUtils.openBufferedOutputStream(tempFile)) {
                FileUtils.copy(originalInputStream, fos);
            }
        }

        return tempFile;
    }

    /**
     * Checks whether a resource exists.
     * 
     * @param path name of the resource.
     * @return Returns <code>true</code> is a resource with this name exists.
     */
    public static boolean resourceExists(String path) {
        return ResourceDataLoader.class.getResource(path) != null;
    }

    /**
     * 
     * @param path
     * @return
     */
    public static File[] getFilesInsideDirectory(String path) {
        URL url = ResourceDataLoader.class.getResource(path);
        if (url == null)
            return null;
        File dir = null;
        try {
            dir = new File(url.toURI());
        } catch (URISyntaxException e) {
            return null;
        }
        File[] res = dir.listFiles();
        Arrays.sort(res);
        return res;
    }

    private static String inputStreamToString(InputStream ins) throws Exception {
        if (ins == null)
            return null;
        java.util.Scanner s = new java.util.Scanner(ins, "UTF-8");
        s.useDelimiter("\\A");
        String res = s.hasNext() ? s.next() : "";
        s.close();
        return res;
    }
}
