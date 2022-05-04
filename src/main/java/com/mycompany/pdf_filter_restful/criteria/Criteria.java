/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdf_filter_restful.criteria;

import com.mycompany.pdf_filter_restful.MultipartMap;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author Admin
 */
@WebServlet(name = "Criteria", urlPatterns = {"/Criteria"})
@MultipartConfig
public class Criteria extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MultipartMap map;
        try {
            map = new MultipartMap(req, this);
            String content = map.getParameter("content");
            String listCriteria = map.getParameter("criteria");
            
            JSONObject criteria = new JSONObject(listCriteria);
            List<Object> list = criteria.getJSONArray("data").toList();
            try (PrintWriter writer = new PrintWriter(resp.getOutputStream())) {
                // Now do your thing with the obtained input.
                ArrayList<String> resData = findMatchedCriteria(list, content);
                JSONObject res = new JSONObject();
                res.put("numberOfMatchedCriteria", resData.size());
                res.put("listCriteria", resData);
                
                writer.write(res.toString());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private ArrayList<String> findMatchedCriteria(List<Object> criteria, String content) {
        ArrayList<String> listCriteria = new ArrayList<>();
        
        for(Object s : criteria) {
            String data = (String) s;
            if(content.contains(data)) {
                listCriteria.add(data);
            }
        }
        
        return listCriteria;
    }  
}
