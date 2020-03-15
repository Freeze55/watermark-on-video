package ru.freeze.watermark;



import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class WatermarkVideoProcessor {


    private static final String INPUT_FILE_PATH = "src/main/resources/video.mp4";
    private static final String OUTPUT_FILE_PATH = "src/main/resources/output.mp4";


    public void run() throws Exception {

        FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(INPUT_FILE_PATH);
        frameGrabber.setFormat("mp4");

        frameGrabber.start();


        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(OUTPUT_FILE_PATH, frameGrabber.getImageWidth(), frameGrabber.getImageHeight(),frameGrabber.getAudioChannels());
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



    public void multiThreadRun(){


    }


}
