package com.GreenLemonMobile.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.R.color;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;

public class ImageUtil {
    /**
     * 
     * @param drawable drawable图片
     * @param roundPx 角度 
     * @return  
     * @Description:// 获得圆角图片的方法
     */
    
    public static Bitmap getRoundedCornerBitmap(Drawable drawable, float roundPx) {
        Bitmap bitmap  = drawableToBitmap(drawable);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap

        .getHeight(), Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;

        final Paint paint = new Paint();

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);

        paint.setColor(color);

        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;

    }

    /**
     * 
     * @param drawable
     * @return 
     * @Description:将Drawable转化为Bitmap
     */

    private static Bitmap drawableToBitmap(Drawable drawable) {

        int width = drawable.getIntrinsicWidth();

        int height = drawable.getIntrinsicHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height,

        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

        : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, width, height);

        drawable.draw(canvas);

        return bitmap;

    }
    
	// 这个函数会对图片的大小进行判断，并得到合适的缩放比例，比如2即1/2,3即1/3
	static int computeSampleSize(BitmapFactory.Options options, int height,
			int width) {
		int w = options.outWidth;
		int h = options.outHeight;
		int candidate = (w * h) / (height * width);
		if (((w * h) % (height * width)) > 0) {
			candidate++;
		}
		if (candidate == 0)
			return 1;

		if (true);
//			DLog.i("ImageTool", "for w/h " + w + "/" + h + " returning "
//					+ candidate + "(" + (w / candidate) + " / "
//					+ (h / candidate));
		return candidate;
	}

	public static Bitmap getBitpMap(Context context, InputStream is,
			int height, int width) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			// 先指定原始大小
			options.inSampleSize = 1;
			// 只进行大小判断
			options.inJustDecodeBounds = true;
			// 调用此方法得到options得到图片的大小
			is.mark(0);
			BitmapFactory.decodeStream(is, null, options);
			// 我们的目标是在800pixel的画面上显示。
			// 所以需要调用computeSampleSize得到图片缩放的比例

			options.inSampleSize = computeSampleSize(options, height, width);
			// OK,我们得到了缩放的比例，现在开始正式读入BitMap数据
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			is.reset();
			// 根据options参数，减少所需要的内存
			Bitmap sourceBitmap = BitmapFactory.decodeStream(is, null, options);
			return sourceBitmap;
		} catch (OutOfMemoryError error) {
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getImageFromStream(Context context, String imagePath,
			int height, int width) {
		InputStream is;
		try {
			is = StreamToolBox.loadStreamFromFile(imagePath);
			is = StreamToolBox.flushInputStream(is);
			return getBitpMap(context, is, height, width);
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getImage(InputStream is) {
		try {
			int height = DisplayUtil.getHeight();
			int width = DisplayUtil.getWidth();
			return getBitpMap(MyApplication.getInstance(), is, height, width);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}


	public static Bitmap getImage(String imagePath) {
		try {
			InputStream inputStream = StreamToolBox.loadStreamFromFile(imagePath);
			inputStream = StreamToolBox.flushInputStream(inputStream);
 		       return getImage(inputStream);
		} catch (Exception e) {
		}
		return null;
	}

	@SuppressWarnings("static-access")
	public static Bitmap getScaleImage(Bitmap bitmap, int resizedWidth,
			int resizedHeight, boolean isenlarge, boolean isScale,boolean isFill) {

		Bitmap BitmapOrg = bitmap;

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();

		int newWidth = resizedWidth;
		int newHeight = resizedHeight;

		if (!isenlarge) {
			if (newWidth > width && newHeight > height) {
				return bitmap;
			}
		}

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		Log.i("ImageTool", "width=="+width);
		Log.i("ImageTool", "height=="+height);
		Log.i("ImageTool", "resizedWidth=="+resizedWidth);
		Log.i("ImageTool", "resizedHeight=="+resizedHeight);
		Log.i("ImageTool", "scaleWidth0=="+scaleWidth);
		Log.i("ImageTool", "scaleHeight0=="+scaleHeight);
		if (!isenlarge) {
			if (isScale) {
				scaleWidth = scaleWidth < scaleHeight ? scaleWidth
						: scaleHeight;
				scaleHeight = scaleWidth;
			}
		} else {
			if (isScale) {
				scaleWidth = scaleWidth > scaleHeight ?scaleWidth
						: scaleHeight;
				scaleHeight = scaleWidth;
			}
		}
		Log.i("ImageTool", "scaleWidth1=="+scaleWidth);
		Log.i("ImageTool", "scaleHeight1=="+scaleHeight);

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);
		if(isFill){
			int ShearWidth = 0;
			int ShearHeight = 0;
			if(resizedBitmap.getWidth()>resizedWidth){
				ShearWidth = resizedBitmap.getWidth() - resizedWidth;
			}
			if(resizedBitmap.getHeight()>resizedHeight){
				 ShearHeight = resizedBitmap.getHeight() - resizedHeight;
			}
			resizedBitmap = resizedBitmap.createBitmap(resizedBitmap, ShearWidth/2, ShearHeight/2, resizedBitmap.getWidth()-ShearWidth, resizedBitmap.getHeight()-ShearHeight);
		Log.i("ImageTool", "scaleWidth2=="+resizedBitmap.getWidth());
		Log.i("ImageTool", "scaleHeight3=="+resizedBitmap.getHeight());
		}
		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
		return resizedBitmap;
	}

    /**
     * 获得带倒影的图片方法 封装
     * 
     * @param bitmap
     * @return
     */ 
	public static Bitmap getReflectedImage(Bitmap originalImage, int reflectionGap) {
		Bitmap getImage = null;
		try {
			getImage = createReflectedImage(originalImage, reflectionGap);

		} catch (Exception e) {
			e.printStackTrace();
			getImage = null;
		}
		return getImage;
	}
    /**
     * 获得带倒影的图片方法1
     * 
     * @param bitmap
     * @return
     */ 
	public static Bitmap createReflectedImage(Bitmap originalImage, int reflectionGap) {
        // The gap we want between the reflection and the original image
        //final int reflectionGap = 4;

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // This will not scale but will flip on the Y axis
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        // Create a Bitmap with the flip matrix applied to it.
        // We only want the bottom half of the image
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
        		 height/2, width,  height/2, matrix, false);

        // Create a new bitmap with same width but taller to fit reflection
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                (height +(int)DPIUtil.dip2px(30)), Config.ARGB_8888);

        // Create a new Canvas with the bitmap that's big enough for
        // the image plus gap plus reflection
        Canvas canvas = new Canvas(bitmapWithReflection);
        // Draw in the original image
        canvas.drawBitmap(originalImage, 0, 0, null);
        // Draw in the gap
        Paint defaultPaint = new Paint();
        defaultPaint.setColor(color.transparent);
        canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
        // Draw in the reflection
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

        // Create a shader that is a linear gradient that covers the reflection
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0,
                originalImage.getHeight(), 0, bitmapWithReflection.getHeight()
                        + reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
		// Set the paint to use this shader (linear gradient)
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		return bitmapWithReflection;
    }
	
	
    /**
     * 3.获得带倒影的图片方法 原始数据
     * 
     * @param bitmap
     * @return
     */ 
    public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap) { 
        final int reflectionGap = 4; 
        int width = bitmap.getWidth(); 
        int height = bitmap.getHeight(); 
        Matrix matrix = new Matrix(); 
        matrix.preScale(1, -1); 
        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2, 
                width, height / 2, matrix, false); 
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, 
                (height + height / 2), Config.ARGB_8888); 
        Canvas canvas = new Canvas(bitmapWithReflection); 
        canvas.drawBitmap(bitmap, 0, 0, null); 
        Paint deafalutPaint = new Paint(); 
        canvas 
                .drawRect(0, height, width, height + reflectionGap, 
                        deafalutPaint); 
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null); 
        Paint paint = new Paint(); 
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0, 
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 
                0x00ffffff, TileMode.CLAMP); 
        paint.setShader(shader); 
        // Set the Transfer mode to be porter duff and destination in 
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN)); 
        // Draw a rectangle using the paint with our linear gradient 
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() 
                + reflectionGap, paint); 
        return bitmapWithReflection; 
    } 
    /**  
     * 保存文件  
     * @param bm  
     * @param fileName  
     * @throws IOException  
     */  
    public static boolean saveFile(Bitmap bm, String filePath)  {   
        try {
        File myCaptureFile = new File(filePath);   
        if(!myCaptureFile.exists()){
        	myCaptureFile.createNewFile();
        	Log.i("zhoubo", "myCaptureFile.createNewFile();");
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));   
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);   
			bos.flush();
	        bos.close();   
		    return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return false;
    }  
    
    
    /**
     * 1.放大缩小图片
     * 
     * @param bitmap
     * @param w
     * @param h
     * @return
     */ 
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) { 
        int width = bitmap.getWidth(); 
        int height = bitmap.getHeight(); 
        Matrix matrix = new Matrix(); 
        float scaleWidht = ((float) w / width); 
        float scaleHeight = ((float) h / height); 
        matrix.postScale(scaleWidht, scaleHeight); 
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, 
                matrix, true); 
        return newbmp; 
    } 
     
    /**
     * 2.获得圆角图片的方法
     * 
     * @param bitmap
     * @param roundPx
     * @return
     */ 
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) { 
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap 
                .getHeight(), Config.ARGB_8888); 
        Canvas canvas = new Canvas(output); 
        final int color = 0xff424242; 
        final Paint paint = new Paint(); 
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()); 
        final RectF rectF = new RectF(rect); 
        paint.setAntiAlias(true); 
        canvas.drawARGB(0, 0, 0, 0); 
        paint.setColor(color); 
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint); 
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
        canvas.drawBitmap(bitmap, rect, rect, paint); 
        return output; 
    } 
     
     
