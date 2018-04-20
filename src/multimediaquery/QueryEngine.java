/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multimediaquery;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author yihanyang
 */
public class QueryEngine {
    class VideoFeatures {
        Video video;
        List<List<Integer>> videoColors;
        List<Set<String>> videoMotions;
        List<Integer> videoAudios;
        double dist;
        
        public VideoFeatures(Video video) {
            this.video = video;
            this.videoColors = new ArrayList<>();
            this.videoMotions = new ArrayList<>();
            this.videoAudios=new ArrayList<>();
            this.dist = 0;
        }
    }
    
    class QueryEngineThread implements Runnable {
        Video queriedVideo;
        
        public QueryEngineThread(Video queriedVideo) {
            this.queriedVideo = queriedVideo;
        }
        
        public void run() {
            VideoFeatures queriedVideoFeatures = new VideoFeatures(queriedVideo);
            // Analyze queried video
            analyzeVideo(queriedVideoFeatures);
            
            // Compare features of queried video and features of videos in database
            List<VideoFeatures> queryResults = new ArrayList<>();
            for (VideoFeatures candidateVideoFeatures : videosFeatures) {
                distance(queriedVideoFeatures, candidateVideoFeatures);
                queryResults.add(candidateVideoFeatures);
            }
            
            // Sort query results based on distance
            queryResults.sort(new Comparator<VideoFeatures>() {
                public int compare(VideoFeatures videoFeatures1, VideoFeatures videoFeatures2) {
                    return (int) (videoFeatures1.dist - videoFeatures2.dist);
                }
            });
            
            // display query results
            displayQueryResults(queryResults);
        }
    }
    
    DisplayMain displayMain;
    String databasePath;
    String databaseFeaturesPath;
    File[] videoFiles;
    int nVideos;
    VideoFeatures[] videosFeatures;
    
    ColorExtractor colorExtractor;
    final int nBuckets = 2;
    
    MotionExtractor motionExtractor;
    
    AudioExtractor audioExtractor;
    
    public QueryEngine(DisplayMain displayMain, String databasePath) {
        this.displayMain = displayMain;
        this.databasePath = databasePath;
        this.databaseFeaturesPath = this.databasePath + "features.txt";
        this.colorExtractor = new ColorExtractor();
        this.motionExtractor = new MotionExtractor();
        this.audioExtractor=new AudioExtractor();
        
        File file = new File(this.databasePath);
        videoFiles = file.listFiles();
        nVideos = videoFiles.length - 1;
        videosFeatures = new VideoFeatures[nVideos];
        for(int i = 0; i < nVideos; i++) {
            Video video = new Video(this.displayMain, videoFiles[i + 1].getAbsolutePath(), 2);
            videosFeatures[i] = new VideoFeatures(video);
        }
        readFeaturesOfDB();
    }
    
    public QueryEngine(String databasePath) {
        this.displayMain = null;
        this.databasePath = databasePath;
        this.databaseFeaturesPath = this.databasePath + "features.txt";
        this.colorExtractor = new ColorExtractor();
        this.motionExtractor = new MotionExtractor();
        this.audioExtractor= new AudioExtractor();
        
        File file = new File(this.databasePath);
        videoFiles = file.listFiles();
        nVideos = videoFiles.length - 1;
        videosFeatures = new VideoFeatures[nVideos];
        for(int i = 0; i < nVideos; i++) {
            Video video = new Video(this.displayMain, videoFiles[i + 1].getAbsolutePath(), 2);
            videosFeatures[i] = new VideoFeatures(video);
        }
        long start = System.currentTimeMillis();
        analyzeDatabase();
        long end = System.currentTimeMillis();
        System.out.println((end - start) / 1000.0);
        writeFeaturesOfDB();
    }
    
    /**
     * Analyze all videos in database when launching this program
     */
    public void analyzeDatabase() {
        for(VideoFeatures videoFeatures : videosFeatures) {
            analyzeVideo(videoFeatures);
        }
    }
    
