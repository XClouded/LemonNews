package com.GreenLemonMobile.cipher;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.StringTokenizer;

import javax.crypto.Cipher;

import android.text.TextUtils;

/**
 * @author qt-liuguanqing
 *	Rsa���ܸ�����
 */
public class RsaDecoder {

	private PrivateKey rsaPrivateKey;
	private  Cipher cipher;
	private final static String SPRIT_CHAR="|";//�ֶμ���/����,����ָ��
	private RsaDecoder(String privateKey) throws Exception{
		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		this.rsaPrivateKey = this.generatePrivate(privateKey);
	}
	/**
	 * ��ȡ˽Կ,�����Ѿ����ɵĺϸ��RSA˽Կ�ַ���,ת����RSAPrivateKey����,(PKCS8EncodedKeySpec)
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
	 * ����
	 *
	 * @param encrypt ���ܺ�Ķ������ֽ�
	 *
	 * @return ���ܺ�Ķ�����
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
	 * �Լ������ݽ��н���,�Զ��ֶ�
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
		//�滻�ո�
		return sb.toString().replace("\u0000","");

	}

	/**
	 *
	 * @param str
	 * @return
	 */
	public   String getFromatBase64String(String str,int times){
		int timesModes = (int) (Math.pow(1.5, times-1)*10);
		final int  subLength = 172*timesModes/10;//�����������RSA1024λ����base 64����0.5����������㹫ʽΪ��rsa��Կ����/8*(1.5);
		String ret = str.substring(str.length()-subLength, str.length());
		return ret;

	}

	/**
	 * ����˽Կ��ȡ���ܶ���
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
	 * ���ü���key
	 * @param publicKey
	 */
	public void reset(String privateKey) throws Exception{
		this.rsaPrivateKey = this.generatePrivate(privateKey);
	}
}
