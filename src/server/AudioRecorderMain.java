package server;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;

public class AudioRecorderMain {
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = true;


    public static void main(String[] args) {
        int duration = 5; // seconds
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        ByteArrayOutputStream out = new ByteArrayOutputStream();


        RecordAudio recorder = new RecordAudio(format, duration, out);
        Thread t = new Thread(recorder);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        byte[] audio = out.toByteArray();
        PlayerAudio player = new PlayerAudio(format);
        player.initAudio(audio);
        
    }
}