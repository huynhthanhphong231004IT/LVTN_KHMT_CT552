<template>
  <div class="wrapper" :class="{ 'shake-screen': isShaking, 'museum-core-theme': true }">
    <div class="top-bar" :class="{ 'scan-active-bar': activeFirewall }">
      <div class="progress-container">
        <div
          class="progress-fill museum-energy-fill"
          :style="{ width: progressPercent + '%' }"
        ></div>
        <span class="progress-text font-mono">
          {{ activeFirewall ? 'HỆ THỐNG ĐANG QUÉT KHÔI PHỤC PHÂN MẢNH LỊCH SỬ CORES' : `TIẾN TRÌNH PHỤC CHẾ DI SẢN: ${progressPercent}%` }}
        </span>
      </div>
    </div>

    <div class="game-layout">
      <div class="main-game-area">
        <div class="area-header">
          <h2 class="neon-text-gold"><i class="bi bi-bank2"></i> TRẠM PHỤC CHẾ: {{ currentArtifactName.toUpperCase() }}</h2>
          <p class="game-hint-text" style="color: #ffff; font-size: 15px;">
            <strong>Trải nghiệm tương tác:</strong> Để vượt qua trạm này, người chơi cần mở khóa tất cả các mảng ghép để tìm cổ vật bị ẩn. Hãy tận dụng các vật phẩm để kiếm điểm tri thức cao nhất trong thời gian ngắn nhất.
          </p>
        </div>

        <div class="grid-container-relative" style="position: relative; width: fit-content; margin: 0 auto;">
          
        <div class="grid-container" :class="{ 'grid-disabled': waitingAnswer }">
          <div
            v-for="(cell, index) in cells"
            :key="cell.id"
            class="cell-box museum-cell"
            :class="{
              'is-locked': waitingAnswer && currentIndex !== index,
              'is-answered': answered[index],
              'is-revealed': revealed[index],
              'cell-trap-detected': cell.scanned && cell.isTrap,
              'cell-buff-detected': cell.scanned && cell.isBuff,
              'cell-trap-revealed': revealed[index] && cell.isTrap,
              'cell-buff-revealed': revealed[index] && cell.isBuff,
              'use-xray-vision': xrayActive && !answered[index]
            }"
            @click="handleCellClick(index)"
          >
            <div class="cell-front">
              <div class="historical-dust-matrix" v-if="!revealed[index] && !answered[index]">
                <i class="bi" :class="dynamicCryptoDisplay[index] || 'bi-bank2'"></i>
                <i class="bi" :class="dynamicCryptoDisplay[index] || 'bi-book-fill'"></i>
              </div>

              <div class="inner-pattern">
                <i v-if="cell.scanned && cell.isTrap" class="bi bi-shield-exclamation text-amber-pulse"></i>
                <i v-else-if="cell.scanned && cell.isBuff" class="bi bi-gem text-emerald-pulse"></i>
                <span v-else class="hologram-question">?</span>
              </div>
            </div>

            <div class="cell-back">
              <div class="museum-laser-line" v-if="revealed[index] && !answered[index]"></div>

              <img
                v-if="revealed[index]"
                :src="cell.url"
                alt="Museum Piece"
                class="src-image"
                :class="{ 'image-restored': answered[index] }"
              />

              <span v-if="revealed[index] && cell.isTrap" class="badge-node trap-badge">
                CỔ VẬT BỊ MÒN
              </span>

              <span v-if="revealed[index] && cell.isBuff" class="badge-node buff-badge">
                CỔ VẬT HOÀN HẢO
              </span>

              <div v-if="answered[index]" class="success-overlay museum-overlay">
                <div class="crypto-char-badge neon-glow-gold">
                  <i class="bi" :class="cell.cryptoChar"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
<div class="core-wrapper">

  <div class="core-diamond-slot" :class="{ 'core-disabled': coreArtifactUploaded }" @click="triggerCoreUpload">
    <div class="core-diamond-content">
      <input 
        type="file" 
        ref="coreFileInp" 
        style="display: none;" 
        accept="image/*" 
        @change="handleCoreArtifactUpload"
      />

      <div v-if="!coreArtifactUploaded" class="core-status-init">
        <i class="bi bi-camera-fill" style="font-size: 2.2rem; color: #ffffff; opacity: 0.5;"></i>
      </div>

      <div v-else class="core-status-result">
        <img v-if="coreSuccess" :src="coreImageUrl" class="core-preview-image" alt="Core Artifact" />
        <div v-else class="core-verify-container">
          <i class="bi bi-shield-check text-info" style="font-size: 1.8rem; filter: drop-shadow(0 0 5px rgba(0, 240, 255, 0.4));"></i>
        </div>
      </div>
    </div>
  </div>

  <div v-if="!coreArtifactUploaded" class="core-hover-note">
    <div class="note-arrow"></div> <i class="bi bi-info-circle-fill"></i>
    <span>Chụp ảnh cổ vật tương tự cổ vật bị ẩn để <strong class="highlight-x2">X2 điểm</strong></span>
  </div>

