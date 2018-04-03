package com.hw.videoprocessor;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaMetadataRetriever;
import com.hw.videoprocessor.util.CL;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by huangwei on 2018/3/8 0008.
 */

public class VideoEffects {

    /**
     * 鬼畜效果，先按speed倍率对视频进行加速，然后按splitTimeMs分割视频，并对每一个片段做正放+倒放
     */
    public static void doKichiku(Context context, String inputVideo, String outputVideo, float speed, int splitTimeMs) throws IOException {
        File cacheDir = new File(context.getCacheDir(), "kichiku_" + System.currentTimeMillis());
        cacheDir.mkdir();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(inputVideo);
        int oriBitrate = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
        retriever.release();
        File speedVideo = new File(cacheDir, "speed_" + speed + ".tmp");
        VideoProcessor.processVideo(context, inputVideo, speedVideo.getAbsolutePath(), null, null, null, null,
                speed, null, null);
        int bitrate = VideoUtil.getBitrateForAllKeyFrameVideo(inputVideo);
        List<File> fileList;
        try {
            fileList = VideoUtil.splitVideo(context, speedVideo.getAbsolutePath(), cacheDir.getAbsolutePath(), splitTimeMs, 500, bitrate, 0);
        } catch (MediaCodec.CodecException e) {
            CL.e(e);
            /** Nexus5上-1代表全关键帧*/
            fileList = VideoUtil.splitVideo(context, speedVideo.getAbsolutePath(), cacheDir.getAbsolutePath(), splitTimeMs, 500, bitrate, -1);
        }
        File cacheCombineFile = new File(cacheDir, "combine_" + System.currentTimeMillis() + ".tmp");
        VideoUtil.combineVideos(fileList, cacheCombineFile.getAbsolutePath());
        VideoProcessor.processVideo(context, cacheCombineFile.getAbsolutePath(), outputVideo, null, null, null, null, null,
                oriBitrate, VideoProcessor.DEFAULT_I_FRAME_INTERVAL);
        cacheCombineFile.delete();
    }
}
