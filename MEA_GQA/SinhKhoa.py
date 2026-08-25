import random, os, time, hashlib
import numpy as np
from sympy import factorint
class SinhKhoa:
    def entropy_seed():
        t = str(time.time()).encode()
        h = hashlib.sha256(os.urandom(32) + t).hexdigest()
        return int(h, 16) % (2**31)
    def rand_int(a, b):
        random.seed(SinhKhoa.entropy_seed())
        return random.randint(a, b)
    def la_so_nguyen_to(n, k=8):
        if n < 2:
            return False
        if n % 2 == 0:
            return n == 2
        r, d = 0, n - 1
        while d % 2 == 0:
            r += 1
            d //= 2
        for _ in range(k):
            a = SinhKhoa.rand_int(2, n - 2)
            x = pow(a, d, n)
            if x == 1 or x == n - 1:
                continue
            for _ in range(r - 1):
                x = pow(x, 2, n)
                if x == n - 1:
                    break
            else:
                return False
        return True
    def sinh_so_nguyen_to(min_p, max_p):
        while True:
            p = SinhKhoa.rand_int(min_p, max_p)
            if SinhKhoa.la_so_nguyen_to(p):
                return p
    def tim_phan_tu_sinh(p):
        if p == 2:
            return 1
        phi = p - 1
        factors = set()
        d = 2
        x = phi
        while d * d <= x:
            if x % d == 0:
                factors.add(d)
                while x % d == 0:
                    x //= d
            d += 1 if d == 2 else 2
        if x > 1:
            factors.add(x)
        for g in range(2, p):
            ok = True
            for q in factors:
                if pow(g, phi // q, p) == 1:
                    ok = False
                    break
            if ok:
                return g
        raise ValueError("Không tìm được primitive root")

    def mod_matrix_inverse(A, mod):
        A = np.array(A, dtype=int) % mod
        n = A.shape[0]
        I = np.eye(n, dtype=int)
        Aug = np.concatenate((A, I), axis=1) % mod
        for i in range(n):
            if Aug[i, i] % mod == 0:
                for r in range(i+1, n):
                    if Aug[r, i] % mod != 0:
                        Aug[[i, r]] = Aug[[r, i]]
                        break
            pivot = Aug[i, i] % mod
            if pivot == 0:
                raise ValueError("Ma trận không khả nghịch modulo", mod)
            inv_pivot = pow(int(pivot), -1, mod)
            Aug[i] = (Aug[i] * inv_pivot) % mod
            for r in range(n):
                if r != i:
                    factor = Aug[r, i] % mod
                    Aug[r] = (Aug[r] - factor * Aug[i]) % mod
        A_inv = Aug[:, n:] % mod
        return A_inv.astype(int)
    def sinh_ma_tran_S(mod, n=3):
            while True:
                A = np.random.randint(0, mod, size=(n,n))
                try:
                    A_inv = SinhKhoa.mod_matrix_inverse(A, mod)
                    return A
                except Exception:
                    continue
    def sinh_ma_tran_det_3():
        np.random.seed(SinhKhoa.entropy_seed())
        while True:
            M = np.random.randint(1000, 10000, (3, 3))
            det = round(np.linalg.det(M))
            if det == 0:
                continue
            scale = (3 / abs(det)) ** (1/3)
            M = np.rint(M * scale).astype(int)

            new_det = round(np.linalg.det(M))
            if new_det == 3:
                np.random.shuffle(M)
                M = M[:, np.random.permutation(3)]
                return M
    def sinh_35_ma_tran():
        tap_ma_tran = []
        while len(tap_ma_tran) < 36:
            M = SinhKhoa.sinh_ma_tran_det_3()
            tap_ma_tran.append(M)
        return tap_ma_tran
    def sinh_p_B():
        p = random.randint(0, 1000)
        if 0 <= p < 1:
            B = random.randint(60, 180)
        elif 1 <= p < 5:
            B = random.randint(40, 74)
        elif 5 <= p < 20:
            B = random.randint(25, 45)
        elif 20 <= p < 100:
            B = random.randint(18, 30)
        elif 100 <= p < 300:
            B = random.randint(12, 18)
        elif 300 <= p < 700:
            B = random.randint(9, 12)
        elif 700 <= p <= 1000:
            B = random.randint(8, 9)
        return p, B
    
    def sinh_8byte_des():
        bits = [str(random.randint(0, 1)) for _ in range(64)]
        key_bytes = int(''.join(bits), 2).to_bytes(8, byteorder='big')
        return key_bytes

    def Sinh_khoa():
        p = SinhKhoa.sinh_so_nguyen_to(100, 500)  
        g = SinhKhoa.tim_phan_tu_sinh(p)
        S = SinhKhoa.sinh_ma_tran_S(p)
        ds35_matran_K = SinhKhoa.sinh_35_ma_tran()
        K = SinhKhoa.sinh_8byte_des()
        p1,B = SinhKhoa.sinh_p_B()
        return [p,g,S,ds35_matran_K,p1,B,K]

if __name__ == "__main__":
    print("Sinh ra khóa")
    Key = SinhKhoa.Sinh_khoa()
    print(f"p = {Key[0]}")
    print(f"g = {Key[1]}")
    print(f"S = {Key[2]}")
    j = 1
    for i in Key[3]:
        print(f"K_{j} = {i}")
        j +=1
    print(f"Z = {Key[4]}")
    p = SinhKhoa.sinh_so_nguyen_to(100,10000)
    g = SinhKhoa.tim_phan_tu_sinh(p)
    print(f"Phần tử sinh modulo {p} là g = {g}")
