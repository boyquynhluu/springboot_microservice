package com.example.microservice_based.controller;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.microservice_based.exceptionhandler.SystemException;
import com.example.microservice_based.modal.ConvertDataReponseOk;
import com.example.microservice_based.modal.LoginInfo;
import com.example.microservice_based.utils.HttpManager;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/v1/api/sinhviens")
public class SinhVienController {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String ACCEPT = "Accept";
    private static final String HEADER_CONNECTION = "Connection";
    private static final String KEEP_ALIVE = "keep-alive";
    private static final String URL_LOGIN_INFO = "http://localhost:8080/api/auth/login";
    private static final String URL_GET_SINHVIENS = "http://localhost:8080/api/sinhviens";
    private static final String HEADER_AUTHORIZATION = "Authorization";

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllSinhViens() throws IOException, SystemException {
        // Get token
        LoginInfo info = this.getLoginInfo();
        if (Objects.isNull(info)) {
            throw new IllegalArgumentException("Call API External has error");
        }
        // Get all Sinh Vien
        ConvertDataReponseOk sinhviens = this.getAllSinhViens(info);
        return ResponseEntity.ok(sinhviens);
    }

    /**
     * Call API external get loginfo
     * 
     * @return loginInfo
     * @throws IOException
     * @throws SystemException
     */
    private LoginInfo getLoginInfo() throws IOException, SystemException {
        LoginInfo loginInfo = new LoginInfo();
        Map<String, Object> header = new HashMap<>();
        // Add header request
        header.put(CONTENT_TYPE, APPLICATION_JSON);
        header.put(ACCEPT, APPLICATION_JSON);
        header.put(HEADER_CONNECTION, KEEP_ALIVE);

        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("usernameOrEmail", "admin");
        reqBody.put("password", "admin");
        try {
            // Add body request
            ObjectMapper objectMapper = new ObjectMapper();
            Map<Integer, Object> resData = HttpManager.requestMethodPOST(URL_LOGIN_INFO, header, reqBody);
            if (!resData.isEmpty()) {
                Map.Entry<Integer, Object> firstEntry = resData.entrySet().iterator().next();
                // Get status code
                int statusCode = firstEntry.getKey();
                String responseData = (String) firstEntry.getValue();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    try {
                        loginInfo = objectMapper.readValue(responseData, LoginInfo.class);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Convert data response to Object has error!", e);
                    }
                }
            }
        } catch (Exception e) {
            throw new SystemException("Call API Login has error!", e);
        }
        return loginInfo;
    }

    /**
     * Get all sinhviens
     * 
     * @param loginfo
     * @return
     * @throws IOException
     * @throws SystemException
     */
    private ConvertDataReponseOk getAllSinhViens(LoginInfo loginfo) throws IOException, SystemException {
        ConvertDataReponseOk dataReponseOk = new ConvertDataReponseOk();
        Map<String, Object> headerMap = new HashMap<>();
        try {
            // Add header request
            headerMap.put(ACCEPT, APPLICATION_JSON);
            headerMap.put(HEADER_CONNECTION, KEEP_ALIVE);
            headerMap.put(HEADER_AUTHORIZATION, loginfo.getTokenType() + " " + loginfo.getAccessToken());

            Map<Integer, Object> dataResMap = HttpManager.requestMethodGet(URL_GET_SINHVIENS, headerMap);

            if (!dataResMap.isEmpty()) {
                Map.Entry<Integer, Object> entryMap = dataResMap.entrySet().iterator().next();
                int statusCode = entryMap.getKey();
                String responseData = (String) entryMap.getValue();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    // Deserialize JSON response into LoginInfo object
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        dataReponseOk = objectMapper.readValue(responseData, ConvertDataReponseOk.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new SystemException("Convert data response to Object has error!");
                    }
                }
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return dataReponseOk;
    }
}
