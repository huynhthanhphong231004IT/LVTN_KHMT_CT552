const db = require("../config/db");
exports.getBaiVietById = async (req, res) => {
    const id = req.params.id;

    try {
        // 1. Lấy bài viết
        const [baiViet] = await db.promise().query(
            "SELECT * FROM BAI_VIET WHERE MA_BAI_VIET = ?",
            [id]
        );

        if (baiViet.length === 0) {
            return res.status(404).json({ message: "Không tìm thấy bài viết" });
        }

        // 2. Lấy các đoạn
        const [doan] = await db.promise().query(
            "SELECT * FROM BAI_VIET_DOAN WHERE MA_BAI_VIET = ? ORDER BY THU_TU",
            [id]
        );

        // 3. Lấy hình ảnh theo đoạn
        const [hinhAnh] = await db.promise().query(
            `SELECT h.* 
             FROM HINH_ANH_BAI_VIET h
             JOIN BAI_VIET_DOAN d ON h.MA_DOAN = d.MA_DOAN
             WHERE d.MA_BAI_VIET = ?`,
            [id]
        );

        // 4. Lấy video theo đoạn
        const [video] = await db.promise().query(
            `SELECT v.* 
             FROM VIDEO_BAI_VIET v
             JOIN BAI_VIET_DOAN d ON v.MA_DOAN = d.MA_DOAN
             WHERE d.MA_BAI_VIET = ?`,
            [id]
        );

        // 5. Trả về
        res.json({
            bai_viet: baiViet[0],
            doan_bai_viet: doan,
            hinh_anh: hinhAnh,
            video: video
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Lỗi server" });
    }
};

// Lấy danh sách hiện vật
exports.getAllBaiViet= async (req, res) => {
    try {
        const [data] = await db.promise().query("SELECT * FROM BAI_VIET");
        res.json(data);
    } catch (err) {
        res.status(500).json({ error: err });
    }
};