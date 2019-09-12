package com.kpchuck.videoeditor.utils

import java.util.concurrent.TimeUnit


class TimeUtils {

    companion object {

        fun formatTime(time: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(time) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60
            val milliseconds = time % 1000
            var format = String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, milliseconds)
            if (format.startsWith("00:"))
                format = format.drop(3)
            return format
        }
    }
}