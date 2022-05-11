/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdf_filter_restful.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author Admin
 */
public class CV {
    public transient File file;
    public String name;
    public int numberOfMatchedCriteria;
    public transient List<String> listCriteria;
    public transient String content;
    public float matchPercent = 0f;
    public transient String email;
    
    public CV(File file) {
        this.file = file;
        name = file.getName();
        listCriteria = new ArrayList<>();
    }

    @Override
    public String toString() {
        return file.getName() + " " + numberOfMatchedCriteria + " " + listCriteria.size();
    }
}
