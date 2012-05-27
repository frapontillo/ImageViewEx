package net.phoenix.imageviewex;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

/**
 * Class <code>GifDecoder</code> - Decodes a GIF file into one or more frames.
 * <br><pre>
 * Example:
 *    GifDecoder d = new GifDecoder();
 *    d.read("sample.gif");
 *    int n = d.getFrameCount();
 *    for (int i = 0; i < n; i++) {
 *       BufferedImage frame = d.getFrame(i);  // frame i
 *       int t = d.getDelay(i);  // display duration of frame in milliseconds
 *       // do something with frame
 *    }
 * </pre>
 * <p/>
 * No copyright asserted on the source code of this class.  May be used for
 * any purpose, however, refer to the Unisys LZW patent for any additional
 * restrictions. Please forward any corrections to questions at fmsware.com.
 * <p/>
 * Original GifDecoder URL: http://www.fmsware.com/stuff/GifDecoder.java
 * Liao's GifView URL: http://code.google.com/p/gifview/
 * Sebastiano Poggi's URL: https://github.com/frapontillo/ImageViewEx
 *
 * @author Kevin Weiner, FM Software; LZW decoder adapted from John Cristy's ImageMagick.
 * @author liao
 * @author Sebastiano Poggi
 * @version 3.0 May 2012
 */
public class GifDecoder extends Thread {

    //////////////////////////////////////////////
    //             DECODING STATUSES            //
    //////////////////////////////////////////////

    /**
     * Status: image successfully decoded, ready to parse other
     * images. This is the starting status.
     */
    public static final int STATUS_FINISH = -1;
    /**
     * Status: decoding
     */
    public static final int STATUS_PARSING = 0;
    /**
     * Status: decoding failed due to an image format error
     */
    public static final int STATUS_FORMAT_ERROR = 1;
    /**
     * Status: Failed to open the image
     */
    public static final int STATUS_OPEN_ERROR = 2;



    //////////////////////////////////////////////
    //              PRIVATE FIELDS              //
    //////////////////////////////////////////////
    private InputStream mInputStream;
    private int mStatus;

    // IMAGE INFORMATION
    private int mWidth;             // The image width
    private int mHeight;            // The image height
    private int mPixelAR;           // The image pixel aspect ratio (PAR)
    private boolean mInterlaced;    // Flag that indicates if the image is interlaced
    private boolean mIsPlaying;     // Flag that indicates if the image is playing

    // COLOR TABLES (PALETTES)
    private int[] mGct;             // The Global Color Table
    private int mGctSize;           // The Global Color Table size
    private boolean mGctUsedFlag;   // Flag that indicates if the Global Color Table is being used

    private int[] mLct;             // The Local Color Table
    private int mLctSize;           // The Local Color Table size
    private boolean mLctFlag;       // Flag that indicates if the Local Color Table is being used

    private int[] mAct;             // The Active Color Table

    // BACKGROUND COLOR INFORMATION
    private int mBgColorIndex;      // The background color index
    private int mBgColor;           // The background color
    private int mLastBgColor;       // The previous background color

    // DRAWING INFORMATION
    private Rect mCurrDrawRect;     // The current image rectangle TODO: use this instead of four ints
    private Rect mLastDrawRect;     // The last image rectangle TODO: use this instead of four ints
    private int mCurrX, mCurrY, mCurrWidth, mCurrHeight;    // The current image rectangle
    private int mLastX, mLastY, mLastWidth, mLastHeight;    // The last image rectangle
    private Bitmap mCurrImg;        // The current image frame
    private Bitmap mLastImg;        // The previous image frame
    private GifFrame mCurrentFrame; // The current frame TODO: determine if it's a duplicate of mCurrImg

    // DECODER DATA
    private byte[] mBlock;          // The current data block
    private int mBlockSize;         // The current data block size
    private byte[] mGifData;        // The Gif data

    // GRAPHIC CONTROL EXTENSION INFORMATION
    // [0 = no action; 1 = leave in place; 2 = restore to bg; 3 = restore to prev]
    private int mDisposeCode;       // The current Dispose Code
    private int mLastDisposeCode;   // The last Dispose Code
    private boolean mTransparency;  // Flag that indicates if we should use a transparent color
    private int mFrameDuration = 0;         // The next image delay, in milliseconds
    private int mTranspIndex;       // The transparent color index

    // DECODER MAX PIXEL STACK SIZE
    private static final int MAX_STACK_SIZE = 4096;

