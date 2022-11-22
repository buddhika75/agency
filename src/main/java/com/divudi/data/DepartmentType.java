/*
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.data;

/**
 *
 * @author Buddhika
 */
public enum DepartmentType {
    Distributor,
    Manufacturer,
    Company,
    Customer,
    Bank,
    @Deprecated
    Opd,
    @Deprecated
    Inventry,
    @Deprecated
    Inward;
    
    public String getLabel(){
        return this.toString();
    }
}
