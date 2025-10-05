package com.aetheriusmmorpg.client.ui.screen;

import com.aetheriusmmorpg.AetheriusMod;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Intro video screen displayed when player first joins.
 * Plays a video file with skip functionality enabled after 10 seconds.
 *
 * Video Format Support:
 * - Frame sequence (PNG/JPG images in sequence)
 * - Custom video format using pre-extracted frames
 */
public class IntroVideoScreen extends Screen {

    private static final int SKIP_DELAY_SECONDS = 10;
    private static final float FRAMES_PER_SECOND = 30.0f;

    private final Screen nextScreen;
    private final List<VideoFrame> frames = new ArrayList<>();
    private int currentFrame = 0;
    private long startTime;
    private boolean canSkip = false;
    private boolean videoLoaded = false;

    private Button skipButton;
    private int textureId = -1;
    private int videoWidth;
    private int videoHeight;

    public IntroVideoScreen(Screen nextScreen) {
        super(Component.literal("Aetherius Intro"));
        this.nextScreen = nextScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.startTime = System.currentTimeMillis();

        // Create skip button (initially disabled)
        this.skipButton = Button.builder(
            Component.literal("Skip (10s)"),
            btn -> skipVideo()
        ).bounds(this.width - 110, this.height - 40, 100, 20).build();

        this.skipButton.active = false;
        this.addRenderableWidget(skipButton);

        // Load video frames
        if (!videoLoaded) {
            loadVideo();
        }
    }

    private void loadVideo() {
        try {
            // Try to load frame sequence from resources
            // Format: intro_video/frame_0001.png, frame_0002.png, etc.
            int frameNumber = 1;
            boolean hasMoreFrames = true;

            while (hasMoreFrames && frameNumber <= 900) { // Max 30 seconds at 30fps
                String framePath = String.format("textures/intro_video/frame_%04d.png", frameNumber);
                ResourceLocation frameLocation = new ResourceLocation(AetheriusMod.MOD_ID, framePath);

                try (InputStream stream = Minecraft.getInstance().getResourceManager()
                        .getResource(frameLocation).get().open()) {

                    BufferedImage image = ImageIO.read(stream);
                    if (image != null) {
                        frames.add(new VideoFrame(image));
                        if (frameNumber == 1) {
                            videoWidth = image.getWidth();
                            videoHeight = image.getHeight();
                        }
                        frameNumber++;
                    } else {
                        hasMoreFrames = false;
                    }
                } catch (Exception e) {
                    hasMoreFrames = false;
                }
            }

            if (frames.isEmpty()) {
                AetheriusMod.LOGGER.warn("No intro video frames found, using placeholder");
                createPlaceholderVideo();
            } else {
                AetheriusMod.LOGGER.info("Loaded {} video frames", frames.size());
                videoLoaded = true;
            }

        } catch (Exception e) {
            AetheriusMod.LOGGER.error("Failed to load intro video", e);
            createPlaceholderVideo();
        }
    }

    private void createPlaceholderVideo() {
        // Create a simple animated placeholder (60 frames = 2 seconds at 30fps)
        for (int i = 0; i < 60; i++) {
            BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2d = image.createGraphics();

            // Fade in effect
            float alpha = Math.min(1.0f, i / 30.0f);
            g2d.setColor(new java.awt.Color(0, 0, 0));
            g2d.fillRect(0, 0, 1920, 1080);

            g2d.setColor(new java.awt.Color(1.0f, 1.0f, 1.0f, alpha));
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 72));
            String text = "AETHERIUS";
            java.awt.FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            g2d.drawString(text, (1920 - textWidth) / 2, 540);

            g2d.dispose();
            frames.add(new VideoFrame(image));
        }

        videoWidth = 1920;
        videoHeight = 1080;
        videoLoaded = true;
    }

    @Override
    public void tick() {
        super.tick();

        long elapsedTime = System.currentTimeMillis() - startTime;
        long elapsedSeconds = elapsedTime / 1000;

        // Enable skip button after delay
        if (!canSkip && elapsedSeconds >= SKIP_DELAY_SECONDS) {
            canSkip = true;
            skipButton.active = true;
            skipButton.setMessage(Component.literal("Skip"));
        }

        // Update skip button countdown
        if (!canSkip) {
            long remaining = SKIP_DELAY_SECONDS - elapsedSeconds;
            skipButton.setMessage(Component.literal("Skip (" + remaining + "s)"));
        }

        // Update current frame based on elapsed time
        if (!frames.isEmpty()) {
            float frameTime = elapsedTime / 1000.0f * FRAMES_PER_SECOND;
            currentFrame = (int) frameTime;

            // Video finished
            if (currentFrame >= frames.size()) {
                finishVideo();
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render black background
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF000000);

        // Render current video frame
        if (!frames.isEmpty() && currentFrame < frames.size()) {
            VideoFrame frame = frames.get(currentFrame);
            renderVideoFrame(guiGraphics, frame);
        }

        // Render UI elements
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderVideoFrame(GuiGraphics guiGraphics, VideoFrame frame) {
        // Calculate aspect ratio scaling
        float videoAspect = (float) videoWidth / videoHeight;
        float screenAspect = (float) this.width / this.height;

        int renderWidth, renderHeight, renderX, renderY;

        if (screenAspect > videoAspect) {
            // Screen is wider than video
            renderHeight = this.height;
            renderWidth = (int) (renderHeight * videoAspect);
            renderX = (this.width - renderWidth) / 2;
            renderY = 0;
        } else {
            // Screen is taller than video
            renderWidth = this.width;
            renderHeight = (int) (renderWidth / videoAspect);
            renderX = 0;
            renderY = (this.height - renderHeight) / 2;
        }

        // Upload frame to texture if needed
        if (textureId == -1) {
            textureId = GL11.glGenTextures();
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
            frame.width, frame.height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, frame.data);

        // Render textured quad
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, textureId);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(renderX, renderY + renderHeight, 0).uv(0, 1).endVertex();
        buffer.vertex(renderX + renderWidth, renderY + renderHeight, 0).uv(1, 1).endVertex();
        buffer.vertex(renderX + renderWidth, renderY, 0).uv(1, 0).endVertex();
        buffer.vertex(renderX, renderY, 0).uv(0, 0).endVertex();
        tesselator.end();
    }

    private void skipVideo() {
        finishVideo();
    }

    private void finishVideo() {
        cleanup();

        if (nextScreen != null) {
            this.minecraft.setScreen(nextScreen);
        } else {
            this.minecraft.setScreen(null);
        }
    }

    private void cleanup() {
        if (textureId != -1) {
            GL11.glDeleteTextures(textureId);
            textureId = -1;
        }
        frames.clear();
    }

    @Override
    public void removed() {
        super.removed();
        cleanup();
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Don't pause the game
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Allow ESC to skip if enabled
        if (keyCode == 256 && canSkip) { // ESC key
            skipVideo();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Represents a single video frame with pixel data.
     */
    private static class VideoFrame {
        final int width;
        final int height;
        final ByteBuffer data;

        VideoFrame(BufferedImage image) {
            this.width = image.getWidth();
            this.height = image.getHeight();

            // Convert BufferedImage to ByteBuffer (RGBA format)
            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);

            this.data = ByteBuffer.allocateDirect(width * height * 4);
            for (int pixel : pixels) {
                data.put((byte) ((pixel >> 16) & 0xFF)); // Red
                data.put((byte) ((pixel >> 8) & 0xFF));  // Green
                data.put((byte) (pixel & 0xFF));         // Blue
                data.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
            }
            data.flip();
        }
    }
}
