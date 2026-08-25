USE Bao_Tang_Quan_Khu_9;
INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0005,
    'Ghe Xuồng Thuyền',
    'Hiện vật là ghe xuồng và thuyền từng được sử dụng trong thời kỳ kháng chiến chống Mỹ tại khu vực Đồng bằng sông Cửu Long, đặc biệt trong các hoạt động chiến đấu, vận chuyển lương thực, vũ khí và đưa đón cán bộ cách mạng. Với hệ thống sông ngòi chằng chịt của miền Tây Nam Bộ, ghe xuồng trở thành phương tiện di chuyển chủ yếu, gắn liền với đời sống sinh hoạt và chiến đấu của quân dân ta trong suốt thời kỳ chiến tranh.

Ghe xuồng được chế tạo chủ yếu bằng gỗ chắc chắn, có thiết kế nhỏ gọn, linh hoạt và phù hợp với điều kiện địa hình sông nước. Một số loại xuồng còn được cải tiến để tăng khả năng cơ động, ngụy trang và vận chuyển hàng hóa trong đêm nhằm tránh sự phát hiện của đối phương. Nhờ đặc điểm nhẹ và dễ điều khiển, ghe xuồng có thể di chuyển qua các con rạch nhỏ, vùng ngập nước và khu vực căn cứ cách mạng.

Trong thực tế chiến đấu tại Quân khu 9, ghe xuồng đóng vai trò đặc biệt quan trọng trong việc tiếp tế hậu cần, vận chuyển thương binh, đưa lực lượng vũ trang tiếp cận mục tiêu và hỗ trợ các trận đánh trên sông. Nhiều chiến sĩ đã sử dụng ghe xuồng để bí mật vượt qua tuyến kiểm soát của đối phương, góp phần duy trì liên lạc và đảm bảo hoạt động cách mạng tại miền Tây Nam Bộ.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện hình ảnh chiến đấu trên sông nước của quân và dân miền Tây trong thời kỳ kháng chiến. Đây không chỉ là phương tiện di chuyển đơn thuần mà còn là biểu tượng của sự sáng tạo, kiên cường và ý chí vượt khó của dân tộc Việt Nam trong công cuộc đấu tranh giành độc lập và bảo vệ Tổ quốc.',
    '1975',
    'Việt Nam',
    1
);
INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    7,
    5,
    'Hình ảnh xe tăng trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/LuHam/LH_01.jpg'
);


INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    22,
    5,
    'Lịch sử',
    'Ghe xuồng và thuyền là phương tiện di chuyển chủ yếu của quân và dân miền Tây Nam Bộ trong thời kỳ kháng chiến chống Mỹ. Tại chiến trường Quân khu 9, ghe xuồng được sử dụng để vận chuyển lương thực, vũ khí, đưa đón cán bộ và phục vụ các trận đánh trên sông nước. Đây là phương tiện gắn liền với hoạt động cách mạng trong điều kiện địa hình sông ngòi chằng chịt của Đồng bằng sông Cửu Long.',
    1
),

-- 2
(
    23,
    5,
    'Đặc điểm cấu tạo',
    'Ghe xuồng thường được đóng bằng gỗ chắc chắn, thiết kế thon dài và có trọng lượng nhẹ nhằm thuận tiện cho việc di chuyển trên sông, rạch và vùng ngập nước. Một số loại xuồng được cải tiến để tăng khả năng cơ động, dễ ngụy trang và chở được nhiều hàng hóa hoặc người trong điều kiện chiến đấu.',
    2
),

-- 3
(
    24,
    5,
    'Ý nghĩa lịch sử',
    'Hiện vật ghe xuồng thuyền tại Bảo tàng Quân khu 9 là minh chứng cho tinh thần sáng tạo, kiên cường và ý chí vượt khó của quân dân miền Tây trong kháng chiến. Phương tiện này góp phần quan trọng vào việc duy trì liên lạc, vận chuyển tiếp tế và hỗ trợ các hoạt động chiến đấu trong thời kỳ chiến tranh.',
    3
),

-- 4
(
    25,
    5,
    'Kích thước và thiết kế',
    'Ghe xuồng có nhiều kích thước khác nhau tùy theo mục đích sử dụng. Chiều dài trung bình từ 3 đến 10 mét, chiều rộng khoảng 0,8 đến 2 mét. Thiết kế nhỏ gọn giúp phương tiện dễ dàng len lỏi qua các con rạch nhỏ và khu vực sông nước phức tạp của miền Tây Nam Bộ.',
    4
),

-- 5
(
    26,
    5,
    'Cấu tạo bên trong',
    'Bên trong ghe xuồng thường được chia thành khoang chở người, hàng hóa và khu vực điều khiển. Một số ghe có mái che hoặc ngăn chứa bí mật dùng để cất giấu tài liệu, thuốc men và vũ khí nhằm phục vụ hoạt động cách mạng và tránh sự phát hiện của đối phương.',
    5
),

-- 6
(
    27,
    5,
    'Cơ chế hoạt động',
    'Ghe xuồng hoạt động chủ yếu bằng sức chèo tay hoặc sử dụng động cơ nhỏ. Nhờ thiết kế nhẹ và linh hoạt, phương tiện có thể di chuyển thuận lợi trên nhiều tuyến sông, kênh rạch và vùng ngập nước. Trong chiến đấu, ghe xuồng giúp lực lượng cách mạng cơ động nhanh chóng, bí mật tiếp cận mục tiêu và vận chuyển hậu cần hiệu quả.',
    6
),

-- 7
(
    28,
    5,
    'Vai trò trong kháng chiến',
    'Trong thời kỳ kháng chiến chống Mỹ, ghe xuồng giữ vai trò quan trọng trong việc vận chuyển lương thực, vũ khí, thương binh và cán bộ qua các tuyến sông nước miền Tây. Đây còn là phương tiện phục vụ trinh sát, liên lạc và hỗ trợ các trận đánh trên sông, góp phần quan trọng vào thắng lợi của quân dân Quân khu 9.',
    7
);
