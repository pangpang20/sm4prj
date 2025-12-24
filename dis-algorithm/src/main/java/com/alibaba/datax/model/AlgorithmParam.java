package com.alibaba.datax.model;

import java.util.LinkedHashMap;

/**
 * @Description: 算法参数
 * @Title: AlgorithmParam
 * @Package: com.audaque.cloud.dsps.desensitization.model
 * @author: huafu.su
 * @Date: 2025/3/20 13:54
 */
public class AlgorithmParam {
    private DesensitizationAlgorithmCopies algorithm;
    private DesensitizationAlgorithmConfigCopies configCopies;
    private LinkedHashMap<String, String> dictCodes;


    public DesensitizationAlgorithmCopies getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(DesensitizationAlgorithmCopies algorithm) {
        this.algorithm = algorithm;
    }

    public DesensitizationAlgorithmConfigCopies getConfigCopies() {
        return configCopies;
    }

    public void setConfigCopies(DesensitizationAlgorithmConfigCopies configCopies) {
        this.configCopies = configCopies;
    }

    public LinkedHashMap<String, String> getDictCodes() {
        return dictCodes;
    }

    public void setDictCodes(LinkedHashMap<String, String> dictCodes) {
        this.dictCodes = dictCodes;
    }
}
