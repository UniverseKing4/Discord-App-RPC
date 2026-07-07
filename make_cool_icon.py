from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os

width, height = 1024, 1024
text = "RPC"

# 1. Background Image (Full Black)
bg = Image.new("RGBA", (width, height), (0, 0, 0, 255))

# 2. Foreground Image (Transparent)
fg = Image.new("RGBA", (width, height), (0, 0, 0, 0))
try:
    font = ImageFont.truetype("/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf", 220)
except:
    font = ImageFont.load_default()

dummy_draw = ImageDraw.Draw(fg)
bbox = dummy_draw.textbbox((0,0), text, font=font)
text_w = bbox[2] - bbox[0]
text_h = bbox[3] - bbox[1]

x = (width - text_w) / 2 - bbox[0]
y = (height - text_h) / 2 - bbox[1]

# Create a glow layer
glow_layer = Image.new("RGBA", (width, height), (0,0,0,0))
glow_draw = ImageDraw.Draw(glow_layer)

# Draw cyan/purple glow
glow_draw.text((x-8, y-8), text, font=font, fill=(0, 255, 255, 220)) # Cyan
glow_draw.text((x+8, y+8), text, font=font, fill=(180, 0, 255, 220)) # Purple
glow_layer = glow_layer.filter(ImageFilter.GaussianBlur(12))

# Draw the main text
main_layer = Image.new("RGBA", (width, height), (0,0,0,0))
main_draw = ImageDraw.Draw(main_layer)
main_draw.text((x, y), text, font=font, fill=(255, 255, 255, 255))

# Composite
fg = Image.alpha_composite(fg, glow_layer)
fg = Image.alpha_composite(fg, main_layer)

res_dir = "/workspaces/codespaces-blank/app/RPC/app/src/main/res"
sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192
}

for folder, size in sizes.items():
    folder_path = os.path.join(res_dir, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    # Standard launcher
    fg_scaled = fg.resize((size, size), Image.Resampling.LANCZOS)
    bg_scaled = bg.resize((size, size), Image.Resampling.LANCZOS)
    
    legacy = Image.alpha_composite(bg_scaled, fg_scaled)
    legacy.save(os.path.join(folder_path, "ic_launcher.webp"))
    legacy.save(os.path.join(folder_path, "ic_launcher_round.webp"))
    
    ad_size = int(size * 1.5) # 108dp is 1.5x of 72dp
    fg_ad = fg.resize((ad_size, ad_size), Image.Resampling.LANCZOS)
    bg_ad = bg.resize((ad_size, ad_size), Image.Resampling.LANCZOS)
    
    fg_ad.save(os.path.join(folder_path, "ic_launcher_foreground.webp"))
    bg_ad.save(os.path.join(folder_path, "ic_launcher_background.webp"))

print("Icons generated!")
