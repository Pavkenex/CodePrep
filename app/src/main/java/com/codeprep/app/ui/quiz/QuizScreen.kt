package com.codeprep.app.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun QuizScreen(viewModel: QuizViewModel = hiltViewModel(),
               onQuizFinished:()-> Unit
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            // 3. HEADER (Srca i Progres)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "❤️ ${state.userHearts}", style = MaterialTheme.typography.titleLarge)
                Text(text = "Score: ${state.score}", style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { paddingValues ->

        if (state.finished) {
            AlertDialog(
                onDismissRequest = { /* Prevent dismiss on back click if desired */ },
                title = {
                    Text(
                        text = "Kviz završen!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Column {
                        val totalQuestions = state.questions.size
                        val correctAnswers = state.score
                        val percentage = if (totalQuestions > 0) {
                            (correctAnswers.toFloat() / totalQuestions * 100).toInt()
                        } else 0

                        Text("Vaš uspeh: $percentage%", style = MaterialTheme.typography.bodyLarge)
                        Text("Osvojeno XP: ${state.xpEarned} ✨", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tačnih odgovora: $correctAnswers / $totalQuestions")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onQuizFinished,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Završi")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 4. LOGIKA PRIKAZA: Prikazujemo pitanja (čak i ako je dialog preko njih)
            // --- EKRAN PITANJA ---
            state.currentQuestion?.let { question ->
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // 5. LISTA ODGOVORA (Dugmići)
                question.options.forEachIndexed { index, optionText ->

                    // Određivanje boje dugmeta (Vizuelna logika)
                    val buttonColor = getButtonColor(
                        isAnswered = state.isAnswered,
                        isCorrectAnswer = index == question.correctIndex,
                        isSelected = index == state.selectedIndex
                    )

                    Button(
                        onClick = {
                            // 6. AKCIJA: Javljamo ViewModelu šta je kliknuto
                            viewModel.submitAnswer(index)
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        // Onemogući kliktanje ako je već odgovoreno
                        enabled = !state.isAnswered
                    ) {
                        Text(text = optionText)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 7. DUGME "DALJE" (Pojavi se samo kad se odgovori)
                if (state.isAnswered) {
                    Button(
                        onClick = { viewModel.nextQuestion() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Sledeće pitanje")
                    }

                    // Dodatna poruka ako je pogrešio
                    if (state.selectedIndex != question.correctIndex) {
                        Text("Pogrešno! Izgubili ste srce 💔", color = Color.Red)
                    } else {
                        Text("Tačno! +10 XP ✨", color = Color.Green)
                    }
                }
            }
        }
    }
}
// Pomoćna funkcija za boje (čisto da kod bude čitljiviji)
@Composable
fun getButtonColor(isAnswered: Boolean, isCorrectAnswer: Boolean, isSelected: Boolean): Color {
    return when {
        !isAnswered -> MaterialTheme.colorScheme.secondary // Obična boja pre odgovora
        isCorrectAnswer -> Color.Green // UVEK pokaži tačan odgovor zelenom na kraju
        isSelected && !isCorrectAnswer -> Color.Red // Ako si kliknuo ovo, a nije tačno -> Crveno
        else -> Color.Gray // Svi ostali postaju sivi
    }
}
