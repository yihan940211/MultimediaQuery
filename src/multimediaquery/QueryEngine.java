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
import java.util.List;

/**
 *
 * @author yihanyang
 */
public class QueryEngine {
    class VideoFeatures {
        Video video;
        List<List<Integer>> videoColors;
        double dist;
        
        public VideoFeatures(Video video) {
            this.video = video;
            this.videoColors = new ArrayList<>();
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
    
    public QueryEngine(DisplayMain displayMain, String databasePath) {
        this.displayMain = displayMain;
        this.databasePath = databasePath;
        this.databaseFeaturesPath = this.databasePath + "features.txt";
        this.colorExtractor = new ColorExtractor();
        
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
                int nFrames = videoFeatures.videoColors.size();
                bw.write(Integer.toString(nFrames));
                bw.newLine();
                for(List<Integer> frameColors : videoFeatures.videoColors) {
                    StringBuilder frameColorsSB = new StringBuilder();
                    for(int color : frameColors) {
                        frameColorsSB.append(Integer.toString(color));
                        frameColorsSB.append(" ");
                    }
                    bw.write(frameColorsSB.toString());
                    bw.newLine();
                }
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
                int nFrames = Integer.parseInt(line);
                for(int i = 0; i < nFrames; i++) {
                    line = br.readLine();
                    String[] tokens = line.split(" ");
                    List<Integer> frameColors = new ArrayList<>();
                    for(int j = 0; j < tokens.length; j++) {
                        frameColors.add(Integer.parseInt(tokens[j]));
                    }
                    videosFeatures[index].videoColors.add(frameColors);
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
        analyzeVideoColor(videoFeatures);
        
        // To do: extract motion features
        
        // To do: extract audio features
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
    
    public void query(Video queriedVideo) {
        (new Thread(new QueryEngineThread(queriedVideo))).start();
    }
    
    public double distance(VideoFeatures queriedVideoFeatures, VideoFeatures candidateVideoFeatures) {
        // Calculate distance based on color features
        List<Double> colorDistList = colorExtractor.colorDistance(queriedVideoFeatures.videoColors, candidateVideoFeatures.videoColors);
        
        double videoDist = Double.MAX_VALUE;
        for(Double clipDist : colorDistList) {
            videoDist = Math.min(videoDist, clipDist);
        }
        
        // To do: calculate distance based on motion features
        
        // To do: calculate distance based on audio features
        
        // To do: based on distance calculated above to calculate an overall distance
        
        candidateVideoFeatures.dist = videoDist;
        return videoDist;
    }
    
    public void displayQueryResults(List<VideoFeatures> queryResults) {
        displayMain.displayQueryResults(queryResults);
    }
}
