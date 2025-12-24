package com.alibaba.datax.algorithm;

import com.alibaba.datax.model.DesensitizationAlgorithmConfigCopies;
import com.alibaba.datax.model.DesensitizationAlgorithmCopies;
import com.alibaba.datax.utils.AdqDateUtils;
import com.alibaba.datax.utils.AqdNumberUtils;
import com.alibaba.datax.utils.DataConvertUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.LinkedHashMap;

/** 
 * 变换脱敏算法
* @author liudaizhong.liu
* @date 2019年11月2日 下午4:57:10 
* @desc 
*/
public class TransformationDesensitizationAlgorithm extends AbstractDesensitizationAlgorithm {
	/**
	 * algoithmConfigType:1:数字取整、2:日期取整、3:字符位移
	 * algoithmConfigStyle:日期取整=1:年、2:月、3:日，字符位移=1:向左、2:向右
	 * algoithmConfigValue:数字取整=小数点前n位，字符位移=字符位移bit数
	 */
	@Override
	public String desensitization(DesensitizationAlgorithmCopies desensitizationAlgorithm, DesensitizationAlgorithmConfigCopies algorithmConfig, LinkedHashMap<String, String> dictCodes) {
//		DesensitizationAlgorithmConfigCopies algorithmConfig = desensitizationAlgorithm.getAlgorithmConfigCopies();
		Integer algoithmConfigType = algorithmConfig.getAlgoithmConfigType();
		//输入值
		String algoithmConfigValue = algorithmConfig.getAlgoithmConfigValue();
		//脱敏数据
		String desensitizationData = desensitizationAlgorithm.getDesensitizationData();
		if (desensitizationData == null || desensitizationData.isEmpty()) {
			throw new RuntimeException("脱敏数据不允许为空!");
		}
		String resultData = desensitizationData;
		switch (algoithmConfigType) {
		//数字取整
		case 1:
			if (algoithmConfigValue == null || algoithmConfigValue.isEmpty()) {
				throw new RuntimeException("请输入保留小数前第N位!");
			}
			if (!AqdNumberUtils.isPositInt(algoithmConfigValue)) {
				throw new RuntimeException("保留小数前第N位必须为正整数，请检查!");
			}
			int reservedNum = Integer.parseInt(algoithmConfigValue);
			//小数先转成整数
			if (AqdNumberUtils.isDecimal(resultData)) {
				resultData = AqdNumberUtils.format(resultData, 0);
			}
			if(reservedNum >= resultData.length()) {
				resultData = "0";
				break;
			}
			resultData = StringUtils.overlay(resultData, StringUtils.repeat("0", reservedNum), resultData.length()-reservedNum,resultData.length());
			break;
		case 2:
			try {
				Integer algoithmConfigStyle = algorithmConfig.getAlgoithmConfigStyle();
//				algorithmConfig.getAlgoithmConfigValue();
				Date targetDate = AdqDateUtils.strToDate(resultData);
				switch (algoithmConfigStyle) {
					case 1:
						resultData = AdqDateUtils.parseDateToStr(targetDate, AdqDateUtils.DATE_FORMAT_YYYY);
						break;
					case 2:
						resultData = AdqDateUtils.parseDateToStr(targetDate, AdqDateUtils.DATE_FORMAT_YYYY_MM);
						break;
					case 3:
						resultData = AdqDateUtils.parseDateToStr(targetDate, AdqDateUtils.DATE_FORMAT_YYYY_MM_DD);
						break;
					default:
						throw new RuntimeException("请选择日期取整保留时间[年/月/日]!");
				}
				break;
			} catch (Exception e) {
				throw new RuntimeException("脱敏数据时间格式不正确，请检查!", e);
			}
		case 3:
			try {
				if (algoithmConfigValue == null || algoithmConfigValue.isEmpty()) {
					throw new RuntimeException("请输入循环位移Bit数!");
				}
				if (!AqdNumberUtils.isPositInt(algoithmConfigValue)) {
					throw new RuntimeException("循环位移Bit数必须为正整数，请检查!");
				}
				Integer algoithmConfigStyle = algorithmConfig.getAlgoithmConfigStyle();
				int bitNum = Integer.parseInt(algoithmConfigValue);
				int targetData = Integer.valueOf(resultData, 10);
				byte[] resultByte;
				switch (algoithmConfigStyle) {
					case 1:
						resultByte = DataConvertUtil.rotateLeft(DataConvertUtil.intToByteArray(targetData), bitNum);
						resultData = String.valueOf(DataConvertUtil.byteArrayToInt(resultByte));
						break;
					case 2:
						resultByte = DataConvertUtil.rotateRight(DataConvertUtil.intToByteArray(targetData), bitNum);
						resultData = String.valueOf(DataConvertUtil.byteArrayToInt(resultByte));
						break;
					default:
						throw new RuntimeException("请选择字符位移方向[向左/向右]!");
				}
				break;
			}catch(Exception e) {
				throw new RuntimeException("脱敏数据格式不正确，请检查!", e);
			}
		default:
			throw new RuntimeException("未找到变换脱敏参数配置信息");
		}
		return resultData;
	}
 
}