</div>
          <div 
            v-for="corner in corners" 
            :key="corner.position" 
            class="corner-lock-slot" 
            :class="[corner.position, { 'corner-answered': corner.answered }]"
            @click="handleCornerClick(corner)"
          >
            <div class="corner-stripe-pattern"></div>
            <div class="corner-status-indicator">
              <i v-if="corner.answered" class="bi bi-unlock-fill text-emerald-pulse"></i>
              <i v-else class="bi bi-lock-fill corner-lock-text text-warning"></i>
            </div>
          </div>

        </div>

        <div class="crypto-key-panel museum-panel">
          <div class="crypto-panel-title font-mono">
            <i class="bi bi-key-fill text-museum-gold"></i>
            CHUỖI CỔ TỰ ĐỒNG BỘ HIỆN VẬT (VECTOR HOÀNG GIA):
          </div>

          <div class="crypto-slots">
            <div
              v-for="(cell, idx) in cells"
              :key="'slot-'+idx"
              class="crypto-slot-box museum-slot"
              :class="{ 'slot-unlocked': answered[idx], 'slot-morphing': !answered[idx] }"
            >
              <i
                class="bi crypto-icon"
                :class="answered[idx] ? cell.cryptoChar : dynamicCryptoDisplay[idx]"
              ></i>
            </div>
          </div>
        </div>

        <div class="mini-stats">
          <div class="stat-item user-live-card"><i class="bi bi-shield-shaded text-emerald"></i> {{ lives }} NĂNG LƯỢNG</div>
          <div class="stat-item dynamic-score"><i class="bi bi-mortarboard-fill text-gold"></i> {{ score }} ĐIỂM TRI THỨC</div>
          
          <button 
            class="stat-item key-radar-btn radar-interactive" 
            :disabled="keys <= 0 || waitingAnswer"
            @click="triggerRadarScanWithAudio"
            title="Sử dụng 1 Thẻ Bản Đồ để quét tìm vị trí đặc biệt"
          >
            <i class="bi bi-eye-fill text-amber"></i>
              THẤU KÍNH CỔ VẬT
              (<i class="bi bi-key-fill text-warning"></i> {{ keys }})
          </button>
          
          <button 
            class="stat-item cyber-exchange-btn" 
            :disabled="keys <= 0"
            @click="activateXrayVision"
            :class="{ 'btn-active-neon': xrayActive }"
          >
            <i class="bi bi-vector-pen text-cyan"></i>
              KÍNH LÚP X-RAY
              (<i class="bi bi-key-fill museum-key-icon"></i> {{ keys }})
          </button>
        </div>
      </div>

