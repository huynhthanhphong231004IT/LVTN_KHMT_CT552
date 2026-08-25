import numpy as np

class TextEval:

    # =========================
    # 1. ACCURACY
    # =========================
    @staticmethod
    def accuracy(original, recovered):
        min_len = min(len(original), len(recovered))
        correct = sum(o == r for o, r in zip(original[:min_len], recovered[:min_len]))
        return correct / len(original) * 100


    # =========================
    # 2. BER (BIT ERROR RATE)
    # =========================
    @staticmethod
    def ber(original, recovered):
        o_bits = np.unpackbits(np.frombuffer(original.encode(), dtype=np.uint8))
        r_bits = np.unpackbits(np.frombuffer(recovered.encode(), dtype=np.uint8))

        m = min(len(o_bits), len(r_bits))
        return np.sum(o_bits[:m] != r_bits[:m]) / m * 100


    # =========================
    # 3. EXACT MATCH
    # =========================
    @staticmethod
    def exact_match(original, recovered):
        return original == recovered


    # =========================
    # 4. ENTROPY
    # =========================
    @staticmethod
    def entropy(text):
        data = np.frombuffer(text.encode(), dtype=np.uint8)
        hist = np.bincount(data, minlength=256)
        prob = hist / hist.sum()
        prob = prob[prob > 0]
        return -np.sum(prob * np.log2(prob))


    # =========================
    # 5. REDUNDANCY
    # =========================
    @staticmethod
    def redundancy(entropy):
        return 1 - entropy / 8


    # =========================
    # 6. CHI-SQUARE
    # =========================
    @staticmethod
    def chi_square(text):
        data = np.frombuffer(text.encode(), dtype=np.uint8)
        hist = np.bincount(data, minlength=256).astype(np.float64)

        expected = np.mean(hist)
        return np.sum((hist - expected) ** 2 / (expected + 1e-9))


    # =========================
    # 7. AVALANCHE TEXT
    # =========================
    @staticmethod
    def avalanche(text1, text2):
        b1 = np.unpackbits(np.frombuffer(text1.encode(), dtype=np.uint8))
        b2 = np.unpackbits(np.frombuffer(text2.encode(), dtype=np.uint8))

        m = min(len(b1), len(b2))
        return np.sum(b1[:m] != b2[:m]) / m * 100


    # =========================
    # 8. CORRELATION BYTE
    # =========================
    @staticmethod
    def correlation(text):
        data = np.frombuffer(text.encode(), dtype=np.uint8)

        if len(data) < 2:
            return 0

        a = data[:-1]
        b = data[1:]

        return np.corrcoef(a, b)[0, 1]