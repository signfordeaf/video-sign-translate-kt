# WeSign Video Sign Translate
### Video Sign Translate makes a sign language plugin for your video player.
## 🛠️ Install
[![](https://jitpack.io/v/signfordeaf/video-sign-translate-kt.svg)](https://jitpack.io/#signfordeaf/video-sign-translate-kt)

 Step 1. Add the JitPack repository to your build file (settings.gradle)
```gradle
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```
  Step 2. Add the dependency
```gradle
dependencies {
	        implementation 'com.github.signfordeaf:video-sign-translate-kt:1.0.1'
	}
```

### Permission
Ensure that the following permission is present in your Android Manifest file, located in app/src/main/AndroidManifest.xml:
```
<uses-permission android:name="android.permission.INTERNET"/>
```

## 🧑🏻💻 Usage

### 📄 XML File
```kotlin
...
<com.weaccess.wesign.WeSign
        android:id="@+id/weSign"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
...
```

###  📄 Kotlin Class
   Associate your current Player with WeSign.
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WeAccessConfig.initialize("YOUR_API_KEY")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        weSign = findViewById(R.id.weSign)
        playerView = PlayerView(this)
        player = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(Uri.fromFile(YOUR-VİDEO-FİLE))
        player.setMediaItem(mediaItem)
        playerView.player = player
        weSign.setPlayer(playerView)
    }
}
```
