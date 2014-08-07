package com.GreenLemonMobile.util;

import java.io.File;

import java.io.IOException;

import android.os.Environment;

public class FileGuider {// TODO

	
	public static int SPACE_ONLY_INTERNAL = 0;// 优先选择内部存储空间或外部存储空�?
	public static int SPACE_ONLY_EXTERNAL = 1;// 优先选择内部存储空间或外部存储空�?
	public static int SPACE_PRIORITY_INTERNAL = 2;// 优先选择内部存储空间或外部存储空�?
	public static int SPACE_PRIORITY_EXTERNAL = 3;
	
	private int space;// 优先选择内部存储空间或外部存储空�?
	private boolean immutable;// 当优先�?择的存储空间不存在或者空间不足时可否选择另一存储空间
	private long TotalSize;// 总空�?
	private long AvailableSize;// 可用空间
	private String childDirName;// 子目�?
	private String fileName;// 文件�?
	private int mode;// 权限
	private int internalType;// 内部存储空间类型
    private File root;
	public int getSpace() {
		return space;
	}

	public FileGuider(int space){
	this.space = space; 
	 root = getRoot();
	}
	
	public void setSpace(int space) {
		this.space = space;
	}

	public boolean isImmutable() {
		return immutable;
	}

	public void setImmutable(boolean immutable) {
		this.immutable = immutable;
	}

	public long getTotalSize() {
		return TotalSize;
	}

	public void setTotalSize(long totalSize) {
		TotalSize = totalSize;
	}

	public long getAvailableSize() {
		return AvailableSize;
	}

	public void setAvailableSize(long availableSize) {
		AvailableSize = availableSize;
	}

	public String getChildDirName() {
		return childDirName;
	}

	public void setChildDirName(String childDirName) {
		this.childDirName = childDirName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getInternalType() {
		return internalType;
	}
	
	public String getParentPath() throws IOException{
		if(childDirName==null){
			return root.getAbsolutePath()+FileService.aplcationDir;
		}
	return root.getAbsolutePath()+FileService.aplcationDir+childDirName;	
	}
	
	public void checkParentPath() throws IOException{
		File f = new File(getParentPath());
		if(!f.exists()){
			f.mkdirs();
		}
	}
	
	public File getRoot(){
		File root = null;
		long availableSize = getAvailableSize();
		if(SPACE_ONLY_INTERNAL==space){
			root = MyApplication.getInstance().getFilesDir();
		}else if(SPACE_ONLY_EXTERNAL==space){
			root = Environment.getExternalStorageDirectory();	
		}else if(SPACE_PRIORITY_INTERNAL==space){
			if ( 	FileService.getAvailableInternalMemorySize() > availableSize // 内部存储空间足够
			){
			root = MyApplication.getInstance().getFilesDir();	
		}else if(FileService.externalMemoryAvailable()&&FileService.getAvailableExternalMemorySize() > availableSize){
			root = Environment.getExternalStorageDirectory();
		}
		}else if(SPACE_PRIORITY_EXTERNAL==space){
			if(FileService.externalMemoryAvailable()&&FileService.getAvailableExternalMemorySize() > availableSize){
			root = Environment.getExternalStorageDirectory();
		}else if ( 	FileService.getAvailableInternalMemorySize() > availableSize ){// 内部存储空间足够
			root = MyApplication.getInstance().getFilesDir();	
		}
		}
		return root;
	}
	
	public String getFilePath(){
				try {
					checkParentPath();
				String path =	getParentPath()+"/"+getFileName();
				Log.i("zhoubo", "getFilePath()======"+path);
					return path;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
	}
	
	public File getFile(){
		File file =  new File(getFilePath());
		return file;
}
	
	public void setInternalType(int internalType) {
		this.internalType = internalType;
	}

}
