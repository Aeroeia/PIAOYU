package com.damai.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationTotalResult {

    /**
     * 节目id
     * */
    private Long programId;

    /**
     * 以redis记录为主的对比结果
     * */
    private List<ExaminationIdentifierResult> examinationIdentifierResultRedisStandardList;

    /**
     * 以数据库记录为主的对比结果
     * */
    List<ExaminationIdentifierResult> examinationIdentifierResultDbStandardList;
    
}
