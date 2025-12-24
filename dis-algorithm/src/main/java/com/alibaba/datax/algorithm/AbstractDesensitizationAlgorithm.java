package com.alibaba.datax.algorithm;


import com.alibaba.datax.DesensitizationAlgorithmBase;
import com.alibaba.datax.model.DesensitizationAlgorithmConfigCopies;
import com.alibaba.datax.model.DesensitizationAlgorithmCopies;

import java.util.LinkedHashMap;

/** 
 * 脱敏算法父类
* @author liudaizhong.liu
* @date 2019年11月2日 下午4:41:52 
* @desc 
*/
public abstract class AbstractDesensitizationAlgorithm implements DesensitizationAlgorithmBase {
	@Override
	public String execute(DesensitizationAlgorithmCopies desensitizationAlgorithm, DesensitizationAlgorithmConfigCopies algorithmConfig, LinkedHashMap<String, String> dictCodes) throws RuntimeException {
		prepare(desensitizationAlgorithm);
		return desensitization(desensitizationAlgorithm,algorithmConfig, dictCodes);
	}
	/**
	 * 脱敏算法
	 * @author daizhong.liu
	 * @date 2019年11月2日 下午4:43:24 
	 * @desc 
	 * @param desensitizationAlgorithm 具体业务的算法配置信息
	 * @param dictCodes 字典码(允许为空，主要看各算法是否需要字典支持)
	 * @return 脱敏后的字符
	 */
	public abstract String desensitization(DesensitizationAlgorithmCopies desensitizationAlgorithm, DesensitizationAlgorithmConfigCopies algorithmConfig, LinkedHashMap<String, String> dictCodes);
	/**
	 * @author daizhong.liu
	 * @date 2019年11月4日 上午9:04:16 
	 * @desc 脱敏算法准备阶段，同种脱敏类型有不同的实例时可以在此定义，可不重写此方法
	 * @param desensitizationAlgorithm 脱敏算法
	 */
	protected void prepare(DesensitizationAlgorithmCopies desensitizationAlgorithm) {}
}
