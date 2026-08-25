<template>
<div class="wrapper">
    <div class="global-progress-bar-container">
      <div class="progress-header">
        <span class="progress-title">
          <i class="bi bi-lightning-charge-fill animate-lightning-icon"></i> Tiến trình khám phá 
        </span>
        
        <span class="progress-stat">
          <strong>{{ clearedStationsCount }}</strong> / 15 Trạm
        </span>
      </div>
      
      <div class="cyber-lightning-zone">
        
        <svg class="lightning-filters" style="position: absolute; width: 0; height: 0;">
          <filter id="plasma-lightning">
            <feTurbulence type="fractalNoise" baseFrequency="0.04" numOctaves="4" result="noise" />
            <feDisplacementMap in="SourceGraphic" in2="noise" scale="15" xChannelSelector="R" yChannelSelector="G" />
          </filter>
        </svg>

        <div class="energy-emitter left-emitter"></div>

        <div class="cyber-progress-track">
          <div class="cyber-progress-bar" :style="{ width: (clearedStationsCount / 15 * 100) + '%' }"></div>
          
          <div class="lightning-plasma-line"></div>
          <div class="lightning-plasma-line-spiral"></div>
        </div>

        <div class="energy-emitter right-emitter"></div>
      </div>
      
      <div class="progress-footer-tips">
        <span class="hero-rank-text">
          <i class="bi bi-award-fill"></i> Cấp bậc hiện tại: <strong>{{ getHeroTitle() }}</strong>
        </span>
          <button class="btn-history-toggle" @click="showRulesModal">
            <i class="bi bi-info-circle-fill"></i>
            <span>Quy định Cấp bậc</span>
          </button>
          <button class="btn-history-toggle" @click="showHistoryLog = !showHistoryLog">
                <i class="bi bi-journal-text"></i> 
                <span>Nhật ký ({{ clearedStationsCount }}/15)</span>
          </button>
      </div>

    </div>
    <div class="main-topic">
    <div class="cyber-grid-bg"></div>
    <div class="ai-scanning-light"></div>
    <div class="chat-wrapper">
      <div class="chat-container">
        
        <div class="header">
          <div class="ai-brand">
            <span class="brand-title">Hành Trình Di Sản Quân Khu 9</span>
            <span class="status-tag" :class="{ 'active': !gameLocked }">
              <i :class="gameLocked ? 'bi bi-lock-fill' : 'bi bi-unlock-fill'"></i>
              {{ gameLocked ? 'LỘ TRÌNH ĐÃ KHÓA' : 'CHỜ ĐỊNH VỊ ẢNH' }}
            </span>
          </div>
          
          <div class="timer-display" :class="{ 'timer-low': timeLeft < 60 }" v-if="gameLocked && !currentStageCleared">
            <i class="bi bi-clock-history"></i>
            <span>{{ formatTime(timeLeft) }}</span>
          </div>
        </div>

        <div class="upload-section" v-if="!gameLocked">
          <p class="upload-hint">Tải lên hình ảnh hiện vật bảo tàng để xác định vị trí xuất phát của bạn:</p>
          <div class="upload-controls">
            <input type="file" ref="fileInput" @change="handleImageUpload" accept="image/*" class="file-hidden" />
            
            <button class="icon-btn-upload" @click="$refs.fileInput.click()" title="Chụp hoặc tải ảnh lên">
              <i class="bi bi-camera-fill"></i>
            </button>
            
            <span class="upload-text-label">Chụp hoặc chọn ảnh hiện vật từ thiết bị</span>
            
            <div v-if="uploading" class="spinner-ai"></div>
          </div>
        </div>


        <div class="chat-box custom-scrollbar" ref="chatBox">
          <div v-for="(m, i) in messages" :key="i" class="message-wrapper">
            <div :class="['msg-row', m.role === 'user' ? 'user' : 'bot']">
              <div :class="['avatar', m.role === 'user' ? 'user-avatar' : 'bot-avatar']">
                <i :class="m.role === 'user' ? 'bi bi-person-fill' : 'bi bi-robot'"></i>
              </div>
              <div :class="['bubble', m.role === 'user' ? 'user' : 'bot']">
                
                <div v-if="m.image" class="msg-image-container">
                  <img :src="m.image" class="chat-sent-image" alt="Hình ảnh đính kèm" />
                </div>

                <div class="msg-text" v-html="formatRichText(m.text)"></div>
                
                <div v-if="m.role === 'bot'" class="tts-control-box">
                  <button class="btn-tts" @click="speakText(m.text)">
                    <i class="bi" :class="isSpeaking && activeSpeechText === m.text ? 'bi-volume-mute-fill' : 'bi-volume-up-fill'"></i>
                    <span>{{ isSpeaking && activeSpeechText === m.text ? 'Dừng thuyết minh' : 'Nghe thuyết minh' }}</span>
                  </button>
                </div>

                <div v-if="m.role === 'bot' && m.suggestions && m.suggestions.length > 0" class="suggestion-box">
                  <div class="suggest-title"><i class="bi bi-lightning-charge-fill"></i> Gợi ý tìm hiểu thêm:</div>
                  <div class="suggestion-list">
                    <button v-for="(s, idx) in m.suggestions" :key="idx" class="suggest-btn" @click="selectSuggestion(s)">
                      <span>{{ s }}</span>
                    </button>
                  </div>
                </div>

              </div>
            </div>
          </div>
          <div v-if="loading" class="typing">
            <div class="typing-loading-dots"><span></span><span></span><span></span></div>
            <span>Đang truy xuất trung tâm cơ sở dữ liệu...</span>
          </div>
        </div>

        <div class="game-action-panel" v-if="gameLocked">
          <div class="task-status">
            <span v-if="currentStageCleared" class="text-success animate-pulse">
              <i class="bi bi-shield-check"></i> Hệ thống đã xác thực mật mã lõi thành công! Trạm đã được mở khóa.
            </span>
            <span v-else-if="!timeOutReached" class="text-normal">
              <i class="bi bi-info-circle-fill icon-spin-subtle"></i> Đặt câu hỏi nghiên cứu hiện vật hoặc bấm nút bên dưới để giải mã <strong>Mật mã lõi</strong>!
            </span>
            <span v-else class="text-warning animate-flash">
              <i class="bi bi-exclamation-triangle-fill"></i> Đã hết thời gian tự học! Tiến vào Mini-Game để cưỡng bức bẻ khóa mật mã.
            </span>
          </div>
        </div>

        <div class="input-box-wrapper" v-if="gameLocked">
          <div v-if="chatSelectedImageSrc" class="chat-image-preview-bar">
            <div class="preview-thumb-container">
              <img :src="chatSelectedImageSrc" class="preview-thumb" alt="Ảnh chờ gửi" />
              <button class="btn-remove-preview" @click="clearSelectedChatImage">
                <i class="bi bi-x-circle-fill"></i>
              </button>
            </div>
            <span class="preview-status-text"><i class="bi bi-image-fill"></i> Ảnh hiện vật đã sẵn sàng được phân tích...</span>
          </div>

          <div class="input-box">
            <input type="file" ref="chatFileInput" @change="onChatImageSelected" accept="image/*" class="file-hidden" />
            
            <button class="chat-photo-btn" @click="$refs.chatFileInput.click()" :disabled="currentStageCleared" title="Chọn ảnh hiện vật">
              <i class="bi bi-camera-fill"></i>
            </button>

            <input 
              v-model="userInput" 
              class="chat-input" 
              :placeholder="currentStageCleared ? 'Trạm đã mở, nhấn nút [Đi Tới Chặng Tiếp Theo]...' : `Hỏi về hiện vật: ${currentArtifactName}...`" 
              :disabled="currentStageCleared"
              @keyup.enter="handleChatSubmit" 
            />
            
            <button class="send-btn" @click="handleChatSubmit" :disabled="loading || currentStageCleared">
              <i class="bi bi-send-fill"></i>
            </button>
          </div>
        </div>

      </div>
    </div>
    <div class="graph-panel">
      <h2 class="panel-title"><i class="bi bi-gpu-card"></i> Hệ Thống Định Vị Di Sản</h2>
      <p class="panel-subtitle">Hệ thống định vị thông minh đang thiết lập một chuỗi liên kết logic đi qua toàn bộ 15 trạm di sản trong bảo tàng. Đường nét đứt màu xanh neon chính là Lộ trình Hamilton cố định mà bạn cần bám sát để giải mã nối tiếp nhau. Trạm số màu đỏ rực chính là điểm nóng hiện tại — trạm mà bạn đang trực tiếp đối đầu và cần phá đảo để khai thông năng lượng cho toàn hệ thống!</p>

      <div class="svg-viewport">
        <svg viewBox="0 0 460 360">
          <line 
            v-for="(edge, idx) in staticEdges" 
            :key="'e-'+idx"
            :x1="getArtifactNode(edge[0]).x" 
            :y1="getArtifactNode(edge[0]).y"
            :x2="getArtifactNode(edge[1]).x" 
            :y2="getArtifactNode(edge[1]).y"
            :class="['edge-line', { 'edge-locked-path': isEdgeInLockedPath(edge[0], edge[1]) }]"
          />

          <g v-for="(node, id) in graphNodes" :key="'n-'+id">
            <circle 
              :cx="node.x" 
              :cy="node.y" 
              :r="14" 
              :class="['node-circle', getNodeStatusClass(id)]"
            />
            <text :x="node.x" :y="node.y + 4" text-anchor="middle" class="node-lbl">
              {{ id }}
            </text>
          </g>
        </svg>
      </div>
    </div>
  </div>