    // LZW DECODER WORKING ARRAYS
    private short[] mPrefix;
    private byte[] mSuffix;
    private byte[] mPixelStack;
    private byte[] mPixels;

    // ANIMATION DATA
    private GifFrame mFirstGifFrame;     // The frames read from current file TODO: check if this duplicates mCurrFrame
    private int mFramesCount;       // The number of frames in the image
    private int mLoopCount;         // The number of animation iterations; "0" means repeat forever. Default is 1

    // CALLBACKS
    private GifParsingListener mParsingListener;    // The attached parsing listener


    // CACHING STUFF - TODO: remove this crap
    private String imagePath = null;    // The path of the image cache
    private boolean cacheImage = false; // Indicates wether we want to cache stuff


    //////////////////////////////////////////////
    //               CONSTRUCTORS               //
    //////////////////////////////////////////////

    /**
     * Initializes an instance of the GifDecoder.
     * <br/>
     * It's strongly recommended that you set a parsing
     * completion listener before beginning parsing images.
     */
    public GifDecoder() {
        mIsPlaying = false;
        mBlock = new byte[256];
        mBlockSize = 0;
        mTransparency = false;
        mCurrentFrame = null;
        mDisposeCode = 0;
        mLastDisposeCode = 0;
        mLoopCount = 1;
        mGifData = null;
    }

    /**
     * Initializes an instance of the Gif Decoder specifying
     * the parsing completion listener.
     *
     * @param parsingListener The parsing completion listener.
     */
    public GifDecoder(GifParsingListener parsingListener) {
        this();
        mParsingListener = parsingListener;
    }


    //////////////////////////////////////////////
    //              PUBLIC SETTERS              //
    //////////////////////////////////////////////

    /**
     * Loads a Gif image from a byte array.
     *
     * @param gifData The Gif image data bytes.
     */
    public void loadGif(byte[] gifData) {
        mGifData = gifData;
        mInputStream = null;
        run();
    }

    /**
     * Loads a Gif image from an input string.
     *
     * @param gifData The Gif image data stream.
     */
    public void setGifImage(InputStream gifData) {
        mInputStream = gifData;
        mGifData = null;
        run();
    }


    //////////////////////////////////////////////
    //              PUBLIC GETTERS              //
    //////////////////////////////////////////////

    /**
     * Gets the current parsing status.
     *
     * @return Returns a value indicating the current parsing status.
     */
    public int getStatus() {
        return mStatus;
    }

    /**
     * Gets a value indicating wether the last parsing has been successfully
     * completed. Will also return true if no parsing has still been attempted.
     *
     * @return Returns true if the parsing was successfully completed, false
     *         otherwise.
     */
    public boolean hasParsingSucceeded() {
        return getStatus() == STATUS_FINISH;
    }

    /**
     * Gets the duration of the n-th frame.
     *
     * @param n The index of the frame.
     * @return The duration of the frame, in milliseconds.
     */
    public int getDuration(int n) {
        mFrameDuration = -1;
        if ((n >= 0) && (n < mFramesCount)) {
            GifFrame f = getFrame(n);
            if (f != null)
                mFrameDuration = f.delay;
        }
        return mFrameDuration;
    }

    /**
     * Gets all frames durations.
     *
     * @return Returns an array of frames durations.
     */
    public int[] getDurations() {
        GifFrame f = getFrame(0);
        int[] d = new int[mFramesCount];
        int i = 0;
        while (f != null && i < mFramesCount) {
            d[i] = f.delay;
            f = f.nextFrame;
            i++;
        }
        return d;
    }


    /**
     * Gets the total animation loop duration.
     *
     * @return Returns the total duration of a single animation loop.
     */
    public int getTotalDuration() {
        GifFrame f = getFrame(0);
        int tot = 0;
        while (f != null) {
            tot += f.delay;
            f = f.nextFrame;
        }
        return tot;
    }


    /**
     * Gets the number of frames in the image.
     *
     * @return Returns the number of frames in the image.
     */
    public int getFramesCount() {
        return mFramesCount;
    }

    /**
     * Gets the number of animation loops.
     *
     * @return Returns the number of animation loops.
     */
    public int getLoopCount() {
        return mLoopCount;
    }

    /**
     * Gets the decoded image or, if it's an animated Gif, the first frame.
     *
     * @return Returns the decoded image.
     */
    public Bitmap getImage() {
        return getFrameImage(0);
    }

