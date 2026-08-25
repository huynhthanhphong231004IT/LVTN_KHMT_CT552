from PIL import Image, ImageDraw, ImageFont
import os

paths = [
    "./BoxPR_curve.png",
    "./Anh/confusion_matrix_normalized_YoloV12s.png"
]

img_size = (1000, 700)
text_height = 40

images = [Image.open(p).resize(img_size) for p in paths]

# Canvas cho 2 ảnh
result = Image.new(
    "RGB",
    (img_size[0] * 2, img_size[1] + text_height),
    "white"
)

draw = ImageDraw.Draw(result)

try:
    font = ImageFont.truetype("arial.ttf", 24)
except:
    font = ImageFont.load_default()

for i, (img, path) in enumerate(zip(images, paths)):
    x = i * img_size[0]

    result.paste(img, (x, 0))

    filename = os.path.splitext(os.path.basename(path))[0]

    bbox = draw.textbbox((0, 0), filename, font=font)
    text_width = bbox[2] - bbox[0]

    text_x = x + (img_size[0] - text_width) // 2
    text_y = img_size[1] + 5

    draw.text((text_x, text_y), filename, fill="black", font=font)

result.save("merged_2_images.png")

print("Đã ghép thành merged_2_images.png")