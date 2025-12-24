package com.alibaba.datax.model;

import java.io.Serializable;
/**
 * 算法配置副本
 * @description 
 * @fileName DesensitizationAlgorithmConfig.java
 * @date 2019-11-04 17:36:45
 * @author daizhong.liu
 */
public class DesensitizationAlgorithmConfigCopies implements Serializable{
	 
	private static final long serialVersionUID = -8941709989381987418L;
	/** 算法信息表主键 */
	private Integer algoithmId;
	/**
	 * 参数配置类型([哈希=1:MD5、2:SHA-1、3:SHA-256、4:HMAC]/[遮蔽=1:*、2:#]/[替换=1:身份证、2:统一信用代码、3:银行卡号]/[变换=1:数字、2:日期、3:字符]/[加密=1:DES算法、2:3DES算法、3:AES算法、4:国密SM4算法]/[随机=1:打散重排、2:随机选择])
	 */
	private Integer algoithmConfigType;
	/** 参数配置值([哈希=盐/替换=固定值]) */
	private String algoithmConfigValue;
	/** 参数配置方式(遮蔽=1:保留前n后n、2:保留自x至y、3:遮盖前n后m、4:遮盖自x至y、5:特殊字符前遮盖、6:特殊字符后遮盖，替换=1:映射替换、2:随机替换、3:固定替换，变换=(日期取整=1:年、2:月、3:日，字符位移=1:向左、2:向右)) */
	private Integer algoithmConfigStyle;
	/** 特殊字符(@、&、.) */
	private String specialWord;
	/** 开始下标 */
	private Integer startIndex;
	/** 结束下标 */
	private Integer endIndex;
	/**开始区间*/
	private Integer startRange;
	/**结束区间*/
	private Integer endRange;
	/** 是否计算校验位：1是，2否 */
	private Integer verificationBit;
	/** 字典主键 */
	private Integer dictId;
	/** json参数，用于扩展新的参数时保存的数据 */
	private String jsonData;
//	/** 将json转换为map的数据，设置参数后进行转换*/
//	private Map<String,Object> jsonMap;
	/** 源类型0* 1# */
	private Integer sourceType;

//	@SuppressWarnings("unchecked")
//	public Map<String, Object> getJsonMap() {
////		if(jsonMap == null) {
////			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
////			jsonMap = gson.fromJson(jsonData, Map.class);
////		}
//		return jsonMap;
//	}
//	/**
//	 * 获取json参数中的指定数据
//	 * @author daizhong.liu
//	 * @date 2019年11月2日 下午4:37:22
//	 * @desc
//	 * @param jsonKey
//	 * @return
//	 */
//	public Object getJsonAttr(String jsonKey) {
//		return this.getJsonMap().get(jsonKey);
//	}
	
	/** 算法信息表主键 */
	public Integer getAlgoithmId(){
		return algoithmId;
	}
	public void setAlgoithmId(Integer algoithmId){
		this.algoithmId = algoithmId;
	}

	/** 参数配置类型([哈希=1:MD5、2:SHA-1、3:SHA-256、4:HMAC]/[遮蔽=1:*、2:#]/[替换=1:身份证、2:统一信用代码、3:银行卡号]/[变换=1:数字、2:日期、3:字符]/[加密=1:DES算法、2:3DES算法、3:AES算法、4:国密SM4算法]/[随机=1:打散重排、2:随机选择]) */
	public Integer getAlgoithmConfigType(){
		return algoithmConfigType;
	}

	/** 参数配置类型([哈希=1:MD5、2:SHA-1、3:SHA-256、4:HMAC]/[遮蔽=1:*、2:#]/[替换=1:身份证、2:统一信用代码、3:银行卡号]/[变换=1:数字、2:日期、3:字符]/[加密=1:DES算法、2:3DES算法、3:AES算法、4:国密SM4算法]/[随机=1:打散重排、2:随机选择]) */
	public void setAlgoithmConfigType(Integer algoithmConfigType){
		this.algoithmConfigType = algoithmConfigType;
	}
	/** 参数配置值([替换=固定值]) */
	public String getAlgoithmConfigValue(){
		return algoithmConfigValue;
	}
	public void setAlgoithmConfigValue(String algoithmConfigValue){
		this.algoithmConfigValue = algoithmConfigValue;
	}
	/** 参数配置方式(遮蔽=1:保留前n后n、2:保留自x至y、3:遮盖前n后m、4:遮盖自x至y、5:特殊字符前遮盖、6:特殊字符后遮盖，替换=1:映射替换、2:随机替换、3:固定替换，变换=(日期取整=1:年、2:月、3:日，字符位移=1:向左、2:向右)) */
	public Integer getAlgoithmConfigStyle(){
		return algoithmConfigStyle;
	}
	/** 参数配置方式(遮蔽=1:保留前n后n、2:保留自x至y、3:遮盖前n后m、4:遮盖自x至y、5:特殊字符前遮盖、6:特殊字符后遮盖，替换=1:映射替换、2:随机替换、3:固定替换，变换=(日期取整=1:年、2:月、3:日，字符位移=1:向左、2:向右)) */
	public void setAlgoithmConfigStyle(Integer algoithmConfigStyle){
		this.algoithmConfigStyle = algoithmConfigStyle;
	}
	/** 特殊字符(@、&、.) */
	public String getSpecialWord(){
		return specialWord;
	}
	public void setSpecialWord(String specialWord){
		this.specialWord = specialWord;
	}
	/** 开始下标 */
	public Integer getStartIndex(){
		return startIndex;
	}
	public void setStartIndex(Integer startIndex){
		this.startIndex = startIndex;
	}
	/** 结束下标 */
	public Integer getEndIndex(){
		return endIndex;
	}
	public void setEndIndex(Integer endIndex){
		this.endIndex = endIndex;
	}
	/** 是否计算校验位：1是，2否 */
	public Integer getVerificationBit(){
		return verificationBit;
	}
	public void setVerificationBit(Integer verificationBit){
		this.verificationBit = verificationBit;
	}
	/** 字典主键 */
	public Integer getDictId(){
		return dictId;
	}
	public void setDictId(Integer dictId){
		this.dictId = dictId;
	}
	/**开始区间*/
	public Integer getStartRange() {
		return startRange;
	}
	/**开始区间*/
	public void setStartRange(Integer startRange) {
		this.startRange = startRange;
	}
	/**结束区间*/
	public Integer getEndRange() {
		return endRange;
	}
	/**结束区间*/
	public void setEndRange(Integer endRange) {
		this.endRange = endRange;
	}
	/** json参数，用于扩展新的参数时保存的数据 */
	public String getJsonData(){
		return jsonData;
	}
	public void setJsonData(String jsonData){
		this.jsonData = jsonData;
	}
	public Integer getSourceType() {
		return sourceType;
	}
	public void setSourceType(Integer sourceType) {
		this.sourceType = sourceType;
	}
	
	
}
