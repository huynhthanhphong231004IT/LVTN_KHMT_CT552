package com.example.data

data class QuestionItem(
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)

object QuestionBank {
    fun getQuestionForArtifact(artifactId: Int, cellIndex: Int, isCorner: Boolean, position: String = ""): QuestionItem {
        val pool = questionPools[artifactId] ?: defaultPool
        val idx = if (isCorner) {
            when (position) {
                "TL" -> 9
                "TR" -> 10
                "BL" -> 11
                "BR" -> 12
                else -> 9
            }
        } else {
            if (cellIndex >= 0) cellIndex else 0
        }
        return pool[idx % pool.size]
    }

    private val defaultPool = listOf(
        QuestionItem("Cổ vật này thuộc thời kỳ lịch sử nào?", listOf("Kháng chiến chống Pháp", "Kháng chiến chống Mỹ", "Thời phong kiến", "Thời tiền sử"), "Kháng chiến chống Mỹ")
    )

    private val questionPools = mapOf(
        // 1: Bom
        1 to listOf(
            QuestionItem("Quả bom lớn nhất từng ném xuống miền Bắc Việt Nam có tên gọi phổ biến là gì?", listOf("Bom tấn", "Bom tinh khôn", "Bom rải thảm", "Bom hạt nhân"), "Bom tấn"),
            QuestionItem("Chiến dịch không kích quy mô lớn nhất bằng pháo đài bay B-52 vào miền Bắc diễn ra năm nào?", listOf("1972", "1968", "1965", "1975"), "1972"),
            QuestionItem("Loại bom từ trường rải xuống các con sông miền Bắc để ngăn chặn giao thông gọi là gì?", listOf("Bom từ trường", "Bom bi", "Bom na-pan", "Bom điện tử"), "Bom từ trường"),
            QuestionItem("Hệ thống hầm trú ẩn phòng tránh bom cho người dân thời chống Mỹ được gọi là gì?", listOf("Hầm cá nhân", "Hầm trú ẩn tập thể", "Hầm bê tông", "Hầm ngầm"), "Hầm cá nhân"),
            QuestionItem("Loại bom nổ sát thương diện rộng phát ra vô số viên bi nhỏ được gọi là gì?", listOf("Bom bi", "Bom hóa học", "Bom lửa", "Bom chùm"), "Bom bi")
        ),

        // 2: Bệ và đạn tên lửa SAM-2
        2 to listOf(
            QuestionItem("Hệ thống tên lửa SAM-2 bắn hạ pháo đài bay B-52 vào năm nào tại Hà Nội?", listOf("1972", "1968", "1965", "1975"), "1972"),
            QuestionItem("Tên lửa SAM-2 do quốc gia nào sản xuất viện trợ cho Việt Nam?", listOf("Liên Xô", "Trung Quốc", "Triều Tiên", "Đức"), "Liên Xô"),
            QuestionItem("Kỹ thuật vượt nhiễu sóng radar của trắc thủ tên lửa Việt Nam gọi là gì?", listOf("Vạch nhiễu tìm thù", "Bắn mù", "Bắn ứng dụng", "Rải cạm bẫy"), "Vạch nhiễu tìm thù"),
            QuestionItem("Chiến thắng 12 ngày đêm đập tan đợt tập kích B-52 cuối năm 1972 được ví như sự kiện gì?", listOf("Điện Biên Phủ trên không", "Bạch Đằng trên không", "Ngọc Hồi Đống Đa", "Đại thắng Mùa Xuân"), "Điện Biên Phủ trên không")
        ),

        // 3: Ghe, xuồng, thuyền
        3 to listOf(
            QuestionItem("Đội quân nào nổi tiếng gắn liền với những chiếc xuồng ba lá trong phong trào Đồng Khởi?", listOf("Đội quân tóc dài", "Đội quân cảm tử", "Đội du kích Ba Tơ", "Đội thiếu niên tiền phong"), "Đội quân tóc dài"),
            QuestionItem("Phong trào Đồng Khởi năm 1960 nổ ra đầu tiên tại tỉnh nào của vùng sông nước miền Tây?", listOf("Bến Tre", "Cần Thơ", "Tiền Giang", "An Giang"), "Bến Tre"),
            QuestionItem("Nữ anh hùng lãnh đạo phong trào Đồng Khởi Bến Tre là ai?", listOf("Nguyễn Thị Định", "Võ Thị Sáu", "Nguyễn Thị Minh Khai", "Mẹ Suốt"), "Nguyễn Thị Định")
        ),

        // 4: Lỗ hầm bí mật
        4 to listOf(
            QuestionItem("Căn hầm bí mật giấu vũ khí đánh Dinh Độc Lập Tet Mau Than 1968 nằm ở đâu?", listOf("Quận 3, TPHCM", "Củ Chi", "Tây Ninh", "Bình Dương"), "Quận 3, TPHCM"),
            QuestionItem("Hệ thống địa đạo nổi tiếng nhất ở Nam Bộ phục vụ chiến đấu chống Mỹ là gì?", listOf("Địa đạo Củ Chi", "Địa đạo Vịnh Mốc", "Địa đạo Kế Sách", "Địa đạo Tam Tam"), "Địa đạo Củ Chi"),
            QuestionItem("Địa đạo Củ Chi có tổng chiều dài ước tính khoảng bao nhiêu km?", listOf("Hơn 200 km", "50 km", "100 km", "500 km"), "Hơn 200 km")
        ),

        // 5: Máy cán tol
        5 to listOf(
            QuestionItem("Máy cán tol sản xuất quân trang chiến khu phục vụ quân dân miền Nam giai đoạn nào?", listOf("1954 - 1975", "1930 - 1945", "1945 - 1954", "1975 - 1986"), "1954 - 1975"),
            QuestionItem("Công cụ kỹ thuật máy cán tol giúp gia công loại vật liệu nào chính?", listOf("Tôn và kim loại tấm", "Vải bạt", "Gỗ ép", "Nhựa dẻo"), "Tôn và kim loại tấm")
        ),

        // 6: Máy in pedal
        6 to listOf(
            QuestionItem("Máy in pedal chân đạp được dùng chủ yếu để in ấn loại ấn phẩm nào trong chiến khu?", listOf("Truyền đơn và báo chí cách mạng", "Sách giáo khoa", "Thư từ cá nhân", "Tiền giấy công khống"), "Truyền đơn và báo chí cách mạng"),
            QuestionItem("Báo Tiền Phong và báo Giải Phóng thời chiến được in ấn bí mật bằng máy in gì?", listOf("Máy in pedal đạp chân", "Máy in laser", "Máy in offset hiện đại", "Máy photostat"), "Máy in pedal đạp chân")
        ),

        // 7: Mỏ neo tàu
        7 to listOf(
            QuestionItem("Mỏ neo tàu chiến bảo tàng gợi nhớ về tuyến con đường huyền thoại nào trên biển?", listOf("Đường Hồ Chí Minh trên biển", "Đường 559", "Đường 9 Nam Lào", "Đường sông Cửu Long"), "Đường Hồ Chí Minh trên biển"),
            QuestionItem("Các con tàu không số vận chuyển vũ khí chi viện miền Nam xuất phát chính từ đâu?", listOf("Bến K15 - Đồ Sơn (Hải Phòng)", "Bến Cam Ranh", "Bến Vũng Tàu", "Bến Đà Nẵng"), "Bến K15 - Đồ Sơn (Hải Phòng)")
        ),

        // 8: Phao
        8 to listOf(
            QuestionItem("Phao cứu sinh và phao vượt sông được lực lượng nào sử dụng nhiều nhất?", listOf("Đặc công nước & Bộ đội công binh", "Không quân", "Pháo binh", "Tăng thiết giáp"), "Đặc công nước & Bộ đội công binh"),
            QuestionItem("Trận đánh chìm tàu chiến Mỹ trên sông Lòng Tàu gắn liền với lực lượng nào?", listOf("Đặc công Rừng Sác", "Du kích Bến Tre", "Quân đoàn 1", "Bộ đội Biên phòng"), "Đặc công Rừng Sác")
        ),

        // 9: Súng thần công
        9 to listOf(
            QuestionItem("Súng thần công cổ đúc bằng đồng hoặc sắt thường được trang bị ở đâu thời phong kiến?", listOf("Thành lũy và chiến thuyền", "Bộ binh nhẹ", "Kỵ binh", "Hầm ngầm"), "Thành lũy và chiến thuyền"),
            QuestionItem("Chủ quyền biển đảo Việt Nam thời Nguyễn gắn liền với hải đội nổi tiếng nào?", listOf("Hải đội Hoàng Sa", "Hải đội Trường Sa", "Hải đội Côn Đảo", "Hải đội Phú Quốc"), "Hải đội Hoàng Sa")
        ),

        // 10: Trục máy bay B-52
        10 to listOf(
            QuestionItem("Chiếc máy bay B-52 bị bắn rơi xuống hồ Hữu Ngọc (Hà Nội) vào tháng 12/1972 thuộc phi đội nào?", listOf("Không lực Hoa Kỳ (USAF)", "Hải quân Mỹ", "Lục quân Mỹ", "Thủy quân lục chiến"), "Không lực Hoa Kỳ (USAF)"),
            QuestionItem("Chiến dịch 12 ngày đêm lịch sử đánh bại tập kích B-52 diễn ra vào năm nào?", listOf("1972", "1968", "1965", "1975"), "1972")
        ),

        // 11: Tàu tuần tiểu PCF
        11 to listOf(
            QuestionItem("Tàu tuần tiểu PCF (Swift Boat) được quân đội Mỹ sử dụng tuần tra ở địa hình nào?", listOf("Sông rạch và ven biển miền Nam", "Vùng biển sâu đại dương", "Trên sông Hồng", "Hồ Ba Bể"), "Sông rạch và ven biển miền Nam"),
            QuestionItem("Tàu Swift Boat PCF có ưu điểm nổi bật nào khi tác chiến vùng sông nước?", listOf("Tốc độ cao và cơ động", "Có bệ phóng tên lửa hạt nhân", "Chặn sóng radar hoàn toàn", "Chở được xe tăng"), "Tốc độ cao và cơ động")
        ),

        // 12: Xe bọc thép
        12 to listOf(
            QuestionItem("Xe bọc thép M113 lội nước được quân đội giải phóng thu giữ và sử dụng trong chiến dịch nào?", listOf("Chiến dịch Hồ Chí Minh 1975", "Trận Điện Biên Phủ 1954", "Chiến dịch Biên giới 1950", "Chiến dịch Việt Bắc"), "Chiến dịch Hồ Chí Minh 1975"),
            QuestionItem("Hỏa lực chính trang bị trên xe bọc thép M113 là loại súng nào?", listOf("Súng máy 12.7mm (Cal 50)", "Pháo 125mm", "Súng ngắm", "Mìn hông"), "Súng máy 12.7mm (Cal 50)")
        ),

        // 13: Xe Peugeot
        13 to listOf(
            QuestionItem("Chiếc xe Peugeot cổ trưng bày tại bảo tàng từng phục vụ cho cán bộ lãnh đạo nào?", listOf("Cán bộ chỉ huy / Biệt động Sài Gòn", "Phi công", "Trắc thủ tên lửa", "Lính dù"), "Cán bộ chỉ huy / Biệt động Sài Gòn"),
            QuestionItem("Thương hiệu xe ô tô Peugeot nổi tiếng có xuất xứ từ quốc gia nào?", listOf("Pháp", "Đức", "Mỹ", "Anh"), "Pháp")
        ),

        // 14: Xe tăng 390
        14 to listOf(
            QuestionItem("Số hiệu của chiếc xe tăng húc đổ cổng chính Dinh Độc Lập trưa ngày 30/4/1975 là bao nhiêu?", listOf("Xe tăng 390", "Xe tăng 843", "Xe tăng 384", "Xe tăng 398"), "Xe tăng 390"),
            QuestionItem("Ai là trưởng xe chỉ huy chiếc xe tăng 390 húc sập cổng chính Dinh Độc Lập?", listOf("Vũ Đăng Toàn", "Bùi Quang Thận", "Ngô Sỹ Nguyên", "Nguyễn Văn Tập"), "Vũ Đăng Toàn"),
            QuestionItem("Đại đội trưởng Bùi Quang Thận chỉ huy chiếc xe tăng 843 húc vào cổng phụ thuộc đại đội nào?", listOf("Đại đội 4", "Đại đội 1", "Đại đội 3", "Đại đội 9"), "Đại đội 4"),
            QuestionItem("Lữ đoàn xe tăng nào dẫn đầu mũi tiến công húc cổng Dinh Độc Lập ngày 30/4/1975?", listOf("Lữ đoàn 203", "Lữ đoàn 215", "Lữ đoàn 273", "Lữ đoàn 201"), "Lữ đoàn 203"),
            QuestionItem("Lá cờ giải phóng được cắm trên nóc Dinh Độc Lập vào thời điểm nào ngày 30/4/1975?", listOf("11 giờ 30 phút", "10 giờ 30 phút", "12 giờ 00 phút", "9 giờ 15 phút"), "11 giờ 30 phút")
        ),

        // 15: Máy bay trực thang
        15 to listOf(
            QuestionItem("Loại máy bay trực thăng UH-1 Huey do Mỹ sản xuất có chức năng chính là gì?", listOf("Chở quân & Cấp cứu thương binh", "Ném bom nguyên tử", "Bắn tên lửa hành trình", "Rà phá bom mìn"), "Chở quân & Cấp cứu thương binh"),
            QuestionItem("Âm thanh cánh quạt đặc trưng của trực thăng UH-1 gắn liền với chiến trường nào?", listOf("Chiến trường Việt Nam", "Chiến trường Triều Tiên", "Chiến trường Thái Bình Dương", "Chiến trường Châu Âu"), "Chiến trường Việt Nam")
        )
    )
}
