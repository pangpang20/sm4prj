package com.alibaba.datax.algorithm;



import com.alibaba.datax.model.DesensitizationAlgorithmConfigCopies;
import com.alibaba.datax.model.DesensitizationAlgorithmCopies;
import com.alibaba.datax.utils.BankCardUtils;
import com.alibaba.datax.utils.IdentityCardUtils;
import com.alibaba.datax.utils.UnifiedCreditCodeUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/** 
 * 替换脱敏算法
* @author liudaizhong.liu
* @date 2019年11月2日 下午4:57:10 
* @desc 
*/
public class ReplaceDesensitizationAlgorithm extends AbstractDesensitizationAlgorithm {
	/**
	 * 3 替换脱敏
	 * 参数配置类型algoithmConfigType  替换=1:身份证、2:统一信用代码、3:银行卡号
	 */
	@Override
	public String desensitization(DesensitizationAlgorithmCopies desensitizationAlgorithm, DesensitizationAlgorithmConfigCopies algorithmConfig, LinkedHashMap<String, String> dictCodes) {
		String resultData = validateCard(desensitizationAlgorithm, algorithmConfig, dictCodes);
		/*
		//替换=1:身份证、2:统一信用码、3:银行卡号
		Integer algoithmConfigType = algorithmConfig.getAlgoithmConfigType();
		switch (algoithmConfigType) {
		case 1://身份证
		case 2://统一信用码
			resultData = validateCard(desensitizationAlgorithm, dictCodes);
			break;
		case 3://银行卡号
			
			break;
		default:
			throw new RuntimeException("未找替换脱敏类型信息");
		}
			
		*/
		return resultData;
	}
	
