USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0007,
    'Máy Cán Tol',
    'Hiện vật là máy cán tol được sử dụng trong thời kỳ kháng chiến chống Mỹ nhằm phục vụ công tác cơ khí, sửa chữa và gia công vật liệu cho lực lượng quân giới tại chiến trường miền Tây Nam Bộ. Máy có nhiệm vụ cán và ép các tấm kim loại mỏng để chế tạo, sửa chữa dụng cụ, vật tư quân sự và nhiều trang thiết bị phục vụ chiến đấu.

Máy cán tol được chế tạo bằng thép chắc chắn với hệ thống trục cán cơ khí hoạt động bằng tay quay hoặc động cơ. Thiết bị có khả năng ép và làm phẳng các tấm kim loại với độ chính xác cao, phù hợp với điều kiện sản xuất thủ công trong thời chiến. Nhờ kết cấu bền vững và dễ vận hành, máy được sử dụng rộng rãi tại các xưởng quân giới và căn cứ cách mạng.

Trong thực tế tại Quân khu 9, máy cán tol góp phần quan trọng trong việc tự sản xuất và sửa chữa các vật dụng phục vụ chiến đấu, đảm bảo nguồn cung vật tư trong điều kiện chiến tranh khó khăn và thiếu thốn. Đây là minh chứng cho tinh thần tự lực, sáng tạo của lực lượng quân giới và quân dân miền Tây Nam Bộ trong kháng chiến.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện hoạt động sản xuất cơ khí thời chiến và vai trò của ngành quân giới trong công cuộc đấu tranh giải phóng dân tộc. Đồng thời, đây cũng là tư liệu lịch sử quý giá giúp giáo dục truyền thống yêu nước và tinh thần vượt khó của quân dân ta.',
    '1968',
    'Việt Nam',
    1
);

INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    9,
    7,
    'Hình ảnh máy cán tol trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/MayCanTol/MCT_01.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    36,
    7,
    'Lịch sử',
    'Máy cán tol được sử dụng tại các xưởng quân giới trong thời kỳ kháng chiến chống Mỹ nhằm phục vụ công tác gia công kim loại, sửa chữa và sản xuất vật tư quân sự. Tại Quân khu 9, thiết bị này góp phần hỗ trợ hoạt động hậu cần và đảm bảo trang bị phục vụ chiến đấu.',
    1
),

-- 2
(
    37,
    7,
    'Đặc điểm cấu tạo',
    'Máy được chế tạo bằng thép chắc chắn với hệ thống trục cán cơ khí và tay quay vận hành. Cấu tạo đơn giản nhưng bền vững giúp máy hoạt động ổn định trong điều kiện sản xuất thủ công thời chiến.',
    2
),

-- 3
(
    38,
    7,
    'Ý nghĩa lịch sử',
    'Hiện vật máy cán tol là minh chứng cho tinh thần tự lực, sáng tạo của lực lượng quân giới trong thời kỳ kháng chiến. Thiết bị góp phần phục vụ sản xuất, sửa chữa vật tư quân sự và đảm bảo nhu cầu chiến đấu tại chiến trường miền Tây Nam Bộ.',
    3
),

-- 4
(
    39,
    7,
    'Kích thước và thiết kế',
    'Máy cán tol có kích thước trung bình với hệ thống khung thép vững chắc, các trục cán được bố trí song song nhằm tạo lực ép lên bề mặt kim loại. Thiết kế phù hợp với điều kiện làm việc tại các xưởng cơ khí thời chiến.',
    4
),

-- 5
(
    40,
    7,
    'Cấu tạo bên trong',
    'Bên trong máy gồm hệ thống bánh răng, trục cán và bộ phận điều chỉnh khoảng cách ép giữa các trục. Các chi tiết cơ khí được lắp ráp đơn giản nhằm thuận tiện cho việc sửa chữa và bảo trì trong điều kiện thiếu thốn vật tư.',
    5
),

-- 6
(
    41,
    7,
    'Cơ chế hoạt động',
    'Máy hoạt động dựa trên cơ chế quay trục cán để ép và làm phẳng các tấm kim loại. Người vận hành sử dụng tay quay hoặc động cơ để điều khiển chuyển động của trục, tạo áp lực giúp gia công vật liệu theo yêu cầu.',
    6
),

-- 7
(
    42,
    7,
    'Vai trò trong kháng chiến',
    'Trong thời kỳ kháng chiến chống Mỹ, máy cán tol giữ vai trò quan trọng trong việc gia công và sửa chữa vật tư phục vụ chiến đấu. Thiết bị góp phần đảm bảo hoạt động hậu cần, sản xuất quân giới và hỗ trợ lực lượng vũ trang tại Quân khu 9.',
    7
);