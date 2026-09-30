from PIL import Image
import os

src = "/Users/laksharajjha/.gemini/antigravity/brain/3cb26b75-240d-4869-b3f6-4423e7d7a204/.user_uploaded/media_1790792721434.png"
res_dir = "/Users/laksharajjha/Documents/Money/app/src/main/res"

sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192
}

img = Image.open(src)
for density, size in sizes.items():
    out_dir = os.path.join(res_dir, f"mipmap-{density}")
    os.makedirs(out_dir, exist_ok=True)
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    resized.save(os.path.join(out_dir, "ic_launcher.png"))
    resized.save(os.path.join(out_dir, "ic_launcher_round.png"))

print("Logo updated successfully!")
