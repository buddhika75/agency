/*
 * Open Hospital Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.bean.common;

import com.divudi.data.ApplicationInstitution;
import com.divudi.data.BillClassType;
import com.divudi.data.BillType;
import com.divudi.data.CalculationType;
import com.divudi.data.CssVerticalAlign;
import com.divudi.data.Dashboard;
import com.divudi.data.DepartmentListMethod;
import com.divudi.data.DepartmentType;
import com.divudi.data.FeeType;
import com.divudi.data.InvestigationItemType;
import com.divudi.data.InvestigationItemValueType;
import com.divudi.data.PaperType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.ReportItemType;
import com.divudi.data.SessionNumberType;
import com.divudi.data.Sex;
import com.divudi.data.MessageType;
import com.divudi.data.Title;
import com.divudi.entity.PaymentScheme;
import com.divudi.entity.Person;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class EnumController implements Serializable {

    private PaymentScheme paymentScheme;

    SessionNumberType[] sessionNumberTypes;


    public Dashboard[] getDashboardTypes() {
        return Dashboard.values();
    }

    public SessionNumberType[] getSessionNumberTypes() {
        sessionNumberTypes = SessionNumberType.values();
        return sessionNumberTypes;
    }

    public CssVerticalAlign[] getCssVerticalAlign() {
        return CssVerticalAlign.values();
    }

    public DepartmentListMethod[] getDepartmentListMethods() {
        return DepartmentListMethod.values();
    }

    public DepartmentType[] getDepartmentType() {
        return DepartmentType.values();
    }

    public ApplicationInstitution[] getApplicationInstitutions() {
        return ApplicationInstitution.values();
    }

    public PaperType[] getPaperTypes() {
        return PaperType.values();
    }

    public ReportItemType[] getReportItemTypes() {
        Person p;
        return ReportItemType.values();
    }

   


    public void setSessionNumberTypes(SessionNumberType[] sessionNumberTypes) {
        this.sessionNumberTypes = sessionNumberTypes;
    }

    public FeeType[] getFeeTypes() {
        return FeeType.values();
    }

   

    public InvestigationItemType[] getInvestigationItemTypes() {
        return InvestigationItemType.values();
    }

    public InvestigationItemValueType[] getInvestigationItemValueTypes() {
        return InvestigationItemValueType.values();
    }

   

    public BillType[] getBillTypes() {
        return BillType.values();
    }

    public BillClassType[] getBillClassTypes() {
        return BillClassType.values();
    }

    public CalculationType[] getCalculationTypes() {
        return CalculationType.values();
    }

  

    public Title[] getTitle() {
        return Title.values();
    }

    public Title[] getTitleDoctor() {
        Title[] tem = {
            Title.Dr,
            Title.DrMrs,
            Title.DrMs,
            Title.DrMiss,
            Title.Prof,
            Title.ProfMrs,
            Title.Mr,
            Title.Ms,
            Title.Miss,
            Title.Mrs,
            Title.Other,};
        return tem;
    }

    public Sex[] getSex() {
        return Sex.values();
    }

    public Sex[] getGender() {
        Sex[] sexes = {Sex.Male, Sex.Female};
        return sexes;
    }

    public PaymentMethod[] getPaymentMethodForAdmission() {
        PaymentMethod[] tmp = {PaymentMethod.Credit, PaymentMethod.Cash};
        return tmp;
    }

  

    public BillType[] getCashFlowBillTypes() {
        BillType[] b = {
            BillType.OpdBill,
            BillType.PaymentBill,
            BillType.PettyCash,
            BillType.CashRecieveBill,
            BillType.AgentPaymentReceiveBill,
            BillType.InwardPaymentBill,
            BillType.PharmacySale,
            BillType.ChannelCash,
            BillType.ChannelPaid,
            BillType.GrnPaymentPre,
            BillType.CollectingCentrePaymentReceiveBill,//            BillType.PharmacyPurchaseBill,
        //            BillType.GrnPayment,
        };

        return b;
    }

    public BillType[] getCashFlowBillTypesCashier() {
        BillType[] b = {
            BillType.OpdBill,
            BillType.PaymentBill,
            BillType.PettyCash,
            BillType.CashRecieveBill,
            BillType.AgentPaymentReceiveBill,
            BillType.InwardPaymentBill,
            BillType.PharmacySale,
            BillType.GrnPaymentPre,
            BillType.CollectingCentrePaymentReceiveBill,};

        return b;
    }

    public BillType[] getCashFlowBillTypesChannel() {
        BillType[] b = {
            BillType.ChannelCash,
            BillType.ChannelPaid,
            BillType.ChannelProPayment,
            BillType.ChannelIncomeBill,
            BillType.ChannelExpenesBill,};

        return b;
    }

    public BillType[] getStoreBillTypes() {

        BillType[] b = {
            BillType.StoreGrnBill,
            BillType.StoreGrnReturn,
            BillType.StoreOrder,
            BillType.StoreOrderApprove,
            BillType.StorePre,
            BillType.StorePurchase,
            BillType.StoreSale,
            BillType.StoreAdjustment,
            BillType.StorePurchaseReturn,
            BillType.StoreTransferRequest,
            BillType.StoreTransferIssue,};

        return b;
    }

    public BillType[] getPharmacyBillTypes() {
        BillType[] b = {
            BillType.PharmacyGrnBill,
            BillType.PharmacyGrnReturn,
            BillType.PharmacyOrder,
            BillType.PharmacyOrderApprove,
            BillType.PharmacyPre,
            BillType.PharmacyPurchaseBill,
            BillType.PharmacySale,
            BillType.PharmacyAdjustment,
            BillType.PurchaseReturn,
            BillType.GrnPayment,
            BillType.PharmacyTransferRequest,
            BillType.PharmacyTransferIssue,
            BillType.PharmacyWholeSale,
            BillType.PharmacyIssue,
            BillType.PharmacyTransferReceive};

        return b;
    }

    public BillType[] getPharmacyBillTypes2() {
        BillType[] b = {
            BillType.PharmacySale,
            BillType.PharmacyAdjustment,
            BillType.PharmacyTransferIssue,
            BillType.PharmacyIssue,
            BillType.PharmacyBhtPre};

        return b;
    }

    public BillType[] getPharmacyBillTypesForMovementReports() {
        BillType[] b = {
            BillType.PharmacySale,
            BillType.PharmacyAdjustment,
            BillType.PharmacyTransferIssue,
            BillType.PharmacyIssue,
            BillType.PharmacyBhtPre};
        return b;
    }

    public BillType[] getPharmacyBillTypes3() {
        BillType[] b = {
            BillType.PharmacyPre,
            BillType.PharmacyWholesalePre,
            BillType.PharmacyAdjustment,
            BillType.PharmacyTransferIssue,
            BillType.PharmacyIssue,
            BillType.PharmacyBhtPre};

        return b;
    }

    public BillType[] getPharmacySaleBillTypes() {
        BillType[] bt = {
            BillType.PharmacySale,
            BillType.PharmacyWholeSale,};
        return bt;
    }

    public PaymentMethod[] getPaymentMethods() {
        PaymentMethod[] p = {
            PaymentMethod.Cash,
            PaymentMethod.Card,
            PaymentMethod.Cheque,
            PaymentMethod.Slip,
            PaymentMethod.Credit,};

        return p;
    }

    public PaymentMethod[] getCollectingCentrePaymentMethods() {
        PaymentMethod[] p = {
            PaymentMethod.Agent,};
        return p;
    }

    public PaymentMethod[] getPaymentMethodsWithoutCredit() {
        PaymentMethod[] p = {PaymentMethod.Cash,
            PaymentMethod.Card,
            PaymentMethod.Cheque,
            PaymentMethod.Slip};

        return p;
    }

    public PaymentMethod[] getPaymentMethodsForPo() {
        PaymentMethod[] p = {PaymentMethod.Cash, PaymentMethod.Credit};

        return p;
    }

    public PaymentMethod[] getPaymentMethodsForChannel() {
        PaymentMethod[] p = {PaymentMethod.OnCall, PaymentMethod.Cash, PaymentMethod.Agent, PaymentMethod.Staff, PaymentMethod.Card, PaymentMethod.Cheque, PaymentMethod.Slip};
        return p;
    }

    public PaymentMethod[] getPaymentMethodsForChannelSettle() {
        PaymentMethod[] p = {PaymentMethod.Cash, PaymentMethod.Card};
        return p;
    }

    public PaymentMethod[] getPaymentMethodsForChannelAgentSettle() {
        PaymentMethod[] p = {PaymentMethod.Cash, PaymentMethod.Agent};
        return p;
    }

    public BillType[] getChannelType() {
        BillType[] bt = {BillType.Channel, BillType.XrayScan};
        return bt;
    }

//    public boolean checkPaymentScheme(PaymentScheme scheme, String paymentMathod) {
//        if (scheme != null && scheme.getPaymentMethod() != null) {
//            //System.err.println("Payment Scheme : " + scheme.getPaymentMethod());
//            //System.err.println("Payment Method : " + PaymentMethod.valueOf(paymentMathod));
//            if (scheme.getPaymentMethod().equals(PaymentMethod.valueOf(paymentMathod))) {
//                //System.err.println("Returning True");
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        return false;
//
//    }
//    public boolean checkPaymentScheme(String paymentMathod) {
//        if (getPaymentScheme() != null && getPaymentScheme().getPaymentMethod() != null) {
//            //System.err.println("Payment Scheme : " +getPaymentScheme().getPaymentMethod());
//            //System.err.println("Payment Method : " + PaymentMethod.valueOf(paymentMathod));
//            if (getPaymentScheme().getPaymentMethod().equals(PaymentMethod.valueOf(paymentMathod))) {
//                //System.err.println("Returning True");
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        return false;
//
//    }
    public boolean checkPaymentMethod(PaymentMethod paymentMethod, String paymentMathodStr) {
        if (paymentMethod != null) {
            //System.err.println("Payment method : " + paymentMethod);
            //System.err.println("Payment Method String : " + PaymentMethod.valueOf(paymentMathodStr));
            if (paymentMethod.equals(PaymentMethod.valueOf(paymentMathodStr))) {
                //System.err.println("Returning True");
                return true;
            } else {
                return false;
            }
        }

        return false;

    }

 

    public MessageType[] getSmsType() {
        return MessageType.values();
    }

    /**
     * Creates a new instance of EnumController
     */
    public EnumController() {
    }

    public PaymentScheme getPaymentScheme() {
        return paymentScheme;
    }

    public void setPaymentScheme(PaymentScheme paymentScheme) {
        this.paymentScheme = paymentScheme;
    }

}
