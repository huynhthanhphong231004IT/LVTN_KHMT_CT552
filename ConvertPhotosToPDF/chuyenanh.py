import os
from PIL import Image

Image.MAX_IMAGE_PIXELS = 500000000
input_folder = "./Anh_2"
output_folder = "./Anh_2_PDF"

os.makedirs(output_folder, exist_ok=True)

for file in os.listdir(input_folder):
    if file.lower().endswith((".png", ".jpg", ".jpeg", ".bmp", ".webp")):
        image_path = os.path.join(input_folder, file)
        output_pdf = os.path.join(output_folder, os.path.splitext(file)[0] + ".pdf")

        img = Image.open(image_path)

        if img.mode != "RGB":
            img = img.convert("RGB")

        img.save(output_pdf, "PDF", resolution=100.0)

print("Done!")