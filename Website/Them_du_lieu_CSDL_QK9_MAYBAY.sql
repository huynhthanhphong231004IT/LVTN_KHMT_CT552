USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0016,
    'Máy Bay Trực Thăng',
    'Hiện vật là máy bay trực thăng từng được sử dụng trong thời kỳ chiến tranh Việt Nam nhằm phục vụ các hoạt động trinh sát, vận chuyển quân, cứu hộ và hỗ trợ tác chiến trên chiến trường. Đây là loại phương tiện hàng không có khả năng cất và hạ cánh thẳng đứng, phù hợp với điều kiện địa hình phức tạp tại miền Nam Việt Nam.

Máy bay trực thăng được chế tạo bằng hợp kim kim loại nhẹ với hệ thống cánh quạt chính đặt phía trên thân máy bay và cánh quạt đuôi giúp giữ ổn định hướng bay. Phương tiện được trang bị động cơ công suất lớn cùng hệ thống điều khiển hiện đại nhằm phục vụ hoạt động bay linh hoạt ở nhiều độ cao và điều kiện thời tiết khác nhau.

Tại chiến trường Quân khu 9 và khu vực miền Tây Nam Bộ, máy bay trực thăng được sử dụng trong các hoạt động tuần tra, vận chuyển binh lính, tiếp tế hậu cần và yểm trợ chiến đấu. Sự xuất hiện của trực thăng phản ánh vai trò quan trọng của không quân trong chiến tranh hiện đại và các hoạt động quân sự trên chiến trường sông nước.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện hoạt động hàng không quân sự trong thời kỳ kháng chiến chống Mỹ, đồng thời góp phần giáo dục truyền thống yêu nước và tinh thần đấu tranh bảo vệ Tổ quốc cho thế hệ hôm nay.',
    '1971',
    'Hoa Kỳ',
    1
);

INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    18,
    16,
    'Hình ảnh máy bay trực thăng trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/MayBayTrucThang/MBTT_01.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    99,
    16,
    'Lịch sử',
    'Máy bay trực thăng được sử dụng rộng rãi trong chiến tranh Việt Nam nhằm phục vụ các hoạt động vận chuyển quân, trinh sát, cứu hộ và hỗ trợ tác chiến trên chiến trường miền Nam.',
    1
),

-- 2
(
    100,
    16,
    'Đặc điểm cấu tạo',
    'Máy bay được chế tạo bằng hợp kim nhẹ với hệ thống cánh quạt chính và cánh quạt đuôi giúp duy trì khả năng bay ổn định và cơ động linh hoạt.',
    2
),

-- 3
(
    101,
    16,
    'Ý nghĩa lịch sử',
    'Hiện vật máy bay trực thăng phản ánh vai trò quan trọng của lực lượng không quân trong chiến tranh hiện đại và các hoạt động quân sự tại miền Nam Việt Nam.',
    3
),

-- 4
(
    102,
    16,
    'Kích thước và thiết kế',
    'Máy bay có thân dài, khoang chở người và hàng hóa rộng cùng hệ thống càng đáp phục vụ việc cất hạ cánh trên nhiều loại địa hình khác nhau.',
    4
),

-- 5
(
    103,
    16,
    'Cấu tạo bên trong',
    'Bên trong máy bay gồm buồng lái, hệ thống điều khiển, khoang hành khách hoặc vận tải cùng các thiết bị liên lạc và dẫn đường phục vụ hoạt động bay.',
    5
),

-- 6
(
    104,
    16,
    'Cơ chế hoạt động',
    'Máy bay hoạt động nhờ động cơ truyền lực đến cánh quạt chính tạo lực nâng giúp cất cánh thẳng đứng và di chuyển trên không. Cánh quạt đuôi hỗ trợ giữ ổn định hướng bay.',
    6
),

-- 7
(
    105,
    16,
    'Vai trò trong chiến tranh',
    'Trong thời kỳ chiến tranh, máy bay trực thăng giữ vai trò vận chuyển quân, tiếp tế hậu cần, trinh sát và hỗ trợ chiến đấu. Đây là phương tiện hàng không quan trọng trong nhiều hoạt động quân sự tại miền Nam Việt Nam.',
    7
);