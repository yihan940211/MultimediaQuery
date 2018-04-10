/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multimediaquery;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author yihanyang
 */
public class ColorExtractor {
    public List<Integer> medianCut(BufferedImage image, int q){
        int height = image.getHeight();
        int width = image.getWidth();
        List<String> bucket = new ArrayList<>();
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int pixel = image.getRGB(x, y);
                bucket.add(pixel + ":" + x + "," + y);
            }
        }
        List<List<String>> buckets = new ArrayList<>();
        buckets.add(bucket);
        buckets = medianCutHelper(buckets, q);
        buckets = medianCutHelper(buckets, q);
        buckets = medianCutHelper(buckets, q);
        
        List<Integer> colors = new ArrayList<>();
        for(List<String> tempBucket : buckets) {
            long totalR = 0, totalG = 0, totalB = 0;
            for(String s : tempBucket) {
                int i = Integer.parseInt(s.split(":")[0]);
                int r = ((i >>> 16) & (0xff));
                int g = ((i >>> 8) & (0xff));
                int b = (i & (0xff));
                totalR += r;
                totalG += g;
                totalB += b;
            }
            int r = (int)Math.round(totalR * 1.0 / tempBucket.size());
            int g = (int)Math.round(totalG * 1.0 / tempBucket.size());
            int b = (int)Math.round(totalB * 1.0 / tempBucket.size());
            int pixel = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
            colors.add(pixel);
        }
        
        return colors;
    }
    
    public List<List<String>> medianCutHelper(List<List<String>> buckets, int q){
        List<List<String>> res = new ArrayList<>();
        for(List<String> bucket : buckets) {
            medianCutSort(bucket);
            int unit  = bucket.size() / q;
            for(int i = 0; i < q; i++) {
                if(i != q - 1){
                    List<String> temp = new ArrayList<>(bucket.subList(i * unit, (i + 1) * unit));
                    res.add(temp);
                }else{
                    List<String> temp = new ArrayList<>(bucket.subList(i * unit, bucket.size()));
                    res.add(temp);
                }
            }
        }
        return res;
    }
    
    public void medianCutSort(List<String> bucket){
        int rmin = 255, gmin = 255, bmin = 255;
        int rmax = 0, gmax = 0, bmax = 0;
        for(String s : bucket){
            int i = Integer.parseInt(s.split(":")[0]);
            int r = ((i >>> 16) & (0xff));
            int g = ((i >>> 8) & (0xff));
            int b = (i & (0xff));
            rmin = Math.min(rmin, r);
            rmax = Math.max(rmax, r);
            gmin = Math.min(gmin, g);
            gmax = Math.max(gmax, g);
            bmin = Math.min(bmin, b);
            bmax = Math.max(bmax, b);
        }
        
        if(rmax - rmin >= gmax - gmin){
            if(rmax - rmin >= bmax - bmin){
                if(bmax - bmin >= gmax - gmin){
                    bucket.sort(new RGBCMP("RBG"));
                }else{
                    bucket.sort(new RGBCMP("RGB"));
                }
            }else{
                bucket.sort(new RGBCMP("BRG"));
            }
        }else{
            if(gmax - gmin >= bmax - bmin){
                if(bmax - bmin > rmax - rmin){
                    bucket.sort(new RGBCMP("GBM"));
                }else{
                    bucket.sort(new RGBCMP("GRB"));
                }
            }else{
                bucket.sort(new RGBCMP("BGR"));
            }
        }
    }
    
    class RGBCMP implements Comparator<String> {

        private char[] relation;

        public RGBCMP(String relation) {
            super();
            this.relation = new char[3];
            this.relation[0] = relation.charAt(0);
            this.relation[1] = relation.charAt(1);
            this.relation[2] = relation.charAt(2);
        }

        public int compare(String s1, String s2) {
            int i1 = Integer.parseInt(s1.split(":")[0]);
            int i2 = Integer.parseInt(s2.split(":")[0]);

            int r1 = ((i1 >>> 16) & (0xff));
            int g1 = ((i1 >>> 8) & (0xff));
            int b1 = (i1 & (0xff));

            int r2 = ((i2 >>> 16) & (0xff));
            int g2 = ((i2 >>> 8) & (0xff));
            int b2 = (i2 & (0xff));

            if (relation[0] == 'R') {
                if (r1 != r2) {
                    return r2 - r1;
                } else if (relation[1] == 'G') {
                    if (g1 != g2) {
                        return g2 - g1;
                    } else {
                        return b2 - b1;
                    }
                } else if (b1 != b2) {
                    return b2 - b1;
                } else {
                    return g2 - g1;
                }
            } else if (relation[1] == 'G') {
                if (g1 != g2) {
                    return g2 - g1;
                } else if (relation[1] == 'R') {
                    if (r1 != r2) {
                        return r2 - r1;
                    } else {
                        return b2 - b1;
                    }
                } else if (b1 != b2) {
                    return b2 - b1;
                } else {
                    return r2 - r1;
                }
            } else if (relation[2] == 'B') {
                if (b1 != b2) {
                    return b2 - b1;
                } else if (relation[1] == 'G') {
                    if (g1 != g2) {
                        return g2 - g1;
                    } else {
                        return r2 - r1;
                    }
                } else if (r1 != r2) {
                    return r2 - r1;
                } else {
                    return g2 - g1;
                }
            }

            return 0;
        }
    }
    
    public List<Double> colorDistance(List<List<Integer>> queriedVideoColors, List<List<Integer>> candidateVideoColors) {
        List<Double> distList = new ArrayList<>();
        int nFrame = queriedVideoColors.size();
        int nTotalFrame = candidateVideoColors.size();
        
        for(int cur = 0; cur < nTotalFrame - nFrame; cur++) {
            distList.add(colorDistance(queriedVideoColors, candidateVideoColors, cur));
        }
        
        return distList;
    } 
    
    public double colorDistance(List<List<Integer>> queriedVideoColors, List<List<Integer>> candidateVideoColors, int sFrame) {
        double dist = 0;
        int nFrame = queriedVideoColors.size();
        int nColors = queriedVideoColors.get(0).size();
        for(int i = 0; i < nFrame; i++) {
            List<Integer> queriedFrameColors = queriedVideoColors.get(i);
            List<Integer> candidateFrameColors = candidateVideoColors.get(i + sFrame);
            for(int j = 0; j < nColors; j++) {
                int queriedColor = queriedFrameColors.get(j);
                int queriedR = ((queriedColor >>> 16) & (0xff));
                int queriedG = ((queriedColor >>> 8) & (0xff));
                int queriedB = (queriedColor & (0xff));
                
                int candidateColor = candidateFrameColors.get(j);
                int candidateR = ((candidateColor >>> 16) & (0xff));
                int candidateG = ((candidateColor >>> 8) & (0xff));
                int candidateB = (candidateColor & (0xff));
                
                dist = dist + Math.abs(queriedR - candidateR) + Math.abs(queriedG - candidateG) + Math.abs(queriedB - candidateB);
            }
        }
        return dist / (nFrame * nColors);
    }
}
