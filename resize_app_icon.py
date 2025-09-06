#!/usr/bin/env python3
"""
Create properly sized app icon foreground from applogo.png
For adaptive icon system (108dp canvas with safe area)
"""

from PIL import Image, ImageDraw
import os

def create_app_icon_foreground():
    # Input and output paths
    input_path = "app/src/main/res/drawable/applogo.png"
    output_path = "app/src/main/res/drawable/ic_launcher_foreground_resized.png"
    
    # Adaptive icon specs
    CANVAS_SIZE = 432  # 108dp at xxxhdpi (4x) density
    SAFE_AREA = int(CANVAS_SIZE * 0.66)  # Safe area is 66% of canvas
    
    print(f"Resizing logo for adaptive icon: {input_path}")
    
    # Load the source logo
    logo = Image.open(input_path)
    print(f"Original logo size: {logo.size}")
    
    # Create transparent canvas
    canvas = Image.new("RGBA", (CANVAS_SIZE, CANVAS_SIZE), (0, 0, 0, 0))
    
    # Calculate scaling to fit logo within safe area
    logo_ratio = min(SAFE_AREA / logo.width, SAFE_AREA / logo.height)
    new_logo_size = (int(logo.width * logo_ratio), int(logo.height * logo_ratio))
    
    print(f"Scaling ratio: {logo_ratio:.3f}")
    print(f"New logo size: {new_logo_size}")
    
    # Resize logo with high quality
    logo_resized = logo.resize(new_logo_size, Image.Resampling.LANCZOS)
    
    # Center the logo on the canvas with fine-tuning for perfect centering
    x_offset = (CANVAS_SIZE - new_logo_size[0]) // 2
    y_offset = (CANVAS_SIZE - new_logo_size[1]) // 2
    
    # Fine-tune positioning to ensure perfect centering
    # Move slightly up and left to compensate for visual weight
    x_offset -= 2  # Adjust horizontally 
    y_offset -= 5  # Adjust vertically to account for text being heavier at bottom
    
    print(f"Positioning at: ({x_offset}, {y_offset})")
    
    # Paste logo onto transparent canvas
    canvas.paste(logo_resized, (x_offset, y_offset), logo_resized if logo_resized.mode == 'RGBA' else None)
    
    # Save as PNG with transparency
    canvas.save(output_path, "PNG", optimize=True)
    
    print(f"App icon foreground created: {output_path}")
    print(f"Canvas size: {CANVAS_SIZE}x{CANVAS_SIZE} pixels")
    print(f"Safe area used: {SAFE_AREA}x{SAFE_AREA} pixels")
    
    return output_path

if __name__ == "__main__":
    try:
        result = create_app_icon_foreground()
        print("\nApp icon foreground generation SUCCESSFUL!")
        print(f"Use @drawable/ic_launcher_foreground_resized in adaptive icon")
    except Exception as e:
        print(f"Error creating app icon foreground: {e}")
        exit(1)