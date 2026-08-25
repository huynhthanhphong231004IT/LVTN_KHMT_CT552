const express = require("express");
const cors = require("cors");
const path = require("path");
const os = require("os");

// Import các Router
const hienVatRoutes = require("./routes/hienVatRoutes");
const baiVietRoutes = require("./routes/baiVietRoutes");
const puzzleRoutes = require("./routes/puzzle.routes");
const gameRoutes = require("./routes/game.routes");
// Import router Bảng xếp hạng (Leaderboard)
const leaderboardRoutes = require("./routes/leaderboard.routes");

const app = express();

app.use(cors());
app.use(express.json());

// -------------------------------------------------------------
// ĐƯỜNG DẪN TĨNH PHỤC VỤ ẢNH GAME
// -------------------------------------------------------------
const IMAGE_DIR = "D:/LUAN_VAN/MaHoa/runs/XeTang/game_images";
app.use("/game_images", express.static(IMAGE_DIR));

const IMAGE_DIR_SAM = "D:/LUAN_VAN/MaHoa/runs/bevadantenlua/game_images";
app.use("/game_images/bevadantenlua", express.static(IMAGE_DIR_SAM));

const IMAGE_DIR_BOM = "D:/LUAN_VAN/MaHoa/runs/Bom/game_images";
app.use("/game_images/bom", express.static(IMAGE_DIR_BOM));

const IMAGE_DIR_GXT = "D:/LUAN_VAN/MaHoa/runs/ghexuongthuyen/game_images";
app.use("/game_images/ghexuongthuyen", express.static(IMAGE_DIR_GXT));

const IMAGE_DIR_LH = "D:/LUAN_VAN/MaHoa/runs/luhambimat/game_images";
app.use("/game_images/luhambimat", express.static(IMAGE_DIR_LH));

const IMAGE_DIR_MB = "D:/LUAN_VAN/MaHoa/runs/maybaytructhang/game_images";
app.use("/game_images/maybaytructhang", express.static(IMAGE_DIR_MB));

const IMAGE_DIR_MCT = "D:/LUAN_VAN/MaHoa/runs/maycantol/game_images";
app.use("/game_images/maycantol", express.static(IMAGE_DIR_MCT));

const IMAGE_DIR_MIP = "D:/LUAN_VAN/MaHoa/runs/mayinpedal/game_images";
app.use("/game_images/mayinpedal", express.static(IMAGE_DIR_MIP));

const IMAGE_DIR_MNT = "D:/LUAN_VAN/MaHoa/runs/moneotau/game_images";
app.use("/game_images/moneotau", express.static(IMAGE_DIR_MNT));

const IMAGE_DIR_P = "D:/LUAN_VAN/MaHoa/runs/phao/game_images";
app.use("/game_images/phao", express.static(IMAGE_DIR_P));

const IMAGE_DIR_STC = "D:/LUAN_VAN/MaHoa/runs/sungthancong/game_images";
app.use("/game_images/sungthancong", express.static(IMAGE_DIR_STC));

const IMAGE_DIR_PCF = "D:/LUAN_VAN/MaHoa/runs/tautuantieupcf/game_images";
app.use("/game_images/tautuantieupcf", express.static(IMAGE_DIR_PCF));

const IMAGE_DIR_TMB = "D:/LUAN_VAN/MaHoa/runs/trucmaybayb52/game_images";
app.use("/game_images/trucmaybayb52", express.static(IMAGE_DIR_TMB));

const IMAGE_DIR_XBT = "D:/LUAN_VAN/MaHoa/runs/xebocthep/game_images";
app.use("/game_images/xebocthep", express.static(IMAGE_DIR_XBT));

const IMAGE_DIR_XPG = "D:/LUAN_VAN/MaHoa/runs/xepeugeot/game_images";
app.use("/game_images/xepeugeot", express.static(IMAGE_DIR_XPG));

// -------------------------------------------------------------
// CÁC ROUTE API CHÍNH
// -------------------------------------------------------------
app.use("/api/hienvat", hienVatRoutes);
app.use("/api/baiviet", baiVietRoutes);
app.use("/api/puzzle", puzzleRoutes);
app.use("/api/game", gameRoutes);

// Route cho Bảng xếp hạng (hỗ trợ cả /leaderboard và /api/leaderboard)
app.use("/leaderboard", leaderboardRoutes);
app.use("/api/leaderboard", leaderboardRoutes);

// -------------------------------------------------------------
// HÀM TỰ ĐỘNG DÒ TÌM IP LAN TRONG MẠNG LOCAL
// -------------------------------------------------------------
function getLocalIp() {
    const interfaces = os.networkInterfaces();
    for (const name of Object.keys(interfaces)) {
        for (const net of interfaces[name]) {
            // Bỏ qua IPv6 và các địa chỉ Loopback (127.0.0.1)
            if (net.family === 'IPv4' && !net.internal) {
                return net.address;
            }
        }
    }
    return "127.0.0.1";
}

const localIp = getLocalIp();
const PORT_WEB = 3000;
const PORT_APP = 8006;

const server3000 = app.listen(PORT_WEB, "0.0.0.0", () => {
    console.log(`==================================================`);
    console.log(`WEB LOCAL CHẠY TẠI : http://localhost:${PORT_WEB}`);
    console.log(`==================================================`);
});

const server8006 = app.listen(PORT_APP, "0.0.0.0", () => {
    console.log(`==================================================`);
    console.log(`APP MOBILE CHẠY TẠI : http://${localIp}:${PORT_APP}`);
    console.log(`==================================================`);
});