	/**
	  * 身份证
	  *  开始startIndex
	  * 结束endIndex
	  * 关联字典dictItems
	  * 是否计算校验位：1是，2否 verificationBit
	 * @author daizhong.liu
	 * @date 2019年11月5日 下午1:55:58 
	 * @desc
	 * @param desensitizationAlgorithm 算法信息
	 * @param dictCodes 字典信息
	 * @return 脱敏数据
	 */
	private String validateCard(DesensitizationAlgorithmCopies desensitizationAlgorithm, DesensitizationAlgorithmConfigCopies algorithmConfig, LinkedHashMap<String, String> dictCodes) {
//		DesensitizationAlgorithmConfigCopies algorithmConfig = desensitizationAlgorithm.getAlgorithmConfigCopies();
		//替换=1:映射替换、2:随机替换、3:固定替换
		Integer algoithmConfigStyle = algorithmConfig.getAlgoithmConfigStyle();
		String desensitizationData = desensitizationAlgorithm.getDesensitizationData();
		String resultData = desensitizationData;
		Integer algoithmConfigType = 3;
		String targetData;
		String replaceData = null;
		switch (algoithmConfigStyle) {
		case 1://映射替换
			//从指定坐标中取出替换值
			if(algorithmConfig.getStartIndex() > algorithmConfig.getEndIndex()) {
				int swap = algorithmConfig.getStartIndex();
				algorithmConfig.setStartIndex(algorithmConfig.getEndIndex());
				algorithmConfig.setEndIndex(swap);
			}
			/*if (algorithmConfig.getStartIndex() > desensitizationData.length()
					|| desensitizationData.length() < algorithmConfig.getEndIndex()) {//算法与数据彻底对应不上不处理
				return resultData;
			}*/
			algoithmConfigType = algorithmConfig.getAlgoithmConfigType();
			targetData = StringUtils.substring(desensitizationData,algorithmConfig.getStartIndex()-1,algorithmConfig.getEndIndex());
			//将【映射替换】【随机替换】中的【替换位置】可以做到模糊位置替换；
			for (Map.Entry<String, String> entry : dictCodes.entrySet()) {//2024-05-27更改为模糊位置替换；选中的内容全部按字典KEY替换
				if (targetData.contains(entry.getKey())) {
					replaceData = targetData.replace(entry.getKey(), entry.getValue());
					break;
				}
			}
//			String replaceData = dictCodes.get(targetData);
			if (replaceData != null && !replaceData.isEmpty()) {
				resultData = StringUtils.overlay(desensitizationData, replaceData, algorithmConfig.getStartIndex()-1, algorithmConfig.getEndIndex());
				//计算出合法的校验码
				if (Integer.valueOf(1).equals(algorithmConfig.getVerificationBit())) {
					if (Integer.valueOf(1).equals(algoithmConfigType)) {//身份证
						if (!IdentityCardUtils.checkIdCard(desensitizationData)) {
							throw new RuntimeException("数据格式不正确");
						}
						resultData = IdentityCardUtils.generateIdCardCode(resultData);
					} else if (Integer.valueOf(2).equals(algoithmConfigType)) {//统一社会信用代码
						if (!UnifiedCreditCodeUtils.checkUnifiedCreditCode(desensitizationData)) {
							throw new RuntimeException("数据格式不正确");
						}
						resultData = UnifiedCreditCodeUtils.generateUnifiedCreditCode(resultData);
					} else if (Integer.valueOf(3).equals(algoithmConfigType)) {//银行卡号
						if (!BankCardUtils.checkBankCard(desensitizationData)) {
							throw new RuntimeException("数据格式不正确");
						}
						resultData = BankCardUtils.generateBankCardCode(resultData);
					}
				}
			}
			break;
		case 2://随机替换
			//截取的下标位置有误
			if(algorithmConfig.getStartIndex() > algorithmConfig.getEndIndex()) {
				int swap = algorithmConfig.getStartIndex();
				algorithmConfig.setStartIndex(algorithmConfig.getEndIndex());
				algorithmConfig.setEndIndex(swap);
			}
			algoithmConfigType = algorithmConfig.getAlgoithmConfigType();
			//截取的区间位置有误
			if(algorithmConfig.getStartRange() > algorithmConfig.getEndRange()) {
				int swap = algorithmConfig.getStartRange();
				algorithmConfig.setStartRange(algorithmConfig.getEndRange());
				algorithmConfig.setEndRange(swap);
			}
			//截取的区间大于字典码的最大长度
			if(algorithmConfig.getEndRange() > dictCodes.size()) {
				algorithmConfig.setEndRange(dictCodes.size());
			}
			targetData = StringUtils.substring(desensitizationData,algorithmConfig.getStartIndex()-1,algorithmConfig.getEndIndex());
			int randomIndex = RandomUtils.nextInt(algorithmConfig.getStartRange(), algorithmConfig.getEndRange());
			int i = 1;//将【映射替换】【随机替换】中的【替换位置】可以做到模糊位置替换；
			for (Map.Entry<String, String> entry : dictCodes.entrySet()) {
				if(randomIndex == i) {
					if (targetData.contains(entry.getKey())) {
						replaceData = targetData.replace(entry.getKey(), entry.getValue());
						resultData = StringUtils.overlay(desensitizationData, replaceData, algorithmConfig.getStartIndex() - 1, algorithmConfig.getEndIndex());
						//计算出合法的校验码
						if (Integer.valueOf(1).equals(algorithmConfig.getVerificationBit())) {
							if (Integer.valueOf(1).equals(algoithmConfigType)) {//身份证
								if (!IdentityCardUtils.checkIdCard(desensitizationData)) {
									throw new RuntimeException("数据格式不正确");
								}
								resultData = IdentityCardUtils.generateIdCardCode(resultData);
							} else if (Integer.valueOf(2).equals(algoithmConfigType)) {//统一社会信用代码
								if (!UnifiedCreditCodeUtils.checkUnifiedCreditCode(desensitizationData)) {
									throw new RuntimeException("数据格式不正确");
								}
								resultData = UnifiedCreditCodeUtils.generateUnifiedCreditCode(resultData);
							} else if (Integer.valueOf(3).equals(algoithmConfigType)) {//银行卡号
								if (!BankCardUtils.checkBankCard(desensitizationData)) {
									throw new RuntimeException("数据格式不正确");
								}
								resultData = BankCardUtils.generateBankCardCode(resultData);
							}
						}
					}
					break;
				}
				i++;
			}
//			Iterator<String> iterator = dictCodes.values().iterator();
//			for(int i=1;iterator.hasNext();i++) {
//				String data = iterator.next();
//				if(randomIndex == i) {
//					resultData = StringUtils.overlay(desensitizationData, data, algorithmConfig.getStartIndex()-1, algorithmConfig.getEndIndex());
//					//计算出合法的校验码
//					if (Integer.valueOf(1).equals(algorithmConfig.getVerificationBit())) {
//						if (Integer.valueOf(1).equals(algoithmConfigType)) {//身份证
//							resultData = IdentityCardUtils.generateIdCardCode(resultData);
//						} else if (Integer.valueOf(2).equals(algoithmConfigType)) {//统一社会信用代码
//							resultData = UnifiedCreditCodeUtils.generateUnifiedCreditCode(resultData);
//						}
//					}
//					break;
//				}
//			}
			break;
		case 3://固定替换
			String algoithmConfigValue = algorithmConfig.getAlgoithmConfigValue();
			if (algoithmConfigValue != null && !algoithmConfigValue.isEmpty()) {
				resultData = StringUtils.overlay(desensitizationData, algoithmConfigValue, 0, desensitizationData.length());
			}
			break;
		default:
			throw new RuntimeException("未找到替换参数配置信息");
		}
		return resultData;
	}
}
