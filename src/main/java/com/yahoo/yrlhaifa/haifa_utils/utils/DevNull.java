// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.haifa_utils.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class DevNull extends PrintStream {
    private static class EmptyOutput extends OutputStream {
        @Override
        public void write(int b) throws IOException {}

    }

    public DevNull() {
        super(new EmptyOutput());
    }
}
