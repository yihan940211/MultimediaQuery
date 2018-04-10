/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multimediaquery;

import java.awt.image.BufferedImage;
import java.io.File;
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
//            analyzeVideo(queriedVideoFeatures);
            
            // Compare features of queried video and features of videos in database
            List<VideoFeatures> queryResults = new ArrayList<>();
            for (VideoFeatures candidateVideoFeatures : videosFeatures) {
//                distance(queriedVideoFeatures, candidateVideoFeatures);
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
    File[] videoFiles;
    int nVideos;
    VideoFeatures[] videosFeatures;
    
    ColorExtractor colorExtractor;
    final int nBuckets = 2;
    
    public QueryEngine(DisplayMain displayMain, String databasePath) {
        this.displayMain = displayMain;
        this.databasePath = databasePath;
        this.colorExtractor = new ColorExtractor();
        
        File file = new File(this.databasePath);
        videoFiles = file.listFiles();
        nVideos = videoFiles.length - 1;
        videosFeatures = new VideoFeatures[nVideos];
        for(int i = 0; i < nVideos; i++) {
            Video video = new Video(this.displayMain, videoFiles[i + 1].getAbsolutePath(), 2);
            videosFeatures[i] = new VideoFeatures(video);
        }
        analyzeDatabase();
    }
    
    /**
     * Analyze all videos in database when launching this program
     */
    public void analyzeDatabase() {
        for(VideoFeatures videoFeatures : videosFeatures) {
//            analyzeVideo(videoFeatures);
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
            List<Integer> frameColors = colorExtractor.medianCut(image, nBuckets);
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
