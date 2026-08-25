const db = require("../config/db");

// Lấy chi tiết 1 hiện vật
exports.getHienVatById = async (req, res) => {
    const id = req.params.id;

    try {
        const [hienVat] = await db.promise().query(
            "SELECT * FROM HIEN_VAT WHERE MA_HIEN_VAT = ?",
            [id]
        );

        if (hienVat.length === 0) {
            return res.status(404).json({ message: "Không tìm thấy hiện vật" });
        }

        const [moTa] = await db.promise().query(
            "SELECT * FROM MO_TA_HIEN_VAT WHERE MA_HIEN_VAT = ? ORDER BY THU_TU",
            [id]
        );

        const [hinhAnh] = await db.promise().query(
            "SELECT * FROM HINH_ANH_HIEN_VAT WHERE MA_HIEN_VAT = ?",
            [id]
        );

        const [video] = await db.promise().query(
            "SELECT * FROM VIDEO_HIEN_VAT WHERE MA_HIEN_VAT = ?",
            [id]
        );

        res.json({
            hien_vat: hienVat[0],
            mo_ta: moTa,
            hinh_anh: hinhAnh,
            video: video
        });

    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Lỗi server" });
    }
};


// Lấy danh sách hiện vật
exports.getAllHienVat = async (req, res) => {
    try {
        const [data] = await db.promise().query("SELECT * FROM HIEN_VAT");
        res.json(data);
    } catch (err) {
        res.status(500).json({ error: err });
    }
};