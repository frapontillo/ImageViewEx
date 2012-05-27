package net.phoenix.imageviewex;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

public class CY_GifDrawable extends CY_BitmapDrawable implements GifParsingListener {

    private Semaphore semaphore = new Semaphore(0);
    private GifDecoder decoder;
    private Handler handler = new Handler();
    private boolean pause = false;

    public CY_GifDrawable(Resources res, byte[] data) {
        super(res);
        setTargetDensity(res.getDisplayMetrics().densityDpi);
        init(new ByteArrayInputStream(data));
    }

    public CY_GifDrawable(Resources res, InputStream is) {
        super(res);
        setTargetDensity(res.getDisplayMetrics().densityDpi);
        init(is);
    }

    public CY_GifDrawable(final Resources res, int resId) {
        super(res);
        setTargetDensity(res.getDisplayMetrics().densityDpi);
        init(res.openRawResource(resId));
    }

    public void init(InputStream is) {
        decoder = new GifDecoder(this);
        if (is == null)
            return;
        decoder.setGifImage(is);
        decoder.start();
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void free() {
        setBitmap(null);
        decoder.free();
        decoder = null;
    }

    @Override
    public void onGifParsingCompleted(boolean parseStatus, int frameIndex) {
        if (!parseStatus) {
            semaphore.release();
            return;
        }
        if (decoder == null) {
            semaphore.release();
            Log.e("gif", "parse error");
            return;
        }
        if (frameIndex == 1) {
            setBitmap(decoder.getImage());
            semaphore.release();
            handler.post(invalidateRunnable);
        } else if (frameIndex == -1) {
            if (decoder.getFramesCount() >= 1) {
                handler.removeCallbacks(drawRunnable);
                handler.post(drawRunnable);
            } else {
                semaphore.release();
            }
        }
    }

    private Runnable invalidateRunnable = new Runnable() {
        public void run() {
            invalidateSelf();
        }
    };

    private Runnable drawRunnable = new Runnable() {

        public void run() {
            if (decoder == null)
                return;
            if (decoder.getFramesCount() == 1) {
                GifFrame f = decoder.next();
                setBitmap(Bitmap.createBitmap(f.image));
                decoder.free();
                invalidateSelf();
                return;
            }
            if (pause) {
                handler.removeCallbacks(this);
                handler.postDelayed(this, 50);
                return;
            }

            GifFrame frame = decoder.next();
            if (frame == null) {
                handler.removeCallbacks(this);
                handler.postDelayed(this, 50);
                return;
            }
            if (frame.image != null)
                setBitmap(frame.image);
            else if (frame.imageName != null) {
                setBitmap(BitmapFactory.decodeFile(frame.imageName));
            }
            invalidateSelf();
            long sp = frame.delay;
            handler.removeCallbacks(this);
            handler.postDelayed(this, sp);
        }
    };

    public void showCover() {
        if (decoder == null)
            return;
        pause = true;
        setBitmap(decoder.getImage());
        handler.post(invalidateRunnable);
    }

    public void showAnimation() {
        if (pause) {
            pause = false;
        }
    }

}