    public void writeFeaturesOfDB() {
        BufferedWriter bw = null;
        FileWriter fw = null;
        
        try {
            fw = new FileWriter(databaseFeaturesPath);
            bw = new BufferedWriter(fw);
            for(VideoFeatures videoFeatures : videosFeatures) { 
                bw.write("Video: " + videoFeatures.video.videoName);
                bw.newLine();
                
                if(videoFeatures.videoColors.size() != 0) {
                    bw.write("Color");
                    bw.newLine();
                    int nFrames = videoFeatures.videoColors.size();
                    bw.write(Integer.toString(nFrames));
                    bw.newLine();
                    for (List<Integer> frameColors : videoFeatures.videoColors) {
                        StringBuilder frameColorsSB = new StringBuilder();
                        for (int color : frameColors) {
                            frameColorsSB.append(Integer.toString(color));
                            frameColorsSB.append(" ");
                        }
                        bw.write(frameColorsSB.toString());
                        bw.newLine();
                    }
                }
                
                if(videoFeatures.videoMotions.size() != 0) {
                    bw.write("Motion");
                    bw.newLine();
                    int nFrames = videoFeatures.videoMotions.size();
                    bw.write(Integer.toString(nFrames));
                    bw.newLine();
                    for (Set<String> frameMotions : videoFeatures.videoMotions) {
                        StringBuilder frameMotionsSB = new StringBuilder();
                        for (String motion : frameMotions) {
                            frameMotionsSB.append(motion);
                            frameMotionsSB.append(" ");
                        }
                        bw.write(frameMotionsSB.toString());
                        bw.newLine();
                    }
                }
                if(videoFeatures.videoAudios.size()!=0)
                {
                    bw.write("Audio");
                    bw.newLine();
                    int nFrames = videoFeatures.videoAudios.size();
                    bw.write(Integer.toString(nFrames));
                    bw.newLine();
                    for (int frameAudios : videoFeatures.videoAudios) {
                        StringBuilder frameAudiosSB = new StringBuilder();
                        
                        frameAudiosSB.append(frameAudios);
                        bw.write(frameAudiosSB.toString());
                        bw.newLine();
                    }
                }
                bw.write("END");
                bw.newLine();
            }
        } catch(Exception e) {
            
        } finally {
            try{
                if(bw != null) bw.close();
                if(fw != null) fw.close();
            } catch(Exception e) {
                
            }
        }
    }
    
    public void readFeaturesOfDB() {
        String line = null;
        FileReader fr = null; 
        BufferedReader br = null;
        
        try{
            fr = new FileReader(databaseFeaturesPath);
            br = new BufferedReader(fr);
            int index = 0;
            while((line = br.readLine()) != null) {
                boolean end = false;
                while(!end) {
                    line = br.readLine();
                    switch (line) {
                        case "Color": {
                            line = br.readLine();
                            int nFrames = Integer.parseInt(line);
                            for (int i = 0; i < nFrames; i++) {
                                line = br.readLine();
                                String[] tokens = line.split(" ");
                                List<Integer> frameColors = new ArrayList<>();
                                for (int j = 0; j < tokens.length; j++) {
                                    frameColors.add(Integer.parseInt(tokens[j]));
                                }
                                videosFeatures[index].videoColors.add(frameColors);
                            }
                            break;
                        }

                        case "Motion": {
                            line = br.readLine();
                            int nFrames = Integer.parseInt(line);
                            for (int i = 0; i < nFrames; i++) {
                                line = br.readLine();
                                String[] tokens = line.split(" ");
                                Set<String> frameMotions = new HashSet<>();
                                for (int j = 0; j < tokens.length; j++) {
                                    frameMotions.add(tokens[j]);
                                }
                                videosFeatures[index].videoMotions.add(frameMotions);
                            }
                            break;
                        }
                        case "Audio": {
                            line = br.readLine();
                            int nFrames = Integer.parseInt(line);
                            for (int i = 0; i < nFrames; i++) {
                                line = br.readLine();
                                int frameAudios=Integer.parseInt(line);
                                videosFeatures[index].videoAudios.add(frameAudios);
                            }
                            break;
                        }
                        default: end = true; break;
                    }
                }
                index++;
            }
        } catch(Exception e) {
            
        } finally {
            try{
                if(br != null) br.close();
                if(fr != null) fr.close();
            } catch(Exception e) {
                
            }
        }
    }
    