<div class="map-sidebar museum-sidebar">
        <div class="sidebar-header">
          <h3 class="neon-text-gold"><i class="bi bi-map-fill"></i> SƠ ĐỒ PHÒNG TRƯNG BÀY</h3>
          <span class="map-badge sub-neon">LỘ TRÌNH 4.0</span>
        </div>

        <div class="map-nodes-container">
          <div 
            v-for="(node, idx) in mapData.threeNodes" 
            :key="node.id" 
            class="map-node-row"
            :class="getNodeStatusClass(node)"
          >
            <div v-if="idx < mapData.threeNodes.length - 1" class="node-connector-line"></div>

            <div class="node-circle">
              <i v-if="node.status === 'passed'" class="bi bi-bookmark-check-fill text-museum-green"></i>
              <i v-else-if="node.status === 'current'" class="bi bi-geo-alt-fill pin-museum-animation"></i>
              <i v-else class="bi bi-lock-fill"></i>
            </div>

            <div class="node-info">
              <div class="node-name font-mono">
                {{ node.name }}
              </div>
              <div class="node-status-text">
                <span v-if="node.status === 'passed'">ĐÃ TÌM HIỂU // HOÀN THÀNH</span>
                <span v-else-if="node.status === 'current'"> ĐANG KHẢO CỔ SỐ HÓA</span>
                <span v-else>PHÒNG TRƯNG BÀY ĐANG KHÓA</span>
              </div>
            </div>
          </div>
        </div>
        <div class="mission-box museum-mission">
          <h4 class="text-gold"><i class="bi bi-info-circle-fill"></i> THÔNG TIN NHIỆM VỤ:</h4>
          <p class="font-mono text-sm text-amber-200">Hãy trả lời chính xác các câu hỏi lịch sử để mở khóa trọn vẹn {{ cells.length }} phân mảnh hình ảnh và các góc bảo vệ hiện vật: [{{ currentArtifactName }}].</p>
        </div>

        <div class="tutorial-box museum-tutorial">
          <h4 class="text-cyan"><i class="bi bi-compass-fill"></i> CẨM NANG KHẢO CỔ:</h4>
          <ul class="tutorial-steps font-mono text-xs">
            <li class="step-item">
              <i class="bi bi-cursor-fill step-icon text-gold"></i>
              <span><strong>Bước 1:</strong> Chọn các ô vuông <span class="text-highlight">?</span> trên lưới để kích hoạt tia quét phân mảnh hiện vật.</span>
            </li>
            <li class="step-item">
              <i class="bi bi-patch-question-fill step-icon text-amber"></i>
              <span><strong>Bước 2:</strong> Vượt qua thử thách trắc nghiệm lịch sử để giải mã và phục dựng hình ảnh lõi.</span>
            </li>
            <li class="step-item">
              <i class="bi bi-lightning-charge-fill step-icon text-cyan"></i>
              <span><strong>Bước 3:</strong> Sử dụng <span class="text-highlight">Thấu kính</span> hoặc <span class="text-highlight">Kính lúp X-Ray</span> khi gặp cổ tự phức tạp.</span>
            </li>
            <li class="step-item">
              <i class="bi bi-shield-lock-fill step-icon text-emerald"></i>
              <span><strong>Bước 4:</strong> Click 4 góc bảo vệ <span class="text-highlight">🔒</span> để giải khóa đồng bộ hoàn toàn ma trận hoàng gia.</span>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <transition name="modal-fade">
      <div v-if="currentQuestion" class="modal-overlay museum-blur-overlay">
        <div class="question-card museum-hologram" :class="{ 'firewall-card-active': activeFirewall }">
          <div class="question-header">
            <div class="header-title">
              <i class="bi" :class="activeFirewall ? 'bi-exclamation-octagon-fill text-amber pulse-anim' : 'bi-lightbulb-fill text-gold'"></i>
              <span class="font-mono">{{ isCornerQuestion ? ' GIẢI MÃ KHÓA PHÒNG THỦ GÓC' : (activeFirewall ? 'DỮ LIỆU CỔ BỊ NHÒE - GIẢI MÃ NHANH' : 'TRUNG TÂM TRI THỨC LỊCH SỬ') }}</span>
            </div>
            <div class="combo-badge museum-combo" v-if="combo > 0">CHUỖI ĐÚNG x{{ combo }}</div>
          </div>

          <div class="firewall-countdown-bar" v-if="activeFirewall">
            <div class="firewall-countdown-fill" :style="{ width: (firewallTimeLeft / 12) * 100 + '%' }"></div>
          </div>

          <div class="question-body">
            <div v-if="activeFirewall" class="firewall-alert-text text-flash">
              PHÂN MẢNH MẤT ỔN ĐỊNH: Niên đại hiện vật sâu dữ liệu đang biến đổi, đưa ra đáp án ngay!
            </div>

            <p class="question-text">{{ currentQuestion.question }}</p>
            
            <button 
              v-if="waitingAnswer && keys > 0" 
              class="btn-use-key-bypass museum-btn"
              @click="useKeyToEliminateOptionsWithAudio"
            >
              <i class="bi bi-key-fill museum-key-icon"></i>
              DÙNG THẺ GỢI Ý: Loại bỏ 2 phương án sai
            </button>

            <div class="options-list">
              <button
                v-for="option in currentQuestion.options"
                :key="option"
                class="option-item interactive-option museum-opt"
                :disabled="!waitingAnswer || disabledOptions.includes(option)"
                :class="{
                  'is-correct': !waitingAnswer && option === currentQuestion.answer,
                  'is-wrong': !waitingAnswer && selectedAnswer === option && option !== currentQuestion.answer,
                  'is-eliminated': disabledOptions.includes(option)
                }"
                @click="handleCheckAnswer(option)"
              >
                <span class="option-indicator"></span>
                <span class="option-label">{{ option }}</span>
              </button>
            </div>
            <div v-if="result" class="feedback-msg" :class="selectedAnswer === currentQuestion.answer ? 'text-success text-bounce' : 'text-danger text-shake'">
              {{ result }}
            </div>
          </div>
        </div>
      </div>
    </transition>

    <transition name="modal-fade">
      <div v-if="gameState !== 'playing'" class="modal-overlay">
        <div class="end-game-card museum-end-card" :class="gameState">
          <div class="end-icon">
            <i v-if="gameState === 'victory'" class="bi bi-award-fill text-museum-gold trophy-spin"></i>
            <i v-else class="bi bi-exclamation-triangle-fill text-red skull-pulse"></i>
          </div>
          <h2 class="neon-text">{{ gameState === 'victory' ? 'PHỤC CHẾ HOÀN TOÀN!' : 'MẤT TÍN HIỆU PHỤC CHẾ!' }}</h2>
          <p class="end-subtitle font-mono">
            {{ gameState === 'victory' ? `Hiện vật [${currentArtifactName}] đã được đưa vào danh mục di sản số quốc gia.` : 'Năng lượng quét cạn kiệt. Vui lòng nạp lại hệ thống khảo cổ.' }}
          </p>
          
          <div class="exhibit-info-box" v-if="gameState === 'victory'">
            <h4><i class="bi bi-journal-text"></i> TÓM TẮT DI SẢN LỊCH SỬ</h4>
            <p>Hiện vật đóng vai trò quan trọng trong sự nghiệp bảo vệ nước nhà, thể hiện trí tuệ và tinh thần kiên cường của quân và dân ta qua các thời kỳ hào hùng.</p>
          </div>

          <div class="end-stats">
            <div class="stat-row"><span>Điểm tích lũy bảo tàng:</span><strong>{{ score }} TRI THỨC</strong></div>
            <div class="stat-row"><span>Tỷ lệ chính xác lịch sử:</span><strong>{{ accuracy }}%</strong></div>
          </div>
          <div class="end-actions">
            <button class="btn-action primary museum-btn-action" @click="restartGame">Tải lại vùng di sản</button>
            <button v-if="gameState === 'victory'" class="btn-action success museum-btn-action" @click="continueGame">Đến Trung Tâm Thảo Luận <i class="bi bi-arrow-right"></i></button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="toast">
      <div v-if="toast.show" class="modern-toast" :class="toast.type">
        <div class="toast-content"><span>{{ toast.message }}</span></div>
      </div>
    </transition>

    <div class="museum-chat-trigger" @click="toggleChatBox" v-if="!isChatOpen">
          <i class="bi bi-robot"></i>
          <span class="trigger-ping"></span>
        </div>

    <div class="museum-chat-box" v-if="isChatOpen">
      <div class="museum-chat-header">
        <div class="header-bot-info">
          <i class="bi bi-robot text-gold animate-bounce"></i>
          <div>
            <h4>Trợ Lý Thuyết Minh RAG</h4>
            <span class="status-dot">Trực tuyến về mỏ neo tàu</span>
          </div>
        </div>
        <button class="chat-close-btn" @click="toggleChatBox">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>

      <div class="museum-chat-body">
        <div 
          v-for="(msg, index) in chatMessages" 
          :key="index" 
          class="chat-bubble-row" 
          :class="msg.sender"
        >
          <div class="bubble-avatar">
            <i :class="msg.sender === 'bot' ? 'bi bi-bank' : 'bi bi-person-fill'"></i>
          </div>
          <div class="bubble-content">
            <p class="bubble-text">{{ msg.text }}</p>
            
            <div class="chat-suggestions" v-if="msg.suggestions && msg.suggestions.length > 0">
              <span 
                v-for="(sug, sIdx) in msg.suggestions" 
                :key="sIdx"
                class="suggestion-tag"
                @click="sendChatMessage(sug)"
              >
                # {{ sug }}
              </span>
            </div>
          </div>
        </div>

        <div class="chat-bubble-row bot" v-if="isChatLoading">
          <div class="bubble-avatar"><i class="bi bi-bank"></i></div>
          <div class="bubble-content loading-typing">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <div class="museum-chat-footer">
        <input 
          type="text" 
          v-model="chatInput" 
          placeholder="Hỏi về lịch sử, thông số" 
          @keyup.enter="sendChatMessage()"
        />
        <button class="chat-send-btn" @click="sendChatMessage()" :disabled="isChatLoading">
          <i class="bi bi-send-fill"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { useRouter } from "vue-router";
