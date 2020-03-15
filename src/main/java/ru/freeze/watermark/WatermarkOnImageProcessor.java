package ru.freeze.watermark;

import org.bytedeco.javacv.Java2DFrameUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class WatermarkOnImageProcessor {

    private static final String WATERMARK_PICTURE = "src/main/resources/watermark.png";

    public org.bytedeco.javacv.Frame processImage(org.bytedeco.javacv.Frame frame) throws InterruptedException {
        try {
            final CountDownLatch latch = new CountDownLatch(2);


            File watermarkFile = new File(WATERMARK_PICTURE);
            final BufferedImage source = Java2DFrameUtils.toBufferedImage(frame);
            final BufferedImage watermark = resizeImage(ImageIO.read(watermarkFile), 0.3);


            if (watermark.getHeight() < source.getHeight() + 10 && watermark.getWidth() < source.getWidth() + 10) {
                synchronized (source) {

                    final BufferedImage finalWatermark = watermark;

                    Thread one = new Thread(new Runnable() {
                        public void run() {

                            for (int x = source.getWidth() - finalWatermark.getWidth() - 10; x < source.getWidth() - 10; x += 2) {
                                for (int y = source.getHeight() - finalWatermark.getHeight() - 10; y < source.getHeight() - 10; y++) {

                                    Color colorWatermark = new Color(finalWatermark.getRGB(
                                            (x - source.getWidth() + finalWatermark.getWidth() + 10),
                                            (y - source.getHeight() + finalWatermark.getHeight() + 10)));

                                    Color colorSource = new Color(source.getRGB(x, y));


                                    int r = (int) (colorSource.getRed() + (colorWatermark.getRed() - colorSource.getRed()) * 0.3);
                                    int g = (int) (colorSource.getGreen() + (colorWatermark.getGreen() - colorSource.getGreen()) * 0.3);
                                    int b = (int) (colorSource.getBlue() + (colorWatermark.getBlue() - colorSource.getBlue()) * 0.3);
                                    Color resultColor = new Color(r, g, b);

                                    if (colorWatermark.getRGB() != -16777216) //avoid black
                                        source.setRGB(x, y, resultColor.getRGB()
                                        );


                                }
                            }

                            latch.countDown();
                        }
                    });

                    Thread second = new Thread(new Runnable() {
                        public void run() {

                            for (int x = source.getWidth() - finalWatermark.getWidth() - 10 + 1; x < source.getWidth() - 10; x += 2) {
                                for (int y = source.getHeight() - finalWatermark.getHeight() - 10; y < source.getHeight() - 10; y++) {

                                    Color colorWatermark = new Color(finalWatermark.getRGB(
                                            (x - source.getWidth() + finalWatermark.getWidth() + 10),
                                            (y - source.getHeight() + finalWatermark.getHeight() + 10)));

                                    Color colorSource = new Color(source.getRGB(x, y));


                                    int r = (int) (colorSource.getRed() + (colorWatermark.getRed() - colorSource.getRed()) * 0.3);
                                    int g = (int) (colorSource.getGreen() + (colorWatermark.getGreen() - colorSource.getGreen()) * 0.3);
                                    int b = (int) (colorSource.getBlue() + (colorWatermark.getBlue() - colorSource.getBlue()) * 0.3);
                                    Color resultColor = new Color(r, g, b);

                                    if (colorWatermark.getRGB() != -16777216) //avoid black
                                        source.setRGB(x, y, resultColor.getRGB()
                                        );


                                }
                            }
                            latch.countDown();
                        }
                    });

                    one.start();
                    second.start();



                }
                latch.await();

                return Java2DFrameUtils.toFrame(source);

            } else
                System.err.println("Файл водяного знака больше размера изображения");


        } catch (IOException e) {


            System.out.println("Файл не найден или не удалось сохранить");}

        return null;
    }


    private BufferedImage resizeImage(BufferedImage image, double resizeIndex) {

        int scaledWidth = (int) (image.getWidth() * resizeIndex);
        int scaledHeight = (int) (image.getHeight() * resizeIndex);
        BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, image.getType());


        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(image, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return outputImage;

    }

}
