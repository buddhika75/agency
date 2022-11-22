/*
 * Open Hospital Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.data.dataStructure;

import com.divudi.entity.Institution;


/**
 *
 * @author Buddhika
 */
public class DealerDueDetailRow {
    Institution dealer;
    Double zeroToThirty;

    
    Double thirtyToSixty;

    Double sixtyToNinty;

    Double moreThanNinty;


    public Institution getDealer() {
        return dealer;
    }

    public void setDealer(Institution dealer) {
        this.dealer = dealer;
    }

    public Double getZeroToThirty() {
        return zeroToThirty;
    }

    public void setZeroToThirty(Double zeroToThirty) {
        this.zeroToThirty = zeroToThirty;
    }


    public Double getThirtyToSixty() {
        return thirtyToSixty;
    }

    public void setThirtyToSixty(Double thirtyToSixty) {
        this.thirtyToSixty = thirtyToSixty;
    }

 
    public Double getSixtyToNinty() {
        return sixtyToNinty;
    }

    public void setSixtyToNinty(Double sixtyToNinty) {
        this.sixtyToNinty = sixtyToNinty;
    }

    public Double getMoreThanNinty() {
        return moreThanNinty;
    }

    public void setMoreThanNinty(Double moreThanNinty) {
        this.moreThanNinty = moreThanNinty;
    }


    
    
}
