package com.GreenLemonMobile.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.StatFs;

import com.GreenLemonMobile.constant.Constant;

public class FileService {

	public static final String aplcationDir = "/"+Constant.DIR_ROOT_NAME;// 定义本应用在SD卡上�?��用的文件�?

	// 目录类型
	public static final int IMAGE_DIR = 1;// 图片缓存目录
	public static final int JSON_DIR = 2;//
	public static final int XML_DIR = 3;//
	public static final int STREAM_DIR = 4;//
	// 对应目录
	private static Directory imageDir;
	private static Directory jsonDir;
	private static int jsonDirState;// -1:没有适合存储的空�?0:未定,1:内部存储空间,2:外部存储空间

	private static final String SHARED_PREFERENCES_JSON_DIR = "jsonFileCachePath";// json
	// 文件缓存路径（应用设置）
	private static final String SHARED_PREFERENCES_JSON_DIR_STATE = "jsonFileCachePathState";// json
	// 文件缓存路径状�?（应用设置）

	public static final String IMAGE_CHILD_DIR = "/image";// json 子目�?
	public static final String JSON_CHILD_DIR = "/json";// image 子目�?
	public static final String EBOOK_CHILD_DIR = "/ebook";// 电子�?子目�?
	public static final String APK_CHILD_DIR = "/apk";// 电子�?子目�?
	private static final long BIG_SIZE_THRESHOLD = 1024 * 1024 * 512;// 判断为大空间

	// 内置存储空间的目录类�?
	public static final int INTERNAL_TYPE_FILE = 1;
	public static final int INTERNAL_TYPE_CACHE = 2;

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-31 上午10:37:19
	 * 
	 *         Name:
	 * 
	 *         Description: SDCard正确安装，并且可读写
	 * 
	 * @return
	 * 
	 */
	public static boolean isReady() {
		return externalMemoryAvailable();
	}

