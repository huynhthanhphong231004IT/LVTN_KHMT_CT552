# API.py
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse, FileResponse
from fastapi.middleware.cors import CORSMiddleware
import shutil, os, base64, time
from typing import List
from giama import M_Box
import sys
from SinhKhoa import SinhKhoa
from HoanViDau import HoanViDau
from HoanViCuoi import HoanViCuoi
from SinhKhoaMEA import SinhKhoaMEA
from DauAnh import DauAnh
from MEA_tang_cuong import MEA_tc
import numpy as np
import time
import random
import json
from fastapi import FastAPI
import uvicorn
from fastapi.middleware.cors import CORSMiddleware
Name = "bevadantenlua"
JSONname = "Bệ và đạn tên lửa SAM.json"
imagename = "SAM01"
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], 
    allow_credentials=True,
    allow_methods=["*"],   
    allow_headers=["*"],
)

class MEATC:
    @staticmethod
    def clean(o):
        if isinstance(o, np.ndarray):
            return o.tolist()

        if isinstance(o, list):
            return [MEATC.clean(x) for x in o]

        return o
    @staticmethod
    def create_run_folders():
        run_id = time.strftime("%Y%m%d_%H%M%S")
        base_dir = f"runs/{Name}/{run_id}"
        paths = {
            "output": os.path.join(base_dir, "temp_output"),
            "watermark": os.path.join(base_dir, "watermarked_images")
        }
        for p in paths.values():
            os.makedirs(p, exist_ok=True)
        return paths, run_id
    
    @staticmethod
    def SinhKhoaTongHop_MEAtc():
        p = SinhKhoa.sinh_so_nguyen_to(256, 1000)
        g = SinhKhoa.tim_phan_tu_sinh(p)
        S = SinhKhoa.sinh_ma_tran_S(p, 3)
        key_hex = SinhKhoaMEA.generate_matrices(35)
        des_key = os.urandom(8)
        data = {
            "p": p,
            "g": g,
            "S": S.tolist() if hasattr(S, "tolist") else S,
            "key_hex": key_hex,
            "des_key": des_key.hex()
        }
        with open("./Khoa/khoa.json", "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=4)
        return data

    @staticmethod
    def DocKhoaTongHop_MEAtc():

        if not os.path.exists("./Khoa/khoa.json"):
            MEATC.SinhKhoaTongHop_MEAtc()

        with open("./Khoa/khoa.json", "r", encoding="utf-8") as f:
            data = json.load(f)

        p = data["p"]
        g = data["g"]
        S = np.array(data["S"])
        key_hex = data["key_hex"]
        des_key = bytes.fromhex(data["des_key"])

        return p, g, S, key_hex, des_key
    @staticmethod
    def MaHoa_MEAtc(plain_text, input_dir):
        paths, run_id = MEATC.create_run_folders()
        output_dir = paths["output"]
        wm_dir = paths["watermark"]
        
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        blocks_X, orig_len = HoanViDau.text_to_byte_matrices(plain_text)

        # Hoán vị đầu
        Ys, my_encrypted_var = HoanViDau.encrypt_blocks_byte_mode(blocks_X, S, g, p)

        # Mã hóa MEA
        cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
        cipher_bytes = b''.join(block.astype(np.uint8).tobytes() for block in cipher_blocks)
        cipher_text = base64.b64encode(cipher_bytes).decode('utf-8')

        # Hoán vị cuối
        bits = HoanViCuoi.text_to_bits(cipher_text)
        cipher_blocks_final = HoanViCuoi.encrypt_bits(bits)
        cipher_blocks_final = ''.join(cipher_blocks_final)
        # Nhúng văn bản vào tất cả ảnh trong folder temp_images
        DauAnh.embed_text_in_folder(cipher_blocks_final, input_dir, wm_dir, des_key)
        state = {
            "run_id": run_id,
            "bits_len": len(bits),
            "Ys": MEATC.clean(Ys),
            "my_encrypted_var": MEATC.clean(my_encrypted_var),
            "orig_len": int(orig_len)
        }
        with open(f"runs/{Name}/{run_id}/temp_output/state.json", "w", encoding="utf-8") as f:
            json.dump(state, f, ensure_ascii=False, indent=4)

        # return run_id, wm_dir
    
    @staticmethod
    def GiaiMa_MEAtc(run_id, wm_dir, khoa_file):
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        state_path = f"runs/{Name}/{run_id}/temp_output/state.json"

        with open(state_path, "r", encoding="utf-8") as f:
            state = json.load(f)

        bits_len = state["bits_len"]
        Ys = state["Ys"]
        my_encrypted_var = state["my_encrypted_var"]
        orig_len = state["orig_len"]
        recovered_text = DauAnh.extract_text_from_folder(wm_dir, des_key)

        decoded_bits = HoanViCuoi.decrypt_bits(recovered_text, bits_len)
        decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
        decoded_text = list(decoded_text)
        cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
        recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
        Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
        bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
        print(bytes_rec.decode())
    @staticmethod
    def GanCauHoiNgauNhien(json_path, image_dir, output_path):
        import random

        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        questions = data["questions"]

        # lấy 4 câu random
        selected = random.sample(questions, 4)

        images = sorted([
            f for f in os.listdir(image_dir)
            if f.lower().endswith((".png", ".jpg", ".jpeg"))
        ])

        if len(images) < 4:
            raise ValueError("Cần ít nhất 4 ảnh")

        result = []

        for img, q in zip(images[:4], selected):
            result.append({
                "image": img,
                "id": q["id"],
                "question": q["question"],
                "answer": q["answer"],
                "options": q["options"]
            })

        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(result, f, ensure_ascii=False, indent=4)

        return result
    @staticmethod
    def TaoGame(json_path, image_input, image_output, run_id):
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        questions = data["questions"]

        selected = random.sample(questions, 4)

        images = sorted([
            f for f in os.listdir(image_input)
            if f.lower().endswith((".png", ".jpg", ".jpeg"))
        ])[:4]

        os.makedirs(image_output, exist_ok=True)

        game = []

        for i in range(4):
            q = selected[i]

            img_in = os.path.join(image_input, images[i])
            img_out = os.path.join(image_output, images[i])
            MEATC.MaHoa_1Anh(
                plain_text=str(q["id"]),
                input_image=img_in,
                output_image=img_out,
                run_id=run_id   
            )

            game.append({
                "image": images[i],
                "id": q["id"],
                "question": q["question"],
                "answer": q["answer"],
                "options": q["options"]
            })

        return game
    
    @staticmethod
    def MaHoa_1Anh(plain_text, input_image, output_image, run_id):
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()

        blocks_X, orig_len = HoanViDau.text_to_byte_matrices(plain_text)
        Ys, my_encrypted_var = HoanViDau.encrypt_blocks_byte_mode(blocks_X, S, g, p)
        cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
        cipher_bytes = b''.join(block.astype(np.uint8).tobytes() for block in cipher_blocks)
        cipher_text = base64.b64encode(cipher_bytes).decode('utf-8')
        bits = HoanViCuoi.text_to_bits(cipher_text)
        cipher_bits = HoanViCuoi.encrypt_bits(bits)
        cipher_bits = ''.join(cipher_bits)
        DauAnh.embed_text_block(cipher_bits,input_image,output_image,des_key,total_images=1,chunk_index=1 )
        state = {
            "bits_len": len(bits),
            "Ys": MEATC.clean(Ys),
            "my_encrypted_var": MEATC.clean(my_encrypted_var),
            "orig_len": int(orig_len)
        }
        state_path = f"runs/{Name}/{run_id}/temp_output/state_{os.path.basename(input_image)}.json"

        with open(state_path, "w", encoding="utf-8") as f:
            json.dump(state, f, ensure_ascii=False, indent=4)


@app.get("/game/start")
def start_game():
    paths, run_id = MEATC.create_run_folders()

    game = MEATC.TaoGame(
        json_path=f"../Text-Model/Huan-Luyen/CauHoi_WithOptions/{JSONname}",
        image_input=f"./AnhDau/{Name}/{imagename}",
        image_output=f"./runs/{Name}/game_images",
        run_id=run_id
    )

    return {
        "run_id": run_id,
        "game": game
    }
@app.post("/game/decrypt-xetang")
def decrypt_image(payload: dict):
    start_time = time.time()  # 1. Đo thời gian bắt đầu

    run_id = payload["run_id"]
    wm_dir = payload["wm_dir"]
    p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
    recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
    result = []

    for item in recovered_list:
        bits = item["bits"]
        img_name = item["image"]
        print(img_name)
        state_path = f"runs/XeTang/{run_id}/temp_output/state_{img_name}.json"

        with open(state_path, "r", encoding="utf-8") as f:
            state = json.load(f)

        bits_len = state["bits_len"]
        Ys = state["Ys"]
        my_encrypted_var = state["my_encrypted_var"]
        orig_len = state["orig_len"]
        decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
        decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
        decoded_text = list(decoded_text)
        cipher_blocks = MEA_tc.encrypt_text(
            my_encrypted_var, key_hex, use_rotate_shift=True
        )
        recovered = MEA_tc.decrypt_blocks_to_text(
            cipher_blocks, key_hex, use_rotate_shift=True
        )
        Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
        bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len)
        result.append({"image": item["image"], "id": bytes_rec})

    # --- TÍNH TOÁN VÀ PRINT THÔNG SỐ (KHÔNG THAY ĐỔI CẤU TRÚC ĐẦU RA) ---
    process_time_sec = time.time() - start_time
    time_ms = process_time_sec * 1000

    # Dữ liệu nhận vào (Payload request)
    req_size_kb = sys.getsizeof(str(payload)) / 1024

    # Dữ liệu gửi đi (Response size an toàn với kiểu bytes)
    res_size_kb = sys.getsizeof(str(result)) / 1024

    # Tốc độ truyền tải
    upload_speed = (
        req_size_kb / process_time_sec if process_time_sec > 0 else 0
    )
    download_speed = (
        res_size_kb / process_time_sec if process_time_sec > 0 else 0
    )

    print("\n------------------------------------")
    print(f"Dữ liệu nhận vào (KB) : {req_size_kb:.2f}")
    print(f"Tốc độ tải lên (KB/s) : {upload_speed:.2f}")
    print(f"Dữ liệu gửi đi (KB)   : {res_size_kb:.2f}")
    print(f"Tốc độ tải xuống (KB/s): {download_speed:.2f}")
    print(f"Time (ms)             : {time_ms:.2f}")
    print("------------------------------------\n")

    return {"answers": result}

