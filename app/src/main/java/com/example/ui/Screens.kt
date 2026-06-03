package com.example.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.repository.Difficulty
import com.example.viewmodel.DayActivity
import com.example.viewmodel.GameUiEffect
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.SharedFlow

// Custom high-end design gradient colors
val DeepMidnight = Color(0xFF0A0C10)
val TwilightBlue = Color(0xFF0F172A)
val LightSlate = Color(0xFFF1F5F9)
val AccendingOrange = Color(0xFFF97316)
val BrightYellow = Color(0xFFEAB308)
val DarkGreen = Color(0xFF166534)
val DarkRed = Color(0xFF991B1B)

val GlassBg = Color(0x600F172A)      // Deep semi-translucent slate (38% opacity) - pristine contrast
val GlassBgLight = Color(0x901E293B) // Slightly brighter deep semi-translucent slate (56% opacity)
val GlassBorder = Color(0x33FFFFFF)      // 20% white border
val GlassBorderLight = Color(0x4DFFFFFF) // 30% white border

fun triggerVibration(context: Context, isCorrect: Boolean) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isCorrect) {
                vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 80, 100), 
                    intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE), 
                    -1
                ))
            }
        } else {
            @Suppress("DEPRECATION")
            if (isCorrect) {
                vibrator.vibrate(120)
            } else {
                vibrator.vibrate(longArrayOf(0, 100, 80, 100), -1)
            }
        }
    } catch (e: Exception) {
        // Safe protection against missing permissions or device issues
    }
}