<div class="route-stations-list" v-if="gameLocked && lockedHamiltonPath.length > 0">
  <div class="route-list-title">
    <i class="bi bi-signpost-split-fill"></i> DANH SÁCH LỘ TRÌNH TRẠM PHẢI QUA:
  </div>
  <div class="stations-wrapper route-grid-layout custom-scrollbar">
    <div 
      v-for="(nodeId, idx) in lockedHamiltonPath" 
      :key="'route-st-' + idx" 
      :class="['station-item', { 'is-current-standing': currentArtifactId === parseInt(nodeId) }]"
    >
      <span class="station-index">{{ idx + 1 }}</span>
      <span class="station-name">{{ graphNodes[nodeId].name }}</span>
      <span class="station-badge" v-if="currentArtifactId === parseInt(nodeId)">ĐANG ĐỨNG</span>
    </div>
  </div>
</div>
<transition name="cyber-drop">
  <div :class="['cyber-actions-overlay', { 'is-minimized': isMinimized }]" v-if="gameLocked">
    <div class="cyber-overlay-backdrop" @click="isMinimized = true"></div>
    
    <div class="action-buttons-card">
      <div class="card-glow-line"></div>
      
      <div class="overlay-status-title">
        <i class="bi bi-cpu-fill"></i> 
        <span>{{ isMinimized ? 'HỆ THỐNG MINI' : 'HỆ THỐNG ĐIỀU KHIỂN TIẾN TRÌNH' }}</span>
        
        <button class="btn-minimize-toggle" @click="isMinimized = !isMinimized" :title="isMinimized ? 'Mở rộng bảng' : 'Thu nhỏ bảng'">
          <i :class="['bi', isMinimized ? 'bi-fullscreen' : 'bi-dash-lg']"></i>
        </button>
      </div>
      
