const fs = require("fs");
const path = require("path");

const IMAGE_FOLDER_XT = "D:/LUAN_VAN/MaHoa/runs/XeTang/game_images";
const IMAGE_FOLDER_SAM = "D:/LUAN_VAN/MaHoa/runs/bevadantenlua/game_images";
const IMAGE_FOLDER_BOM = "D:/LUAN_VAN/MaHoa/runs/Bom/game_images"; 
const IMAGE_FOLDER_GXT = "D:/LUAN_VAN/MaHoa/runs/ghexuongthuyen/game_images"; 

const IMAGE_FOLDER_LH = "D:/LUAN_VAN/MaHoa/runs/luhambimat/game_images"; 
const IMAGE_FOLDER_MB = "D:/LUAN_VAN/MaHoa/runs/maybaytructhang/game_images"; 
const IMAGE_FOLDER_MCT = "D:/LUAN_VAN/MaHoa/runs/maycantol/game_images"; 
const IMAGE_FOLDER_MIP = "D:/LUAN_VAN/MaHoa/runs/mayinpedal/game_images"; 

const IMAGE_FOLDER_MNT = "D:/LUAN_VAN/MaHoa/runs/moneotau/game_images"; 
const IMAGE_FOLDER_P = "D:/LUAN_VAN/MaHoa/runs/phao/game_images"; 
const IMAGE_FOLDER_STC = "D:/LUAN_VAN/MaHoa/runs/sungthancong/game_images"; 
const IMAGE_FOLDER_PCF = "D:/LUAN_VAN/MaHoa/runs/tautuantieupcf/game_images"; 

const IMAGE_FOLDER_TMB = "D:/LUAN_VAN/MaHoa/runs/trucmaybayb52/game_images"; 
const IMAGE_FOLDER_XBT = "D:/LUAN_VAN/MaHoa/runs/xebocthep/game_images"; 
const IMAGE_FOLDER_XPG = "D:/LUAN_VAN/MaHoa/runs/xepeugeot/game_images"; 


function getImagesBySet_XT(setName = "XT01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_XT);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/${f}`
  }));
}

function getImagesBySet_SAM(setName = "SAM01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_SAM);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/bevadantenlua/${f}`
  }));
}

function getImagesBySet_BOM(setName = "Bom01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_BOM);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/bom/${f}`
  }));
}

function getImagesBySet_GXT(setName = "GXT01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_GXT);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/ghexuongthuyen/${f}`
  }));
}

function getImagesBySet_LH(setName = "LH01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_LH);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/luhambimat/${f}`
  }));
}

function getImagesBySet_MB(setName = "MB01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_MB);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/maybaytructhang/${f}`
  }));
}

function getImagesBySet_MCT(setName = "CT01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_MCT);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/maycantol/${f}`
  }));
}

function getImagesBySet_MIP(setName = "MIP01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_MIP);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/mayinpedal/${f}`
  }));
}

function getImagesBySet_MNT(setName = "MTT01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_MNT);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/moneotau/${f}`
  }));
}

function getImagesBySet_P(setName = "P01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_P);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/phao/${f}`
  }));
}

function getImagesBySet_STC(setName = "STC01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_STC);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/sungthancong/${f}`
  }));
}

function getImagesBySet_PCF(setName = "PCF01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_PCF);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/tautuantieupcf/${f}`
  }));
}

function getImagesBySet_TMB(setName = "TMB01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_TMB);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/trucmaybayb52/${f}`
  }));
}

function getImagesBySet_XBT(setName = "XBT01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_XBT);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/xebocthep/${f}`
  }));
}

function getImagesBySet_XPG(setName = "XPG01") {
  const allFiles = fs.readdirSync(IMAGE_FOLDER_XPG);
  const files = allFiles
    .filter(f => {
      const ext = path.extname(f).toLowerCase();
      return [".jpg", ".jpeg", ".png"].includes(ext);
    })
    .filter(f => f.startsWith(setName + "_"))
    .sort();
  return files.slice(0, 4).map(f => ({
    id: path.parse(f).name,
    filename: f,
    url: `/game_images/xepeugeot/${f}`
  }));
}
module.exports = {
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
 };