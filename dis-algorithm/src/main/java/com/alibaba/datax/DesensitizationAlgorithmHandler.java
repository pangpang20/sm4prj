package com.alibaba.datax;


import com.alibaba.datax.model.AlgorithmParam;
import com.alibaba.datax.model.AlgorithmType;
import com.alibaba.datax.model.DesensitizationAlgorithmConfigCopies;
import com.alibaba.datax.model.DesensitizationAlgorithmCopies;

import java.util.LinkedHashMap;

/** 
 * 脱敏算法处理器
* @author liudaizhong.liu
* @date 2019年11月4日 上午9:49:50 
* @desc 
*/
public class DesensitizationAlgorithmHandler implements DesensitizationAlgorithmBase {
    private static final DesensitizationAlgorithmHandler INSTANCE = new DesensitizationAlgorithmHandler();

    private DesensitizationAlgorithmHandler() {
    }

    public static DesensitizationAlgorithmHandler getInstance() {
        return INSTANCE;
    }
	
	@Override
    public String execute(DesensitizationAlgorithmCopies desensitizationAlgorithm,
                          DesensitizationAlgorithmConfigCopies algorithmConfig, LinkedHashMap<String, String> dictCodes) throws InstantiationException, IllegalAccessException {
        DesensitizationAlgorithmBase instance = AlgorithmType.getAlgorithm(desensitizationAlgorithm.getAlgorithmType());
		return instance.execute(desensitizationAlgorithm,algorithmConfig, dictCodes);
	}

    public String execute(AlgorithmParam param, String data) throws InstantiationException, IllegalAccessException {
        final DesensitizationAlgorithmCopies algorithm = param.getAlgorithm();
        algorithm.setDesensitizationData(data);
        return INSTANCE.execute(algorithm, param.getConfigCopies(), param.getDictCodes());
    }

}
