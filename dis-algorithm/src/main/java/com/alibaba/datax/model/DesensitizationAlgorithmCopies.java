package com.alibaba.datax.model;

import java.io.Serializable;

/**
 * @author daizhong.liu
 * @description 脱敏算法信息副本
 * @fileName DesensitizationAlgorithm.java
 * @date 2019-11-02 16:06:27
 */
public class DesensitizationAlgorithmCopies implements Serializable {

	private static final long serialVersionUID = -1279994651334529359L;
	/**
	 * 算法名称
	 */
	private String algorithmName;
	/**
	 * 算法类型:1哈希脱敏，2遮蔽脱敏，3替换脱敏，4变换脱敏，5加密脱敏，6随机脱敏
	 */
	private Integer algorithmType;
	/**
	 * 是否可逆：0不可逆，1可逆
	 */
	private Integer isReversible;
	/**
	 * 是否启用：0不启用，1启用
	 */
	private Integer isEnable;
	/**
	 * 脱敏数据
	 */
	private String desensitizationData;
	/**
	 * 是否内置
	 */
	private Integer isInside;
	/**
	 * 算法描述
	 */
	private String description;

//	/** 算法配置信息不放库，只用于关联 */
//	private DesensitizationAlgorithmConfigCopies algorithmConfigCopies;

	/**
	 * 算法名称
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	/**
	 * 算法类型:1哈希脱敏，2遮蔽脱敏，3替换脱敏，4变换脱敏，5加密脱敏，6随机脱敏
	 */
	public Integer getAlgorithmType() {
		return algorithmType;
	}

	/**
	 * 算法类型:1哈希脱敏，2遮蔽脱敏，3替换脱敏，4变换脱敏，5加密脱敏，6随机脱敏
	 */
	public void setAlgorithmType(Integer algorithmType) {
		this.algorithmType = algorithmType;
	}

	/**
	 * 是否可逆：0不可逆，1可逆
	 */
	public Integer getIsReversible() {
		return isReversible;
	}

	public void setIsReversible(Integer isReversible) {
		this.isReversible = isReversible;
	}

	/**
	 * 是否启用：0不启用，1启用
	 */
	public Integer getIsEnable() {
		return isEnable;
	}

	public void setIsEnable(Integer isEnable) {
		this.isEnable = isEnable;
	}

	/**
	 * 脱敏数据(一般不入库，入库时也可以为测试数据)
	 *
	 * @return
	 * @author daizhong.liu
	 * @date 2019年11月4日 上午10:30:34
	 * @desc
	 */
	public String getDesensitizationData() {
		return desensitizationData;
	}

	public void setDesensitizationData(String desensitizationData) {
		this.desensitizationData = desensitizationData;
	}

	public Integer getIsInside() {
		return isInside;
	}

	public void setIsInside(Integer isInside) {
		this.isInside = isInside;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
//	public DesensitizationAlgorithmConfigCopies getAlgorithmConfigCopies() {
//		return algorithmConfigCopies;
//	}
//	public void setAlgorithmConfigCopies(DesensitizationAlgorithmConfigCopies algorithmConfigCopies) {
//		this.algorithmConfigCopies = algorithmConfigCopies;
//	}

}
