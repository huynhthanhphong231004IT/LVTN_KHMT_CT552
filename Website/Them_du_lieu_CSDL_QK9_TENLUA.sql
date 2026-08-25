USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0003,
    'Bệ và đạn tên lửa',
    'Hiện vật là bệ phóng và đạn tên lửa phòng không vác vai, được sử dụng trong kháng chiến chống Mỹ nhằm tiêu diệt các mục tiêu bay thấp như máy bay và trực thăng của đối phương. Loại vũ khí này có tính cơ động cao, phù hợp với điều kiện chiến trường miền Tây Nam Bộ.

Bệ phóng được thiết kế gọn nhẹ, có thể mang vác và triển khai nhanh chóng. Đạn tên lửa có đầu tự dẫn hồng ngoại, cho phép bám bắt mục tiêu dựa trên nguồn nhiệt phát ra từ động cơ máy bay. Khi phóng, tên lửa bay theo quỹ đạo định hướng và tự động điều chỉnh để đánh trúng mục tiêu.

Tại chiến trường Quân khu 9, loại vũ khí này được sử dụng hiệu quả trong việc hạn chế hoạt động không kích của đối phương, góp phần bảo vệ lực lượng và căn cứ cách mạng. Sự xuất hiện của tên lửa phòng không đã tạo ra bước chuyển biến quan trọng trong tác chiến phòng không của quân đội ta.

Hiện vật được lưu giữ tại bảo tàng nhằm tái hiện sự phát triển của vũ khí hiện đại trong chiến tranh, đồng thời là minh chứng cho sự hỗ trợ quốc tế và tinh thần chiến đấu kiên cường của quân và dân Việt Nam.',
    '1968',
    'Liên Xô',
    1
);



INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    5,
    3,
    'Hình ảnh bệ và đạn tên lửa trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/TenLua/TenLua_01.jpg'
);
INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    4,
    2,
    'Hình ảnh xe tăng trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/XeTang/XeTang_02.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    8,
    2,
    'Lịch sử',
    'Xe tăng là phương tiện chiến đấu chủ lực của lực lượng thiết giáp, được sử dụng trong kháng chiến chống Mỹ, đặc biệt trong giai đoạn cao điểm của cuộc Tổng tiến công và nổi dậy mùa Xuân năm 1975. Tại chiến trường Quân khu 9, xe tăng tham gia nhiều trận đánh quan trọng, góp phần phá vỡ hệ thống phòng thủ của đối phương và hỗ trợ bộ binh tiến công giải phóng các khu vực chiến lược.',
    1
),

-- 2
(
    9,
    2,
    'Đặc điểm kỹ thuật',
    'Xe tăng được chế tạo bằng thép dày, có khả năng chống đạn và chịu được tác động mạnh từ chiến trường. Trang bị chính gồm pháo cỡ lớn đặt trên tháp pháo xoay 360 độ và súng máy đồng trục, cho phép tấn công nhiều mục tiêu khác nhau. Xe sử dụng động cơ mạnh, hệ thống bánh xích giúp di chuyển linh hoạt trên nhiều địa hình phức tạp.',
    2
),

-- 3
(
    10,
    2,
    'Ý nghĩa lịch sử',
    'Hiện vật xe tăng trưng bày tại bảo tàng là minh chứng cho vai trò quan trọng của lực lượng thiết giáp trong chiến tranh giải phóng dân tộc. Qua đó góp phần giáo dục truyền thống yêu nước, tinh thần chiến đấu anh dũng và ý chí kiên cường của quân và dân ta, đồng thời khẳng định giá trị của hòa bình và độc lập dân tộc.',
    3
),

-- 4
(
    11,
    2,
    'Kích thước và khối lượng',
    'Xe tăng có chiều dài trung bình từ 6 đến 9 mét, rộng khoảng 3 đến 3,5 mét và cao khoảng 2,5 đến 3 mét. Khối lượng thường dao động từ 14 đến hơn 40 tấn tùy loại. Kích thước lớn và trọng lượng nặng giúp xe có độ ổn định cao khi chiến đấu và di chuyển.',
    4
),

-- 5
(
    12,
    2,
    'Cấu tạo bên trong',
    'Bên trong xe tăng gồm nhiều khoang chức năng như khoang lái, khoang chiến đấu và khoang động cơ. Xe thường có kíp lái từ 3 đến 4 người, bao gồm trưởng xe, lái xe, pháo thủ và nạp đạn. Hệ thống điều khiển, ngắm bắn và liên lạc được bố trí khoa học nhằm đảm bảo hiệu quả tác chiến.',
    5
),

-- 6
(
    13,
    2,
    'Cơ chế hoạt động',
    'Xe tăng hoạt động nhờ động cơ công suất lớn kết hợp với hệ thống bánh xích, cho phép di chuyển trên nhiều loại địa hình như đường đất, đồng ruộng và khu vực ngập nước. Khi chiến đấu, pháo chính và súng máy được điều khiển để tiêu diệt mục tiêu, trong khi lớp giáp bảo vệ giúp xe giảm thiểu thiệt hại từ hỏa lực đối phương.',
    6
),

-- 7
(
    14,
    2,
    'Phạm vi và hiệu quả chiến đấu',
    'Xe tăng có khả năng tấn công mục tiêu ở khoảng cách xa với độ chính xác cao. Nhờ hỏa lực mạnh và khả năng cơ động tốt, xe tăng đóng vai trò quan trọng trong việc đột phá phòng tuyến, hỗ trợ bộ binh và chiếm giữ mục tiêu chiến lược. Sự xuất hiện của xe tăng trên chiến trường thường tạo ưu thế lớn cả về quân sự và tâm lý.',
    7
);


