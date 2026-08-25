import os
from PIL import Image

# Thư mục chứa ảnh gốc
input_folder = "../xepeugeot"

# Các định dạng ảnh hỗ trợ
image_extensions = (".jpg", ".jpeg", ".png", ".bmp", ".tiff", ".webp")

for filename in os.listdir(input_folder):
    if filename.lower().endswith(image_extensions):
        image_path = os.path.join(input_folder, filename)

        # Mở ảnh
        img = Image.open(image_path)
        width, height = img.size

        # Kích thước mỗi phần
        half_width = width // 2
        half_height = height // 2

        # Tên ảnh không có phần mở rộng
        image_name = os.path.splitext(filename)[0]

        # Tạo thư mục lưu kết quả
        output_folder = os.path.join(input_folder, image_name)
        os.makedirs(output_folder, exist_ok=True)

        # Các vùng cắt
        crops = [
            (0, 0, half_width, half_height),                  # trên trái
            (half_width, 0, width, half_height),             # trên phải
            (0, half_height, half_width, height),            # dưới trái
            (half_width, half_height, width, height),        # dưới phải
        ]

        # Lưu từng phần
        for i, box in enumerate(crops, start=1):
            cropped = img.crop(box)

            save_path = os.path.join(
                output_folder,
                f"{image_name}_{i}.png"
            )

            cropped.save(save_path)

        print(f"Đã xử lý: {filename}")

print("Hoàn thành!")