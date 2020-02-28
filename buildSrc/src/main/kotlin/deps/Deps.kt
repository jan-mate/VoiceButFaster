package deps

import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.dsl.RepositoryHandler

object Versions {
  val sourceCompatibility = JavaVersion.VERSION_1_8
  val targetCompatibility = JavaVersion.VERSION_1_8

  const val compileSdk = 29
  const val minSdk = 23
  const val targetSdk = 28
  const val versionCode = 3060331
  const val versionName = "5.0.0-rc4"
}

object Deps {

  object AndroidX {
    const val supportAnnotations = "androidx.annotation:annotation:1.0.0"
    const val appCompat = "androidx.appcompat:appcompat:1.1.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.3"
    const val recyclerView = "androidx.recyclerview:recyclerview:1.1.0"
    const val transitions = "androidx.transition:transition:1.3.0"
    const val palette = "androidx.palette:palette:1.0.0"
    const val mediaCompat = "androidx.media:media:1.1.0"
    const val fragment = "androidx.fragment:fragment:1.2.0"
    const val ktx = "androidx.core:core-ktx:1.1.0"

    object Room {
      private const val version = "2.2.3"
      const val runtime = "androidx.room:room-runtime:$version"
      const val compiler = "androidx.room:room-compiler:$version"
      const val testing = "androidx.room:room-testing:$version"
      const val rxJava = "androidx.room:room-rxjava2:$version"
    }

    object Test {
      const val runner = "androidx.test:runner:1.2.0"
      const val junit = "androidx.test.ext:junit:1.1.1"
      const val core = "androidx.test:core:1.2.0"
    }
  }

  const val androidGradlePlugin = "com.android.tools.build:gradle:3.6.0"
  const val material = "com.google.android.material:material:1.2.0-alpha04"
  const val floatingActionButton = "com.getbase:floatingactionbutton:1.10.1"
  const val materialCab = "com.afollestad:material-cab:2.0.1"
  const val picasso = "com.squareup.picasso:picasso:2.71828"
  const val tapTarget = "com.getkeepsafe.taptargetview:taptargetview:1.13.0"
  const val chapterReader = "com.github.PaulWoitaschek:ChapterReader:0.1.4"
  const val lifecycle = "androidx.lifecycle:lifecycle-common-java8:2.2.0"
  const val groupie = "com.xwray:groupie:2.7.2"

  object MaterialDialog {
    private const val version = "3.1.1"
    const val core = "com.afollestad.material-dialogs:core:$version"
    const val input = "com.afollestad.material-dialogs:input:$version"
  }

  object Conductor {
    private const val version = "2.1.5"
    const val base = "com.bluelinelabs:conductor:$version"
    const val support = "com.bluelinelabs:conductor-support:$version"
  }

  const val crashlytics = "com.crashlytics.sdk.android:crashlytics:2.10.1@aar"
  const val fabricGradlePlugin = "io.fabric.tools:gradle:1.31.2"

  object Dagger {
    private const val version = "2.26"
    const val core = "com.google.dagger:dagger:$version"
    const val compiler = "com.google.dagger:dagger-compiler:$version"
  }

  object ExoPlayer {
    private const val extensionVersion = "2.11.1"
    const val core = "com.google.android.exoplayer:exoplayer-core:2.11.1"
    const val opus = "com.github.PaulWoitaschek.ExoPlayer-Extensions:extension-opus:$extensionVersion"
    const val flac = "com.github.PaulWoitaschek.ExoPlayer-Extensions:extension-flac:$extensionVersion"
  }

  const val moshi = "com.squareup.moshi:moshi:1.9.2"
  const val rxAndroid = "io.reactivex.rxjava2:rxandroid:2.1.1"
  const val rxJava = "io.reactivex.rxjava2:rxjava:2.2.17"
  const val rxPreferences = "com.f2prateek.rx.preferences2:rx-preferences:2.0.0"
  const val timber = "com.jakewharton.timber:timber:4.7.1"

  object Kotlin {
    private const val versionKotlin = "1.3.61"
    private const val versionCoroutines = "1.3.3"

    const val std = "org.jetbrains.kotlin:kotlin-stdlib:$versionKotlin"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$versionCoroutines"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$versionCoroutines"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$versionCoroutines"
    const val coroutinesRx = "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:$versionCoroutines"
    const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$versionKotlin"
  }

  const val junit = "junit:junit:4.13"
  const val mockk = "io.mockk:mockk:1.9.3"
  const val truth = "com.google.truth:truth:1.0.1"
  const val robolectric = "org.robolectric:robolectric:4.3.1"
}

@Suppress("UnstableApiUsage")
fun configureBaseRepos(repositoryHandler: RepositoryHandler) {
  repositoryHandler.apply {
    google()
        .mavenContent {
          includeGroupByRegex("androidx.*")
          includeGroupByRegex("com.google.*")
          includeGroupByRegex("com.android.*")
        }
    maven { setUrl("https://maven.fabric.io/public") }
        .mavenContent {
          includeGroup("io.fabric.tools")
          includeGroup("io.fabric.sdk.android")
          includeGroup("com.crashlytics.sdk.android")
        }
    maven { setUrl("https://jitpack.io") }
        .mavenContent {
          includeGroupByRegex("com.github.PaulWoitaschek.*")
        }
    mavenCentral()
        .mavenContent {
          includeGroup("javax.inject")
        }
    jcenter()
        .mavenContent { releasesOnly() }
  }
}
