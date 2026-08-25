<template>
  <div class="container">

    <h2>Duyệt DFS trên đồ thị 15 đỉnh</h2>

    <svg width="900" height="700">

      <!-- Edges -->
      <line
        v-for="(edge,index) in edges"
        :key="'e'+index"
        :x1="getNode(edge[0]).x"
        :y1="getNode(edge[0]).y"
        :x2="getNode(edge[1]).x"
        :y2="getNode(edge[1]).y"
        class="edge"
      />

      <!-- Nodes -->
      <g
        v-for="node in nodes"
        :key="node.id"
        @click="startDFS(node.id)"
        style="cursor:pointer"
      >
        <circle
          :cx="node.x"
          :cy="node.y"
          r="30"
          :class="visitedNodes.includes(node.id)
            ? 'node-active'
            : 'node-default'"
        />

        <text
          :x="node.x"
          :y="node.y+6"
          text-anchor="middle"
          class="node-text"
        >
          {{ node.id }}
        </text>
      </g>

    </svg>

    <div class="result">
      <strong>DFS:</strong>
      <div class="path">
        {{ dfsOrder.join(" → ") }}
      </div>
    </div>

  </div>
</template>

<script>
export default {
  data() {
    return {

      dfsOrder: [],

      visitedNodes: [],

nodes: [

  // đỉnh trung tâm
  { id: "12", x: 450, y: 180 },

  // vòng trên
  { id: "0",  x: 250, y: 80 },
  { id: "11", x: 450, y: 60 },
  { id: "1",  x: 650, y: 80 },

  // vòng giữa
  { id: "9",  x: 120, y: 220 },
  { id: "8",  x: 250, y: 300 },

  { id: "14", x: 650, y: 300 },
  { id: "2",  x: 780, y: 220 },

  // trung tâm phụ
  { id: "10", x: 350, y: 320 },
  { id: "13", x: 550, y: 320 },

  // vòng dưới
  { id: "7",  x: 120, y: 450 },
  { id: "6",  x: 280, y: 550 },

  { id: "5",  x: 450, y: 620 },

  { id: "4",  x: 620, y: 550 },
  { id: "3",  x: 780, y: 450 }

],

edges: [

  // ===== CẠNH GỐC =====
  ["9","8"],
  ["5","9"],

  ["5","2"],
  ["8","14"],

  ["12","14"],
  ["2","12"],

  ["14","7"],

  ["2","6"],
  ["4","3"],

  ["1","8"],
  ["10","12"],

  ["0","11"],
  ["1","11"],

  ["6","13"],

  ["10","13"],
  ["7","10"],
  ["12","3"],

  // ===== THÊM MỚI =====

  ["0","1"],
  ["4","13"],

  ["3","10"],
  ["6","7"],

  ["11","8"],
  ["9","2"],

  ["13","14"],

  ["5","6"],

  ["1","12"],
["0","12"],
["4","14"],
["3","7"],
["5","10"],
["8","11"]
]

    };
  },

  methods: {
    distance(a, b) {
    const n1 = this.getNode(a);
    const n2 = this.getNode(b);

    return Math.sqrt(
        Math.pow(n1.x - n2.x, 2) +
        Math.pow(n1.y - n2.y, 2)
    );
    },

    getNode(id) {
      return this.nodes.find(n => n.id === id);
    },

    buildGraph() {

      const graph = {};

      this.nodes.forEach(node => {
        graph[node.id] = [];
      });

      this.edges.forEach(([u,v]) => {
        graph[u].push(v);
        graph[v].push(u);
      });

      return graph;
    },

    sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    },

async startDFS(startNode) {

  this.dfsOrder = [];
  this.visitedNodes = [];

  const graph = this.buildGraph();

  const visited = new Set([startNode]);

  const path = [startNode];

  const hamilton = async (current) => {

    this.visitedNodes = [...path];

    await this.sleep(300);

    // Đã đi qua tất cả đỉnh
    if (path.length === this.nodes.length) {
      return true;
    }

    for (const next of graph[current]) {

      if (!visited.has(next)) {

        visited.add(next);
        path.push(next);

        if (await hamilton(next)) {
          return true;
        }

        // Backtrack
        path.pop();
        visited.delete(next);

        this.visitedNodes = [...path];
      }
    }

    return false;
  };

  const found = await hamilton(startNode);

  if (found) {

    this.dfsOrder = [...path];

    this.visitedNodes = [...path];

  } else {

    alert(
      `Không tồn tại Hamilton Path bắt đầu từ đỉnh ${startNode}`
    );

    this.dfsOrder = [];
    this.visitedNodes = [];
  }
}

  }

};
</script>

<style scoped>

.container{
  text-align:center;
  font-family:Arial;
  padding:20px;
}

svg{
  border:1px solid #ddd;
  background:white;
}

.edge{
  stroke:#555;
  stroke-width:3;
}

.node-default{
  fill:white;
  stroke:black;
  stroke-width:3;
  transition:.3s;
}

.node-active{
  fill:#4caf50;
  stroke:#2e7d32;
  stroke-width:3;
  transition:.3s;
}

.node-text{
  font-size:18px;
  font-weight:bold;
  user-select:none;
}

.result{
  margin-top:20px;
}

.path{
  margin-top:10px;
  font-size:24px;
  color:#d32f2f;
  font-weight:bold;
}

</style>