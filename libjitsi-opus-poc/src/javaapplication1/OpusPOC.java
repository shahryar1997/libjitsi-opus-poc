/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package javaapplication1;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.AudioFormat;
//import javax.sound.sampled.AudioFormat;
import org.jitsi.impl.neomedia.codec.audio.opus.JNIDecoder;
import org.jitsi.impl.neomedia.codec.audio.opus.JNIEncoder;
import org.jitsi.impl.neomedia.codec.audio.opus.Opus;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.codec.Constants;

/**
 *
 * @author root
 */
public class OpusPOC {
//static {
//        System.loadLibrary("jnopus"); // loads libjnopus.so
//    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ResourceUnavailableException {


        LibJitsi.start();
        // TODO code application logic here


 // Create a small PCM buffer (sine wave)
        int sampleRate = 48000;
        int channels = 1;
        int frameSize = (sampleRate / 1000) * 20; // 20ms
        int bytesPerSample = 2;

        byte[] pcmData = new byte[frameSize * bytesPerSample];
        for (int i = 0; i < frameSize; i++) {
            short val = (short)(Math.sin(2 * Math.PI * 440 * i / sampleRate) * Short.MAX_VALUE);
            pcmData[2 * i] = (byte)(val & 0xff);
            pcmData[2 * i + 1] = (byte)((val >> 8) & 0xff);
        }

        // Build input Buffer with correct format
        Buffer in = new Buffer();
        in.setFormat(new AudioFormat(
                AudioFormat.LINEAR,
                sampleRate,
                16,
                channels,
                AudioFormat.LITTLE_ENDIAN,
                AudioFormat.SIGNED,
                Format.NOT_SPECIFIED,
                Format.NOT_SPECIFIED,
                Format.byteArray
        ));
        in.setData(pcmData);
        in.setOffset(0);
        in.setLength(pcmData.length);
        
        System.out.println("before encoding "+in.getLength());

        // Prepare output buffer for encoded data
        Buffer enc = new Buffer();
        enc.setData(new byte[4096]);
        enc.setOffset(0);

        // ENCODING
        JNIEncoder encoder = new JNIEncoder();
//        encoder.open();
        Format matched = encoder.setInputFormat(in.getFormat());
//        if (matched == null) throw new RuntimeException("Input format not supported");
//        encoder.setOutputFormat(enocoder.getMatchingOutputFormats(matched)[0]); // typical workflow
        encoder.open();

        int r = encoder.process(in, enc);
//        if (r != Buffer.BUFFER_PROCESSED_OK && (r & Buffer.INPUT_BUFFER_NOT_CONSUMED) == 0) {
//            throw new RuntimeException("Encoding failed: " + r);
//        }
        int encLen = enc.getLength();
        System.out.println("Encoded length: " + encLen);

        encoder.close();

        // Prepare decoder input & output
        Buffer decIn = new Buffer();
        decIn.setFormat(enc.getFormat());
        decIn.setData(enc.getData());
        decIn.setOffset(enc.getOffset());
        decIn.setLength(encLen);

        Buffer decOut = new Buffer();
        decOut.setData(new byte[frameSize * bytesPerSample]);
        decOut.setOffset(0);

        // DECODING
        JNIDecoder decoder = new JNIDecoder();
      
//        decoder.setInputFormat(decIn.getFormat());
        decoder.setInputFormat(new AudioFormat(Constants.OPUS_RTP));
        decoder.setOutputFormat(new AudioFormat(
                AudioFormat.LINEAR,
                sampleRate,
                16,
                channels,
                AudioFormat.LITTLE_ENDIAN,
                AudioFormat.SIGNED,
                Format.NOT_SPECIFIED,
                Format.NOT_SPECIFIED,
                Format.byteArray
        ));
        decoder.open();

        r = decoder.process(decIn, decOut);
//        if (r != Buffer.BUFFER_PROCESSED_OK && (r & Buffer.OUTPUT_BUFFER_NOT_FILLED) != 0) {
//            throw new RuntimeException("Decoding failed: " + r);
//        }
        int decLen = decOut.getLength();
        System.out.println("Decoded bytes: " + decLen);

        decoder.close();

        // Convert first few PCM bytes to short values
        byte[] decodedBytes = (byte[]) decOut.getData();
        for (int i = 0; i < Math.min(10, decLen/2); i++) {
            int lo = decodedBytes[2*i] & 0xFF;
            int hi = decodedBytes[2*i+1] << 8;
            short s = (short)(hi | lo);
            System.out.println("Decoded sample " + i + ": " + s);
        }


        LibJitsi.stop();
    }

}