    /**
     * Analyze given video
     * @param videoFeatures 
     */
    public void analyzeVideo(VideoFeatures videoFeatures) {
        // Extract color features
//        analyzeVideoColor(videoFeatures);
        
        // To do: extract motion features
        //analyzeVideoMotion(videoFeatures);
        
        // To do: extract audio features
        
        analyzeVideoAudio(videoFeatures);
    }
    
    public void analyzeVideoColor(VideoFeatures videoFeatures) {
        int totalFrames = videoFeatures.video.totalFrames;
        Video video = videoFeatures.video;
        for(int i = 1; i <= totalFrames; i++) {
            String imagePath = video.folder + "/" + video.videoName + String.format("%03d", i) + ".rgb";
            BufferedImage image = video.readImage(imagePath);
//            List<Integer> frameColors = colorExtractor.medianCut(image, nBuckets);
            List<Integer> frameColors = colorExtractor.uniformQuantization(image, 8, 8, 4);
            videoFeatures.videoColors.add(frameColors);
        }
    }
    
    public void analyzeVideoMotion(VideoFeatures videoFeatures) {
        int totalFrames = videoFeatures.video.totalFrames;
        Video video = videoFeatures.video;
        String imagePath = video.folder + "/" + video.videoName + "001.rgb";
        BufferedImage targetImage = null;
        BufferedImage candidateImage = video.readImage(imagePath);
        for(int i = 2; i <= totalFrames; i++) {
            targetImage = candidateImage;
            imagePath = video.folder + "/" + video.videoName + String.format("%03d", i) + ".rgb";
            candidateImage = video.readImage(imagePath);
            Set<String> frameMotions = motionExtractor.motionExtractor(targetImage, candidateImage);
            videoFeatures.videoMotions.add(frameMotions);
        }
    }
    public void analyzeVideoAudio(VideoFeatures videoFeatures)
    {
        Video video = videoFeatures.video;
        String audioPath=video.folder + "/" + video.videoName+".wav";
        List<Integer> frameAudios=audioExtractor.audioExtractor(audioPath);
        //System.out.println(video.videoName);
       // System.out.println(frameAudios);
        videoFeatures.videoAudios=frameAudios;
    }
    public void query(Video queriedVideo) {
        (new Thread(new QueryEngineThread(queriedVideo))).start();
    }
    
    public double distance(VideoFeatures queriedVideoFeatures, VideoFeatures candidateVideoFeatures) {
        // Calculate distance based on color features
//        List<Double> colorDistList = colorExtractor.colorDistance(queriedVideoFeatures.videoColors, candidateVideoFeatures.videoColors);
//        
//        double videoColorDist = Double.MAX_VALUE;
//        for(Double clipDist : colorDistList) {
//            videoColorDist = Math.min(videoColorDist, clipDist);
//        }
        
        // To do: calculate distance based on motion features
//        List<Double> motionDistList = motionExtractor.motionDistance(queriedVideoFeatures.videoMotions, candidateVideoFeatures.videoMotions); 
        
 //       double videoMotionDist = Double.MAX_VALUE;
 //       for(double clipDist : motionDistList) {
  //          videoMotionDist = Math.min(videoMotionDist, clipDist);
 //       }
        
        // To do: calculate distance based on audio features
         List<Double> audioDistList = audioExtractor.audioDistance(queriedVideoFeatures.videoAudios, candidateVideoFeatures.videoAudios); 
        
       double videoAudioDist = Double.MAX_VALUE;
       for(double clipDist : audioDistList) {
           videoAudioDist = Math.min(videoAudioDist, clipDist);
       }      
        
        
        
        // To do: based on distance calculated above to calculate an overall distance
        double videoDist = videoAudioDist;
        
        candidateVideoFeatures.dist = videoDist;
        return videoDist;
    }
    
    public void displayQueryResults(List<VideoFeatures> queryResults) {
        displayMain.displayQueryResults(queryResults);
    }
}
