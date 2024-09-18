package com.example.audioplayer.Navigation

import android.provider.MediaStore.Audio.Media

sealed class MediaPlayerScreens(val route :String){
    object AudioPlayer : MediaPlayerScreens(route = MediaRoutes.AUDIO_PLAYER_SCREEN)
    object AudioRecorder : MediaPlayerScreens(route = MediaRoutes.AUDIO_RECORDER_SCREEN)
    object HomeScreen : MediaPlayerScreens(route = MediaRoutes.HOME_SCREEN)
}