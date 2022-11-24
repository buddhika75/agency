/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package com.divudi.bean.common;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 *
 * @author Buddhika
 */
@Named(value = "testController")
@SessionScoped
public class TestController implements Serializable {

    /**
     * Creates a new instance of TestController
     */
    public TestController() {
    }
    
}
