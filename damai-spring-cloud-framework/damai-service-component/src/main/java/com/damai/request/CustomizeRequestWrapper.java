package com.damai.request;

import com.damai.util.StringUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class CustomizeRequestWrapper extends HttpServletRequestWrapper {
    
    private final String requestBody;
    
    private String rewriteRequestBody;
    
    private boolean rewriteFlag = false;
    
    
    public CustomizeRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        requestBody = StringUtil.inputStreamConvertString(request.getInputStream());
    }
    
    public CustomizeRequestWrapper(HttpServletRequest request, String sourceRequstBody, String rewriteRequestBody) throws IOException {
        super(request);
        this.rewriteFlag = true;
        this.requestBody = sourceRequstBody;
        this.rewriteRequestBody = rewriteRequestBody;
        
    }
    
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        String data = "";
        if(rewriteFlag) {
            data = rewriteRequestBody;
        }else {
            data = requestBody;
        }
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.getBytes("UTF-8"));
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
            
            @Override
            public boolean isFinished() {
                return false;
            }
            
            @Override
            public boolean isReady() {
                return false;
            }
            
            @Override
            public void setReadListener(ReadListener listener) {
                
            }
        };
    }
    
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }
    
    public String getRequestBody() {
        return this.requestBody;
    }
    
    
    
}
