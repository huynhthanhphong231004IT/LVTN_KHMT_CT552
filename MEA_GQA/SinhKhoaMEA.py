import numpy as np
from typing import List
import random

random.seed(42)
np.random.seed(42)

def int_det(mat: np.ndarray) -> int:
    d = round(float(np.linalg.det(mat)))
    return int(d)

def random_transform_preserve_det(base: np.ndarray,
                                  max_ops: int = 20,
                                  mult_range: tuple[int,int] = (-5,5),
                                  value_bounds: tuple[int,int] = (0,255)) -> np.ndarray:
    for attempt in range(10000):  # giới hạn thử nhiều lần
        M = base.copy().astype(np.int64)  # dùng int64 để tránh vượt tràn tạm thời
        ops = random.randint(5, max_ops)
        for _ in range(ops):
            i, j = random.sample(range(3), 2)
            r = 0
            # tránh r == 0
            while r == 0:
                r = random.randint(mult_range[0], mult_range[1])
            # thực hiện phép biến đổi hàng
            M[i, :] = M[i, :] + r * M[j, :]
        # kiểm tra giới hạn giá trị
        # kiểm tra giới hạn giá trị và không có phần tử 0
        if M.min() <= 0 or M.max() > value_bounds[1] or np.any(M == 0):
            continue

                # kiểm tra det = 3
        if int_det(M) == 3:
            return M.astype(np.uint8)
    # nếu không tìm được trong số lần thử, trả về None
    return None
class SinhKhoaMEA:
    def generate_matrices(n: int = 35) -> List[List[List[str]]]:
        base = np.diag([1, 1, 3]).astype(np.int64)  # det = 3
        matrices = []
        tries_total = 0
        while len(matrices) < n and tries_total < 5000:
            tries_total += 1
            M = random_transform_preserve_det(base, max_ops=30, mult_range=(-6,6), value_bounds=(0,255))
            if M is None:
                continue
            # chuyển thành list of hex strings
            mat_hex = [[format(int(M[r,c]), '02X') for c in range(3)] for r in range(3)]
            # tránh trùng lặp
            if mat_hex not in matrices:
                matrices.append(mat_hex)
        if len(matrices) < n:
            raise RuntimeError(f"Không thể sinh đủ {n} ma trận trong giới hạn thử ({len(matrices)} được sinh).")
        return matrices
import time
if __name__ == "__main__":
    start_decrypt = time.time() 
    key_hex_35 = SinhKhoaMEA.generate_matrices(35)
    end_decrypt = time.time()  # kết thúc đếm thời gian giải mã
    print(f"\nThời gian giải mã: {end_decrypt - start_decrypt:.3f} giây")

    # in ra 35 ma trận dưới định dạng giống bạn yêu cầu
    print("key_hex = [")
    for mat in key_hex_35:
        print("    [")
        for row in mat:
            print("        {}".format(row) + ",")
        print("    ],")
    print("]")