<div class="action-buttons" v-show="!isMinimized">
  <button class="btn-cyber btn-puzzle" @click="goToPuzzleGame" :disabled="currentStageCleared">
    <i class="bi bi-controller"></i> 
    <span>{{ currentStageCleared ? 'Đã Giải Mã Trạm' : 'Tiến Vào Trận Khóa Puzzle' }}</span>
  </button>
  
  <button v-if="clearedStationsCount >= 0" class="btn-cyber btn-finish-early" @click="triggerEarlyFinish">
    <span>Kết Thúc Hành Trình Sớm</span> 
    <i class="bi bi-check2-circle"></i>
  </button>
  
  <button class="btn-cyber btn-next" :disabled="!currentStageCleared" @click="advanceNextStage">
    <span>Đi Tới Chặng Tiếp Theo</span> 
    <i class="bi bi-chevron-right"></i>
  </button>
</div>
    </div>
  </div>
</transition>

<transition name="cyber-modal-fade">
  <div class="summary-overlay" v-if="showSummaryModal">
    <div class="summary-backdrop" @click="closeSummary"></div>
    
    <div class="summary-card">
      <div class="summary-glow-border"></div>
      <div class="summary-scanner"></div>
      <div class="corner-light tl"></div>
      <div class="corner-light tr"></div>
      <div class="corner-light bl"></div>
      <div class="corner-light br"></div>

      <div class="summary-header">
        <div class="summary-icon-box">
          <i class="bi bi-trophy-fill"></i>
        </div>
        <h2>BẢNG VÀNG TỔNG KẾT</h2>
        <p class="summary-subtitle">HÀNH TRÌNH DI SẢN QUÂN KHU 9</p>
      </div>

      <div class="summary-body">
        <div class="hero-title-showcase">
          <span class="title-label">DANH HIỆU ĐẠT ĐƯỢC</span>
          <h3 class="title-value">{{ getHeroTitle() }}</h3>
        </div>

        <div class="stats-grid">
          <div class="stat-box animate-box-1">
            <div class="stat-icon"><i class="bi bi-geo-alt-fill"></i></div>
            <div class="stat-info">
              <span class="stat-label">Số trạm đã qua</span>
              <span class="stat-number text-cyan">{{ clearedStationsCount }} <small>/15</small></span>
            </div>
          </div>

          <div class="stat-box animate-box-2">
            <div class="stat-icon"><i class="bi bi-star-fill"></i></div>
            <div class="stat-info">
              <span class="stat-label">Tổng điểm số</span>
              <span class="stat-number text-gold">{{ totalScore }}</span>
            </div>
          </div>

          <div class="stat-box animate-box-3">
            <div class="stat-icon"><i class="bi bi-hourglass-split"></i></div>
            <div class="stat-info">
              <span class="stat-label">Tổng thời gian</span>
              <span class="stat-number text-pink">{{ formatTotalTime(totalElapsedTime) }}</span>
            </div>
          </div>
        </div>

        <div class="summary-message">
          <i class="bi bi-terminal-box"></i>
          <span>Hệ thống ghi nhận: Bạn đã hoàn thành xuất sắc công tác số hóa và bẻ khóa cơ sở dữ liệu lịch sử bảo tàng!</span>
        </div>
      </div>

      <div class="summary-footer">
        <button class="btn-summary-action btn-replay" @click="restartNewAdventure">
          <i class="bi bi-arrow-counterclockwise"></i> Chơi Lại Từ Đầu
        </button>
        <button class="btn-summary-action btn-close-summary" @click="closeSummary">
          <i class="bi bi-x-lg"></i> Xem Lại Bản Đồ
        </button>
      </div>
    </div>
  </div>
</transition>

<div class="history-log-overlay" v-if="showHistoryLog && gameLocked">
          <div class="history-log-card">
            <h3><i class="bi bi-shield-shaded"></i> NHẬT KÝ KHÁM PHÁ DI SẢN</h3>
            <ul class="custom-scrollbar">
              <li v-for="(nodeId, idx) in lockedHamiltonPath" :key="idx" :class="{ 'completed': isStationCleared(nodeId) }">
                <span class="step-num">#{{ idx + 1 }}</span>
                <span class="step-name">{{ graphNodes[nodeId].name }}</span>
                <span class="step-status">
                  <i :class="isStationCleared(nodeId) ? 'bi bi-check-circle-fill' : 'bi bi-lock-fill'"></i>
                  {{ isStationCleared(nodeId) ? 'Đã giải mã (' + getClearTime(nodeId) + 's)' : 'Chưa tới' }}
                </span>
              </li>
            </ul>
            <button class="btn-close-log" @click="showHistoryLog = false">
              <i class="bi bi-x-lg"></i> Đóng nhật ký hành trình
            </button>
          </div>
</div>


