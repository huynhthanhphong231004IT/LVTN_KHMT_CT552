<template>
  <div class="reward-game-wrapper" :class="{ shake: isShaking }">

    <!-- ================= HUD TRÊN CÙNG ================= -->
    <div class="hud-bar">
      <div class="hud-item hud-score">
        <i class="bi bi-mortarboard-fill"></i>
        <span>Điểm: <strong>{{ score }}</strong></span>
      </div>

      <div class="hud-item hud-lives">
        <i class="bi bi-heart-fill"></i>
        <span v-for="n in MAX_LIVES" :key="n" class="heart" :class="{ dead: n > lives }">♥</span>
      </div>

      <div class="hud-item hud-keys">
        <i class="bi bi-key-fill"></i>
        <span>Chìa khóa: <strong>{{ keys }}</strong></span>
      </div>

      <div class="hud-item hud-progress">
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
        </div>
        <span class="progress-label">{{ collectedCount }}/4 mảnh ghép</span>
      </div>

      <div class="hud-item hud-artifact">
        <i class="bi bi-bank2"></i>
        <span>{{ artifactInfo.name || 'Đang tải hiện vật bí ẩn...' }}</span>
      </div>
    </div>

    <!-- ================= KHU VỰC CHÍNH ================= -->
    <div class="game-body">

      <!-- ---------- BÊN TRÁI: 8 HỘP PHẦN THƯỞNG ---------- -->
      <div class="boxes-panel">
        <h2 class="panel-title"><i class="bi bi-gift-fill"></i> KHO HỘP PHẦN THƯỞNG</h2>
        <p class="panel-sub">Mở hộp — trả lời đúng câu hỏi lịch sử để nhận thưởng. 4/8 hộp giấu mảnh ghép hình ảnh bí mật!</p>

        <div class="boxes-grid">
          <div
            v-for="box in boxes"
            :key="box.id"
            class="reward-box"
            :class="{
              opened: box.opened,
              'is-correct': box.opened && box.correct === true,
              'is-wrong': box.opened && box.correct === false,
              'is-treasure': box.opened && box.correct === true && box.hasImage,
              locked: gameState !== 'playing' || (activeBox && activeBox.id !== box.id)
            }"
            @click="openBox(box)"
          >
            <div class="box-shine"></div>

            <template v-if="!box.opened">
              <i class="bi bi-gift-fill box-icon"></i>
              <span class="box-number">#{{ box.id + 1 }}</span>
            </template>

            <template v-else>
              <template v-if="box.correct && box.hasImage">
                <div class="box-image-frame">
                  <img :src="getBoxImage(box)" class="box-reward-image" alt="Ảnh hiện vật trong hộp" />
                  <i class="bi bi-check-circle-fill result-icon result-icon-badge"></i>
                </div>
                <span class="box-tag">MẢNH GHÉP</span>
              </template>
              <template v-else>
                <i v-if="box.correct" class="bi bi-check-circle-fill result-icon"></i>
                <i v-else class="bi bi-x-circle-fill result-icon"></i>
                <span class="box-tag empty-tag" v-if="box.correct && !box.hasImage">TRỐNG</span>
              </template>
            </template>
          </div>
        </div>
      </div>

      <!-- ---------- BÊN PHẢI: GHÉP HÌNH + CHỤP ẢNH ---------- -->
      <div class="puzzle-panel">
        <h2 class="panel-title"><i class="bi bi-puzzle-fill"></i> GHÉP HÌNH BÍ ẨN</h2>
        <p class="panel-sub">Kéo - thả các mảnh đã thu thập vào 4 ô ghép, có thể đổi vị trí tuỳ ý. Ô đang bị nhiễu sóng cần dùng <strong>chìa khóa</strong> để giải mã trước khi lắp mảnh vào.</p>

        <div class="puzzle-frame">
          <div
            v-for="(slot, idx) in puzzleSlots"
            :key="idx"
            class="puzzle-slot"
            :class="{
              'slot-hint': !slot && hasUnplaced && slotUnlocked[idx],
              'slot-filled': !!slot,
              'slot-locked-noise': !slot && !slotUnlocked[idx]
            }"
            @dragover.prevent
            @drop="onDropToSlot($event, idx)"
            @click="handleSlotClick(idx)"
          >
            <transition name="piece-pop">
              <img
                v-if="slot"
                :key="slot.index"
                :src="slot.dataUrl"
                class="piece-img"
                draggable="true"
                @dragstart="onDragStart($event, 'slot', idx)"
                alt="Mảnh ghép"
              />
            </transition>

            <div v-if="!slot && !slotUnlocked[idx]" class="noise-overlay">
              <div class="noise-static"></div>
              <i class="bi bi-lock-fill noise-lock-icon"></i>
              <span class="noise-label">TÍN HIỆU NHIỄU</span>
            </div>
            <span v-else-if="!slot" class="slot-empty"><i class="bi bi-plus-lg"></i></span>
          </div>
        </div>

        <button
          class="btn-search-key"
          :disabled="keyQuestionLoading || gameState !== 'playing'"
          @click="searchForKey"
        >
          <i class="bi" :class="keyQuestionLoading ? 'bi-arrow-repeat spin-icon' : 'bi-search'"></i>
          {{ keyQuestionLoading ? 'Đang dò tín hiệu...' : 'Tìm Chìa Khóa Bí Ẩn' }}
        </button>

        <div class="pieces-tray">
          <p class="tray-title"><i class="bi bi-collection-fill"></i> Mảnh đã thu thập:</p>
          <transition-group name="piece-pop" tag="div" class="tray-list">
            <div v-if="unplacedPieces.length === 0" key="empty" class="tray-empty">Chưa có mảnh nào — hãy mở hộp!</div>
            <img
              v-for="piece in unplacedPieces"
              :key="piece.index"
              :src="piece.dataUrl"
              class="tray-piece"
              draggable="true"
              @dragstart="onDragStart($event, 'tray', piece.index)"
              alt="Mảnh chưa đặt"
            />
          </transition-group>
        </div>

        <div class="capture-section">
          <p class="capture-hint"><i class="bi bi-lightbulb-fill"></i> Bạn đoán đây là hiện vật gì? Hãy chụp ảnh vật thật/mô hình để kiểm tra!</p>

          <input
            ref="captureInput"
            type="file"
            accept="image/*"
            capture="environment"
            style="display:none"
            @change="handleCapture"
          />

          <button
            class="btn-capture"
            :disabled="capturing || gameState !== 'playing'"
            @click="captureInput.click()"
          >
            <i class="bi" :class="capturing ? 'bi-arrow-repeat spin-icon' : 'bi-camera-fill'"></i>
            {{ capturing ? 'Đang phân tích ảnh...' : 'Chụp ảnh đoán hiện vật' }}
          </button>

          <div v-if="capturePreview" class="capture-preview">
            <img :src="capturePreview" alt="Ảnh chụp" />
          </div>

          <transition name="fade">
            <div v-if="captureResult" class="capture-result" :class="captureResult.type">
              <i :class="captureResult.type === 'success' ? 'bi bi-patch-check-fill' : 'bi bi-exclamation-triangle-fill'"></i>
              {{ captureResult.message }}
            </div>
          </transition>
        </div>
      </div>
    </div>

    <!-- ================= MODAL CÂU HỎI ================= -->
    <transition name="fade">
      <div v-if="activeBox" class="modal-overlay">
        <div class="question-modal">
          <div class="modal-header">
            <i class="bi bi-patch-question-fill"></i>
            <span>Câu hỏi — Hộp #{{ activeBox.id + 1 }}</span>
          </div>

          <div v-if="loadingQuestion" class="loading-box">
            <div class="spinner"></div> Đang tải câu hỏi từ máy chủ...
          </div>

          <template v-else-if="activeBox.question">
            <p class="question-text">{{ activeBox.question.question }}</p>

            <div class="options-grid">
              <button
                v-for="opt in activeBox.question.options"
                :key="opt"
                class="option-btn"
                :disabled="answerLocked"
                :class="{
                  correct: answerLocked && opt === activeBox.question.answer,
                  wrong: answerLocked && opt === selectedOption && opt !== activeBox.question.answer
                }"
                @click="submitAnswer(opt)"
              >
                {{ opt }}
              </button>
            </div>

            <transition name="reveal-pop">
              <div v-if="revealPieceUrl" class="piece-reveal-box">
                <div class="piece-reveal-spin-wrap">
                  <img :src="revealPieceUrl" class="piece-reveal-img" alt="Mảnh ghép mới" />
                </div>
                <p class="reveal-caption"><i class="bi bi-stars"></i> Mảnh ghép mới xuất hiện!</p>
              </div>
            </transition>

            <transition name="fade">
              <div v-if="feedback" class="feedback" :class="feedback.type">{{ feedback.message }}</div>
            </transition>

            <button v-if="answerLocked" class="btn-continue" @click="closeBox">Đóng hộp</button>
          </template>

          <div v-else class="loading-box error-box">
            <i class="bi bi-wifi-off"></i> Không tải được câu hỏi. Vui lòng thử hộp khác.
            <button class="btn-continue" @click="closeBox">Đóng</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ================= MODAL CÂU HỎI PHỤ: TÌM CHÌA KHÓA ================= -->
    <transition name="fade">
      <div v-if="keyQuestion || keyQuestionLoading" class="modal-overlay">
        <div class="question-modal key-quest-modal">
          <div class="modal-header key-quest-header">
            <i class="bi bi-key-fill"></i>
            <span>Thử Thách Tìm Chìa Khóa</span>
          </div>

          <div v-if="keyQuestionLoading" class="loading-box">
            <div class="spinner"></div> Đang dò tín hiệu câu hỏi phụ...
          </div>

          <template v-else-if="keyQuestion">
            <p class="question-text">{{ keyQuestion.question }}</p>

            <div class="options-grid">
              <button
                v-for="opt in keyQuestion.options"
                :key="opt"
                class="option-btn"
                :disabled="keyAnswerLocked"
                :class="{
                  correct: keyAnswerLocked && opt === keyQuestion.answer,
                  wrong: keyAnswerLocked && opt === keySelectedOption && opt !== keyQuestion.answer
                }"
                @click="submitKeyAnswer(opt)"
              >
                {{ opt }}
              </button>
            </div>

            <transition name="fade">
              <div v-if="keyFeedback" class="feedback" :class="keyFeedback.type">{{ keyFeedback.message }}</div>
            </transition>

            <button v-if="keyAnswerLocked" class="btn-continue" @click="closeKeyModal">Đóng</button>
          </template>

          <div v-else class="loading-box error-box">
            <i class="bi bi-wifi-off"></i> Không tải được câu hỏi phụ. Thử lại sau.
            <button class="btn-continue" @click="closeKeyModal">Đóng</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- ================= MODAL KẾT THÚC ================= -->
    <transition name="fade">
      <div v-if="gameState !== 'playing'" class="modal-overlay">
        <div class="end-modal" :class="gameState">
          <i v-if="gameState === 'victory'" class="bi bi-trophy-fill end-icon"></i>
          <i v-else class="bi bi-emoji-frown-fill end-icon"></i>

          <h2>{{ gameState === 'victory' ? 'CHÚC MỪNG! BẠN ĐÃ QUA MÀN' : 'BẠN ĐÃ HẾT LƯỢT CHƠI' }}</h2>
          <p class="end-sub">
            {{ gameState === 'victory'
              ? `Bạn đã nhận diện chính xác hiện vật: ${artifactInfo.name}!`
              : 'Đừng nản lòng, hãy thử lại và quan sát kỹ các mảnh ghép nhé!' }}
          </p>

          <div class="end-stats">
            <div class="stat-row"><span>Điểm số cuối cùng:</span><strong>{{ score }}</strong></div>
            <div class="stat-row"><span>Mảnh ghép thu thập:</span><strong>{{ collectedCount }}/4</strong></div>
          </div>

          <button class="btn-restart" @click="restart"><i class="bi bi-arrow-clockwise"></i> Chơi lại</button>
        </div>
      </div>
    </transition>

    <!-- ================= TOAST ================= -->
    <transition name="toast">
      <div v-if="toast.show" class="toast-box" :class="toast.type">{{ toast.message }}</div>
    </transition>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";

