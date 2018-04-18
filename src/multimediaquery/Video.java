/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multimediaquery;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author yihanyang
 */
public class Video { 
    class PlayVideo implements Runnable {
        public void run() {
            int nBytes = 0;
            byte[] audioBuffer = new byte[bufferSize];
            while (true) {;
                lock.lock();
                boolean tempPause = pause;
                boolean tempStop = stop;
                lock.unlock();
                if (!tempPause && !tempStop) {
                    if(currentFrame > totalFrames) {
                        resetCurrentFrame();
                        closeAudio();
                        openAudio();
                    }
                    File file = new File(folder + "/" + videoName + String.format("%03d", currentFrame) + ".rgb");
                    currentImage = readImage(folder + "/" + videoName + String.format("%03d", currentFrame) + ".rgb");
                    displayImage();
                    
                    nBytes = readAudio(audioBuffer);
                    playAudio(audioBuffer, nBytes);
                    
                    try {
                        Thread.sleep(1000 / frameRate);
                    } catch (Exception e) {

                    }
                    increaseCurrentFrame();
                } else if (tempPause) {
                    break;
                } else {
                    resetCurrentFrame();
                    closeAudio();
                    openAudio();
                    break;
                }
            }
        }
    }
    
    final int width = 352;
    final int height = 288;
    final int frameRate = 30;
    final int bufferSize = 5883;
    
    int mode;
    DisplayMain displayMain;
    String folder;
    String videoName;
    int totalFrames;
    int currentFrame;
    BufferedImage currentImage;
    AudioInputStream audioInputStream;
    AudioFormat audioFormat;
    Info info;
    SourceDataLine dataLine;
    boolean pause;
    boolean stop;
    Lock lock;
    
    public Video(DisplayMain displayMain, String folder, int mode){
        this.mode = mode; // mode == 1 if this is query video, else mode == 2
        this.displayMain = displayMain;
        this.folder = folder;
        this.videoName = folder.substring(folder.lastIndexOf("/") + 1);
        this.totalFrames = (new File(folder)).list().length - 1;
        this.currentFrame = 1;
        this.currentImage = null;
        openAudio();
        this.pause = false;
        this.stop = true;
        this.lock = new ReentrantLock();
    }
    
    public BufferedImage readImage(String path){
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        try{
            File file = new File(path);
            InputStream is = new FileInputStream(file);
            
            long len = file.length();
            byte[] bytes = new byte[(int)len];

            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }


            int ind = 0;
            for(int y = 0; y < height; y++){

                for(int x = 0; x < width; x++){

                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    image.setRGB(x,y,pix);
                    ind++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return image;
    }
    
    public void displayImage(){
        if(mode == 1){
            displayMain.displayQueriedVideo(currentImage);
        }else {
            displayMain.displaySelectedVideo(currentImage);
        }
    }
    
    public void openAudio() {
        try{
            FileInputStream waveStream = new FileInputStream(folder + "/" + videoName + ".wav");
            InputStream bufferedIn = new BufferedInputStream(waveStream);
            this.audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
            this.audioFormat = audioInputStream.getFormat();
            this.info = new Info(SourceDataLine.class, this.audioFormat);
            dataLine = (SourceDataLine) AudioSystem.getLine(info);
            dataLine.open(audioFormat, bufferSize);
            dataLine.start();
        } catch(Exception e){
            
        }
    }
    
    public int readAudio(byte[] audioBuffer) {
        int nBytes = -1;
        try{
            nBytes = audioInputStream.read(audioBuffer, 0, audioBuffer.length);
        } catch(Exception e) {
            
        }
        return nBytes;                                 
    }
    
    public void playAudio(byte[] audioBuffer, int nBytes) {
        if(nBytes >= 0) {
            dataLine.write(audioBuffer, 0, nBytes);
        }
    }
    
    public void closeAudio(){
        dataLine.close();
    }
    
    public void play(){
        lock.lock();
        if(pause || stop) {
            pause = false;
            stop = false; 
            (new Thread(new PlayVideo())).start();
        }
        lock.unlock();
    }
    
    public void pause(){
        lock.lock();
        pause = true;
        lock.unlock();
    }
    
    public void stop(){
        lock.lock();
        stop = true;
        lock.unlock();
    }
    
    public void increaseCurrentFrame(){
        currentFrame++;
        if(mode == 2) {
            displayMain.setSliderDescriptor(currentFrame, totalFrames);
        }
    }
    
    public void resetCurrentFrame(){
        currentFrame = 1;
        if(mode == 2) {
            displayMain.setSliderDescriptor(currentFrame, totalFrames);
        }
    }
}
