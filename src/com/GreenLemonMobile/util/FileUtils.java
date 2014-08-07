package com.GreenLemonMobile.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;

import com.GreenLemonMobile.cipher.Base64;

public class FileUtils {
	private Context context;
	private String SDPATH;
	private String FILESPATH;
	
    private final int FILESIZE = 1 * 1024;

    public String getSDPATH(){
        return SDPATH;
    }

    public FileUtils(Context context){
    	this.context = context;
		SDPATH = Environment.getExternalStorageDirectory().getPath()
				+ File.separator;
		FILESPATH = context.getFilesDir().getPath() + File.separator;
    }

    /**
     * 鍒涘缓鏂囦欢
     * @param fileName
     * @return
     * @throws IOException
     */
    public File createFile(String fileName) throws IOException{
        File file = new File(fileName);
        file.createNewFile();
        return file;
    }

    /**
     * 鍦⊿D鍗′笂鍒涘缓鐩綍
     * @param dirName
     * @return
     */
    public File createSDDir(String dirName){
        File dir = new File(SDPATH + dirName);
        dir.mkdir();
        return dir;
    }

    /**
     * 鍒ゆ柇SD鍗′笂鐨勬枃浠跺す鏄惁瀛樺湪
     * @param fileName
     * @return
     */
    public boolean isFileExist(String fileName){
        File file = new File(SDPATH + fileName);
        return file.exists();
    }