/* =========================================================
   CẤU HÌNH API — chỉnh lại theo địa chỉ triển khai thực tế
   - QUESTION_API : tham khảo API_2.py  (GET /random-question-{category})
   - PREDICT_API  : tham khảo api.py    (POST /predict  - Yolo + CNN)
   ========================================================= */
const CATEGORY = "xetang"; // đổi theo CATEGORIES trong API_2.py (vd: "bom", "phao", ...)
const QUESTION_API = "http://localhost:8004";
const PREDICT_API = "http://localhost:8001";

/* API giải mã ảnh thật đã giấu tin (tham khảo app_2.py — POST /game/decrypt-xetang) */
const DECRYPT_API = "http://localhost:8003";
const XETANG_RUN_ID = "20260608_223506"; // đổi theo run_id thực tế đã dùng khi nhúng ảnh
const XETANG_WM_DIR = "D:/LUAN_VAN/MaHoa/runs/XeTang/game_images"; // đổi theo thư mục ảnh đã nhúng thực tế
// Danh sách ảnh XeTang đang được serve tĩnh thật sự (giống Puzzle_XeTang.vue: fetch /api/puzzle/xetang
// rồi ghép "http://localhost:3000" + img.url). decrypt-xetang chỉ trả về TÊN FILE, nên cần map
// tên file đó sang URL ảnh thật đang có sẵn ở đây, KHÔNG cần thêm route ảnh tĩnh mới.
const IMAGE_LIST_API = "http://localhost:3000";