@app.post("/game/decrypt-bom")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/Bom/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }

@app.post("/game/decrypt-ghexuongthuyen")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/ghexuongthuyen/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }

@app.post("/game/decrypt-luhambimat")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/luhambimat/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }


@app.post("/game/decrypt-maybaytructhang")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/maybaytructhang/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }


@app.post("/game/decrypt-maycantol")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/maycantol/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }

@app.post("/game/decrypt-mayinpedal")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/mayinpedal/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }

@app.post("/game/decrypt-moneotau")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/moneotau/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }

@app.post("/game/decrypt-phao")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/phao/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }


@app.post("/game/decrypt-sungthancong")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/sungthancong/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }


@app.post("/game/decrypt-tautuantieupcf")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/tautuantieupcf/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }

@app.post("/game/decrypt-trucmaybayb52")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/trucmaybayb52/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }


@app.post("/game/decrypt-xebocthep")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/xebocthep/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }

@app.post("/game/decrypt-xepeugeot")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/xepeugeot/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }

@app.post("/game/decrypt-bevadantenlua")
def decrypt_image(payload: dict):
        run_id = payload["run_id"]
        wm_dir = payload["wm_dir"]
        p, g, S, key_hex, des_key = MEATC.DocKhoaTongHop_MEAtc()
        recovered_list = DauAnh.extract_text_from_folder(wm_dir, des_key)
        result = []
        for item in recovered_list:
            bits = item["bits"]
            img_name = item["image"]
            print(img_name)
            state_path = f"runs/bevadantenlua/{run_id}/temp_output/state_{img_name}.json"

            with open(state_path, "r", encoding="utf-8") as f:
                state = json.load(f)

            bits_len = state["bits_len"]
            Ys = state["Ys"]
            my_encrypted_var = state["my_encrypted_var"]
            orig_len = state["orig_len"]
            decoded_bits = HoanViCuoi.decrypt_bits(bits[0], bits_len)
            decoded_text = HoanViCuoi.bits_to_text(decoded_bits)
            decoded_text = list(decoded_text)
            cipher_blocks = MEA_tc.encrypt_text(my_encrypted_var, key_hex, use_rotate_shift=True)
            recovered = MEA_tc.decrypt_blocks_to_text(cipher_blocks, key_hex, use_rotate_shift=True)
            Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
            bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len) 
            result.append({
                "image": item["image"],
                "id": bytes_rec
            })
        return {
            "answers": result
        }
if __name__ == "__main__":
    import uvicorn
    import socket
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80)) 
        current_ip = s.getsockname()[0]
        s.close()
    except Exception:
        current_ip = "0.0.0.0" 

    print(f"==================================================")
    print(f"SERVER TỰ ĐỘNG CHẠY TRÊN IP: http://{current_ip}:8003")
    print(f"==================================================")

    uvicorn.run("app_2:app", host=current_ip, port=8003, reload=True)