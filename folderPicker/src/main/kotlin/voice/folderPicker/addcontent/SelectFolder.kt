package voice.folderPicker.addcontent

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import voice.common.navigation.Destination
import voice.folderPicker.R
import voice.folderPicker.folderPicker.FileTypeSelection
import voice.logging.core.Logger
import voice.strings.R as StringsR

@Composable
internal fun SelectFolder(
  onBack: () -> Unit,
  onAdd: (FileTypeSelection, Uri) -> Unit,
  mode: Destination.AddContent.Mode,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.Outlined.ArrowBack,
              contentDescription = stringResource(id = StringsR.string.close),
            )
          }
        },
      )
    },
    content = { contentPadding ->
      Column(Modifier.padding(contentPadding)) {
        if (LocalConfiguration.current.screenHeightDp > 500) {
          Spacer(modifier = Modifier.size(16.dp))
          Image(
            modifier = Modifier
              .widthIn(max = 400.dp)
              .padding(horizontal = 32.dp)
              .align(Alignment.CenterHorizontally),
            painter = painterResource(id = R.drawable.folder_type_artwork),
            contentDescription = null,
          )
        }
        Spacer(modifier = Modifier.size(16.dp))

        Text(
          modifier = Modifier.padding(horizontal = 24.dp),
          text = stringResource(
            when (mode) {
              Destination.AddContent.Mode.Default -> StringsR.string.select_folder_title_default
              Destination.AddContent.Mode.Onboarding -> StringsR.string.select_folder_title_onboarding
            },
          ),
          style = MaterialTheme.typography.displayMedium,
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
          modifier = Modifier.padding(horizontal = 24.dp),
          text = stringResource(StringsR.string.select_folder_subtitle),
          style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.size(24.dp))
        Row(
          Modifier
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          val openDocumentLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
          ) { uri ->
            if (uri != null) {
              onAdd(FileTypeSelection.File, uri)
            }
          }
          val documentTreeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
              onAdd(FileTypeSelection.Folder, uri)
            }
          }

          FilledTonalButton(
            onClick = {
              try {
                documentTreeLauncher.launch(null)
              } catch (e: ActivityNotFoundException) {
                Logger.e(e, "Could not add folder")
              }
            },
          ) {
            Icon(
              modifier = Modifier.size(ButtonDefaults.IconSize),
              imageVector = Icons.Outlined.Folder,
              contentDescription = stringResource(id = StringsR.string.select_folder_type_folder),
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(id = StringsR.string.select_folder_type_folder))
          }
          Spacer(modifier = Modifier.size(8.dp))
          FilledTonalButton(
            onClick = {
              try {
                openDocumentLauncher.launch(arrayOf("*/*"))
              } catch (e: ActivityNotFoundException) {
                Logger.e(e, "Could not add file")
              }
            },
          ) {
            Icon(
              modifier = Modifier.size(ButtonDefaults.IconSize),
              imageVector = Icons.Outlined.AudioFile,
              contentDescription = stringResource(id = StringsR.string.select_folder_type_file),
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(id = StringsR.string.select_folder_type_file))
          }
        }
      }
    },
  )
}

@Composable
@Preview
private fun SelectFolderPreview() {
  SelectFolder(
    onBack = {},
    onAdd = { _, _ -> },
    mode = Destination.AddContent.Mode.Default,
  )
}
