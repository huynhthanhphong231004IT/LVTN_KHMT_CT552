USE Bao_Tang_Quan_Khu_9;

USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0006,
    'Lu Hầm Bí Mật',
    'Hiện vật là lu hầm bí mật được quân và dân miền Tây Nam Bộ sử dụng trong thời kỳ kháng chiến chống Mỹ nhằm cất giấu tài liệu, vũ khí và che giấu cán bộ cách mạng trước sự truy lùng của đối phương. Lu hầm thường được ngụy trang khéo léo trong nhà dân, vườn cây hoặc khu vực căn cứ cách mạng, góp phần bảo vệ lực lượng cách mạng hoạt động bí mật an toàn trong vùng địch kiểm soát.

Lu được làm bằng đất nung hoặc xi măng chắc chắn, có kích thước đủ lớn để chứa tài liệu, vật dụng quân sự hoặc thậm chí một người ẩn náu trong thời gian ngắn. Bên ngoài lu thường được phủ đất, lá cây hoặc đặt dưới nền nhà để tránh bị phát hiện. Nhờ sự sáng tạo và kinh nghiệm của người dân địa phương, nhiều hầm bí mật đã tồn tại suốt thời gian dài mà đối phương không phát hiện được.

Trong thực tế chiến đấu tại Quân khu 9, lu hầm bí mật đóng vai trò quan trọng trong việc duy trì liên lạc, bảo vệ cơ sở cách mạng và hỗ trợ hoạt động của lực lượng du kích. Đây là phương tiện hỗ trợ hiệu quả cho chiến tranh nhân dân, thể hiện tinh thần đoàn kết quân dân và ý chí đấu tranh kiên cường của nhân dân miền Tây Nam Bộ.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện hình thức hoạt động bí mật của lực lượng cách mạng trong thời kỳ chiến tranh. Đồng thời, đây cũng là minh chứng sinh động cho sự mưu trí, sáng tạo và tinh thần bất khuất của quân và dân ta trong công cuộc đấu tranh giành độc lập dân tộc.',
    '1975',
    'Việt Nam',
    1
);

INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    8,
    6,
    'Hình ảnh lu hầm bí mật trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/LuHam/LH_01.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    29,
    6,
    'Lịch sử',
    'Lu hầm bí mật là phương tiện được quân và dân miền Tây Nam Bộ sử dụng trong thời kỳ kháng chiến chống Mỹ để che giấu cán bộ cách mạng, cất giữ tài liệu và vũ khí. Tại chiến trường Quân khu 9, nhiều hầm bí mật được xây dựng trong nhà dân và căn cứ cách mạng nhằm bảo vệ lực lượng hoạt động bí mật trước sự truy quét của đối phương.',
    1
),

-- 2
(
    30,
    6,
    'Đặc điểm cấu tạo',
    'Lu hầm bí mật thường được làm bằng đất nung hoặc xi măng chắc chắn, có hình tròn hoặc hình trụ với phần nắp được ngụy trang kỹ lưỡng. Thiết kế kín đáo giúp lu có thể chôn dưới nền nhà, dưới đất hoặc trong vườn cây mà khó bị phát hiện.',
    2
),

-- 3
(
    31,
    6,
    'Ý nghĩa lịch sử',
    'Hiện vật lu hầm bí mật là minh chứng cho sự mưu trí, sáng tạo của quân và dân ta trong chiến tranh. Việc sử dụng hầm bí mật góp phần bảo vệ lực lượng cách mạng, duy trì liên lạc và đảm bảo an toàn cho nhiều hoạt động bí mật trong vùng địch kiểm soát.',
    3
),

-- 4
(
    32,
    6,
    'Kích thước và thiết kế',
    'Lu hầm bí mật có kích thước đa dạng tùy theo mục đích sử dụng. Đường kính thường từ 0,8 đến 1,5 mét, chiều sâu đủ để chứa người hoặc cất giấu tài liệu, vũ khí. Phần nắp được thiết kế kín và dễ ngụy trang nhằm đảm bảo bí mật tuyệt đối.',
    4
),

-- 5
(
    33,
    6,
    'Cấu tạo bên trong',
    'Bên trong lu hầm thường có không gian chứa tài liệu, thuốc men, lương thực hoặc nơi ẩn náu tạm thời cho cán bộ cách mạng. Một số hầm còn được thiết kế hệ thống thông khí đơn giản nhằm đảm bảo an toàn cho người trú ẩn trong thời gian dài.',
    5
),

-- 6
(
    34,
    6,
    'Cơ chế hoạt động',
    'Lu hầm bí mật hoạt động dựa trên nguyên tắc ngụy trang và che giấu tuyệt đối. Khi cần thiết, cán bộ cách mạng hoặc tài liệu sẽ được đưa xuống hầm qua phần nắp kín đáo. Nhờ thiết kế tinh vi và bí mật, nhiều hầm đã tránh được sự phát hiện trong các cuộc kiểm tra và truy quét của đối phương.',
    6
),

-- 7
(
    35,
    6,
    'Vai trò trong kháng chiến',
    'Trong thời kỳ kháng chiến chống Mỹ, lu hầm bí mật giữ vai trò quan trọng trong việc bảo vệ cán bộ, lưu giữ tài liệu mật và hỗ trợ các hoạt động cách mạng tại miền Tây Nam Bộ. Đây là biểu tượng cho tinh thần kiên cường, đoàn kết và sự sáng tạo của quân dân Quân khu 9 trong đấu tranh giành độc lập dân tộc.',
    7
);