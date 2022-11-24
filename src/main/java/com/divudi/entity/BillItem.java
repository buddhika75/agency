/*
* Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.entity;

import com.divudi.entity.pharmacy.PharmaceuticalBillItem;
import com.divudi.entity.pharmacy.UserStock;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;

/**
 *
 * @author buddhika
 */
@Entity
public class BillItem implements Serializable {

    @ManyToOne
    private BillItem parentBillItem;

    @OneToOne(mappedBy = "billItem", fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    PharmaceuticalBillItem pharmaceuticalBillItem;
    static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    Double qty = 0.0;
    @Transient
    private Double absoluteQty ;
    @Lob
    String descreption;

    double remainingQty;
    double Rate;
    double discountRate;
    double marginRate;
    double netRate;

    double grossValue;
    double discount;
    double vat;
    double netValue;
    @Transient
    private double absoluteNetValue;
    double vatPlusNetValue;

    double marginValue;
    private double adjustedValue;
    double hospitalFee;
    double staffFee;
//    private double dblValue;
    @ManyToOne
    Item item;
    @ManyToOne
    Bill bill;
    @ManyToOne
    Bill expenseBill;
    Boolean refunded;
    //Created Properties
    @ManyToOne
    WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date createdAt;
    //Edited Properties
    @ManyToOne
    private WebUser editor;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date editedAt;
    //Retairing properties
    boolean retired;
    @ManyToOne
    WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date retiredAt;
    String retireComments;
    String insId;
    String deptId;
    String catId;
    String sessionId;
    String itemId;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date fromTime;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date toTime;
    @OneToOne
    BillItem referanceBillItem;

    @Temporal(javax.persistence.TemporalType.DATE)
    Date sessionDate;
    @ManyToOne
    Bill referenceBill;
    String agentRefNo;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date billTime;

//    @Transient
    int searialNo;
    @Transient
    double totalGrnQty;
    @Transient
    private List<Item> tmpSuggession;
    @Transient
    private double tmpQty;
    @Transient
    private UserStock transUserStock;
    @Transient
    private BillItem transBillItem;
    @OneToMany(mappedBy = "parentBillItem")
    private List<BillItem> chiledBillItems;

    @Transient
    double transCCFee;
    @Transient
    double transWithOutCCFee;
    @Transient
    boolean transRefund;

    
    
    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

    public double getHospitalFee() {
        return hospitalFee;
    }

    public void setHospitalFee(double hospitalFee) {
        this.hospitalFee = hospitalFee;
    }

    public double getStaffFee() {
        return staffFee;
    }

    public void setStaffFee(double staffFee) {
        this.staffFee = staffFee;
    }



    public double getMarginValue() {
        return marginValue;
    }

    public void setMarginValue(double marginValue) {
        this.marginValue = marginValue;
    }

    public void copy(BillItem billItem) {
        item = billItem.getItem();
        sessionDate = billItem.getSessionDate();
        agentRefNo = billItem.getAgentRefNo();
        item = billItem.getItem();
        qty = billItem.getQty();
        descreption = billItem.getDescreption();
        billTime = billItem.getBillTime();
        grossValue = billItem.getGrossValue();
        netValue = billItem.getNetValue();
        discount = billItem.getDiscount();
        adjustedValue = billItem.getAdjustedValue();
        discountRate = billItem.getDiscountRate();
        staffFee = billItem.getStaffFee();
        hospitalFee = billItem.getHospitalFee();
        Rate = billItem.getRate();
        netRate = billItem.getNetRate();
        searialNo = billItem.getSearialNo();
        tmpQty = billItem.tmpQty;
        referenceBill = billItem.getReferenceBill();
        marginValue = billItem.getMarginValue();
        agentRefNo = billItem.getAgentRefNo();
        vat = billItem.getVat();
        vatPlusNetValue = billItem.getVatPlusNetValue();
        //  referanceBillItem=billItem.getReferanceBillItem();
    }

    public void resetValue() {
        qty = 0.0;
        grossValue = 0;
        netValue = 0;
        discount = 0;
        adjustedValue = 0;
        discountRate = 0.0;
        staffFee = 0.0;
        hospitalFee = 0.0;
        Rate = 0.0;
        netRate = 0.0;
        tmpQty = 0.0;
        marginValue = 0.0;
        vat = 0.0;
        vatPlusNetValue = 0.0;
    }

    public BillItem() {
    }

    public void invertValue(BillItem billItem) {
        if (billItem.getQty() != null) {
            qty = 0 - billItem.getQty();
        }
        Rate = 0 - billItem.getRate();
        discount = 0 - billItem.getDiscount();
        netRate = 0 - billItem.getNetRate();
        grossValue = 0 - billItem.getGrossValue();
        marginValue = 0 - billItem.getMarginValue();
        netValue = 0 - billItem.getNetValue();
        adjustedValue = 0 - billItem.getAdjustedValue();
        staffFee = 0 - billItem.getStaffFee();
        hospitalFee = 0 - billItem.getHospitalFee();
        vat = 0 - billItem.getVat();
        vatPlusNetValue = 0 - billItem.getVatPlusNetValue();
    }

    public void invertValue() {
        if (getQty() != null) {
            qty = 0 - getQty();
        }
        Rate = 0 - getRate();
        discount = 0 - getDiscount();
        netRate = 0 - getNetRate();
        grossValue = 0 - getGrossValue();
        marginValue = 0 - getMarginValue();
        netValue = 0 - getNetValue();
        adjustedValue = 0 - getAdjustedValue();
        staffFee = 0 - getStaffFee();
        hospitalFee = 0 - getHospitalFee();
        vat = 0 - getVat();
        vatPlusNetValue = 0 - getVatPlusNetValue();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        ////System.out.println("object = " + object);
        if (!(object instanceof BillItem)) {
            ////System.out.println("not a bill item = ");
            return false;
        }
        BillItem other = (BillItem) object;
        ////System.out.println("other = " + other);
        ////System.out.println("this id = " + this.id);
        ////System.out.println("other id = " + other.id);
        if ((this.id == null || this.id == 0) && (other.id == null || other.id == 0)) {
            if (this.searialNo == other.searialNo) {
                ////System.out.println("this = other");
                return true;
            } else {
                ////System.out.println("this not eq other");
                return false;
            }

        }
        ////System.out.println("not Null");
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.BillItem[ id=" + id + " ]";
    }

    public PharmaceuticalBillItem getPharmaceuticalBillItem() {
//        if (pharmaceuticalBillItem == null) {
//            pharmaceuticalBillItem = new PharmaceuticalBillItem();
//            pharmaceuticalBillItem.setBillItem(this);
//        }
        return pharmaceuticalBillItem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getRate() {
        return Rate;
    }

    public void setRate(double Rate) {
        this.Rate = Rate;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    public double getNetRate() {
        return netRate;
    }

    public void setNetRate(double netRate) {
        this.netRate = netRate;
    }

    public double getGrossValue() {
        return grossValue;
    }

    public void setGrossValue(double grossValue) {
        this.grossValue = grossValue;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getNetValue() {
        return netValue;
    }

    public void setNetValue(double netValue) {
        this.netValue = netValue;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Bill getBill() {
        return bill;
    }
    
    

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public Boolean isRefunded() {
        return refunded;
    }

    public Boolean getRefunded() {
        return refunded;
    }

    public void setRefunded(Boolean refunded) {
        this.refunded = refunded;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public String getInsId() {
        return insId;
    }

    public void setInsId(String insId) {
        this.insId = insId;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    public Date getToTime() {
        return toTime;
    }

    public void setToTime(Date toTime) {
        this.toTime = toTime;
    }

    public BillItem getReferanceBillItem() {
        return referanceBillItem;
    }

    public void setReferanceBillItem(BillItem referanceBillItem) {
        this.referanceBillItem = referanceBillItem;
    }

    public Date getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Date sessionDate) {
        this.sessionDate = sessionDate;
    }

    public int getSearialNo() {
        return searialNo;
    }

    public Integer getSearialNoInteger() {
        return searialNo;
    }

    public void setSearialNo(int searialNo) {
        this.searialNo = searialNo;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double Qty) {
        this.qty = Qty;

    }

    public double getRemainingQty() {
        return remainingQty;
    }

    public void setRemainingQty(double remainingQty) {
        this.remainingQty = remainingQty;
    }



    public Bill getReferenceBill() {
        return referenceBill;
    }

    public void setReferenceBill(Bill referenceBill) {
        this.referenceBill = referenceBill;
    }

    public String getAgentRefNo() {
        return agentRefNo;
    }

    public void setAgentRefNo(String agentRefNo) {
        this.agentRefNo = agentRefNo;
    }

    public double getTotalGrnQty() {
        return totalGrnQty;
    }

    public void setTotalGrnQty(double totalGrnQty) {
        this.totalGrnQty = totalGrnQty;
    }

    public void setPharmaceuticalBillItem(PharmaceuticalBillItem pharmaceuticalBillItem) {
        this.pharmaceuticalBillItem = pharmaceuticalBillItem;
    }

//    public double getDblValue() {
//        return dblValue;
//    }
//
//    public void setDblValue(double dblValue) {
//        this.dblValue = dblValue;
//    }
    public List<Item> getTmpSuggession() {
        return tmpSuggession;
    }

    public void setTmpSuggession(List<Item> tmpSuggession) {
        this.tmpSuggession = tmpSuggession;
    }

    public UserStock getTransUserStock() {
        return transUserStock;
    }

    public void setTransUserStock(UserStock transUserStock) {
        this.transUserStock = transUserStock;
    }

    public double getAdjustedValue() {
        return adjustedValue;
    }

    public void setAdjustedValue(double adjustedValue) {
        this.adjustedValue = adjustedValue;
    }

    public BillItem getTransBillItem() {
        return transBillItem;
    }

    public void setTransBillItem(BillItem transBillItem) {
        this.transBillItem = transBillItem;
    }

    public WebUser getEditor() {
        return editor;
    }

    public void setEditor(WebUser editor) {
        this.editor = editor;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    public String getDescreption() {
        return descreption;
    }

    public void setDescreption(String descreption) {
        this.descreption = descreption;
    }

    public Date getBillTime() {
        return billTime;
    }

    public void setBillTime(Date billTime) {
        this.billTime = billTime;
    }

    public Bill getExpenseBill() {
        return expenseBill;
    }

    public void setExpenseBill(Bill expenseBill) {
        this.expenseBill = expenseBill;
    }

    public double getMarginRate() {
        return marginRate;
    }

    public void setMarginRate(double marginRate) {
        this.marginRate = marginRate;
    }

    public BillItem getParentBillItem() {
        return parentBillItem;
    }

    public void setParentBillItem(BillItem parentBillItem) {
        this.parentBillItem = parentBillItem;
    }

    public List<BillItem> getChiledBillItems() {
        return chiledBillItems;
    }

    public void setChiledBillItems(List<BillItem> chiledBillItems) {
        this.chiledBillItems = chiledBillItems;
    }

    public double getTransCCFee() {
        return transCCFee;
    }

    public void setTransCCFee(double transCCFee) {
        this.transCCFee = transCCFee;
    }

    public double getTransWithOutCCFee() {
        return transWithOutCCFee;
    }

    public void setTransWithOutCCFee(double transWithOutCCFee) {
        this.transWithOutCCFee = transWithOutCCFee;
    }

    public boolean isTransRefund() {
        return transRefund;
    }

    public void setTransRefund(boolean transRefund) {
        this.transRefund = transRefund;
    }

    public double getVatPlusNetValue() {
        return vatPlusNetValue;
    }

    public void setVatPlusNetValue(double vatPlusNetValue) {
        this.vatPlusNetValue = vatPlusNetValue;
    }

    public double getAbsoluteNetValue() {
        absoluteNetValue = Math.abs(netValue);
        return absoluteNetValue;
    }

    public Double getAbsoluteQty() {
        if(qty!=null){
            absoluteQty = Math.abs(qty);
        }else{
            absoluteQty = 0.0;
        }
        return absoluteQty;
    }

}
