package com.alibaba.datax.utils;



import com.alibaba.fastjson2.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * 身份证号码工具类
 * 身份证组成:2(省级)2(地级市)2(县级)8(出生:年月日)3(派出所代码)1(校验码) 其中第17位:奇数=男,偶数=女 加权码wi:7 9 10 5
 * 8 4 2 1 6 3 7 9 10 5 8 4 2 获取校验码:将身份证 (前17位*加权码)所得总和 再对11取余=校验码索引 校验码(18): 1
 * 0 X 9 8 7 6 5 4 3 2
 * @author liudaizhong.liu
 * @date 2019年11月5日 上午9:04:31
 * @desc
 */
public class IdentityCardUtils {
	private final static Map<String, String> areaCodeMap = new HashMap<>();

	private final static StringBuilder areaCodeInfo = new StringBuilder();

	static {
		areaCodeInfo.append("{ \"110000\": \"中华人民共和国\", \"110100\": \"北京市\", \"110101\": \"市辖区\", \"110102\": \"东城区\", \"110103\": \"西城区\", \"110104\": \"崇文区\", \"110105\": \"宣武区\", \"110106\": \"朝阳区\", \"110107\": \"丰台区\", \"110108\": \"石景山区\", \"110109\": \"海淀区\", \"110110\": \"门头沟区\"}");
		final HashMap<String, String> tempMap = JSONObject.parseObject(areaCodeInfo.toString(), HashMap.class);
		areaCodeMap.putAll(tempMap);
	}

	public static void main(String[] args) {
		System.out.println(areaCodeMap);
	}


	public static Boolean isEmptyForAreaCodeMap() {
		return areaCodeMap.isEmpty();
	}

	public static Boolean containsKeyForAreaCodeMap(String key) {
		return areaCodeMap.containsKey(key);
	}

	/**
	 * 检验身份证号码
	 * @author daizhong.liu
	 * @date 2019年11月5日 上午9:21:11 
	 * @param idCard 身份证号码
	 */
	public static Boolean checkIdCard(String idCard) {
		// 正则初步:校验身份证
		// 日期: ((0[1-9])|(1[0-2])):匹配"01-12"月
		// (([0-2][1-9])|10|20|30|31):匹配"01-31"天
		String regID = "[1-9]\\d{5}[1-9]\\d{3}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9xX]";
		if (!idCard.matches(regID)) {
			throw new RuntimeException("dsps.module.algorithm.service.0031");
		}
		boolean flag2 = false;
		if (!areaCodeMap.isEmpty()) {
			final String areaCodeStr = idCard.substring(0, 6);
			if (!areaCodeMap.containsKey(areaCodeStr)) {
				flag2 = true;
			}
		}
		if (flag2) {
			throw new RuntimeException("dsps.module.algorithm.service.0037");
		}
		// 进一步:年月日校验
		int year = Integer.parseInt(idCard.substring(6, 10));
		int month = Integer.parseInt(idCard.substring(10, 12));
		int day = Integer.parseInt(idCard.substring(12, 14));
		if (day > 28 && !isDate(year, month, day)) {
			throw new RuntimeException("dsps.module.algorithm.service.0032");
		}
		// 再进一步:检验最后一位校验码
		// 获取加权码wi:7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
		int[] wi = new int[17];
		for (int i = 1; i < 18; i++) {
			// 获取加权数字
			int winum = (int) (Math.pow(2, i) % 11);
			// 将加权数反向存入数组
			wi[wi.length - i] = winum;
		}
		// 获取加权后的总和
		int sum = 0;
		idCard = idCard.trim();// 去空格
		for (int i = 0; i < wi.length; i++) {
			// 获取身份证前17位的每一位,转换为数字
			int j = Integer.parseInt(idCard.substring(i, i + 1));
			// 获取加权后所得的和
			sum += (j * wi[i]);
		}
		// 匹配校验码
		String Ai = "10x987654342";
		// 获取验证码索引:
		int index = sum % 11;
		// 比较输入身份证与所得校验码比较(忽略大小写)
		if (idCard.substring(17).equalsIgnoreCase(Ai.substring(index, index + 1))) {
			// 判断男女
//			System.out.println(((Integer.parseInt(idCard.substring(16, 17)) % 2) == 0) ? "女生" : "男生");
		} else {
			throw new RuntimeException("dsps.module.algorithm.service.0033");
		}
//		boolean flag2 = true;
//		for (Entry<Object, Object> entry : areaCodes) {
//			String key = (String) entry.getKey();
//			// 匹配身份证所属区域
//			if (idCard.substring(0, 6).equals(key)) {
//				flag2 = false;
//				break;
//			}
//		}
		return true;
	}
	