// Full background gradient wrap with gorgeous ambient radial orbs
@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C10))
            .drawBehind {
                // Top-left radial gradient (Blue glow)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF2563EB).copy(alpha = 0.22f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(-size.width * 0.1f, -size.height * 0.1f),
                        radius = size.width * 0.7f
                    )
                )
                // Bottom-right radial gradient (Indigo/Purple glow)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF312E81).copy(alpha = 0.28f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 1.1f, size.height * 1.1f),
                        radius = size.width * 0.7f
                    )
                )
                // Center-ish soft purple glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF581C87).copy(alpha = 0.18f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = size.width * 0.8f
                    )
                )
            },
        content = content
    )
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(navController: NavController) {
    var startAnim by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnim) 1.2f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )
    val opacityAnim by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(1200, easing = LinearOutSlowInEasing),
        label = "LogoOpacity"
    )

    LaunchedEffect(key1 = true) {
        startAnim = true
        delay(2200L) // Wait and transition to Home
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant Animated Logo Globe
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scaleAnim)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0X00FFFFFF))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "WordWise Logo",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Text Animations
            Text(
                text = "WordWise",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Word Meaning Puzzle Game",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFA5B4FC),
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.scale(scaleAnim),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color(0xFF6366F1),
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// 2. HOME SCREEN
@Composable
fun HomeScreen(
    navController: NavController,
    state: GameUiState,
    onSetGoal: (Int) -> Unit
) {
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Settings gear and Coins balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.testTag("settings_top_icon")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }

                // Balance display Pill (Frosted Glass style)
                Row(
                    modifier = Modifier
                        .background(GlassBg, RoundedCornerShape(20.dp))
                        .border(
                            1.dp,
                            GlassBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = BrightYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${state.coins} c",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            // Central focus: Goal Progress Ring with Daily Streak Badge above it!
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // WordWise Brand Title
                Text(
                    text = "WordWise",
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        letterSpacing = 1.sp,
                        fontSize = 32.sp
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "Master your Oxford 3000 & GRE vocabulary",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Small elegant Streak badge
                Row(
                    modifier = Modifier
                        .background(
                            if (state.dailyStreak > 0) Color(0xFFFF9800).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (state.dailyStreak > 0) Color(0xFFFF9800).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak Count",
                        tint = if (state.dailyStreak > 0) Color(0xFFFF9800) else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.dailyStreak > 0) "${state.dailyStreak} DAY STREAK" else "0 DAY STREAK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Beautiful interactive circular Goal Ring
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .testTag("daily_goal_ring"),
                    contentAlignment = Alignment.Center
                ) {
                    val progressFraction = if (state.dailyProgressGoal > 0) (state.dailyProgressCount.toFloat() / state.dailyProgressGoal.toFloat()).coerceIn(0f, 1f) else 0f
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background circle track
                        drawArc(
                            color = Color.White.copy(alpha = 0.07f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 11.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                        // Progress ring with beautiful turquoise - emerald gradient
                        drawArc(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFF10B981))
                            ),
                            startAngle = -90f,
                            sweepAngle = progressFraction * 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 11.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${state.dailyProgressCount} / ${state.dailyProgressGoal}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 34.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "words today",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium
                              )
                        )
                    }
                }
            }

            // Bottom Core Action buttons: Play Button (big, center) & Level + Flashcards buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Play Button (big center button)
                Button(
                    onClick = { navController.navigate("level_selection") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(60.dp)
                        .testTag("play_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow, 
                            contentDescription = "Play Quiz Mode", 
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START LEARNING",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                fontSize = 16.sp
                            )
                        )
                    }
                }

                // Row of small functional buttons: Difficulty Level and Flashcards study
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Level selection button
                    Button(
                        onClick = { navController.navigate("level_selection") },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("levels_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassBg,
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            GlassBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dashboard, 
                                contentDescription = "Levels Icon", 
                                tint = Color(0xFFA5B4FC),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LEVELS",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Flashcard study mode button
                    Button(
                        onClick = { navController.navigate("flashcard_difficulty") },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("flashcards_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassBg,
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            GlassBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Style, 
                                contentDescription = "Flashcard study", 
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CARDS",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityBarChart(
    activityList: List<DayActivity>,
    modifier: Modifier = Modifier
) {
    val maxCount = maxOf(1, activityList.maxOfOrNull { it.count } ?: 0)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .testTag("activity_bar_chart"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBgLight),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Activity Icon",
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LAST 7 DAYS ACTIVITY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA5B4FC),
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        )
                    )
                }
                
                val totalWordsLearnedThisWeek = activityList.sumOf { it.count }
                Text(
                    text = "Total: $totalWordsLearnedThisWeek",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                activityList.forEach { day ->
                    val barHeightFraction = day.count.toFloat() / maxCount.toFloat()
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.height(20.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (day.count > 0) {
                                Text(
                                    text = day.count.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF60A5FA),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .fillMaxHeight(0.72f * barHeightFraction)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            if (day.count > 0) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.05f),
                                            if (day.count > 0) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.02f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                                .border(
                                    1.dp,
                                    if (day.count > 0) Color(0xFF60A5FA).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = day.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (day.count > 0) Color.White else Color(0xFF94A3B8),
                                fontWeight = if (day.count > 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyGoalSelectorCard(
    currentGoal: Int,
    onSetGoal: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 5.dp)
            .testTag("daily_goal_selector_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBgLight),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Target",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SET YOUR DAILY GOAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA5B4FC),
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(3, 5, 10, 15, 20).forEach { goalValue ->
                    val isSelected = goalValue == currentGoal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .height(34.dp)
                            .background(
                                if (isSelected) Color(0xFF60A5FA).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF60A5FA) else GlassBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onSetGoal(goalValue)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = goalValue.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyStatsCard(
    streak: Int,
    progress: Int,
    goal: Int
) {
    val progressFraction = if (goal > 0) (progress.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 10.dp)
            .testTag("daily_stats_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GlassBgLight),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Side: Streak information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.weight(1.1f)
            ) {
                // Flame / Streak Icon with simple background glow
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (streak > 0) Color(0xFFFF9800).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (streak > 0) Color(0xFFFF9800).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak Counter",
                        tint = if (streak > 0) Color(0xFFFF9800) else Color(0xFF94A3B8),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column {
                    Text(
                        text = "DAILY STREAK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA5B4FC),
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (streak > 0) "$streak Day${if (streak > 1) "s" else ""}" else "Start Streak!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .width(1.dp)
                    .background(GlassBorder.copy(alpha = 0.5f))
            )

            // Right Side: Daily Goal progress ring
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .weight(0.9f)
                    .padding(start = 6.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Text(
                        text = "DAILY GOAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA5B4FC),
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$progress / $goal Words",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (progress >= goal) Color(0xFF10B981) else Color.White,
                            fontSize = 12.sp
                        )
                    )
                }

                // Progress Circular Ring
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background track
                        drawArc(
                            color = Color.White.copy(alpha = 0.08f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 3.5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                        // Progress arch
                        drawArc(
                            color = if (progress >= goal) Color(0xFF10B981) else Color(0xFF60A5FA),
                            startAngle = -90f,
                            sweepAngle = progressFraction * 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 3.5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }

                    if (progress >= goal) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Goal Met",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        val percentage = (progressFraction * 100).toInt()
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

// 3. LEVEL SELECTION SCREEN
@Composable
fun LevelSelectionScreen(
    navController: NavController,
    state: GameUiState,
    onLevelSelected: (Difficulty) -> Unit
) {
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom description
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = GlassBg
                ),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = "Tip",
                        tint = BrightYellow,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Unlock higher categories using your accrued coins. Medium unlocks at 100 coins, Hard at 250 coins.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFCBD5E1)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Difficulty Cards (Easy, Medium, Hard)
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DifficultyRow(
                    difficulty = Difficulty.EASY,
                    label = "EASY",
                    description = "Oxford 3000 basics. Quick and elementary.",
                    coinsRequired = 0,
                    isUnlocked = true, // Always unlocked
                    accentColor = Color(0xFF10B981),
                    currentCoins = state.coins,
                    onClick = {
                        onLevelSelected(Difficulty.EASY)
                        navController.navigate("game")
                    }
                )

                DifficultyRow(
                    difficulty = Difficulty.MEDIUM,
                    label = "MEDIUM",
                    description = "Intermediate vocabulary. Broad topics.",
                    coinsRequired = 100,
                    isUnlocked = state.unlockedLevels.contains(Difficulty.MEDIUM),
                    accentColor = Color(0xFFEAB308),
                    currentCoins = state.coins,
                    onClick = {
                        onLevelSelected(Difficulty.MEDIUM)
                        navController.navigate("game")
                    }
                )

                DifficultyRow(
                    difficulty = Difficulty.HARD,
                    label = "HARD",
                    description = "GRE advanced elite words. Ultimate challenge.",
                    coinsRequired = 250,
                    isUnlocked = state.unlockedLevels.contains(Difficulty.HARD),
                    accentColor = Color(0xFFEF4444),
                    currentCoins = state.coins,
                    onClick = {
                        onLevelSelected(Difficulty.HARD)
                        navController.navigate("game")
                    }
                )
            }
        }
    }
}

@Composable
fun DifficultyRow(
    difficulty: Difficulty,
    label: String,
    description: String,
    coinsRequired: Int,
    isUnlocked: Boolean,
    accentColor: Color,
    currentCoins: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_row_${difficulty.name.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) GlassBg else GlassBg.copy(alpha = 0.05f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUnlocked) GlassBorder else GlassBorder.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(0.7f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored dot / Locked indicator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isUnlocked) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                        contentDescription = if (isUnlocked) "Unlocked" else "Locked",
                        tint = if (isUnlocked) accentColor else Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = if (isUnlocked) Color.White else Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isUnlocked) Color(0xFF94A3B8) else Color(0xFF475569)
                        )
                    )
                }
            }

            // Lock / Cost text on the right
            if (!isUnlocked) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.weight(0.3f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins cost",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$coinsRequired",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B)
                            )
                        )
                    }
                    Text(
                        text = "Need ${coinsRequired - currentCoins} c",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFEF4444),
                            fontSize = 10.sp
                        )
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// 4. GAME PLAY SCREEN
@Composable
fun GameScreen(
    navController: NavController,
    state: GameUiState,
    onAnswerSelected: (String) -> Unit,
    onQuit: () -> Unit,
    effectFlow: kotlinx.coroutines.flow.SharedFlow<GameUiEffect>
) {
    val context = LocalContext.current

    // Observe effects for vibration feedback
    LaunchedEffect(key1 = true) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                is GameUiEffect.VibrateCorrect -> triggerVibration(context, true)
                is GameUiEffect.VibrateWrong -> triggerVibration(context, false)
            }
        }
    }

    // Handle end game redirect to results
    LaunchedEffect(key1 = state.isGameOver) {
        if (state.isGameOver) {
            navController.navigate("results") {
                popUpTo("game") { inclusive = true }
            }
        }
    }

    val currentQuestion = state.currentQuestion

    AppBackground {
        if (currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onQuit,
                        modifier = Modifier.testTag("quit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quit match",
                            tint = Color.White
                        )
                    }

                    // Score pill (Frosted glass)
                    Row(
                        modifier = Modifier
                            .background(GlassBgLight, RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                GlassBorderLight,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Score",
                            tint = AccendingOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.score}/10",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    // Level/Difficulty label (Frosted glass with dynamic glowing boundary)
                    val diffColor = when (state.currentDifficulty) {
                        Difficulty.EASY -> Color(0xFF10B981)
                        Difficulty.MEDIUM -> Color(0xFFEAB308)
                        Difficulty.HARD -> Color(0xFFEF4444)
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = diffColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                diffColor.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = state.currentDifficulty.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = when (state.currentDifficulty) {
                                    Difficulty.EASY -> Color(0xFF10B981)
                                    Difficulty.MEDIUM -> Color(0xFFF1C40F)
                                    Difficulty.HARD -> Color(0xFFFF6B6B)
                                }
                            )
                        )
                    }
                }

                // Progress Indicators bar (Dots or segment bar)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Question ${state.currentIndex + 1} of 10",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                        )

                        if (state.streak >= 3) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak on",
                                    tint = AccendingOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.streak} STREAK!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AccendingOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Progress track (Frosted glass background track)
                    LinearProgressIndicator(
                        progress = { (state.currentIndex + 1) / 10f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF3B82F6),
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )

                    // Dot representation of previous answers (Green = Correct, Red = Wrong, Grey = Remaining)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0 until 10) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when {
                                            i < state.questionResults.size -> {
                                                if (state.questionResults[i]) Color(0xFF10B981) else Color(0xFFEF4444)
                                            }
                                            i == state.currentIndex -> Color(0xFF3B82F6)
                                            else -> Color.White.copy(alpha = 0.12f)
                                        }
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timer Visual & Word Display Container inside dynamic Box constraints
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                        .background(GlassBg, RoundedCornerShape(24.dp))
                        .border(
                            1.dp,
                            GlassBorder,
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Countdown Ring
                        Box(
                            modifier = Modifier.size(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val animatedProgress by animateFloatAsState(
                                targetValue = state.secondsRemaining / 15f,
                                animationSpec = tween(500, easing = LinearEasing),
                                label = "TimerRing"
                            )

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color.White.copy(alpha = 0.08f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = when {
                                        state.secondsRemaining > 8 -> Color(0xFF10B981)
                                        state.secondsRemaining > 4 -> Color(0xFFEAB308)
                                        else -> Color(0xFFEF4444)
                                    },
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress,
                                    useCenter = false,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            Text(
                                text = "${state.secondsRemaining}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        state.secondsRemaining > 8 -> Color.White
                                        state.secondsRemaining > 4 -> Color(0xFFEAB308)
                                        else -> Color(0xFFEF4444)
                                    }
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Large centered Word with elegant typography pairing (Serif Italic)
                        Text(
                            text = currentQuestion.word,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 38.sp,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Serif,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category hint pill
                        Text(
                            text = "Find the correct definition below",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Answer Multiple-Choice Options List (4 buttons style of tailwind markup blocks)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.shuffledOptions.forEachIndexed { optIndex, option ->
                        val isSelected = state.selectedAnswer == option
                        val hasAnswered = state.selectedAnswer != null
                        val isCorrectChoice = option == currentQuestion.correct

                        // Beautiful glass color mappings depending on state
                        val optionBgColor = when {
                            !hasAnswered -> GlassBg
                            isSelected && isCorrectChoice -> Color(0x3B10B981)
                            isSelected && !isCorrectChoice -> Color(0x3BEF4444)
                            isCorrectChoice -> Color(0x3B10B981)
                            else -> GlassBg.copy(alpha = 0.05f)
                        }

                        val strokeColor = when {
                            !hasAnswered -> GlassBorder
                            isCorrectChoice -> Color(0xFF22C55E)
                            isSelected -> Color(0xFFEF4444)
                            else -> GlassBorder.copy(alpha = 0.1f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clickable(
                                    enabled = !hasAnswered,
                                    onClick = { onAnswerSelected(option) }
                                )
                                .testTag("option_button_$optIndex"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = optionBgColor),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, strokeColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (hasAnswered && !isCorrectChoice && !isSelected) Color(0xFF64748B) else Color.White,
                                        fontWeight = if (isCorrectChoice && hasAnswered) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier.weight(0.85f)
                                )

                                // Trailing status icon
                                if (hasAnswered) {
                                    Box(
                                        modifier = Modifier.weight(0.15f),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isCorrectChoice -> Icons.Default.CheckCircle
                                                isSelected -> Icons.Default.Cancel
                                                else -> Icons.Default.RadioButtonUnchecked
                                            },
                                            contentDescription = "Answer Feedback",
                                            tint = when {
                                                isCorrectChoice -> Color(0xFF22C55E)
                                                isSelected -> Color(0xFFEF4444)
                                                else -> Color(0xFF475569)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. RESULT SCORE SCREEN
@Composable
fun ResultScreen(
    navController: NavController,
    state: GameUiState,
    onRestart: () -> Unit
) {
    val totalCoinsEarned = (state.score * 10) + (if (state.score >= 7) 30 else 0)
    val unlockedNewLevel = state.score >= 7

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Screen Title header
            Text(
                text = "Session Complete!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA5B4FC),
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(top = 16.dp)
            )

            // Circular Score Dial Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color.White.copy(alpha = 0.08f),
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = when {
                                state.score >= 8 -> Color(0xFF10B981) // Well done!
                                state.score >= 5 -> Color(0xFFEAB308) // Passed!
                                else -> Color(0xFFEF4444) // Try again!
                            },
                            startAngle = -220f,
                            sweepAngle = 260f * (state.score / 10f),
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.score}",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                        HorizontalDivider(
                            modifier = Modifier.width(60.dp),
                            thickness = 2.dp,
                            color = Color.White.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "10 questions",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom performance evaluation
                Text(
                    text = when {
                        state.score == 10 -> "EXCELLENT! Word Mastermind!"
                        state.score >= 8 -> "GREAT WORK! Solid vocabulary!"
                        state.score >= 5 -> "GOOD EFFORT! Keep studying!"
                        else -> "TRY AGAIN! Keep practicing words!"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = when {
                            state.score >= 8 -> Color(0xFF10B981)
                            state.score >= 5 -> Color(0xFFEAB308)
                            else -> Color(0xFFEF4444)
                        }
                    )
                )
            }

            // Earned Rewards List container (Frosted Glass style)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GlassBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "REWARDS EARNED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                    )

                    // Correct answers row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        statsItem(
                            icon = Icons.Default.Check,
                            iconColor = Color(0xFF10B981),
                            title = "Correct Questions",
                            subtitle = "${state.score} correct"
                        )
                        Text(
                            text = "+${state.score * 10} c",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    // Level complete bonus row
                    if (state.score >= 7) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            statsItem(
                                icon = Icons.Default.EmojiEvents,
                                iconColor = BrightYellow,
                                title = "High Score Reward",
                                subtitle = "Achieved >= 7 correct"
                            )
                            Text(
                                text = "+30 c",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrightYellow
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = GlassBorder.copy(alpha = 0.2f), thickness = 1.dp)

                    // Total earnings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL BALANCE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins balance",
                                tint = BrightYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${state.coins} c",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BrightYellow
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Buttons (Restart, Next Level / Difficulty Select, Home)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Restart Match Button (Frosted highlighting style)
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("restart_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassBgLight,
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        Color(0xFF60A5FA)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color(0xFF60A5FA))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PLAY AGAIN",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                // Menu Select Difficulty (Frosted glass)
                Button(
                    onClick = {
                        state.isSessionActive // Trigger state pop
                        navController.navigate("level_selection") {
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("select_level_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GlassBg,
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        GlassBorder
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Dashboard, contentDescription = "Levels list", tint = Color(0xFFA5B4FC))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CHANGE CATEGORY",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                // Home view
                TextButton(
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_button")
                ) {
                    Text(
                        text = "Return to Dashboard",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFA5B4FC),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun statsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8)
                )
            )
        }
    }
}

// 6. SETTINGS & PROGRESS SCREEN
@Composable
fun SettingsScreen(
    navController: NavController,
    state: GameUiState,
    onAdminAddCoins: () -> Unit,
    onResetProgress: () -> Unit,
    onIncrementStreak: () -> Unit,
    onIncrementDailyGoal: () -> Unit,
    onSetGoal: (Int) -> Unit
) {
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Settings & Progress",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress stats (Frosted Glass style)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GlassBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "PLAYER PROGRESSION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Accrued Balance",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = BrightYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${state.coins} c",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrightYellow
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categories Unlocked",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                        )
                        Text(
                            text = if (state.unlockedLevels.size == 3) "ALL (3)" else "${state.unlockedLevels.size}/3",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful goal setting directly in Settings Screen!
            DailyGoalSelectorCard(
                currentGoal = state.dailyProgressGoal,
                onSetGoal = onSetGoal
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful 7-day Activity Chart & Progress Analytics!
            ActivityBarChart(
                activityList = state.lastSevenDaysActivity,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Options list (Frosted Glass style)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GlassBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "DEVELOPER / TESTING CHEATS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cheat coins button (Translucent outline)
                    Button(
                        onClick = onAdminAddCoins,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_boost_coins_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3B38BDF8)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BOOST PROFILE BALANCE (+100 Coins)",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cheat streak button (Translucent outline)
                    Button(
                        onClick = onIncrementStreak,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_boost_streak_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3BFF9800)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BOOST STREAK (+1 Day)",
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Cheat goal progress button (Translucent outline)
                    Button(
                        onClick = onIncrementDailyGoal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_boost_goal_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3B60A5FA)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DonutLarge,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PROGRESS DAILY GOAL (+1 Word Learned)",
                            color = Color(0xFF60A5FA),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Reset button (Translucent outline)
                    Button(
                        onClick = onResetProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_progress_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3BF97316)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = Color(0xFFF97316)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RESET ALL PROGRESS & STREAKS",
                            color = Color(0xFFF97316),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Game version
            Text(
                text = "WordWise Game v1.0.0 (Offline Native Play)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF475569)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------- NEW FEATURE: FLASHCARDS MODE SCREENS ----------------

@Composable
fun FlashcardLevelSelectionScreen(
    navController: NavController,
    state: GameUiState,
    onLevelSelected: (Difficulty) -> Unit
) {
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }},
                    modifier = Modifier.testTag("fc_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to main menu",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "STUDY DECKS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle
            Text(
                text = "Choose a vocabulary deck below to start studying and earning coins (+5 coins/mastered card). Unlock new decks by earning coins!",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8)),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Level Option list in column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(
                    Triple(Difficulty.EASY, "EASY DECK", "81 core Oxford 3000 words. Ready to study."),
                    Triple(Difficulty.MEDIUM, "MEDIUM DECK", "70 intermediate GRE words. Needs 100 coins."),
                    Triple(Difficulty.HARD, "HARD DECK", "70 advanced scholastic GRE words. Needs 250 coins.")
                ).forEach { (difficulty, title, desc) ->
                    val isUnlocked = state.unlockedLevels.contains(difficulty)
                    
                    val cardBg = if (isUnlocked) GlassBg else GlassBg.copy(alpha = 0.15f)
                    val borderColor = if (isUnlocked) GlassBorder else GlassBorder.copy(alpha = 0.08f)
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(enabled = isUnlocked) {
                                onLevelSelected(difficulty)
                            }
                            .testTag("fc_deck_${difficulty.name.lowercase()}"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(0.75f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.4f),
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (isUnlocked) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Unlocked",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isUnlocked) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )
                                )
                            }
                            
                            // Right Chevron or lock requirements
                            if (isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Start studying",
                                    tint = Color(0xFFA5B4FC)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF7F1D1D).copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (difficulty == Difficulty.MEDIUM) "100 c" else "250 c",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFCA5A5)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Subtitle bottom note
            Text(
                text = "Unlocking is unified. Earning coins in Quiz mode unlocks higher library decks here too!",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF475569)),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun FlashcardStudyScreen(
    navController: NavController,
    state: GameUiState,
    onFlip: () -> Unit,
    onKnewIt: () -> Unit,
    onForgotIt: () -> Unit,
    onQuit: () -> Unit,
    onRestart: () -> Unit,
    effectFlow: SharedFlow<GameUiEffect>
) {
    val context = LocalContext.current

    // Listen to vibration triggers
    LaunchedEffect(key1 = true) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                is GameUiEffect.VibrateCorrect -> triggerVibration(context, isCorrect = true)
                is GameUiEffect.VibrateWrong -> triggerVibration(context, isCorrect = false)
            }
        }
    }

    // Card Flipping Angle Animation
    val rotationAngle by animateFloatAsState(
        targetValue = if (state.isFlashcardFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "FlashcardRotation"
    )

    AppBackground {
        if (state.isFlashcardDeckCompleted) {
            // GORGEOUS COMPLETED DECK VIEW
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Trophy Globe
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFEAB308).copy(alpha = 0.25f), Color.Transparent)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy Complete",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(85.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "DECK COMPLETED!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "You studied and mastered every single word in the ${state.flashcardDifficulty} vocabulary deck!",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Stats overview (Frosted Glass)
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GlassBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL WORDS MASTERED",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${state.totalDeckSize}",
                            style = MaterialTheme.typography.displayMedium.copy(color = Color.White, fontWeight = FontWeight.Black)
                        )
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = GlassBorder, thickness = 1.dp)

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = "Coins Earned", tint = BrightYellow, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+${state.totalDeckSize * 5} Coins Earned!",
                                style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Buttons list (Restart Deck vs Menu)
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp)
                        .testTag("fc_restart_session"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = "Study Again")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "STUDY DECK AGAIN", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onQuit,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp)
                        .testTag("fc_go_home_finished"),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassBg, contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "RETURN TO HOME", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        } else {
            // ACTIVE STUDY VIEW
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header and Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onQuit,
                        modifier = Modifier.testTag("fc_quit_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Quit study session",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "DECK: ${state.flashcardDifficulty}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFA5B4FC),
                            letterSpacing = 1.sp
                        )
                    )

                    // Balance Display
                    Row(
                        modifier = Modifier
                            .background(GlassBg, RoundedCornerShape(20.dp))
                            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Coins Balance",
                            tint = BrightYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.coins}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                // Progress Indicator
                val progressFraction = if (state.totalDeckSize > 0) {
                    state.completedFlashcardsCount.toFloat() / state.totalDeckSize.toFloat()
                } else 0f

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Deck studied progress",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF64748B))
                        )
                        Text(
                            text = "${state.completedFlashcardsCount} / ${state.totalDeckSize} words",
                            style = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = Color(0xFF60A5FA),
                        trackColor = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }

                // 3D FLIPPABLE FLASHCARD
                val currentWord = state.currentFlashcard
                if (currentWord != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .padding(vertical = 12.dp)
                            .graphicsLayer {
                                rotationY = rotationAngle
                                cameraDistance = 14 * density
                            }
                            .clickable(onClick = onFlip)
                            .testTag("fc_clickable_card"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rotationAngle <= 90f) {
                            // FRONT SIDE
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = GlassBgLight),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, GlassBorderLight),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "WORD",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color(0xFF60A5FA),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 3.sp
                                        )
                                    )
                                    
                                    Spacer(modifier = Modifier.height(20.dp))

                                    Text(
                                        text = currentWord.word,
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Serif
                                        ),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(26.dp))

                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = BrightYellow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Tap Card to Reveal Meaning",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = Color(0xFFA5B4FC),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // BACK SIDE (must rotate by 180f on rotation to prevent mirrored content!)
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        rotationY = 180f
                                    },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xEC0B0F19)), // High opacity dark backing for flawless definition clarity!
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF60A5FA).copy(alpha = 0.4f)),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Reference Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentWord.word,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = Color(0xFF60A5FA),
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Serif
                                            )
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Current Card word",
                                            tint = Color(0xFF10B981).copy(alpha = 0.5f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Definition Body
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "DEFINITION",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF64748B),
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.5.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = currentWord.correct,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                lineHeight = 24.sp
                                            ),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Quick visual pointer
                                    Text(
                                        text = "(Tap again to see front side)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF475569)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // BOTTOM ACTION BAR
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!state.isFlashcardFlipped) {
                        // Standard Unflipped helper button
                        Button(
                            onClick = onFlip,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("fc_reveal_prompt"),
                            colors = ButtonDefaults.buttonColors(containerColor = GlassBg, contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RemoveRedEye, contentDescription = "Reveal Meaning", tint = Color(0xFFA5B4FC))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "REVEAL MEANING",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                )
                            }
                        }
                    } else {
                        // Flipped side-by-side Know vs Forget buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // DIDN'T KNOW (Red Outline/Subtle Glow)
                            Button(
                                onClick = onForgotIt,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("fc_btn_forgot"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x28EF4444), // Translucent red
                                    contentColor = Color(0xFFEF4444)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF87171)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Forgot It", tint = Color(0xFFEF4444))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "FORGOT",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }

                            // KNEW IT! (Green solid/semi-frosted glow)
                            Button(
                                onClick = onKnewIt,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("fc_btn_knew"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981), // Fluent Emerald Green
                                    contentColor = Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF34D399)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Knew It", tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "KNEW IT (+5)",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
