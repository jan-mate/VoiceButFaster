package voice.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.datastore.core.DataStore
import dev.olshevski.navigation.reimagined.AnimatedNavHost
import dev.olshevski.navigation.reimagined.NavBackHandler
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import dev.olshevski.navigation.reimagined.rememberNavController
import dev.olshevski.navigation.reimagined.replaceAll
import dev.olshevski.navigation.reimagined.replaceLast
import voice.app.injection.appComponent
import voice.bookOverview.views.BookOverviewScreen
import voice.common.BookId
import voice.common.compose.ComposeController
import voice.common.navigation.Destination
import voice.common.navigation.NavigationCommand
import voice.common.navigation.Navigator
import voice.common.pref.CurrentBook
import voice.cover.SelectCoverFromInternet
import voice.folderPicker.addcontent.AddContent
import voice.folderPicker.folderPicker.FolderOverview
import voice.folderPicker.selectType.SelectFolderType
import voice.migration.views.Migration
import voice.onboarding.OnboardingCompletion
import voice.onboarding.OnboardingExplanation
import voice.onboarding.OnboardingWelcome
import voice.settings.views.Settings
import javax.inject.Inject

class AppController : ComposeController() {

  init {
    appComponent.inject(this)
  }

  @field:[
  Inject
  CurrentBook
  ]
  lateinit var currentBookIdPref: DataStore<BookId?>

  @Inject
  lateinit var navigator: Navigator

  @Composable
  override fun Content() {
    val navController = rememberNavController<Destination.Compose>(
      startDestination = Destination.BookOverview,
    )
    NavBackHandler(navController)
    AnimatedNavHost(
      navController,
      transitionSpec = { action, destination, _ ->
        navTransition(action, destination)
      },
    ) { destination ->
      when (destination) {
        Destination.BookOverview -> {
          BookOverviewScreen()
        }
        Destination.FolderPicker -> {
          FolderOverview(
            onCloseClick = {
              navController.pop()
            },
          )
        }
        Destination.Migration -> {
          Migration()
        }
        is Destination.SelectFolderType -> {
          SelectFolderType(
            uri = destination.uri,
            mode = destination.mode,
          )
        }
        Destination.Settings -> {
          Settings()
        }
        is Destination.CoverFromInternet -> {
          SelectCoverFromInternet(
            bookId = destination.bookId,
            onCloseClick = { navController.pop() },
          )
        }
        is Destination.AddContent -> AddContent(destination.mode)

        Destination.OnboardingCompletion -> OnboardingCompletion(
          onBack = {
            navController.pop()
          },
          onNext = {
            navController.replaceAll(Destination.BookOverview)
          },
        )
        Destination.OnboardingExplanation -> OnboardingExplanation(
          onNext = {
            navController.navigate(
              Destination.AddContent(
                mode = Destination.AddContent.Mode.Onboarding,
              ),
            )
          },
          onBack = {
            navController.pop()
          },
        )
        Destination.OnboardingWelcome -> OnboardingWelcome(
          onNext = { navController.navigate(Destination.OnboardingExplanation) },
        )
      }
    }

    LaunchedEffect(navigator) {
      navigator.navigationCommands.collect { command ->
        when (command) {
          is NavigationCommand.GoTo -> {
            when (val destination = command.destination) {
              is Destination.Compose -> {
                if (command.replace) {
                  navController.replaceLast(destination)
                } else {
                  navController.navigate(destination)
                }
              }
              else -> {
                // no-op
              }
            }
          }
          NavigationCommand.GoBack -> {
            navController.pop()
          }
          is NavigationCommand.Execute -> {
            command.action(navController)
          }
        }
      }
    }
  }
}
