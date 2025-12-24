package com.alibaba.datax.algorithm;

import com.alibaba.datax.model.DesensitizationAlgorithmConfigCopies;
import com.alibaba.datax.model.DesensitizationAlgorithmCopies;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;

/** 
 * 遮蔽脱敏算法
* @author liudaizhong.liu
* @date 2019年11月2日 下午4:57:10 
* @desc 
*/
public class ShadingDesensitizationAlgorithm extends AbstractDesensitizationAlgorithm {
	/**
	 * 算法类型：2遮蔽脱敏
	 * 参数配置类型algoithmConfigType：1:*、2:#
	 * 参数配置方式algoithmConfigStyle(遮蔽=1:保留前n后n、2:保留自x至y、3:遮盖前n后m、4:遮盖自x至y、5:特殊字符前遮盖、6:特殊字符后遮盖)
	 * 开始startIndex
	 * 结束endIndex
	 */
	@Override
	public String desensitization(DesensitizationAlgorithmCopies desensitizationAlgorithm, DesensitizationAlgorithmConfigCopies algorithmConfig, LinkedHashMap<String, String> dictCodes) {
//		DesensitizationAlgorithmConfigCopies algorithmConfig = desensitizationAlgorithm.getAlgorithmConfigCopies();
		//默认值
		String repeatWord = "*";
		if (algorithmConfig.getAlgoithmConfigType() == 2) {
			repeatWord = "#";
		}
		Integer algoithmConfigStyle = algorithmConfig.getAlgoithmConfigStyle();
		String desensitizationData = desensitizationAlgorithm.getDesensitizationData();
		String resultData = desensitizationData;
		int specialIndex = -1;
		switch (algoithmConfigStyle) {
		case 1://保留前n后n
			//算出前n的位置，后n的位置,daizhong.liu   ，保留前3后4，则为dai*****************.com
			int startPosition = algorithmConfig.getStartIndex();
			int endPosition = desensitizationData.length() - algorithmConfig.getEndIndex();
			//出现前n后n下标大于等于1时，不进行脱敏，返回原数据
			if(startPosition >= endPosition) {
				break;
			}
			resultData = StringUtils.overlay(desensitizationData, StringUtils.repeat(repeatWord, endPosition-startPosition), startPosition, endPosition);
			break;
		case 2://保留自x至y
			//位置出现颠倒则交换位置
			if(algorithmConfig.getStartIndex() > algorithmConfig.getEndIndex()) {
				int swap = algorithmConfig.getStartIndex();
				algorithmConfig.setStartIndex(algorithmConfig.getEndIndex());
				algorithmConfig.setEndIndex(swap);
			}
			resultData = StringUtils.overlay(desensitizationData, StringUtils.repeat(repeatWord, algorithmConfig.getStartIndex()-1), 0, algorithmConfig.getStartIndex()-1);
			resultData = StringUtils.overlay(resultData, StringUtils.repeat(repeatWord, desensitizationData.length()-algorithmConfig.getEndIndex()), algorithmConfig.getEndIndex(), desensitizationData.length());
			break;
		case 3://遮盖前n后m
			//超过总长度则全部替换
			if((algorithmConfig.getStartIndex()+algorithmConfig.getEndIndex())>desensitizationData.length()) {
				resultData = StringUtils.overlay(desensitizationData, StringUtils.repeat(repeatWord, desensitizationData.length()), 0, desensitizationData.length());
				break;
			}
			resultData = StringUtils.overlay(desensitizationData, StringUtils.repeat(repeatWord, algorithmConfig.getStartIndex()), 0, algorithmConfig.getStartIndex());
			resultData = StringUtils.overlay(resultData, StringUtils.repeat(repeatWord, algorithmConfig.getEndIndex()), desensitizationData.length() - algorithmConfig.getEndIndex() , desensitizationData.length());
			break;
		case 4://遮盖自x至y
			//位置出现颠倒则交换位置
			if(algorithmConfig.getStartIndex() > algorithmConfig.getEndIndex()) {
				int swap = algorithmConfig.getStartIndex();
				algorithmConfig.setStartIndex(algorithmConfig.getEndIndex());
				algorithmConfig.setEndIndex(swap);
			}
			resultData = StringUtils.overlay(desensitizationData, StringUtils.repeat(repeatWord, algorithmConfig.getEndIndex()-algorithmConfig.getStartIndex()+1), algorithmConfig.getStartIndex()-1, algorithmConfig.getEndIndex());
			break;
		case 5://特殊字符前遮盖
			if(algorithmConfig.getSpecialWord()==null||"".equals(algorithmConfig.getSpecialWord())) {
				throw new RuntimeException("特殊字符不允许为空，请检查!");
			}
			specialIndex = desensitizationData.indexOf(algorithmConfig.getSpecialWord());
			if(specialIndex != -1) {
				resultData = StringUtils.overlay(desensitizationData, StringUtils.repeat(repeatWord, specialIndex), 0, specialIndex);
			}
			break;
		case 6://特殊字符后遮盖
			if(algorithmConfig.getSpecialWord()==null||"".equals(algorithmConfig.getSpecialWord())) {
				throw new RuntimeException("特殊字符不允许为空，请检查!");
			}
			specialIndex = desensitizationData.lastIndexOf(algorithmConfig.getSpecialWord());
			if(specialIndex != -1) {
				resultData = StringUtils.overlay(desensitizationData, StringUtils.repeat(repeatWord, desensitizationData.length() - specialIndex - 1), specialIndex + 1, desensitizationData.length());
			}
			break;

		default:
			throw new RuntimeException("未找到遮蔽参数配置信息");
		}
		return resultData;
	}

}