    /**
     * Gets the specified image frame Bitmap.
     *
     * @param n The index of the frame.
     * @return Returns the image frame Bitmap, or null if the frame
     *         doesn't exist.
     */
    public Bitmap getFrameImage(int n) {
        GifFrame frame = getFrame(n);
        return (frame != null ? frame.image : null);
    }

    /**
     * Gets the currently displayed frame.
     *
     * @return Returns the currently displayed frame.
     */
    public GifFrame getCurrentFrame() {
        return mCurrentFrame;
    }

    /**
     * Gets the specified image frame.
     *
     * @param n The index of the frame.
     * @return Returns the requested GifFrame.
     */
    public GifFrame getFrame(int n) {
        GifFrame frame = mFirstGifFrame;
        int i = 0;
        while (frame != null) {
            if (i == n) {
                return frame;
            } else {
                frame = frame.nextFrame;
            }
            i++;
        }
        return null;
    }



    //////////////////////////////////////////////
    //         PUBLIC DECODING METHODS          //
    //////////////////////////////////////////////

    /**
     *   TODO: check this pile of crap
     */
    private void setPixels() {
        int[] dest = new int[getWidth() * getImageHeight()];
        // fill in starting image contents based on last image's dispose code
        if (mLastDisposeCode > 0) {
            if (mLastDisposeCode == 3) {
                // use image before last
                int n = mFramesCount - 2;
                if (n > 0) {
                    mLastImg = getFrameImage(n - 1);
                } else {
                    mLastImg = null;
                }
            }
            if (mLastImg != null) {
                mLastImg.getPixels(dest, 0, getWidth(), 0, 0, getWidth(), getImageHeight());
                // copy pixels
                if (mLastDisposeCode == 2) {
                    // fill last image rect area with background color
                    int c = 0;
                    if (!mTransparency) {
                        c = mLastBgColor;
                    }
                    for (int i = 0; i < mLastHeight; i++) {
                        int n1 = (mLastY + i) * getWidth() + mLastX;
                        int n2 = n1 + mLastWidth;
                        for (int k = n1; k < n2; k++) {
                            dest[k] = c;
                        }
                    }
                }
            }
        }

        // copy each source line to the appropriate place in the destination
        int pass = 1;
        int inc = 8;
        int iline = 0;
        for (int i = 0; i < mCurrHeight; i++) {
            int line = i;
            if (mInterlaced) {
                if (iline >= mCurrHeight) {
                    pass++;
                    switch (pass) {
                        case 2:
                            iline = 4;
                            break;
                        case 3:
                            iline = 2;
                            inc = 4;
                            break;
                        case 4:
                            iline = 1;
                            inc = 2;
                    }
                }
                line = iline;
                iline += inc;
            }
            line += mCurrY;
            if (line < getImageHeight()) {
                int k = line * getWidth();
                int dx = k + mCurrX; // start of line in dest
                int dlim = dx + mCurrWidth; // end of dest line
                if ((k + getWidth()) < dlim) {
                    dlim = k + getWidth(); // past dest edge
                }
                int sx = i * mCurrWidth; // start of line in source
                while (dx < dlim) {
                    // map color and insert in destination
                    int index = ((int) mPixels[sx++]) & 0xff;
                    int c = mAct[index];
                    if (c != 0) {
                        dest[dx] = c;
                    }
                    dx++;
                }
            }
        }

        mCurrImg = Bitmap.createBitmap(dest, getWidth(), getImageHeight(), Config.ARGB_4444);
    }



    // TODO: remove Thread extension and use a private working thread instead
    public void run() {
        if (mInputStream != null) {
            readStream();
        } else if (mGifData != null) {
            decodeByteArray();
        } else
            throw new IllegalStateException("Unable to start parsing, no image data loaded yet.");
    }

    /**
     * Releases all memory resources.
     */
    public void free() {
        // Free frames data
        while (mFirstGifFrame != null) {
            // Recycle the frame Bitmap and void references
            if (mFirstGifFrame.image != null && !mFirstGifFrame.image.isRecycled())
                mFirstGifFrame.image.recycle();

            mFirstGifFrame.image = null;

            // On to the next frame (if any)...
            mFirstGifFrame = mFirstGifFrame.nextFrame;
        }

        // Try closing the input stream if it's still references (shouldn't)
        if (mInputStream != null) {
            try {
                mInputStream.close();
            }
            catch (Exception ignored) {}
            finally {
                mInputStream = null;
            }
        }

        // Release all other resources and reset status
        mGifData = null;
        mStatus = 0;
        mCurrentFrame = null;
    }

