package com.aetheriusmmorpg.client.video;

import com.aetheriusmmorpg.AetheriusMod;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Utility class to convert video files to frame sequences.
 *
 * Usage Instructions:
 * 1. Use FFmpeg to extract frames from your video:
 *    ffmpeg -i intro_video.mp4 -vf fps=30 -s 1920x1080 frame_%04d.png
 *
 * 2. Place the extracted frames in:
 *    src/main/resources/assets/aetherius/textures/intro_video/
 *
 * 3. Frames should be named: frame_0001.png, frame_0002.png, etc.
 *
 * For best results:
 * - Use 30 FPS for smooth playback
 * - Recommended resolution: 1920x1080 (Full HD)
 * - Keep video length under 30 seconds (900 frames max)
 * - Use PNG format for quality, or JPG for smaller file size
 *
 * Alternative: Compress frames using this utility before packaging
 */
public class VideoConverter {

    /**
     * Converts a directory of images to optimized frames.
     * Can be run as a standalone utility during development.
     */
    public static void optimizeFrames(File inputDir, File outputDir, int targetWidth, int targetHeight) {
        if (!inputDir.exists() || !inputDir.isDirectory()) {
            AetheriusMod.LOGGER.error("Input directory does not exist: {}", inputDir);
            return;
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File[] files = inputDir.listFiles((dir, name) ->
            name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));

        if (files == null || files.length == 0) {
            AetheriusMod.LOGGER.warn("No image files found in {}", inputDir);
            return;
        }

        // Sort files by name
        java.util.Arrays.sort(files);

        int frameNumber = 1;
        for (File file : files) {
            try {
                BufferedImage original = ImageIO.read(file);
                if (original == null) continue;

                // Resize if needed
                BufferedImage scaled;
                if (original.getWidth() != targetWidth || original.getHeight() != targetHeight) {
                    scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                    java.awt.Graphics2D g2d = scaled.createGraphics();
                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
                    g2d.dispose();
                } else {
                    scaled = original;
                }

                // Save optimized frame
                File outputFile = new File(outputDir, String.format("frame_%04d.png", frameNumber));
                ImageIO.write(scaled, "PNG", outputFile);

                frameNumber++;
                AetheriusMod.LOGGER.info("Processed frame {}: {}", frameNumber - 1, file.getName());

            } catch (IOException e) {
                AetheriusMod.LOGGER.error("Failed to process frame: {}", file.getName(), e);
            }
        }

        AetheriusMod.LOGGER.info("Frame optimization complete. Processed {} frames", frameNumber - 1);
    }

    /**
     * Main method for standalone frame conversion utility.
     * Run this from your IDE during development.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: VideoConverter <input_dir> <output_dir> [width] [height]");
            System.out.println("Example: VideoConverter ./raw_frames ./optimized_frames 1920 1080");
            return;
        }

        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        int width = args.length > 2 ? Integer.parseInt(args[2]) : 1920;
        int height = args.length > 3 ? Integer.parseInt(args[3]) : 1080;

        optimizeFrames(inputDir, outputDir, width, height);
    }
}
