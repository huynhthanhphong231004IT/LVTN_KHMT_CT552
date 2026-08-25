USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0008,
    'Máy In Pédal',
    'Hiện vật là máy in Pédal được sử dụng trong thời kỳ kháng chiến chống Mỹ nhằm phục vụ công tác in ấn tài liệu, truyền đơn, báo chí cách mạng và văn bản phục vụ hoạt động tuyên truyền tại chiến trường miền Tây Nam Bộ. Đây là thiết bị quan trọng góp phần duy trì công tác thông tin, tuyên truyền và liên lạc của lực lượng cách mạng trong điều kiện chiến tranh khó khăn.

Máy in được chế tạo bằng kim loại chắc chắn với cơ chế vận hành bằng bàn đạp chân kết hợp hệ thống ép mực cơ học. Thiết bị cho phép in chữ trên giấy với số lượng lớn hơn so với phương pháp thủ công thông thường, phù hợp với điều kiện hoạt động bí mật tại các cơ sở in ấn cách mạng thời chiến.

Trong thực tế hoạt động tại Quân khu 9, máy in Pédal được sử dụng để in tài liệu tuyên truyền, thông báo, khẩu hiệu và các văn bản phục vụ công tác chỉ đạo chiến đấu. Những tài liệu được in từ máy góp phần nâng cao tinh thần chiến đấu, tuyên truyền đường lối cách mạng và cổ vũ nhân dân tham gia kháng chiến.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện vai trò của công tác tuyên truyền và thông tin liên lạc trong thời kỳ kháng chiến chống Mỹ. Đồng thời, đây cũng là minh chứng cho tinh thần sáng tạo, vượt khó và ý chí đấu tranh bền bỉ của quân và dân miền Tây Nam Bộ.',
    '1965',
    'Pháp',
    1
);

INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    10,
    8,
    'Hình ảnh máy in Pédal trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/MayInPedal/MIP_01.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    43,
    8,
    'Lịch sử',
    'Máy in Pédal được sử dụng trong thời kỳ kháng chiến chống Mỹ nhằm phục vụ công tác in ấn tài liệu, báo chí và truyền đơn cách mạng. Tại Quân khu 9, thiết bị này góp phần hỗ trợ công tác tuyên truyền và truyền tải thông tin đến lực lượng vũ trang và nhân dân.',
    1
),

-- 2
(
    44,
    8,
    'Đặc điểm cấu tạo',
    'Máy được chế tạo bằng kim loại chắc chắn với hệ thống ép mực cơ học và bàn đạp chân để vận hành. Thiết kế cơ khí đơn giản nhưng hiệu quả giúp máy hoạt động ổn định trong điều kiện thời chiến.',
    2
),

-- 3
(
    45,
    8,
    'Ý nghĩa lịch sử',
    'Hiện vật máy in Pédal là minh chứng cho vai trò quan trọng của công tác tuyên truyền trong kháng chiến. Thiết bị góp phần truyền tải thông tin, cổ vũ tinh thần chiến đấu và lan tỏa phong trào cách mạng tại miền Tây Nam Bộ.',
    3
),

-- 4
(
    46,
    8,
    'Kích thước và thiết kế',
    'Máy in có kích thước trung bình với khung thép chắc chắn, bàn đạp đặt phía dưới và mặt ép giấy phía trên. Thiết kế phù hợp với việc sử dụng trong các cơ sở in ấn thủ công thời chiến.',
    4
),

-- 5
(
    47,
    8,
    'Cấu tạo bên trong',
    'Bên trong máy gồm hệ thống bánh răng, trục ép, khay mực và bộ phận giữ khuôn chữ in. Các chi tiết cơ khí được lắp ráp đơn giản nhằm thuận tiện cho việc sửa chữa và bảo trì trong điều kiện thiếu thốn vật tư.',
    5
),

-- 6
(
    48,
    8,
    'Cơ chế hoạt động',
    'Máy hoạt động bằng lực đạp chân kết hợp cơ chế ép mực lên giấy. Khi vận hành, hệ thống trục ép sẽ truyền lực giúp in nội dung từ khuôn chữ lên bề mặt giấy với tốc độ nhanh và rõ nét.',
    6
),

-- 7
(
    49,
    8,
    'Vai trò trong kháng chiến',
    'Trong thời kỳ kháng chiến chống Mỹ, máy in Pédal giữ vai trò quan trọng trong việc in tài liệu tuyên truyền, khẩu hiệu và văn bản phục vụ chỉ đạo chiến đấu. Thiết bị góp phần duy trì công tác thông tin liên lạc và nâng cao tinh thần đấu tranh của quân dân Quân khu 9.',
    7
);