	public static boolean externalMemoryAvailable() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取文件输出�?TODO
	 */
	public static FileOutputStream openFileOutput(FileGuider fileGuider) throws FileNotFoundException {

		long availableSize = fileGuider.getAvailableSize();
		if (0 != availableSize) {
			if (Directory.INTERNAL == fileGuider.getSpace() && // 内部存储空间不足
					getAvailableInternalMemorySize() < availableSize) {
				return null;
			} else if (Directory.EXTERNAL == fileGuider.getSpace() && // 外部存储空间不足
					getAvailableExternalMemorySize() < availableSize) {
				return null;
			}
		}
        FileOutputStream fos = new FileOutputStream(fileGuider.getFilePath());
		return fos;
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-31 上午10:27:03
	 * 
	 *         Name:
	 * 
	 *         Description: 获得 External 应用根目�?
	 * 
	 * @return
	 * 
	 */
	public static File getExternalDirectory(String childDirName) {
		if (Log.D) {
			Log.d("Temp", "getExternalDirectory() -->> ");
		}
		File dir = new File(Environment.getExternalStorageDirectory(), aplcationDir + ((null != childDirName) ? childDirName : ""));
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-31 上午10:27:03
	 * 
	 *         Name:
	 * 
	 *         Description: 获得 Internal 应用根目�?
	 * 
	 * @return
	 * 
	 */
	public static File getInternalDirectory(String childDirName, int internalType) {

		if (Log.D) {
			Log.d("Temp", "getInternalDirectory() -->> ");
		}

		File typeDir = null;

		switch (internalType) {

		case INTERNAL_TYPE_FILE:
			typeDir = MyApplication.getInstance().getFilesDir();
			break;

		case INTERNAL_TYPE_CACHE:
			typeDir = MyApplication.getInstance().getCacheDir();
			break;

		}

		File dir = new File(typeDir, aplcationDir + ((null != childDirName) ? childDirName : ""));
		if (!dir.exists()) {
			dir.mkdirs();
		}

		if (Log.D) {
			Log.d("Temp", "getInternalDirectory() dir.getAbsolutePath() -->> " + dir.getAbsolutePath());
		}
		if (Log.D) {
			Log.d("Temp", "getInternalDirectory() dir.exists() -->> " + dir.exists());
		}

		return dir;
	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-31 上午10:27:03
	 * 
	 *         Name:
	 * 
	 *         Description: 获得 Internal 应用根目录（缓存�?
	 * 
	 * @return
	 * 
	 */
	public static File getInternalDirectory(String childDirName) {
		return getInternalDirectory(childDirName, INTERNAL_TYPE_CACHE);
	}

	/**
	 * 选用大容量存储空间并建立�?��子目�?
	 */
	private static Directory getDirectoryByBigSize(String childDirName) {
		if (Log.D) {
			Log.d("Temp", "getDirectoryByBigSize() -->> ");
		}
		if (getTotalInternalMemorySize() > BIG_SIZE_THRESHOLD) {// 选用 Internal
			// 空间
			if (Log.D) {
				Log.d("Temp", "getDirectoryByBigSize() -->> INTERNAL");
			}
			return new Directory(getInternalDirectory(childDirName), Directory.INTERNAL);
		} else if (getTotalExternalMemorySize() > BIG_SIZE_THRESHOLD) {// 选用
			// External
			// 空间
			if (Log.D) {
				Log.d("Temp", "getDirectoryByBigSize() -->> EXTERNAL");
			}
			return new Directory(getExternalDirectory(childDirName), Directory.EXTERNAL);
		}
		return null;
	}

	/**
	 * 尝试获得�?��路径
	 */
	public static Directory getDirectory(int dir) {

		switch (dir) {
		case JSON_DIR:
			return getJsonDirectory();
		case IMAGE_DIR:
			return getImageDirectory();
		default:
			return null;
		}

	}

	/**
	 * 尝试获得 json 存储路径
	 */
	private static Directory getJsonDirectory() {

		if (Log.D) {
			Log.d("Temp", "getJsonDirectory() jsonDirState -->> " + jsonDirState);
		}
		if (Log.D) {
			Log.d("Temp", "getJsonDirectory() jsonDir -->> " + jsonDir);
		}

		if (jsonDirState == -1) {
			return null;
		} else if (null != jsonDir) {
			return jsonDir;
		}

		SharedPreferences sharedPreferences = CommonUtil.getDefaultSharedPreferences();
		String jsonFileCachePath = sharedPreferences.getString(SHARED_PREFERENCES_JSON_DIR, null);
		jsonDirState = sharedPreferences.getInt(SHARED_PREFERENCES_JSON_DIR_STATE, 0);
		if (null == jsonFileCachePath) {// 应用设置（未曾设置）

			if (Log.D) {
				Log.d("Temp", "getJsonDirectory() no preferences -->> ");
			}

			Directory directory = getDirectoryByBigSize(JSON_CHILD_DIR);
			if (null == directory) {// 没有大容量存储空�?
				if (Log.D) {
					Log.d("Temp", "getJsonDirectory() no big size -->> ");
				}
				jsonDirState = -1;
				// 不记录（应用设置），因为希望将来能再次检�?
				return null;
			} else {// 存在大容量存储空�?
				if (Log.D) {
					Log.d("Temp", "getJsonDirectory() has big size -->> ");
				}
				jsonDir = directory;
				jsonDirState = directory.getSpace();
				// 记录（应用设置）
				jsonFileCachePath = jsonDir.getDir().getAbsolutePath();
				Editor edit = sharedPreferences.edit();
				edit.putString(SHARED_PREFERENCES_JSON_DIR, jsonFileCachePath);
				edit.putInt(SHARED_PREFERENCES_JSON_DIR_STATE, jsonDirState);
				edit.commit();
				return jsonDir;
			}

		} else {// 应用设置（已经设置）

			if (Log.D) {
				Log.d("Temp", "getJsonDirectory() is preferences -->> ");
			}

			if (jsonDirState == 2) {// 外部存储空间
				if (!externalMemoryAvailable()) {// 预防外部存储空间意外移除
					if (Log.D) {
						Log.d("Temp", "getJsonDirectory() no external -->> ");
					}
					jsonDirState = -1;
					return null;
				}
			}

			if (Log.D) {
				Log.d("Temp", "getJsonDirectory() jsonFileCachePath -->> " + jsonFileCachePath);
			}

			jsonDir = new Directory(new File(jsonFileCachePath), jsonDirState == 1 ? Directory.INTERNAL : Directory.EXTERNAL);
			File dir = jsonDir.getDir();
			if (!dir.exists()) {// 预防创建之后被意外删�?
				dir.mkdirs();
			}
			return jsonDir;
		}
	}

	/**
	 * 尝试获得 image 存储路径
	 */
	private static Directory getImageDirectory() {
		if (!externalMemoryAvailable()) {
			return new Directory(getInternalDirectory(IMAGE_CHILD_DIR,INTERNAL_TYPE_CACHE), Directory.INTERNAL);
		}
		return new Directory(getExternalDirectory(IMAGE_CHILD_DIR), Directory.EXTERNAL);
	}

	/**
	 * 保存内容到SDCard
	 * 
	 * @param filename
	 *            文件名称
	 * @param content
	 *            文件内容
	 * @throws Exception
	 */
	public void saveToSDCard(String filename, String content) throws Exception {
		saveToSDCard(null, filename, content);
	}

	/**
	 * 保存文件
	 */
	public static boolean saveToSDCard(Directory directory, String fileName, String content) {
		return saveToSDCard(directory, fileName, content, Context.MODE_PRIVATE);
	}

	/**
	 * 保存文件
	 */
	public static boolean saveToSDCard(Directory directory, String fileName, String content, int mode) {
		if (null == content) {
			return false;
		}
		return saveToSDCard(directory, fileName, content.getBytes(), mode);
	}

	/**
	 * 保存文件
	 */
	public static boolean saveToSDCard(Directory directory, String fileName, byte[] data) {
		if(null == data){
			return false;
		}
		return saveToSDCard(directory, fileName, data, Context.MODE_PRIVATE);
	}

	/**
	 * 保存文件
	 */
	public static boolean saveToSDCard(Directory directory, String fileName, byte[] data, int mode) {

		File dir = directory.getDir();

		File file = new File(dir, fileName);
		FileOutputStream outStream = null;

		try {
			outStream = new FileOutputStream(file);
			if (null != outStream) {
				outStream.write(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (null != outStream) {
				try {
					outStream.close();
				} catch (IOException e) {
				}
			}
		}
		return true;

	}

	/**
	 * 保存内容
	 * 
	 * @param filename
	 *            文件名称
	 * @param content
	 *            文件内容
	 * @throws Exception
	 */
	public void save(String filename, String content) throws Exception {
		FileOutputStream outStream = MyApplication.getInstance().openFileOutput(filename, Context.MODE_PRIVATE);
		outStream.write(content.getBytes());
		outStream.close();
	}

	/**
	 * 以追加方式保存内�?
	 * 
	 * @param filename
	 *            文件名称
	 * @param content
	 *            文件内容
	 * @throws Exception
	 */
	public void saveAppend(String filename, String content) throws Exception {// ctrl+shift+y变小�?x变大�?
		FileOutputStream outStream = MyApplication.getInstance().openFileOutput(filename, Context.MODE_APPEND);
		outStream.write(content.getBytes());
		outStream.close();
	}

	/**
	 * 保存内容,允许其他应用对其进行读访�?
	 * 
	 * @param filename
	 *            文件名称
	 * @param content
	 *            文件内容
	 * @throws Exception
	 */
	public void saveReadable(String filename, String content) throws Exception {// ctrl+shift+y变小�?x变大�?
		FileOutputStream outStream = MyApplication.getInstance().openFileOutput(filename, Context.MODE_WORLD_READABLE);
		outStream.write(content.getBytes());
		outStream.close();
	}

	/**
	 * 保存内容,允许其他应用对其进行写访�?
	 * 
	 * @param filename
	 *            文件名称
	 * @param content
	 *            文件内容
	 * @throws Exception
	 */
	public void saveWriteable(String filename, String content) throws Exception {// ctrl+shift+y变小�?x变大�?
		FileOutputStream outStream = MyApplication.getInstance().openFileOutput(filename, Context.MODE_WORLD_WRITEABLE);
		outStream.write(content.getBytes());
		outStream.close();
	}

	/**
	 * 保存内容,允许其他应用对其进行读写访问
	 * 
	 * @param filename
	 *            文件名称
	 * @param content
	 *            文件内容
	 * @throws Exception
	 */
	public void saveReadableWriteable(String filename, String content) throws Exception {// ctrl+shift+y变小�?x变大�?
		FileOutputStream outStream = MyApplication.getInstance().openFileOutput(filename, Context.MODE_APPEND + Context.MODE_WORLD_WRITEABLE + Context.MODE_WORLD_READABLE);
		outStream.write(content.getBytes());
		outStream.close();
	}

	/**
	 * 读取内容
	 * 
	 * @param filename
	 *            文件名称
	 * @return 文件内容
	 * @throws Exception
	 */
	public String read(String filename) throws Exception {
		FileInputStream inStream = MyApplication.getInstance().openFileInput(filename);
		byte[] data = readInputStream(inStream);
		return new String(data);
	}

	/**
	 * 读取内容
	 * 
	 * @param filename
	 *            文件名称
	 * @return 文件内容
	 * @throws Exception
	 */
	public byte[] readAsByteArray(String filename) throws Exception {
		FileInputStream inStream = MyApplication.getInstance().openFileInput(filename);
		return readInputStream(inStream);
	}

	// 手机：自带存储空间，外部插进来SDCard

	private byte[] readInputStream(FileInputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		inStream.close();
		outStream.close();
		return outStream.toByteArray();
	}

	private static final int ERROR = -1;

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2011-3-23 下午04:23:09
	 * 
	 *         Name:
	 * 
	 *         Description: 内部可用空间大小
	 * 
	 * @return
	 * 
	 */
	static public long getAvailableInternalMemorySize() {

		File path = Environment.getDataDirectory();

		StatFs stat = new StatFs(path.getPath());

		long blockSize = stat.getBlockSize();

		long availableBlocks = stat.getAvailableBlocks();

		return availableBlocks * blockSize;

	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2011-3-23 下午04:22:22
	 * 
	 *         Name:
	 * 
	 *         Description: 内部总空间大�?
	 * 
	 * @return
	 * 
	 */
	static public long getTotalInternalMemorySize() {

		File path = Environment.getDataDirectory();

		StatFs stat = new StatFs(path.getPath());

		long blockSize = stat.getBlockSize();

		long totalBlocks = stat.getBlockCount();

		long result = totalBlocks * blockSize;

		if (Log.D) {
			Log.d("Temp", "getTotalInternalMemorySize() -->> " + result);
		}

		return result;

	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2011-3-23 下午04:23:48
	 * 
	 *         Name:
	 * 
	 *         Description: 外部可用空间大小
	 * 
	 * @return
	 * 
	 */
	static public long getAvailableExternalMemorySize() {

		if (externalMemoryAvailable()) {

			File path = Environment.getExternalStorageDirectory();

			StatFs stat = new StatFs(path.getPath());

			long blockSize = stat.getBlockSize();

			long availableBlocks = stat.getAvailableBlocks();

			return availableBlocks * blockSize;

		} else {

			return ERROR;

		}

	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2011-3-23 下午04:23:51
	 * 
	 *         Name:
	 * 
	 *         Description: 外部总空间大�?
	 * 
	 * @return
	 * 
	 */
	static public long getTotalExternalMemorySize() {

		if (externalMemoryAvailable()) {

			File path = Environment.getExternalStorageDirectory();

			StatFs stat = new StatFs(path.getPath());

			long blockSize = stat.getBlockSize();

			long totalBlocks = stat.getBlockCount();

			long result = totalBlocks * blockSize;

			if (Log.D) {
				Log.d("Temp", "getTotalExternalMemorySize() -->> " + result);
			}

			return result;

		} else {
			return ERROR;
		}

	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2011-3-23 下午04:24:40
	 * 
	 *         Name:
	 * 
	 *         Description: 把空间大小格式化为字符串
	 * 
	 * @param size
	 * @return
	 * 
	 */
	static public String formatSize(long size) {

		String suffix = null;

		if (size >= 1024) {

			suffix = "KiB";

			size /= 1024;

			if (size >= 1024) {

				suffix = "MiB";

				size /= 1024;

			}

		}

		StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

		int commaOffset = resultBuffer.length() - 3;

		while (commaOffset > 0) {

			resultBuffer.insert(commaOffset, ',');

			commaOffset -= 3;

		}

		if (suffix != null)

			resultBuffer.append(suffix);

		return resultBuffer.toString();

	}

	/**
	 * @author lijingzuo
	 * 
	 *         Time: 2011-3-23 下午04:24:40
	 * 
	 *         Name:
	 * 
	 *         Description: 把空间大小格式化为字符串（小数点�?
	 * 
	 * @param size
	 * @return
	 * 
	 */
	static public String formatSize2(long size) {

		String suffix = null;
		
		float sizeF = Long.valueOf(size).floatValue();

		if (sizeF >= 1024) {

			suffix = "KB";

			sizeF = sizeF / 1024;

			if (sizeF >= 1024) {

				suffix = "MB";

				sizeF = sizeF / 1024;

			}

		}
		
		String format = new DecimalFormat( ".00" ).format(sizeF);

		StringBuilder resultBuffer = new StringBuilder(format);
		

		int commaOffset = resultBuffer.indexOf(".") - 3;

		while (commaOffset > 0) {

			resultBuffer.insert(commaOffset, ',');

			commaOffset -= 3;

		}

		if (suffix != null)

			resultBuffer.append(suffix);
		
		return resultBuffer.toString();

	}

	/**
	 * 清理缓存文件
	 */
//	public static void clearCacheFiles() {
//		ArrayList<CacheFile> list = CacheFileTable.getListByClean();
//		boolean externalMemoryAvailable = FileService.externalMemoryAvailable();
//		for (int i = 0; i < list.size(); i++) {
//			CacheFile cacheFile = list.get(i);
//			Directory directory = cacheFile.getDirectory();
//			if (directory.getSpace() == Directory.INTERNAL || //
//					(directory.getSpace() == Directory.EXTERNAL && externalMemoryAvailable == true)) {
//				boolean result = cacheFile.getFile().delete();
//				if (Log.D) {
//					Log.d("Temp", "cacheFile.getName() -->> " + cacheFile.getName());
//				}
//				if (result) {
//					CacheFileTable.delete(cacheFile);
//				}
//			}
//		}
//	}

	/**
	 * 目录封装�?
	 */
	public static class Directory {

		public static final int INTERNAL = 1;
		public static final int EXTERNAL = 2;

		private File dir;
		private String path;
		private int space;

		public Directory(String path, int space) {
			this(new File(path), space);
		}

		public Directory(File dir, int space) {
			this.dir = dir;
			this.space = space;
		}

		public File getDir() {
			return dir;
		}

		public void setDir(File dir) {
			this.dir = dir;
		}

		public int getSpace() {
			return space;
		}

		public void setSpace(int space) {
			this.space = space;
		}

		public String getPath() {
			if (null == path && null != dir) {
				path = dir.getAbsolutePath();
			}
			return path;
		}

		public void setPath(String path) {
			if (null == getPath() || !getPath().equals(path)) {
				dir = new File(path);
				this.path = path;
			}
		}

	}

}
