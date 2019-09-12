package views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.media.MediaExtractor
import android.net.Uri
import android.os.Handler
import android.util.ArrayMap
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.VideoView
import com.bumptech.glide.Glide.init
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.kpchuck.videoeditor.R
import com.kpchuck.videoeditor.video.VideoUtils
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView
import java.io.File
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.decoder.DecoderCounters
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.video.VideoFrameMetadataListener
import com.google.android.exoplayer2.video.VideoRendererEventListener
import com.kpchuck.videoeditor.MainActivity
import kotlin.concurrent.thread
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class VideoViewer(private val videoContainer: FrameLayout, val file: String, val mainActivity: MainActivity): Player.EventListener {

    var cursor = 0
    val frameMap = ArrayMap<BaseEffectView, Bitmap>()
    private val videoChangeListeners = ArrayList<OnVideoChangeListener>()
    private var cuttingVideo = false
    var cuttingTimes = ArrayList<Pair<Int, Int>>()

    private val videoView = videoContainer.findViewById<PlayerView>(R.id.videoView)
    private val effectOverlayView = videoContainer.findViewById<ImageView>(R.id.imageView)

    private val player = ExoPlayerFactory.newSimpleInstance(
        videoContainer.context,
        DefaultRenderersFactory(videoContainer.context),
        DefaultTrackSelector(),
        DefaultLoadControl()
    )

    // Produces DataSource instances through which media data is loaded.
    private val dataSourceFactory = DefaultDataSourceFactory(videoContainer.context, Util.getUserAgent(videoContainer.context, "ExoPlayer"))
    // This is the MediaSource representing the media to be played.
    private val videoSource: MediaSource
        get() = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.fromFile(File(file)))


    private var playing = false
    private var cutTime = 0

    val dimens: Pair<Int, Int>
        get() = Pair(player.videoFormat!!.width, player.videoFormat!!.height)

    val seekPosition: Int
        get() {
            var position = 0L
            val timeline = player.currentTimeline
            val window = Timeline.Window()
            val currentWindow = player.currentWindowIndex
            for (i in 0 until timeline.windowCount) {
                if (i == currentWindow) {
                    position += player.contentPosition
                    break
                }
                position += timeline.getWindow(i, window).durationMs
            }

            Log.d("kpchuck", "Position seekbar is $position")
            return position.toInt()
        }

    val duration: Int
        get() {
            val timeline = player.currentTimeline ?: return player.duration.toInt()
            var duration = 0L
            var windows = Timeline.Window()
            for (i in 0 until timeline.windowCount) {
                val currentDuration = timeline.getWindow(i, windows).durationMs
                Log.d("duration-testing", "Duration for windows $i is $currentDuration")
                duration += currentDuration

            }
            return duration.toInt()
        }

    var fullDuration = 0L

    init {
        videoView.player = player
        videoView.keepScreenOn = true
        // Prepare the player with the source.
        player.addListener(this)
        player.prepare(videoSource)

        videoView.useController = false
    }

    fun stopPlaying(){
        playing = false
        player.playWhenReady = false
    }

    fun startPlaying(){
        playing = true
        player.playWhenReady = true
    }

    fun setFasterSeek(){
        player.seekParameters = SeekParameters.CLOSEST_SYNC
    }

    fun setAccurateSeek(){
        player.seekParameters = SeekParameters.EXACT
    }

    fun getFrameFromTime(time: Long): Int {
        return (time/player.videoFormat!!.frameRate).roundToInt()
    }

    fun addVideoChangeListener(listener: OnVideoChangeListener){
        videoChangeListeners.add(listener)
    }

    fun nextFrame(){
        seekTo(seekPosition + (player.videoFormat!!.frameRate).toInt())
    }

    fun prevFrame(){
        seekTo(seekPosition - (player.videoFormat!!.frameRate).toInt())
    }

    fun seekTo(position: Int){
        val timeline = player.currentTimeline ?: return
        val window = Timeline.Window()
        var counter = 0L
        for (i in 0 until timeline.windowCount){
            val currentDuration = timeline.getWindow(i, window).durationMs
            if (position < counter + currentDuration){
                while (player.currentWindowIndex < i)
                    player.next()
                while (player.currentWindowIndex > i)
                    player.previous()
                player.seekTo(position - counter)
                return
            }
            counter += currentDuration
        }
    }

    fun cutVideo(startFrame: Int, endFrame: Int){
        cuttingVideo = true
        var start = startFrame
        var end = endFrame
        if (startFrame > endFrame) {
            start = endFrame
            end = startFrame
        }
        // Need to shift start and end up to original times if there were any other cuts
        for (time in cuttingTimes){
            // If that cut was before this one -> shift it up by cut time
            if (time.first < start){
                val shift = time.second - time.first
                start += shift
                end += shift
            }
        }
        cuttingTimes.add(Pair(start, end))
        val newTimes = ArrayList<Pair<Int, Int>>()
        for (cutTimes in cuttingTimes){
            var notInRange = true
            for (otherTime in cuttingTimes){
                if (otherTime == cutTimes) continue
                if (cutTimes.first >= otherTime.first && cutTimes.second <= otherTime.second){
                    notInRange = false
                    break
                }
            }
            if (notInRange)
                newTimes.add(cutTimes)
        }
        cuttingTimes = newTimes

        var startCutTime = 0L
        val src = ConcatenatingMediaSource(true)
        Log.d("kpchuck", "Cuttimes are $cuttingTimes")
        for (time in cuttingTimes.sortedBy { pair: Pair<Int, Int> -> pair.first }){
            src.addMediaSource(ClippingMediaSource(videoSource, startCutTime * 1000, time.first.toLong() * 1000, false, false, false))
            startCutTime = time.second.toLong()
        }
        src.addMediaSource(ClippingMediaSource(videoSource, startCutTime * 1000, fullDuration * 1000, false, false, false))
        player.prepare(src)
        cutTime = startFrame
    }

    fun updateEffectImages(){
        if (frameMap.isEmpty()) {
            return
        }
        val format = player.videoFormat!!
        val bmOverlay = Bitmap.createBitmap(format.width, format.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmOverlay)
        for (overlays in frameMap.values) {
            overlays ?: continue
            canvas.drawBitmap(overlays, 0f, 0f, null)
        }
        effectOverlayView.setImageBitmap(bmOverlay)
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
    }

    var currentPosition = 0L

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> {
                 }
            ExoPlayer.STATE_IDLE -> {
            }
            ExoPlayer.STATE_READY -> {
                if (playWhenReady){
                    thread(isDaemon = true){
                        mainActivity.runOnUiThread {
                            currentPosition = seekPosition.toLong()
                        }
                        while (playing){
                            Thread.sleep(EFFECT_UPDATE_TIME)
                            currentPosition += EFFECT_UPDATE_TIME
                            mainActivity.runOnUiThread {
                                for (i in videoChangeListeners)
                                    i.onFrameUpdated(currentPosition.toInt())
                                updateEffectImages()
                            }
                        }
                    }
                    return
                }

                for (i in videoChangeListeners)
                    i.onFrameUpdated(seekPosition)
                if (cuttingVideo){
                    for (i in videoChangeListeners)
                        i.onFrameCut(duration)
                    cuttingVideo = false
                    seekTo(cutTime)
                }
                updateEffectImages()
                if (fullDuration == 0L){
                    fullDuration = player.duration
                }
            }
            ExoPlayer.STATE_ENDED -> {
            }
        }//You can use progress dialog to show user that video is preparing or buffering so please wait
        //idle state
        // dismiss your dialog here because our video is ready to play now
        // do your processing after ending of video
    }

    override fun onLoadingChanged(isLoading: Boolean) {
    }


    interface OnVideoChangeListener {
        fun onFrameCut(newDuration: Int)
        fun onFrameUpdated(currentPosition: Int)
    }

    companion object {
        const val EFFECT_UPDATE_TIME = 50L
    }

}