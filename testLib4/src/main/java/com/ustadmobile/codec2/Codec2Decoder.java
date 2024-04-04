package com.ustadmobile.codec2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Helper class to manage decoding Codec2 audio into something that can be played in Android.  The
 * decoded data is 16Khz MONO PCM 16Bit encoded.
 *
 * This can be played back using something along the lines of the following:
 *
 * int minBufferSize = AudioTrack.getMinBufferSize(8000,
 *                 AudioFormat.CHANNEL_CONFIGURATION_MONO,
 *                 AudioFormat.ENCODING_PCM_16BIT);
 *
 * AudioTrack track = new AudioTrack(
 *                 AudioManager.STREAM_MUSIC,
 *                 8000,
 *                 AudioFormat.CHANNEL_OUT_MONO,
 *                 AudioFormat.ENCODING_PCM_16BIT,
 *                 Math.max(minBufferSize, decoder.getOutputBufferSize(),
 *                 AudioTrack.MODE_STREAM);
 */
public class Codec2Decoder {

    private final long codec2Con;

    private final byte[] codec2InBufBytes;

    short[] rawAudioOutBuf;

    private ByteBuffer rawAudioOutBytesBuffer;

    private final int samplesPerFrame;

    /**
     * Create and allocate new decoder
     *
     *
     * @param codec2Mode As per Codec2.MODE flags
     */
    public Codec2Decoder( int codec2Mode) {
        codec2Con = Codec2.create(codec2Mode);

        int nBytes = Codec2.getBitsSize(codec2Con);
        int nByte = (nBytes + 7) / 8;
        codec2InBufBytes = new byte[nBytes];

        rawAudioOutBuf = new short[Codec2.getSamplesPerFrame(codec2Con)];

        //multiply by two to handle the fact that output is in shorts (2 bytes)
        samplesPerFrame =Codec2.getSamplesPerFrame(codec2Con);
        rawAudioOutBytesBuffer = null;
        rawAudioOutBytesBuffer = ByteBuffer.allocate(samplesPerFrame * 2);

        rawAudioOutBytesBuffer.order(ByteOrder.nativeOrder());
    }

    public int getOutputBufferSize() {
        return rawAudioOutBytesBuffer.capacity();
    }

    public int getSamplesPerFrame() {
        return samplesPerFrame;
    }

    public int getInputBufferSize() {
        return codec2InBufBytes.length;
    }

    public ByteBuffer getRawAudioOutBytesBuffer() {
        return rawAudioOutBytesBuffer;
    }

    /**
     * Read and decode a frame of audio.
     *
     * @return ByteBuffer containing the decoded audio, or null if there is nothing left
     *
     * @throws IOException if an IOException occurs in the underlying system
     */
    public ByteBuffer readFrame(byte [] codec2InBufBytes) throws IOException {
        try {
            rawAudioOutBuf = new short[Codec2.getSamplesPerFrame(codec2Con)];
            Codec2.decode(codec2Con, rawAudioOutBuf, codec2InBufBytes);
            rawAudioOutBytesBuffer.rewind();
            for (int i = 0; i < rawAudioOutBuf.length; i++) {
                rawAudioOutBytesBuffer.putShort(rawAudioOutBuf[i]);
            }
        }catch (Exception exception) {
            exception.printStackTrace();
        }
        return rawAudioOutBytesBuffer;

    }

    /**
     * Release resources held by the JNI system. Must be called once finished to release resources.
     */
    public void destroy() {
        Codec2.destroy(codec2Con);
    }

}
