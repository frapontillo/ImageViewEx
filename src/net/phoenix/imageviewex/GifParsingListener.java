package net.phoenix.imageviewex;

/**
 * An interface that implements a Gif parsing completion callback.
 */
public abstract interface GifParsingListener {

    /**
     * This method is called whenever the GifDecoder finishes parsing
     * a byte buffer.
     * TODO: complete this
     *
     * @param parseStatus
     * @param frameIndex
     */
    public abstract void onGifParsingCompleted(boolean parseStatus, int frameIndex);
}