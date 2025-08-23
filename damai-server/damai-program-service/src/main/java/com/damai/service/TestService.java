package com.damai.service;

import com.baidu.fsg.uid.utils.PaddedAtomicLong;
import com.damai.dto.TestDto;
import com.damai.dto.TestSendDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

import static com.damai.constant.Constant.TRACE_ID;

@Slf4j
@Service
public class TestService {
    
    AtomicLong count = new PaddedAtomicLong(0);
    
    
    public Boolean reset(final TestSendDto testSendDto) {
        count.set(0);
        return true;
    }
    
    public void test(TestDto testDto, HttpServletRequest request) {
        log.info("获取的链路id:{}",request.getHeaders(TRACE_ID));
    }
}
