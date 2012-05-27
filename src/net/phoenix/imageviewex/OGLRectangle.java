package net.phoenix.imageviewex;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class OGLRectangle {

    private final Object mSyncLock = new Object();

    private FloatBuffer vertexBuffer;    // buffer holding the vertices

    private float vertices[];

    private FloatBuffer textureBuffer;  // buffer holding the texture coordinates
    private float texture[];

    /**
     * The texture pointer
     */
    private int[] textures = new int[1];
    private boolean mCanDraw = false;
    private Bitmap mBitmap;

    private void prepareVertices() {
        if(mBitmap == null)
            return;

        // Fill in vertices data
        vertices = new float[] {-1.0f, -1.0f, 0.0f,         // V1 - bottom left
                                -1.0f, 1.0f, 0.0f,          // V2 - top left
                                1.0f, -1.0f, 0.0f,          // V3 - bottom right
                                1.0f, 1.0f, 0.0f};          // V4 - top right

        // Fill in texture mapping vertices data
        texture = new float[] {0.0f, 1.0f,                  // top left		(V2)
                               0.0f, 0.0f,                  // bottom left	(V1)
                               1.0f, 1.0f,                  // top right	(V4)
                               1.0f, 0.0f};                 // bottom right	(V3)

        // a float has 4 bytes so we allocate for each coordinate 4 bytes
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());

        // allocates the memory from the byte buffer
        vertexBuffer = byteBuffer.asFloatBuffer();

        // fill the vertexBuffer with the vertices
        vertexBuffer.put(vertices);

        // set the cursor position to the beginning of the buffer
        vertexBuffer.position(0);

        // Do the same for the texture
        byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);
    }

    /**
     * The draw method for the square with the GL context
     */
    public void draw(GL10 gl) {
        // Synch-lock the drawing to avoid race conditions
        synchronized (mSyncLock) {
            if(!mCanDraw)
                return;
        }

        // bind the previously generated texture
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        // Point to our buffers
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // Set the face rotation
        gl.glFrontFace(GL10.GL_CW);

        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

        // Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }


    public void loadGLTexture(GL10 gl, Bitmap bitmap) {
        if (bitmap == null)
            return;

        // Avoid synch issues on the draw() method
        synchronized (mSyncLock) {
            // We can't draw until this phase is over
            mCanDraw = false;
            mBitmap = bitmap;
        }

        // generate one texture pointer
        gl.glGenTextures(1, textures, 0);
        // ...and bind it to our array
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        // create nearest filtered texture
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        // Use Android GLUtils to specify a two-dimensional texture image from our bitmap
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

        prepareVertices();

        // Avoid synch issues on the draw() method
        synchronized (mSyncLock) {
            mCanDraw = true;
        }
    }

    public boolean canDraw() {
        return mCanDraw;
    }
}
