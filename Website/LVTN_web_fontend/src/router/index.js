import { createRouter, createWebHashHistory } from 'vue-router'

import Home from '../views/Home.vue'
import About from '../views/About.vue'
import HienVat from '../views/HienVat.vue'
import BaiViet from '../views/BaiViet.vue'
import DanhGia from '../views/DanhGia.vue'

import Chatbox from '../views/Chatbox.vue'
import Graph from '../views/Graph.vue'

import Puzzle_XeTang from '../views/Puzzle_XeTang.vue'
import Puzzle_bevadantenlua from '../views/Puzzle_bevadantenlua.vue'
import Puzzle_bom from '../views/Puzzle_bom.vue'
import Puzzle_ghexuongthuyen from '../views/Puzzle_ghexuongthuyen.vue'
import Puzzle_luhambimat from '../views/Puzzle_luhambimat.vue'
import Puzzle_maybaytructhang from '../views/Puzzle_maybaytructhang.vue'
import Puzzle_maycantol from '../views/Puzzle_maycantol.vue'
import Puzzle_mayinpedal from '../views/Puzzle_mayinpedal.vue'
import Puzzle_moneotau from '../views/Puzzle_moneotau.vue'
import Puzzle_phao from '../views/Puzzle_phao.vue'
import Puzzle_sungthancong from '../views/Puzzle_sungthancong.vue'
import Puzzle_tautuantieupcf from '../views/Puzzle_tautuantieupcf.vue'
import Puzzle_trucmaybayb52 from '../views/Puzzle_trucmaybayb52.vue'
import Puzzle_xebocthep from '../views/Puzzle_xebocthep.vue'
import Puzzle_xepeugeot from '../views/Puzzle_xepeugeot.vue'

import Game_xetang from '../views/Game/Game_XeTang.vue'


const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/about', name: 'About', component: About },
  { path: '/hienvat', name: 'HienVat', component: HienVat },
  { path: '/baiviet', name: 'BaiViet', component: BaiViet },
  { path: '/danhgia', name: 'DanhGia', component: DanhGia },
  { path: '/chatbox', name: 'Chatbox', component: Chatbox },
  { path: '/graph', name: 'Graph', component: Graph },
  
  { path: '/puzzle-xetang',name: 'PuzzleXeTang',component: Puzzle_XeTang, meta: {hideLayout: true}},
  { path: '/puzzle-bevadantenlua', name: 'Puzzlebevadantenlua', component: Puzzle_bevadantenlua },
  { path: '/puzzle-bom', name: 'Puzzlebom', component: Puzzle_bom },
  { path: '/puzzle-ghexuongthuyen', name: 'Puzzleghexuongthuyen', component: Puzzle_ghexuongthuyen },
  { path: '/puzzle-luhambimat', name: 'Puzzleluhambimat', component: Puzzle_luhambimat},
  { path: '/puzzle-maybaytructhang', name: 'Puzzlemaybaytructhang', component: Puzzle_maybaytructhang},
  { path: '/puzzle-maycantol', name: 'Puzzlemaycantol', component: Puzzle_maycantol},
  { path: '/puzzle-mayinpedal', name: 'Puzzlemayinpedal', component: Puzzle_mayinpedal},
  { path: '/puzzle-moneotau', name: 'Puzzlemoneotau', component: Puzzle_moneotau},
  { path: '/puzzle-phao', name: 'Puzzlephao', component: Puzzle_phao},
  { path: '/puzzle-sungthancong', name: 'Puzzlesungthancong', component: Puzzle_sungthancong},
  { path: '/puzzle-tautuantieupcf', name: 'Puzzletautuantieupcf', component: Puzzle_tautuantieupcf},
  { path: '/puzzle-trucmaybayb52', name: 'Puzzletrucmaybayb52', component: Puzzle_trucmaybayb52},
  { path: '/puzzle-xebocthep', name: 'Puzzlexebocthep', component: Puzzle_xebocthep},
  { path: '/puzzle-xepeugeot', name: 'Puzzlexepeugeot', component: Puzzle_xepeugeot},

  { path: '/game-xetang', name: 'Gamexetang', component: Game_xetang},

]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export default router