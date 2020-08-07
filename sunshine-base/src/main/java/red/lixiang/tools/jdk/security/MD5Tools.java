package red.lixiang.tools.jdk.security;


import red.lixiang.tools.jdk.RandomTools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *  字符串加密
 * @author lixiang
 */

public class MD5Tools {
	/**
	 * 对字符串进行MD5加密
	 */
		public final static String md5(String pwd) {
	        //用于加密的字符
	        char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	                'a', 'b', 'c', 'd', 'e', 'f' };
	        try {
	            //使用平台的默认字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中
	            byte[] btInput = pwd.getBytes();
	             
	            // 获得指定摘要算法的 MessageDigest对象，此处为MD5
	            //MessageDigest类为应用程序提供信息摘要算法的功能，如 MD5 或 SHA 算法。
	            //信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。
	            MessageDigest mdInst = MessageDigest.getInstance("MD5");

	            //MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要
	            mdInst.update(btInput);

	            // 摘要更新之后，通过调用digest（）执行哈希计算，获得密文
	            byte[] md = mdInst.digest();

	            // 把密文转换成十六进制的字符串形式
	            int j = md.length;
	            //System.out.println(j);
	            char str[] = new char[j * 2];
	            int k = 0;
	            for (int i = 0; i < j; i++) {   //  i = 0
	                byte byte0 = md[i];  //95
	                str[k++] = md5String[byte0 >>> 4 & 0xf];    //    5 
	                str[k++] = md5String[byte0 & 0xf];   //   F
	            }
	             
	            //返回经过加密后的字符串
	            return new String(str);
	             
	        } catch (Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
	
	/**
	 * 字符串加密
	 * @param sourceStr 源字符串
	 * @param salt 加密随机串
	 * @return
	 */
	public static String encryption(String sourceStr, String salt) {
		String destStr = md5(sourceStr+salt);
		return destStr;
	}

	public static byte[] encryptSha256(String data) throws IOException {
		byte[] bytes = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			bytes = md.digest(data.getBytes(StandardCharsets.UTF_8));
		} catch (GeneralSecurityException gse) {
			throw new IOException(gse.getMessage());
		}
		return bytes;
	}

	public static String byte2hex(byte[] bytes) {
		StringBuilder sign = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex.toUpperCase());
		}
		return sign.toString();
	}

	private static byte[] md5(byte[] str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str);
        return md.digest();
    }


    public static String getSalt(){
        return RandomTools.getComCharStr(5);
    }

	public static void main(String[] args) {
		String s = "123456";
		String nzjZgzZr = AESTools.AESEncode("NzjZgzZr", "2665");
		System.out.println(nzjZgzZr);
		
	}
}
