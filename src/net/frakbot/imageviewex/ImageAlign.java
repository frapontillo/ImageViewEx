package net.frakbot.imageviewex;

/**
 * Enum that contains image alignments for ImageViewEx.
 * <p/>
 * Author: Sebastiano Poggi
 * Created on: 29/11/12 Time: 17.21
 * File version: 1.0
 * <p/>
 * Changelog:
 * Version 1.0
 * * Initial revision
 */
public enum ImageAlign {
    /**
     * No forced alignment. Image will be placed where the
     * scaleType dictates it to.
     */
    NONE,

    /**
     * Force top alignment: the image is fitted on the View
     * width and its top edge is aligned with the View top.
     */
    TOP
}
