/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaquery;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author yihanyang
 */
public class MotionExtractor {
    static final int threshold = 100;
    static final int blockWidth = 16;
    static final int blockHeight = 16;
    
    public List<String> motionExtractor(BufferedImage targetImage, BufferedImage candidateImage) {
        int height = targetImage.getHeight();
        int width = targetImage.getWidth();
        List<String> motions = new ArrayList<>();

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
        double diff = 0;
        
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
                
                diff += Math.abs(targetR - candidateR) + Math.abs(targetG - candidateG) + Math.abs(targetB - candidateB);
            }
        }
        
        return diff / (curBlockHeight * curBlockWidth);
    }
}
