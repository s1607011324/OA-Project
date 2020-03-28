package com.oa.modular.system.entity;

public enum  VariableEnum {
    person("人力资源部主管"),
    DS("董事长"),
    Sum("总经理"),
    FSum("副总经理"),
    model,	    //业务对象变量名
    initiator,  //流程发起人变量名
    flow ;       //流程跳转变量名

    String personnel;

    VariableEnum(String personnel) {
        this.personnel = personnel;
    }

    VariableEnum() {

    }

    public String getPersonnel() {
        return personnel;
    }

    public void setPersonnel(String personnel) {
        this.personnel = personnel;
    }
}