const router = useRouter();
const graphNodesStatic = {
  0:  { name: "Bệ và đạn tên lửa SAM" },
  1:  { name: "Bom" },
  2:  { name: "Ghe xuồng thuyền" },
  3:  { name: "Lu hầm bí mật" },
  4:  { name: "Máy bay trực thăng" },
  5:  { name: "Máy cán Tol" },
  6:  { name: "Máy in" },
  7:  { name: "Mỏ neo tàu" },
  8:  { name: "Pháo" },
  9:  { name: "Súng thần công" },
  10: { name: "Tàu tuần tiều PCF" },
  11: { name: "Trục máy bay B52" },
  12: { name: "Xe bọc thép" },
  13: { name: "Xe Peugeot" },
  14: { name: "Xe tăng" }
};
const mapData = ref({ threeNodes: [] });
const currentArtifactName = ref("Mỏ neo tàu");
const currentArtifactId = ref(7);
const MAX_LIVES = 3;
const BASE_SCORE = 10;
const TIME_BONUS_LIMIT = 5;
const cells = ref([]);
const answers = ref([]);
const revealed = ref([]);
const answered = ref([]);
const currentQuestion = ref(null);
const currentIndex = ref(-1);
const selectedAnswer = ref(null);
const result = ref("");
const score = ref(0);
const lives = ref(MAX_LIVES);
const keys = ref(0);
const combo = ref(0);
const loadingQuestion = ref(false);
const waitingAnswer = ref(false);
const correctAnswers = ref(0);
const wrongAnswers = ref(0);
const startTime = ref(Date.now());
const elapsedTime = ref(0);
const questionStartTime = ref(0);
const gameState = ref("playing");
const toast = ref({ show: false, message: "", type: "success" });
let timer = null;

