import math

class HoanViCuoi:
    def g(x, p):
        return x + p * x**2

    def ma_hoa(x1, x2, x3, x4, p, B):
        y = HoanViCuoi.g(x1, p) + B*HoanViCuoi.g(x2, p) + B**2*HoanViCuoi.g(x3, p) + B**3*HoanViCuoi.g(x4, p)
        return y

    def giai_ma(y, p, B):
        d4, r3 = divmod(y, B**3)
        d3, r2 = divmod(r3, B**2)
        d2, d1 = divmod(r2, B)
        ds = [d1, d2, d3, d4]
        
        xs = []
        for di in ds:
            x = int(round((-1 + math.sqrt(1 + 4*p*di)) / (2*p)))
            xs.append(x)
        return xs

    # --- Chuyển text <-> bit ---
    def text_to_bits(text):
        bits = ''.join(f'{b:08b}' for b in text.encode('utf-8'))
        return bits

    def bits_to_text(bits):
        chars = []
        for i in range(0, len(bits), 8):
            byte = bits[i:i+8]
            if len(byte) < 8:  # padding cuối
                byte = byte.ljust(8, '0')
            chars.append(int(byte, 2))
        return bytes(chars).decode('utf-8', errors='ignore')

    # --- Mã hóa MEA trên chuỗi bit ---
    # Thay thế phần mã/giải mã block decimal bằng binary (48-bit)
    def encrypt_bits(bits, p=1, B=241):
        cipher_blocks = []
        for i in range(0, len(bits), 16):
            block_bits = bits[i:i+16]
            # tạo 4 nibble (4-bit each)
            nibbles = [int(block_bits[j:j+4], 2) if j+4 <= len(block_bits)
                    else int(block_bits[j:].ljust(4, '0'), 2)
                    for j in range(0, 16, 4)]
            while len(nibbles) < 4:
                nibbles.append(0)
            y = HoanViCuoi.ma_hoa(*nibbles, p, B)
            # Chuyển y thành 48-bit nhị phân
            bits48 = f'{y:048b}'
            cipher_blocks.append(bits48)
        return cipher_blocks

    def decrypt_bits(cipher_blocks, orig_bit_len, p=1, B=241):
        bits = ''
        for bits48 in cipher_blocks:
            y = int(bits48, 2)
            xs = HoanViCuoi.giai_ma(y, p, B)
            for x in xs:
                bits += f'{x:04b}'
        return bits[:orig_bit_len]


# # --- Thử nghiệm ---
# text = "Huỳnh Thanh Phong"
# print("Chuỗi gốc:", text)

# bits = HoanViCuoi.text_to_bits(text)
# print("Chuỗi bit gốc:", bits)

# cipher_blocks = HoanViCuoi.encrypt_bits(bits)
# print("\nCipher blocks (mỗi block 48 bit):")
# for cb in cipher_blocks:
#     print(cb)

# decoded_bits = HoanViCuoi.decrypt_bits(cipher_blocks, len(bits))
# decoded_text = HoanViCuoi.bits_to_text(decoded_bits)

# print("\nGiải mã xong:")
# print(decoded_text)
# print("Khớp hoàn toàn!" if decoded_text == text else "Chưa khớp")
