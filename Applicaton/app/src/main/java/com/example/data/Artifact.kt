package com.example.data

val MUSEUM_LOCATION = Location(
    latitude = 10.030027777777778, 
    longitude = 105.771222222222222, 
    label = "Vị trí Test"
)

val MUSEUM_LOCATION_2 = Location(
    latitude = 10.030972, 
    longitude = 105.768972, 
    label = "Vị trí Test"
)

val MUSEUM_LOCATION_3 = Location(
    latitude = 10.030028, 
    longitude = 105.763528, 
    label = "Vị trí Test"
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val label: String? = null 
)

data class Artifact(
    val id: Int,
    val name: String,
    val description: String,
    val locations: List<Location>,
    val gameType: GameType,
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val funFact: String
) {
    val latitude: Double
        get() = locations.firstOrNull()?.latitude ?: MUSEUM_LOCATION.latitude

    val longitude: Double
        get() = locations.firstOrNull()?.longitude ?: MUSEUM_LOCATION.longitude
}

enum class GameType {
    QUIZ,        
    GUESS_YEAR,   
    CODE_BREAKER  
}

object MuseumRepository {
    val artifacts = listOf(
        Artifact(
            id = 1,
            name = "Bom",
            description = "Quả bom khổng lồ được sử dụng trong các chiến dịch không kích oanh tạc dữ dội, thể hiện sự khốc liệt của chiến tranh phá hoại.",
            locations = listOf(
                Location(latitude = 10.0356111111111, longitude = 105.785388888888, label = "Vị trí 1"),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.QUIZ,
            question = "Quả bom lớn nhất từng ném xuống miền Bắc Việt Nam có tên gọi phổ biến là gì?",
            options = listOf("Bom tinh khôn", "Bom tấn", "Bom rải thảm", "Bom hạt nhân"),
            correctAnswer = "Bom tấn",
            funFact = "Sau chiến tranh, nhiều vỏ bom đã được người dân tận dụng làm chuông báo hoặc dụng cụ sinh hoạt độc đáo."
        ),
        Artifact(
            id = 2,
            name = "Bệ và đạn tên lửa",
            description = "Hệ thống tên lửa SAM-2 huyền thoại đã lập chiến công bắn rơi nhiều máy bay chiến lược tầm cao của quân địch.",
            locations = listOf(
                Location(latitude = 10.0359444444444, longitude = 105.785527777777, label = "Vị trí 1"),
                Location(latitude = 10.0356666666666, longitude = 105.784861111111, label = "Vị trí 2"),
                MUSEUM_LOCATION ,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.GUESS_YEAR,
            question = "Hệ thống tên lửa SAM-2 bắn hạ pháo đài bay B-52 vào năm nào tại Hà Nội?",
            correctAnswer = "1972",
            funFact = "SAM-2 là vũ khí phòng không do Liên Xô viện trợ, nhưng được các chiến sĩ Việt Nam sáng tạo ra cách đánh độc đáo để bắn hạ B-52."
        ),
        Artifact(
            id = 3,
            name = "Ghe, xuồng, thuyền",
            description = "Những phương tiện vận tải thô sơ được sử dụng để chở vũ khí, lương thực dọc các tuyến sông miền Tây trong phong trào Đồng Khởi.",
            locations = listOf(
                Location(latitude = 10.0355555555555, longitude = 105.7855),
                Location(latitude = 10.0355555555555, longitude = 105.785138888888),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.QUIZ,
            question = "Đội quân nào nổi tiếng gắn liền với những chiếc xuồng ba lá trong phong trào Đồng Khởi?",
            options = listOf("Đội quân tóc dài", "Đội quân cảm tử", "Đội du kích Ba Tơ", "Đội thiếu niên tiền phong"),
            correctAnswer = "Đội quân tóc dài",
            funFact = "Phương tiện nhỏ gọn này đã tạo nên mạng lưới vận tải thủy chằng chịt, qua mắt được hệ thống đồn bốt dày đặc của đối phương."
        ),
        Artifact(
            id = 4,
            name = "Lu hầm bí mật",
            description = "Chiếc lu đất nung khổng lồ được cải tiến tinh vi thành miệng hầm bí mật để nuôi giấu cán bộ kháng chiến ngay dưới lòng đất.",
            locations = listOf(
                Location(latitude = 10.0355555555555, longitude = 105.7855),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.CODE_BREAKER,
            question = "Giải mã mật thư sau để tìm mật danh liên lạc: 'H_M_B_M_T' (Bỏ dấu gạch dưới)",
            correctAnswer = "HAMBIMAT",
            funFact = "Chiếc lu này trông hoàn toàn bình thường bên ngoài nhưng đáy lu được khoét rỗng liên thông với địa đạo dài hàng chục mét."
        ),
        Artifact(
            id = 5,
            name = "Máy cán tol",
            description = "Thiết bị dùng để cán các tấm tôn, gia cố hầm hào, công sự bảo vệ bộ đội trước làn đạn pháo dày đặc.",
            locations = listOf(
                Location(latitude = 10.0355555555555, longitude = 105.7855),
                Location(latitude = 10.0355555555555, longitude = 105.785138888888),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.QUIZ,
            question = "Các tấm tôn thường được dùng để che chắn bộ phận nào trong chiến hào chống mảnh pháo?",
            options = listOf("Mái hầm", "Đường đi", "Bếp Hoàng Cầm", "Cột cờ"),
            correctAnswer = "Mái hầm",
            funFact = "Tol (tôn) phế liệu từ các căn cứ quân sự địch thường được du kích tịch thu về để chế tạo mìn tự chế và gia cố công sự."
        ),
        Artifact(
            id = 6,
            name = "Máy in Pédal",
            description = "Máy in truyền đơn bằng chân vô cùng thô sơ nằm sâu trong rừng rậm, giúp phát hành hàng ngàn tờ truyền đơn cổ động cách mạng.",
            locations = listOf(
                Location(latitude = 10.0355555555555, longitude = 105.785138888888),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.GUESS_YEAR,
            question = "Những tờ truyền đơn đầu tiên kêu gọi Toàn quốc kháng chiến được in và phát tán rộng rãi vào năm nào?",
            correctAnswer = "1946",
            funFact = "Tiếng động của máy in được ngụy trang khéo léo bên cạnh tiếng thác nước chảy róc rách hoặc suối sâu để tránh bị phát hiện."
        ),
        Artifact(
            id = 7,
            name = "Mỏ neo tàu",
            description = "Mỏ neo bằng sắt rỉ sét của những con tàu không số anh dũng trên tuyến đường Hồ Chí Minh trên biển huyền thoại.",
            locations = listOf(
                Location(latitude = 10.0355555555555, longitude = 105.785138888888),
                Location(latitude = 10.0356666666666, longitude = 105.784861111111),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.QUIZ,
            question = "Đoàn tàu vận tải chi viện vũ khí bằng đường biển vào Nam có mật danh quân sự là gì?",
            options = listOf("Đoàn 759", "Đoàn 559", "Đoàn 959", "Đoàn 359"),
            correctAnswer = "Đoàn 759",
            funFact = "Các con tàu không số thường giả dạng tàu đánh cá để hòa vào biển cả, ngụy trang tuyệt đối trước máy bay tuần tiễu địch."
        ),
        Artifact(
            id = 8,
            name = "Pháo",
            description = "Khẩu đại pháo cao xạ hạng nặng, từng được kéo bằng sức người qua những dốc núi dựng đứng trong chiến dịch Điện Biên Phủ.",
            locations = listOf(
                Location(latitude = 10.0357777777778, longitude = 105.785305555556),
                Location(latitude = 10.0355833333333, longitude = 105.785444444444),
                Location(latitude = 10.0355555555555, longitude = 105.7855),
                Location(latitude = 10.0359444444444, longitude = 105.785611111111),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.QUIZ,
            question = "Khẩu pháo nổi tiếng được anh hùng Tô Vĩnh Diện dùng thân mình chèn cứu pháo thuộc chiến dịch nào?",
            options = listOf("Chiến dịch Điện Biên Phủ", "Chiến dịch Biên Giới", "Chiến dịch Huế - Đà Nẵng", "Chiến dịch Hồ Chí Minh"),
            correctAnswer = "Chiến dịch Điện Biên Phủ",
            funFact = "Bộ đội Việt Nam đã lập kỳ tích kéo những khẩu pháo nặng hàng tấn lên đỉnh đèo cao bằng dây thừng và sức cơ bắp thuần túy."
        ),
        Artifact(
            id = 9,
            name = "Súng thần công",
            description = "Khẩu thần công bằng đồng đúc từ thời Nguyễn, biểu tượng cho nền quốc phòng thời trung đại chống giặc ngoại xâm phương Tây.",
            locations = listOf(
                Location(latitude = 10.0359722222222, longitude = 105.785527777777),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.GUESS_YEAR,
            question = "Triều đại nhà Nguyễn bắt đầu đúc các khẩu súng thần công lớn (Cửu Vị Thần Công) vào năm nào?",
            correctAnswer = "1803",
            funFact = "Súng thần công thời xưa sử dụng thuốc súng đen và bắn ra những quả cầu gang đặc để phá hủy chiến thuyền gỗ của quân địch."
        ),
        Artifact(
            id = 10,
            name = "Trục máy bay B52",
            description = "Bộ phận động cơ và trục cánh quạt của siêu pháo đài bay B52 bị bắn rơi tại chỗ, chìm sâu dưới hồ Hữu Tiệp.",
            locations = listOf(
                Location(latitude = 10.0359444444444, longitude = 105.785527777777),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.QUIZ,
            question = "Hồ nước nào giữa lòng Hà Nội hiện đang lưu giữ xác một chiếc máy bay B-52 bị bắn rơi?",
            options = listOf("Hồ Hữu Tiệp", "Hồ Hoàn Kiếm", "Hồ Tây", "Hồ Thiền Quang"),
            correctAnswer = "Hồ Hữu Tiệp",
            funFact = "Hồ Hữu Tiệp còn được gọi là 'Hồ B-52', nằm tại làng hoa Ngọc Hà, Hà Nội - một chứng tích sống động lịch sử chống Mỹ."
        ),
        Artifact(
            id = 11,
            name = "Tàu tuần tiễu PCF",
            description = "Chiếc tàu tuần duyên bọc thép hạng nhẹ chạy tốc độ cao thu giữ từ quân đội đối phương, dùng bảo vệ vùng duyên hải.",
            locations = listOf(
                Location(latitude = 10.0355555555555, longitude = 105.785138888888),
                Location(latitude = 10.0357777777777, longitude = 105.785111111111),
                Location(latitude = 10.0356666666666, longitude = 105.784861111111),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.QUIZ,
            question = "Tàu tuần tiễu PCF (Swift Boat) hoạt động chủ yếu ở địa hình sông ngòi nào của miền Nam?",
            options = listOf("Vùng sông nước Cửu Long", "Sông Hồng", "Sông Hương", "Sông Đà"),
            correctAnswer = "Vùng sông nước Cửu Long",
            funFact = "Tàu PCF có động cơ cực khỏe giúp lướt nhanh trên những kênh rạch chằng chịt đầy dừa nước của vùng Tây Nam Bộ."
        ),
        Artifact(
            id = 12,
            name = "Xe bọc thép",
            description = "Chiếc xe thiết giáp lội nước chở quân kiên cố, đóng vai trò mũi nhọn đột phá xuyên thủng các phòng tuyến kiên cố.",
            locations = listOf(
                Location(latitude = 10.0355833333333, longitude = 105.785361111111),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.CODE_BREAKER,
            question = "Giải mã mật tự sau để biết tên dòng xe bọc thép lội nước nổi tiếng của Liên Xô: 'B_T_R' (Bỏ dấu gạch dưới)",
            correctAnswer = "BTR",
            funFact = "BTR có khả năng di chuyển cả trên cạn lẫn dưới nước bằng hệ thống cánh quạt đẩy nước ở phía sau đuôi xe."
        ),
        Artifact(
            id = 13,
            name = "Xe Peugeot",
            description = "Chiếc xe cổ sang trọng được các đặc khu quân sự dùng làm phương tiện liên lạc, đưa đón mật vụ trong lòng đô thị Sài Gòn.",
            locations = listOf(
                Location(latitude = 10.0355555555555, longitude = 105.785194444444),
                Location(latitude = 10.0355555555555, longitude = 105.785138888888),
                Location(latitude = 10.0356666666666, longitude = 105.784861111111),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.GUESS_YEAR,
            question = "Hãng xe Peugeot của nước nào sản xuất những mẫu xe du lịch sang trọng xuất hiện ở Việt Nam?",
            correctAnswer = "Pháp",
            funFact = "Nhiều chiến sĩ tình báo Việt Nam đã hóa thân thành các quý ông thượng lưu lái xe Peugeot sang trọng để luồn sâu vào sào huyệt đối phương."
        ),
        Artifact(
            id = 14,
            name = "Xe tăng",
            description = "Chiếc xe tăng húc đổ cánh cổng dinh độc lập trưa ngày 30/4/1975, đánh dấu thời khắc thống nhất non sông hoàn toàn.",
            locations = listOf(
                Location(latitude = 10.035806, longitude = 105.785417),
                Location(latitude = 10.035899, longitude = 105.785502),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.QUIZ,
            question = "Số hiệu của chiếc xe tăng húc đổ cổng chính Dinh Độc Lập trưa ngày 30/4/1975 là bao nhiêu?",
            options = listOf("Xe tăng 390", "Xe tăng 843", "Xe tăng 384", "Xe tăng 398"),
            correctAnswer = "Xe tăng 390",
            funFact = "Mặc dù xe tăng 843 tiếp cận cổng phụ trước, nhưng xe tăng 390 mới chính là chiếc húc đổ sập cánh cổng chính Dinh Độc Lập."
        ),
        Artifact(
            id = 15,
            name = "Máy bay trực thăng",
            description = "Chiếc trực thăng UH-1 'Đại bàng bộ binh' chuyên chở quân và di tản, chứng tích quan trọng trong các trận càn quét dốc núi miền Trung.",
            locations = listOf(
                Location(latitude = 10.0356111111111, longitude = 105.785388888888),
                Location(latitude = 10.0355555555555, longitude = 105.785194444444),
                Location(latitude = 10.0355555555555, longitude = 105.785138888888),
                Location(latitude = 10.0356666666666, longitude = 105.784861111111),
                MUSEUM_LOCATION,
                MUSEUM_LOCATION_2,
                MUSEUM_LOCATION_3
            ),
            gameType = GameType.QUIZ,
            question = "Dòng máy bay trực thăng UH-1 được sản xuất chủ yếu bởi quốc gia nào?",
            options = listOf("Mỹ", "Liên Xô", "Pháp", "Đức"),
            correctAnswer = "Mỹ",
            funFact = "Tiếng động phành phạch đặc trưng của cánh quạt trực thăng UH-1 đã trở thành biểu tượng âm thanh khó quên của chiến tranh Việt Nam."
        )
    )
}
