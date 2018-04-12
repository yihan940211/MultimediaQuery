/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multimediaquery;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author yihanyang
 */
public class MotionExtractor {
    static final int threshold = 100;
    static final int blockWidth = 16;
    static final int blockHeight = 16;
    
    public Set<String> motionExtractor(BufferedImage targetImage, BufferedImage candidateImage) {
        int height = targetImage.getHeight();
        int width = targetImage.getWidth();
        Set<String> motions = new HashSet<>();

        for (int targetY = 0; targetY < height; targetY += blockHeight) {
            for(int targetX = 0; targetX < width; targetX += blockWidth) {
                double diff = MAD(targetImage, targetX, targetY, candidateImage, targetX, targetY);
                if(diff >= threshold) {
                    motions.add(targetX + "," + targetY);
                }
            }
        }
        
        return motions;
    }

    public double MAD(BufferedImage targetImage, int targetX, int targetY, BufferedImage candidateImage, int candidateX, int candidateY) {
        int curBlockHeight = Math.min(blockHeight, targetImage.getHeight() - targetY);
        int curBlockWidth = Math.min(blockWidth, targetImage.getWidth() - targetX);
        double dist = 0;
        
        for(int diffY = 0; diffY < curBlockHeight; diffY++) {
            for(int diffX = 0; diffX < curBlockWidth; diffX++) {
                int targetPixel = targetImage.getRGB(targetX + diffX, targetY + diffY);
                int targetR = ((targetPixel >>> 16) & (0xff));
                int targetG = ((targetPixel >>> 8) & (0xff));
                int targetB = (targetPixel & (0xff));
                
                int candidatePixel = candidateImage.getRGB(candidateX + diffX, candidateY + diffY);
                int candidateR = ((candidatePixel >>> 16) & (0xff));
                int candidateG = ((candidatePixel >>> 8) & (0xff));
                int candidateB = (candidatePixel & (0xff));
                
                dist += Math.abs(targetR - candidateR) + Math.abs(targetG - candidateG) + Math.abs(targetB - candidateB);
            }
        }
        
        return dist / (curBlockHeight * curBlockWidth);
    }
    
    public List<Double> motionDistance(List<Set<String>> queriedVideoMotions, List<Set<String>> candidateVideoMotions) {
        List<Double> distList = new ArrayList<>();
        int nFrame = queriedVideoMotions.size();
        int nTotalFrame = candidateVideoMotions.size();
        
        for(int cur = 0; cur < nTotalFrame - nFrame; cur++) {
            if(cur == 120) {
                int test = 0;
            }
            distList.add(motionDistance(queriedVideoMotions, candidateVideoMotions, cur));
        }
        
        return distList;
    } 
    
    public double motionDistance(List<Set<String>> queriedVideoMotions, List<Set<String>> candidateVideoMotions, int sFrame) {
        int dist = 0;
        int nFrame = queriedVideoMotions.size();
        for(int i = 0; i < nFrame; i++) {
            Set<String> queriedFrameMotions = queriedVideoMotions.get(i);
            Set<String> candidateFrameMotions = candidateVideoMotions.get(i + sFrame);
            int same = 0;
            for(String queriedMotion : queriedFrameMotions) {
                if(candidateFrameMotions.contains(queriedMotion))same++;
            }
            dist += queriedFrameMotions.size() + candidateFrameMotions.size() - 2 * same;
            if(dist != 0) {
                int test = 0;
            }
        }
        return dist * 1.0 / nFrame;
    }
}
