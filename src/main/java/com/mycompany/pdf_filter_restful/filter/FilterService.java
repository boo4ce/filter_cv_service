/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdf_filter_restful.filter;

import com.google.gson.Gson;
import com.mycompany.pdf_filter_restful.MultipartMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

/**
 *
 * @author Admin
 */
@WebServlet(name = "Filter Process", urlPatterns = {"/FilterProcess"})
@MultipartConfig
public class FilterService extends HttpServlet{
    private final static String criteriaEndPoint = "http://localhost:8080/PDF_Filter_RESTful/Criteria";
    private final static String cvEndPoint = "http://localhost:8080/PDF_Filter_RESTful/CV";
    
    private List<CV> cvs;
    private String criteria;
    private List<String> criterias;
    
    private Map<String, List<CV>> res;
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MultipartMap map;
        try {
            map = new MultipartMap(req, this);
            
            //step 1
            initCVFromInput(map);
            //step 2
            initCriteria(map);
            //step 3
            convertAllCVtoText();
            //step 4
            findMatchedInfo();
            //step 5,6,7
            filter();
            //step 8
            sortCVByNumberOfMatchedCriteria();
            //final
            returnResutlt(resp);
            
            cleanResource();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * step 1
     * Khoi tao cv tu dau vao
     */
    private void initCVFromInput(MultipartMap map) throws Exception{
        cvs = new ArrayList<>();
        res = new HashMap<>();
        
        int fileCount = Integer.parseInt(map.getParameter("file_count"));
        
        for(int i = 1; i <= fileCount; i++) {
            cvs.add(new CV(map.getFile("attachment" + i)));
        }
    }
    
    /**
     * step 2
     * Khoi tao tieu chi 
     */
    private void initCriteria(MultipartMap map) {
        criteria = map.getParameter("criteria");
        criterias = new JSONObject(criteria).getJSONArray("data").toList().stream().map((data) -> {
            return data.toString();
        }).toList();
    }
    
    /**
     * step 3
     * Chuyen noi dung cv sang text
     */
    private void convertAllCVtoText() throws FileNotFoundException, IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(cvEndPoint);
        // This attaches the file to the POST:
        for(CV cv : cvs) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            builder.addBinaryBody(
                "attachment",
                new FileInputStream(cv.file),
                ContentType.APPLICATION_OCTET_STREAM,
                cv.name
            );

            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            CloseableHttpResponse response = httpClient.execute(uploadFile);
            HttpEntity responseEntity = response.getEntity();
            
            cv.content = new String(responseEntity.getContent().readAllBytes());
        }
    }
    
    /**
     * step 4
     * Tim kiem so tieu chi khop
     */
    private void findMatchedInfo() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(criteriaEndPoint);
        
        for(CV cv : cvs) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            
            builder.addTextBody("content", cv.content, ContentType.TEXT_PLAIN);
            builder.addTextBody("criteria", criteria, ContentType.TEXT_PLAIN);
            
            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            CloseableHttpResponse response = httpClient.execute(uploadFile);
            HttpEntity responseEntity = response.getEntity();
            
            JSONObject res = new JSONObject(new String(responseEntity.getContent().readAllBytes()));
            cv.numberOfMatchedCriteria = res.getInt("numberOfMatchedCriteria");
            cv.listCriteria = res.getJSONArray("listCriteria").toList().stream().map((t) -> {
                return t.toString();
            }).toList();
        }
    }

    /**
     * step 5, 6, 7
     * loc cv
     */
    private void filter() {
        List<CV> suitable = new ArrayList();
        List<CV> unsuitable = new ArrayList();
        
        for(CV cv : cvs) {
            if(cv.numberOfMatchedCriteria > 0) {
                cv.matchPercent = (float)cv.numberOfMatchedCriteria / criterias.size();
                suitable.add(cv);
            } else unsuitable.add(cv);
        }
        
        res.put("suitableList", suitable);
        res.put("unsuitableList", suitable);
    }
    
    /**
     * step 8
     * sap xep
     */
    private void sortCVByNumberOfMatchedCriteria() {
        res.get("suitableList").sort((CV o1, CV o2) -> {
            if(o1.numberOfMatchedCriteria > o2.numberOfMatchedCriteria) {
                return -1;
            } else if(o1.numberOfMatchedCriteria < o2.numberOfMatchedCriteria) {
                return 1;
            } else return 0;
        });
    }
    
    /**
     * final step
     */
    private void returnResutlt(HttpServletResponse resp) {
        try (PrintWriter writer = new PrintWriter(resp.getOutputStream())) {
            // Now do your thing with the obtained input.
            writer.write(new Gson().toJson(res));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void cleanResource() {
        for(CV cv : cvs) {
            cv.file.delete();
        }
        cvs.clear();
        criteria = "";
    }
}
