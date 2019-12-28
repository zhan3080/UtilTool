package com.example.commonutil.mirror;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VideoEncoder extends Thread {
    private static final String TAG = "VideoEncoder";
    private Handler mHandler;
    private boolean isRotation;
    private boolean isQuitting = true;

    private static final long TIMEOUT_US = 33333;

    private ByteBuffer mHeader;
    private MediaCodec mEncoder;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private ByteBuffer mBuffer;
    public VideoEncoder(MediaCodec codec, Handler handler, boolean isRotation) {
        setName(TAG);
        mEncoder = codec;
        mHandler = handler;
        this.isRotation = isRotation;
    }

    @Override
    public void run() {
        try {
            startEncode(mEncoder, 2, isRotation);
        } catch (Exception e) {
            stopCallback();
            Log.w(TAG, e);
        }
    }

    public void startEncode(MediaCodec codec, int type, boolean isRatoion) {
        long l = System.currentTimeMillis();
        long m = System.currentTimeMillis();
        ByteBuffer[] outbuffers = codec.getOutputBuffers();
        ByteBuffer[] bb3 = new ByteBuffer[3];
        ByteBuffer[] bb2 = new ByteBuffer[2];
        long ptsdiff = 0;
        OutputStream outputStream = null;
        if (true) {
            File dir = new File("/sdcard/Encoder");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MMdd-HHmmss", Locale.US);
            File file = new File(dir, format.format(new Date()) + ".h264");
            file.delete();
            try {
                file.createNewFile();
                outputStream = new FileOutputStream(file);
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }

        while (isQuitting) {
            Log.i(TAG,"isQuitting:"+isQuitting);
            int index = codec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US);
            Log.i(TAG,"index:"+index);
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = codec.getOutputFormat();
                Log.i(TAG, "change---index == --------------------------------> " + index);

                try {
                    ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
                    DataOutputStream localDataOutputStream = new DataOutputStream(
                            localByteArrayOutputStream);

                    ByteBuffer localByteBuffer1 = newFormat
                            .getByteBuffer("csd-0");
                    byte[] arrayOfByte2 = new byte[localByteBuffer1
                            .remaining()];
                    localByteBuffer1.duplicate().get(arrayOfByte2, 0, arrayOfByte2.length);
                    ByteBuffer localByteBuffer2 = newFormat
                            .getByteBuffer("csd-1");
                    byte[] arrayOfByte3 = new byte[localByteBuffer2
                            .capacity()];
                    localByteBuffer2.duplicate().get(arrayOfByte3, 0, arrayOfByte3.length);
                    if (outputStream != null) {
                        Log.i(TAG, "start set sps  " + localByteBuffer1.capacity());
                        byte[] sps = new byte[localByteBuffer1.capacity()];
                        localByteBuffer1.get(sps, 0, sps.length);
                        Log.i(TAG, "start set pps  " + localByteBuffer2.capacity());
                        byte[] pps = new byte[localByteBuffer2.capacity()];
                        localByteBuffer2.get(pps);
                        try {
                            outputStream.write(sps);
                            outputStream.write(pps);
                        } catch (IOException e) {
                            Log.w(TAG, e);
                        }
                    }
                } catch (Exception e) {
                    // todo
                    stopCallback();
                    Log.w(TAG, e);
                    break;
                }
            } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                Log.i(TAG, "wait---index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED--------> ");
                outbuffers = codec.getOutputBuffers();
                 Log.i(TAG, "2 =====> INFO_OUTPUT_BUFFERS_CHANGED" );
            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                 Log.i(TAG, "6 =====> INFO_TRY_AGAIN_LATER" );
            } else if (index >= 0) {
                 Log.i(TAG, "7 =====> index > 0" );
                ByteBuffer encodedData = outbuffers[index];
                byte flag = encodedData.get(4);
                flag &= 0xf;
                //Log.d(TAG, "FLAG--------------------------------------->" + mWidth + "  isRatoion " + isRatoion);
                if (flag == 7) {
                    Log.i(TAG, "sps pps len = " + mBufferInfo.size);
                    encodedData.clear();
                    codec.releaseOutputBuffer(index, false);
                    continue;
                }
                if (outputStream != null) {
                    byte[] outData = new byte[mBufferInfo.size];
                    encodedData.get(outData);
                    try {
                        Log.i(TAG, "start writ" + mBufferInfo.size);
                        outputStream.write(outData, 0, outData.length);
                        outputStream.flush();
                        encodedData.rewind();
                    } catch (IOException e) {
                        Log.w(TAG, e);
                    }
                }
                ByteOrder localByteOrder = encodedData.order();
                encodedData.order(ByteOrder.BIG_ENDIAN);
                if (encodedData.getInt() != 1) {
                    // LogCat.e(TAG, "did not receive expected annex b mHeader");
                    encodedData.clear();
                    codec.releaseOutputBuffer(index, false);
                    Log.i(TAG, "writ end= ----2");
                    break;
                }
                encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                encodedData.order(localByteOrder);
                encodedData.clear();
                codec.releaseOutputBuffer(index, false);
            }

        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        }
        stopCallback();
        Log.i(TAG, " record over ---> ");
    }


    private void stopCallback() {
        mHandler.sendEmptyMessage(ScreenCast.MSG_ON_STOP_CALLBACK);
    }

    public void release() {
        isQuitting = false;
        try {
            mBufferInfo = null;
            if (mBuffer != null) {
                mBuffer.clear();
                mBuffer = null;
            }

            if (mHeader != null) {
                mHeader.clear();
                mHeader = null;
            }

            this.interrupt();
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }
}