const MAX_LIVES = 5;
const CORRECT_POINTS = 10;
const WRONG_PENALTY = 5;
const CAPTURE_WIN_POINTS = 30;
const CAPTURE_WRONG_PENALTY = 10;

/* ---------------- STATE CHUNG ---------------- */
const score = ref(0);
const lives = ref(MAX_LIVES);
const gameState = ref("playing"); // playing | victory | gameover
const isShaking = ref(false);
const toast = ref({ show: false, message: "", type: "success" });

/* ---------------- 8 HỘP PHẦN THƯỞNG ---------------- */
const boxes = ref([]);
const activeBox = ref(null);
const loadingQuestion = ref(false);
const answerLocked = ref(false);
const selectedOption = ref(null);
const feedback = ref(null);

/* ---------------- HIỆN VẬT + MẢNH GHÉP ---------------- */
const artifactInfo = ref({});
const slicedPieces = ref([]); // 4 dataURL (ảnh gốc cắt thành 4 phần — dùng khi ảnh thật từ API chưa sẵn sàng)
const realPieceImages = ref([]); // 4 URL ảnh THẬT lấy từ API decrypt-xetang, ưu tiên dùng thay cho ảnh cắt
const pendingPieceIndexes = ref([]); // hộp đã mở đúng nhưng ảnh (thật hoặc cắt) chưa sẵn sàng
const collectedPieces = ref([]); // [{ index, dataUrl, placedSlot }]
const puzzleSlots = ref([null, null, null, null]);
const revealPieceUrl = ref(""); // ảnh đang chạy hiệu ứng xoay khi vừa mở hộp trúng thưởng
const piecesReady = computed(() => realPieceImages.value.length === 4 || slicedPieces.value.length === 4);

const collectedCount = computed(() => collectedPieces.value.length);
const progressPercent = computed(() => Math.round((collectedCount.value / 4) * 100));
const unplacedPieces = computed(() => collectedPieces.value.filter((p) => p.placedSlot === null));
const hasUnplaced = computed(() => unplacedPieces.value.length > 0);

/* ---------------- THỬ THÁCH TÌM CHÌA KHÓA (mở khóa 4 ô ghép đang bị nhiễu sóng) ---------------- */
const keys = ref(0);
const slotUnlocked = ref([false, false, false, false]); // đã dùng chìa khóa giải mã ô hay chưa
const keyQuestion = ref(null);
const keyQuestionLoading = ref(false);
const keyAnswerLocked = ref(false);
const keySelectedOption = ref(null);
const keyFeedback = ref(null);

/* ---------------- CHỤP ẢNH NHẬN DIỆN ---------------- */
const captureInput = ref(null);
const capturing = ref(false);
const capturePreview = ref("");
const captureResult = ref(null);

/* ========================================================= */

function shuffle(arr) {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

function showToast(message, type = "success") {
  toast.value = { show: true, message, type };
  setTimeout(() => (toast.value.show = false), 2500);
}

function triggerShake() {
  isShaking.value = true;
  setTimeout(() => (isShaking.value = false), 400);
}

/* -------- Lấy ảnh THẬT của các mảnh ghép, khớp tên file với danh sách ảnh đang serve tĩnh -------- */
async function fetchRealPieceImages() {
  try {
    // 1. Lấy danh sách ảnh XeTang đang được serve tĩnh thật sự (đã hoạt động, giống Puzzle_XeTang.vue)
    const listRes = await fetch(`${IMAGE_LIST_API}/api/puzzle/xetang`);
    if (!listRes.ok) throw new Error("Lỗi kết nối API danh sách ảnh");
    const listData = await listRes.json();

    const imageUrlByFilename = {};
    (listData.images || []).forEach((img) => {
      const filename = (img.url || "").split("/").pop();
      if (filename) imageUrlByFilename[filename] = `${IMAGE_LIST_API}${img.url}`;
    });

    // 2. Lấy tên file của 4 ảnh đã giấu tin (đã giải mã xong) từ app_2.py
    const res = await fetch(`${DECRYPT_API}/game/decrypt-xetang`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        run_id: XETANG_RUN_ID,
        wm_dir: XETANG_WM_DIR,
      }),
    });
    if (!res.ok) throw new Error("Lỗi kết nối API giải mã ảnh");
    const data = await res.json();
    const answers = data.answers || [];

    // 3. Khớp tên file (vd "XT01_1.png") với URL ảnh thật đã lấy ở bước 1 — không gọi thêm route ảnh mới
    realPieceImages.value = answers.slice(0, 4).map((item) => imageUrlByFilename[item.image] || "");

    // Ảnh thật đã sẵn sàng: áp dụng ngay cho các hộp đã mở đúng trước đó (nếu có)
    if (realPieceImages.value.length === 4 && pendingPieceIndexes.value.length) {
      pendingPieceIndexes.value.forEach((idx) => pushCollectedPiece(idx, true));
      pendingPieceIndexes.value = [];
    }
  } catch (err) {
    console.error(err);
    // Không chặn game nếu API ảnh thật lỗi — ảnh cắt từ artifact (slicedPieces) sẽ được dùng dự phòng
  }
}

