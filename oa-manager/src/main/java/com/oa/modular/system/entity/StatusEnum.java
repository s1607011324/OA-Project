package com.oa.modular.system.entity;

/**
 * 流程业务对象状态
 * @author 刘雪松
 * 2015年3月27日
 */
public enum StatusEnum {
	
	unstart(0), //未启动
	started(1), //已批中
	finished(2);//结束
	
	private Integer value;

	private StatusEnum(Integer value) {
		this.value = value;
	}
	
	public Integer getValue() {
		return value;
	}

}
