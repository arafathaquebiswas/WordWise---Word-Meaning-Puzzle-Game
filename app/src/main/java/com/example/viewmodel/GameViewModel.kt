package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Question
import com.example.repository.Difficulty
import com.example.repository.QuestionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DayActivity(
    val dayLabel: String,
    val count: Int,
    val dateString: String
)

sealed class GameUiEffect {
    object VibrateCorrect : GameUiEffect()
    object VibrateWrong : GameUiEffect()
}

data class GameUiState(
    val coins: Int = 0,
    val unlockedLevels: Set<Difficulty> = setOf(Difficulty.EASY),
    
    // Active session state
    val currentDifficulty: Difficulty = Difficulty.EASY,
    val isSessionActive: Boolean = false,
    val sessionQuestions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val currentQuestion: Question? = null,
    val shuffledOptions: List<String> = emptyList(),
    val selectedAnswer: String? = null,
    val secondsRemaining: Int = 15,
    val score: Int = 0,
    val streak: Int = 0,
    val maxStreakInSession: Int = 0,
    val isGameOver: Boolean = false,
    val questionResults: List<Boolean> = emptyList(), // True for correct, False for incorrect

    // Flashcard study mode state
    val isFlashcardActive: Boolean = false,
    val flashcardDifficulty: Difficulty = Difficulty.EASY,
    val flashcardDeckRemaining: List<Question> = emptyList(),
    val currentFlashcard: Question? = null,
    val isFlashcardFlipped: Boolean = false,
    val completedFlashcardsCount: Int = 0,
    val totalDeckSize: Int = 0,
    val isFlashcardDeckCompleted: Boolean = false,

    // Daily Streak & Progress Tracker
    val dailyStreak: Int = 0,
    val dailyProgressCount: Int = 0,
    val dailyProgressGoal: Int = 5,
    val lastSevenDaysActivity: List<DayActivity> = emptyList(),
    val wrongAnswersCount: Int = 0
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuestionRepository()
    private val sharedPrefs = application.getSharedPreferences("wordwise_quiz_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<GameUiEffect>()
    val uiEffect: SharedFlow<GameUiEffect> = _uiEffect.asSharedFlow()

    private var timerJob: Job? = null

    init {
        loadProgress()
    }

    private fun getTodayDateString(): String {
        return try {
            java.time.LocalDate.now().toString()
        } catch (e: Exception) {
            val cal = java.util.Calendar.getInstance()
            val year = cal.get(java.util.Calendar.YEAR)
            val month = cal.get(java.util.Calendar.MONTH) + 1
            val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
            String.format("%04d-%02d-%02d", year, month, day)
        }
    }

    private fun getDaysBetween(dateStr1: String, dateStr2: String): Long {
        if (dateStr1.isEmpty() || dateStr2.isEmpty()) return -1L
        return try {
            val d1 = java.time.LocalDate.parse(dateStr1)
            val d2 = java.time.LocalDate.parse(dateStr2)
            java.time.temporal.ChronoUnit.DAYS.between(d1, d2)
        } catch (e: Exception) {
            try {
                val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val d1 = format.parse(dateStr1)
                val d2 = format.parse(dateStr2)
                if (d1 != null && d2 != null) {
                    val diff = d2.time - d1.time
                    diff / (1000 * 60 * 60 * 24)
                } else {
                    -1L
                }
            } catch (ex: Exception) {
                -1L
            }
        }
    }

    private fun updateLastSevenDaysActivity() {
        val list = mutableListOf<DayActivity>()
        val cal = java.util.Calendar.getInstance()
        
        val sdfDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val sdfDay = java.text.SimpleDateFormat("EEE", java.util.Locale.US)
        
        cal.add(java.util.Calendar.DAY_OF_YEAR, -6)
        
        for (i in 0..6) {
            val dateStr = sdfDate.format(cal.time)
            val dayLabel = sdfDay.format(cal.time)
            val count = sharedPrefs.getInt("daily_progress_count_for_$dateStr", 0)
            list.add(DayActivity(dayLabel, count, dateStr))
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        
        _uiState.update {
            it.copy(lastSevenDaysActivity = list)
        }
    }

    private fun recordDailyActivity() {
        val today = getTodayDateString()
        val lastPlayed = sharedPrefs.getString("last_played_date", "") ?: ""
        var streak = sharedPrefs.getInt("daily_streak_count", 0)
        
        if (lastPlayed.isEmpty()) {
            streak = 1
            sharedPrefs.edit()
                .putString("last_played_date", today)
                .putInt("daily_streak_count", 1)
                .apply()
        } else if (lastPlayed != today) {
            val daysBetween = getDaysBetween(lastPlayed, today)
            if (daysBetween == 1L) {
                streak += 1
                sharedPrefs.edit()
                    .putString("last_played_date", today)
                    .putInt("daily_streak_count", streak)
                    .apply()
            } else if (daysBetween > 1L) {
                streak = 1
                sharedPrefs.edit()
                    .putString("last_played_date", today)
                    .putInt("daily_streak_count", 1)
                    .apply()
            } else {
                sharedPrefs.edit()
                    .putString("last_played_date", today)
                    .apply()
            }
        }

        val currentCountForToday = sharedPrefs.getInt("daily_progress_count_for_$today", 0)
        val newProgressCount = currentCountForToday + 1
        sharedPrefs.edit()
            .putInt("daily_progress_count_for_$today", newProgressCount)
            .apply()

        _uiState.update {
            it.copy(
                dailyStreak = streak,
                dailyProgressCount = newProgressCount
            )
        }
        updateLastSevenDaysActivity()
    }

    private fun loadProgress() {
        val savedCoins = sharedPrefs.getInt("user_coins_count", 0)
        val today = getTodayDateString()
        val lastPlayed = sharedPrefs.getString("last_played_date", "") ?: ""
        var streak = sharedPrefs.getInt("daily_streak_count", 0)
        val dailyCount = sharedPrefs.getInt("daily_progress_count_for_$today", 0)
        val goal = sharedPrefs.getInt("daily_progress_goal", 5)
        val wrongCount = sharedPrefs.getInt("wrong_answers_count", 0)
        
        if (lastPlayed.isNotEmpty()) {
            val daysBetween = getDaysBetween(lastPlayed, today)
            if (daysBetween > 1L) {
                streak = 0
                sharedPrefs.edit().putInt("daily_streak_count", 0).apply()
            }
        }

        val unlockedList = mutableSetOf(Difficulty.EASY)
        if (savedCoins >= 100) unlockedList.add(Difficulty.MEDIUM)
        if (savedCoins >= 250) unlockedList.add(Difficulty.HARD)

        _uiState.update {
            it.copy(
                coins = savedCoins,
                unlockedLevels = unlockedList,
                dailyStreak = streak,
                dailyProgressCount = dailyCount,
                dailyProgressGoal = goal,
                wrongAnswersCount = wrongCount
            )
        }
        updateLastSevenDaysActivity()
    }

    private fun saveCoins(newCoins: Int) {
        sharedPrefs.edit().putInt("user_coins_count", newCoins).apply()
        
        val unlockedList = mutableSetOf(Difficulty.EASY)
        if (newCoins >= 100) unlockedList.add(Difficulty.MEDIUM)
        if (newCoins >= 250) unlockedList.add(Difficulty.HARD)

        _uiState.update {
            it.copy(
                coins = newCoins,
                unlockedLevels = unlockedList
            )
        }
    }

    fun startNewSession(difficulty: Difficulty) {
        val allQuestionsOfDifficulty = repository.getQuestions(difficulty)
        // Select 10 random questions, ensuring no duplicates
        val randomizedList = allQuestionsOfDifficulty.shuffled().take(10)

        _uiState.update {
            it.copy(
                currentDifficulty = difficulty,
                isSessionActive = true,
                sessionQuestions = randomizedList,
                currentIndex = 0,
                currentQuestion = randomizedList.firstOrNull(),
                selectedAnswer = null,
                score = 0,
                streak = 0,
                maxStreakInSession = 0,
                isGameOver = false,
                questionResults = emptyList()
            )
        }
        prepareQuestionOptions()
        startTimer()
    }

    private fun prepareQuestionOptions() {
        val question = _uiState.value.currentQuestion ?: return
        val options = listOf(
            question.correct,
            question.option1,
            question.option2,
            question.option3
        ).shuffled()
        
        _uiState.update {
            it.copy(
                shuffledOptions = options,
                selectedAnswer = null,
                secondsRemaining = 15
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.secondsRemaining > 0 && _uiState.value.selectedAnswer == null) {
                delay(1000L)
                _uiState.update {
                    it.copy(secondsRemaining = it.secondsRemaining - 1)
                }
            }
            if (_uiState.value.secondsRemaining == 0 && _uiState.value.selectedAnswer == null) {
                // Time's up! Treat as wrong answer
                handleAnswerSelection("")
            }
        }
    }

    fun handleAnswerSelection(option: String) {
        if (_uiState.value.selectedAnswer != null) return // Already answered
        timerJob?.cancel()

        val currentQuestion = _uiState.value.currentQuestion ?: return
        val isCorrect = option == currentQuestion.correct

        var earnedCoinsInQuestion = 0
        val currentStreak = if (isCorrect) _uiState.value.streak + 1 else 0
        val maxStreak = maxOf(_uiState.value.maxStreakInSession, currentStreak)
        val isStreakBonus = isCorrect && currentStreak > 0 && currentStreak % 3 == 0
        var finalCoinsPenalty = 0

        if (isCorrect) {
            earnedCoinsInQuestion += 10 // +10 coins for correct answer
            if (isStreakBonus) {
                earnedCoinsInQuestion += 15 // Bonus streak rewards (+15 coins for every 3 in a row!)
            }
            viewModelScope.launch {
                _uiEffect.emit(GameUiEffect.VibrateCorrect)
            }
            recordDailyActivity()
        } else {
            viewModelScope.launch {
                _uiEffect.emit(GameUiEffect.VibrateWrong)
            }
            val currentWrongCount = sharedPrefs.getInt("wrong_answers_count", 0) + 1
            sharedPrefs.edit().putInt("wrong_answers_count", currentWrongCount).apply()
            
            if (currentWrongCount % 4 == 0) {
                finalCoinsPenalty = -1 // Deduct 1 coin/point (minimum 0)
            }
            
            _uiState.update {
                it.copy(wrongAnswersCount = currentWrongCount)
            }
        }

        val updatedCoins = maxOf(0, _uiState.value.coins + earnedCoinsInQuestion + finalCoinsPenalty)
        saveCoins(updatedCoins)

        val updatedResults = _uiState.value.questionResults + isCorrect
        val updatedScore = if (isCorrect) _uiState.value.score + 1 else _uiState.value.score

        _uiState.update {
            it.copy(
                selectedAnswer = option,
                streak = currentStreak,
                maxStreakInSession = maxStreak,
                score = updatedScore,
                questionResults = updatedResults
            )
        }

        // Wait 1.5 seconds and proceed
        viewModelScope.launch {
            delay(1500L)
            progressToNextQuestion()
        }
    }

    private fun progressToNextQuestion() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1

        if (nextIndex < state.sessionQuestions.size) {
            _uiState.update {
                it.copy(
                    currentIndex = nextIndex,
                    currentQuestion = state.sessionQuestions[nextIndex]
                )
            }
            prepareQuestionOptions()
            startTimer()
        } else {
            // End of active level run!
            // Calculate Level completion bonus
            var finalCompletionBonus = 0
            if (state.score >= 7) { // Earn a high score bonus!
                finalCompletionBonus += 30 // Level completion bonus
            }
            
            if (finalCompletionBonus > 0) {
                saveCoins(state.coins + finalCompletionBonus)
            }

            _uiState.update {
                it.copy(
                    isGameOver = true
                )
            }
        }
    }

    fun restartSession() {
        startNewSession(_uiState.value.currentDifficulty)
    }

    fun endSessionAndGoHome() {
        _uiState.update {
            it.copy(
                isSessionActive = false,
                isGameOver = false
            )
        }
    }

    // --- Flashcard studies logic implementation ---
    fun startNewFlashcardSession(difficulty: Difficulty) {
        val allQuestionsOfDifficulty = repository.getQuestions(difficulty)
        val shuffledDeck = allQuestionsOfDifficulty.shuffled()

        _uiState.update {
            it.copy(
                isFlashcardActive = true,
                flashcardDifficulty = difficulty,
                flashcardDeckRemaining = shuffledDeck,
                currentFlashcard = shuffledDeck.firstOrNull(),
                isFlashcardFlipped = false,
                completedFlashcardsCount = 0,
                totalDeckSize = shuffledDeck.size,
                isFlashcardDeckCompleted = false
            )
        }
    }

    fun flipFlashcard() {
        _uiState.update {
            it.copy(isFlashcardFlipped = !it.isFlashcardFlipped)
        }
    }

    fun handleFlashcardKnewIt() {
        val state = _uiState.value
        val currentCard = state.currentFlashcard ?: return
        
        // Remove current card from deck remaining
        val nextDeck = state.flashcardDeckRemaining.filter { it.word != currentCard.word }
        val newCompletedCount = state.completedFlashcardsCount + 1
        
        // Correct feedback
        viewModelScope.launch {
            _uiEffect.emit(GameUiEffect.VibrateCorrect)
        }
        
        // Earn +5 coins!
        val newCoins = state.coins + 5
        saveCoins(newCoins)
        recordDailyActivity()
        
        if (nextDeck.isEmpty()) {
            _uiState.update {
                it.copy(
                    flashcardDeckRemaining = emptyList(),
                    currentFlashcard = null,
                    completedFlashcardsCount = newCompletedCount,
                    isFlashcardDeckCompleted = true,
                    isFlashcardFlipped = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    flashcardDeckRemaining = nextDeck,
                    currentFlashcard = nextDeck.first(),
                    completedFlashcardsCount = newCompletedCount,
                    isFlashcardFlipped = false
                )
            }
        }
    }

    fun handleFlashcardForgotIt() {
        val state = _uiState.value
        val currentCard = state.currentFlashcard ?: return
        
        // Feedback
        viewModelScope.launch {
            _uiEffect.emit(GameUiEffect.VibrateWrong)
        }

        // Put card to the very back of the deck (or randomly in the deck) so it shows up again later in the session
        val remainingWithoutCurrent = state.flashcardDeckRemaining.filter { it.word != currentCard.word }
        
        val nextDeck = if (remainingWithoutCurrent.isNotEmpty()) {
            remainingWithoutCurrent + currentCard
        } else {
            listOf(currentCard)
        }
        
        _uiState.update {
            it.copy(
                flashcardDeckRemaining = nextDeck,
                currentFlashcard = nextDeck.firstOrNull(),
                isFlashcardFlipped = false
            )
        }
    }

    fun restartFlashcardSession() {
        startNewFlashcardSession(_uiState.value.flashcardDifficulty)
    }

    fun endFlashcardSessionAndGoHome() {
        _uiState.update {
            it.copy(
                isFlashcardActive = false,
                isFlashcardDeckCompleted = false
            )
        }
    }

    // Manual Admin/Preset coin booster for easy testing/play!
    fun addBonusCoins(amount: Int) {
        val updatedCoins = _uiState.value.coins + amount
        saveCoins(updatedCoins)
    }

    fun incrementCheatStreak() {
        val today = getTodayDateString()
        val streak = sharedPrefs.getInt("daily_streak_count", 0) + 1
        sharedPrefs.edit()
            .putString("last_played_date", today)
            .putInt("daily_streak_count", streak)
            .apply()

        _uiState.update {
            it.copy(dailyStreak = streak)
        }
    }

    fun incrementCheatDailyGoal() {
        val today = getTodayDateString()
        val currentCountForToday = sharedPrefs.getInt("daily_progress_count_for_$today", 0)
        val newProgressCount = currentCountForToday + 1
        sharedPrefs.edit()
            .putInt("daily_progress_count_for_$today", newProgressCount)
            .apply()

        _uiState.update {
            it.copy(dailyProgressCount = newProgressCount)
        }
    }

    fun setDailyGoal(newGoal: Int) {
        sharedPrefs.edit().putInt("daily_progress_goal", newGoal).apply()
        _uiState.update {
            it.copy(dailyProgressGoal = newGoal)
        }
        updateLastSevenDaysActivity()
    }

    fun resetGameProgress() {
        saveCoins(0)
        val today = getTodayDateString()
        sharedPrefs.edit()
            .putString("last_played_date", "")
            .putInt("daily_streak_count", 0)
            .putInt("daily_progress_count_for_$today", 0)
            .putInt("wrong_answers_count", 0)
            .apply()
        _uiState.update {
            it.copy(
                unlockedLevels = setOf(Difficulty.EASY),
                dailyStreak = 0,
                dailyProgressCount = 0,
                wrongAnswersCount = 0
            )
        }
        updateLastSevenDaysActivity()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
