package com.alibaba.datax.algorithm;



import com.alibaba.datax.model.DesensitizationAlgorithmConfigCopies;
import com.alibaba.datax.model.DesensitizationAlgorithmCopies;
import com.alibaba.datax.utils.EncryptionUtils;

import java.util.LinkedHashMap;

/** 
 * 哈希脱敏算法
* @author liudaizhong.liu
* @date 2019年11月2日 下午4:57:10 
* @desc 
*/
public class HashDesensitizationAlgorithm extends AbstractDesensitizationAlgorithm {
	/**
	 * 1哈希脱敏algorithmType
	 * algoithmConfigType 参数配置类型([哈希=1:MD5、2:SHA-1、3:SHA-256、4:HMAC
	 * algoithmConfigValue 参数配置值([哈希=盐/替换=固定值])
	 */
	@Override
	public String desensitization(DesensitizationAlgorithmCopies desensitizationAlgorithm, DesensitizationAlgorithmConfigCopies algorithmConfig, LinkedHashMap<String, String> dictCodes) {
		Integer algoithmConfigType = algorithmConfig.getAlgoithmConfigType();
		//盐
		String algoithmConfigValue = algorithmConfig.getAlgoithmConfigValue();
		//脱敏数据
		String desensitizationData = desensitizationAlgorithm.getDesensitizationData();
		if(algoithmConfigValue == null) {
			algoithmConfigValue = "";
		}
		if (desensitizationData == null || desensitizationData.isEmpty()) {
			throw new RuntimeException("脱敏数据不允许为空!");
		}
		switch (algoithmConfigType) {
			case 1:
				return EncryptionUtils.MD5Salt(desensitizationData, algoithmConfigValue);
			case 2:
				return EncryptionUtils.SHA1Salt(desensitizationData, algoithmConfigValue);
			case 3:
				return EncryptionUtils.SHA256Salt(desensitizationData, algoithmConfigValue);
			case 4:
				return EncryptionUtils.HMACSalt(desensitizationData, algoithmConfigValue);
			default:
				throw new RuntimeException("未找到哈希参数配置信息");
		}
	}

}
