package eu.kutscheid.elegoomonitor.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import eu.kutscheid.elegoomonitor.R
import eu.kutscheid.elegoomonitor.presentation.MainActivity
import kotlin.math.roundToInt

/** Height at which the widget has room to show a second active print. */
private val TwoPrinterMinHeight = 150.dp

class PrinterWidget : GlanceAppWidget() {

    // Recomposes with the real cell size so we can show one or two printers.
    override val sizeMode = SizeMode.Exact

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val data = WidgetState.decode(prefs[WidgetState.DataKey])
            GlanceTheme {
                WidgetContent(data)
            }
        }
    }
}

@Composable
private fun WidgetContent(data: WidgetData) {
    val height = LocalSize.current.height
    val maxPrinters = if (height >= TwoPrinterMinHeight) 2 else 1
    val active = data.printers.take(maxPrinters)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(20.dp)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Header()
        Spacer(GlanceModifier.height(8.dp))
        when {
            !data.hasData -> CenteredMessage(LocalContext.current.getString(R.string.widget_searching))
            active.isEmpty() -> CenteredMessage(LocalContext.current.getString(R.string.widget_no_active_prints))
            else -> active.forEachIndexed { index, printer ->
                PrinterRow(printer)
                if (index < active.lastIndex) Spacer(GlanceModifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = LocalContext.current.getString(R.string.app_name),
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(GlanceModifier.defaultWeight())
        Text(
            text = LocalContext.current.getString(R.string.widget_refresh),
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = GlanceModifier.clickable(actionRunCallback<RefreshAction>()),
        )
    }
}

@Composable
private fun PrinterRow(printer: WidgetPrinter) {
    val context = LocalContext.current
    val percent = (printer.progress.coerceIn(0f, 1f) * 100).roundToInt()
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = printer.name,
                maxLines = 1,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = "$percent%",
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = context.getString(printer.status.labelResId()),
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 11.sp,
            ),
        )
        Spacer(GlanceModifier.height(6.dp))
        LinearProgressIndicator(
            progress = printer.progress.coerceIn(0f, 1f),
            modifier = GlanceModifier.fillMaxWidth(),
            color = GlanceTheme.colors.primary,
            backgroundColor = GlanceTheme.colors.secondaryContainer,
        )
    }
}

@Composable
private fun CenteredMessage(message: String) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 14.sp,
            ),
        )
    }
}
