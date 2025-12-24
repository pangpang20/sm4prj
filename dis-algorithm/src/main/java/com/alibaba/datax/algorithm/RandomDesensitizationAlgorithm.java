package com.alibaba.datax.algorithm;


import com.alibaba.datax.model.DesensitizationAlgorithmConfigCopies;
import com.alibaba.datax.model.DesensitizationAlgorithmCopies;
import org.apache.commons.lang3.RandomUtils;

import java.util.LinkedHashMap;

/** 
 * 随机脱敏算法
* @author liudaizhong.liu
* @date 2019年11月2日 下午4:57:10 
* @desc 
*/
public class RandomDesensitizationAlgorithm extends AbstractDesensitizationAlgorithm {
	/**
	 * algoithmConfigType:1:打散重排、2:随机选择
	 */
	@Override
	public String desensitization(DesensitizationAlgorithmCopies desensitizationAlgorithm, DesensitizationAlgorithmConfigCopies algorithmConfig, LinkedHashMap<String, String> dictCodes) {
//		DesensitizationAlgorithmConfigCopies algorithmConfig = desensitizationAlgorithm.getAlgorithmConfigCopies();
		Integer algoithmConfigType = algorithmConfig.getAlgoithmConfigType();
		//脱敏数据
		String desensitizationData = desensitizationAlgorithm.getDesensitizationData();
		if (desensitizationData == null || desensitizationData.isEmpty()) {
			throw new RuntimeException("脱敏数据不允许为空!");
		}
		String resultData = desensitizationData;
		char[] dataArrays = resultData.toCharArray();
		switch (algoithmConfigType) {
		case 1:
			resultData = new String(shufflecard(dataArrays));
			break;
		case 2:
			int len = RandomUtils.nextInt((dataArrays.length / 2), dataArrays.length);
			resultData = new String(shufflecard(dataArrays)).substring(0, len);//随机选择,规则为从字符串一半到总长之间挑一个随机数，根据这个随机数截取打散后的字符
			break;
		default:
			throw new RuntimeException("未找到随机脱敏参数配置信息");
		}
		return resultData;
	}
	/**
	 * @author daizhong.liu
	 * @date 2019年11月8日 下午6:26:28 
	 * @desc 随机洗牌
	 * @param card 要重排的数据
	 * @return 重排后的数据
	 */
	private char[] shufflecard(char[] card){
        for(int i=0;i<card.length;i++) {
            int j = RandomUtils.nextInt(0, card.length);//生成随机数
            char temp = card[i];//交换
            card[i]=card[j];
            card[j]=temp;
        }
        return card;
    }
	

}