//    /**
//     * 4.将Drawable转化为Bitmap
//     * 
//     * @param drawable
//     * @return
//     */ 
//    public static Bitmap drawableToBitmap(Drawable drawable) { 
//        int width = drawable.getIntrinsicWidth(); 
//        int height = drawable.getIntrinsicHeight(); 
//        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable 
//                .getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 
//                : Bitmap.Config.RGB_565); 
//        Canvas canvas = new Canvas(bitmap); 
//        drawable.setBounds(0, 0, width, height); 
//        drawable.draw(canvas); 
//        return bitmap; 
//    }    

	public static final float RADIO_CROP_WIDTH = 0.72f;
	
	public static final int HEIGHT_REFLECTION = 125;

	public static final int MAX_SINGLE_IMAGE_PIX = (600 * 1024) / 2;
	private static final float startAlpha = 105;
	private static final float offset = startAlpha / HEIGHT_REFLECTION;

	public static Bitmap CropForExtraWidth(Bitmap source, int[] targetSize,
			int maxWidth) {
		if ((source == null) || (targetSize == null)
				|| (targetSize != null && targetSize[0] < maxWidth))
			return source;

		float ratio = (float) source.getWidth() / (float) targetSize[0];

		int targetWidth = (int) (maxWidth * ratio);

		Bitmap newBmp = Bitmap.createBitmap(source,
				(source.getWidth() - targetWidth) / 2, 0, targetWidth,
				source.getHeight());
		if (!source.equals(newBmp))
			source.recycle();

		return newBmp;
	}

	public static Bitmap CropForExtraWidth(Bitmap source) {
		if (source == null) {
			return source;
		}
		float ratio = (float) source.getWidth() / (float) source.getHeight();
		if (ratio <= RADIO_CROP_WIDTH) {
			return source;
		}
		float leftCropRatio = (ratio - RADIO_CROP_WIDTH) / 2.0f;
		float rightCropRatio = leftCropRatio;
		int targetWidth = source.getWidth();
		int x = 0;
		targetWidth = (int) (RADIO_CROP_WIDTH * source.getHeight());
		x = (int) (leftCropRatio * source.getHeight());
		Bitmap newBmp = Bitmap.createBitmap(source, x, 0, targetWidth,
				source.getHeight());
		if (!source.equals(newBmp)) {
			source.recycle();
		}
		return newBmp;
	}

	public static Bitmap addReflection(Bitmap srcBitmap, int bkgColor) {
		if (srcBitmap == null)
			return null;

		int reflectionGap = 1;

		int width = srcBitmap.getWidth();
		int height = srcBitmap.getHeight();
		Bitmap outBitmap = Bitmap.createBitmap(width, (height
				+ HEIGHT_REFLECTION + reflectionGap), Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(outBitmap);
		canvas.drawColor(bkgColor);
		canvas.drawBitmap(srcBitmap, 0, 0, new Paint(Paint.ANTI_ALIAS_FLAG));

		int[] original = new int[width * HEIGHT_REFLECTION];
		srcBitmap.getPixels(original, 0, width, 0, height - HEIGHT_REFLECTION,
				width, HEIGHT_REFLECTION);

		int reflection[] = new int[width * HEIGHT_REFLECTION];

		for (int i = 0; i < HEIGHT_REFLECTION; i++) {
			int alpha = (int) (startAlpha - offset * i);
			for (int j = 0; j < width; j++) {
				int sourcePixel = original[(HEIGHT_REFLECTION - i - 1) * width
						+ j];
				reflection[i * width + j] = Color.argb((int) alpha, // Alpha
						Color.red(sourcePixel), // Red
						Color.green(sourcePixel), // Green
						Color.blue(sourcePixel)); // Blue
			}
		}
		// draw reflection
		Bitmap reflectionBmp = Bitmap.createBitmap(reflection, width,
				HEIGHT_REFLECTION, Bitmap.Config.ARGB_8888);
		canvas.drawBitmap(reflectionBmp, 0, height + reflectionGap, new Paint(
				Paint.ANTI_ALIAS_FLAG));

		srcBitmap.recycle();
		srcBitmap = null;
		reflectionBmp.recycle();
		reflectionBmp = null;

		return outBitmap;
	}

	public static int[] countNewSize(int width, int height, int maxHeight) {
		int[] newSize = new int[2];

		newSize[0] = 1;
		if (height != 0)
			newSize[0] = width * maxHeight / height;

		if (newSize[0] <= 0)
			newSize[0] = 1;
		newSize[1] = maxHeight;
		return newSize;
	}

	public static Bitmap resize(Bitmap source, int newWidth, int newHeight) {
		if (source == null)
			return null;

		Matrix matrix = new Matrix();
		int originalWidth = source.getWidth();
		int orginalHeight = source.getHeight();
		matrix.postScale(((float) newWidth / originalWidth),
				((float) newHeight / orginalHeight));

		Bitmap output = null;
		int count = 0;
		do {
			count++;
			try {
				output = Bitmap.createBitmap(source, 0, 0, originalWidth,
						orginalHeight, matrix, true);
			} catch (OutOfMemoryError ex) {
			}
		} while (output == null && count <= 3);
		if (!source.equals(output))
			source.recycle();
		return output;
	}

	public static String ResImageToMemory(Context context, int resId,
			String path) {
		String filePath = "";

		FileUtils helper = new FileUtils(context);

		File file = null;

		try {
			file = helper.isDataFileExist(path);
			if (file != null)
				filePath = file.getPath();
			else {
				file = helper.creatDataFile(path);
				if (file != null) {
					InputStream in = context.getResources().openRawResource(
							resId);

					int bytesum = 0;
					int byteread = 0;
					// file://
					// InputStream inStream=new FileInputStream( "c:/aaa.doc ");

					FileOutputStream fs = new FileOutputStream(file);
					byte[] buffer = new byte[1024];
					while ((byteread = in.read(buffer)) != -1) {
						bytesum += byteread;
						fs.write(buffer, 0, byteread);
					}
					in.close();
					fs.flush();
					fs.close();
					filePath = file.getPath();
				}
			}
		} catch (IOException e) {

		}

		return filePath;
	}
	
	 /**
     * Creates a centered bitmap of the desired size. Recycles the input.
     * 
     * @param source
     */
    public static Bitmap extractMiniThumb(Bitmap source, int width, int height, boolean rec) {
    	
    	boolean recycle = rec;
    	
    	 if (source == null) {
             return null;
         }

         float scale;
         if (source.getWidth() < source.getHeight()) {
             scale = width / (float) source.getWidth();
         } else {
             scale = height / (float) source.getHeight();
         }
         Matrix matrix = new Matrix();
         matrix.setScale(scale, scale);
//         Bitmap miniThumbnail = transform(matrix, source, width, height, false);
		Matrix scaler = matrix;
		// Bitmap source = source,
		int targetWidth = width;
		int targetHeight = height;
		boolean scaleUp = false;
		
         int deltaX = source.getWidth() - targetWidth;
         int deltaY = source.getHeight() - targetHeight;
         if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
             /*
              * In this case the bitmap is smaller, at least in one dimension,
              * than the target. Transform it by placing as much of the image as
              * possible into the target and leaving the top/bottom or left/right
              * (or both) black.
              */
             Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
             Canvas c = new Canvas(b2);

             int deltaXHalf = Math.max(0, deltaX / 2);
             int deltaYHalf = Math.max(0, deltaY / 2);
             Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf + Math.min(targetWidth, source.getWidth()), deltaYHalf
                     + Math.min(targetHeight, source.getHeight()));
             int dstX = (targetWidth - src.width()) / 2;
             int dstY = (targetHeight - src.height()) / 2;
             Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight - dstY);
             c.drawBitmap(source, src, dst, null);
             return b2;
         }
         float bitmapWidthF = source.getWidth();
         float bitmapHeightF = source.getHeight();

         float bitmapAspect = bitmapWidthF / bitmapHeightF;
         float viewAspect = (float) targetWidth / targetHeight;

         if (bitmapAspect > viewAspect) {
//             float scale = targetHeight / bitmapHeightF;
             scale = targetHeight / bitmapHeightF;
             if (scale < .9F || scale > 1F) {
                 scaler.setScale(scale, scale);
             } else {
                 scaler = null;
             }
         } else {
//             float scale = targetWidth / bitmapWidthF;
             scale = targetWidth / bitmapWidthF;
             if (scale < .9F || scale > 1F) {
                 scaler.setScale(scale, scale);
             } else {
                 scaler = null;
             }
         }

         Bitmap b1;
         if (scaler != null) {
             // this is used for minithumb and crop, so we want to filter here.
             b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), scaler, true);
         } else {
             b1 = source;
         }

         int dx1 = Math.max(0, b1.getWidth() - targetWidth);
         int dy1 = Math.max(0, b1.getHeight() - targetHeight);

         Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth, targetHeight);

         if (b1 != source) {
             b1.recycle();
         }

