/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mediaquery;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author yihanyang
 */
public class MotionExtractorTest {
    final int width = 352;
    final int height = 288;
    
    public BufferedImage readImage(String path) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        try {
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

                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    image.setRGB(x,y,pix);
                    ind++;
                }
            }
        } catch(Exception e) {
            
        }
        
        return image;
    }
    
    public void displayImage(BufferedImage targetImage, BufferedImage candidateImage) {
        JFrame frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);
        
        JLabel lbText1 = new JLabel("Target Image (Left)");
        lbText1.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel lbText2 = new JLabel("Candidate Image (Right)");
        lbText2.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel lbIm1 = new JLabel(new ImageIcon(targetImage));
        JLabel lbIm2 = new JLabel(new ImageIcon(candidateImage));
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        frame.getContentPane().add(lbText1, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        frame.getContentPane().add(lbText2, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        frame.getContentPane().add(lbIm2, c);

        frame.pack();
        frame.setVisible(true);
    }
    
    public BufferedImage showMotions(BufferedImage image, List<String> motions) {
        int blockWidth = MotionExtractor.blockWidth;
        int blockHeight = MotionExtractor.blockHeight;
        BufferedImage resImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for(String motion : motions) {
            int targetX = Integer.parseInt(motion.split(",")[0]);
            int targetY = Integer.parseInt(motion.split(",")[1]);
            
            for(int diffY = 0; diffY < blockHeight; diffY++) {
                for(int diffX = 0; diffX < blockWidth; diffX++) {
                    int pixel = image.getRGB(targetX + diffX, targetY + diffY);
                    resImage.setRGB(targetX + diffX, targetY + diffY, pixel);
                }
            }
        }
        
        return resImage;
    }

    public static void main(String[] args) {
        String folderPath = "/Users/yihanyang/Documents/USCCourse/CSCI576/Project/query/second";
        String targetImagePath = folderPath + "/second010.rgb";
        String candidateImagePath = folderPath + "/second011.rgb";
        MotionExtractorTest test = new MotionExtractorTest();
        MotionExtractor motionExtractor = new MotionExtractor();
        
        BufferedImage targetImage = test.readImage(targetImagePath);
        BufferedImage candidateImage = test.readImage(candidateImagePath);
        List<String> motions = motionExtractor.motionExtractor(targetImage, candidateImage);
        BufferedImage targetMotionsImage = test.showMotions(targetImage, motions);
        BufferedImage candidateMotionsImage = test.showMotions(candidateImage, motions);
        test.displayImage(targetImage, candidateImage);
        test.displayImage(targetMotionsImage, candidateMotionsImage);
    }
}
