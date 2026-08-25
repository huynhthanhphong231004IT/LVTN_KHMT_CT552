USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0015,
    'Xe Peugeot',
    'Hiện vật là xe Peugeot từng được sử dụng tại miền Nam Việt Nam trong giai đoạn chiến tranh và thời kỳ trước năm 1975. Đây là dòng xe ô tô do hãng Peugeot của Pháp sản xuất, nổi tiếng với thiết kế bền bỉ, phù hợp cho việc di chuyển trong điều kiện giao thông và địa hình phức tạp thời bấy giờ.

Xe được chế tạo với khung thép chắc chắn, động cơ đốt trong cùng hệ thống truyền động cơ khí ổn định. Phương tiện thường được sử dụng để vận chuyển cán bộ, liên lạc, phục vụ công tác hành chính hoặc di chuyển trong các khu vực đô thị và căn cứ quân sự.

Tại khu vực Quân khu 9 và miền Tây Nam Bộ, xe Peugeot xuất hiện trong nhiều hoạt động dân sự và quân sự, phản ánh đời sống, phương tiện giao thông và điều kiện hoạt động trong giai đoạn lịch sử trước năm 1975. Đây là loại phương tiện mang dấu ấn đặc trưng của thời kỳ chiến tranh và hậu chiến tại Việt Nam.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện hình ảnh phương tiện giao thông trong giai đoạn lịch sử đặc biệt của đất nước, đồng thời góp phần giúp thế hệ hôm nay hiểu rõ hơn về điều kiện sinh hoạt, di chuyển và hoạt động của quân dân miền Nam Việt Nam trong thời kỳ chiến tranh.',
    '1967',
    'Pháp',
    1
);

INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    17,
    15,
    'Hình ảnh xe Peugeot trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/XePeugeot/XP_01.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    92,
    15,
    'Lịch sử',
    'Xe Peugeot là dòng xe ô tô được sử dụng phổ biến tại miền Nam Việt Nam trong giai đoạn trước năm 1975, phục vụ nhu cầu đi lại, liên lạc và vận chuyển trong cả lĩnh vực dân sự lẫn quân sự.',
    1
),

-- 2
(
    93,
    15,
    'Đặc điểm cấu tạo',
    'Xe được chế tạo với khung thép chắc chắn, động cơ đốt trong và hệ thống truyền động cơ khí ổn định. Thiết kế phù hợp với điều kiện giao thông và khí hậu tại Việt Nam thời bấy giờ.',
    2
),

-- 3
(
    94,
    15,
    'Ý nghĩa lịch sử',
    'Hiện vật xe Peugeot phản ánh đời sống xã hội, phương tiện giao thông và điều kiện hoạt động trong giai đoạn lịch sử trước năm 1975 tại miền Nam Việt Nam.',
    3
),

-- 4
(
    95,
    15,
    'Kích thước và thiết kế',
    'Xe có thiết kế thân dài với khoang hành khách rộng, hệ thống cửa kính và khung gầm chắc chắn. Kiểu dáng mang đặc trưng của các dòng xe châu Âu trong thập niên 1960.',
    4
),

-- 5
(
    96,
    15,
    'Cấu tạo bên trong',
    'Bên trong xe gồm khoang lái, ghế hành khách, bảng điều khiển cơ khí và hệ thống vô lăng điều khiển. Nội thất được thiết kế đơn giản nhưng đảm bảo tiện nghi cơ bản cho người sử dụng.',
    5
),

-- 6
(
    97,
    15,
    'Cơ chế hoạt động',
    'Xe hoạt động bằng động cơ đốt trong sử dụng nhiên liệu xăng, kết hợp hộp số cơ khí và hệ thống truyền động giúp phương tiện di chuyển ổn định trên nhiều loại địa hình.',
    6
),

-- 7
(
    98,
    15,
    'Vai trò trong giai đoạn lịch sử',
    'Trong giai đoạn chiến tranh và trước năm 1975, xe Peugeot được sử dụng cho hoạt động di chuyển, liên lạc và phục vụ công tác hành chính. Đây là loại phương tiện phản ánh rõ nét đời sống và điều kiện giao thông của thời kỳ lịch sử đó.',
    7
);