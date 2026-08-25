const mysql = require("mysql2");

const db = mysql.createConnection({
    host: "localhost",
    user: "root",
    password: "ThanhPhong@123",
    database: "Bao_Tang_Quan_Khu_9"
});

db.connect(err => {
    if (err) {
        console.error("Lỗi kết nối DB:", err);
    } else {
        console.log("MySQL connected");
    }
});

module.exports = db;