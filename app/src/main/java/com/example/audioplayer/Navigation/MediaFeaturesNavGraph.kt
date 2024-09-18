package com.example.audioplayer.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.audioplayer.AudioPlayer.AudioScreen
import com.example.audioplayer.AudioRecorder.AudioRecorderScreen
import com.example.audioplayer.HomeScreen.HomeScreen

@Composable
fun NavigationHost(){
    val navHostController = rememberNavController()
    NavHost(navController = navHostController, startDestination = MediaPlayerScreens.HomeScreen.route){
        composable(
            route = MediaPlayerScreens.HomeScreen.route
        ){
            HomeScreen(
                moveToAudioPlayerScreen = {
                    navHostController.navigate(MediaPlayerScreens.AudioPlayer.route)
                },
                moveToAudioRecorderScreen = {
                    navHostController.navigate(MediaPlayerScreens.AudioRecorder.route)
                }
            )
        }
        composable(
            route = MediaPlayerScreens.AudioPlayer.route
        ){
            AudioScreen(
                moveToHomeScreen = {
                   navHostController.popBackStack()
                }
            )
        }
        composable(
            route = MediaPlayerScreens.AudioRecorder.route
        ){
            AudioRecorderScreen()
        }
    }
}