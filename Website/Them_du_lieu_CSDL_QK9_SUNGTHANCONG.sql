USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0011,
    'Súng Thần Công',
    'Hiện vật là súng thần công được sử dụng trong các giai đoạn lịch sử bảo vệ đất nước nhằm phục vụ phòng thủ và chiến đấu tại các khu vực trọng yếu. Đây là loại vũ khí hỏa lực lớn xuất hiện từ thời phong kiến, thường được bố trí tại thành lũy, căn cứ quân sự và các tuyến phòng thủ ven sông nhằm chống lại sự tấn công của đối phương.

Súng thần công được đúc bằng đồng hoặc gang với thân pháo lớn, nòng dài và trọng lượng nặng. Vũ khí hoạt động bằng cách sử dụng thuốc súng để phóng đạn ra xa với sức công phá mạnh. Nhờ uy lực lớn, súng thần công từng là phương tiện chiến đấu quan trọng trong nhiều trận chiến lịch sử của dân tộc Việt Nam.

Tại khu vực Nam Bộ và Quân khu 9, súng thần công được xem là biểu tượng của nghệ thuật quân sự và kỹ thuật chế tạo vũ khí thời kỳ trước. Hiện vật phản ánh quá trình đấu tranh bảo vệ lãnh thổ và khả năng chế tạo vũ khí của quân dân ta trong lịch sử.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện hình ảnh vũ khí cổ trong lịch sử quân sự Việt Nam, đồng thời góp phần giáo dục truyền thống yêu nước, tinh thần bảo vệ Tổ quốc và niềm tự hào dân tộc cho thế hệ hôm nay.',
    '1850',
    'Việt Nam',
    1
);

INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    13,
    11,
    'Hình ảnh súng thần công trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/SungThanCong/STC_01.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    64,
    11,
    'Lịch sử',
    'Súng thần công là loại vũ khí hỏa lực lớn được sử dụng trong nhiều giai đoạn lịch sử của dân tộc Việt Nam nhằm bảo vệ thành lũy, căn cứ quân sự và các tuyến phòng thủ chiến lược. Đây là một trong những loại pháo cổ tiêu biểu trong lịch sử quân sự Việt Nam.',
    1
),

-- 2
(
    65,
    11,
    'Đặc điểm cấu tạo',
    'Súng thần công thường được đúc bằng đồng hoặc gang với thân pháo dày, nòng dài và trọng lượng lớn. Thiết kế chắc chắn giúp vũ khí chịu được áp lực cao khi khai hỏa bằng thuốc súng.',
    2
),

-- 3
(
    66,
    11,
    'Ý nghĩa lịch sử',
    'Hiện vật súng thần công là minh chứng cho trình độ kỹ thuật chế tạo vũ khí của cha ông ta trong lịch sử. Đồng thời, đây cũng là biểu tượng cho tinh thần đấu tranh bảo vệ độc lập và chủ quyền dân tộc.',
    3
),

-- 4
(
    67,
    11,
    'Kích thước và thiết kế',
    'Súng thần công có kích thước lớn với chiều dài nòng từ vài mét đến hơn 4 mét tùy loại. Phần thân được đúc liền khối với các hoa văn và tay cầm phục vụ việc di chuyển hoặc cố định trên bệ pháo.',
    4
),

-- 5
(
    68,
    11,
    'Cấu tạo bên trong',
    'Bên trong súng gồm lòng pháo rỗng để chứa thuốc súng và đạn pháo. Phía sau thân pháo có lỗ nhỏ dùng để châm lửa kích hoạt thuốc súng khi khai hỏa.',
    5
),

-- 6
(
    69,
    11,
    'Cơ chế hoạt động',
    'Khi sử dụng, thuốc súng và đạn được nạp vào nòng pháo. Người vận hành sẽ châm lửa qua lỗ khai hỏa để tạo áp lực đẩy đầu đạn bay ra khỏi nòng với sức công phá lớn.',
    6
),

-- 7
(
    70,
    11,
    'Vai trò trong lịch sử quân sự',
    'Trong lịch sử quân sự Việt Nam, súng thần công giữ vai trò quan trọng trong việc phòng thủ thành trì, bảo vệ tuyến sông và chống lại sự tấn công của đối phương. Đây là loại vũ khí góp phần thể hiện sức mạnh quân sự và tinh thần bảo vệ Tổ quốc của dân tộc.',
    7
);