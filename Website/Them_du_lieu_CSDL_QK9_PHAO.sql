USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0010,
    'Pháo',
    'Hiện vật là khẩu pháo từng được sử dụng trong thời kỳ kháng chiến chống Mỹ nhằm phục vụ chiến đấu, bảo vệ căn cứ và hỗ trợ hỏa lực cho lực lượng vũ trang tại chiến trường miền Tây Nam Bộ. Pháo là loại vũ khí có sức công phá lớn, đóng vai trò quan trọng trong các trận đánh quy mô lớn và hoạt động phòng thủ chiến lược của quân dân ta.

Khẩu pháo được chế tạo bằng thép hợp kim chắc chắn với nòng pháo dài, hệ thống giá đỡ và bánh xe cơ động giúp thuận tiện cho việc di chuyển trên nhiều địa hình khác nhau. Vũ khí này có khả năng bắn đạn ở khoảng cách xa với sức sát thương lớn, hỗ trợ tiêu diệt mục tiêu cố định, phương tiện quân sự và công sự của đối phương.

Trong thực tế chiến đấu tại Quân khu 9, pháo được sử dụng để yểm trợ bộ binh, phá hủy căn cứ đối phương và bảo vệ các khu vực chiến lược. Sự phối hợp giữa lực lượng pháo binh và bộ binh đã góp phần quan trọng vào nhiều chiến thắng trong thời kỳ kháng chiến chống Mỹ cứu nước.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện vai trò của lực lượng pháo binh trong chiến tranh giải phóng dân tộc. Đồng thời, đây cũng là minh chứng cho tinh thần chiến đấu kiên cường và ý chí quyết thắng của quân và dân miền Tây Nam Bộ.',
    '1972',
    'Liên Xô',
    1
);

INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    12,
    10,
    'Hình ảnh khẩu pháo trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/Phao/PH_01.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    57,
    10,
    'Lịch sử',
    'Khẩu pháo được sử dụng trong thời kỳ kháng chiến chống Mỹ nhằm hỗ trợ hỏa lực cho lực lượng vũ trang tại chiến trường miền Tây Nam Bộ. Tại Quân khu 9, pháo góp phần yểm trợ bộ binh và tham gia nhiều trận đánh quan trọng.',
    1
),

-- 2
(
    58,
    10,
    'Đặc điểm cấu tạo',
    'Pháo được chế tạo bằng thép hợp kim chắc chắn với nòng pháo dài, hệ thống khóa nòng, giá đỡ và bánh xe cơ động. Thiết kế giúp vũ khí có khả năng chịu áp lực lớn khi khai hỏa.',
    2
),

-- 3
(
    59,
    10,
    'Ý nghĩa lịch sử',
    'Hiện vật khẩu pháo là minh chứng cho vai trò quan trọng của lực lượng pháo binh trong chiến tranh giải phóng dân tộc. Vũ khí này góp phần nâng cao sức mạnh chiến đấu và hỗ trợ các chiến dịch quân sự tại Quân khu 9.',
    3
),

-- 4
(
    60,
    10,
    'Kích thước và thiết kế',
    'Khẩu pháo có kích thước lớn với nòng dài, thân pháo chắc chắn và hệ thống bánh xe phục vụ việc kéo, di chuyển. Thiết kế phù hợp với hoạt động chiến đấu trên nhiều loại địa hình.',
    4
),

-- 5
(
    61,
    10,
    'Cấu tạo bên trong',
    'Bên trong pháo gồm hệ thống nòng pháo, buồng đạn, khóa nòng và cơ cấu khai hỏa. Các bộ phận được thiết kế chính xác nhằm đảm bảo an toàn và hiệu quả trong quá trình sử dụng.',
    5
),

-- 6
(
    62,
    10,
    'Cơ chế hoạt động',
    'Khi khai hỏa, thuốc phóng trong đạn tạo áp lực lớn đẩy đầu đạn bay ra khỏi nòng với tốc độ cao. Hệ thống điều chỉnh góc bắn và hướng bắn giúp pháo tấn công mục tiêu ở khoảng cách xa với độ chính xác cao.',
    6
),

-- 7
(
    63,
    10,
    'Vai trò trong kháng chiến',
    'Trong thời kỳ kháng chiến chống Mỹ, pháo giữ vai trò quan trọng trong việc yểm trợ hỏa lực, phá hủy công sự đối phương và bảo vệ căn cứ chiến lược. Đây là loại vũ khí góp phần quan trọng vào thắng lợi của quân dân Quân khu 9.',
    7
);