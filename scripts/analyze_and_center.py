#!/usr/bin/env python3
"""
Analyze applogo.png content bounds and create perfectly centered icon
"""

from PIL import Image, ImageOps
import numpy as np

def find_content_bounds(image):
    """Find the actual content bounds (non-transparent area) of the image"""
    # Convert to numpy array
    img_array = np.array(image)
    
    # If RGBA, use alpha channel to find content
    if image.mode == 'RGBA':
        alpha = img_array[:, :, 3]
        # Find rows and columns that contain non-transparent pixels
        rows = np.any(alpha > 0, axis=1)
        cols = np.any(alpha > 0, axis=0)
    else:
        # For RGB, find non-white pixels (assuming white background)
        gray = np.array(image.convert('L'))
        rows = np.any(gray < 255, axis=1)
        cols = np.any(gray < 255, axis=0)
    
    # Get bounding box
    top, bottom = np.where(rows)[0][[0, -1]]
    left, right = np.where(cols)[0][[0, -1]]
    
    return (left, top, right + 1, bottom + 1)

def create_perfectly_centered_icon():
    input_path = "app/src/main/res/drawable/applogo.png"
    output_path = "app/src/main/res/drawable/ic_launcher_foreground_centered.png"
    
    CANVAS_SIZE = 432
    SAFE_AREA = int(CANVAS_SIZE * 0.55)  # Reduced from 66% to 55% for better breathing room
    
    print(f"Analyzing content bounds of {input_path}")
    
    # Load logo
    logo = Image.open(input_path)
    print(f"Original canvas size: {logo.size}")
    
    # Find actual content bounds
    content_bounds = find_content_bounds(logo)
    left, top, right, bottom = content_bounds
    content_width = right - left
    content_height = bottom - top
    
    print(f"Content bounds: left={left}, top={top}, right={right}, bottom={bottom}")
    print(f"Content size: {content_width}x{content_height}")
    print(f"Original padding: left={left}, top={top}, right={logo.width-right}, bottom={logo.height-bottom}")
    
    # Crop to content bounds with a small padding
    padding = 5
    crop_left = max(0, left - padding)
    crop_top = max(0, top - padding)
    crop_right = min(logo.width, right + padding)
    crop_bottom = min(logo.height, bottom + padding)
    
    # Crop the logo to its actual content
    logo_cropped = logo.crop((crop_left, crop_top, crop_right, crop_bottom))
    print(f"Cropped to content size: {logo_cropped.size}")
    
    # Create new canvas
    canvas = Image.new("RGBA", (CANVAS_SIZE, CANVAS_SIZE), (0, 0, 0, 0))
    
    # Calculate scaling to fit within safe area
    logo_ratio = min(SAFE_AREA / logo_cropped.width, SAFE_AREA / logo_cropped.height)
    new_size = (int(logo_cropped.width * logo_ratio), int(logo_cropped.height * logo_ratio))
    
    print(f"Scaling ratio: {logo_ratio:.3f}")
    print(f"Final logo size: {new_size}")
    
    # Resize with high quality
    logo_resized = logo_cropped.resize(new_size, Image.Resampling.LANCZOS)
    
    # Perfect mathematical centering
    x_offset = (CANVAS_SIZE - new_size[0]) // 2
    y_offset = (CANVAS_SIZE - new_size[1]) // 2
    
    print(f"Perfect center position: ({x_offset}, {y_offset})")
    
    # Paste at perfect center
    canvas.paste(logo_resized, (x_offset, y_offset), logo_resized if logo_resized.mode == 'RGBA' else None)
    
    # Save
    canvas.save(output_path, "PNG", optimize=True)
    
    print(f"Perfectly centered icon created: {output_path}")
    return output_path

if __name__ == "__main__":
    try:
        create_perfectly_centered_icon()
        print("\nPerfectly centered icon generation SUCCESSFUL!")
    except Exception as e:
        print(f"Error: {e}")
        exit(1)