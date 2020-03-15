package ru.freeze.watermark;


import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;

public class WatermarkVideoProcessor {


    private static final String INPUT_FILE_PATH = "src/main/resources/video.mp4";
    private static final String OUTPUT_FILE_PATH = "src/main/resources/output.mp4";


    public void run() throws Exception {

        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(INPUT_FILE_PATH);
        frameGrabber.setFormat("mp4");

        frameGrabber.start();


        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(OUTPUT_FILE_PATH, frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), frameGrabber.getAudioChannels());
        recorder.setFormat("mp4");

        recorder.setVideoCodec(frameGrabber.getVideoCodec());

        recorder.setFrameRate(frameGrabber.getFrameRate());
        recorder.setSampleFormat(frameGrabber.getSampleFormat());
        recorder.setSampleRate(frameGrabber.getSampleRate());

        System.out.println(frameGrabber.getFrameRate());

        recorder.start();
        System.out.println(frameGrabber.getLengthInFrames());
        frameGrabber.setFrameNumber(1000);
        WatermarkOnImageProcessor watermarkOnImageProcessor = new WatermarkOnImageProcessor();
        Frame frame = null;
        while ((frame = frameGrabber.grab()) != null) {


            recorder.setTimestamp(frameGrabber.getTimestamp());
            System.out.println("frame " + frameGrabber.getFrameNumber());
            recorder.record(watermarkOnImageProcessor.processImage(frame));
        }
        frameGrabber.stop();
        recorder.stop();


    }


    public void multiThreadRun() throws FrameGrabber.Exception, InterruptedException, FrameRecorder.Exception {

        final WatermarkOnImageProcessor watermarkOnImageProcessor = new WatermarkOnImageProcessor();
        final FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(INPUT_FILE_PATH);
        frameGrabber.setFormat("mp4");

        Collection<Frame> frames = new ArrayList<>();
        final Collection<Frame> framesT2 = new ArrayList<>();
        final Collection<Frame> framesT3 = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(2);



        frameGrabber.start();
        int countFrames = frameGrabber.getLengthInFrames();
        final int countFramesForThreads = (int) countFrames / 3;

        Thread second = new Thread(new Runnable() {
            public void run() {
                FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(INPUT_FILE_PATH);
                frameGrabber.setFormat("mp4");
                try {
                    frameGrabber.start();
                    frameGrabber.setFrameNumber(countFramesForThreads);

                    Frame frame = null;
                    while (
                            frameGrabber.getFrameNumber() < countFramesForThreads * 2 &&
                                    (frame = frameGrabber.grab()) != null) {
                        framesT2.add(watermarkOnImageProcessor.processImage(frame));
                        System.out.println("frame " + frameGrabber.getFrameNumber());
                    }
                    frameGrabber.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        });

        Thread third = new Thread(new Runnable() {
            public void run() {
                FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(INPUT_FILE_PATH);
                frameGrabber.setFormat("mp4");
                try {
                    /*frameGrabber.start();
                    frameGrabber.setFrameNumber(countFramesForThreads * 2);

                    Frame frame = null;
                    while ((frame = frameGrabber.grab()) != null) {
                        framesT3.add(watermarkOnImageProcessor.processImage(frame));
                        System.out.println("frame " + frameGrabber.getFrameNumber());
                    }
                    frameGrabber.stop();
                    */
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            }
        });

        second.start();
        third.start();
/*
        frameGrabber.setFrameNumber(0);

        Frame frame = null;
        while (frameGrabber.getFrameNumber() < countFramesForThreads &&
                (frame = frameGrabber.grab()) != null) {
            frames.add(watermarkOnImageProcessor.processImage(frame));
            System.out.println("frame " + frameGrabber.getFrameNumber());
        }
        */
        frameGrabber.stop();

        latch.await();

        frames.addAll(framesT2);
        frames.addAll(framesT3);

        final FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(OUTPUT_FILE_PATH, frameGrabber.getImageWidth(), frameGrabber.getImageHeight(), frameGrabber.getAudioChannels());
        recorder.setFormat("mp4");

        recorder.setVideoCodec(frameGrabber.getVideoCodec());

        recorder.setFrameRate(frameGrabber.getFrameRate());
        recorder.setSampleFormat(frameGrabber.getSampleFormat());
        recorder.setSampleRate(frameGrabber.getSampleRate());

        recorder.start();

        frames.forEach(f -> {
            try {
                recorder.record(f);
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        });
        recorder.stop();

    }


}
