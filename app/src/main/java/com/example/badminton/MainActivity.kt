package com.example.badminton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BadmintonScoreScreen()
                }
            }
        }
    }
}

@Composable
fun BadmintonScoreScreen() {
    // États de l'application
    var scoreA by remember { mutableIntStateOf(0) }
    var scoreB by remember { mutableIntStateOf(0) }
    var setsA by remember { mutableIntStateOf(0) }
    var setsB by remember { mutableIntStateOf(0) }
    var challengesA by remember { mutableIntStateOf(2) }
    var challengesB by remember { mutableIntStateOf(2) }
    var statusText by remember { mutableStateOf("Match en cours") }

    // États pour les boîtes de dialogue
    var activeChallengePlayer by remember { mutableStateOf<String?>(null) }
    var showEndMatchDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- Affichage des Sets (Haut) ---
        Row(
            modifier = Modifier
                .background(Color(0xFFF0F0F0))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sets A: $setsA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE)
            )
            Text(text = "  |  ", fontSize = 18.sp)
            Text(
                text = "Sets B: $setsB",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6200EE)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = statusText, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Joueur A
            PlayerColumn(
                modifier = Modifier.weight(1f),
                name = "Joueur A",
                score = scoreA,
                onAddPoint = {
                    scoreA++
                    statusText = "Point pour Joueur A"
                },
                onMinusPoint = { if (scoreA > 0) scoreA-- },
                onChallenge = { activeChallengePlayer = "A" },
                onWinSet = {
                    setsA++
                    scoreA = 0
                    scoreB = 0
                    challengesA = 2
                    challengesB = 2
                    statusText = "Joueur A gagne le set !"
                }
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(250.dp)
                    .background(Color.LightGray)
            )

            // Joueur B
            PlayerColumn(
                modifier = Modifier.weight(1f),
                name = "Joueur B",
                score = scoreB,
                onAddPoint = {
                    scoreB++
                    statusText = "Point pour Joueur B"
                },
                onMinusPoint = { if (scoreB > 0) scoreB-- },
                onChallenge = { activeChallengePlayer = "B" },
                onWinSet = {
                    setsB++
                    scoreA = 0
                    scoreB = 0
                    challengesA = 2
                    challengesB = 2
                    statusText = "Joueur B gagne le set !"
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                scoreA = 0
                scoreB = 0
                setsA = 0
                setsB = 0
                challengesA = 2
                challengesB = 2
                statusText = "Match réinitialisé"
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Réinitialiser le match", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showEndMatchDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Terminer le match", color = Color.White)
        }
    }

    // Gestion du Challenge
    if (activeChallengePlayer != null) {
        val player = activeChallengePlayer!!
        val isA = player == "A"
        val currentChallenges = if (isA) challengesA else challengesB

        if (currentChallenges <= 0) {
            LaunchedEffect(Unit) {
                statusText = "Plus de challenges pour le Joueur $player"
                activeChallengePlayer = null
            }
        } else {
            AlertDialog(
                onDismissRequest = { activeChallengePlayer = null },
                title = { Text("Vidéo Review - Joueur $player") },
                text = { Text("Challenges restants : $currentChallenges\nQuel est le verdict ?") },
                confirmButton = {
                    Row {
                        TextButton(onClick = {
                            if (isA) scoreA++ else scoreB++
                            statusText = "Challenge réussi : Point pour Joueur $player"
                            activeChallengePlayer = null
                        }) {
                            Text("IN (Réussi)")
                        }
                        TextButton(onClick = {
                            if (isA) {
                                challengesA--
                                scoreB++
                            } else {
                                challengesB--
                                scoreA++
                            }
                            val remaining = if (isA) challengesA else challengesB
                            statusText = "Challenge perdu : Point pour l'adversaire ($remaining restants)"
                            activeChallengePlayer = null
                        }) {
                            Text("OUT (Perdu)")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeChallengePlayer = null }) {
                        Text("Annuler")
                    }
                }
            )
        }
    }

    if (showEndMatchDialog) {
        val winner = when {
            setsA > setsB -> "Joueur A"
            setsB > setsA -> "Joueur B"
            else -> "Égalité"
        }
        AlertDialog(
            onDismissRequest = { showEndMatchDialog = false },
            title = { Text("Match Terminé") },
            text = { Text("Le vainqueur est $winner !\nScore final : $setsA - $setsB (Sets)") },
            confirmButton = {
                Button(onClick = { showEndMatchDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun PlayerColumn(
    name: String,
    score: Int,
    onAddPoint: () -> Unit,
    onMinusPoint: () -> Unit,
    onChallenge: () -> Unit,
    onWinSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        Text(
            text = score.toString(),
            fontSize = 64.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Row {
            Button(
                onClick = onMinusPoint,
                modifier = Modifier.width(50.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("-", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onAddPoint,
                modifier = Modifier.width(50.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("+", fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onChallenge,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text("Challenge", maxLines = 1)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onWinSet,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text("Win Set", color = Color.White, maxLines = 1)
        }
    }
}
