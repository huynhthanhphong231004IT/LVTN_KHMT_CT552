const express = require("express");
const router = express.Router();
const hienVatController = require("../controllers/hienVatController");

router.get("/", hienVatController.getAllHienVat);
router.get("/:id", hienVatController.getHienVatById);

module.exports = router;