    /**
     * Brings the animation back to the first frame.
     */
    public void rewind() {
        mCurrentFrame = mFirstGifFrame;
    }

    /**
     * Advances to the next frame in the animation and returns it.
     *
     * @return Returns the next frame.
     */
    public GifFrame next() {
        if (!mIsPlaying) {
            // Start playing if we weren't already playing, and return the first frame
            mIsPlaying = true;
            return mFirstGifFrame;
        }
        else {
            // Return null if we've not got a frame yet (or anymore)
            if (mCurrentFrame == null)
                return null;

            if (getStatus() == STATUS_PARSING) {
                // If we're still parsing, check if the next frame is available then advance
                if (mCurrentFrame.nextFrame != null) {
                    mCurrentFrame = mCurrentFrame.nextFrame;
                }
            } else {
                // Handle looping the animation
                mCurrentFrame = mCurrentFrame.nextFrame;
                if (mCurrentFrame == null) {
                    mCurrentFrame = mFirstGifFrame;
                }
            }
            return mCurrentFrame;
        }
    }


    //////////////////////////////////////////////
    //        PRIVATE DECODING METHODS          //
    //////////////////////////////////////////////

    /**
     * Decodes a Gif from a byte array.
     * @return Returns a parsing status.
     */
    private int decodeByteArray() {
        mInputStream = new ByteArrayInputStream(mGifData);
        mGifData = null;
        return readStream();
    }

