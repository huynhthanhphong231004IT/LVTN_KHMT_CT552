import os
from PIL import Image
import numpy as np
from Cryptodome.Cipher import DES
import math
import matplotlib.pyplot as plt
class DauAnh:
    @staticmethod
    def text_to_bits(text):
        return ''.join(f'{ord(c):08b}' for c in text)
    @staticmethod
    def bits_to_text(bits):
        chars = [bits[i:i+8] for i in range(0, len(bits), 8)]
        return ''.join(chr(int(c, 2)) for c in chars)
    @staticmethod
    def embed_text_block(bits, input_image, output_image, des_key, total_images, chunk_index):
        # Mở ảnh và giữ nguyên RGBA uint8
        img = Image.open(input_image).convert("RGBA")
        data = np.array(img, dtype=np.uint8)
        
        # Lấy alpha channel
        alpha = data[..., 3].flatten()
        
        max_bits = alpha.size - 8
        if len(bits) > max_bits:
            raise ValueError(f"Block quá lớn cho ảnh {input_image}! Chỉ chứa được {max_bits} bit.")
        
        # Mã hóa metadata
        metadata = total_images.to_bytes(4,'big') + chunk_index.to_bytes(4,'big')
        des = DES.new(des_key, DES.MODE_ECB)
        encrypted_metadata = des.encrypt(metadata)
        alpha[:8] = np.frombuffer(encrypted_metadata, dtype=np.uint8)
        
        # Giấu bit text
        for i, b in enumerate(bits):
            alpha[i+8] = 255 if b=='1' else 254
        # Phần còn lại của alpha
        alpha[8+len(bits):] = 233
        
        # Chỉ gán lại alpha, giữ nguyên RGB
        data[..., 3] = alpha.reshape(data.shape[0], data.shape[1])
        
        # Lưu ảnh PNG, giữ nguyên dữ liệu
        Image.fromarray(data.astype(np.uint8), 'RGBA').save(output_image, format='PNG', compress_level=0)


    @staticmethod
    def embed_text_in_folder(plaintext, input_folder, output_folder, des_key):
        if not os.path.exists(output_folder):
            os.makedirs(output_folder)
        
        image_files = [f for f in os.listdir(input_folder) if f.lower().endswith(('.png','.jpg','.jpeg'))]
        image_files.sort()
        num_images = len(image_files)
        
        bits_all = DauAnh.text_to_bits(plaintext)
        total_bits = len(bits_all)
        start = 0
        
        for i, filename in enumerate(image_files):
            input_path = os.path.join(input_folder, filename)
            output_path = os.path.join(output_folder, filename)
            img = Image.open(input_path).convert("RGBA")
            alpha = np.array(img)[..., 3].flatten()
            max_bits = alpha.size - 8
            
            block_bits = bits_all[start:start + max_bits]
            if not block_bits: 
                break
            
            DauAnh.embed_text_block(block_bits, input_path, output_path, des_key, total_images=num_images, chunk_index=i+1)
            start += len(block_bits)
            print(f"Đã giấu block {i+1} vào {filename}, {len(block_bits)} bits")
        
        if start < total_bits:
            print(f"Cảnh báo: text chưa được giấu hết, còn {total_bits - start} bits chưa được sử dụng.")
    @staticmethod
    def extract_text_from_folder(input_folder, des_key):
        image_files = [f for f in os.listdir(input_folder) if f.lower().endswith(('.png','.jpg','.jpeg'))]
        
        chunks = []
        for filename in image_files:
            img_path = os.path.join(input_folder, filename)
            img = Image.open(img_path).convert("RGBA")
            alpha = np.array(img)[..., 3].flatten()
            
            encrypted_metadata = alpha[:8].tobytes()
            des = DES.new(des_key, DES.MODE_ECB)
            metadata = des.decrypt(encrypted_metadata)
            total_images = int.from_bytes(metadata[:4],'big')
            chunk_index = int.from_bytes(metadata[4:],'big')
            
            bits_extracted = ''
            for v in alpha[8:]:
                if v == 255:
                    bits_extracted += '1'
                elif v == 254:
                    bits_extracted += '0'
                else:
                    break  
            chunks.append((chunk_index, bits_extracted))
        
        chunks.sort(key=lambda x: x[0])
        all_bits = ''.join([b for _,b in chunks])
        return DauAnh.bits_to_text(all_bits)
    @staticmethod
    def plot_rgb_alpha(before_path, after_path):
        # Load ảnh
        img_before = Image.open(before_path).convert("RGBA")
        img_after = Image.open(after_path).convert("RGBA")
        
        data_before = np.array(img_before)
        data_after = np.array(img_after)
        
        # Flatten RGB channels
        Rb, Gb, Bb = data_before[...,0].flatten(), data_before[...,1].flatten(), data_before[...,2].flatten()
        Ra, Ga, Ba = data_after[...,0].flatten(), data_after[...,1].flatten(), data_after[...,2].flatten()
        
        # Alpha sau dấu
        alpha_after = data_after[...,3].flatten()
        
        # Tính histogram RGB (0-255)
        bins = np.arange(257)
        hist_R_before, _ = np.histogram(Rb, bins=bins)
        hist_G_before, _ = np.histogram(Gb, bins=bins)
        hist_B_before, _ = np.histogram(Bb, bins=bins)
        
        hist_R_after, _ = np.histogram(Ra, bins=bins)
        hist_G_after, _ = np.histogram(Ga, bins=bins)
        hist_B_after, _ = np.histogram(Ba, bins=bins)
        
        # Histogram alpha 3 mức (253, 254, 255)
        alpha_values, alpha_counts = np.unique(alpha_after, return_counts=True)
        mask = np.isin(alpha_values, [253,254,255])
        alpha_values, alpha_counts = alpha_values[mask], alpha_counts[mask]
        
        fig, axs = plt.subplots(2,2, figsize=(16,12))
        
        # RGB trước (line)
        axs[0,0].plot(hist_R_before, color='r', label='R')
        axs[0,0].plot(hist_G_before, color='g', label='G')
        axs[0,0].plot(hist_B_before, color='b', label='B')
        axs[0,0].set_title("Histogram RGB ảnh trước khi dấu tin")
        axs[0,0].set_xlabel("Giá trị pixel")
        axs[0,0].set_ylabel("Số pixel")
        axs[0,0].legend()
        
        # RGB sau (line)
        axs[0,1].plot(hist_R_after, color='r', label='R')
        axs[0,1].plot(hist_G_after, color='g', label='G')
        axs[0,1].plot(hist_B_after, color='b', label='B')
        axs[0,1].set_title("Histogram RGB ảnh sau khi dấu tin")
        axs[0,1].set_xlabel("Giá trị pixel")
        axs[0,1].set_ylabel("Số pixel")
        axs[0,1].legend()
        
        # Alpha 3 mức (line)
        axs[1,0].bar(alpha_values, alpha_counts, color='black', width=0.5, alpha=0.7, label='Alpha')
        axs[1,0].set_xticks([253, 254, 255])
        axs[1,0].set_xlim([252, 256])
        axs[1,0].set_title("Alpha channel 3 mức sau khi dấu")
        axs[1,0].set_xlabel("Giá trị alpha")
        axs[1,0].set_ylabel("Số pixel")
        axs[1,0].legend()

        
        # So sánh RGB trước vs sau (line)
        axs[1,1].plot(hist_R_before, color='r', alpha=0.4, label='R trước')
        axs[1,1].plot(hist_R_after, color='r', linestyle='--', label='R sau')
        axs[1,1].plot(hist_G_before, color='g', alpha=0.4, label='G trước')
        axs[1,1].plot(hist_G_after, color='g', linestyle='--', label='G sau')
        axs[1,1].plot(hist_B_before, color='b', alpha=0.4, label='B trước')
        axs[1,1].plot(hist_B_after, color='b', linestyle='--', label='B sau')
        axs[1,1].set_title("So sánh RGB trước vs sau (line)")
        axs[1,1].set_xlabel("Giá trị pixel")
        axs[1,1].set_ylabel("Số pixel")
        axs[1,1].legend()
        
        plt.tight_layout(pad=3.0)
        fig.subplots_adjust(hspace=0.35, wspace=0.3)
        plt.show()

if __name__ == "__main__":
    des_key = b'12345678'
    plaintext = "Hello, this is a long text that will be split across multiple images!"
    plaintext = plaintext*100000
    DauAnh.embed_text_in_folder(plaintext, "./Images", "./watermarked_images", des_key)
    recovered_text = DauAnh.extract_text_from_folder("./watermarked_images", des_key)
    
    print("Text giải mã:", recovered_text)

    # before_image = "./Images/anh.jpg"         
    # after_image  = "./watermarked_images/anh.jpg"  
    # DauAnh.plot_rgb_alpha(before_image, after_image)
