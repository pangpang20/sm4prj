package com.alibaba.datax;


import com.alibaba.datax.model.DesensitizationAlgorithmConfigCopies;
import com.alibaba.datax.model.DesensitizationAlgorithmCopies;

import java.util.LinkedHashMap;


/** 
 * 脱敏接口
* @author liudaizhong.liu
* @date 2019年11月2日 下午4:28:47 
* @desc 
*/
public interface DesensitizationAlgorithmBase {
	/**
	 * 执行脱敏
	 * @author daizhong.liu
	 * @date 2019年11月2日 下午4:31:11 
	 * @desc 
	 * @param desensitizationAlgorithm 脱敏的算法
	 * @param algorithmConfig 需要脱敏的配置信息
	 * @param dictCodes 字典码(允许为空，主要看各算法是否需要字典支持)
	 * @return 脱敏后的数据
	 */
    String execute(DesensitizationAlgorithmCopies desensitizationAlgorithm, DesensitizationAlgorithmConfigCopies algorithmConfig, LinkedHashMap<String, String> dictCodes) throws InstantiationException, IllegalAccessException;
	
}
