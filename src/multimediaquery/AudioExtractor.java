/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multimediaquery;
import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
/**
 *Extract Frequency from the audio
 * @author Xiaochi
 */
public class AudioExtractor {
    public static final int window_size = 1024;

    /**
     * this fft function was implmented by radix 2 Cooley-Tukey FFT.
     * Transforming the complex number to FFT values.
     *
     *
     * @param x
     *            incoming complex numbers, the length must be power of 2.
     * @return FFT values
     */
    public static Complex[] fastFourierTransform(Complex[] x) {
        int n = x.length;

        // base case
        if (n == 1) {
            return new Complex[] { x[0] };
        }

        // calculate even terms
        Complex[] even = new Complex[n / 2];
        for (int i = 0; i < n / 2; i++) {
            even[i] = x[2 * i];
        }
        Complex[] even_result = fastFourierTransform(even);

        // fft of odd terms
        Complex[] odd = even;
        for (int i = 0; i < n / 2; i++) {
            odd[i] = x[2 * i + 1];
        }
        Complex[] odd_result = fastFourierTransform(odd);

        //y_{k+(N/2)}=F_even(k)-wk*F_odd(k)
        //y_k=F_even(k)+wk*F_odd(k)
        //w_(k+N)=w_k
        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;//kth number in the N of 2pi
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = even_result[k].plus(wk.times(odd_result[k]));
            y[k + n / 2] = even_result[k].minus(wk.times(odd_result[k]));
        }
        return y;
    }

    /**
     * Calculate the distance between target audio and candidate audio
     * dist=avg(min(sum(Math.abs(candidate-target))))
     *
     * @param target
     *            the target video we are going to search
     * @param candidate
     *            the video in the databases
     * @return distance
     */
    public static double findDistance(List<Integer> target, List<Integer> candidate) {
        int t_length = target.size();
        int c_length = candidate.size();
        int min = Integer.MAX_VALUE;
        for (int i = 0; i <= c_length - t_length; i++) {
            int sum = 0;
            for (int j = 0; j < t_length; j++) {
                sum += Math.abs(target.get(j) - candidate.get(i + j));
            }
            min = Math.min(sum, min);
        }

        return min * 1.0 / t_length;
    }

    /**
     * calculate the major frequency in incoming pcm_signal.
     *
     * @param pcm_signal
     *            the byte value read from audio
     * @return the index of major frequency
     *
     */
    public static int calculateFFT(byte[] pcm_signal) {

        double temp;
        Complex[] y;
        Complex[] complexSignal = new Complex[window_size];
        double[] absSignal = new double[window_size / 2];

        for (int i = 0; i < window_size; i++) {
            temp = (double) ((pcm_signal[2 * i] & 0xFF) | (pcm_signal[2 * i + 1] << 8)) / 32768.0F;
            complexSignal[i] = new Complex(temp, 0.0);
        }

        y = fastFourierTransform(complexSignal);
        double maxSample = 0.0;
        int maxPos = 0;
        for (int i = 0; i < (window_size / 2); i++) {
            absSignal[i] = Math.sqrt(Math.pow(y[i].real_part(), 2) + Math.pow(y[i].imagin_part(), 2));
            if (absSignal[i] > maxSample) {
                maxSample = absSignal[i];
                maxPos = i;
            }
        }
        //return the max index for major frequency
        //freq=index*simpleRate/window_size
        //0:   0 * 44100 / 1024 =     0.0 Hz
        //1:   1 * 44100 / 1024 =    43.1 Hz
        //511: 511 * 44100 / 1024 = 22006.9 Hz
        return maxPos;
    }

    /**
     * extract the frequency per window_size from the audio file.
     *
     * @param fileName
     * @return the frequency changes in the audio file.
     * @throws UnsupportedAudioFileException
     * @throws IOException
     */
    public static List<Integer> readFile(String fileName) throws UnsupportedAudioFileException, IOException {
        File file = new File(fileName);
        AudioInputStream in = AudioSystem.getAudioInputStream(file);
        byte[] data = new byte[window_size * in.getFormat().getSampleSizeInBits() / 8];
        int read = 0;
        List<Integer> freq = new ArrayList<Integer>();
        while ((read = in.read(data)) > 0) {
            int index = calculateFFT(data);
            freq.add(index);
        }
        return freq;
        //1722 frames for database videos, 432 frames for query videos
    }

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {

        String filename = "./audio/movie.wav";
        List<Integer> candidate = readFile(filename);
        String filename2 = "./audio/first.wav";
        List<Integer> target = readFile(filename2);
        double dist = findDistance(target, candidate);
        System.out.println(dist);
    }
}
