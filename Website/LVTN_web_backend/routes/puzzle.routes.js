const express = require("express");
const router = express.Router();
const { 
    getPuzzle,
    getPuzzle_SAM,
    getPuzzle_BOM,

    getPuzzle_GXT,
    getPuzzle_LH,
    getPuzzle_MB,    
    
    getPuzzle_MCT,
    getPuzzle_MIP,
    getPuzzle_MNT, 

    getPuzzle_P,
    getPuzzle_STC,
    getPuzzle_PCF, 

    getPuzzle_TMB,
    getPuzzle_XBT,
    getPuzzle_XPG, 
 } = require("../controllers/puzzle.controller");
router.get("/xetang", getPuzzle);
router.get("/bevadantenlua", getPuzzle_SAM);
router.get("/bom", getPuzzle_BOM);
router.get("/ghexuongthuyen", getPuzzle_GXT);
router.get("/luhambimat", getPuzzle_LH);
router.get("/maybaytructhang", getPuzzle_MB);
router.get("/maycantol", getPuzzle_MCT);
router.get("/mayinpedal", getPuzzle_MIP);
router.get("/moneotau", getPuzzle_MNT);
router.get("/phao", getPuzzle_P);
router.get("/sungthancong", getPuzzle_STC);
router.get("/tautuantieupcf", getPuzzle_PCF);
router.get("/trucmaybayb52", getPuzzle_TMB);
router.get("/xebocthep", getPuzzle_XBT);
router.get("/xepeugeot", getPuzzle_XPG);

module.exports = router;