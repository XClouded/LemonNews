package com.GreenLemonMobile.cipher;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

import com.GreenLemonMobile.entity.BodyEncodeEntity;
import com.GreenLemonMobile.util.DeviceUtil;

import android.text.TextUtils;

/**
 * @author qt-liuguanqing
 *	Rsa加密辅助�?
 */
public class RsaEncoder {

	private PublicKey rsaPublicKey;

	private  Cipher cipher;

	private final static int PT_LEN = 117;// 1024位RSA加密算法�?当加密明文长度超�?17个字节后,会出现异�?�?��采用分段加密
	private final static String SPRIT_CHAR="|";//分段加密/解密,段落分割�?
	private static BodyEncodeEntity encodeEntity;
//    private static BodyEncodeEntity encodeEntity;
	
	public static BodyEncodeEntity getEncodeEntity() {
		return encodeEntity;
	}

	public static void setEncodeEntity(BodyEncodeEntity encodeEntity) {
		RsaEncoder.encodeEntity = encodeEntity;
	}

	
	/*
	 * �?��sessionkey是否存在: true 存在 false 不存�?
	 */
	public static boolean checkSessionKey() {
		BodyEncodeEntity encodeEntity = RsaEncoder.getEncodeEntity();
		if (encodeEntity != null) {
			if (encodeEntity.isSuccess = false || TextUtils
					.isEmpty(encodeEntity.desSessionKey)) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}
	
	private RsaEncoder(String publicKey) throws Exception{
		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		this.rsaPublicKey = this.generatePublic(publicKey);
	}

	private  PublicKey generatePublic(String key) throws Exception {
		byte[] keyBytes;
		keyBytes = Base64.decode(key);

		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}

	/**
	 * 加密输入的明文字符串
	 * 当value的字节长度大�?17,将会采用分段加密,即依次对117个字�?加密,并�?�?|"对段落进行分�?请解密�?注意
	 * @param value 加密后的字符�?1024个字节长�?
	 * @return
	 */
	public String encrypt(String value) throws IOException{
		if(TextUtils.isEmpty(value)){
			return null;
		}
		return encryptBySeg(value.getBytes(),Base64.NO_OPTIONS);
	}

	/**
	 * 分段加密
	 * @param plainText,各个段落�?|'分割
	 * @param option
	 * @return
	 * @throws IOException
	 */
	private String encryptBySeg(byte[] plainText,int option) throws IOException{
		//获取加密段落个数
		int length = plainText.length;//
		int mod = length %PT_LEN;//余数
		int ptime = length /PT_LEN;//段数
		int ptimes = (mod == 0 ? ptime : ptime +1);
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while(i < ptimes){
			int from = i*PT_LEN;
			int to = Math.min(length, (i+1)*PT_LEN);
			byte[] temp = copyofRange(plainText,from, to);
			sb.append(Base64.encodeBytes(encrypt(temp),option));
			if(i != (ptimes - 1)){
				sb.append(SPRIT_CHAR);
			}
			i++;
		}
		return sb.toString();

	}
	/*
	 * test
	 */
	private byte[] copyofRange(byte[] plainText,int from, int to){
		int length = to - from;
		byte[] result = new byte[length];
        for(int i=0;i<length;i++){
             result[i] = plainText[from];
             from=from+1;
        }
		return result;
	}
	/**
	 * 加密
	 * @param plainTextArray
	 * @return
	 */
	private byte[] encrypt(byte[] plainTextArray) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
			byte[] encryptByteArray = cipher.doFinal(plainTextArray);
			return encryptByteArray;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 根据公钥,获取加密工具类实�?
	 * @param publicKey
	 * @return
	 */
	public static RsaEncoder getInstance(String publicKey) throws Exception{
		if(TextUtils.isEmpty(publicKey)){
			return null;
		}
		return new RsaEncoder(publicKey);
	}

	/**
	 * 重置加密key
	 * @param publicKey
	 */
	public void reset(String publicKey) throws Exception{
		this.rsaPublicKey = this.generatePublic(publicKey);
	}

    /**
     * 字符错位算法
     * @param ori
     * @return
     */
    public static byte [] confuse(byte [] ori){
        for (int i = 0, byteLength = ori.length; i < byteLength; i++) {
                                ori[i] = (byte) ~ori[i];
                            }
            return ori;

    }
    /*
     * 获取是否成功获取到了加密key
     */
    public static boolean ifdesKeySuccess(){
    	if(encodeEntity==null){
    		return false;
    	}
    	return encodeEntity.isSuccess;
    }
/*
 * 生成�?��信封
 */
    public static String stringEnvelope(BodyEncodeEntity encodeEntity){
    	//BodyEncodeEntity encodeEntity = (BodyEncodeEntity)DataIntent.get(KEY_BODYENCODEENTITY, true);
    	if(encodeEntity==null){return "";}
    	String templeStr = System.currentTimeMillis() + DeviceUtil.getDeviceId();
    	String strEnvelope = MD5Calculator.calculateMD5(templeStr);
        encodeEntity.strEnvelope = strEnvelope;
        //DataIntent.put(KEY_BODYENCODEENTITY, encodeEntity);
    	return stringRsaPublicKey(strEnvelope,encodeEntity.sourcePublicKey);
    }
    /*
     * 加密信封
     */
    public static String stringRsaPublicKey(String info,String cSourcePublicKey ){
		String jResult = "";
		try {
			if(TextUtils.isEmpty(cSourcePublicKey)){return "";}
			//="z35gz/L59tV5t3kI8v7+/vr//H5y/89+dv1+fv9ty1Bwsoodk1/EkSruncOdFCQMJYGVKR4aqFqC  IrBS3lK3uHhodLL/efU4rANQCqc7/id1UYeCrRFIi2Tw0sEKisXwrwmAnXvgWZSFfmSBP8La5zQl  J9hOFCTFBl5NHl2W9cejAlDtRVshMJukiBVcNvvxigCaKMMWt28dg8cMRP38/v/+";
			//RsaUtil rsaUtil = new RsaUtil();
			String jPublicKey = Base64.encodeBytes(confuse(Base64.decode(cSourcePublicKey)));
			RsaEncoder rsaEncoder = RsaEncoder.getInstance(jPublicKey);
			jResult = rsaEncoder.encrypt(info);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jResult;
	}
 /*
  * 加密body
  */
	public static String stringBodyEncoder(String info,BodyEncodeEntity encodeEntity){
		String jResult = "";
		try {
//			BodyEncodeEntity encodeEntity = (BodyEncodeEntity)DataIntent.get("BodyEncodeEntity", false);
	    	if(encodeEntity==null){return "";}
			String desSessionKey=encodeEntity.desSessionKey;
			//RsaUtil rsaUtil = new RsaUtil();
			//RsaEncoder rsaEncoder = RsaEncoder.getInstance(jPublicKey);
			//jResult = rsaEncoder.encrypt(info);
			jResult = DesUtil.encrypt(info, desSessionKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jResult;
	}

	/*
	 * rsa
	 */

	public static String stringRSAEncoder(String info){
		String jResult = "";
		try {
			String cSourcePublicKey="z35gz/L59tV5t3kI8v7+/vr//H5y/89+dv1+fv9eoV+NVmZWqeldWlxIEL+zoGXx1VgfbheUzTIa  uaQKFbLK3ORRHWvDdvo2+PSmxV4fUmg29JaOXCDbSCzv04JRTMRZHLvUivmTJZvyr8bSTO8cqjwY  fhqR0PFh4XSozXJrmK9fCFeVgyjZGy5yEfPhDrRVW/n0cX+xNNcsqYar/v38/v/+";
			String jSourcePublicKey="z35gz/L59tV5t3kI8v7+/vr//H5y/89+dv1+fv9ty1Bwsoodk1/EkSruncOdFCQMJYGVKR4aqFqC  IrBS3lK3uHhodLL/efU4rANQCqc7/id1UYeCrRFIi2Tw0sEKisXwrwmAnXvgWZSFfmSBP8La5zQl  J9hOFCTFBl5NHl2W9cejAlDtRVshMJukiBVcNvvxigCaKMMWt28dg8cMRP38/v/+";
			//String cPrivateKey="MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKFeoHKpmalWFqKlo7fvQExfmg4q  p+CR6GsyzeVGW/XqTTUjG67ilDyJBckHC1k6oeCtl8kLaXGj3yS30xAsfa6zO6bjRCt1BmzaZA1Q  OS2zEONVw+eB5W4vDp4ei1cyjZRnUKD3qGp81ybk0Y3uDB7xS6qkBguOgE7LKNNWeVQBAgMBAAEC  gYAo3wW2ZPx1ZkePZHKmCKP5dBFZ2zXv7CS42gJKOtrZ20E43y6IyfyPpIjhlLYsIGyVsoIKOqi3  TrEa5LBBnXMrX9OmpwA9sOp4dQqzCXveN31ISfQyE7aNOnM8PF7EixDjozKwa/ZFSqSVJUjnB0J+  8HOgT9toICDrDYP5KJ3qDQJBAOHhLXxU62a4Pht8lIQtni6nLKMuoO36jHb+Q7BbsJXaTYTAyZ0j  2zTD60qDnvcNvnf9u3Z+k9R35XiVBMEesnsCQQC240fmnrvGEDllFJjjgSG/vG220R1uggooNcIH  u9AZ/KmKyWd/rPaJi8tvpQ0NRXDyLZy0pqr7s6FobknspRizAkB6CmC6CWO6dxdPYIsZs1AA4uAS  NrJKghF8hTprQc7x2CYD8Om9lk7sfmJVOzIbR3i+ef/cMN2McU8xTEpqUTybAkB0aa+2yItVw7YE  9VtsVSIaXeKoX+uQEA5PEgjzy0TnhcCVqyXKS1qSqv1Pj4wDSpReU1JQW1ay6OBxDOLZUy2pAkA1  jTiB/j6/hnXRjejcYz2ZvASZ8Pyintfqbw035rbFyxoH8F8It5Br6rYXMQL6Moq7chAG+Mw6DhZ3  xYt+0TTa";
			//String jPrivateKey="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJI0r49NdeJsoDtu1RFiPGLr2/Pa  fmrW4eVXpX3dT60hrUhHh5eLTQCGCsdT/K/1WMQB2IqueH1S7rd0mw8tPvV1Og9Q9n9ihB+ma3qB  m37APSUYy9rYJ7Hr2zr5obLhomkKOFz9rxK6pN7PZFt36qPJBA51/2XXPOlIkOJ8OPO7AgMBAAEC  gYBmzckMWIkllv/sCnijapHPdM0KYH43nbTlUIW7RNx7foyboEBFXNveHGKD2hh5mWi7GhkrGpb6  eFAGi2VkfIohcJXknqctESVKbxHdFB6O/azsULD59bCht+0s6Sl5rGxZKqojzUpBm+9RU8zk298Z  16HCN6NkrAm7RhutCCi8MQJBAPv6z07yOQ7y952SWZb8NMXGDBHQ2Vd6o/9orfFDs0n7Di5rvExj  /4CSIhioOhfuiqAs8b1HDyhp4evWegZ8MA8CQQCUidnoxkSRtO9YnBYBhfNY9iuKKulJ8+6wRG/o  7AVcACoZenCWMbGE17MKlef5IkanSuWa5rwgSKkqzYXqRFWVAkEAryP1U/oojqtyUKOYgclrJMwz  N27iolssAirQPBHCXHmTsdBQYQhgXw0zhc/oERpMYGjc3aK2dnCiBzpcNoW7IQJAT+8vOTkZRWAd  PY4huYggQMuY+q3FBWskE++AWdRJvgzImxVs6Sas5VQ+oX+yajur0fNYRFfO/YCcqNAZNMy5UQJA  UHXXhMvKzDt8g+EwlXtVWd67rs4AFms2aeGECdbZDM1oyuFjPPOjGqG/zdFXmptWyH4HnWL8UKwx  464SVClwig==";
			@SuppressWarnings("unused")
			String tmpResult ="cjka8VVyo94B/QcPaCWPegXqTSMDQ0Ljvh+yBn02Kr7f1ml7q2Db/X3r8U1M5te+vyDWF1HfjxsZv0a01b2qPJMo4hzbM8bgQGnqpVFZ/jZUUwNTWCqZVujR5m+L3LyI0a6ZA/xd764AglksvYzhZ7jxCCBlI3Aap+oVOzj77hI=|HzEevQJRWuYmaWmTkRe7s9DwkJ9ZsgcXTVX3j5QB7czouhwhsoVG9gA+HZpdenLQ9iIhUaMfPSmj9lGzYsClUmltOE84/jNxR70lOUQrpe7xbWc+Fx/XI9nxLHiIO+sP2cbT+i2bsB0txm2b84MTW3bAwNAigpdJ9FuEqGCCW2g=";
			//RsaUtil rsaUtil = new RsaUtil();
			String jPublicKey = Base64.encodeBytes(confuse(Base64.decode(jSourcePublicKey)));
			String cPublicKey = Base64.encodeBytes(confuse(Base64.decode(cSourcePublicKey)));

			String infoJson = info;
			RsaEncoder rsaEncoder = RsaEncoder.getInstance(cPublicKey);
			String cResult;

				cResult = rsaEncoder.encrypt(infoJson);

			rsaEncoder.reset(jPublicKey);
			 jResult = rsaEncoder.encrypt(cResult);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jResult;
		}
}
