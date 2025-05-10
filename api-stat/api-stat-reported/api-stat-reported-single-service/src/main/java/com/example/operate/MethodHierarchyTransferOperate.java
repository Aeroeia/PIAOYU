package com.example.operate;

import com.example.structure.MethodData;
import com.example.structure.MethodHierarchyTransfer;
import org.aopalliance.intercept.MethodInvocation;

public class MethodHierarchyTransferOperate {
    
    public MethodHierarchyTransfer getMethodHierarchyTransfer(MethodInvocation methodInvocation, MethodData parentMethodData,
                                                              MethodData currentMethodData, boolean exceptionFlag) {
        MethodHierarchyTransfer methodHierarchyTransfer = new MethodHierarchyTransfer();
        methodHierarchyTransfer.setCurrentMethodData(currentMethodData);
        methodHierarchyTransfer.setParentMethodData(parentMethodData);
        methodHierarchyTransfer.setNames(methodInvocation.getMethod().getParameters());
        methodHierarchyTransfer.setValues(methodInvocation.getArguments());
        methodHierarchyTransfer.setExceptionFlag(exceptionFlag);
        return methodHierarchyTransfer;
    }
}
