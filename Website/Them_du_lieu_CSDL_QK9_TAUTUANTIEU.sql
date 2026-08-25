USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0012,
    'Tàu Tuần Tiễu PCF',
    'Hiện vật là tàu tuần tiễu PCF từng được sử dụng trong chiến tranh tại khu vực sông nước miền Nam Việt Nam. PCF là viết tắt của Patrol Craft Fast, loại tàu tuần tra cao tốc được sử dụng nhằm kiểm soát tuyến sông, tuần tra, vận chuyển lực lượng và hỗ trợ chiến đấu trên các tuyến thủy lộ chiến lược.

Tàu được chế tạo bằng hợp kim nhôm với thiết kế nhỏ gọn, tốc độ cao và khả năng cơ động linh hoạt trên sông nước. PCF được trang bị động cơ mạnh cùng hệ thống vũ khí như súng máy và thiết bị thông tin liên lạc nhằm phục vụ hoạt động tuần tra và tác chiến trong điều kiện chiến tranh sông nước phức tạp.

Tại chiến trường miền Tây Nam Bộ và khu vực Quân khu 9, tàu tuần tiễu PCF xuất hiện trong nhiều hoạt động kiểm soát tuyến sông, hỗ trợ vận tải quân sự và tham gia các trận đánh trên sông. Hiện vật phản ánh rõ đặc điểm chiến tranh sông nước và vai trò của lực lượng hải quân, thủy quân trong thời kỳ kháng chiến chống Mỹ.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện hoạt động quân sự trên sông nước miền Tây Nam Bộ, đồng thời góp phần giáo dục truyền thống yêu nước và tinh thần đấu tranh bảo vệ Tổ quốc cho thế hệ hôm nay.',
    '1969',
    'Hoa Kỳ',
    1
);

INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    14,
    12,
    'Hình ảnh tàu tuần tiễu PCF trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/TauTuanTieuPCF/TTTPCF_01.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    71,
    12,
    'Lịch sử',
    'Tàu tuần tiễu PCF là loại tàu cao tốc được sử dụng trong chiến tranh nhằm tuần tra, kiểm soát và hỗ trợ hoạt động quân sự trên các tuyến sông chiến lược tại miền Nam Việt Nam. Đây là phương tiện đặc trưng của chiến tranh sông nước.',
    1
),

-- 2
(
    72,
    12,
    'Đặc điểm cấu tạo',
    'Tàu được chế tạo bằng hợp kim nhôm nhẹ với thiết kế nhỏ gọn, tốc độ cao và khả năng cơ động linh hoạt. Trên tàu được trang bị động cơ mạnh, hệ thống liên lạc và các loại súng máy phục vụ chiến đấu.',
    2
),

-- 3
(
    73,
    12,
    'Ý nghĩa lịch sử',
    'Hiện vật tàu tuần tiễu PCF phản ánh đặc điểm chiến tranh sông nước tại miền Tây Nam Bộ và vai trò của lực lượng hải quân trong thời kỳ kháng chiến chống Mỹ.',
    3
),

-- 4
(
    74,
    12,
    'Kích thước và thiết kế',
    'Tàu có chiều dài khoảng 15 đến 16 mét với thân tàu hẹp và trọng lượng nhẹ nhằm tăng tốc độ di chuyển trên sông nước. Thiết kế phù hợp cho hoạt động tuần tra và chiến đấu cơ động.',
    4
),

-- 5
(
    75,
    12,
    'Cấu tạo bên trong',
    'Bên trong tàu gồm khoang điều khiển, khu vực vận hành động cơ, khoang chứa nhiên liệu và vị trí bố trí vũ khí. Các thiết bị thông tin liên lạc được lắp đặt phục vụ công tác chỉ huy và tuần tra.',
    5
),

-- 6
(
    76,
    12,
    'Cơ chế hoạt động',
    'Tàu hoạt động bằng động cơ tốc độ cao kết hợp chân vịt giúp di chuyển nhanh trên sông và vùng nước nông. Hệ thống điều khiển hỗ trợ tàu cơ động linh hoạt trong quá trình tuần tra và tác chiến.',
    6
),

-- 7
(
    77,
    12,
    'Vai trò trong kháng chiến',
    'Trong thời kỳ chiến tranh, tàu tuần tiễu PCF giữ vai trò kiểm soát tuyến sông, hỗ trợ vận chuyển quân sự và tham gia các hoạt động chiến đấu trên sông nước miền Tây Nam Bộ.',
    7
);