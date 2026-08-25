USE Bao_Tang_Quan_Khu_9;

INSERT INTO HIEN_VAT (MA_HIEN_VAT, TEN_HIEN_VAT, MO_TA, NAM, NGUON_GOC, MA_KHU)
VALUES (
    0009,
    'Mỏ Neo Tàu',
    'Hiện vật là mỏ neo tàu được sử dụng cho các phương tiện vận tải và tàu quân sự hoạt động trên sông nước miền Tây Nam Bộ trong thời kỳ kháng chiến chống Mỹ. Mỏ neo có vai trò giữ ổn định tàu thuyền khi dừng đỗ, hỗ trợ vận chuyển quân nhu, vũ khí và lực lượng chiến đấu tại các tuyến sông chiến lược thuộc Quân khu 9.

Mỏ neo được chế tạo bằng thép hoặc gang đúc chắc chắn với thiết kế gồm thân chính, hai cánh neo và vòng móc nối với dây xích hoặc dây neo. Nhờ trọng lượng lớn và cấu tạo bám chắc xuống đáy sông, mỏ neo giúp tàu thuyền cố định vị trí an toàn trong điều kiện dòng chảy mạnh hoặc thời tiết xấu.

Trong thực tế chiến đấu, các tàu vận tải và phương tiện quân sự trên sông thường sử dụng mỏ neo để dừng tại căn cứ, bến tập kết hoặc khu vực bí mật nhằm vận chuyển hàng hóa, tiếp tế hậu cần và hỗ trợ chiến đấu. Đây là thiết bị gắn liền với hoạt động chiến tranh sông nước đặc trưng của miền Tây Nam Bộ.

Hiện vật được lưu giữ tại Bảo tàng Quân khu 9 nhằm tái hiện hoạt động vận tải quân sự trên sông nước trong thời kỳ kháng chiến. Đồng thời, đây cũng là minh chứng cho vai trò quan trọng của lực lượng vận tải và hậu cần trong công cuộc đấu tranh bảo vệ Tổ quốc.',
    '1972',
    'Việt Nam',
    1
);

INSERT INTO HINH_ANH_HIEN_VAT (MA_ANH, MA_HIEN_VAT, MO_TA_HIEN_VAT, URL_ANH)
VALUES (
    11,
    9,
    'Hình ảnh mỏ neo tàu trưng bày tại bảo tàng',
    '/Hinh_anh/Hien_Vat/MoNeoTau/MNT_01.jpg'
);

INSERT INTO MO_TA_HIEN_VAT (MA_MO_TA, MA_HIEN_VAT, TIEU_DE, NOI_DUNG, THU_TU)
VALUES
-- 1
(
    50,
    9,
    'Lịch sử',
    'Mỏ neo tàu được sử dụng phổ biến trên các phương tiện vận tải và tàu quân sự hoạt động tại chiến trường miền Tây Nam Bộ trong thời kỳ kháng chiến chống Mỹ. Tại Quân khu 9, thiết bị này hỗ trợ các hoạt động vận chuyển quân nhu, vũ khí và lực lượng chiến đấu trên sông nước.',
    1
),

-- 2
(
    51,
    9,
    'Đặc điểm cấu tạo',
    'Mỏ neo được chế tạo bằng thép hoặc gang đúc chắc chắn với phần thân chính, hai cánh neo và vòng móc nối với dây neo hoặc dây xích. Thiết kế giúp neo bám chặt xuống đáy sông để giữ ổn định tàu thuyền.',
    2
),

-- 3
(
    52,
    9,
    'Ý nghĩa lịch sử',
    'Hiện vật mỏ neo tàu là minh chứng cho vai trò của lực lượng vận tải đường sông trong thời kỳ kháng chiến. Thiết bị góp phần đảm bảo hoạt động hậu cần, tiếp tế và vận chuyển lực lượng phục vụ chiến đấu tại Quân khu 9.',
    3
),

-- 4
(
    53,
    9,
    'Kích thước và thiết kế',
    'Mỏ neo có kích thước lớn với trọng lượng nặng nhằm tăng khả năng bám giữ dưới đáy sông. Thiết kế đối xứng với hai cánh neo cong giúp thiết bị ổn định khi sử dụng trong điều kiện dòng nước mạnh.',
    4
),

-- 5
(
    54,
    9,
    'Cấu tạo bên trong',
    'Mỏ neo có cấu tạo đơn giản gồm thân neo liền khối, cánh neo và vòng nối dây xích. Chất liệu kim loại dày giúp thiết bị chịu được áp lực lớn và hạn chế hư hỏng trong quá trình sử dụng lâu dài.',
    5
),

-- 6
(
    55,
    9,
    'Cơ chế hoạt động',
    'Khi thả xuống nước, mỏ neo sẽ chìm xuống đáy sông và các cánh neo cắm sâu vào bùn hoặc cát để giữ cố định tàu thuyền. Dây neo hoặc dây xích kết nối giúp phương tiện duy trì vị trí an toàn khi neo đậu.',
    6
),

-- 7
(
    56,
    9,
    'Vai trò trong kháng chiến',
    'Trong thời kỳ kháng chiến chống Mỹ, mỏ neo tàu giữ vai trò quan trọng trong việc hỗ trợ tàu vận tải và phương tiện quân sự neo đậu an toàn trên các tuyến sông chiến lược. Thiết bị góp phần phục vụ công tác hậu cần, tiếp tế và vận chuyển lực lượng tại Quân khu 9.',
    7
);