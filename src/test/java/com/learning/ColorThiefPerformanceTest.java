package com.learning;

import com.learning.colorthief.ColorThief;
import com.learning.colorthief.MMCQ;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ColorThiefPerformanceTest
{

    private static final int NUM_TESTS_WARMUP = 500;
    private static final int NUM_TESTS = 500;

    public static void main(String[] args) throws Exception
    {
        BufferedImage img1 = ImageIO.read(new File("examples/img/photo1.jpg"));
        BufferedImage img2 = ImageIO.read(new File("examples/img/photo2.jpg"));
        BufferedImage img3 = ImageIO.read(new File("examples/img/photo3.jpg"));

        // Warm up JIT
        System.out.println("Warming up...");
        test(img1, img2, img3, NUM_TESTS_WARMUP);

        // Test
        System.out.println("Testing...");
        long start = System.currentTimeMillis();
        test(img1, img2, img3, NUM_TESTS);
        long end = System.currentTimeMillis();
        long total = end - start;
        System.out.println("Total time = " + total + " ms / per image = "
                + ((double) total / NUM_TESTS / 3) + " ms");
    }

    private static void test(
            BufferedImage img1,
            BufferedImage img2,
            BufferedImage img3,
            int max) throws IOException
    {
        long sum = 0;

        for (int i = 0; i < max; i++)
        {
            if (i % 100 == 0)
            {
                System.out.println("Round " + (i + 1) + " of " + max + "...");
            }

            MMCQ.CMap result = ColorThief.getColorMap(img1, 10);
            sum += result.vboxes.size();

            result = ColorThief.getColorMap(img2, 10);
            sum += result.vboxes.size();

            result = ColorThief.getColorMap(img3, 10);
            sum += result.vboxes.size();
        }

        // The sum is calculated (and printed) so that the JIT doesn't think the
        // result is never used and optimizes the whole method calls away ;)
        System.out.println("Finished (sum = " + sum + ")");
    }

}
