import numpy as np
def gf_mul(a, b):
    if a == 0 or b == 0:
        return 0
    result = 0
    for i in range(8):
        if b & 1:
            result ^= a
        hi_bit_set = a & 0x80
        a <<= 1
        if hi_bit_set:
            a ^= 0x11B  
        a &= 0xFF
        b >>= 1
    return result
def gf_inv(a):
    if a == 0:
        return 0  # nghịch đảo của 0 là 0 để tránh lỗi
    for b in range(1, 256):
        if gf_mul(a, b) == 1:
            return b
    raise ValueError("Không tìm thấy nghịch đảo (rất hiếm)")

class M_Box:
    def mbox(a_hex, b_hex):
        a = int(a_hex, 16)
        b = int(b_hex, 16)
        result = gf_mul(a, b)
        return f"{result:02X}"

    def inv_mbox(y_hex, b_hex):
        y = int(y_hex, 16)
        b = int(b_hex, 16)
        inv_b = gf_inv(b)
        a = gf_mul(y, inv_b)
        return f"{a:02X}"

def main():
    # Tạo ma trận ví dụ
    a_matrix = np.array([[0x00, 0x34, 0x56],
                         [0x78, 0x00, 0xBC],
                         [0xDE, 0xF0, 0x11]], dtype=np.uint8)
    
    b_matrix = np.array([[0x01, 0x02, 0x03],
                         [0x04, 0x05, 0x06],
                         [0x07, 0x08, 0x09]], dtype=np.uint8)
    
    print("=== M_Box nhân ma trận ===")
    y_matrix = np.empty_like(a_matrix, dtype=object)
    for r in range(3):
        for c in range(3):
            y_matrix[r, c] = M_Box.mbox(f"{a_matrix[r,c]:02X}", f"{b_matrix[r,c]:02X}")
    print(y_matrix)
    
    print("\n=== M_Box nghịch đảo ma trận ===")
    a_recovered = np.empty_like(a_matrix, dtype=object)
    for r in range(3):
        for c in range(3):
            a_recovered[r, c] = M_Box.inv_mbox(y_matrix[r, c], f"{b_matrix[r,c]:02X}")
    print(a_recovered)

# if __name__ == "__main__":
#     main()