//         return b2;
         
         Bitmap miniThumbnail = b2;
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         
         if (recycle && miniThumbnail != source) {
             source.recycle();
         }
         return miniThumbnail;
    }
    
	public static  Bitmap getMiniBitmap(byte[] data, int w, int h) {
		if (data == null || data.length == 0) {
			return null;
		}
    	
		Bitmap result = null;
		int width = w;
		int heigt = h;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		// BitmapFactory.decodeResource(res, mImageIds[position],
		// options);
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		options.inJustDecodeBounds = false;
		// pre-calculate the sample using filmstrip height
		int bitmapSample = (options.outHeight / width);
		if (bitmapSample == 0)
			bitmapSample = 1;
		boolean isExtraWidth = (((options.outWidth * options.outHeight) / (bitmapSample * bitmapSample)) 
				> ImageUtil.MAX_SINGLE_IMAGE_PIX/*ITEM_WIDTH * ITEM_HEIGHT*/);

		options.inSampleSize = isExtraWidth ?
		/**
		 * determine weather the bitmap is still too large if we only consider
		 * the ratio between original height and film strip height.
		 */
		((int) Math.ceil(Math.sqrt((options.outWidth * options.outHeight)
				/ (double) ImageUtil.MAX_SINGLE_IMAGE_PIX/*ITEM_WIDTH * ITEM_HEIGHT*/))) : bitmapSample;
		options.inDither = false;
        result = BitmapFactory.decodeByteArray(data, 0, data.length, options);

		if (result == null || result.isRecycled()) {
			return null;
		}

		return result;
	}
      
}
