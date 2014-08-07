package com.GreenLemonMobile.cipher;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

/**
 * Created by IntelliJ IDEA.
 * User: George
 * Date: 11-9-2
 * Time: ����3:18
 * To change this template use File | Settings | File Templates.
 */
public class DesUtil {
             private final static String DES = "DES";
             private final static String PADDING="DES/ECB/PKCS5Padding";



             /**

              * ����

              * @param src ����?

              * @param key ��Կ�����ȱ�����8�ı���

              * @return  ���ؼ��ܺ�����

              * @throws Exception

              */

             public static byte[] encrypt(byte[] src, byte[] key)throws Exception {

                     //DES�㷨Ҫ����һ�������ε�������?

                     SecureRandom sr = new SecureRandom();

                     // ��ԭʼ�ܳ���ݴ���DESKeySpec����

                     DESKeySpec dks = new DESKeySpec(key);

                     // ����һ���ܳ׹�����Ȼ�������DESKeySpecת����

                     // һ��SecretKey����

                     SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);

                     SecretKey securekey = keyFactory.generateSecret(dks);

                     // Cipher����ʵ����ɼ��ܲ���?

                     Cipher cipher = Cipher.getInstance(PADDING);

                     // ���ܳ׳�ʼ��Cipher����

                     cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

                     // ���ڣ���ȡ��ݲ�����?

                     // ��ʽִ�м��ܲ���

                     return cipher.doFinal(src);

                  }


                  /**

                  * ����

                  * @param src ����?

                  * @param key ��Կ�����ȱ�����8�ı���

                  * @return   ���ؽ��ܺ��ԭʼ���

                  * @throws Exception

                  */

                  public static byte[] decrypt(byte[] src, byte[] key)throws Exception {

                     // DES�㷨Ҫ����һ�������ε�������?

                     SecureRandom sr = new SecureRandom();

                     // ��ԭʼ�ܳ���ݴ���һ��DESKeySpec����

                     DESKeySpec dks = new DESKeySpec(key);

                     // ����һ���ܳ׹�����Ȼ�������DESKeySpec����ת����

                     // һ��SecretKey����

                     SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);

                     SecretKey securekey = keyFactory.generateSecret(dks);

                     // Cipher����ʵ����ɽ��ܲ���?

                     Cipher cipher = Cipher.getInstance(PADDING);

                     // ���ܳ׳�ʼ��Cipher����

                     cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

                     // ���ڣ���ȡ��ݲ�����?

                     // ��ʽִ�н��ܲ���

                     return cipher.doFinal(src);

                  }

               /**

                * �������?

                * @param data

                * @return

                * @throws Exception

                */

               public final static String decrypt(String data,String key){

                  try {

                   return new String(decrypt(Base64.decode(data.getBytes()),

                      key.getBytes()),"utf-8");

                 }catch(Exception e) {
                 }

                 return null;

               }

               /**

                * �������?

                * @param code

                * @return

                * @throws Exception

                */

               public final static String encrypt(String code,String key){

                 try {

                   return Base64.encodeBytes(encrypt(code.getBytes("utf-8"), key.getBytes()));
                 }catch(Exception e) {
                 }

                 return null;

               }

    public static void main(String[] args) {
       System.out.print( DesUtil.encrypt("mGsAmeU/lQA=","1234567890abcdEfg"));
    }

}
