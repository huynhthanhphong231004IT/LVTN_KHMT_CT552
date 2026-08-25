import numpy as np
import math
import base64
from SinhKhoa import SinhKhoa

class HoanViDau:
    def text_to_byte_matrices(text):
        data = text.encode('utf-8')
        orig_len = len(data)
        blocks = []
        for i in range(0, orig_len, 9):
            chunk = data[i:i+9]
            if len(chunk) < 9:
                chunk = chunk + b'\x00' * (9 - len(chunk))
            mat = np.array(list(chunk), dtype=int).reshape((3,3))
            blocks.append(mat)
        if len(blocks) == 0:
            blocks = [np.zeros((3,3), dtype=int)]
        return blocks, orig_len

    def byte_matrices_to_bytes(list_X, orig_len):
        parts = []
        for X in list_X:
            flat = np.array(X).reshape(-1)
            parts.extend([int(v) & 0xFF for v in flat])
        return bytes(parts[:orig_len])

    def build_M_from_X_bytes(X, g, p):
        M = np.zeros((3,3), dtype=int)
        for i in range(3):
            for j in range(3):
                x = int(X[i,j]) % (p-1)
                M[i,j] = pow(g, x, p)
        return M

    def encrypt_blocks_byte_mode(blocks_X, S, g, p):
        S_inv = SinhKhoa.mod_matrix_inverse(S, p)
        Ys = []
        encrypted_bytes = b""
        for idx, X in enumerate(blocks_X):
            M = HoanViDau.build_M_from_X_bytes(X, g, p)
            Y = (S.dot(M).dot(S_inv)) % p
            Ys.append(Y)
            flat_bytes = [int(v) % 128 for v in Y.flatten()]
            encrypted_bytes += bytes(flat_bytes)

        # Chuyển bytes sang Base64 để dùng làm chuỗi ký tự
        encrypted_text_str = base64.b64encode(encrypted_bytes).decode('utf-8')
        return Ys, encrypted_text_str

    def decrypt_blocks_byte_mode(Ys, S, g, p):
        S_inv = SinhKhoa.mod_matrix_inverse(S, p)
        Xs_rec = []
        for idx, Y in enumerate(Ys):
            Mprime = (S_inv.dot(Y).dot(S)) % p
            X_rec = np.zeros((3,3), dtype=int)
            for i in range(3):
                for j in range(3):
                    found = None
                    val = int(Mprime[i,j]) % p
                    for k in range(0, p-1):
                        if pow(g, k, p) == val:
                            found = k
                            break
                    if found is None:
                        raise ValueError(f"Không tìm log cho giá trị {val}")
                    X_rec[i,j] = found % (p-1)
            Xs_rec.append(X_rec)
        return Xs_rec


if __name__ == "__main__":
    p = SinhKhoa.sinh_so_nguyen_to(256, 1000)
    g = SinhKhoa.tim_phan_tu_sinh(p)
    S = SinhKhoa.sinh_ma_tran_S(p, 3)

    van_ban = "WxALKlhVY2VS"
    print("Văn bản gốc:", van_ban)

    blocks_X, orig_len = HoanViDau.text_to_byte_matrices(van_ban)

    Ys, encrypted_text = HoanViDau.encrypt_blocks_byte_mode(blocks_X, S, g, p)
    
    # Bây giờ encrypted_text là **chuỗi ký tự sau khi mã hóa**,
    # bạn có thể truyền vào biến khác
    my_encrypted_var = encrypted_text
    print("\nChuỗi ký tự sau khi mã hóa (Base64):")
    print(my_encrypted_var)

    # Giải mã lại
    Xs_rec = HoanViDau.decrypt_blocks_byte_mode(Ys, S, g, p)
    bytes_rec = HoanViDau.byte_matrices_to_bytes(Xs_rec, orig_len)
    text_rec = bytes_rec.decode('utf-8') 
    print("\nVăn bản phục hồi:", text_rec)