<div class="rank-rules-overlay" v-if="showRankRules">
  <div class="rank-rules-card">
    
    <div class="cyber-glow-core"></div>

    <div class="rules-header">
      <i class="bi bi-shield-shaded"></i>
      <h3>QUY CHẾ PHONG TẶNG CẤP BẬC</h3>
      <p class="panel-subtitle">Hệ thống tự động quét và đánh giá cấp bậc dựa trên 3 chỉ số: Số trạm giải mã, Tổng điểm tích lũy và Thời gian thực thi.</p>
    </div>

    <div class="rules-list">
      
      <div class="rule-item rank-1">
        <div class="rule-rank-name">
          <i class="bi bi-person-plus-fill" style="color: #64748b; margin-right: 6px;"></i> Tân Binh Nhập Ngũ
        </div>
        <div class="rule-cond">Trạng thái khởi tạo khi chưa giải mã thành công trạm nào</div>
      </div>

      <div class="rule-item rank-2">
        <div class="rule-rank-name">
          <i class="bi bi-compass-fill" style="color: #00f0ff; margin-right: 6px;"></i> Chiến Sĩ Trinh Sát
        </div>
        <div class="rule-cond">Đạt từ <strong>1 - 4 Trạm</strong> bất kỳ trên Sơ đồ Hamilton</div>
      </div>

      <div class="rule-item rank-3">
        <div class="rule-rank-name">
          <i class="bi bi-layer-forward" style="color: #a014ff; margin-right: 6px;"></i> Sĩ Quan Tham Mưu
        </div>
        <div class="rule-cond">Đạt từ <strong>5 - 9 Trạm</strong> hoặc Tổng điểm <strong>≥ 400 điểm</strong></div>
      </div>

      <div class="rule-item rank-4">
        <div class="rule-rank-name">
          <i class="bi bi-lightning-charge-fill" style="color: #ffcc00; margin-right: 6px;"></i> Huyền Thoại Tốc Biến / Đại Sứ Di Sản
        </div>
        <div class="rule-cond">Đạt <strong>≥ 10 Trạm</strong> (Tặng "Tốc Biến" nếu tổng thời gian <strong>&lt; 10 phút</strong>)</div>
      </div>

      <div class="rule-item rank-5">
        <div class="rule-rank-name">
          <i class="bi bi-award-fill" style="color: #ff0055; margin-right: 6px;"></i> Anh Hùng Di Sản Toàn Lộ Trình
        </div>
        <div class="rule-cond">Đạt tối đa <strong>15/15 Trạm</strong> + Tổng điểm <strong>≥ 1200 điểm</strong></div>
      </div>

    </div>

    <button class="btn-close-rules" @click="showRankRules = false">
      ĐÃ HIỂU ĐIỀU KIỆN
    </button>

  </div>
</div>

