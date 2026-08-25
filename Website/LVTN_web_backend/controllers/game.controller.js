const fs = require("fs");

const QUESTION_FILE_XT =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Xe tăng.json";
const QUESTION_FILE_SAM =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Bệ và đạn tên lửa SAM.json";
const QUESTION_FILE_BOM =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Bom.json";
// 
const QUESTION_FILE_GXT =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Ghe xuồng thuyền.json";
const QUESTION_FILE_LH =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Lu hầm bí mật.json";
const QUESTION_FILE_MB =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Máy bay trực thăng.json";

const QUESTION_FILE_MCT =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Máy cán Tol.json";
const QUESTION_FILE_MIP =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Máy in.json";
const QUESTION_FILE_MNT =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Mỏ neo tàu.json";

const QUESTION_FILE_P =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Pháo.json";
const QUESTION_FILE_STC =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Súng thần công.json";
const QUESTION_FILE_PCF =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Tàu tuần tiểu PCF.json";

const QUESTION_FILE_TMB =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Trục máy bay B-52.json";
const QUESTION_FILE_XBT =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Xe bọc thép.json";
const QUESTION_FILE_XPG =
  "D:/LUAN_VAN/Text-Model/Huan-Luyen/CauHoi_WithOptions/Xe Peugeot.json";

function getQuestionById_XT(id) {
  const raw = fs.readFileSync(QUESTION_FILE_XT, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}
function getQuestionById_SAM(id) {
  const raw = fs.readFileSync(QUESTION_FILE_SAM, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_BOM(id) {
  const raw = fs.readFileSync(QUESTION_FILE_BOM, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_GXT(id) {
  const raw = fs.readFileSync(QUESTION_FILE_GXT, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_LH(id) {
  const raw = fs.readFileSync(QUESTION_FILE_LH, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_MB(id) {
  const raw = fs.readFileSync(QUESTION_FILE_MB, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_MCT(id) {
  const raw = fs.readFileSync(QUESTION_FILE_MCT, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_MIP(id) {
  const raw = fs.readFileSync(QUESTION_FILE_MIP, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_MNT(id) {
  const raw = fs.readFileSync(QUESTION_FILE_MNT, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_P(id) {
  const raw = fs.readFileSync(QUESTION_FILE_P, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_STC(id) {
  const raw = fs.readFileSync(QUESTION_FILE_STC, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_PCF(id) {
  const raw = fs.readFileSync(QUESTION_FILE_PCF, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_TMB(id) {
  const raw = fs.readFileSync(QUESTION_FILE_TMB, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_XBT(id) {
  const raw = fs.readFileSync(QUESTION_FILE_XBT, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

function getQuestionById_XPG(id) {
  const raw = fs.readFileSync(QUESTION_FILE_XPG, "utf-8");
  const data = JSON.parse(raw);

  return data.questions.find(q => q.id == Number(id));
}

async function getQuestion_XT(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_XT(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_SAM(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_SAM(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_BOM(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_BOM(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_GXT(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_GXT(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_LH(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_LH(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_MB(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_MB(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_MCT(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_MCT(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_MIP(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_MIP(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_MNT(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_MNT(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_P(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_P(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_STC(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_STC(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_PCF(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_PCF(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_TMB(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_TMB(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_XBT(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_XBT(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}

async function getQuestion_XPG(req, res) {
  try {
    const { id } = req.body;

    const question = getQuestionById_XPG(id);

    if (!question) {
      return res.status(404).json({
        message: "Question not found"
      });
    }

    return res.json({
      id: question.id,
      question: question.question,
      options: question.options,
      answer: question.answer
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ message: "Server error" });
  }
}


module.exports = { 
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
};