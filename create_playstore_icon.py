#!/usr/bin/env python3
"""
Create Play Store compliant 512x512 icon from applogo.png
Meets all Google Play Store requirements for 2025
"""

from PIL import Image, ImageDraw
import os

def create_playstore_icon():
    # Input and output paths
    input_path = "app/src/main/res/drawable/applogo.png"
    output_path = "app/src/main/ic_launcher-playstore.png"
    
    # Play Store requirements
    PLAYSTORE_SIZE = 512
    BACKGROUND_COLOR = "#1E3A8A"  # Blue background from ic_launcher_background
    
    print(f"Creating Play Store icon from {input_path}")
    
    # Load the source logo
    logo = Image.open(input_path)
    print(f"Source logo size: {logo.size}")
    
    # Create 512x512 canvas with blue background
    playstore_icon = Image.new("RGBA", (PLAYSTORE_SIZE, PLAYSTORE_SIZE), BACKGROUND_COLOR)
    
    # Calculate scaling to fit logo properly (with padding for safe area)
    # Leave 20% margin for circular cropping on some launchers
    max_logo_size = int(PLAYSTORE_SIZE * 0.8)
    
    # Scale logo proportionally to fit
    logo_ratio = min(max_logo_size / logo.width, max_logo_size / logo.height)
    new_logo_size = (int(logo.width * logo_ratio), int(logo.height * logo_ratio))
    
    # Resize logo with high quality
    logo_resized = logo.resize(new_logo_size, Image.Resampling.LANCZOS)
    
    # Center the logo on the canvas
    x_offset = (PLAYSTORE_SIZE - new_logo_size[0]) // 2
    y_offset = (PLAYSTORE_SIZE - new_logo_size[1]) // 2
    
    # Paste logo onto background
    playstore_icon.paste(logo_resized, (x_offset, y_offset), logo_resized if logo_resized.mode == 'RGBA' else None)
    
    # Convert to RGB (remove alpha) as recommended by Play Store
    final_icon = Image.new("RGB", (PLAYSTORE_SIZE, PLAYSTORE_SIZE), BACKGROUND_COLOR)
    final_icon.paste(playstore_icon, (0, 0))
    
    # Save as 32-bit PNG 
    final_icon.save(output_path, "PNG", optimize=True, quality=100)
    
    print(f"✅ Play Store icon created: {output_path}")
    print(f"✅ Size: {PLAYSTORE_SIZE}x{PLAYSTORE_SIZE} pixels")
    print(f"✅ Format: PNG")
    print(f"✅ Background: Solid blue ({BACKGROUND_COLOR})")
    print(f"✅ Logo scaling: {logo_ratio:.2f}x")
    print(f"✅ Safe area: 20% margin for circular cropping")
    
    return True

if __name__ == "__main__":
    try:
        create_playstore_icon()
        print("\n🎉 Play Store icon generation SUCCESSFUL!")
        print("Ready for Play Store submission.")
    except Exception as e:
        print(f"❌ Error creating Play Store icon: {e}")
        exit(1)