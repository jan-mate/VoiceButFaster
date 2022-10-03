package voice.bookOverview.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.paulwoitaschek.flowpref.Pref
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import voice.bookOverview.GridCount
import voice.bookOverview.GridMode
import voice.bookOverview.overview.BookOverviewItemViewState
import voice.bookOverview.overview.BookOverviewLayoutMode
import voice.bookOverview.overview.toItemViewState
import voice.common.BookId
import voice.common.navigation.Destination
import voice.common.navigation.Navigator
import voice.common.pref.PrefKeys
import voice.data.repo.internals.dao.RecentBookSearchDao
import voice.search.BookSearch
import javax.inject.Inject
import javax.inject.Named

class BookSearchViewModel
@Inject constructor(
  private val search: BookSearch,
  private val navigator: Navigator,
  @Named(PrefKeys.GRID_MODE)
  private val gridModePref: Pref<GridMode>,
  private val gridCount: GridCount,
  private val recentBookSearchDao: RecentBookSearchDao,
) {

  private val query = mutableStateOf("")
  private val scope = MainScope()

  @Composable
  internal fun viewState(): BookSearchViewState {
    val layoutMode = when (gridModePref.value) {
      GridMode.LIST -> BookOverviewLayoutMode.List
      GridMode.GRID -> BookOverviewLayoutMode.Grid
      GridMode.FOLLOW_DEVICE -> if (gridCount.useGridAsDefault()) {
        BookOverviewLayoutMode.Grid
      } else {
        BookOverviewLayoutMode.List
      }
    }

    val recentBookSearch: List<String> by produceState(
      initialValue = emptyList(),
    ) {
      value = recentBookSearchDao.recentBookSearch().reversed()
    }
    var books by remember {
      mutableStateOf(emptyList<BookOverviewItemViewState>())
    }
    LaunchedEffect(query.value) {
      books = search.search(query.value).map { it.toItemViewState() }
    }

    return if (query.value.isNotBlank()) {
      BookSearchViewState.SearchResults(
        query = query.value,
        books = books,
        layoutMode = layoutMode,
      )
    } else {
      BookSearchViewState.InactiveSearch(
        recentQueries = recentBookSearch,
        query = query.value,
      )
    }
  }

  fun onBookClick(id: BookId) {
    val query = query.value
    if (query.isNotBlank()) {
      scope.launch {
        recentBookSearchDao.add(query.trim())
      }
    }
    navigator.goTo(Destination.Playback(id))
  }

  fun onCloseClick() {
    navigator.goBack()
  }

  fun onNewSearch(query: String) {
    this.query.value = query
  }
}
