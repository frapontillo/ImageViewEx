package net.phoenix.imageviewex;

import android.graphics.Bitmap;

public class GifFrame {
    public Bitmap image;
    public int delay;
    public String imageName = null;

    public GifFrame nextFrame = null;

    public GifFrame(Bitmap im, int del) {
        this.image = im;
        this.delay = del;
    }

    public GifFrame(String name, int del) {
        this.imageName = name;
        this.delay = del;
    }
}