/* -------- Ảnh hiển thị bên trong 1 hộp phần thưởng (ưu tiên ảnh thật) -------- */
function getBoxImage(box) {
  if (!box || !box.hasImage || box.pieceIndex === null || box.pieceIndex === undefined) return "";
  return realPieceImages.value[box.pieceIndex] || slicedPieces.value[box.pieceIndex] || "";
}

/* -------- Khởi tạo 8 hộp: 4 hộp có ảnh, 4 hộp trống -------- */
function initBoxes() {
  const flags = shuffle([true, true, true, true, false, false, false, false]);
  const pieceOrder = shuffle([0, 1, 2, 3]);
  let p = 0;

  boxes.value = flags.map((hasImage, i) => ({
    id: i,
    hasImage,
    pieceIndex: hasImage ? pieceOrder[p++] : null,
    question: null,
    opened: false,
    correct: null
  }));
}

/* -------- Mở hộp: gọi API câu hỏi ngẫu nhiên -------- */
async function openBox(box) {
  if (box.opened || activeBox.value || gameState.value !== "playing") return;

  activeBox.value = box;
  loadingQuestion.value = true;
  answerLocked.value = false;
  selectedOption.value = null;
  feedback.value = null;
  revealPieceUrl.value = "";

  try {
    const res = await fetch(`${QUESTION_API}/random-question-${CATEGORY}`);
    if (!res.ok) throw new Error("Lỗi kết nối API câu hỏi");
    const data = await res.json();

    box.question = data.question;

    if (!artifactInfo.value.name && data.artifact) {
      setArtifactInfo(data.artifact);
    }
  } catch (err) {
    console.error(err);
    showToast("Không thể tải câu hỏi từ máy chủ!", "error");
    box.question = null;
  } finally {
    loadingQuestion.value = false;
  }
}

function closeBox() {
  activeBox.value = null;
  revealPieceUrl.value = "";
}

/* -------- Trả lời câu hỏi -------- */
function submitAnswer(option) {
  if (answerLocked.value || !activeBox.value?.question) return;

  const box = activeBox.value;
  selectedOption.value = option;
  answerLocked.value = true;

  const isCorrect = option === box.question.answer;
  box.correct = isCorrect;
  box.opened = true;

  if (isCorrect) {
    score.value += CORRECT_POINTS;

    if (box.hasImage) {
      collectPiece(box.pieceIndex);
      feedback.value = {
        type: "success",
        message: `Chính xác! +${CORRECT_POINTS} điểm — Bạn nhận được 1 mảnh ghép bí ẩn!`
      };
    } else {
      feedback.value = {
        type: "success",
        message: `Chính xác! +${CORRECT_POINTS} điểm — Nhưng hộp này không chứa hình ảnh.`
      };
    }
  } else {
    score.value = Math.max(0, score.value - WRONG_PENALTY);
    lives.value--;
    triggerShake();
    feedback.value = {
      type: "error",
      message: `Sai rồi! Đáp án đúng là "${box.question.answer}" (-${WRONG_PENALTY} điểm)`
    };

    if (lives.value <= 0) {
      setTimeout(() => {
        gameState.value = "gameover";
        activeBox.value = null;
      }, 1200);
    }
  }
}

/* -------- Xử lý ảnh hiện vật gốc -------- */
function setArtifactInfo(artifact) {
  artifactInfo.value = artifact;
  const url = resolveArtifactImageUrl(artifact);
  if (url) sliceArtifactImage(url);
}

function resolveArtifactImageUrl(artifact) {
  // Tuỳ theo cấu trúc thực tế trong file JSON CauHoi_WithOptions, đổi lại field cho phù hợp
  const raw = artifact.image_url || artifact.image || artifact.thumbnail || "";
  if (!raw) return "";
  if (raw.startsWith("http")) return raw;
  return `${QUESTION_API}/${raw.replace(/^\/+/, "")}`;
}

function sliceArtifactImage(url) {
  const img = new Image();
  img.crossOrigin = "anonymous";

  img.onload = () => {
    const size = 480;
    const half = size / 2;

    const full = document.createElement("canvas");
    full.width = size;
    full.height = size;
    full.getContext("2d").drawImage(img, 0, 0, size, size);

    const quadrants = [
      [0, 0],
      [half, 0],
      [0, half],
      [half, half]
    ];

    slicedPieces.value = quadrants.map(([sx, sy]) => {
      const c = document.createElement("canvas");
      c.width = half;
      c.height = half;
      c.getContext("2d").drawImage(full, sx, sy, half, half, 0, 0, half, half);
      return c.toDataURL("image/jpeg", 0.85);
    });

    // Áp dụng cho các hộp đã mở đúng trước khi ảnh gốc kịp cắt xong
    pendingPieceIndexes.value.forEach((idx) => pushCollectedPiece(idx, true));
    pendingPieceIndexes.value = [];
  };

  img.onerror = () => showToast("Không tải được ảnh gốc của hiện vật!", "error");
  img.src = url;
}

