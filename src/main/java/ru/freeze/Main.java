package ru.freeze;


import ru.freeze.watermark.WatermarkVideoProcessor;

public class Main{

    public static void main(String[] args) throws Exception {

        WatermarkVideoProcessor processor = new WatermarkVideoProcessor();

        processor.run();
    }

}