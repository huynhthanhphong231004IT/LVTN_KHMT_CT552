import axios from 'axios';

export default {
  name: 'MuseumAdventure',
  data() {
    return {
      uploading: false,
      loading: false,
      gameLocked: false,
      timeOutReached: false,
      timeLeft: 300, // 5 phút đếm ngược cho tương tác học tập kiến thức
      timerInterval: null,
      userInput: '',
      currentArtifactId: null,
      currentArtifactName: '',
      currentPathIndex: 0,
      currentStageCleared: false, // Sẽ chuyển thành true khi chơi xong puzzle quay lại
      lockedHamiltonPath: [], // Mảng lưu lộ trình đi qua toàn bộ 15 đỉnh duy nhất
      messages: [
        { role: 'bot', text: 'Chào mừng bạn đến với hệ thống thử thách. Vui lòng tải ảnh hiện vật lên để kích hoạt định vị điểm khởi hành và lập lộ trình!', suggestions: [] }
      ],
      // Hệ thống tọa độ định vị chính xác 15 đỉnh cố định
      graphNodes: {
        0:  { name: "Bệ và đạn tên lửa SAM", slug: "bevadantenlua", x: 319, y: 39 },
        1:  { name: "Bom",                  slug: "bom",           x: 382, y: 108 },
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
      // Cấu trúc đồ thị liên kết ma trận bảo tàng Quân khu 9
      staticEdges: [
        [0, 11], [0, 14], [1, 8], [1, 11], [1, 12], [2, 3], [2, 12], [3, 7], [3, 13],
        [4, 5], [4, 8], [4, 9], [5, 6], [5, 9], [6, 13], [6, 14], [7, 10], [7, 11],
        [7, 14], [8, 11], [9, 12], [9, 13], [10, 12], [10, 13], [13, 14]
      ]
    };
  },
  mounted() {
    this.restoreGameState();
  },
  methods: {
    getArtifactNode(id) {
      return this.graphNodes[id] || { x: 0, y: 0 };
    },
    
    // --- 1. TIẾP NHẬN ẢNH ĐỊNH VỊ TỪ API 8001 ---
    async handleImageUpload(event) {
      const file = event.target.files[0];
      if (!file) return;

      const formData = new FormData();
      formData.append('file', file);
      this.uploading = true;

      try {
        const res = await axios.post('http://localhost:8001/predict', formData);
        if (res.data && res.data.results && res.data.results.length > 0) {
          // Trích xuất cnn_id của đối tượng nhận diện đầu tiên được tìm thấy làm vị trí xuất phát
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

    // --- 2. THUẬT TOÁN TÌM ĐƯỜNG HAMILTON KHÔNG LẶP ĐỈNH ---
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
        this.gameLocked = true;
        this.currentPathIndex = 0;
        this.setupCurrentStage(this.lockedHamiltonPath[0]);
        this.startStageTimer();
        this.saveGameState();
      } else {
        // Fallback sang DFS nếu đồ thị thưa không đủ điều kiện tạo chu trình hoàn hảo qua 15 đỉnh
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
      
      // Bổ sung các đỉnh còn thiếu nếu có để đảm bảo đi qua đủ tất cả các đỉnh
      Object.keys(this.graphNodes).forEach(id => {
        if (!visited.has(parseInt(id))) path.push(parseInt(id));
      });

      this.lockedHamiltonPath = path.map(Number);
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
        text: `📍 Định vị thành công. Chặng hiện tại của bạn là: [${this.currentArtifactName}]. Hãy đặt các câu hỏi tìm hiểu lịch sử trước khi tham gia trò chơi!`,
        suggestions: []
      });
      this.triggerRAGAutoGreeting();
    },

    // --- 3. KẾT NỐI API 8000 CHAT RAG ---
    async triggerRAGAutoGreeting() {
      this.loading = true;
      try {
        const res = await axios.post('http://localhost:8000/chat', {
          message: `Giới thiệu về ${this.currentArtifactName}`,
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
      const msg = this.userInput;
      this.userInput = '';
      this.messages.push({ role: 'user', text: msg });
      this.loading = true;
      this.scrollToBottom();

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
      this.sendChatMessage();
    },

    // --- 4. BỘ ĐẾM THỜI GIAN GAME 5 PHÚT ---
    startStageTimer() {
      if (this.timerInterval) clearInterval(this.timerInterval);
      this.timeLeft = 300; // 5 phút
      this.timerInterval = setInterval(() => {
        this.timeLeft--;
        if (this.timeLeft <= 0) {
          clearInterval(this.timerInterval);
          this.timeOutReached = true;
        }
      }, 1000);
    },

    // --- 5. CHUYỂN HƯỚNG SANG GAME PUZZLE ĐÚNG ROUTER ---
    goToPuzzleGame() {
      const slug = this.graphNodes[this.currentArtifactId].slug;
      
      // Lưu lại trạng thái đánh dấu đã chơi để khi quay lại trang này người dùng có thể bấm nút "Đi tiếp"
      this.currentStageCleared = true;
      this.saveGameState();

      // Chuyển hướng sang router đã có sẵn của bạn
      this.$router.push(`/puzzle-${slug}`);
    },

    advanceNextStage() {
      if (this.currentPathIndex >= this.lockedHamiltonPath.length - 1) {
        alert('🎉 Xuất sắc! Bạn đã vượt qua toàn bộ lộ trình 15 điểm di sản Quân khu 9!');
        this.resetAdventure();
        return;
      }
      this.currentPathIndex++;
      const nextId = this.lockedHamiltonPath[this.currentPathIndex];
      this.setupCurrentStage(nextId);
      this.startStageTimer();
      this.saveGameState();
    },

    // --- SYSTEM UTILS ---
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
        messages: this.messages
      };
      localStorage.setItem('museum_game_state_save', JSON.stringify(state));
    },

    restoreGameState() {
      const saved = localStorage.getItem('museum_game_state_save');
      if (saved) {
        const parsed = JSON.parse(saved);
        this.gameLocked = parsed.gameLocked;
        this.currentPathIndex = parsed.currentPathIndex;
        this.lockedHamiltonPath = parsed.lockedHamiltonPath;
        this.currentArtifactId = parsed.currentArtifactId;
        this.currentStageCleared = parsed.currentStageCleared;
        this.messages = parsed.messages;
        
        if (this.currentArtifactId) {
          this.currentArtifactName = this.graphNodes[this.currentArtifactId].name;
          if (!this.currentStageCleared) this.startStageTimer();
        }
      }
    },

    resetAdventure() {
      if (this.timerInterval) clearInterval(this.timerInterval);
      localStorage.removeItem('museum_game_state_save');
      window.location.reload();
    }
  }
};