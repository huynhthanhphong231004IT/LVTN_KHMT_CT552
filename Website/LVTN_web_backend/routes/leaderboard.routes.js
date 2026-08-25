const express = require("express");
const router = express.Router();
const leaderboardController = require("../controllers/leaderboard.controller");

// GET /api/leaderboard
router.get("/", leaderboardController.getLeaderboard);

// POST /api/leaderboard
router.post("/", leaderboardController.submitScore);

module.exports = router;