function collectPiece(index) {
  if (collectedPieces.value.some((p) => p.index === index)) return;

  if (piecesReady.value) {
    pushCollectedPiece(index, true);
  } else {
    pendingPieceIndexes.value.push(index);
  }
}

function pushCollectedPiece(index, triggerReveal = false) {
  if (collectedPieces.value.some((p) => p.index === index)) return;
  const dataUrl = realPieceImages.value[index] || slicedPieces.value[index];
  if (!dataUrl) return;
  collectedPieces.value.push({ index, dataUrl, placedSlot: null });

  if (triggerReveal) {
    // Hiệu ứng ảnh xoay xuất hiện ngay trong modal câu hỏi
    revealPieceUrl.value = dataUrl;
    setTimeout(() => {
      if (revealPieceUrl.value === dataUrl) revealPieceUrl.value = "";
    }, 1800);
  }
}

/* -------- Kéo - thả ghép hình -------- */
function onDragStart(e, source, id) {
  e.dataTransfer.setData("text/plain", JSON.stringify({ source, id }));
}

function onDropToSlot(e, targetIdx) {
  if (!slotUnlocked.value[targetIdx]) {
    showToast("Ô này đang bị nhiễu sóng! Hãy dùng chìa khóa để giải mã trước.", "error");
    return;
  }

  let data;
  try {
    data = JSON.parse(e.dataTransfer.getData("text/plain"));
  } catch {
    return;
  }

  if (data.source === "tray") {
    const piece = collectedPieces.value.find((p) => p.index === data.id);
    if (!piece) return;

    const existing = puzzleSlots.value[targetIdx];
    if (existing) existing.placedSlot = null;

    puzzleSlots.value[targetIdx] = piece;
    piece.placedSlot = targetIdx;
  } else if (data.source === "slot") {
    const fromIdx = data.id;
    if (fromIdx === targetIdx) return;

    const moving = puzzleSlots.value[fromIdx];
    const target = puzzleSlots.value[targetIdx];

    puzzleSlots.value[fromIdx] = target;
    puzzleSlots.value[targetIdx] = moving;

    if (target) target.placedSlot = fromIdx;
    if (moving) moving.placedSlot = targetIdx;
  }
}

/* -------- Giải mã ô ghép bằng chìa khóa -------- */
function handleSlotClick(idx) {
  if (slotUnlocked.value[idx] || puzzleSlots.value[idx]) return; // đã mở hoặc đã có mảnh ghép rồi
  unlockSlot(idx);
}

function unlockSlot(idx) {
  if (gameState.value !== "playing" || slotUnlocked.value[idx]) return;
  if (keys.value <= 0) {
    showToast("Bạn cần ít nhất 1 chìa khóa để giải mã ô này! Hãy đi tìm chìa khóa.", "error");
    return;
  }
  keys.value--;
  slotUnlocked.value[idx] = true;
  showToast("Đã dùng 1 chìa khóa để giải mã tín hiệu nhiễu! Ô đã sẵn sàng lắp mảnh ghép.", "success");
}

/* -------- Thử thách phụ: đi tìm chìa khóa (dùng chung API câu hỏi random-question-xetang) -------- */
async function searchForKey() {
  if (keyQuestion.value || keyQuestionLoading.value || gameState.value !== "playing") return;

  keyQuestionLoading.value = true;
  keyAnswerLocked.value = false;
  keySelectedOption.value = null;
  keyFeedback.value = null;

  try {
    const res = await fetch(`${QUESTION_API}/random-question-${CATEGORY}`);
    if (!res.ok) throw new Error("Lỗi kết nối API câu hỏi phụ");
    const data = await res.json();
    keyQuestion.value = data.question;
  } catch (err) {
    console.error(err);
    showToast("Không thể tải câu hỏi tìm chìa khóa!", "error");
    keyQuestion.value = null;
  } finally {
    keyQuestionLoading.value = false;
  }
}

function closeKeyModal() {
  keyQuestion.value = null;
  keyFeedback.value = null;
}

function submitKeyAnswer(option) {
  if (keyAnswerLocked.value || !keyQuestion.value) return;

  keySelectedOption.value = option;
  keyAnswerLocked.value = true;

  const isCorrect = option === keyQuestion.value.answer;

  if (isCorrect) {
    keys.value++;
    keyFeedback.value = {
      type: "success",
      message: `Chính xác! Bạn tìm thấy 1 chìa khóa bí ẩn (+1 🔑) — dùng để giải mã 1 ô ghép đang nhiễu sóng!`
    };
  } else {
    keyFeedback.value = {
      type: "error",
      message: `Sai rồi! Đáp án đúng là "${keyQuestion.value.answer}" — không có chìa khóa lần này.`
    };
  }
}

/* -------- Chụp ảnh nhận diện hiện vật (Yolo + CNN) -------- */
function normalize(str) {
  return (str || "")
    .toString()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .trim();
}

async function handleCapture(e) {
  const file = e.target.files[0];
  if (!file || gameState.value !== "playing") return;

  capturePreview.value = URL.createObjectURL(file);
  capturing.value = true;
  captureResult.value = null;

  const formData = new FormData();
  formData.append("file", file);

  try {
    const res = await fetch(`${PREDICT_API}/predict`, { method: "POST", body: formData });
    const data = await res.json();

    if (data.error || !data.results || data.results.length === 0) {
      captureResult.value = {
        type: "error",
        message: "Không nhận diện được vật thể trong ảnh. Hãy thử chụp lại rõ hơn!"
      };
      penalizeWrongCapture();
      return;
    }

    const top = data.results[0];
    const guess = normalize(top.cnn_label);
    const target = normalize(artifactInfo.value.name);

    const isMatch = target && (guess.includes(target) || target.includes(guess));

    if (isMatch) {
      score.value += CAPTURE_WIN_POINTS;
      captureResult.value = {
        type: "success",
        message: `Chính xác! Đây là "${artifactInfo.value.name}". +${CAPTURE_WIN_POINTS} điểm — Bạn đã qua màn!`
      };
      gameState.value = "victory";
    } else {
      captureResult.value = {
        type: "error",
        message: `Chưa đúng! Hệ thống nhận diện: "${top.cnn_label}". Hãy ghép thêm mảnh và quan sát kỹ hơn.`
      };
      penalizeWrongCapture();
    }
  } catch (err) {
    console.error(err);
    captureResult.value = { type: "error", message: "Không thể kết nối máy chủ nhận diện AI!" };
  } finally {
    capturing.value = false;
    e.target.value = "";
  }
}

