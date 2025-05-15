package operations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ModeStandby
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import layout.bottomBorder

@OptIn(ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun FileRow(
    device: IpswFileStatus,
    isLoading: Boolean,
) {
    val fileName = device.file.name
    val operations = Operations()

    var deviceInfo: Device? by remember { mutableStateOf<Device?>(null) }
    var expandColumn by remember { mutableStateOf(false) }
    var expandRow by remember { mutableStateOf(false) }
    var detailsText by remember { mutableStateOf("See Details") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(device.file) {
        deviceInfo = operations.buildDevice(device.file.name)
    }

    if (!expandColumn){
        detailsText = "See Details"
    } else {
        detailsText = "Details"
    }



    Box(
        modifier = Modifier
            .padding(vertical = 2.dp,  horizontal = 12.dp)
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp)
                .onPointerEvent(PointerEventType.Enter) {
                    expandRow = true
                }
                .onPointerEvent(PointerEventType.Exit) {
                    if (!expandColumn) expandRow = false
                }
                .combinedClickable(
                    onClick = {
                        expandColumn = !expandColumn
                    },
                )
                .drawBehind {
                    // convert 1.dp into pixels
                    val strokeWidth = 1.dp.toPx()
                    // left edge
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, 0f),
                        end   = Offset(0f, size.height),
                        strokeWidth = strokeWidth
                    )
                }
                .bottomBorder(0.5.dp, Color.Gray),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp, 4.dp)

            ) {
                Row(
                    modifier =  Modifier
                        .padding(end = 6.dp),
                    horizontalArrangement =  Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    AnimatedVisibility(expandRow){
                        Text(detailsText)
                    }
                    Text(
                        modifier =  Modifier
                            .padding(horizontal = 6.dp),
                        text = deviceInfo?.apiDevice?.name ?: "Loading..",
                        color = Color.Black,
                        fontWeight =  FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        modifier =  Modifier
                            .padding(end = 6.dp),
                        text = "${deviceInfo?.currentVersion} >> ${deviceInfo?.latestVersion}" ?: "Loading..",
                        color = Color.Gray,
                        fontWeight =  FontWeight.Medium,
                        fontStyle =  FontStyle.Italic,
                        textAlign = TextAlign.Center,
                    )
                }
                AnimatedVisibility(expandColumn) {
                    expandedDetails(
                        deviceInfo = deviceInfo,
                    )
                }
            }
            Row (
                verticalAlignment = Alignment.Top
            ){

                Text(
                    fileName,
                    color = Color.Black,
                )

                Box{
                    if(isLoading){
                        CircularProgressIndicator()
                    } else {
                        when (device.isUpToDate) {
                            true -> Icon(Icons.Default.CheckCircle, "Up-to-date", tint = Color(0xFF4CAF50))
                            false -> Icon(Icons.Default.Warning, "Needs update", tint = Color(0xFFF44336))
                            null -> Icon(Icons.Default.ModeStandby, "Not checked", tint = Color(0xFFF44336))
                        }
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun expandedDetails(
    deviceInfo: Device?,
){
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 12.dp)

    ) {
        ElevatedCard  (
            Modifier,
            shape = RoundedCornerShape(8.dp),
        ){
            Column{
                Text(
                    text = deviceInfo?.apiDevice?.name ?: "Loading...",
                    color = Color.Black,
                    fontWeight =  FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = ("${deviceInfo?.apiDevice?.identifier}"),
                )
                Text(
                    text = ("Current version: ${deviceInfo?.currentVersion}"),
                )
                Text(
                    text = ("Latest version: ${deviceInfo?.latestVersion}"),
                )
            }
        }
    }
}