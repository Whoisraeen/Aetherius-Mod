# Aetherius Intro Video Setup Guide

## Overview

The Aetherius mod supports custom intro videos that play when players first join the server. The video system uses a frame-by-frame approach for maximum compatibility and control.

## Video Format

The intro video is stored as a sequence of PNG images (frames) rather than a traditional video file. This approach:
- Works perfectly with Minecraft's resource system
- Requires no external libraries or codecs
- Provides frame-perfect control
- Supports any resolution and length

## Preparing Your Video

### Step 1: Create Your Video

Create your intro video using any video editing software. Recommended specifications:
- **Resolution**: 1920x1080 (Full HD)
- **Frame Rate**: 30 FPS
- **Duration**: 10-30 seconds (300-900 frames)
- **Format**: MP4, AVI, MOV, or any format supported by FFmpeg

### Step 2: Extract Frames with FFmpeg

Use FFmpeg to convert your video to a frame sequence:

```bash
ffmpeg -i your_intro_video.mp4 -vf fps=30 -s 1920x1080 frame_%04d.png
```

**Command Breakdown:**
- `-i your_intro_video.mp4` - Input video file
- `-vf fps=30` - Extract at 30 frames per second
- `-s 1920x1080` - Scale to 1920x1080 resolution
- `frame_%04d.png` - Output file naming (frame_0001.png, frame_0002.png, etc.)

**Alternative with quality control:**
```bash
ffmpeg -i your_intro_video.mp4 -vf "fps=30,scale=1920:1080:flags=lanczos" -compression_level 9 frame_%04d.png
```

### Step 3: Optimize Frame Size (Optional)

To reduce mod file size, you can compress frames:

```bash
# Using ImageMagick
mogrify -quality 85% -format jpg *.png

# Or convert PNG to optimized PNG
optipng *.png
```

### Step 4: Place Frames in Resources

1. Navigate to your mod's resources directory:
   ```
   src/main/resources/assets/aetherius/textures/
   ```

2. Create the intro_video directory:
   ```
   mkdir intro_video
   ```

3. Copy all extracted frames to this directory:
   ```
   src/main/resources/assets/aetherius/textures/intro_video/
       ├── frame_0001.png
       ├── frame_0002.png
       ├── frame_0003.png
       └── ...
   ```

## Frame Naming Convention

**IMPORTANT**: Frames must be named exactly as follows:
- Format: `frame_XXXX.png` where XXXX is a 4-digit number
- Examples: `frame_0001.png`, `frame_0002.png`, `frame_0150.png`
- Start numbering at 0001
- Use leading zeros (frame_0001.png, NOT frame_1.png)

## Video Specifications

### Recommended Settings

| Setting | Value | Notes |
|---------|-------|-------|
| Resolution | 1920x1080 | Full HD, best quality |
| Frame Rate | 30 FPS | Smooth playback |
| Max Frames | 900 | 30 seconds at 30 FPS |
| Format | PNG | Best quality |
| Alternative | JPG | Smaller file size |

### Resolution Options

- **1920x1080** (Full HD) - Recommended for best quality
- **1280x720** (HD) - Good balance of quality and file size
- **1600x900** - Alternative 16:9 resolution
- **Custom** - Any resolution, will be scaled to fit screen

## File Size Considerations

Each frame adds to your mod's file size:
- PNG (1920x1080): ~500KB-2MB per frame
- JPG (1920x1080): ~100KB-300KB per frame
- 30 second video (900 frames): 90MB-1.8GB (PNG) or 90MB-270MB (JPG)

**Tips to Reduce Size:**
1. Use JPG instead of PNG for smaller files
2. Reduce resolution (720p uses ~44% less space than 1080p)
3. Compress frames with ImageMagick or similar tools
4. Keep video length under 20 seconds
5. Use lower frame rate (24 FPS instead of 30 FPS)

## Advanced: Using the VideoConverter Utility

The mod includes a VideoConverter utility for batch processing frames:

```java
// Run this during development
VideoConverter.optimizeFrames(
    new File("./raw_frames"),        // Input directory
    new File("./optimized_frames"),  // Output directory
    1920,                            // Target width
    1080                             // Target height
);
```

Or run from command line:
```bash
java -cp aetherius.jar com.aetheriusmmorpg.client.video.VideoConverter ./raw_frames ./optimized_frames 1920 1080
```

## Testing Your Video

1. Build your mod with the frames included
2. Run Minecraft with your mod
3. Join a server/world as a new player (or delete your player data)
4. The intro video should play automatically
5. Skip button becomes active after 10 seconds
6. Press ESC or click Skip to skip to character creation

## Troubleshooting

### Video Not Playing
- Check that frames are in the correct directory
- Verify frame naming follows the convention (frame_0001.png)
- Check console for error messages
- Ensure at least frame_0001.png exists

### Video Stuttering
- Reduce frame rate (try 24 FPS instead of 30 FPS)
- Reduce resolution
- Compress frames to reduce file size

### Skip Button Not Working
- The skip button activates after 10 seconds
- Check that canSkip is being set to true
- Verify ESC key handling is working

### Video Too Large
- Use JPG instead of PNG
- Reduce resolution to 720p or lower
- Shorten video duration
- Compress frames with optimization tools

## Example Workflow

Here's a complete workflow from video to game:

```bash
# 1. Extract frames from video
ffmpeg -i intro.mp4 -vf "fps=30,scale=1920:1080" frame_%04d.png

# 2. (Optional) Convert to JPG for smaller size
mogrify -format jpg -quality 85 *.png
rm *.png  # Remove original PNGs

# 3. Rename if using JPG
rename 's/\.jpg$/.png/' *.jpg  # Convert .jpg extension to .png in filenames

# 4. Move frames to mod resources
mv frame_*.png /path/to/mod/src/main/resources/assets/aetherius/textures/intro_video/

# 5. Build and test
./gradlew build
```

## Custom Implementation

To use the intro video system programmatically:

```java
// Show intro video with no screen after
NetworkHandler.sendToPlayer(new S2COpenIntroVideoPacket(false), serverPlayer);

// Show intro video followed by character creation
NetworkHandler.sendToPlayer(new S2COpenIntroVideoPacket(true), serverPlayer);
```

## Credits

The intro video system uses:
- LWJGL for OpenGL rendering
- Java AWT for image processing
- Minecraft's resource system for loading
- Custom frame-by-frame playback engine

## Support

For issues or questions:
1. Check the console for error messages
2. Verify your frame sequence is correct
3. Test with the placeholder video first (no frames needed)
4. Check the AetheriusMod.log for detailed information
