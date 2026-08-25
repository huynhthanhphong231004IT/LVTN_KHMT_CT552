const { 
  getImagesBySet_XT,
  getImagesBySet_SAM,
  getImagesBySet_BOM,

  getImagesBySet_GXT,
  getImagesBySet_LH,
  getImagesBySet_MB,

  getImagesBySet_MCT,
  getImagesBySet_MIP,
  getImagesBySet_MNT,

  getImagesBySet_P,
  getImagesBySet_STC,
  getImagesBySet_PCF,

  getImagesBySet_TMB,
  getImagesBySet_XBT,
  getImagesBySet_XPG,
 } = require("../services/puzzle.service");

function getPuzzle(req, res) {
  try {
    const setName = "XT01"; 

    const images = getImagesBySet_XT(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}
function getPuzzle_SAM(req, res) {
  try {
    const setName = "SAM01"; 

    const images = getImagesBySet_SAM(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}
function getPuzzle_BOM(req, res) {
  try {
    const setName = "Bom01"; 

    const images = getImagesBySet_BOM(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_GXT(req, res) {
  try {
    const setName = "GXT01"; 

    const images = getImagesBySet_GXT(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_LH(req, res) {
  try {
    const setName = "LH01"; 

    const images = getImagesBySet_LH(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_MB(req, res) {
  try {
    const setName = "MB01"; 

    const images = getImagesBySet_MB(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_MCT(req, res) {
  try {
    const setName = "CT01"; 

    const images = getImagesBySet_MCT(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_MIP(req, res) {
  try {
    const setName = "MIP01"; 

    const images = getImagesBySet_MIP(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_MNT(req, res) {
  try {
    const setName = "MTT01"; 

    const images = getImagesBySet_MNT(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_P(req, res) {
  try {
    const setName = "P01"; 

    const images = getImagesBySet_P(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_STC(req, res) {
  try {
    const setName = "STC01"; 

    const images = getImagesBySet_STC(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_PCF(req, res) {
  try {
    const setName = "PCF01"; 

    const images = getImagesBySet_PCF(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_TMB(req, res) {
  try {
    const setName = "TMB01"; 

    const images = getImagesBySet_TMB(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_XBT(req, res) {
  try {
    const setName = "XBT01"; 

    const images = getImagesBySet_XBT(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}

function getPuzzle_XPG(req, res) {
  try {
    const setName = "XPG01"; 

    const images = getImagesBySet_XPG(setName);

    res.json({
      success: true,
      images
    });
  } catch (err) {
    console.error(err);
    res.status(500).json({
      success: false,
      message: "Error loading puzzle images"
    });
  }
}
module.exports = { 
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
 };