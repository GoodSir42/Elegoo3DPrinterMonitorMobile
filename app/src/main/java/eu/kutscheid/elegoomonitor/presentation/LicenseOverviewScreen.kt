package eu.kutscheid.elegoomonitor.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import eu.kutscheid.elegoomonitor.R

@Composable
fun LicenseOverviewScreen(modifier: Modifier = Modifier) {
    val libs by produceLibraries(R.raw.aboutlibraries)
    LibrariesContainer(
        libraries = libs,
        modifier = modifier,
    )
}
