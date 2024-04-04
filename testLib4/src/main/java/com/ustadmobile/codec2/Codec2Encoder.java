package com.ustadmobile.codec2;

import android.util.Log;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Codec2Encoder {

    private InputStream input;

    private final long codec2Con;

    private final byte[] codec2InBufBytes;

    private final short[] rawAudioOutBuf;

    private final ByteBuffer rawAudioOutBytesBuffer;

    private final int samplesPerFrame;

    /**
     * Create and allocate new decoder
     *
     * @param input InputStream
     *
     * @param codec2Mode As per Codec2.MODE flags
     */
    public Codec2Encoder( int codec2Mode) {
        codec2Con = Codec2.create(codec2Mode);

        int nBytes = Codec2.getBitsSize(codec2Con);
        int nByte = (nBytes + 7) / 8;
        codec2InBufBytes = new byte[nBytes];

        rawAudioOutBuf = new short[Codec2.getSamplesPerFrame(codec2Con)];

        //multiply by two to handle the fact that output is in shorts (2 bytes)
        samplesPerFrame =Codec2.getSamplesPerFrame(codec2Con);
        rawAudioOutBytesBuffer = ByteBuffer.allocate(samplesPerFrame *2 );

        rawAudioOutBytesBuffer.order(ByteOrder.nativeOrder());
    }

    public int getOutputBufferSize() {
        return rawAudioOutBytesBuffer.capacity();
    }


    public short[] getRawAudioOutBuf() {
        return rawAudioOutBuf;
    }


    public int getSamplesPerFrame() {
        return samplesPerFrame;
    }

    public int getInputBufferSize() {
        return codec2InBufBytes.length;
    }

    public void encode (short[] input,char[] output) {
        StringBuilder logShort = new StringBuilder();
        for (short s : input) {
            logShort.append(Integer.toHexString(s)).append(" ");
        }
        Codec2.encode700c(codec2Con, input, output);

        StringBuilder logChar = new StringBuilder();
        for (char c : output) {
            logChar.append(Integer.toHexString(c)).append(" ");
        }
    }


    /**
     * Release resources held by the JNI system. Must be called once finished to release resources.
     */
    public void destroy() {
        Codec2.destroy(codec2Con);
    }
}