    /**
     * Decodes a Gif from an input stream.
     * @return Returns a parsing status.
     */
    private int readStream() {
        init();
        if (mInputStream != null) {
            readHeader();
            if (!err()) {
                readContents();
                if (mFramesCount < 0) {
                    mStatus = STATUS_FORMAT_ERROR;
                    mParsingListener.onGifParsingCompleted(false, -1);
                } else {
                    mStatus = STATUS_FINISH;
                    mParsingListener.onGifParsingCompleted(true, -1);
                }
            }
            try {
                mInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            mStatus = STATUS_OPEN_ERROR;
            mParsingListener.onGifParsingCompleted(false, -1);
        }
        return getStatus();
    }

    private void decodeImageData() {
        int NullCode = -1;
        int npix = mCurrWidth * mCurrHeight;
        int available, clear, code_mask, code_size, end_of_information, in_code, old_code, bits, code, count, i, datum, data_size, first, top, bi, pi;

        if ((mPixels == null) || (mPixels.length < npix)) {
            mPixels = new byte[npix]; // allocate new pixel array
        }
        if (mPrefix == null) {
            mPrefix = new short[MAX_STACK_SIZE];
        }
        if (mSuffix == null) {
            mSuffix = new byte[MAX_STACK_SIZE];
        }
        if (mPixelStack == null) {
            mPixelStack = new byte[MAX_STACK_SIZE + 1];
        }
        // Initialize GIF data stream decoder.
        data_size = read();
        clear = 1 << data_size;
        end_of_information = clear + 1;
        available = clear + 2;
        old_code = NullCode;
        code_size = data_size + 1;
        code_mask = (1 << code_size) - 1;
        for (code = 0; code < clear; code++) {
            mPrefix[code] = 0;
            mSuffix[code] = (byte) code;
        }

        // Decode GIF pixel stream.
        datum = bits = count = first = top = pi = bi = 0;
        for (i = 0; i < npix; ) {
            if (top == 0) {
                if (bits < code_size) {
                    // Load bytes until there are enough bits for a code.
                    if (count == 0) {
                        // Read a new data block.
                        count = readBlock();
                        if (count <= 0) {
                            break;
                        }
                        bi = 0;
                    }
                    datum += (((int) mBlock[bi]) & 0xff) << bits;
                    bits += 8;
                    bi++;
                    count--;
                    continue;
                }
                // Get the next code.
                code = datum & code_mask;
                datum >>= code_size;
                bits -= code_size;

                // Interpret the code
                if ((code > available) || (code == end_of_information)) {
                    break;
                }
                if (code == clear) {
                    // Reset decoder.
                    code_size = data_size + 1;
                    code_mask = (1 << code_size) - 1;
                    available = clear + 2;
                    old_code = NullCode;
                    continue;
                }
                if (old_code == NullCode) {
                    mPixelStack[top++] = mSuffix[code];
                    old_code = code;
                    first = code;
                    continue;
                }
                in_code = code;
                if (code == available) {
                    mPixelStack[top++] = (byte) first;
                    code = old_code;
                }
                while (code > clear) {
                    mPixelStack[top++] = mSuffix[code];
                    code = mPrefix[code];
                }
                first = ((int) mSuffix[code]) & 0xff;
                // Add a new string to the string table,
                if (available >= MAX_STACK_SIZE) {
                    break;
                }
                mPixelStack[top++] = (byte) first;
                mPrefix[available] = (short) old_code;
                mSuffix[available] = (byte) first;
                available++;
                if (((available & code_mask) == 0)
                        && (available < MAX_STACK_SIZE)) {
                    code_size++;
                    code_mask += available;
                }
                old_code = in_code;
            }

            // Pop a pixel off the pixel stack.
            top--;
            mPixels[pi++] = mPixelStack[top];
            i++;
        }
        for (i = pi; i < npix; i++) {
            mPixels[i] = 0; // clear missing pixels
        }
    }

    private boolean err() {
        return getStatus() != STATUS_PARSING;
    }

    private void init() {
        mStatus = STATUS_PARSING;
        mFramesCount = 0;
        mFirstGifFrame = null;
        mGct = null;
        mLct = null;
    }

    private int read() {
        int curByte = 0;
        try {

            curByte = mInputStream.read();
        } catch (Exception e) {
            mStatus = STATUS_FORMAT_ERROR;
        }
        return curByte;
    }


    private int readBlock() {
        mBlockSize = read();
        int n = 0;
        if (mBlockSize > 0) {
            try {
                int count = 0;
                while (n < mBlockSize) {
                    count = mInputStream.read(mBlock, n, mBlockSize - n);
                    if (count == -1) {
                        break;
                    }
                    n += count;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (n < mBlockSize) {
                mStatus = STATUS_FORMAT_ERROR;
            }
        }
        return n;
    }

    private int[] readColorTable(int ncolors) {
        int nbytes = 3 * ncolors;
        int[] tab = null;
        byte[] c = new byte[nbytes];
        int n = 0;
        try {
            n = mInputStream.read(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (n < nbytes) {
            mStatus = STATUS_FORMAT_ERROR;
        } else {
            tab = new int[256]; // max size to avoid bounds checks
            int i = 0;
            int j = 0;
            while (i < ncolors) {
                int r = ((int) c[j++]) & 0xff;
                int g = ((int) c[j++]) & 0xff;
                int b = ((int) c[j++]) & 0xff;
                tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
        return tab;
    }

    private void readContents() {
        // read GIF file content blocks
        boolean done = false;
        while (!(done || err())) {
            int code = read();
            switch (code) {
                case 0x2C: // image separator
                    readImage();
                    break;
                case 0x21: // extension
                    code = read();
                    switch (code) {
                        case 0xf9: // graphics control extension
                            readGraphicControlExt();
                            break;
                        case 0xff: // application extension
                            readBlock();
                            String app = "";
                            for (int i = 0; i < 11; i++) {
                                app += (char) mBlock[i];
                            }
                            if (app.equals("NETSCAPE2.0")) {
                                readNetscapeExt();
                            } else {
                                skip(); // don't care
                            }
                            break;
                        default: // uninteresting extension
                            skip();
                    }
                    break;
                case 0x3b: // terminator
                    done = true;
                    break;
                case 0x00: // bad byte, but keep going and see what happens
                    break;
                default:
                    mStatus = STATUS_FORMAT_ERROR;
            }
        }
    }

    private void readGraphicControlExt() {
        read(); // block size
        int packed = read(); // packed fields
        mDisposeCode = (packed & 0x1c) >> 2; // disposal method
        if (mDisposeCode == 0) {
            mDisposeCode = 1; // elect to keep old image if discretionary
        }
        mTransparency = (packed & 1) != 0;
        mFrameDuration = readShort() * 10; // delay in milliseconds
        mTranspIndex = read(); // transparent color index
        read(); // block terminator
    }

    private void readHeader() {
        String id = "";
        for (int i = 0; i < 6; i++) {
            id += (char) read();
        }
        if (!id.startsWith("GIF")) {
            mStatus = STATUS_FORMAT_ERROR;
            return;
        }
        readLSD();
        if (mGctUsedFlag && !err()) {
            mGct = readColorTable(mGctSize);
            mBgColor = mGct[mBgColorIndex];
        }
    }

    private void readImage() {
        mCurrX = readShort(); // (sub)image position & size
        mCurrY = readShort();
        mCurrWidth = readShort();
        mCurrHeight = readShort();
        int packed = read();
        mLctFlag = (packed & 0x80) != 0; // 1 - local color table flag
        mInterlaced = (packed & 0x40) != 0; // 2 - interlace flag
        // 3 - sort flag
        // 4-5 - reserved
        mLctSize = 2 << (packed & 7); // 6-8 - local color table size
        if (mLctFlag) {
            mLct = readColorTable(mLctSize); // read table
            mAct = mLct; // make local table active
        } else {
            mAct = mGct; // make global table active
            if (mBgColorIndex == mTranspIndex) {
                mBgColor = 0;
            }
        }
        int save = 0;
        if (mTransparency) {
            save = mAct[mTranspIndex];
            mAct[mTranspIndex] = 0; // set transparent color if specified
        }
        if (mAct == null) {
            mStatus = STATUS_FORMAT_ERROR; // no color table defined
        }
        if (err()) {
            return;
        }
        decodeImageData(); // decode pixel data
        skip();
        if (err()) {
            return;
        }
        mFramesCount++;
        // create new image to receive frame data
        mCurrImg = Bitmap.createBitmap(getWidth(), getImageHeight(), Config.ARGB_4444);
        // createImage(width, height);
        setPixels(); // transfer pixel data to image
        if (mFirstGifFrame == null) {
            if (cacheImage) {
                String name = getDir();
                mFirstGifFrame = new GifFrame(imagePath + File.separator + name + ".png", mFrameDuration);
                saveImage(mCurrImg, name);
            } else {
                mFirstGifFrame = new GifFrame(mCurrImg, mFrameDuration);
            }
            mCurrentFrame = mFirstGifFrame;
        } else {
            GifFrame f = mFirstGifFrame;
            while (f.nextFrame != null) {
                f = f.nextFrame;
            }
            if (cacheImage) {
                String name = getDir();
                f.nextFrame = new GifFrame(imagePath + File.separator + name + ".png", mFrameDuration);
                saveImage(mCurrImg, name);
            } else {
                f.nextFrame = new GifFrame(mCurrImg, mFrameDuration);
            }
        }
        // frames.addElement(new GifFrame(mCurrImg, mFrameDuration)); // add mCurrImg to frame
        // list
        if (mTransparency) {
            mAct[mTranspIndex] = save;
        }
        resetFrame();
        mParsingListener.onGifParsingCompleted(true, mFramesCount);
    }

    private void readLSD() {
        // logical screen size
        mWidth = readShort();
        mHeight = readShort();
        // packed fields
        int packed = read();
        mGctUsedFlag = (packed & 0x80) != 0; // 1 : global color table flag
        // 2-4 : color resolution
        // 5 : gct sort flag
        mGctSize = 2 << (packed & 7); // 6-8 : gct size
        mBgColorIndex = read(); // background color index
        mPixelAR = read(); // pixel aspect ratio
    }

    private void readNetscapeExt() {
        do {
            readBlock();
            if (mBlock[0] == 1) {
                // loop count sub-block
                int b1 = ((int) mBlock[1]) & 0xff;
                int b2 = ((int) mBlock[2]) & 0xff;
                mLoopCount = (b2 << 8) | b1;
            }
        } while ((mBlockSize > 0) && !err());
    }

    private int readShort() {
        // read 16-bit value, LSB first
        return read() | (read() << 8);
    }

    private void resetFrame() {
        mLastDisposeCode = mDisposeCode;
        mLastX = mCurrX;
        mLastY = mCurrY;
        mLastWidth = mCurrWidth;
        mLastHeight = mCurrHeight;
        mLastImg = mCurrImg;
        mLastBgColor = mBgColor;
        mDisposeCode = 0;
        mTransparency = false;
        mFrameDuration = 0;
        mLct = null;
    }

    /**
     * Skips variable length blocks up to and including next zero length block.
     */
    private void skip() {
        do {
            readBlock();
        } while ((mBlockSize > 0) && !err());
    }

    public int getWidth() {
        return mWidth;
    }

    public int getImageHeight() {
        return mHeight;
    }

    public int getPixelAR() {
        return mPixelAR;
    }
}
