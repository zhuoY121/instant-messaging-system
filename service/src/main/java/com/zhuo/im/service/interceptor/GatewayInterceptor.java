package com.zhuo.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.zhuo.im.common.BaseErrorCode;
import com.zhuo.im.common.ResponseVO;
import com.zhuo.im.common.enums.GatewayErrorCode;
import com.zhuo.im.common.exception.ApplicationExceptionEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @description:
 * @version: 1.0
 */
@Component
public class GatewayInterceptor implements HandlerInterceptor {

    @Autowired
    IdentityCheck identityCheck;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // Get appId, operator, user signature
        String appIdStr = request.getParameter("appId");
        if (StringUtils.isBlank(appIdStr)) {
            resp(ResponseVO.errorResponse(GatewayErrorCode.APPID_NOT_EXIST), response);
            return false;
        }

        String identifier = request.getParameter("identifier");
        if (StringUtils.isBlank(identifier)) {
            resp(ResponseVO.errorResponse(GatewayErrorCode.OPERATOR_NOT_EXIST), response);
            return false;
        }

        String userSignature = request.getParameter("userSignature");
        if (StringUtils.isBlank(userSignature)) {
            resp(ResponseVO.errorResponse(GatewayErrorCode.USER_SIGNATURE_NOT_EXIST), response);
            return false;
        }

        // Check if the signature matches the operator and appid
        ApplicationExceptionEnum applicationExceptionEnum = identityCheck.checkUserSignature(identifier, appIdStr, userSignature);
        if (applicationExceptionEnum != BaseErrorCode.SUCCESS) {
            resp(ResponseVO.errorResponse(applicationExceptionEnum),response);
            return false;
        }

        return true;
    }


    private void resp(ResponseVO respVO, HttpServletResponse response){

        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            String resp = JSONObject.toJSONString(respVO);

            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-type", "application/json;charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin","*");
            response.setHeader("Access-Control-Allow-Credentials","true");
            response.setHeader("Access-Control-Allow-Methods","*");
            response.setHeader("Access-Control-Allow-Headers","*");
            response.setHeader("Access-Control-Max-Age","3600");

            writer = response.getWriter();
            writer.write(resp);

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (writer != null) {
                writer.checkError();
            }
        }

    }
}
