package server;

import java.io.ByteArrayInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class PlayerAudio {

    private AudioFormat format;
    private SourceDataLine lineOut;
    private AudioInputStream in;

    public PlayerAudio(AudioFormat format) {
        this.format = format;
        
    }

    public void initAudio(byte[] audio) {
        System.out.println(audio.length);
        try{
            in = new AudioInputStream(new ByteArrayInputStream(audio), format, audio.length / format.getFrameSize());
            lineOut = AudioSystem.getSourceDataLine(format);
            lineOut.open(format);
            lineOut.start();
            playAudio();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void playAudio() {
        byte[] buffer = new byte[1024];
        int bytesRead;
        try {
            System.out.println("Reproduciendo audio...");
            while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                System.out.println("Reproduciendo buffer..."+ bytesRead);
                lineOut.write(buffer, 0, bytesRead);
            }
            lineOut.drain();
            lineOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
