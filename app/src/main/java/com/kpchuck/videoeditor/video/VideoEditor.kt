package com.kpchuck.videoeditor.video

import java.nio.ByteBuffer
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMetadataRetriever
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.annotation.TargetApi
import android.content.Context
import android.util.Log
import android.util.SparseIntArray
import android.widget.Toast
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler
import com.kpchuck.videoeditor.views.efffectviews.BaseEffectView
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class VideoEditor(val tempFolder: File, val context: Context) {

    companion object {
        const val TAG = "kpchuck.VideoEditor"
    }

    private fun cleanupTempFile(){
        for (file in tempFolder.listFiles { f, _ -> f.extension.endsWith("mp4") }) {
            file.delete()
        }
    }

    // Trims video for each cutting time
    // Merges all the videos together
    fun cutVideo(srcPath: String, dstPath: String, cuttingTimes: ArrayList<Pair<Int, Int>>, onFinished: () -> Unit){
        if (cuttingTimes.isEmpty()){
            FileUtils.copyFile(File(srcPath), File(dstPath))
            onFinished()
            return
        }
        cleanupTempFile()
        var startCutTime = 0
        val cutVideoStreams = ArrayList<File>()
        Log.d("kpchuck", "Cuttimes are $cuttingTimes")
        // That is a trippy for loop :)
        // Loops through sorted cut times keeping track of index
        val extractor = MediaExtractor()
        extractor.setDataSource(srcPath)
        for ((i, time) in cuttingTimes.sortedBy { pair: Pair<Int, Int> -> pair.first }.withIndex()){
            if (startCutTime != time.first) {
                trimVideo(extractor, srcPath, File(tempFolder, "temp_movie_$i.mp4").absolutePath, startCutTime, time.first)
                val inputStream = File(tempFolder, "temp_movie_$i.mp4")
                cutVideoStreams.add(inputStream)
            }
            startCutTime = time.second
        }
        trimVideo(extractor, srcPath, File(tempFolder, "temp_movie_final.mp4").absolutePath, startCutTime, -1)
        val inputStream = File(tempFolder, "temp_movie_final.mp4")
        cutVideoStreams.add(inputStream)

        combineVideoStart(cutVideoStreams, dstPath, onFinished)
    }

    fun createVideoFromView(view: BaseEffectView, duration: Int, dimens: Pair<Int, Int>, outFile: String){
        thread(isDaemon = false) {
            // duration is the number of milliseconds
            // So eg 30 second video has 30000
            // So convert to seconds ... and multiply by 30 to get num total frames in vidoe
            val numFrames = (duration / 1000) * 30
            Log.d(TAG, "Duration of vidoe is $duration")
            val bitmapEncoder = BitmapToVideoEncoder(object : BitmapToVideoEncoder.IBitmapToVideoEncoderCallback {
                override fun onEncodingComplete(outputFile: File?) {
                    Log.d(TAG, "Creating video finsihed")
                }
            })
            bitmapEncoder.startEncoding(dimens.first, dimens.second, File(outFile))
            for (i in 0..duration step (33 + 1 / 3)) {
                if (bitmapEncoder.activeBitmaps > 100) {
                    Thread.sleep(100)
                    Log.d(TAG, "Slept until ${bitmapEncoder.activeBitmaps} bitmaps. Currently on second ${i/1000}")
                }
                val bitmap = view.getFrame(i) ?: continue
                bitmapEncoder.queueFrame(bitmap)
            }
            bitmapEncoder.stopEncoding()
        }
    }

    private fun combineVideoStart(videoStreams: ArrayList<File>, dstPath: String, onFinished: () -> Unit){
        val ffmpeg = FFmpeg.getInstance(context)
        ffmpeg.loadBinary(object: FFmpegLoadBinaryResponseHandler{
            override fun onFinish() {
            }

            override fun onSuccess() {
                combineVideo(videoStreams, dstPath, ffmpeg!!, onFinished)
            }

            override fun onFailure() {
            }

            override fun onStart() {
            }
        })
    }

    private fun combineVideo(videoStreams: ArrayList<File>, dstPath: String, fFmpeg: FFmpeg, onFinished: () -> Unit){
        // -f concat -i mylist.txt -c copy output.mp4
        val inputFiles = ArrayList<String>()
        videoStreams.forEach { inputFiles.add("file '${it.absolutePath}'") }
        val inputs = File(tempFolder, "input2.txt")
        inputs.delete()
        FileUtils.writeLines(inputs, inputFiles)
        val cmd = "-y -f concat -safe 0 -i ${tempFolder.absolutePath}/input2.txt -c copy $dstPath"
        Log.d(TAG, "FFmpeg command is $cmd")
        fFmpeg.execute(cmd.split(" ").toTypedArray(), object : FFmpegExecuteResponseHandler{
            override fun onFinish() {
                Log.d(TAG, "Finished concating video")
            }

            override fun onStart() {
                Log.d(TAG, "Started concating video")
            }

            override fun onSuccess(message: String?) {
                Log.d(TAG, "Successfully copied all files: $message")
                onFinished()
            }

            override fun onFailure(message: String?) {
                Log.d(TAG, "Error: $message")
            }

            override fun onProgress(message: String?) {
                Log.d(TAG, "Progress: $message")
            }
        })
    }

    /**
     * @param srcPath  the path of source video file.
     * @param dstPath  the path of destination video file.
     * @param startMs  starting time in milliseconds for trimming. Set to
     * negative if starting from beginning.
     * @param endMs    end time for trimming in milliseconds. Set to negative if
     * no trimming at the end.
     * @param useAudio true if keep the audio track from the source.
     * @param useVideo true if keep the video track from the source.
     * @throws IOException
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Throws(IOException::class)
    fun trimVideo(
        extractor: MediaExtractor,
        srcPath: String, dstPath: String,
        startMs: Int, endMs: Int, useAudio: Boolean = true, useVideo: Boolean = true
    ) {
        // Set up MediaExtractor to read from the source.
        val trackCount = extractor.trackCount
        // Set up MediaMuxer for the destination.
        val muxer = MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        // Set up the tracks and retrieve the max buffer size for selected
        // tracks.
        val indexMap = SparseIntArray(trackCount)
        var bufferSize = -1
        for (i in 0 until trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            var selectCurrentTrack = false
            if (mime.startsWith("audio/") && useAudio) {
                selectCurrentTrack = true
            } else if (mime.startsWith("video/") && useVideo) {
                selectCurrentTrack = true
            }
            if (selectCurrentTrack) {
                extractor.selectTrack(i)
                val dstIndex = muxer.addTrack(format)
                indexMap.put(i, dstIndex)
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    val newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    bufferSize = if (newSize > bufferSize) newSize else bufferSize
                }
            }
        }
        if (bufferSize < 0) {
            bufferSize = DEFAULT_BUFFER_SIZE
        }
        // Set up the orientation and starting time for extractor.
        val retrieverSrc = MediaMetadataRetriever()
        retrieverSrc.setDataSource(srcPath)
        val degreesString = retrieverSrc.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION
        )
        if (degreesString != null) {
            val degrees = Integer.parseInt(degreesString)
            if (degrees >= 0) {
                muxer.setOrientationHint(degrees)
            }
        }
        if (startMs > 0) {
            extractor.seekTo((startMs * 1000).toLong(), MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        }
        // Copy the samples from MediaExtractor to MediaMuxer. We will loop
        // for copying each sample and stop when we get to the end of the source
        // file or exceed the end time of the trimming.
        val offset = 0
        var trackIndex = -1
        val dstBuf = ByteBuffer.allocate(bufferSize)
        val bufferInfo = MediaCodec.BufferInfo()
        try {
            muxer.start()
            while (true) {
                bufferInfo.offset = offset
                bufferInfo.size = extractor.readSampleData(dstBuf, offset)
                if (bufferInfo.size < 0) {
                    Log.d(TAG, "Saw input EOS.")
                    bufferInfo.size = 0
                    break
                } else {
                    bufferInfo.presentationTimeUs = extractor.sampleTime
                    //Log.d(TAG, "Current time is ${bufferInfo.presentationTimeUs}")
                    if (endMs > 0 && bufferInfo.presentationTimeUs > endMs * 1000) {
                        Log.d(TAG, "The current sample is over the trim end time.")
                        break
                    } else {
                        bufferInfo.flags = extractor.sampleFlags
                        trackIndex = extractor.sampleTrackIndex
                        muxer.writeSampleData(
                            indexMap.get(trackIndex), dstBuf,
                            bufferInfo
                        )
                        extractor.advance()
                    }
                }
            }
            muxer.stop()
        } catch (e: IllegalStateException) {
            // Swallow the exception due to malformed source.
            Log.w(TAG, "The source video file is malformed")
        } finally {
            muxer.release()
        }
        return
    }
}