function penalizeWrongCapture() {
  score.value = Math.max(0, score.value - CAPTURE_WRONG_PENALTY);
  lives.value--;
  triggerShake();
  if (lives.value <= 0) gameState.value = "gameover";
}

function restart() {
  window.location.reload();
}

onMounted(() => {
  initBoxes();
  fetchRealPieceImages();
});
</script>

<style scoped>
* { box-sizing: border-box; }

.reward-game-wrapper {
  min-height: 100vh;
  padding: 20px;
  background: radial-gradient(circle at 20% 10%, #1b2340 0%, #0b0e1a 55%, #05060c 100%);
  color: #eaf0ff;
  font-family: "Segoe UI", Roboto, sans-serif;
  transition: transform 0.05s;
}
.reward-game-wrapper.shake { animation: shakeAnim 0.4s; }
@keyframes shakeAnim {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-6px); }
  75% { transform: translateX(6px); }
}

/* ---------- HUD ---------- */
.hud-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: center;
  justify-content: space-between;
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,215,120,0.25);
  border-radius: 14px;
  padding: 12px 18px;
  margin-bottom: 20px;
  backdrop-filter: blur(6px);
}
.hud-item { display: flex; align-items: center; gap: 8px; font-size: 0.95rem; }
.hud-score i { color: #ffd76a; }
.hud-lives .heart { color: #ff5c7a; font-size: 1.1rem; margin-left: 2px; }
.hud-lives .heart.dead { color: #3a3f52; }
.hud-progress { flex: 1; min-width: 180px; display: flex; align-items: center; gap: 10px; }
.progress-track {
  flex: 1; height: 10px; background: rgba(255,255,255,0.1);
  border-radius: 6px; overflow: hidden;
}
.progress-fill {
  height: 100%; background: linear-gradient(90deg,#42e0a8,#57c8ff);
  transition: width 0.4s ease;
}
.progress-label { font-size: 0.8rem; opacity: 0.8; white-space: nowrap; }
.hud-artifact { color: #ffd76a; font-weight: 600; }
.hud-keys i { color: #a06bff; }
.hud-keys strong { color: #c9a6ff; }

/* ---------- LAYOUT ---------- */
.game-body {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 24px;
}
@media (max-width: 920px) {
  .game-body { grid-template-columns: 1fr; }
}

.panel-title {
  font-size: 1.2rem;
  margin: 0 0 4px;
  display: flex; align-items: center; gap: 8px;
  color: #ffd76a;
}
.panel-sub { font-size: 0.85rem; opacity: 0.75; margin: 0 0 16px; }

/* ---------- HỘP QUÀ ---------- */
.boxes-panel {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 18px;
  padding: 20px;
}
.boxes-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
@media (max-width: 560px) {
  .boxes-grid { grid-template-columns: repeat(2, 1fr); }
}

.reward-box {
  position: relative;
  aspect-ratio: 1/1;
  border-radius: 16px;
  background: linear-gradient(145deg,#3a2f6b,#241a45);
  border: 2px solid rgba(255,215,120,0.35);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
}
.reward-box:hover:not(.locked):not(.opened) {
  transform: translateY(-4px) scale(1.03);
  box-shadow: 0 8px 20px rgba(255,215,120,0.25);
}
.reward-box.locked { opacity: 0.45; cursor: not-allowed; }
.reward-box .box-icon { font-size: 2rem; color: #ffd76a; margin-bottom: 6px; }
.reward-box .box-number { font-size: 0.75rem; opacity: 0.7; }
.reward-box .box-shine {
  position: absolute; inset: 0;
  background: linear-gradient(120deg, transparent 40%, rgba(255,255,255,0.15) 50%, transparent 60%);
  animation: shine 3s infinite;
}
@keyframes shine { 0% { transform: translateX(-100%);} 100% { transform: translateX(100%);} }

.reward-box.is-correct { background: linear-gradient(145deg,#1f7a52,#0f4a33); border-color: #4ee3a0; }
.reward-box.is-wrong { background: linear-gradient(145deg,#7a1f2f,#4a0f18); border-color: #ff6b7d; }
.reward-box.is-treasure { animation: treasurePop 0.7s ease; }
@keyframes treasurePop {
  0%   { transform: scale(1) rotate(0deg); }
  30%  { transform: scale(1.25) rotate(-12deg); box-shadow: 0 0 30px rgba(255,215,120,0.7); }
  55%  { transform: scale(0.92) rotate(10deg); }
  75%  { transform: scale(1.1) rotate(-4deg); }
  100% { transform: scale(1) rotate(0deg); }
}
.reward-box.is-treasure .result-icon { animation: iconSpin 0.7s ease; }
@keyframes iconSpin {
  0%   { transform: rotateY(0deg) scale(0.3); opacity: 0; }
  60%  { transform: rotateY(360deg) scale(1.3); opacity: 1; }
  100% { transform: rotateY(720deg) scale(1); }
}
.result-icon { font-size: 1.8rem; }
.is-correct .result-icon { color: #4ee3a0; }
.is-wrong .result-icon { color: #ff6b7d; }

/* Ảnh thật hiển thị bên trong hộp khi hộp chứa ảnh và trả lời đúng */
.box-image-frame {
  position: relative;
  width: 78%;
  aspect-ratio: 1/1;
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid #4ee3a0;
  box-shadow: 0 0 16px rgba(78,227,160,0.45);
  animation: boxImagePop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.box-reward-image {
  width: 100%; height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}
.reward-box:hover .box-reward-image { transform: scale(1.08); }
.result-icon-badge {
  position: absolute;
  bottom: -4px; right: -4px;
  font-size: 1.1rem;
  background: #0f4a33;
  border-radius: 50%;
  padding: 2px;
  box-shadow: 0 0 8px rgba(78,227,160,0.6);
}
@keyframes boxImagePop {
  0%   { transform: scale(0.3) rotate(-10deg); opacity: 0; }
  70%  { transform: scale(1.1) rotate(3deg); opacity: 1; }
  100% { transform: scale(1) rotate(0deg); opacity: 1; }
}

.box-tag {
  margin-top: 6px; font-size: 0.62rem; letter-spacing: 0.05em;
  background: rgba(255,215,120,0.2); color: #ffd76a;
  padding: 2px 8px; border-radius: 20px; font-weight: 700;
}
.empty-tag { background: rgba(255,255,255,0.12); color: #b8bfd6; }

/* ---------- PUZZLE ---------- */
.puzzle-panel {
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 18px;
  padding: 20px;
}
.puzzle-frame {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 6px;
  width: 100%;
  aspect-ratio: 1/1;
  max-width: 320px;
  margin: 0 auto 18px;
  padding: 6px;
  background: rgba(0,0,0,0.3);
  border-radius: 14px;
  border: 2px dashed rgba(87,200,255,0.4);
}
.puzzle-slot {
  background: rgba(255,255,255,0.05);
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  overflow: hidden;
  border: 1px solid rgba(255,255,255,0.1);
}
.slot-empty { font-size: 1.4rem; opacity: 0.35; font-weight: 700; display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; }
.piece-img { width: 100%; height: 100%; object-fit: cover; cursor: grab; }

/* Ô trống được gợi ý khi có mảnh chưa đặt vào */
.puzzle-slot.slot-hint {
  border: 2px dashed #ffd76a;
  background: rgba(255,215,120,0.1);
  animation: hintPulse 1.2s ease-in-out infinite;
}
.puzzle-slot.slot-hint .slot-empty { color: #ffd76a; opacity: 0.9; }
@keyframes hintPulse {
  0%, 100% { box-shadow: 0 0 0 rgba(255,215,120,0); }
  50% { box-shadow: 0 0 14px rgba(255,215,120,0.6); }
}
.puzzle-slot.slot-filled { border-color: rgba(78,227,160,0.5); }

/* Ô đang bị "nhiễu sóng" — chưa dùng chìa khóa giải mã */
.puzzle-slot.slot-locked-noise {
  cursor: pointer;
  border: 2px dashed rgba(255,107,125,0.45);
  background: #0a0a14;
  transition: border-color 0.2s;
}
.puzzle-slot.slot-locked-noise:hover { border-color: #ffd76a; }
.noise-overlay {
  position: relative;
  width: 100%; height: 100%;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 4px;
  overflow: hidden;
}
.noise-static {
  position: absolute; inset: 0;
  background-image:
    repeating-linear-gradient(0deg, rgba(255,255,255,0.07) 0px, rgba(255,255,255,0.07) 1px, transparent 1px, transparent 3px),
    repeating-linear-gradient(90deg, rgba(87,200,255,0.1), rgba(255,107,125,0.1), rgba(255,255,255,0.06));
  filter: blur(1.4px);
  opacity: 0.75;
  animation: staticFlicker 0.15s steps(2) infinite, staticShift 1.6s linear infinite;
  mix-blend-mode: screen;
}
@keyframes staticFlicker {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 0.85; }
}
@keyframes staticShift {
  0% { background-position: 0 0, 0 0; }
  100% { background-position: 0 42px, 42px 0; }
}
.noise-lock-icon {
  position: relative; z-index: 1;
  font-size: 1.25rem; color: #ff8fa0;
  filter: drop-shadow(0 0 6px rgba(255,107,125,0.7));
  animation: noisePulse 1.4s ease-in-out infinite;
}
@keyframes noisePulse {
  0%, 100% { opacity: 0.6; transform: scale(0.95); }
  50% { opacity: 1; transform: scale(1.08); }
}
.noise-label {
  position: relative; z-index: 1;
  font-size: 0.55rem; letter-spacing: 0.05em;
  color: #ff8fa0; opacity: 0.9; font-weight: 700;
  text-shadow: 0 0 4px rgba(255,107,125,0.6);
}

/* Nút tìm chìa khóa */
.btn-search-key {
  display: inline-flex; align-items: center; gap: 8px;
  background: linear-gradient(135deg,#a06bff,#5f3bff);
  border: none; color: #fff; font-weight: 700;
  padding: 10px 20px; border-radius: 30px; cursor: pointer;
  margin: 2px 0 16px; transition: transform 0.15s;
}
.btn-search-key:hover:not(:disabled) { transform: translateY(-2px); }
.btn-search-key:disabled { opacity: 0.5; cursor: not-allowed; }

/* Hiệu ứng mảnh ghép bật vào vị trí */
.piece-pop-enter-active { animation: piecePop 0.4s ease; }
.piece-pop-leave-active { animation: piecePop 0.25s ease reverse; position: absolute; }
@keyframes piecePop {
  0%   { transform: scale(0.2) rotate(-25deg); opacity: 0; }
  70%  { transform: scale(1.15) rotate(6deg); opacity: 1; }
  100% { transform: scale(1) rotate(0deg); opacity: 1; }
}

.pieces-tray { margin-bottom: 18px; }
.tray-title { font-size: 0.85rem; opacity: 0.85; margin: 0 0 8px; display: flex; gap: 6px; align-items: center; }
.tray-list {
  display: flex; flex-wrap: wrap; gap: 10px;
  min-height: 62px;
  background: rgba(0,0,0,0.2);
  border-radius: 10px;
  padding: 10px;
}
.tray-empty { font-size: 0.8rem; opacity: 0.5; align-self: center; }
.tray-piece {
  width: 56px; height: 56px; object-fit: cover;
  border-radius: 8px; border: 2px solid rgba(255,215,120,0.5);
  cursor: grab;
}

.capture-section { text-align: center; }
.capture-hint { font-size: 0.85rem; opacity: 0.8; margin-bottom: 12px; }
.btn-capture {
  background: linear-gradient(135deg,#57c8ff,#3a7bff);
  border: none; color: #fff; font-weight: 700;
  padding: 12px 22px; border-radius: 30px; cursor: pointer;
  display: inline-flex; align-items: center; gap: 8px;
  transition: transform 0.15s;
}
.btn-capture:hover:not(:disabled) { transform: translateY(-2px); }
.btn-capture:disabled { opacity: 0.5; cursor: not-allowed; }
.spin-icon { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.capture-preview { margin-top: 14px; }
.capture-preview img { max-width: 180px; border-radius: 10px; border: 2px solid rgba(255,255,255,0.15); }

.capture-result {
  margin-top: 12px; padding: 10px 14px; border-radius: 10px;
  font-size: 0.85rem; display: flex; align-items: center; gap: 8px; justify-content: center;
}
.capture-result.success { background: rgba(78,227,160,0.15); color: #4ee3a0; }
.capture-result.error { background: rgba(255,107,125,0.15); color: #ff6b7d; }

/* ---------- MODAL CÂU HỎI ---------- */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(5,6,12,0.75);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  z-index: 100; padding: 16px;
}
.question-modal, .end-modal {
  background: linear-gradient(160deg,#1c2140,#0f1224);
  border: 1px solid rgba(255,215,120,0.3);
  border-radius: 18px;
  padding: 26px;
  width: 100%; max-width: 460px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.5);
}
.modal-header {
  display: flex; align-items: center; gap: 8px;
  font-size: 1.05rem; font-weight: 700; color: #ffd76a;
  margin-bottom: 16px;
}
.key-quest-modal { border-color: rgba(160,107,255,0.4); }
.key-quest-header { color: #c9a6ff; }
.question-text { font-size: 1rem; line-height: 1.5; margin-bottom: 18px; }
.loading-box { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 20px 0; opacity: 0.85; }
.error-box { color: #ff6b7d; }
.spinner {
  width: 26px; height: 26px; border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.2); border-top-color: #ffd76a;
  animation: spin 0.8s linear infinite;
}
.options-grid { display: flex; flex-direction: column; gap: 10px; }
.option-btn {
  text-align: left; padding: 12px 14px; border-radius: 10px;
  background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.12);
  color: #eaf0ff; cursor: pointer; font-size: 0.92rem; transition: 0.15s;
}
.option-btn:hover:not(:disabled) { background: rgba(255,215,120,0.15); border-color: #ffd76a; }
.option-btn:disabled { cursor: not-allowed; }
.option-btn.correct { background: rgba(78,227,160,0.2); border-color: #4ee3a0; color: #4ee3a0; }
.option-btn.wrong { background: rgba(255,107,125,0.2); border-color: #ff6b7d; color: #ff6b7d; }

/* Hiệu ứng ảnh xoay xuất hiện khi mở trúng hộp có ảnh */
.piece-reveal-box {
  margin-top: 16px;
  padding: 16px;
  border-radius: 14px;
  background: rgba(255,215,120,0.08);
  border: 1px solid rgba(255,215,120,0.3);
  text-align: center;
}
.piece-reveal-spin-wrap {
  width: 130px; height: 130px;
  margin: 0 auto;
  perspective: 800px;
}
.piece-reveal-img {
  width: 100%; height: 100%;
  object-fit: cover;
  border-radius: 12px;
  border: 3px solid #ffd76a;
  box-shadow: 0 0 25px rgba(255,215,120,0.5);
  animation: pieceSpinReveal 1s cubic-bezier(0.34, 1.56, 0.64, 1);
}
@keyframes pieceSpinReveal {
  0%   { transform: rotateY(0deg) scale(0.2); opacity: 0; }
  50%  { transform: rotateY(540deg) scale(1.15); opacity: 1; }
  100% { transform: rotateY(720deg) scale(1); opacity: 1; }
}
.reveal-caption { margin: 10px 0 0; font-size: 0.85rem; color: #ffd76a; font-weight: 600; }

.reveal-pop-enter-active { transition: all 0.3s ease; }
.reveal-pop-leave-active { transition: all 0.3s ease; }
.reveal-pop-enter-from, .reveal-pop-leave-to { opacity: 0; transform: translateY(-10px); }

.feedback { margin-top: 14px; padding: 10px 14px; border-radius: 10px; font-size: 0.88rem; }
.feedback.success { background: rgba(78,227,160,0.15); color: #4ee3a0; }
.feedback.error { background: rgba(255,107,125,0.15); color: #ff6b7d; }

.btn-continue, .btn-restart {
  margin-top: 18px; width: 100%;
  background: linear-gradient(135deg,#ffd76a,#ff9d4d);
  border: none; color: #241a45; font-weight: 700;
  padding: 12px; border-radius: 12px; cursor: pointer;
  display: flex; align-items: center; justify-content: center; gap: 8px;
}

/* ---------- END MODAL ---------- */
.end-modal { text-align: center; }
.end-icon { font-size: 3rem; color: #ffd76a; margin-bottom: 10px; }
.end-modal.gameover .end-icon { color: #ff6b7d; }
.end-sub { opacity: 0.85; font-size: 0.9rem; margin-bottom: 16px; }
.end-stats { background: rgba(255,255,255,0.05); border-radius: 10px; padding: 12px; margin-bottom: 6px; }
.stat-row { display: flex; justify-content: space-between; font-size: 0.9rem; padding: 4px 0; }

/* ---------- TOAST ---------- */
.toast-box {
  position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%);
  padding: 12px 20px; border-radius: 30px; font-size: 0.88rem; font-weight: 600;
  z-index: 200; box-shadow: 0 10px 25px rgba(0,0,0,0.4);
}
.toast-box.success { background: #1f7a52; color: #fff; }
.toast-box.error { background: #7a1f2f; color: #fff; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.25s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.toast-enter-active, .toast-leave-active { transition: all 0.3s; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translate(-50%, 20px); }
</style>