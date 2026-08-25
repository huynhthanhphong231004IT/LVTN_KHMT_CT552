const express = require("express");
const router = express.Router();
const baiVietController = require("../controllers/baiDangController")

router.get("/", baiVietController.getAllBaiViet);
router.get("/:id", baiVietController.getBaiVietById);

module.exports = router;