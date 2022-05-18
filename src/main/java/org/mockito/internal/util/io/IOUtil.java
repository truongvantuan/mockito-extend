/*
 * Copyright (c) 2016 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.util.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.mockito.exceptions.base.MockitoException;

/**
 * IO utils. A bit of reinventing the wheel but we don't want extra dependencies at this stage and
 * we want to be java.
 */
public final class IOUtil {

    /**
     * Writes text to file
     */
    public static void writeText(String text, File output) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(output));
            pw.write(text);
        } catch (Exception e) {
            throw new MockitoException("Problems writing text to file: " + output, e);
        } finally {
            close(pw);
        }
    }

    public static Collection<String> readLines(InputStream is) {
        List<String> out = new LinkedList<>();
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = r.readLine()) != null) {
                out.add(line);
            }
        } catch (IOException e) {
            throw new MockitoException("Problems reading from: " + is, e);
        }
        return out;
    }

    /**
     * Closes the target. Does nothing when target is null. Is silent.
     *
     * @param closeable the target, may be null
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            close(closeable);
        } catch (MockitoException ignored) {
            // ignore, for backwards compatibility
        }
    }

    /**
     * Closes the target. Does nothing when target is null. Is not silent and exceptions are rethrown.
     *
     * @param closeable the target, may be null
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                throw new MockitoException("Problems closing stream: " + closeable, e);
            }
        }
    }

    private IOUtil() {}
}
