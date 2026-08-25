USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0014,
    'Xe Bọc Thép',
    'Hiện vật là xe bọc thép được sử dụng trong thời kỳ kháng chiến chống Mỹ nhằm phục vụ hoạt động cơ động chiến đấu, vận chuyển binh lính và hỗ trợ hỏa lực trên chiến trường. Đây là loại phương tiện quân sự có lớp giáp bảo vệ chắc chắn, giúp tăng khả năng sống sót cho lực lượng bên trong khi tham gia chiến đấu.

Xe bọc thép được chế tạo bằng thép hợp kim dày với thiết kế kín, có khả năng chống đạn và chịu tác động mạnh từ chiến trường. Phương tiện được trang bị động cơ công suất lớn, hệ thống bánh lốp hoặc bánh xích cùng vũ khí hỗ trợ như súng máy nhằm phục vụ chiến đấu và bảo vệ lực lượng cơ động.

Tại chiến trường Quân khu 9, xe bọc thép được sử dụng trong các hoạt động tuần tra, vận chuyển quân và hỗ trợ tiến công tại nhiều khu vực chiến lược. Sự xuất hiện của xe bọc thép phản ánh mức độ hiện đại của phương tiện quân sự trong chiến tranh và vai trò của lực lượng cơ giới trên chiến trường miền Tây Nam Bộ.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện hoạt động quân sự cơ giới trong thời kỳ kháng chiến chống Mỹ, đồng thời góp phần giáo dục truyền thống yêu nước và tinh thần đấu tranh bảo vệ Tổ quốc cho thế hệ hôm nay.',
    '1970',
    'Hoa Kỳ',
    1
);

INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    16,
    14,
    'Hình ảnh xe bọc thép trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/XeBocThep/XBT_01.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    85,
    14,
    'Lịch sử',
    'Xe bọc thép được sử dụng trong thời kỳ kháng chiến chống Mỹ nhằm phục vụ hoạt động cơ động chiến đấu, tuần tra và vận chuyển binh lính trên chiến trường miền Nam Việt Nam.',
    1
),

-- 2
(
    86,
    14,
    'Đặc điểm cấu tạo',
    'Xe được chế tạo bằng thép hợp kim dày với lớp giáp bảo vệ chắc chắn, trang bị động cơ công suất lớn cùng hệ thống bánh lốp hoặc bánh xích giúp di chuyển linh hoạt trên nhiều địa hình.',
    2
),

-- 3
(
    87,
    14,
    'Ý nghĩa lịch sử',
    'Hiện vật xe bọc thép phản ánh sự phát triển của phương tiện chiến đấu cơ giới trong chiến tranh và vai trò của lực lượng cơ giới trên chiến trường miền Tây Nam Bộ.',
    3
),

-- 4
(
    88,
    14,
    'Kích thước và thiết kế',
    'Xe có kích thước lớn với thân xe bọc giáp kín, khoang chở binh lính và vị trí gắn vũ khí chiến đấu. Thiết kế giúp tăng khả năng bảo vệ và cơ động trong điều kiện chiến trường.',
    4
),

-- 5
(
    89,
    14,
    'Cấu tạo bên trong',
    'Bên trong xe gồm khoang lái, khoang chở binh lính, hệ thống điều khiển và vị trí bố trí vũ khí. Các thiết bị liên lạc và hỗ trợ chiến đấu được lắp đặt nhằm phục vụ tác chiến hiệu quả.',
    5
),

-- 6
(
    90,
    14,
    'Cơ chế hoạt động',
    'Xe hoạt động bằng động cơ công suất lớn truyền lực đến hệ thống bánh xe hoặc bánh xích, cho phép di chuyển trên nhiều loại địa hình. Hệ thống vũ khí trên xe hỗ trợ lực lượng bên trong trong quá trình chiến đấu.',
    6
),

-- 7
(
    91,
    14,
    'Vai trò trong kháng chiến',
    'Trong thời kỳ chiến tranh, xe bọc thép giữ vai trò hỗ trợ cơ động lực lượng, tuần tra và tăng cường hỏa lực trên chiến trường. Đây là phương tiện quân sự quan trọng trong nhiều hoạt động chiến đấu tại miền Nam Việt Nam.',
    7
);