</div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'MuseumAdventure',
  data() {
    return {
      showRankRules: false,
      chatSelectedFile: null,
      chatSelectedImageSrc: null, 
      showSummaryModal: false,   
      totalScore: 0,           
      totalElapsedTime: 0,      
      uploading: false,
      loading: false,
      gameLocked: false,
      timeOutReached: false,
      timeLeft: 300, 
      timerInterval: null,
      userInput: '',
      currentArtifactId: null,
      currentArtifactName: '',
      currentPathIndex: 0,
      currentStageCleared: false, 
      lockedHamiltonPath: [], 
      isMinimized: false,
      messages: [
        { role: 'bot', text: 'Chào mừng bạn đến với hệ thống thử thách. Vui lòng tải ảnh hiện vật lên để kích hoạt định vị điểm khởi hành và lập lộ trình!', suggestions: [] }
      ],
      cryptoKeysPool: {
        0: "SAM7", 1: "BOM9", 2: "GHE2", 3: "LUH3", 4: "MAY4",
        5: "TOL5", 6: "IND6", 7: "NEO7", 8: "PHA8", 9: "SUN9",
        10: "PCF1", 11: "B522", 12: "XEB3", 13: "PEU4", 14: "XET5"
      },
      graphNodes: {
        0:  { name: "Bệ và đạn tên lửa SAM", slug: "bevadantenlua", x: 319, y: 39 },
        1:  { name: "Bom",                  slug: "bom",            x: 382, y: 108 },
        2:  { name: "Ghe xuồng thuyền",     slug: "ghexuongthuyen", x: 382, y: 221 },
        3:  { name: "Lu hầm bí mật",        slug: "luhambimat",    x: 304, y: 270 },
        4:  { name: "Máy bay trực thăng",   slug: "maybaytruckhang",x: 221, y: 304 },
        5:  { name: "Máy cán Tol",          slug: "maycantol",     x: 137, y: 270 },
        6:  { name: "Máy in",               slug: "mayinpedal",    x: 59,  y: 221 },
        7:  { name: "Mỏ neo tàu",           slug: "moneotau",      x: 123, y: 147 },
        8:  { name: "Pháo",                 slug: "phao",          x: 59,  y: 108 },
        9:  { name: "Súng thần công",       slug: "sungthancong",  x: 172, y: 157 },
        10: { name: "Tàu tuần tiều PCF",    slug: "tautuantieupcf", x: 221, y: 29 },
        11: { name: "Trục máy bay B52",     slug: "trucmaybayb52", x: 221, y: 88 },
        12: { name: "Xe bọc thép",          slug: "xebocthep",     x: 270, y: 157 },
        13: { name: "Xe Peugeot",           slug: "xepeugeot",     x: 319, y: 147 },
        14: { name: "Xe tăng",              slug: "xetang",        x: 123, y: 39 }
      },
      staticEdges: [
        [0, 11], [0, 14], [1, 8], [1, 11], [1, 12], [2, 3], [2, 12], [3, 7], [3, 13],
        [4, 5], [4, 8], [4, 9], [5, 6], [5, 9], [6, 13], [6, 14], [7, 10], [7, 11],
        [7, 14], [8, 11], [9, 12], [9, 13], [10, 12], [10, 13], [13, 14]
      ],

      showHistoryLog: false,      
      stationHistoryLog: {},      
      isSpeaking: false,          
      activeSpeechText: '',        
      speechUtterance: null,      
      secondsElapsedInStage: 0,    
      performanceInterval: null,

      chatSelectedFile: null,
      chatSelectedImageSrc: null,   
    };
  },
  mounted() {
    this.restoreGameState();
    this.checkAutoVerifyCrypto();
  },
  beforeDestroy() {
    if (this.performanceInterval) clearInterval(this.performanceInterval);
    if (window.speechSynthesis) window.speechSynthesis.cancel();
  },
  computed: {
    clearedStationsCount() {
      return Object.keys(this.stationHistoryLog).length;
    }
  },
  methods: {
    showRulesModal() {
      this.showRankRules = true;
    },
    getArtifactNode(id) {
      return this.graphNodes[id] || { x: 0, y: 0 };
    },
    
    checkAutoVerifyCrypto() {
      const decryptedKey = localStorage.getItem('latest_decrypted_key');
      if (decryptedKey && this.gameLocked && !this.currentStageCleared) {
        this.userInput = decryptedKey;
        setTimeout(() => {
          this.sendChatMessage();
          localStorage.removeItem('latest_decrypted_key');
        }, 800);
      }
    },

    async handleImageUpload(event) {
      const file = event.target.files[0];
      if (!file) return;

      const formData = new FormData();
      formData.append('file', file);
      this.uploading = true;

      try {
        const res = await axios.post('http://localhost:8001/predict', formData);
        if (res.data && res.data.results && res.data.results.length > 0) {
          const detectedId = parseInt(res.data.results[0].cnn_id);
          this.initializeGamePath(detectedId);
        } else {
          alert('Không tìm thấy hiện vật đặc trưng trong ảnh, vui lòng thử lại!');
        }
      } catch (err) {
        console.error('Lỗi phân tích nhận diện ảnh:', err);
        alert('Kết nối máy chủ định vị thất bại.');
      } finally {
        this.uploading = false;
      }
    },

    onChatImageSelected(event) {
      const file = event.target.files[0];
      if (!file) return;
      this.chatSelectedFile = file;
      this.chatSelectedImageSrc = URL.createObjectURL(file);
    },

    clearSelectedChatImage() {
      this.chatSelectedFile = null;
      this.chatSelectedImageSrc = null;
      if (this.$refs.chatFileInput) this.$refs.chatFileInput.value = '';
    },

    handleChatSubmit() {
      if (this.chatSelectedFile) {
        this.submitChatWithImage();
      } else {
        this.sendChatMessage();
      }
    },

    async submitChatWithImage() {
      if (!this.chatSelectedFile || this.loading) return;

      const file = this.chatSelectedFile;
      const imgUrl = this.chatSelectedImageSrc;
      const optionalText = this.userInput.trim();

      this.clearSelectedChatImage();
      this.userInput = '';

      this.messages.push({ 
        role: 'user', 
        text: optionalText ? optionalText : '<i>[Đã gửi một hình ảnh hiện vật]</i>', 
        image: imgUrl 
      });
      
      this.loading = true;
      this.scrollToBottom();

      const formData = new FormData();
      formData.append('file', file);

      try {
        const res = await axios.post('http://localhost:8001/predict', formData);
        if (res.data && res.data.results && res.data.results.length > 0) {
          const detectedId = parseInt(res.data.results[0].cnn_id);
          const detectedName = this.graphNodes[detectedId]?.name || "Hiện vật chưa xác định";

          let autoChatMessage = `Tôi vừa gửi ảnh chụp của hiện vật: "${detectedName}". Hãy thuyết minh chi tiết cho tôi về lịch sử và ý nghĩa của hiện vật này.`;
          if (optionalText) {
            autoChatMessage += ` Ngoài ra, tôi muốn hỏi thêm: ${optionalText}`;
          }
          
          const ragRes = await axios.post('http://localhost:8000/chat', {
            message: autoChatMessage,
            label: this.currentArtifactId
          });

          const ans = ragRes.data.responses?.[0]?.answer || 'Đã nhận diện thành công ảnh nhưng hệ thống tư liệu chưa kịp phản hồi.';
          const sug = ragRes.data.responses?.[0]?.suggestions || [];
          
          this.messages.push({ role: 'bot', text: `<strong>Hệ thống nhận diện ảnh:</strong> Đây là <strong>${detectedName}</strong>.<br><br>${ans}`, suggestions: sug });
        } else {
          this.messages.push({ role: 'bot', text: 'Hệ thống không nhận dạng được hiện vật cụ thể nào trong bức ảnh bạn vừa gửi. Vui lòng chụp rõ góc cạnh hiện vật hơn.', suggestions: [] });
        }
      } catch (err) {
        console.error(err);
        this.messages.push({ role: 'bot', text: 'Gặp sự cố kết nối trong quá trình phân tích ảnh hiện vật.', suggestions: [] });
      } finally {
        this.loading = false;
        this.scrollToBottom();
        this.saveGameState();
      }
    },

    initializeGamePath(startNodeId) {
      const graph = {};
      Object.keys(this.graphNodes).forEach(id => graph[id] = []);
      this.staticEdges.forEach(([u, v]) => {
        graph[u].push(v);
        graph[v].push(u);
      });

      const path = [startNodeId];
      const visited = new Set([startNodeId]);

      const findHamilton = (current) => {
        if (path.length === Object.keys(this.graphNodes).length) return true;
        
        for (const neighbor of graph[current]) {
          if (!visited.has(neighbor)) {
            visited.add(neighbor);
            path.push(neighbor);
            if (findHamilton(neighbor)) return true;
            path.pop();
            visited.delete(neighbor);
          }
        }
        return false;
      };

      if (findHamilton(startNodeId)) {
        this.lockedHamiltonPath = [...path];
        localStorage.setItem('museum_generated_route', JSON.stringify(this.lockedHamiltonPath));
        this.gameLocked = true;
        this.currentPathIndex = 0;
        this.setupCurrentStage(this.lockedHamiltonPath[0]);
        this.startStageTimer();
        this.saveGameState();
      } else {
        this.generateBackupDFSPath(startNodeId);
      }
    },

    generateBackupDFSPath(startId) {
      const path = [];
      const visited = new Set();
      const graph = {};
      Object.keys(this.graphNodes).forEach(id => graph[id] = []);
      this.staticEdges.forEach(([u, v]) => { graph[u].push(v); graph[v].push(u); });

      const dfs = (node) => {
        visited.add(node);
        path.push(node);
        for (const n of graph[node]) {
          if (!visited.has(n)) dfs(n);
        }
      };
      dfs(startId);
      
      Object.keys(this.graphNodes).forEach(id => {
        if (!visited.has(parseInt(id))) path.push(parseInt(id));
      });

      this.lockedHamiltonPath = path.map(Number);
      localStorage.setItem('museum_generated_route', JSON.stringify(this.lockedHamiltonPath));
      this.gameLocked = true;
      this.currentPathIndex = 0;
      this.setupCurrentStage(this.lockedHamiltonPath[0]);
      this.startStageTimer();
      this.saveGameState();
    },

    setupCurrentStage(artifactId) {
      this.currentArtifactId = artifactId;
      this.currentArtifactName = this.graphNodes[artifactId].name;
      this.currentStageCleared = false;
      this.timeOutReached = false;
      
      this.messages.push({
        role: 'bot',
        text: `Định vị thành công. Chặng hiện tại của bạn là: <strong>[${this.currentArtifactName}]</strong>. Hãy đặt câu hỏi tìm hiểu lịch sử, lựa chọn đính kèm ảnh hiện vật hoặc tham gia trò chơi để tìm mã mở khoá!`,
        suggestions: []
      });
      this.triggerRAGAutoGreeting();
      this.startPerformanceTracker(); 
    },

    async triggerRAGAutoGreeting() {
      this.loading = true;
      let contextPrompt = `Giới thiệu về hiện vật bảo tàng: ${this.currentArtifactName}`;
      if (this.currentPathIndex > 0) {
        const previousArtifactId = this.lockedHamiltonPath[this.currentPathIndex - 1];
        const previousName = this.graphNodes[previousArtifactId].name;
        contextPrompt += `. Kết nối lộ trình tiếp nối từ chặng trước đó có tên là: "${previousName}" để dẫn dắt một cách mượt mà cho học viên.`;
      }

      try {
        const res = await axios.post('http://localhost:8000/chat', {
          message: contextPrompt,
          label: this.currentArtifactId
        });
        const botReply = res.data.responses?.[0]?.answer || 'Chào mừng bạn đến chặng di sản này.';
        const sugList = res.data.responses?.[0]?.suggestions || [];
        this.messages.push({ role: 'bot', text: botReply, suggestions: sugList });
      } catch (err) {
        console.error(err);
      } finally {
        this.loading = false;
        this.scrollToBottom();
      }
    },

    async sendChatMessage() {
      if (!this.userInput.trim() || this.loading) return;
      const msg = this.userInput.trim();
      this.userInput = '';
      
      this.messages.push({ role: 'user', text: msg });
      this.loading = true;
      this.scrollToBottom();

      const correctKey = this.cryptoKeysPool[this.currentArtifactId];
      if (msg.toUpperCase() === correctKey || msg.toUpperCase().includes(correctKey)) {
        this.currentStageCleared = true;
        if (this.timerInterval) clearInterval(this.timerInterval);
        if (this.performanceInterval) clearInterval(this.performanceInterval); 

        this.$set(this.stationHistoryLog, this.currentArtifactId, {
          timeSpent: this.secondsElapsedInStage,
          clearedAt: new Date().toLocaleTimeString()
        });

        this.messages.push({
          role: 'bot',
          text: `<strong>XÁC THỰC THÀNH CÔNG!</strong> Mật mã lõi <code>${correctKey}</code> chính xác. Bạn đã mất đúng <strong>${this.secondsElapsedInStage} giây</strong> nghiên cứu tại trạm này. Dữ liệu của trạm <strong>[${this.currentArtifactName}]</strong> đã được đồng bộ hóa hoàn toàn.`,
          suggestions: []
        });
        
        if (this.currentPathIndex >= this.lockedHamiltonPath.length - 1) {
          setTimeout(() => {
            alert('Chúc mừng! Bạn đã hoàn thành toàn bộ lộ trình di sản 15 điểm của Quân khu 9!');
            this.triggerEarlyFinish();
          }, 1000);
          this.loading = false;
          return;
        }

        this.loading = false;
        this.scrollToBottom();
        this.saveGameState();
        return; 
      }

      try {
        const res = await axios.post('http://localhost:8000/chat', {
          message: msg,
          label: this.currentArtifactId
        });
        const ans = res.data.responses?.[0]?.answer || 'Bộ não AI đang bảo trì.';
        const sug = res.data.responses?.[0]?.suggestions || [];
        this.messages.push({ role: 'bot', text: ans, suggestions: sug });
      } catch (err) {
        this.messages.push({ role: 'bot', text: 'Lỗi đồng bộ dữ liệu bảo tàng.' });
      } finally {
        this.loading = false;
        this.scrollToBottom();
        this.saveGameState();
      }
    },

    selectSuggestion(suggestionText) {
      this.userInput = suggestionText;
      this.handleChatSubmit();
    },

    startStageTimer() {
      if (this.timerInterval) clearInterval(this.timerInterval);
      this.timeLeft = 300; 
      this.timerInterval = setInterval(() => {
        this.timeLeft--;
        if (this.timeLeft <= 0) {
          clearInterval(this.timerInterval);
          this.timeOutReached = true;
        }
      }, 1000);
    },

    startPerformanceTracker() {
      if (this.performanceInterval) clearInterval(this.performanceInterval);
      this.secondsElapsedInStage = 0;
      this.performanceInterval = setInterval(() => {
        this.secondsElapsedInStage++;
      }, 1000);
    },

    speakText(rawHtmlText) {
      if (!window.speechSynthesis) {
        alert("Trình duyệt không hỗ trợ tính năng thuyết minh âm thanh.");
        return;
      }

      if (this.isSpeaking && this.activeSpeechText === rawHtmlText) {
        window.speechSynthesis.cancel();
        this.isSpeaking = false;
        this.activeSpeechText = '';
        return;
      }

      window.speechSynthesis.cancel(); 
      
      const tempDiv = document.createElement("div");
      tempDiv.innerHTML = rawHtmlText;
      const cleanText = tempDiv.textContent || tempDiv.innerText || "";

      this.speechUtterance = new SpeechSynthesisUtterance(cleanText);
      this.speechUtterance.lang = 'vi-VN'; 
      this.activeSpeechText = rawHtmlText;
      this.isSpeaking = true;

      this.speechUtterance.onend = () => {
        this.isSpeaking = false;
        this.activeSpeechText = '';
      };

      this.speechUtterance.onerror = () => {
        this.isSpeaking = false;
      };

      window.speechSynthesis.speak(this.speechUtterance);
    },

    getHeroTitle() {
      const count = this.clearedStationsCount || 0;
      const score = this.totalScore || 0;
      const time = this.totalElapsedTime || 0;
      if (count >= 15 && score >= 1200) {
        return "Anh Hùng Di Sản Toàn Lộ Trình";
      } 
      if (count >= 10 || score >= 800) {
        return (time > 0 && time < 600) ? "Huyền Thoại Tốc Biến Di Sản" : "Đại Sứ Di Sản Quân Khu";
      } 
      if (count >= 5 || score >= 400) {
        return "Sĩ Quan Tham Mưu";
      } 
      if (count > 0) {
        return "Chiến Sĩ Trinh Sát";
      } 
      return "Tân Binh Nhập Ngũ";
    },

    isStationCleared(nodeId) {
      return !!this.stationHistoryLog[nodeId];
    },

    getClearTime(nodeId) {
      return this.stationHistoryLog[nodeId] ? this.stationHistoryLog[nodeId].timeSpent : 0;
    },

    formatRichText(text) {
      if (!text) return '';
      let formatted = text;
      if (formatted.includes('\n- ')) {
        formatted = formatted.replace(/\n-\s(.+)/g, '<li>$1</li>');
      }
      formatted = formatted.replace(/(năm \d{4}|ngày \d{1,2}\/\d{1,2}\/\d{4})/gi, '<strong class="history-highlight">$1</strong>');
      return formatted;
    },

    goToPuzzleGame() {
      const slug = this.graphNodes[this.currentArtifactId].slug;
      this.saveGameState();
      this.$router.push(`/puzzle-${slug}`);
    },

    triggerEarlyFinish() {
      let accumulatedScore = 0;
      let accumulatedTime = 0;

      // 1. Đọc file lưu trữ tổng từ LocalStorage để xem các màn khác đã chơi ra sao
      const savedState = localStorage.getItem('museum_game_state_save');
      
      if (savedState) {
        const parsed = JSON.parse(savedState);
        // Lấy lịch sử các trạm đã lưu (nếu không có thì mặc định là rỗng)
        const history = parsed.stationHistoryLog || {};
        const lockedPath = parsed.lockedHamiltonPath || [];

        // Duyệt qua chuỗi lộ trình 15 điểm
        lockedPath.forEach(nodeId => {
          if (history[nodeId]) {
            // Lấy điểm số thực tế kiếm được từ các trang chơi puzzle (scoreSpent)
            accumulatedScore += parseInt(history[nodeId].scoreSpent) || 0;
            // Lấy thời gian thực tế đã chơi của màn đó (timeSpent)
            accumulatedTime += parseInt(history[nodeId].timeSpent) || 0;
          }
        });

        // 2. KIỂM TRA RIÊNG MÀN HIỆN TẠI (Nếu người chơi bấm Kết thúc sớm khi đang ở trang này mà chưa bấm nút "Tiếp tục hành trình")
        if (!history[this.currentArtifactId]) {
          // Cộng nốt điểm số và thời gian đang hiển thị trên màn hình hiện tại của trang này vào tổng kết
          accumulatedScore += (parseInt(this.score) || 0);
          accumulatedTime += (parseInt(this.elapsedTime) || 0);
        }
      } else {
        // Nếu LocalStorage trống hoàn toàn, lấy luôn điểm/thời gian của màn hiện tại
        accumulatedScore = parseInt(this.score) || 0;
        accumulatedTime = parseInt(this.elapsedTime) || 0;
      }

      // 3. Gán giá trị tính toán được vào các biến dữ liệu thông qua từ khóa "this"
      this.totalScore = accumulatedScore;
      this.totalElapsedTime = accumulatedTime;

      // 4. Mở modal tổng kết lên
      this.showSummaryModal = true;
      
      // 5. Dọn dẹp các bộ đếm thời gian
      if (this.timerInterval) clearInterval(this.timerInterval);
      if (this.performanceInterval) clearInterval(this.performanceInterval);
      if (this.timer) clearInterval(this.timer); // Phòng hờ biến timer ở các trang puzzle
    },

    formatTotalTime(seconds) {
      if (!seconds || seconds <= 0) return "00p 00s";
      const m = Math.floor(seconds / 60).toString().padStart(2, '0');
      const s = (seconds % 60).toString().padStart(2, '0');
      return `${m}p ${s}s`;
    },
    closeSummary() {
      this.showSummaryModal = false;
    },
    restartNewAdventure() {
      this.showSummaryModal = false;
      this.resetAdventure(); 
    },
    advanceNextStage() {
      if (this.currentPathIndex >= this.lockedHamiltonPath.length - 1) {
        alert('Xuất sắc! Bạn đã vượt qua toàn bộ lộ trình 15 điểm di sản Quân khu 9!');
        this.triggerEarlyFinish();
        return;
      }
      this.currentPathIndex++;
      const nextId = this.lockedHamiltonPath[this.currentPathIndex];
      this.setupCurrentStage(nextId);
      this.startStageTimer();
      this.saveGameState();
    },

    getNodeStatusClass(id) {
      const numericId = parseInt(id);
      if (!this.gameLocked) return 'node-default';
      if (this.currentArtifactId === numericId) return 'node-current';
      
      const pathIdx = this.lockedHamiltonPath.indexOf(numericId);
      if (pathIdx !== -1 && pathIdx < this.currentPathIndex) return 'node-active';
      if (pathIdx !== -1 && pathIdx === this.currentPathIndex + 1 && this.currentStageCleared) return 'node-next-ready';
      
      return 'node-locked';
    },

    isEdgeInLockedPath(u, v) {
      if (this.lockedHamiltonPath.length === 0) return false;
      const idxU = this.lockedHamiltonPath.indexOf(u);
      const idxV = this.lockedHamiltonPath.indexOf(v);
      return (idxU !== -1 && idxV !== -1 && Math.abs(idxU - idxV) === 1);
    },

    formatTime(seconds) {
      const m = Math.floor(seconds / 60);
      const s = seconds % 60;
      return `${m}:${s.toString().padStart(2, '0')}`;
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const el = this.$refs.chatBox;
        if (el) el.scrollTop = el.scrollHeight;
      });
    },

    saveGameState() {
      const state = {
        gameLocked: this.gameLocked,
        currentPathIndex: this.currentPathIndex,
        lockedHamiltonPath: this.lockedHamiltonPath,
        currentArtifactId: this.currentArtifactId,
        currentStageCleared: this.currentStageCleared,
        messages: this.messages,
        stationHistoryLog: this.stationHistoryLog 
      };
      localStorage.setItem('museum_game_state_save', JSON.stringify(state));
    },

    restoreGameState() {
      const savedRoute = localStorage.getItem('museum_generated_route');
      if (savedRoute) {
        this.lockedHamiltonPath = JSON.parse(savedRoute);
      }

      const saved = localStorage.getItem('museum_game_state_save');
      if (saved) {
        const parsed = JSON.parse(saved);
        this.gameLocked = parsed.gameLocked;
        this.currentPathIndex = parsed.currentPathIndex;
        this.lockedHamiltonPath = parsed.lockedHamiltonPath || this.lockedHamiltonPath;
        this.currentArtifactId = parsed.currentArtifactId;
        this.currentStageCleared = parsed.currentStageCleared;
        this.messages = parsed.messages;
        this.stationHistoryLog = parsed.stationHistoryLog || {}; 
        
        if (this.currentArtifactId) {
          this.currentArtifactName = this.graphNodes[this.currentArtifactId].name;
          if (!this.currentStageCleared) {
            this.startStageTimer();
            this.startPerformanceTracker(); 
          }
        }
      }
    },

    resetAdventure() {
      if (this.timerInterval) clearInterval(this.timerInterval);
      if (this.performanceInterval) clearInterval(this.performanceInterval);
      if (window.speechSynthesis) window.speechSynthesis.cancel();
      localStorage.removeItem('museum_game_state_save');
      localStorage.removeItem('museum_generated_route');
      window.location.reload();
    }
  }
};
</script>

<style scoped>
@import "../assets/css/chatbox.css";

</style>