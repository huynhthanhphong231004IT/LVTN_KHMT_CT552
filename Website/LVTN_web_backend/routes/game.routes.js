const express = require("express");
const router = express.Router();

const { 
    getQuestion_XT,
    getQuestion_SAM,
    getQuestion_BOM,

    getQuestion_GXT,
    getQuestion_LH,
    getQuestion_MB,

    getQuestion_MCT,
    getQuestion_MIP,
    getQuestion_MNT,

    getQuestion_P,
    getQuestion_STC,
    getQuestion_PCF,

    getQuestion_TMB,
    getQuestion_XBT,
    getQuestion_XPG,
 } = require("../controllers/game.controller");

router.post("/question", getQuestion_XT);
router.post("/question_bevadantenlua", getQuestion_SAM);
router.post("/question_bom", getQuestion_BOM);

router.post("/question_ghexuongthuyen", getQuestion_GXT);
router.post("/question_luhambimat", getQuestion_LH);
router.post("/question_maybaytructhang", getQuestion_MB);

router.post("/question_maycantol", getQuestion_MCT);
router.post("/question_mayinpedal", getQuestion_MIP);
router.post("/question_moneotau", getQuestion_MNT);

router.post("/question_phao", getQuestion_P);
router.post("/question_sungthancong", getQuestion_STC);
router.post("/question_tautuantieupcf", getQuestion_PCF);

router.post("/question_trucmaybayb52", getQuestion_TMB);
router.post("/question_xebocthep", getQuestion_XBT);
router.post("/question_xepeugeot", getQuestion_XPG);
module.exports = router;