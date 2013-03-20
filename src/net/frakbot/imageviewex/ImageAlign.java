package net.frakbot.imageviewex;

/**
 * Enum that contains image alignments for ImageViewEx.
 * 
 * @author Sebastiano Poggi, Francesco Pontillo
 *
 * @deprecated Use ScaleType.FIT_START and ScaleType.FIT_END instead.
 */
public enum ImageAlign {
    /**
     * No forced alignment. Image will be placed where the
     * scaleType dictates it to.
     */
    NONE,

    /**
     * Force top alignment: the top edge is aligned with
     * the View top.
     */
    TOP
}
