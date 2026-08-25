import numpy as np
import cv2
import math
import time
from skimage.metrics import peak_signal_noise_ratio as psnr
from skimage.metrics import structural_similarity as ssim

class DanhGia:
    # =========================
    # ENTROPY
    # =========================
    @staticmethod
    def entropy(img):
        hist = cv2.calcHist([img], [0], None, [256], [0,256])
        hist = hist / hist.sum()
        return -np.sum([p*np.log2(p) for p in hist if p != 0])


    # =========================
    # CORRELATION
    # =========================
    @staticmethod
    def correlation(img, direction="h"):
        img = img.astype(np.float64)

        if direction == "h":
            a = img[:, :-1].flatten()
            b = img[:, 1:].flatten()
        elif direction == "v":
            a = img[:-1, :].flatten()
            b = img[1:, :].flatten()
        else:
            a = img[:-1, :-1].flatten()
            b = img[1:, 1:].flatten()

        return np.corrcoef(a, b)[0,1]


    # =========================
    # NPCR
    # =========================
    @staticmethod
    def npcr(img1, img2):
        return np.sum(img1 != img2) / img1.size * 100


    # =========================
    # UACI
    # =========================
    @staticmethod
    def uaci(img1, img2):
        return np.mean(np.abs(img1 - img2) / 255) * 100


    # =========================
    # AVALANCHE
    # =========================
    @staticmethod
    def avalanche(bits1, bits2):
        m = min(len(bits1), len(bits2))
        return sum(b1 != b2 for b1,b2 in zip(bits1[:m], bits2[:m])) / m * 100


    # =========================
    # HISTOGRAM VARIANCE
    # =========================
    @staticmethod
    def hist_variance(img):
        hist = cv2.calcHist([img],[0],None,[256],[0,256])
        return np.var(hist)


    # =========================
    # CHI SQUARE
    # =========================
    @staticmethod
    def chi_square(img):
        hist = cv2.calcHist([img],[0],None,[256],[0,256]).flatten()
        expected = np.mean(hist)
        return np.sum((hist - expected)**2 / (expected + 1e-9))


    # =========================
    # FULL EVALUATION
    # =========================
    @staticmethod
    def full_evaluation(img1, img2, encrypt_time, decrypt_time):
        img1 = np.array(img1, dtype=np.uint8)
        img2 = np.array(img2, dtype=np.uint8)

        results = {}

        # Quality
        results["MSE"] = np.mean((img1 - img2)**2)
        results["PSNR"] = psnr(img1, img2, data_range=255)
        results["SSIM"] = ssim(img1, img2, data_range=255)

        # Entropy
        results["Entropy_plain"] = DanhGia.entropy(img1)
        results["Entropy_cipher"] = DanhGia.entropy(img2)

        # Correlation
        results["Corr_H"] = DanhGia.correlation(img2, "h")
        results["Corr_V"] = DanhGia.correlation(img2, "v")
        results["Corr_D"] = DanhGia.correlation(img2, "d")

        # Diffusion
        results["NPCR"] = DanhGia.npcr(img1, img2)
        results["UACI"] = DanhGia.uaci(img1, img2)

        # Histogram
        results["Hist_Var"] = DanhGia.hist_variance(img2)
        results["Chi_square"] = DanhGia.chi_square(img2)

        # Time
        results["Enc_time"] = encrypt_time
        results["Dec_time"] = decrypt_time

        return results