	// 进一步日期判断
	public static boolean isDate(int year, int month, int day) {// day=29|30|31
		// 日期口诀: 一三五七八十腊（12月），三十一天永不差；平年二月二十八；闰年二月把一加 ；四六九冬（11月）三十日
		// 闰年:四年一闰,百年不闰,四百年再闰.
		if (month == 2) {// 当月份=2时
			if (day == 29 && (year % 4 == 0 && year % 100 != 0 || year % 400 == 0))
				return true;
			// 是2月非闰年
			return false;
		}
		// 天数是31天,月份却是:4|6|9|11
		if (day == 31 && (month == 4 || month == 6 || month == 9 || month == 11))
			return false;
		// 其它正常情况
		return true;
	}
	/**
	 * 生成身份证
	 * @author daizhong.liu
	 * @date 2019年11月5日 上午9:14:27 
	 * @return
	 */
	public static String generateIdCard() throws NoSuchAlgorithmException {
		// 获取6为地区码
		 Random ra = SecureRandom.getInstanceStrong();
        // 以生成的随机数=循环的次数:来获取随机地区码
        int num = ra.nextInt(3465) + 1;
        int count = 0;// 循环次数
        String str1 = "";// 六位地区码
        // 遍历地区码文件
		for (Entry<String, String> entry : areaCodeMap.entrySet()) {
            count++;
            // 匹配身份证所属区域
            if (num == count) {
				str1 = entry.getKey();// 地区码
                break;
            }
        }
        // 获取年份
        Calendar cal = Calendar.getInstance();
        // 获取现在年份
		int year = cal.get(Calendar.YEAR);
        // 获取150年前到现在的年份
        String str2 = Integer.toString(year - ra.nextInt(151));
        // 获取月份
        String str3 = "";
        int month = ra.nextInt(12) + 1;
        if (month < 10) {
			str3 = "0" + month;
        } else {
            str3 = Integer.toString(month);
        }
        // 获取天
        String str4 = "";
        int day = ra.nextInt(31) + 1;
        if (day < 10) {
			str4 = "0" + day;
        } else {
            str4 = Integer.toString(day);
        }
        // 判断生成日期是否符合规则
        if (day > 28 && !isDate(year, month, day)) {
            // 重新赋值天数
            str4 = Integer.toString(day - 3);
        }
        // 获取3位随机数
        String str5 = "";
        int ran3 = ra.nextInt(1000);
        if (ran3 < 10) {
			str5 = "00" + ran3;
        } else if (ran3 < 100) {
			str5 = "0" + ran3;
        } else {
            str5 = Integer.toString(ran3);
        }
        // 获取校验码
        // 获取加权后的总和
        String id = str1 + str2 + str3 + str4 + str5;// 前17位ID
        // 获取加权码wi:7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
        int[] wi = new int[17];
        for (int i = 1; i < 18; i++) {
            // 获取加权数字
            int winum = (int) (Math.pow(2, i) % 11);
            // 将加权数反向存入数组
            wi[wi.length - i] = winum;
        }
        // 获取加权后的总和
        int sum = 0;
        for (int i = 0; i < wi.length; i++) {
            // 获取身份证前17位的每一位,转换为数字
            int j = Integer.parseInt(id.substring(i, i + 1));
            // 获取加权后所得的和
            sum += (j * wi[i]);
        }
        // 匹配校验码
        String Ai = "10x987654342";
        // 获取验证码索引:
        int index = sum % 11;
        String str6 = Ai.substring(index, index + 1);// 校验码
        // 输出身份证
        return id + str6;
	}
	
	/**
	 * 校验身份证最末位校验码是否合法
	 * @author daizhong.liu
	 * @date 2019年11月5日 上午9:34:29 
	 * @param idCard 身份证号码
	 * @return true：合法,false：非法
	 */
	public static boolean checkIdCardCode(String idCard) {
		int[] wi = new int[17];
		for (int i = 1; i < 18; i++) {
			// 获取加权数字
			int winum = (int) (Math.pow(2, i) % 11);
			// 将加权数反向存入数组
			wi[wi.length - i] = winum;
		}
		// 获取加权后的总和
		int sum = 0;
		idCard = idCard.trim();// 去空格
		for (int i = 0; i < wi.length; i++) {
			// 获取身份证前17位的每一位,转换为数字
			int j = Integer.parseInt(idCard.substring(i, i + 1));
			// 获取加权后所得的和
			sum += (j * wi[i]);
		}
		// 匹配校验码
		String Ai = "10x987654342";
		// 获取验证码索引:
		int index = sum % 11;
		// 比较输入身份证与所得校验码比较(忽略大小写)
		if (idCard.substring(17).equalsIgnoreCase(Ai.substring(index, index + 1))) {
			// 判断男女
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 生成末位校验码,将身份证号码放入，截取前17位生成末位校验码
	 * @author daizhong.liu
	 * @date 2019年11月5日 上午10:46:18 
	 * @desc 
	 * @param idCard 身份证号码
	 * @return
	 */
	public static String generateIdCardCode(String idCard) {
		if (idCard == null || idCard.trim().isEmpty()) {
			throw new RuntimeException("dsps.module.algorithm.service.0014");
		}
		if(!idCard.matches("^[a-z0-9A-Z]+$")){
			throw new RuntimeException("数据格式不正确");
		}
		// 获取校验码
        // 获取加权后的总和
        String id = idCard.substring(0, 17);// 前17位ID
        // 获取加权码wi:7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
        int[] wi = new int[17];
        for (int i = 1; i < 18; i++) {
            // 获取加权数字
            int winum = (int) (Math.pow(2, i) % 11);
            // 将加权数反向存入数组
            wi[wi.length - i] = winum;
        }
        // 获取加权后的总和
        int sum = 0;
        for (int i = 0; i < wi.length; i++) {
            // 获取身份证前17位的每一位,转换为数字
            int j = Integer.parseInt(id.substring(i, i + 1));
            // 获取加权后所得的和
            sum += (j * wi[i]);
        }
        // 匹配校验码
        String Ai = "10x987654342";
        // 获取验证码索引:
        int index = sum % 11;
        String str6 = Ai.substring(index, index + 1);// 校验码
        return id + str6;
	}
	
	
}
