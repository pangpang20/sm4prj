package com.alibaba.datax.algorithm;


import com.alibaba.datax.model.DesensitizationAlgorithmConfigCopies;
import com.alibaba.datax.model.DesensitizationAlgorithmCopies;
import com.alibaba.datax.utils.AESUtils;
import com.alibaba.datax.utils.DESUtils;
import com.alibaba.datax.utils.SM4Utils;
import com.alibaba.datax.utils.ThreeDESUtils;

import java.util.LinkedHashMap;

/** 
 * 加密脱敏算法
* @author liudaizhong.liu
* @date 2019年11月2日 下午4:57:10 
* @desc 
*/
public class EncryptionDesensitizationAlgorithm extends AbstractDesensitizationAlgorithm {
	/**
	 *  algoithmConfigType:1:DES算法、2:3DES算法、3:AES算法、4:国密SM4算法
	 */
	@Override
	public String desensitization(DesensitizationAlgorithmCopies desensitizationAlgorithm, DesensitizationAlgorithmConfigCopies algorithmConfig, LinkedHashMap<String, String> dictCodes) {
//		DesensitizationAlgorithmConfigCopies algorithmConfig = desensitizationAlgorithm.getAlgorithmConfigCopies();
		Integer algoithmConfigType = algorithmConfig.getAlgoithmConfigType();
		//密钥
		String algoithmConfigValue = algorithmConfig.getAlgoithmConfigValue();
		//脱敏数据
		String desensitizationData = desensitizationAlgorithm.getDesensitizationData();
		if(algoithmConfigValue == null) {
			throw new RuntimeException("加密密钥不允许为空!");
		}
		if (desensitizationData == null || desensitizationData.isEmpty()) {
			throw new RuntimeException("脱敏数据不允许为空!");
		}
		String resultData = null;
		try {
			switch (algoithmConfigType) {
				case 1:
					//DES
					resultData = DESUtils.encrypt(desensitizationData, algoithmConfigValue);
					break;
				case 2:
					//3DES
					resultData = ThreeDESUtils.tDesEncryptECB(desensitizationData, algoithmConfigValue);
					break;
				case 3:
					//AES
					resultData = AESUtils.encrypt(desensitizationData, algoithmConfigValue);
					break;
				case 4:
					//SM4
					resultData = SM4Utils.encryptGcmKeyHexResultHex(algoithmConfigValue, desensitizationData);
					break;
				default:
					throw new RuntimeException("未找到加密脱敏参数配置信息");
			}
		}catch(Exception e) {
			try {
				throw e;
			} catch (Exception ex) {

			}
		}
		return resultData;
	}

}
