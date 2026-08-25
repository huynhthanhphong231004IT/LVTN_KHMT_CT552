USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0013,
    'Trục Máy Bay B-52',
    'Hiện vật là bộ phận trục của máy bay ném bom chiến lược B-52 từng được sử dụng trong chiến tranh Việt Nam. Đây là một chi tiết kỹ thuật quan trọng thuộc hệ thống vận hành của máy bay, phản ánh quy mô và mức độ hiện đại của phương tiện quân sự được sử dụng trong chiến tranh.

Máy bay B-52 là loại máy bay ném bom chiến lược cỡ lớn do Hoa Kỳ sản xuất, được sử dụng trong nhiều chiến dịch không kích tại Việt Nam. Các bộ phận còn sót lại của máy bay sau chiến tranh trở thành chứng tích lịch sử quan trọng, ghi dấu những trận chiến ác liệt và tinh thần chiến đấu kiên cường của quân dân Việt Nam.

Trục máy bay được chế tạo bằng hợp kim kim loại có độ bền cao nhằm chịu được áp lực lớn trong quá trình vận hành. Bộ phận này tham gia vào cơ cấu truyền động và hỗ trợ hoạt động ổn định của máy bay trong điều kiện bay tốc độ cao và tải trọng lớn.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện một phần dấu tích chiến tranh không quân tại Việt Nam, đồng thời là minh chứng cho ý chí đấu tranh, tinh thần bảo vệ bầu trời Tổ quốc và chiến thắng của quân dân ta trong cuộc kháng chiến chống Mỹ cứu nước.',
    '1972',
    'Hoa Kỳ',
    1
);

INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    15,
    13,
    'Hình ảnh trục máy bay B-52 trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/TrucMayBayB52/TMBB52_01.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    78,
    13,
    'Lịch sử',
    'Trục máy bay B-52 là bộ phận thuộc máy bay ném bom chiến lược B-52 được sử dụng trong chiến tranh Việt Nam. Đây là dấu tích còn lại của loại máy bay từng tham gia nhiều chiến dịch không kích quy mô lớn.',
    1
),

-- 2
(
    79,
    13,
    'Đặc điểm cấu tạo',
    'Bộ phận trục được chế tạo bằng hợp kim kim loại có độ bền cao nhằm chịu được tải trọng lớn và áp lực mạnh trong quá trình vận hành của máy bay.',
    2
),

-- 3
(
    80,
    13,
    'Ý nghĩa lịch sử',
    'Hiện vật là minh chứng cho cuộc chiến tranh không quân tại Việt Nam và tinh thần chiến đấu kiên cường của quân dân ta trong cuộc kháng chiến chống Mỹ cứu nước.',
    3
),

-- 4
(
    81,
    13,
    'Kích thước và thiết kế',
    'Trục máy bay có kết cấu chắc chắn với hình dạng trụ dài, được gia công chính xác nhằm đảm bảo khả năng vận hành ổn định cho các bộ phận cơ khí của máy bay.',
    4
),

-- 5
(
    82,
    13,
    'Cấu tạo bên trong',
    'Bên trong trục gồm lõi kim loại chịu lực và các chi tiết cơ khí hỗ trợ truyền động. Vật liệu được xử lý đặc biệt nhằm tăng khả năng chịu nhiệt và chống mài mòn.',
    5
),

-- 6
(
    83,
    13,
    'Cơ chế hoạt động',
    'Trục hoạt động như một bộ phận truyền động cơ khí, giúp liên kết và hỗ trợ chuyển động của các hệ thống bên trong máy bay trong quá trình vận hành.',
    6
),

-- 7
(
    84,
    13,
    'Vai trò trong chiến tranh',
    'Bộ phận trục máy bay B-52 phản ánh trình độ kỹ thuật quân sự hiện đại của máy bay chiến lược thời kỳ chiến tranh, đồng thời là chứng tích lịch sử về cuộc đấu tranh bảo vệ bầu trời của quân dân Việt Nam.',
    7
);