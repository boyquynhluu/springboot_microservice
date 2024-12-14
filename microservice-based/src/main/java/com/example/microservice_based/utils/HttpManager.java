package com.example.microservice_based.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.example.microservice_based.exceptionhandler.SystemException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HttpManager {

    private static final String REQUEST_METHOD_POST = "POST";
    private static final String REQUEST_METHOD_GET = "GET";
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_CALL_API = 2000;

    private static int resTimeout;
    private static int reqTimeout;

    public HttpManager(int resTimeout, int reqTimeout) {
        HttpManager.resTimeout = resTimeout;
        HttpManager.reqTimeout = reqTimeout;
    }

    /**
     * Call API External method POST
     * 
     * @param urlPost
     * @param header
     * @param jsonMap
     * @return map
     * @throws IOException
     * @throws SystemException
     */
    public static Map<Integer, Object> requestMethodPOST(String url, Map<String, Object> header,
            Map<String, Object> jsonMap) throws IOException, SystemException {
        Map<Integer, Object> map = new HashMap<>();
        HttpURLConnection con = null;
        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                con = (HttpURLConnection) new URL(url).openConnection();
                con.setUseCaches(false);
                con.setDoInput(true);
                con.setDoOutput(true);

                // Add header request
                con.setRequestMethod(REQUEST_METHOD_POST);
                con.setConnectTimeout(reqTimeout * 1000);
                con.setReadTimeout(resTimeout * 1000);
                if (!header.isEmpty()) {
                    for (Map.Entry<String, Object> entry : header.entrySet()) {
                        con.setRequestProperty(entry.getKey(), entry.getValue().toString());
                    }
                }

                // Add body request
                ObjectMapper objectMapper = new ObjectMapper();
                if (!jsonMap.isEmpty()) {
                    // Convert map to String
                    String jsonBody = objectMapper.writeValueAsString(jsonMap);
                    try (OutputStream os = con.getOutputStream()) {
                        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    } catch (IOException e) {
                        throw new SystemException("Set body data has error!", e);
                    }
                }
                // Get status code
                int statusCode = con.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    // Read data from response API
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    } catch (IOException e) {
                        throw new SystemException("Read datas response has error!", e);
                    }
                    map.put(con.getResponseCode(), response.toString());
                    break;
                } else {
                    retryCount++;
                    if (retryCount < MAX_RETRY_COUNT) {
                        try {
                            Thread.sleep(RETRY_DELAY_CALL_API);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt(); // Restore the interrupt flag
                        }
                    }
                }
            } catch (SocketTimeoutException e) {
                // Handle retry call api when socketimeout
                throw new SystemException("Call API External Has SocketTimeoutException!", e);
            } catch (IOException e) {
                throw new SystemException("Has error when read data response!", e);
            } catch (Exception e) {
                throw new SystemException("Call API Login has error!", e);
            } finally {
                if (!Objects.isNull(con)) {
                    // Close connection
                    con.disconnect();
                }
            }
        }
        return map;
    }

    /**
     * Call API External method GET
     * 
     * @param urlGet
     * @param headers
     * @return map
     * @throws IOException
     * @throws SystemException
     */
    public static Map<Integer, Object> requestMethodGet(String url, Map<String, Object> headers)
            throws IOException, SystemException {
        Map<Integer, Object> map = new HashMap<>();
        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                // Add header request
                con.setRequestMethod(REQUEST_METHOD_GET);
                con.setConnectTimeout(reqTimeout * 1000);
                con.setReadTimeout(resTimeout * 1000);
                if (!headers.isEmpty()) {
                    for (Map.Entry<String, Object> entry : headers.entrySet()) {
                        con.setRequestProperty(entry.getKey(), entry.getValue().toString());
                    }
                }
                // Get status code
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read data from response API
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }
                    map.put(responseCode, response.toString());
                    break;
                } else {
                    // Handle retry call api when socketimeout
                    if (retryCount < MAX_RETRY_COUNT) {
                        retryCount++;
                        try {
                            Thread.sleep(RETRY_DELAY_CALL_API); // Optional: wait before retrying
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt(); // Restore the interrupt flag
                        }
                    }
                }
            } catch (SocketTimeoutException e) {
                throw new SystemException("Call API External Has SocketTimeoutException!", e);
            } catch (IOException e) {
                throw new SystemException("Has error when read data response!", e);
            } catch (Exception e) {
                throw new SystemException("Call API External Has Error!", e);
            }
        }
        return map;
    }
}
