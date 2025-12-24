package com.alibaba.datax.model;


import com.alibaba.datax.DesensitizationAlgorithmBase;
import com.alibaba.datax.algorithm.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脱敏算法类型
 * @author liudaizhong.liu
 * @date 2019年11月4日 上午9:06:17
 * @desc
 */
public enum AlgorithmType {
	/**
	 * 哈希脱敏
	 */
	HASH("dsps.module.algorithm.enum.0001", 1, HashDesensitizationAlgorithm.class),
	/**
	 * 遮蔽脱敏
	 */
	SHADING("dsps.module.algorithm.enum.0002", 2, ShadingDesensitizationAlgorithm.class),
	/**
	 * 替换脱敏
	 */
	REPLACE("dsps.module.algorithm.enum.0003", 3, ReplaceDesensitizationAlgorithm.class),
	/**
	 * 变换脱敏
	 */
	TRANSFORMATION("dsps.module.algorithm.enum.0004", 4, TransformationDesensitizationAlgorithm.class),
	/**
	 * 加密脱敏
	 */
	ENCRYPTION("dsps.module.algorithm.enum.0005", 5, EncryptionDesensitizationAlgorithm.class),
	/**
	 * 随机脱敏
	 */
	RANDOM("dsps.module.algorithm.enum.0006", 6, RandomDesensitizationAlgorithm.class);

	private String name;
	private Integer type;
	private Class<? extends AbstractDesensitizationAlgorithm> algorithm;
	//注意，由于DesensitizationAlgorithmHandler交由spring管理后单例中的传参使用的ThreadLocal模式，因此不会出现线程安全问题，但自己创建对象有可能存在线程安全问题。
	private static ConcurrentHashMap<Integer, DesensitizationAlgorithmBase> algorithmCache = new ConcurrentHashMap<Integer, DesensitizationAlgorithmBase>();
	// 缓存枚举值数组，避免每次调用values()产生新数组
	private static final AlgorithmType[] ALGORITHM_TYPES = AlgorithmType.values();

	private AlgorithmType(String name, Integer type, Class<? extends AbstractDesensitizationAlgorithm> algorithm) {
		this.name = name;
		this.type = type;
		this.algorithm = algorithm;
	}

	public String getName() {
		return "脱敏算法类型";
	}

	public Integer getType() {
		return type;
	}

	public Class<? extends AbstractDesensitizationAlgorithm> getAlgorithm() {
		return algorithm;
	}

	public static DesensitizationAlgorithmBase getAlgorithm(int algorithm)
			throws  InstantiationException, IllegalAccessException {
		for (AlgorithmType type : ALGORITHM_TYPES) {
			if (type.getType() == algorithm) {
				// 使用computeIfAbsent原子操作保证线程安全
				return algorithmCache.computeIfAbsent(algorithm, k -> {
					try {
						// 通过方法引用优化反射调用
						Constructor<? extends AbstractDesensitizationAlgorithm> constructor =
								type.getAlgorithm().getDeclaredConstructor();
						if (!constructor.isAccessible()) {
							constructor.setAccessible(true);
						}
						return constructor.newInstance();
					} catch (NoSuchMethodException | InstantiationException |
							 IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException("未找到相应的脱敏算法，请检查");
					}
				});
			}
		}
		throw new RuntimeException("dsps.module.algorithm.service.0001");
	}
}