    /**
     * 鍒ゆ柇SD鍗′笂鐨勬枃浠跺す鏄惁瀛樺湪
     * @param fileName
     * @return
     */
    public void deleFile(String fileName){
        File file = new File(SDPATH + fileName);
        if(file.exists()){
        	file.deleteOnExit();
        }
        return ;
    }
    /**
     * 灏嗕竴涓狪nputStream閲岄潰鐨勬暟鎹啓鍏ュ埌SD鍗′腑
     * @param path
     * @param fileName
     * @param input
     * @return
     */
    public File writeFromInput(String path,String fileName,InputStream input){
        File file = null;
        FileOutputStream output = null;
        try {
//            createSDDir(path);
        	file = new File(path+fileName);
        	if(file.exists()){
        		file.deleteOnExit();
        	}
        	file.createNewFile();

            output = new FileOutputStream(file);
            byte[] buffer = new byte[FILESIZE];
            int readLength = 0;
            BufferedInputStream bufferedInputStream = new BufferedInputStream(input);
            while((readLength = bufferedInputStream.read(buffer)) != -1){
                output.write(buffer,0,readLength);
            }
            output.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
            	if(output!=null){
            		output.close();
            	}

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    /**
     * 瀛楃閿欎綅绠楁硶
     * @param ori
     * @return
     */
    public static byte [] confuse(byte [] ori){
        for (int i = 0, byteLength = ori.length; i < byteLength; i++) {
                                ori[i] = (byte) ~ori[i];
                            }
            return ori;

    }
	public  PublicKey getPublicKey(String key) throws Exception {
		byte[] keyBytes;
		keyBytes = Base64.decode(key);

		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}

//    /*
//     * RSA鍔犲瘑  瀹夊叏閫氶亾,鍔犲瘑绋嬪簭,cpa娉ㄥ唽info鍔犲瘑
//     */
//    public static String StringCpaEncryption(String info){
//    	//缁忚繃浣嶇Щ鐨勫叕閽?
//    	String clientPublicKey = "z35gz/L59tV5t3kI8v7+/vr//H5y/89+dv1+fv9ty1Bwsoodk1/EkSruncOdFCQMJYGVKR4aqFqC  IrBS3lK3uHhodLL/efU4rANQCqc7/id1UYeCrRFIi2Tw0sEKisXwrwmAnXvgWZSFfmSBP8La5zQl  J9hOFCTFBl5NHl2W9cejAlDtRVshMJukiBVcNvvxigCaKMMWt28dg8cMRP38/v/+";
//    	//浣嶇Щ杩愮畻,寰楀埌鐪熷疄鐨勫叕閽?
//    	//RsaUtil rsaUtil = new RsaUtil();
//
//    	String pubKey = Base64.encodeBytes(confuse(Base64.decode(clientPublicKey)));//鐪熷疄鍏挜
//    	String sessionKey =  "0+MEKvEICHYETKGu/rWdVyNopHP3hatw";
//    	//闇?RSA鍔犲瘑鐨勫瓧绗︿覆,鑷冲皯8涓瓧绗?64浣?
//    	 String desKey= "my des key,by random,only client know me!!!";
//    	 RsaHelper rsaHelper = new RsaHelper();
//    	 RSAPublicKey = (RSAPublicKey)rsaHelper.getPublicKey(pubKey);//鏋勫缓RSAPublicKey瀵硅薄
//    	 String result = rsaHelper.encrypt(desKey);//浣跨敤rsa鎶?湳鍔犲瘑deskey
//    	return result;
//    }
	
	
	/**
	 * 
	 * @throws IOException
	 */
	public File creatSDFile(String fileName) throws IOException {
		File file = new File(SDPATH + fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * 
	 * @param fileName
	 */
	public boolean delSDFile(String fileName) {
		File file = new File(SDPATH + fileName);
		if (file == null || !file.exists() || file.isDirectory())
			return false;
		file.delete();
		return true;
	}

	/**
	 * 
	 * @param dirName
	 */
	public File creatSDDir(String dirName) {
		File dir = new File(SDPATH + dirName);
		dir.mkdir();
		return dir;
	}

	/**
	 * 
	 * @param dirName
	 */
	public boolean delSDDir(String dirName) {
		File dir = new File(SDPATH + dirName);
		return delDir(dir);
	}

	/**
	 * 
	 * @param fileName
	 */
	public boolean renameSDFile(String oldfileName, String newFileName) {
		File oleFile = new File(SDPATH + oldfileName);
		File newFile = new File(SDPATH + newFileName);
		return oleFile.renameTo(newFile);
	}

	/**
	 * 
	 * @param path
	 * @throws IOException
	 */
	public boolean copySDFileTo(String srcFileName, String destFileName)
			throws IOException {
		File srcFile = new File(SDPATH + srcFileName);
		File destFile = new File(SDPATH + destFileName);
		return copyFileTo(srcFile, destFile);
	}

	/**
	 * 
	 * @param srcDirName
	 * @param destDirName
	 * @return
	 * @throws IOException
	 */
	public boolean copySDFilesTo(String srcDirName, String destDirName)
			throws IOException {
		File srcDir = new File(SDPATH + srcDirName);
		File destDir = new File(SDPATH + destDirName);
		return copyFilesTo(srcDir, destDir);
	}

	/**
	 * 
	 * @param srcFileName
	 * @param destFileName
	 * @return
	 * @throws IOException
	 */
	public boolean moveSDFileTo(String srcFileName, String destFileName)
			throws IOException {
		File srcFile = new File(SDPATH + srcFileName);
		File destFile = new File(SDPATH + destFileName);
		return moveFileTo(srcFile, destFile);
	}

	/**
	 * 
	 * @param srcDirName
	 * @param destDirName
	 * @return
	 * @throws IOException
	 */
	public boolean moveSDFilesTo(String srcDirName, String destDirName)
			throws IOException {
		File srcDir = new File(SDPATH + srcDirName);
		File destDir = new File(SDPATH + destDirName);
		return moveFilesTo(srcDir, destDir);
	}

	/*
	 */
	public FileOutputStream writeSDFile(String fileName) throws IOException {
		File file = new File(SDPATH + fileName);
		return new FileOutputStream(file);
	}

	/*
	 */
	public FileOutputStream appendSDFile(String fileName) throws IOException {
		File file = new File(SDPATH + fileName);
		return new FileOutputStream(file, true);
	}

	/*
	 * ��SD����ȡ�ļ�����:readSDFile("test.txt");
	 */
	public FileInputStream readSDFile(String fileName) throws IOException {
		File file = new File(SDPATH + fileName);
		return new FileInputStream(file);
	}

	/**
	 * �ļ��Ƿ����
	 * 
	 * @param fileName
	 * @return
	 */
	public File isDataFileExist(String fileName) throws IOException {
		// TODO Auto-generated method stub
		File file = new File(FILESPATH + fileName);
		if (!file.exists()) {
			file = null;
		}
		return file;
	}

	/**
	 * ����˽���ļ�
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public File creatDataFile(String fileName) throws IOException {
		File file = new File(FILESPATH + fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * ����˽��Ŀ¼
	 * 
	 * @param dirName
	 * @return
	 */
	public File creatDataDir(String dirName) {
		File dir = new File(FILESPATH + dirName);
		dir.mkdir();
		return dir;
	}
	
	/**
	 * ����˽��Ŀ¼
	 * 
	 * @param dirName
	 * @return
	 */
	public File mkDir(String dirName) {
		File dir = new File(FILESPATH + dirName);
		if (!dir.exists()) {
			boolean hasCreated = dir.mkdir();
			if (!hasCreated) {
				dir = null;
			}
		}
		return dir;
	}
	
	/**
	 * ɾ��˽���ļ�
	 * 
	 * @param fileName
	 * @return
	 */
	public boolean delDataFile(String fileName) {
		File file = new File(FILESPATH + fileName);
		return delFile(file);
	}

	/**
	 * ɾ��˽��Ŀ¼
	 * 
	 * @param dirName
	 * @return
	 */
	public boolean delDataDir(String dirName) {
		File file = new File(FILESPATH + dirName);
		return delDir(file);
	}

	/**
	 * ���˽���ļ���
	 * 
	 * @param oldName
	 * @param newName
	 * @return
	 */
	public boolean renameDataFile(String oldName, String newName) {
		File oldFile = new File(FILESPATH + oldName);
		File newFile = new File(FILESPATH + newName);
		return oldFile.renameTo(newFile);
	}

	/**
	 * ��˽��Ŀ¼�½����ļ�����
	 * 
	 * @param srcFileName
	 *            �� ��·�����ļ���
	 * @param destFileName
	 * @return
	 * @throws IOException
	 */
	public boolean copyDataFileTo(String srcFileName, String destFileName)
			throws IOException {
		File srcFile = new File(FILESPATH + srcFileName);
		File destFile = new File(FILESPATH + destFileName);
		return copyFileTo(srcFile, destFile);
	}

	/**
	 * ����˽��Ŀ¼��ָ��Ŀ¼�������ļ�
	 * 
	 * @param srcDirName
	 * @param destDirName
	 * @return
	 * @throws IOException
	 */
	public boolean copyDataFilesTo(String srcDirName, String destDirName)
			throws IOException {
		File srcDir = new File(FILESPATH + srcDirName);
		File destDir = new File(FILESPATH + destDirName);
		return copyFilesTo(srcDir, destDir);
	}

	/**
	 * �ƶ�˽��Ŀ¼�µĵ����ļ�
	 * 
	 * @param srcFileName
	 * @param destFileName
	 * @return
	 * @throws IOException
	 */
	public boolean moveDataFileTo(String srcFileName, String destFileName)
			throws IOException {
		File srcFile = new File(FILESPATH + srcFileName);
		File destFile = new File(FILESPATH + destFileName);
		return moveFileTo(srcFile, destFile);
	}

	/**
	 * �ƶ�˽��Ŀ¼�µ�ָ��Ŀ¼�µ������ļ�
	 * 
	 * @param srcDirName
	 * @param destDirName
	 * @return
	 * @throws IOException
	 */
	public boolean moveDataFilesTo(String srcDirName, String destDirName)
			throws IOException {
		File srcDir = new File(FILESPATH + srcDirName);
		File destDir = new File(FILESPATH + destDirName);
		return moveFilesTo(srcDir, destDir);
	}

	/*
	 * ���ļ�д��Ӧ��˽�е�filesĿ¼����:writeFile("test.txt");
	 */
	public OutputStream wirteFile(String fileName) throws IOException {
		return context.openFileOutput(fileName, Context.MODE_WORLD_WRITEABLE);
	}

	/*
	 * ��ԭ���ļ��ϼ���д�ļ�����:appendFile("test.txt");
	 */
	public OutputStream appendFile(String fileName) throws IOException {
		return context.openFileOutput(fileName, Context.MODE_APPEND);
	}

	/*
	 * ��Ӧ�õ�˽��Ŀ¼files��ȡ�ļ�����:readFile("test.txt");
	 */
	public InputStream readFile(String fileName) throws IOException {
		return context.openFileInput(fileName);
	}

	/**********************************************************************************************************/
	/*********************************************************************************************************/
	/**
	 * 
	 * @param file
	 * @return
	 */
	public static boolean delFile(File file) {
		if (file.isDirectory())
			return false;
		return file.delete();
	}

	/**
	 * 
	 * @param dir
	 */
	public boolean delDir(File dir) {
		if (dir == null || !dir.exists() || dir.isFile()) {
			return false;
		}
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				delDir(file);// �ݹ�
			}
		}
		dir.delete();
		return true;
	}

	/**
	 * 
	 * @param path
	 * @throws IOException
	 */
	public static boolean copyFileTo(File srcFile, File destFile)
			throws IOException {
		if (srcFile.isDirectory() || destFile.isDirectory())
			return false;// �ж��Ƿ����ļ�
		FileInputStream fis = new FileInputStream(srcFile);
		FileOutputStream fos = new FileOutputStream(destFile);
		int readLen = 0;
		byte[] buf = new byte[1024];
		while ((readLen = fis.read(buf)) != -1) {
			fos.write(buf, 0, readLen);
		}
		fos.flush();
		fos.close();
		fis.close();
		return true;
	}

	/**
	 * 
	 * @param srcDir
	 * @param destDir
	 * @return
	 * @throws IOException
	 */
	public boolean copyFilesTo(File srcDir, File destDir) throws IOException {
		if (!srcDir.isDirectory() || !destDir.isDirectory())
			return false;// �ж��Ƿ���Ŀ¼
		if (!destDir.exists())
			return false;// �ж�Ŀ��Ŀ¼�Ƿ����
		File[] srcFiles = srcDir.listFiles();
		for (int i = 0; i < srcFiles.length; i++) {
			if (srcFiles[i].isFile()) {
				// ���Ŀ���ļ�
				File destFile = new File(destDir.getPath() + "\\"
						+ srcFiles[i].getName());
				copyFileTo(srcFiles[i], destFile);
			} else if (srcFiles[i].isDirectory()) {
				File theDestDir = new File(destDir.getPath() + "\\"
						+ srcFiles[i].getName());
				copyFilesTo(srcFiles[i], theDestDir);
			}
		}
		return true;
	}

	/**
	 * �ƶ�һ���ļ�
	 * 
	 * @param srcFile
	 * @param destFile
	 * @return
	 * @throws IOException
	 */
	public static boolean moveFileTo(File srcFile, File destFile)
			throws IOException {
		boolean iscopy = copyFileTo(srcFile, destFile);
		if (!iscopy)
			return false;
		delFile(srcFile);
		return true;
	}

	public static boolean moveFileTo(String srcPath, String destPath)
			throws IOException {
		if (srcPath == null || destPath == null) {
			return false;
		}
		
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);

		return moveFileTo(srcFile, destFile);
	}

	/**
	 * �ƶ�Ŀ¼�µ������ļ���ָ��Ŀ¼
	 * 
	 * @param srcDir
	 * @param destDir
	 * @return
	 * @throws IOException
	 */
	public boolean moveFilesTo(File srcDir, File destDir) throws IOException {
		if (!srcDir.isDirectory() || !destDir.isDirectory()) {
			return false;
		}
		File[] srcDirFiles = srcDir.listFiles();
		for (int i = 0; i < srcDirFiles.length; i++) {
			if (srcDirFiles[i].isFile()) {
				File oneDestFile = new File(destDir.getPath() + "\\"
						+ srcDirFiles[i].getName());
				moveFileTo(srcDirFiles[i], oneDestFile);
				delFile(srcDirFiles[i]);
			} else if (srcDirFiles[i].isDirectory()) {
				File oneDestFile = new File(destDir.getPath() + "\\"
						+ srcDirFiles[i].getName());
				moveFilesTo(srcDirFiles[i], oneDestFile);
				delDir(srcDirFiles[i]);
			}
		}
		return true;
	}

	public static List<String> scanFile(String path, String fileType,
			boolean scanSubFonder) {
		File file = new File(path);

		if (file.exists() == false)
			return null;

		if (file.isDirectory()) {
			List<String> returnList = new ArrayList<String>();
			
			File[] array = file.listFiles();

			for (int i = 0; i < array.length; i++) {
				File f = array[i];
				if (f.isFile()) {
					String name = f.getName().toLowerCase();
					String fileExtra = "." + fileType.toLowerCase();
					if (name.contains(fileExtra)) {
						returnList.add(f.getPath());
					}
				} else {
					if (scanSubFonder) {
						List<String> list = scanFile(f.getAbsolutePath(),
								fileType, true);
						returnList.addAll(list);
					}
				}
			}

			return returnList;
		}
		
		return null;
	}
	
	/**
	 * �ж��Ƿ�ָ���ļ�����
	 * @param path ȫ·��
	 * @param ext ��׺���硰.txt��
	 * @return
	 */
	public static boolean isFileType(String path, String ext) {
		if (path != null && ext != null) {
			return path.toLowerCase().endsWith(ext);
		}
		
		return ext == null ? true : false;
	}

	public static String getFileTitle(String path) {
		if (path == null) {
			return null;
		}

		File file = new File(path);
		String name = file.getName();
		int position = name.lastIndexOf('.');
		if (position != -1) {
			return name.substring(0, position);
		}
		
		return name;
	}	
}
