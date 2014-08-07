package com.GreenLemonMobile.cipher;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.StringTokenizer;

import javax.crypto.Cipher;

import android.text.TextUtils;

/**
 * @author qt-liuguanqing
 *	Rsa解密辅助类
 */
public class RsaDecoder {

	private PrivateKey rsaPrivateKey;
	private  Cipher cipher;
	private final static String SPRIT_CHAR="|";//分段加密/解密,段落分割符
	private RsaDecoder(String privateKey) throws Exception{
		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		this.rsaPrivateKey = this.generatePrivate(privateKey);
	}
	/**
	 * 获取私钥,根据已经生成的合格的RSA私钥字符串,转化成RSAPrivateKey对象,(PKCS8EncodedKeySpec)
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private PrivateKey generatePrivate(String key) throws Exception {
		byte[] keyBytes;
		keyBytes = Base64.decode(key);

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}

	/**
	 * 解密
	 *
	 * @param encrypt 加密后的二进制字节
	 *
	 * @return 解密后的二进制
	 */
	private  byte[] dencrypt(byte[] encrypt) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
			byte[] decryptByteArray = cipher.doFinal(encrypt);
			return decryptByteArray;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 对加密数据进行解密,自动分段
	 *
	 * @param plainText
	 */
	public  String dencrypt(String plainTextA) {
		StringTokenizer tokenizer = new StringTokenizer(plainTextA,SPRIT_CHAR);
		StringBuffer sb = new StringBuffer();
		while (tokenizer.hasMoreTokens()) {
			byte[] tmp;
			String tmpBase64Str = (String) tokenizer.nextElement();
			try {
				tmp = Base64.decode(getFromatBase64String(tmpBase64Str,1));
				tmp = dencrypt(tmp);
				sb.append(new String(tmp));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//替换空格
		return sb.toString().replace("\u0000","");

	}

	/**
	 *
	 * @param str
	 * @return
	 */
	public   String getFromatBase64String(String str,int times){
		int timesModes = (int) (Math.pow(1.5, times-1)*10);
		final int  subLength = 172*timesModes/10;//这个数字是由RSA1024位　及base 64增大0.5倍，具体计算公式为：rsa密钥长度/8*(1.5);
		String ret = str.substring(str.length()-subLength, str.length());
		return ret;

	}

	/**
	 * 根据私钥获取解密对象
	 * @param privateKey
	 * @return
	 * @throws Exception
	 */
	public static RsaDecoder getInstance(String privateKey) throws Exception{
		if(TextUtils.isEmpty(privateKey)){
			return null;
		}
		return new RsaDecoder(privateKey);
	}

	/**
	 * 重置加密key
	 * @param publicKey
	 */
	public void reset(String privateKey) throws Exception{
		this.rsaPrivateKey = this.generatePrivate(privateKey);
	}
}
