import deps.Deps

plugins {
  id("com.android.library")
  id("kotlin-android")
  id("kotlin-kapt")
}

android {
  flavorDimensions("free")
  productFlavors {
    create("opensource") {
      dimension = "free"
    }
    create("proprietary") {
      dimension = "free"
    }
  }
}

dependencies {
  implementation(project(":common"))
  implementation(project(":core"))
  implementation(project(":data"))
  implementation(project(":crashreporting"))
  implementation(project(":prefs"))

  implementation(Deps.timber)
  implementation(Deps.Kotlin.coroutines)
  implementation(Deps.AndroidX.mediaCompat)
  implementation(Deps.picasso)
  implementation(Deps.AndroidX.ktx)
  implementation(Deps.Prefs.core)

  implementation(Deps.Dagger.core)
  kapt(Deps.Dagger.compiler)

  implementation(Deps.ExoPlayer.core)
  implementation(Deps.ExoPlayer.flac) { isTransitive = false }
}