const activeFirewall = ref(false);
const firewallTimeLeft = ref(12);
const disabledOptions = ref([]);
let firewallTimer = null;
const cryptoPool = [
  "bi-shield-fill",
  "bi-shield-lock-fill",
  "bi-award-fill",
  "bi-award",
  "bi-flag-fill",
  "bi-flag",
  "bi-star-fill",
  "bi-stars",
  "bi-trophy-fill",
  "bi-trophy",
  "bi-bank2",
  "bi-building",
  "bi-compass-fill",
  "bi-compass",
  "bi-book-fill",
  "bi-book",
  "bi-journal-bookmark-fill",
  "bi-camera-fill",
  "bi-camera",
  "bi-binoculars-fill",
  "bi-globe-asia-australia",
  "bi-globe2",
  "bi-pin-map-fill",
  "bi-pin-map",
  "bi-geo-alt-fill",
  "bi-geo-alt",
  "bi-lightning-fill",
  "bi-fire",
  "bi-gem",
  "bi-hexagon-fill",
  "bi-diamond-fill",
  "bi-bullseye",
  "bi-crosshair",
  "bi-cpu-fill",
  "bi-collection-fill"
];
const dynamicCryptoDisplay = ref([]);
let morphingTimer = null;
const isShaking = ref(false);
let audioCtx = null;
const xrayActive = ref(false);
const coreFileInp = ref(null);
const coreArtifactUploaded = ref(false);
const coreSuccess = ref(false);
const coreImageUrl = ref(""); 
const isCornerQuestion = ref(false);
const activeCornerPosition = ref("");
const corners = ref([
  { position: "top-left", answered: false },
  { position: "top-right", answered: false },
  { position: "bottom-left", answered: false },
  { position: "bottom-right", answered: false }
]);
const maxKeys = computed(() => Math.ceil(cells.value.length / 3) || 0);
const answeredCount = computed(() => answered.value.filter(Boolean).length);
const progressPercent = computed(() => {
  if (!cells.value.length) return 0;
  const totalItems = cells.value.length + corners.value.length;
  const totalAnswered = answeredCount.value + corners.value.filter(c => c.answered).length;
  return Math.round((totalAnswered / totalItems) * 100);
});
const accuracy = computed(() => {
  const total = correctAnswers.value + wrongAnswers.value;
  return total ? Math.round((correctAnswers.value / total) * 100) : 0;
});
function showToast(message, type = "success") {
  toast.value = { show: true, message, type };
  setTimeout(() => { toast.value.show = false; }, 2500);
}
function startCryptoMorphing() {
  morphingTimer = setInterval(() => {
    cells.value.forEach((cell, idx) => {
      if (!answered.value[idx]) {
        const randomChar = cryptoPool[Math.floor(Math.random() * cryptoPool.length)];
        dynamicCryptoDisplay.value[idx] = randomChar;
        cell.cryptoChar = randomChar;
      }
    });
  }, 1000);
}
function activateXrayVision() {
  if (keys.value <= 0) {
    showToast("Bạn cần Thẻ Chìa Khóa để kích hoạt Thấu kính X-Ray!", "error");
    return;
  }
  keys.value--;
  xrayActive.value = true;
  playSynthSound(880, "sine", 0.4);
  showToast(" Chế độ X-Ray được kích hoạt: Cấu trúc lõi hiện vật đã hiển thị mờ!", "success");
  setTimeout(() => {
    xrayActive.value = false;
  }, 4000);
}
function playSynthSound(freq, type, duration) {
  try {
    if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = audioCtx.createOscillator();
    const gainNode = audioCtx.createGain();
    osc.type = type;
    osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
    gainNode.gain.setValueAtTime(0.1, audioCtx.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.00001, audioCtx.currentTime + duration);
    osc.connect(gainNode);
    gainNode.connect(audioCtx.destination);
    osc.start();
    osc.stop(audioCtx.currentTime + duration);
  } catch (e) {}
}
function triggerScreenShake() {
  isShaking.value = true;
  setTimeout(() => { isShaking.value = false; }, 500);
}
function triggerRadarScanWithAudio() {
  if (keys.value <= 0 || waitingAnswer.value) return;
  playSynthSound(523.25, "sine", 0.15);
  setTimeout(() => playSynthSound(659.25, "sine", 0.25), 100);
  triggerRadarScan();
}
function useKeyToEliminateOptionsWithAudio() {
  if (keys.value <= 0 || !currentQuestion.value || !waitingAnswer.value) return;
  playSynthSound(783.99, "triangle", 0.2);
  useKeyToEliminateOptions();
}
function handleCellClick(index) {
  if (gameState.value !== "playing" || answered.value[index] || loadingQuestion.value || waitingAnswer.value) return;
  isCornerQuestion.value = false; 
  playSynthSound(440, "sine", 0.08);
  reveal(index);
}
async function handleCornerClick(corner) {
  if (gameState.value !== "playing" || corner.answered || loadingQuestion.value || waitingAnswer.value) return;
  disabledOptions.value = [];
  loadingQuestion.value = true;
  isCornerQuestion.value = true;
  activeCornerPosition.value = corner.position;
  selectedAnswer.value = null;
  result.value = "";
  activeFirewall.value = false;
  playSynthSound(440, "sine", 0.08);
  try {
    const res = await fetch("http://localhost:8004/random-question-moneotau");
    if (!res.ok) throw new Error("Cổng kết nối API 8004 lỗi"); 
    const data = await res.json();
    currentQuestion.value = data.question;
    waitingAnswer.value = true;
    questionStartTime.value = Date.now();
  } catch (err) {
    console.error(err);
    showToast("Không thể tải câu hỏi khóa góc từ máy chủ!", "error");
  } finally {
    loadingQuestion.value = false;
  }
}
function triggerRadarScan() {
  if (keys.value <= 0 || waitingAnswer.value) return;
  keys.value--;
  showToast("Thấu kính đã rà quét! Tìm thấy khu vực cổ vật biến tính và lõi hồi phục.", "success");
  cells.value.forEach(cell => { cell.scanned = true; });
}
function useKeyToEliminateOptions() {
  if (keys.value <= 0 || !currentQuestion.value || !waitingAnswer.value) return;
  keys.value--;
  showToast("Chuyên gia bảo tàng gợi ý: Đã loại bỏ 2 phương án sai!", "gold");
  const wrongOptions = currentQuestion.value.options.filter(opt => opt !== currentQuestion.value.answer);
  const shuffledWrong = [...wrongOptions].sort(() => 0.5 - Math.random());
  disabledOptions.value = shuffledWrong.slice(0, 2);
}
function startFirewallCountdown() {
  firewallTimeLeft.value = 12;
  if (firewallTimer) clearInterval(firewallTimer);
  
  firewallTimer = setInterval(() => {
    firewallTimeLeft.value--;
    if (firewallTimeLeft.value <= 4 && firewallTimeLeft.value > 0) {
      playSynthSound(900, "sine", 0.05);
    }
    if (firewallTimeLeft.value <= 0) {
      clearInterval(firewallTimer);
      handleFirewallTimeout();
    }
  }, 1000);
}
function handleFirewallTimeout() {
  waitingAnswer.value = false;
  activeFirewall.value = false;
  combo.value = 0;
  wrongAnswers.value++;
  lives.value--;
  playSynthSound(220, "sawtooth", 0.4);
  triggerScreenShake();
  score.value = Math.max(0, score.value - 10);
  revealed.value[currentIndex.value] = false;
  result.value = `Quá thời gian quy định! Mảnh tư liệu bị đóng băng. Trừ 10 điểm!`;
  showToast("Mảnh tư liệu phục chế bị đóng băng do quá hạn!", "error");

  executeModalClosingDelay();
}
function handleCheckAnswer(option) {
  const isCorrect = option === currentQuestion.value.answer;
  if (isCorrect) {
    playSynthSound(523.25, "sine", 0.1);
    setTimeout(() => playSynthSound(1046.50, "sine", 0.2), 60);
  } else {
    playSynthSound(293.66, "triangle", 0.3);
    triggerScreenShake();
  }
  checkAnswer(option);
}
function loadMapProgress() {
  const routeRaw = localStorage.getItem('museum_generated_route');
  const lockedPath = routeRaw ? JSON.parse(routeRaw) : [];
  const savedState = localStorage.getItem('museum_game_state_save');
  let currentIdx = 0;

  if (savedState) {
    const parsed = JSON.parse(savedState);
    if (parsed.currentPathIndex !== undefined) {
      currentIdx = parsed.currentPathIndex;
    } 
    else if (parsed.currentArtifactId !== undefined && lockedPath.length > 0) {
      const foundIdx = lockedPath.indexOf(Number(parsed.currentArtifactId));
      if (foundIdx !== -1) currentIdx = foundIdx;
    }
    if (parsed.currentArtifactId !== undefined) {
      currentArtifactId.value = parsed.currentArtifactId;
      if (graphNodesStatic[currentArtifactId.value]) {
        currentArtifactName.value = graphNodesStatic[currentArtifactId.value].name;
      }
    }
  } else {
    if (lockedPath.length > 0) {
      const initialId = lockedPath[0];
      currentArtifactId.value = initialId;
      if (graphNodesStatic[initialId]) {
        currentArtifactName.value = graphNodesStatic[initialId].name;
      }
    }
  }
  const tempNodes = [];
  if (lockedPath.length > 0) {
    if (currentIdx > 0) {
      const prevId = lockedPath[currentIdx - 1];
      tempNodes.push({ 
        id: prevId, 
        name: graphNodesStatic[prevId] ? graphNodesStatic[prevId].name : `Vùng ${prevId}`, 
        status: 'passed' 
      });
    }
    const currId = lockedPath[currentIdx];
    tempNodes.push({ 
      id: currId, 
      name: graphNodesStatic[currId] ? graphNodesStatic[currId].name : `Vùng ${currId}`, 
      status: 'current' 
    });
    if (currentIdx < lockedPath.length - 1) {
      const nextId = lockedPath[currentIdx + 1];
      tempNodes.push({ 
        id: nextId, 
        name: graphNodesStatic[nextId] ? graphNodesStatic[nextId].name : `Vùng ${nextId}`, 
        status: 'future' 
      });
    }
  }
  mapData.value.threeNodes = tempNodes;
}
function getNodeStatusClass(node) {
  return { 'node-completed': node.status === 'passed', 'node-current': node.status === 'current', 'node-locked': node.status === 'future' };
}
function triggerCoreUpload() {
  if (coreArtifactUploaded.value || gameState.value !== "playing") return;
  coreFileInp.value.click();
}
async function handleCoreArtifactUpload(event) {
  const file = event.target.files[0];
  if (!file) return;
  const formData = new FormData();
  formData.append("file", file);
  try {
    coreArtifactUploaded.value = true; 
    const res = await fetch("http://localhost:8001/predict", {
      method: "POST",
      body: formData,
    });
    const data = await res.json();
    if (data.error) {
      showToast("Hệ thống nhận diện thất bại! Thử lại sau.", "error");
      coreArtifactUploaded.value = false;
      return;
    }
    if (data.message === "Too many objects detected") {
      showToast("Phát hiện quá nhiều đối tượng! Vui lòng tải ảnh chỉ chứa 1 đối tượng duy nhất.", "error");
      coreArtifactUploaded.value = false;
      return;
    }
    if (!data.results || data.results.length === 0) {
      showToast("Không tìm thấy đối tượng nào trong hình ảnh!", "error");
      score.value = 0;
      coreSuccess.value = false;
      playSynthSound(150, "sawtooth", 0.5);
      return;
    }
    const detectedCnnId = data.results[0].cnn_id; 
    if (Number(detectedCnnId) === Number(currentArtifactId.value)) {
      coreImageUrl.value = URL.createObjectURL(file);
      score.value = score.value * 2;
      coreSuccess.value = true;
      playSynthSound(880, "sine", 0.2);
      setTimeout(() => playSynthSound(1760, "sine", 0.3), 100);
      showToast("Tuyệt vời! Xác thực lõi thành công: Nhân đôi toàn bộ điểm số!", "success");
    } else {
      score.value = 0;
      coreSuccess.value = false;
      playSynthSound(150, "sawtooth", 0.6);
      triggerScreenShake();
      showToast("Sai hiện vật trạm phục chế! Toàn bộ điểm tri thức bị đặt về 0!", "error");
    }
  } catch (err) {
    console.error(err);
    showToast("Không thể kết nối đến máy chủ AI!", "error");
    coreArtifactUploaded.value = false;
  }
}
onMounted(async () => {
  loadMapProgress();
  timer = setInterval(() => {
    if (gameState.value === "playing") {
      elapsedTime.value = Math.floor((Date.now() - startTime.value) / 1000);
    }
  }, 1000);
  await initGame();
  startCryptoMorphing();
});
onBeforeUnmount(() => { 
  clearInterval(timer); 
  if (firewallTimer) clearInterval(firewallTimer);
  if (morphingTimer) clearInterval(morphingTimer);
  
  if (coreImageUrl.value && coreImageUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(coreImageUrl.value);
  }
});
async function initGame() {
  try {
    const res = await fetch("http://localhost:3000/api/puzzle/moneotau");
    const data = await res.json();
    cells.value = (data.images || []).map((img, i) => {
      const isTrap = i === 2 || i === 6;
      const isBuff = i === 4;
      dynamicCryptoDisplay.value.push(cryptoPool[i % cryptoPool.length]);
      return {
        ...img,
        url: "http://localhost:3000" + img.url,
        isTrap: isTrap,
        isBuff: isBuff,
        scanned: false,
        cryptoChar: cryptoPool[i % cryptoPool.length]
      };
    });
    revealed.value = Array(cells.value.length).fill(false);
    answered.value = Array(cells.value.length).fill(false);
    const res2 = await fetch("http://localhost:8003/game/decrypt-moneotau", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        run_id: "20260609_022632",
        wm_dir: "D:/LUAN_VAN/MaHoa/runs/moneotau/game_images",
      }),
    });
    const data2 = await res2.json();
    answers.value = data2.answers.map((item) => item.id);
  } catch (err) {
    console.error(err);
  }
}
async function reveal(index) {
  if (gameState.value !== "playing" || answered.value[index] || loadingQuestion.value || waitingAnswer.value) return;
  disabledOptions.value = [];
  loadingQuestion.value = true;
  currentIndex.value = index;
  revealed.value[index] = true;
  selectedAnswer.value = null;
  result.value = "";
  if (cells.value[index].isBuff) {
    loadingQuestion.value = false;
    answered.value[index] = true;
    playSynthSound(587.33, "sine", 0.15);
    setTimeout(() => playSynthSound(698.46, "sine", 0.3), 80);
    if (lives.value < MAX_LIVES) lives.value++;
    score.value += 15;
    showToast("Bạn tìm thấy Cổ Vật Nguyên Bản: Nhận 1 Năng lượng bảo tồn và 15 điểm tri thức!", "success");
    checkWinOrContinue();
    return;
  }
  try {
    const res = await fetch("http://localhost:3000/api/game/question_moneotau", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ id: answers.value[index] }),
    });
    currentQuestion.value = await res.json();
    waitingAnswer.value = true;
    questionStartTime.value = Date.now();

    if (cells.value[index].isTrap) {
      activeFirewall.value = true;
      startFirewallCountdown();
    } else {
      activeFirewall.value = false;
    }

  } catch (err) {
    console.error(err);
    revealed.value[index] = false;
  } finally {
    loadingQuestion.value = false;
  }
}
function checkAnswer(option) {
  if (!currentQuestion.value || !waitingAnswer.value) return;
  if (activeFirewall.value && firewallTimer) clearInterval(firewallTimer);
  selectedAnswer.value = option;
  waitingAnswer.value = false;
  const isCorrect = option === currentQuestion.value.answer;
  if (isCorrect) {
    combo.value++;
    let points = BASE_SCORE;
    if (combo.value > 1) points += combo.value * 2;
    const spent = Math.floor((Date.now() - questionStartTime.value) / 1000);
    points += Math.max(0, TIME_BONUS_LIMIT - spent);
    
    if (activeFirewall.value) {
      points *= 2;
      showToast("Phục chế xuất sắc mảnh di sản biến tính! Nhận X2 Điểm tư liệu!", "success");
    }
    score.value += points;
    correctAnswers.value++;
    if (isCornerQuestion.value) {
      const targetCorner = corners.value.find(c => c.position === activeCornerPosition.value);
      if (targetCorner) targetCorner.answered = true;
      result.value = `Hoàn thành! Đã hóa giải khóa góc phòng thủ thủ [${activeCornerPosition.value.toUpperCase()}] (+${points} điểm)`;
    } else {
      answered.value[currentIndex.value] = true;
      if (answeredCount.value % 3 === 0 && keys.value < maxKeys.value) {
        keys.value++;
        showToast("Nhận được một Thẻ Bản Đồ Tri Thức mới!", "gold");
      }
      result.value = `Hoàn thành! Thu thập thành công mảnh cổ tự [${cells.value[currentIndex.value].cryptoChar}] (+${points} điểm)`;
    }

  } else {
    combo.value = 0;
    wrongAnswers.value++;
    lives.value--;
    if (!isCornerQuestion.value) {
      revealed.value[currentIndex.value] = false;
      if (activeFirewall.value) {
        score.value = Math.max(0, score.value - 10);
        showToast("Trả lời sai tại phân mảnh biến tính bị trừ 10 điểm tích lũy!", "error");
      }
    } else {
      showToast("Trả lời sai hệ thống phòng thủ góc! Thao tác thất bại.", "error");
    }

    result.value = `Sai sót tư liệu! Gợi ý đáp án chính xác: ${currentQuestion.value.answer}`;
  }
  activeFirewall.value = false;
  executeModalClosingDelay();
}
function executeModalClosingDelay() {
  setTimeout(() => {
    if (lives.value <= 0) {
      gameState.value = "gameover";
      currentQuestion.value = null;
      clearInterval(timer);
      playSynthSound(146.83, "triangle", 0.8);
      return;
    }
    checkWinOrContinue();
  }, 2000);
}
function checkWinOrContinue() {
  const allCellsAnswered = answered.value.every(Boolean);
  const allCornersAnswered = corners.value.every(c => c.answered);
  if (allCellsAnswered && allCornersAnswered) {
    gameState.value = "victory";
    currentQuestion.value = null;
    clearInterval(timer);
    saveGameProgress();
    return;
  }
  currentQuestion.value = null;
}
function saveGameProgress() {
  const savedState = localStorage.getItem('museum_game_state_save');
  let parsed = savedState ? JSON.parse(savedState) : {};
  if (!parsed.stationHistoryLog) {
    parsed.stationHistoryLog = {};
  }
  parsed.stationHistoryLog[currentArtifactId.value] = {
    scoreSpent: score.value,               
    timeSpent: elapsedTime.value,          
    clearedAt: new Date().toLocaleTimeString()
  };
  parsed.currentStageCleared = true;
  localStorage.setItem('museum_game_state_save', JSON.stringify(parsed));
  loadMapProgress();
}
function restartGame() { window.location.reload(); }
function continueGame() { router.push("/chatbox"); }
const isChatOpen = ref(false);          
const chatInput = ref("");              
const isChatLoading = ref(false);      
const chatMessages = ref([             
  {
    sender: "bot",
    text: "Xin chào! Tôi là thuyết minh viên quân đội tại Bảo tàng Quân khu 9. Bạn có thắc mắc gì về hiện vật không?",
    suggestions: ["Lịch sử về hiện vật", "Chiến công nổi bật", "Thông số kỹ thuật"]
  }
]);
function toggleChatBox() {
  isChatOpen.value = !isChatOpen.value;
}
async function sendChatMessage(textToSend) {
  const message = textToSend || chatInput.value.trim();
  if (!message || isChatLoading.value) return;
  if (!textToSend) chatInput.value = "";
  chatMessages.value.push({ sender: "user", text: message });
  isChatLoading.value = true;

  try {
    const res = await fetch("http://localhost:8000/chat_2", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        message: message,
        label: 7
      })
    });

    if (!res.ok) throw new Error("Lỗi kết nối API Chat");
    const data = await res.json();

    if (data.responses && data.responses.length > 0) {
      const responseData = data.responses[0];
      chatMessages.value.push({
        sender: "bot",
        text: responseData.answer,
        suggestions: responseData.suggestions || []
      });
    } else {
      chatMessages.value.push({
        sender: "bot",
        text: "Xin lỗi, hệ thống không tìm thấy dữ liệu tư liệu phù hợp.",
        suggestions: []
      });
    }
  } catch (error) {
    console.error(error);
    chatMessages.value.push({
      sender: "bot",
      text: "Hệ thống kết nối đến máy chủ thuyết minh gặp sự cố. Vui lòng thử lại sau!",
      suggestions: []
    });
  } finally {
    isChatLoading.value = false;
    setTimeout(() => {
      const container = document.querySelector(".museum-chat-body");
      if (container) container.scrollTop = container.scrollHeight;
    }, 100);
  }
}
</script>

<style scoped>
@import "../assets/css/puzzle.css";
</style>