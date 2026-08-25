const db = require("../config/db");

// 1. Lấy danh sách bảng xếp hạng (sắp xếp giảm dần theo điểm score và số trạm cleared)
exports.getLeaderboard = (req, res) => {
    const sql = `
        SELECT name, title, score, cleared, email, ticket 
        FROM xep_hang 
        ORDER BY score DESC, cleared DESC 
        LIMIT 50
    `;
    
    db.query(sql, (err, results) => {
        if (err) {
            console.error("Lỗi truy vấn bảng xếp hạng:", err);
            return res.status(500).json({ status: "error", message: "Lỗi máy chủ CSDL", data: [] });
        }
        return res.json({
            status: "success",
            data: results
        });
    });
};

// 2. Lưu kết quả chơi game của người dùng vào CSDL
exports.submitScore = (req, res) => {
    const { name, title, score, cleared, email, ticket } = req.body;

    if (!name) {
        return res.status(400).json({ status: "error", message: "Tên người chơi không được để trống" });
    }

    const sql = `
        INSERT INTO xep_hang (name, title, score, cleared, email, ticket) 
        VALUES (?, ?, ?, ?, ?, ?)
    `;

    const values = [
        name,
        title || "Tân Binh Nhập Ngũ",
        score || 0,
        cleared || 0,
        email || "",
        ticket || ""
    ];

    db.query(sql, values, (err, result) => {
        if (err) {
            console.error("Lỗi khi lưu điểm:", err);
            return res.status(500).json({ status: "error", message: "Không thể lưu điểm vào CSDL" });
        }

        // Sau khi lưu xong, lấy lại top bảng xếp hạng mới nhất
        const getTopSql = `
            SELECT name, title, score, cleared, email, ticket 
            FROM xep_hang 
            ORDER BY score DESC, cleared DESC 
            LIMIT 50
        `;

        db.query(getTopSql, (errTop, topResults) => {
            if (errTop) {
                return res.json({ status: "success", message: "Đã lưu điểm thành công", data: [] });
            }
            return res.json({
                status: "success",
                message: "Đã lưu điểm thành công",
                data: topResults
            });
        });
    });
};