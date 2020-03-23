/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.apksig;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class Hints {
    /**
     * Name of hint pattern asset file in APK.
     */
    public static final String PIN_HINT_ASSET_ZIP_ENTRY_NAME = "assets/com.android.hints.pins.txt";

    /**
     * Name of hint byte range data file in APK.  Keep in sync with PinnerService.java.
     */
    public static final String PIN_BYTE_RANGE_ZIP_ENTRY_NAME = "pinlist.meta";

    private static int clampToInt(long value) {
        return (int) Math.max(0, Math.min(value, Integer.MAX_VALUE));
    }

    public static final class ByteRange {
        final long start;
        final long end;

        public ByteRange(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Create a blob of bytes that PinnerService understands as a
     * sequence of byte ranges to pin.
     */
    public static byte[] encodeByteRangeList(List<ByteRange> pinByteRanges) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(pinByteRanges.size() * 8);
        DataOutputStream out = new DataOutputStream(bos);
        try {
            for (ByteRange pinByteRange : pinByteRanges) {
                out.writeInt(clampToInt(pinByteRange.start));
                out.writeInt(clampToInt(pinByteRange.end - pinByteRange.start));
            }
        } catch (IOException ex) {
            throw new AssertionError("impossible", ex);
        }
        return bos.toByteArray();
    }

    public static ArrayList<Pattern> parsePinPatterns(byte[] patternBlob) {
        ArrayList<Pattern> pinPatterns = new ArrayList<>();
        try {
            for (String rawLine : new String(patternBlob, "UTF-8").split("\n")) {
                String line = rawLine.replaceFirst("#.*", "");  // # starts a comment
                if (!("".equals(line))) {
                    pinPatterns.add(Pattern.compile(line));
                }
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 must be supported", ex);
        }
        return pinPatterns;
    }
}
