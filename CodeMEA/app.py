# API.py
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse, FileResponse
from fastapi.middleware.cors import CORSMiddleware
import shutil, os, base64, time
from typing import List
from giama import M_Box
from SinhKhoa import SinhKhoa
from HoanViDau import HoanViDau
from HoanViCuoi import HoanViCuoi
from SinhKhoaMEA import SinhKhoaMEA
from DauAnh import DauAnh
from MEA_tang_cuong import MEA_tc
from evaluation_mea import DanhGia
from evaluation_text import TextEval
import numpy as np
import cv2
app = FastAPI()

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Thư mục tạm
os.makedirs("temp_images", exist_ok=True)
os.makedirs("temp_output", exist_ok=True)
os.makedirs("watermarked_images", exist_ok=True)

@app.post("/encrypt")
async def encrypt_text_api(
    plain_text: str = Form(...),
    images: List[UploadFile] = File(...)
):
    try:
        start_time = time.time()

        # Lưu tất cả ảnh upload vào temp_images
        saved_files = []
        for img in images:
            # chỉ lấy tên file, bỏ folder con nếu có
            filename = os.path.basename(img.filename)
            save_path = os.path.join("temp_images", filename)
            with open(save_path, "wb") as f:
                shutil.copyfileobj(img.file, f)
            saved_files.append(save_path)

        if not saved_files:
            return JSONResponse({"status": "error", "detail": "Không có ảnh nào được upload"})

        # Sinh khóa & ma trận
        p = SinhKhoa.sinh_so_nguyen_to(256, 1000)
        g = SinhKhoa.tim_phan_tu_sinh(p)
        S = SinhKhoa.sinh_ma_tran_S(p, 3)
        key_hex = SinhKhoaMEA.generate_matrices(35)
        des_key = b'12345678'

        # Chuyển văn bản sang ma trận
        blocks_X, orig_len = HoanViDau.text_to_byte_matrices(plain_text)

        # Hoán vị đầu
        Ys, encrypted_text = HoanViDau.encrypt_blocks_byte_mode(blocks_X, S, g, p)
        my_encrypted_var = encrypted_text
        # Mã hóa MEA
        cipher_blocks = MEA_tc.encrypt_text(encrypted_text, key_hex, use_rotate_shift=True)
        cipher_bytes = b''.join(block.astype(np.uint8).tobytes() for block in cipher_blocks)
        cipher_text = base64.b64encode(cipher_bytes).decode('utf-8')

        # Hoán vị cuối
        bits = HoanViCuoi.text_to_bits(cipher_text)
        cipher_blocks_final = HoanViCuoi.encrypt_bits(bits)
        cipher_blocks_final = ''.join(cipher_blocks_final)

        # Nhúng văn bản vào tất cả ảnh trong folder temp_images
        DauAnh.embed_text_in_folder(cipher_blocks_final, "temp_images", "watermarked_images", des_key)

        end_time = time.time()
        start_decrypt = time.time() 
        recovered_text = DauAnh.extract_text_from_folder("./watermarked_images", des_key)
        decoded_bits = HoanViCuoi.decrypt_bits(recovered_text, len(bits))
        decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
        # print(decoded_text)
        decoded_text = list(decoded_text)
        # print("Bước 2: MEA")
        cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
        recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
        # print("\nChuỗi MEA:", recovered)
        # print("Bước 1: Hoán vị đầu")
        Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
        end_decrypt = time.time()  # kết thúc đếm thời gian giải mã
        print(f"\nThời gian giải mã: {end_decrypt - start_decrypt:.3f} giây")

        bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
        # Lấy file đầu tiên để demo
        first_image_path = saved_files[0]
        first_image_name = os.path.basename(first_image_path)
        before_image = first_image_path
        after_image = os.path.join("watermarked_images", first_image_name)

        # Kiểm tra xem ảnh watermark đã tồn tại chưa
        if not os.path.exists(after_image):
            return JSONResponse({"status":"error","detail":"Ảnh watermark chưa được tạo"})

        # Lưu key ra file
        key_file_path = os.path.join("temp_output", "key.txt")
        with open(key_file_path, "w") as f:
            f.write(str(key_hex))
        print(cipher_text)
        print(bytes_rec.decode())
        # =========================
        # ĐÁNH GIÁ TỰ ĐỘNG
        # =========================

        img_before = cv2.imread(before_image, cv2.IMREAD_GRAYSCALE)
        img_after = cv2.imread(after_image, cv2.IMREAD_GRAYSCALE)

        if img_before is not None and img_after is not None:

            metrics = DanhGia.full_evaluation(
                img_before,
                img_after,
                end_time - start_time,
                end_decrypt - start_decrypt
            )

            # print("\n========== DANH GIA HE THONG ==========")

            # for key, value in metrics.items():
            #     print(f"{key}: {value}")

            # print("=======================================\n")

        else:
            metrics = {"error": "Khong doc duoc anh"}
        return JSONResponse({
            "status": "success",
            "cipher_text": cipher_text,
            "plain_test": bytes_rec.decode(),
            "before_images": [before_image],
            "after_images": [after_image],
            "key_file": key_file_path,
            "encrypt_time": round(end_time - start_time, 2)
        })

    except Exception as e:
        return JSONResponse({"status":"error", "detail": str(e)})

@app.get("/download/{filename}")
async def download_file(filename: str):
    file_path = os.path.join("temp_output", filename)
    if os.path.exists(file_path):
        return FileResponse(file_path, filename=filename)
    return JSONResponse({"status":"error", "detail":"File not found"})



if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)


