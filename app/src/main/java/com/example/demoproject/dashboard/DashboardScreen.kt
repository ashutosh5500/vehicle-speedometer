package com.example.demoproject.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val Green = Color(0xFF45F59B)
private val Orange = Color(0xFFFF7045)
private val Muted = Color(0xFF9CA6AE)
private val Panel = Color(0xFF151A1E)

@Composable
fun DashboardScreen(state: DashboardState) {
    BoxWithConstraints(
        Modifier.fillMaxSize().background(Color(0xFF050708)).safeDrawingPadding()
    ) {
        val compact = maxHeight < 390.dp
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("S /", color = Orange, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(12.dp))
                Text("HOME / RIDE", color = Color.White, fontSize = 13.sp, letterSpacing = 2.sp)
                Spacer(Modifier.weight(1f))
                Text("●  SIMULATION", color = Green, fontSize = 11.sp, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(if (compact) 8.dp else 24.dp))
            Row(
                Modifier.widthIn(max = 400.dp).fillMaxWidth()
                    .background(Panel, RoundedCornerShape(14.dp))
                    .border(1.dp, Green.copy(alpha = .4f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SONIC", color = Orange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Text(state.scenario.uppercase(), color = Color.White, fontSize = 12.sp)
            }
            Spacer(Modifier.height(if (compact) 4.dp else 16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("VEHICLE STATUS", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
                    StatusPill("MOTOR", when (state.motorOn) { true -> "ON"; false -> "OFF"; null -> "UNKNOWN" },
                        if (state.motorOn == true) Green else Muted)
                }
                Column(Modifier.weight(1.7f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        state.speedKmh?.roundToInt()?.toString() ?: "—",
                        color = Color.White, fontSize = if (compact) 90.sp else 124.sp,
                        fontWeight = FontWeight.Light, lineHeight = if (compact) 94.sp else 128.sp,
                        textAlign = TextAlign.Center
                    )
                    Text("km / h", color = Muted, fontSize = 13.sp, letterSpacing = 4.sp)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("SAFETY STATUS", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
                    StatusPill("KILL SWITCH", when (state.killSwitchActive) {
                        true -> "ACTIVE"; false -> "INACTIVE"; null -> "UNKNOWN"
                    }, when (state.killSwitchActive) { true -> Orange; false -> Green; null -> Muted })
                }
            }
            SpeedRibbon(state.speedKmh, Modifier.fillMaxWidth().height(if (compact) 30.dp else 52.dp))
            Text(
                state.notice ?: when {
                    state.killSwitchActive == true -> "Motor disabled · kill switch active"
                    state.motorOn == true -> "System ready · simulated vehicle data"
                    else -> "Parked · motor off"
                },
                color = if (state.notice != null || state.killSwitchActive == true) Orange else Muted,
                fontSize = 12.sp, textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            HorizontalDivider(color = Color(0xFF242A2F))
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("HMI  /  DEMO", color = Muted, fontSize = 11.sp, letterSpacing = 1.sp)
                Text("AUTO CYCLE  ·  1 Hz", color = Muted, fontSize = 11.sp)
                Text("●  " + state.scenario, color = if (state.notice == null) Green else Orange, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, value: String, color: Color) {
    Column(
        Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp))
            .border(1.dp, color.copy(alpha = .25f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(label, color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
        Text("●  $value", color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SpeedRibbon(speed: Float?, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(0f, size.height * .85f)
            cubicTo(size.width * .25f, size.height * .85f, size.width * .25f, size.height * .25f, size.width * .5f, size.height * .25f)
            cubicTo(size.width * .75f, size.height * .25f, size.width * .75f, size.height * .85f, size.width, size.height * .85f)
            lineTo(size.width, size.height)
            cubicTo(size.width * .7f, size.height, size.width * .7f, size.height * .5f, size.width * .5f, size.height * .5f)
            cubicTo(size.width * .3f, size.height * .5f, size.width * .3f, size.height, 0f, size.height)
            close()
        }
        drawPath(path, Brush.horizontalGradient(listOf(Panel, if (speed == null) Muted else Orange, Panel)))
        repeat(41) { index ->
            val x = size.width * index / 40f
            drawLine(
                if (speed != null && index <= speed.coerceIn(0f, 100f) / 2.5f) Green else Color(0xFF30373D),
                Offset(x, 0f), Offset(x, if (index % 5 == 0) 9.dp.toPx() else 4.dp.toPx()),
                2.dp.toPx()
            )
        }
    }
}

@Preview(widthDp = 900, heightDp = 400, showBackground = true)
@Composable
private fun DashboardPreview() {
    MaterialTheme {
        DashboardScreen(DashboardState(50f, true, false, "Accelerating", null))
    }
}
