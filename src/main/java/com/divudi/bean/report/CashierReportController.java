/*
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.bean.report;

import com.divudi.bean.common.CommonController;
import com.divudi.bean.common.EnumController;
import com.divudi.bean.common.SessionController;
import com.divudi.data.BillType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.PaymentMethodValue;
import com.divudi.data.dataStructure.BillsTotals;
import com.divudi.data.dataStructure.CashierSummeryData;
import com.divudi.data.dataStructure.WebUserBillsTotal;
import com.divudi.data.table.String1Value1;

import com.divudi.ejb.CommonFunctions;
import com.divudi.entity.Bill;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.RefundBill;
import com.divudi.entity.WebUser;
import com.divudi.facade.BillFacade;
import com.divudi.facade.WebUserFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Buddhika
 */
@Named
@javax.enterprise.context.RequestScoped
public class CashierReportController implements Serializable {

    @Inject
    private SessionController sessionController;
    @Inject
    private CommonController commonController;
    @EJB
    private CommonFunctions commonFunction;
    @EJB
    private WebUserFacade webUserFacade;
    @EJB
    private BillFacade billFacade;
    private WebUser currentCashier;
    @Temporal(TemporalType.TIMESTAMP)
    private Date fromDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date toDate;
    String fromReciptNo;
    String toReciptNo;
    private List<WebUser> cashiers;
    private List<CashierSummeryData> cashierDatas;
    private double finalCashTot;
    private double finalCreditTot;
    private double finalCardTot;
    private CashierSummeryData current;
    double finalChequeTot;
    private List<String1Value1> dataTableDatas;
    @Inject
    private EnumController enumController;
    @Inject
    CommonReport commonReport;

    String header = "";

    /**
     * Creates a new instance of CashierReportController
     */
    public void recreteModal() {
        finalCashTot = 0.0;
        finalChequeTot = 0.0;
        finalCardTot = 0.0;
        finalCreditTot = 0.0;
        current = null;
        cashierDatas = null;
        cashiers = null;
        currentCashier = null;
        dataTableDatas = null;
    }

    public void recreteModal2() {
        fromReciptNo = null;
        toReciptNo = null;
        recreteModal();
    }

    public CashierReportController() {
    }

    public List<CashierSummeryData> getCashierDatasOwn() {
        cashierDatas = new ArrayList<>();
        for (WebUser w : getCashiers()) {
            CashierSummeryData temp = new CashierSummeryData();
            temp.setCasheir(w);
            findSummeryOwn(temp, w);
            cashierDatas.add(temp);
        }

        return cashierDatas;
    }

    private BillsTotals calculateBillTotalsByPaymentMethod(BillType billType, String suffix, Bill bill, WebUser webUser) {
        BillsTotals newB = new BillsTotals();
        newB.setName(billType.getLabel() + " " + suffix);
        for (Object o : userBillTotalsWithPaymentMethods(webUser, bill, billType)) {
            PaymentMethodValue pmv = (PaymentMethodValue) o;
            switch (pmv.getPaymentMethod()) {
                case Card:
                    newB.setCard(pmv.getValue());
                    finalCardTot += pmv.getValue();
                    break;
                case Cash:
                    newB.setCash(pmv.getValue());
                    finalCashTot += pmv.getValue();
                    break;

                case Cheque:
                    newB.setCheque(pmv.getValue());
                    finalChequeTot += pmv.getValue();
                    break;
                case Credit:
                    newB.setCredit(pmv.getValue());
                    finalCreditTot += pmv.getValue();
                    break;
                case Slip:
                    newB.setSlip(pmv.getValue());
                    finalSlipTot += pmv.getValue();
                    break;
            }
        }
        return newB;
    }

//    New Method is calculateBillTotalsByPaymentMethod
    @Deprecated
    private BillsTotals createRow(BillType billType, String suffix, Bill bill, WebUser webUser) {
        BillsTotals newB = new BillsTotals();
        newB.setName(billType.getLabel() + " " + suffix);
        newB.setCard(calTotalValueForLoggedDepartment(webUser, bill, PaymentMethod.Card, billType));
        finalCardTot += newB.getCard();
        newB.setCash(calTotalValueForLoggedDepartment(webUser, bill, PaymentMethod.Cash, billType));
        finalCashTot += newB.getCash();
        newB.setCheque(calTotalValueForLoggedDepartment(webUser, bill, PaymentMethod.Cheque, billType));
        finalChequeTot += newB.getCheque();
        newB.setCredit(calTotalValueForLoggedDepartment(webUser, bill, PaymentMethod.Credit, billType));
        finalCreditTot += newB.getCredit();
        newB.setSlip(calTotalValueForLoggedDepartment(webUser, bill, PaymentMethod.Slip, billType));
        finalSlipTot += newB.getSlip();

        return newB;

    }

    private BillsTotals createRowAgent(BillType billType, String suffix, Bill bill, WebUser webUser) {
        BillsTotals newB = new BillsTotals();
        newB.setName(billType.getLabel() + " " + suffix);
        newB.setCash(calTotalValueOwn(webUser, bill, PaymentMethod.Cash, billType));
        finalCashTot += newB.getCash();

        return newB;

    }

    private BillsTotals createRowWithoutPro(BillType billType, String suffix, Bill bill, WebUser webUser) {
        BillsTotals newB = new BillsTotals();
        newB.setName(billType.getLabel() + " " + suffix);
        newB.setCard(calTotalValueOwnWithoutPro(webUser, bill, PaymentMethod.Card, billType));
        finalCardTot += newB.getCard();
        newB.setCash(calTotalValueOwnWithoutPro(webUser, bill, PaymentMethod.Cash, billType));
        finalCashTot += newB.getCash();
        newB.setCheque(calTotalValueOwnWithoutPro(webUser, bill, PaymentMethod.Cheque, billType));
        finalChequeTot += newB.getCheque();
        newB.setCredit(calTotalValueOwnWithoutPro(webUser, bill, PaymentMethod.Credit, billType));
        finalCreditTot += newB.getCredit();
        newB.setSlip(calTotalValueOwnWithoutPro(webUser, bill, PaymentMethod.Slip, billType));
        finalSlipTot += newB.getSlip();

        return newB;

    }

    private BillsTotals createRowInOut(BillType billType, String suffix, Bill bill, WebUser webUser) {
        BillsTotals newB = new BillsTotals();
        newB.setName(billType.getLabel() + " " + suffix);
        newB.setCard(calTotalValueOwnInOutCreditCard(webUser, bill, billType));
        finalCardTot += newB.getCard();
        newB.setCash(calTotalValueOwnInOutCash(webUser, bill, billType));
        finalCashTot += newB.getCash();
        newB.setCheque(calTotalValueOwnInOutCheque(webUser, bill, billType));
        finalChequeTot += newB.getCheque();
        newB.setSlip(calTotalValueOwnInOutSlip(webUser, bill, billType));
        finalSlipTot += newB.getSlip();

        return newB;

    }

    public void calCashierData() {

        finalCashTot = finalChequeTot = finalCardTot = finalCreditTot = finalSlipTot = 0;
        webUserBillsTotals = new ArrayList<>();
        for (WebUser webUser : getCashiers()) {
            WebUserBillsTotal tmp = new WebUserBillsTotal();
            tmp.setWebUser(webUser);
            List<BillsTotals> billls = new ArrayList<>();

            double uCard = 0;
            double uCash = 0;
            double uCheque = 0;
            double uCredit = 0;
            double uSlip = 0;
            for (BillType btp : getEnumController().getCashFlowBillTypes()) {
                
                BillsTotals newB = createRow(btp, "Billed", new BilledBill(), webUser);

                if (newB.getCard() != 0 || newB.getCash() != 0 || newB.getCheque() != 0 || newB.getCredit() != 0 || newB.getSlip() != 0) {
                    billls.add(newB);
                }

                BillsTotals newC = createRow(btp, "Cancelled", new CancelledBill(), webUser);

                if (newC.getCard() != 0 || newC.getCash() != 0 || newC.getCheque() != 0 || newC.getCredit() != 0 || newC.getSlip() != 0) {
                    billls.add(newC);
                }

                BillsTotals newR = createRow(btp, "Refunded", new RefundBill(), webUser);

                if (newR.getCard() != 0 || newR.getCash() != 0 || newR.getCheque() != 0 || newR.getCredit() != 0 || newR.getSlip() != 0) {
                    billls.add(newR);
                }

                uCard += (newB.getCard() + newC.getCard() + newR.getCard());
                uCash += (newB.getCash() + newC.getCash() + newR.getCash());
                uCheque += (newB.getCheque() + newC.getCheque() + newR.getCheque());
                uCredit += (newB.getCredit() + newC.getCredit() + newR.getCredit());
                uSlip += (newB.getSlip() + newC.getSlip() + newR.getSlip());

            }
            //channel agent bill cash cancel refund
            BillsTotals newC = createRowAgent(BillType.ChannelAgent, "Cancelled", new CancelledBill(), webUser);

            if (newC.getCard() != 0 || newC.getCash() != 0 || newC.getCheque() != 0 || newC.getCredit() != 0 || newC.getSlip() != 0) {
                billls.add(newC);
            }

            BillsTotals newR = createRow(BillType.ChannelAgent, "Refunded", new RefundBill(), webUser);

            if (newR.getCard() != 0 || newR.getCash() != 0 || newR.getCheque() != 0 || newR.getCredit() != 0 || newR.getSlip() != 0) {
                billls.add(newR);
            }

            uCash += (newC.getCash() + newR.getCash());

            //
            //Cash In
            BillsTotals newIn = createRowInOut(BillType.CashIn, "Billed", new BilledBill(), webUser);

            if (newIn.getCard() != 0 || newIn.getCash() != 0
                    || newIn.getCheque() != 0 || newIn.getCredit() != 0
                    || newIn.getSlip() != 0) {
                billls.add(newIn);
            }

            uCard += (newIn.getCard());
            uCash += (newIn.getCash());
            uCheque += (newIn.getCheque());
            uCredit += (newIn.getCredit());
            uSlip += (newIn.getSlip());

            BillsTotals newInCan = createRowInOut(BillType.CashIn, "Cancelled", new CancelledBill(), webUser);
            uCard += (newInCan.getCard());
            uCash += (newInCan.getCash());
            uCheque += (newInCan.getCheque());
            uCredit += (newInCan.getCredit());
            uSlip += (newInCan.getSlip());

            if (newInCan.getCard() != 0 || newInCan.getCash() != 0
                    || newInCan.getCheque() != 0 || newInCan.getCredit() != 0
                    || newInCan.getSlip() != 0) {
                billls.add(newInCan);
            }

            //Cash Out
            BillsTotals newOut = createRowInOut(BillType.CashOut, "Billed", new BilledBill(), webUser);

            if (newOut.getCard() != 0 || newOut.getCash() != 0 || newOut.getCheque() != 0 || newOut.getCredit() != 0 || newOut.getSlip() != 0) {
                billls.add(newOut);
            }

            uCard += (newOut.getCard());
            uCash += (newOut.getCash());
            uCheque += (newOut.getCheque());
            uCredit += (newOut.getCredit());
            uSlip += (newOut.getSlip());

            if (newOut.getCard() != 0 || newOut.getCash() != 0
                    || newOut.getCheque() != 0 || newOut.getCredit() != 0
                    || newOut.getSlip() != 0) {
                billls.add(newOut);
            }

            BillsTotals newOutCan = createRowInOut(BillType.CashOut, "Cancelled", new CancelledBill(), webUser);
            uCard += (newOutCan.getCard());
            uCash += (newOutCan.getCash());
            uCheque += (newOutCan.getCheque());
            uCredit += (newOutCan.getCredit());
            uSlip += (newOutCan.getSlip());

            if (newOutCan.getCard() != 0 || newOutCan.getCash() != 0
                    || newOutCan.getCheque() != 0 || newOutCan.getCredit() != 0
                    || newOutCan.getSlip() != 0) {
                billls.add(newOutCan);
            }

            //Adjustment
            BillsTotals adj = createRowInOut(BillType.DrawerAdjustment, "Adjusment", new BilledBill(), webUser);
            uCard += (adj.getCard());
            uCash += (adj.getCash());
            uCheque += (adj.getCheque());
            uCredit += (adj.getCredit());
            uSlip += (adj.getSlip());

            if (adj.getCard() != 0 || adj.getCash() != 0
                    || adj.getCheque() != 0 || adj.getCredit() != 0
                    || adj.getSlip() != 0) {
                billls.add(adj);
            }

            BillsTotals newSum = new BillsTotals();
            newSum.setName("Total ");
            newSum.setBold(true);
            newSum.setCard(uCard);
            newSum.setCash(uCash);
            newSum.setCheque(uCheque);
            newSum.setCredit(uCredit);
            newSum.setSlip(uSlip);

            if (newSum.getCard() != 0 || newSum.getCash() != 0 || newSum.getCheque() != 0 || newSum.getCredit() != 0 || newSum.getSlip() != 0) {
                billls.add(newSum);
            }

            tmp.setBillsTotals(billls);
            webUserBillsTotals.add(tmp);

        }

        Date startTime = new Date();
        commonController.printReportDetails(fromDate, toDate, startTime, "All Cashier Report(/reportCashier/report_cashier_summery_all.xhtml)");

    }

    public void calCashierDataChannel() {

        header = "Channel";
        finalCashTot = finalChequeTot = finalCardTot = finalCreditTot = finalSlipTot = 0;
        webUserBillsTotals = new ArrayList<>();
        for (WebUser webUser : getCashiersChannel()) {
            WebUserBillsTotal tmp = new WebUserBillsTotal();
            tmp.setWebUser(webUser);
            List<BillsTotals> billls = new ArrayList<>();

            double uCard = 0;
            double uCash = 0;
            double uCheque = 0;
            double uCredit = 0;
            double uSlip = 0;
            for (BillType btp : getEnumController().getCashFlowBillTypesChannel()) {
                BillsTotals newB = createRow(btp, "Billed", new BilledBill(), webUser);

                if (newB.getCard() != 0 || newB.getCash() != 0 || newB.getCheque() != 0 || newB.getCredit() != 0 || newB.getSlip() != 0) {
                    billls.add(newB);
                }

                BillsTotals newC = createRow(btp, "Cancelled", new CancelledBill(), webUser);

                if (newC.getCard() != 0 || newC.getCash() != 0 || newC.getCheque() != 0 || newC.getCredit() != 0 || newC.getSlip() != 0) {
                    billls.add(newC);
                }

                BillsTotals newR = createRow(btp, "Refunded", new RefundBill(), webUser);

                if (newR.getCard() != 0 || newR.getCash() != 0 || newR.getCheque() != 0 || newR.getCredit() != 0 || newR.getSlip() != 0) {
                    billls.add(newR);
                }

                uCard += (newB.getCard() + newC.getCard() + newR.getCard());
                uCash += (newB.getCash() + newC.getCash() + newR.getCash());
                uCheque += (newB.getCheque() + newC.getCheque() + newR.getCheque());
                uCredit += (newB.getCredit() + newC.getCredit() + newR.getCredit());
                uSlip += (newB.getSlip() + newC.getSlip() + newR.getSlip());

            }
            //channel agent bill cash cancel refund
            BillsTotals newC = createRowAgent(BillType.ChannelAgent, "Cancelled", new CancelledBill(), webUser);

            if (newC.getCard() != 0 || newC.getCash() != 0 || newC.getCheque() != 0 || newC.getCredit() != 0 || newC.getSlip() != 0) {
                billls.add(newC);
            }

            BillsTotals newR = createRow(BillType.ChannelAgent, "Refunded", new RefundBill(), webUser);

            if (newR.getCard() != 0 || newR.getCash() != 0 || newR.getCheque() != 0 || newR.getCredit() != 0 || newR.getSlip() != 0) {
                billls.add(newR);
            }

            uCash += (newC.getCash() + newR.getCash());

            //
            //Cash In
            BillsTotals newIn = createRowInOut(BillType.CashIn, "Billed", new BilledBill(), webUser);

            if (newIn.getCard() != 0 || newIn.getCash() != 0
                    || newIn.getCheque() != 0 || newIn.getCredit() != 0
                    || newIn.getSlip() != 0) {
                billls.add(newIn);
            }

            uCard += (newIn.getCard());
            uCash += (newIn.getCash());
            uCheque += (newIn.getCheque());
            uCredit += (newIn.getCredit());
            uSlip += (newIn.getSlip());

            BillsTotals newInCan = createRowInOut(BillType.CashIn, "Cancelled", new CancelledBill(), webUser);
            uCard += (newInCan.getCard());
            uCash += (newInCan.getCash());
            uCheque += (newInCan.getCheque());
            uCredit += (newInCan.getCredit());
            uSlip += (newInCan.getSlip());

            if (newInCan.getCard() != 0 || newInCan.getCash() != 0
                    || newInCan.getCheque() != 0 || newInCan.getCredit() != 0
                    || newInCan.getSlip() != 0) {
                billls.add(newInCan);
            }

            //Cash Out
            BillsTotals newOut = createRowInOut(BillType.CashOut, "Billed", new BilledBill(), webUser);

            if (newOut.getCard() != 0 || newOut.getCash() != 0 || newOut.getCheque() != 0 || newOut.getCredit() != 0 || newOut.getSlip() != 0) {
                billls.add(newOut);
            }

            uCard += (newOut.getCard());
            uCash += (newOut.getCash());
            uCheque += (newOut.getCheque());
            uCredit += (newOut.getCredit());
            uSlip += (newOut.getSlip());

            if (newOut.getCard() != 0 || newOut.getCash() != 0
                    || newOut.getCheque() != 0 || newOut.getCredit() != 0
                    || newOut.getSlip() != 0) {
                billls.add(newOut);
            }

            BillsTotals newOutCan = createRowInOut(BillType.CashOut, "Cancelled", new CancelledBill(), webUser);
            uCard += (newOutCan.getCard());
            uCash += (newOutCan.getCash());
            uCheque += (newOutCan.getCheque());
            uCredit += (newOutCan.getCredit());
            uSlip += (newOutCan.getSlip());

            if (newOutCan.getCard() != 0 || newOutCan.getCash() != 0
                    || newOutCan.getCheque() != 0 || newOutCan.getCredit() != 0
                    || newOutCan.getSlip() != 0) {
                billls.add(newOutCan);
            }

            //Adjustment
            BillsTotals adj = createRowInOut(BillType.DrawerAdjustment, "Adjusment", new BilledBill(), webUser);
            uCard += (adj.getCard());
            uCash += (adj.getCash());
            uCheque += (adj.getCheque());
            uCredit += (adj.getCredit());
            uSlip += (adj.getSlip());

            if (adj.getCard() != 0 || adj.getCash() != 0
                    || adj.getCheque() != 0 || adj.getCredit() != 0
                    || adj.getSlip() != 0) {
                billls.add(adj);
            }

            BillsTotals newSum = new BillsTotals();
            newSum.setName("Total ");
            newSum.setBold(true);
            newSum.setCard(uCard);
            newSum.setCash(uCash);
            newSum.setCheque(uCheque);
            newSum.setCredit(uCredit);
            newSum.setSlip(uSlip);

            if (newSum.getCard() != 0 || newSum.getCash() != 0 || newSum.getCheque() != 0 || newSum.getCredit() != 0 || newSum.getSlip() != 0) {
                billls.add(newSum);
            }

            tmp.setBillsTotals(billls);
            webUserBillsTotals.add(tmp);

        }

        Date startTime = new Date();
        commonController.printReportDetails(fromDate, toDate, startTime, "All Cashier Report(/reportCashier/report_cashier_summery_all.xhtml)");

    }

    public void calCashierDataCashier() {

        header = "Cashier";
        finalCashTot = finalChequeTot = finalCardTot = finalCreditTot = finalSlipTot = 0;
        webUserBillsTotals = new ArrayList<>();
        for (WebUser webUser : getCashiersCashier()) {
            WebUserBillsTotal tmp = new WebUserBillsTotal();
            tmp.setWebUser(webUser);
            List<BillsTotals> billls = new ArrayList<>();

            double uCard = 0;
            double uCash = 0;
            double uCheque = 0;
            double uCredit = 0;
            double uSlip = 0;
            for (BillType btp : getEnumController().getCashFlowBillTypesCashier()) {
                BillsTotals newB = createRow(btp, "Billed", new BilledBill(), webUser);

                if (newB.getCard() != 0 || newB.getCash() != 0 || newB.getCheque() != 0 || newB.getCredit() != 0 || newB.getSlip() != 0) {
                    billls.add(newB);
                }

                BillsTotals newC = createRow(btp, "Cancelled", new CancelledBill(), webUser);

                if (newC.getCard() != 0 || newC.getCash() != 0 || newC.getCheque() != 0 || newC.getCredit() != 0 || newC.getSlip() != 0) {
                    billls.add(newC);
                }

                BillsTotals newR = createRow(btp, "Refunded", new RefundBill(), webUser);

                if (newR.getCard() != 0 || newR.getCash() != 0 || newR.getCheque() != 0 || newR.getCredit() != 0 || newR.getSlip() != 0) {
                    billls.add(newR);
                }

                uCard += (newB.getCard() + newC.getCard() + newR.getCard());
                uCash += (newB.getCash() + newC.getCash() + newR.getCash());
                uCheque += (newB.getCheque() + newC.getCheque() + newR.getCheque());
                uCredit += (newB.getCredit() + newC.getCredit() + newR.getCredit());
                uSlip += (newB.getSlip() + newC.getSlip() + newR.getSlip());

            }
            //channel agent bill cash cancel refund
//            BillsTotals newC = createRowAgent(BillType.ChannelAgent, "Cancelled", new CancelledBill(), webUser);
//
//            if (newC.getCard() != 0 || newC.getCash() != 0 || newC.getCheque() != 0 || newC.getCredit() != 0 || newC.getSlip() != 0) {
//                billls.add(newC);
//            }
//
//            BillsTotals newR = createRow(BillType.ChannelAgent, "Refunded", new RefundBill(), webUser);
//
//            if (newR.getCard() != 0 || newR.getCash() != 0 || newR.getCheque() != 0 || newR.getCredit() != 0 || newR.getSlip() != 0) {
//                billls.add(newR);
//            }
//
//            uCash += (newC.getCash() + newR.getCash());

            //
            //Cash In
            BillsTotals newIn = createRowInOut(BillType.CashIn, "Billed", new BilledBill(), webUser);

            if (newIn.getCard() != 0 || newIn.getCash() != 0
                    || newIn.getCheque() != 0 || newIn.getCredit() != 0
                    || newIn.getSlip() != 0) {
                billls.add(newIn);
            }

            uCard += (newIn.getCard());
            uCash += (newIn.getCash());
            uCheque += (newIn.getCheque());
            uCredit += (newIn.getCredit());
            uSlip += (newIn.getSlip());

            BillsTotals newInCan = createRowInOut(BillType.CashIn, "Cancelled", new CancelledBill(), webUser);
            uCard += (newInCan.getCard());
            uCash += (newInCan.getCash());
            uCheque += (newInCan.getCheque());
            uCredit += (newInCan.getCredit());
            uSlip += (newInCan.getSlip());

            if (newInCan.getCard() != 0 || newInCan.getCash() != 0
                    || newInCan.getCheque() != 0 || newInCan.getCredit() != 0
                    || newInCan.getSlip() != 0) {
                billls.add(newInCan);
            }

            //Cash Out
            BillsTotals newOut = createRowInOut(BillType.CashOut, "Billed", new BilledBill(), webUser);

            if (newOut.getCard() != 0 || newOut.getCash() != 0 || newOut.getCheque() != 0 || newOut.getCredit() != 0 || newOut.getSlip() != 0) {
                billls.add(newOut);
            }

            uCard += (newOut.getCard());
            uCash += (newOut.getCash());
            uCheque += (newOut.getCheque());
            uCredit += (newOut.getCredit());
            uSlip += (newOut.getSlip());

            if (newOut.getCard() != 0 || newOut.getCash() != 0
                    || newOut.getCheque() != 0 || newOut.getCredit() != 0
                    || newOut.getSlip() != 0) {
                billls.add(newOut);
            }

            BillsTotals newOutCan = createRowInOut(BillType.CashOut, "Cancelled", new CancelledBill(), webUser);
            uCard += (newOutCan.getCard());
            uCash += (newOutCan.getCash());
            uCheque += (newOutCan.getCheque());
            uCredit += (newOutCan.getCredit());
            uSlip += (newOutCan.getSlip());

            if (newOutCan.getCard() != 0 || newOutCan.getCash() != 0
                    || newOutCan.getCheque() != 0 || newOutCan.getCredit() != 0
                    || newOutCan.getSlip() != 0) {
                billls.add(newOutCan);
            }

            //Adjustment
            BillsTotals adj = createRowInOut(BillType.DrawerAdjustment, "Adjusment", new BilledBill(), webUser);
            uCard += (adj.getCard());
            uCash += (adj.getCash());
            uCheque += (adj.getCheque());
            uCredit += (adj.getCredit());
            uSlip += (adj.getSlip());

            if (adj.getCard() != 0 || adj.getCash() != 0
                    || adj.getCheque() != 0 || adj.getCredit() != 0
                    || adj.getSlip() != 0) {
                billls.add(adj);
            }

            BillsTotals newSum = new BillsTotals();
            newSum.setName("Total ");
            newSum.setBold(true);
            newSum.setCard(uCard);
            newSum.setCash(uCash);
            newSum.setCheque(uCheque);
            newSum.setCredit(uCredit);
            newSum.setSlip(uSlip);

            if (newSum.getCard() != 0 || newSum.getCash() != 0 || newSum.getCheque() != 0 || newSum.getCredit() != 0 || newSum.getSlip() != 0) {
                billls.add(newSum);
            }

            tmp.setBillsTotals(billls);
            webUserBillsTotals.add(tmp);

        }

        Date startTime = new Date();
        commonController.printReportDetails(fromDate, toDate, startTime, "All Cashier Report(/reportCashier/report_cashier_summery_all.xhtml)");

    }

    public void calCashierDataUsingReciptNo() {
        Date startTime = new Date();
        fromDate = null;
        toDate = null;
        if (fromReciptNo == null) {
            JsfUtil.addErrorMessage("Please Enter Check Bill No");
            return;
        }
        fromDate = commonReport.fetchDate(fromReciptNo);
        if (fromDate == null) {
            JsfUtil.addErrorMessage("Please Enter Correct From Bill No");
            return;
        }
        if (toReciptNo == null) {
            JsfUtil.addErrorMessage("Please Enter Check Bill No");
            return;
        }
        toDate = commonReport.fetchDate(toReciptNo);
        if (toDate == null) {
            JsfUtil.addErrorMessage("Please Enter Correct To Bill No");
            return;
        }
        calCashierData();

        commonController.printReportDetails(fromDate, toDate, startTime, "All cashier report(Using recipt No)(/reportCashier/report_cashier_summery_all_by_reciptno.xhtml)");
    }

    
    public void calculateCashierSummeryTotals() {
        Date startTime = new Date();
        finalCashTot = finalChequeTot = finalCardTot = finalCreditTot = finalSlipTot = 0;
        webUserBillsTotals = new ArrayList<>();
        for (WebUser webUser : getCashiers()) {
            WebUserBillsTotal tmp = new WebUserBillsTotal();
            tmp.setWebUser(webUser);
            List<BillsTotals> billls = new ArrayList<>();

            double uCard = 0;
            double uCash = 0;
            double uCheque = 0;
            double uCredit = 0;
            double uSlip = 0;
            for (BillType btp : getEnumController().getCashFlowBillTypes()) {
                BillsTotals newB = calculateBillTotalsByPaymentMethod(btp, "Billed", new BilledBill(), webUser);
                BillsTotals newC = calculateBillTotalsByPaymentMethod(btp, "Cancelled", new CancelledBill(), webUser);
                BillsTotals newR = calculateBillTotalsByPaymentMethod(btp, "Refunded", new RefundBill(), webUser);

                uCard += (newB.getCard() + newC.getCard() + newR.getCard());
                uCash += (newB.getCash() + newC.getCash() + newR.getCash());
                uCheque += (newB.getCheque() + newC.getCheque() + newR.getCheque());
                uCredit += (newB.getCredit() + newC.getCredit() + newR.getCredit());
                uSlip += (newB.getSlip() + newC.getSlip() + newR.getSlip());
                

            }

            

            BillsTotals newSum = new BillsTotals();
            newSum.setName("Total ");
            newSum.setBold(true);
            newSum.setCard(uCard);
            newSum.setCash(uCash);
            newSum.setCheque(uCheque);
            newSum.setCredit(uCredit);
            newSum.setSlip(uSlip);

            if (newSum.getCard() != 0 || newSum.getCash() != 0 || newSum.getCheque() != 0 || newSum.getCredit() != 0 || newSum.getSlip() != 0) {
                billls.add(newSum);
            }

            tmp.setBillsTotals(billls);
            webUserBillsTotals.add(tmp);

        }
        commonController.printReportDetails(fromDate, toDate, startTime, "All cashier summery(/reportCashier/report_cashier_summery_all_total_only.xhtml)");

    }
    
    public void calCashierDataTotalOnly() {
        Date startTime = new Date();
        finalCashTot = finalChequeTot = finalCardTot = finalCreditTot = finalSlipTot = 0;
        webUserBillsTotals = new ArrayList<>();
        for (WebUser webUser : getCashiers()) {
            WebUserBillsTotal tmp = new WebUserBillsTotal();
            tmp.setWebUser(webUser);
            List<BillsTotals> billls = new ArrayList<>();

            double uCard = 0;
            double uCash = 0;
            double uCheque = 0;
            double uCredit = 0;
            double uSlip = 0;
            for (BillType btp : getEnumController().getCashFlowBillTypes()) {
                BillsTotals newB = calculateBillTotalsByPaymentMethod(btp, "Billed", new BilledBill(), webUser);
                BillsTotals newC = calculateBillTotalsByPaymentMethod(btp, "Cancelled", new CancelledBill(), webUser);
                BillsTotals newR = calculateBillTotalsByPaymentMethod(btp, "Refunded", new RefundBill(), webUser);

                uCard += (newB.getCard() + newC.getCard() + newR.getCard());
                uCash += (newB.getCash() + newC.getCash() + newR.getCash());
                uCheque += (newB.getCheque() + newC.getCheque() + newR.getCheque());
                uCredit += (newB.getCredit() + newC.getCredit() + newR.getCredit());
                uSlip += (newB.getSlip() + newC.getSlip() + newR.getSlip());

            }

            //Cash IN
            BillsTotals newIn = createRowInOut(BillType.CashIn, "Billed", new BilledBill(), webUser);
            uCard += (newIn.getCard());
            uCash += (newIn.getCash());
            uCheque += (newIn.getCheque());
            uCredit += (newIn.getCredit());
            uSlip += (newIn.getSlip());

            BillsTotals newInCan = createRowInOut(BillType.CashIn, "Cancelled", new CancelledBill(), webUser);
            uCard += (newInCan.getCard());
            uCash += (newInCan.getCash());
            uCheque += (newInCan.getCheque());
            uCredit += (newInCan.getCredit());
            uSlip += (newInCan.getSlip());

            //Cahs Out
            BillsTotals newOut = createRowInOut(BillType.CashOut, "Billed", new BilledBill(), webUser);
            uCard += (newOut.getCard());
            uCash += (newOut.getCash());
            uCheque += (newOut.getCheque());
            uCredit += (newOut.getCredit());
            uSlip += (newOut.getSlip());

            BillsTotals newOutCan = createRowInOut(BillType.CashOut, "Cancelled", new CancelledBill(), webUser);
            uCard += (newOutCan.getCard());
            uCash += (newOutCan.getCash());
            uCheque += (newOutCan.getCheque());
            uCredit += (newOutCan.getCredit());
            uSlip += (newOutCan.getSlip());

            //Drawer Adjustment
            BillsTotals adj = createRowInOut(BillType.DrawerAdjustment, "Adjusment", new BilledBill(), webUser);
            uCard += (adj.getCard());
            uCash += (adj.getCash());
            uCheque += (adj.getCheque());
            uCredit += (adj.getCredit());
            uSlip += (adj.getSlip());

            BillsTotals newSum = new BillsTotals();
            newSum.setName("Total ");
            newSum.setBold(true);
            newSum.setCard(uCard);
            newSum.setCash(uCash);
            newSum.setCheque(uCheque);
            newSum.setCredit(uCredit);
            newSum.setSlip(uSlip);

            if (newSum.getCard() != 0 || newSum.getCash() != 0 || newSum.getCheque() != 0 || newSum.getCredit() != 0 || newSum.getSlip() != 0) {
                billls.add(newSum);
            }

            tmp.setBillsTotals(billls);
            webUserBillsTotals.add(tmp);

        }
        commonController.printReportDetails(fromDate, toDate, startTime, "All cashier summery(/reportCashier/report_cashier_summery_all_total_only.xhtml)");

    }

    public void calCashierDataTotalOnlyChannel() {
        header = "Channel";
        Date startTime = new Date();
        finalCashTot = finalChequeTot = finalCardTot = finalCreditTot = finalSlipTot = 0;
        webUserBillsTotals = new ArrayList<>();
        for (WebUser webUser : getCashiersChannel()) {
            WebUserBillsTotal tmp = new WebUserBillsTotal();
            tmp.setWebUser(webUser);
            List<BillsTotals> billls = new ArrayList<>();

            double uCard = 0;
            double uCash = 0;
            double uCheque = 0;
            double uCredit = 0;
            double uSlip = 0;
            for (BillType btp : getEnumController().getCashFlowBillTypesChannel()) {
                BillsTotals newB = createRow(btp, "Billed", new BilledBill(), webUser);
                BillsTotals newC = createRow(btp, "Cancelled", new CancelledBill(), webUser);
                BillsTotals newR = createRow(btp, "Refunded", new RefundBill(), webUser);

                uCard += (newB.getCard() + newC.getCard() + newR.getCard());
                uCash += (newB.getCash() + newC.getCash() + newR.getCash());
                uCheque += (newB.getCheque() + newC.getCheque() + newR.getCheque());
                uCredit += (newB.getCredit() + newC.getCredit() + newR.getCredit());
                uSlip += (newB.getSlip() + newC.getSlip() + newR.getSlip());

            }

            //channel agent bill cash cancel refund
            BillsTotals newC = createRow(BillType.ChannelAgent, "Cancelled", new CancelledBill(), webUser);
            BillsTotals newR = createRow(BillType.ChannelAgent, "Refunded", new RefundBill(), webUser);

            uCard += (newC.getCard() + newR.getCard());
            uCash += (newC.getCash() + newR.getCash());
            uCheque += (newC.getCheque() + newR.getCheque());
            uCredit += (newC.getCredit() + newR.getCredit());
            uSlip += (newC.getSlip() + newR.getSlip());
            //channel agent bill cash cancel refund

            //Cash IN
            BillsTotals newIn = createRowInOut(BillType.CashIn, "Billed", new BilledBill(), webUser);
            uCard += (newIn.getCard());
            uCash += (newIn.getCash());
            uCheque += (newIn.getCheque());
            uCredit += (newIn.getCredit());
            uSlip += (newIn.getSlip());

            BillsTotals newInCan = createRowInOut(BillType.CashIn, "Cancelled", new CancelledBill(), webUser);
            uCard += (newInCan.getCard());
            uCash += (newInCan.getCash());
            uCheque += (newInCan.getCheque());
            uCredit += (newInCan.getCredit());
            uSlip += (newInCan.getSlip());

            //Cahs Out
            BillsTotals newOut = createRowInOut(BillType.CashOut, "Billed", new BilledBill(), webUser);
            uCard += (newOut.getCard());
            uCash += (newOut.getCash());
            uCheque += (newOut.getCheque());
            uCredit += (newOut.getCredit());
            uSlip += (newOut.getSlip());

            BillsTotals newOutCan = createRowInOut(BillType.CashOut, "Cancelled", new CancelledBill(), webUser);
            uCard += (newOutCan.getCard());
            uCash += (newOutCan.getCash());
            uCheque += (newOutCan.getCheque());
            uCredit += (newOutCan.getCredit());
            uSlip += (newOutCan.getSlip());

            //Drawer Adjustment
            BillsTotals adj = createRowInOut(BillType.DrawerAdjustment, "Adjusment", new BilledBill(), webUser);
            uCard += (adj.getCard());
            uCash += (adj.getCash());
            uCheque += (adj.getCheque());
            uCredit += (adj.getCredit());
            uSlip += (adj.getSlip());

            BillsTotals newSum = new BillsTotals();
            newSum.setName("Total ");
            newSum.setBold(true);
            newSum.setCard(uCard);
            newSum.setCash(uCash);
            newSum.setCheque(uCheque);
            newSum.setCredit(uCredit);
            newSum.setSlip(uSlip);

            if (newSum.getCard() != 0 || newSum.getCash() != 0 || newSum.getCheque() != 0 || newSum.getCredit() != 0 || newSum.getSlip() != 0) {
                billls.add(newSum);
            }

            tmp.setBillsTotals(billls);
            webUserBillsTotals.add(tmp);

        }
        commonController.printReportDetails(fromDate, toDate, startTime, "All cashier summery(/reportCashier/report_cashier_summery_all_total_only.xhtml)");

    }

    public void calCashierDataTotalOnlyCashier() {
        header = "Cashier";
        Date startTime = new Date();
        finalCashTot = finalChequeTot = finalCardTot = finalCreditTot = finalSlipTot = 0;
        webUserBillsTotals = new ArrayList<>();
        for (WebUser webUser : getCashiersCashier()) {
            WebUserBillsTotal tmp = new WebUserBillsTotal();
            tmp.setWebUser(webUser);
            List<BillsTotals> billls = new ArrayList<>();

            double uCard = 0;
            double uCash = 0;
            double uCheque = 0;
            double uCredit = 0;
            double uSlip = 0;
            for (BillType btp : getEnumController().getCashFlowBillTypesCashier()) {
                BillsTotals newB = createRow(btp, "Billed", new BilledBill(), webUser);
                BillsTotals newC = createRow(btp, "Cancelled", new CancelledBill(), webUser);
                BillsTotals newR = createRow(btp, "Refunded", new RefundBill(), webUser);

                uCard += (newB.getCard() + newC.getCard() + newR.getCard());
                uCash += (newB.getCash() + newC.getCash() + newR.getCash());
                uCheque += (newB.getCheque() + newC.getCheque() + newR.getCheque());
                uCredit += (newB.getCredit() + newC.getCredit() + newR.getCredit());
                uSlip += (newB.getSlip() + newC.getSlip() + newR.getSlip());

            }

            //Cash IN
            BillsTotals newIn = createRowInOut(BillType.CashIn, "Billed", new BilledBill(), webUser);
            uCard += (newIn.getCard());
            uCash += (newIn.getCash());
            uCheque += (newIn.getCheque());
            uCredit += (newIn.getCredit());
            uSlip += (newIn.getSlip());

            BillsTotals newInCan = createRowInOut(BillType.CashIn, "Cancelled", new CancelledBill(), webUser);
            uCard += (newInCan.getCard());
            uCash += (newInCan.getCash());
            uCheque += (newInCan.getCheque());
            uCredit += (newInCan.getCredit());
            uSlip += (newInCan.getSlip());

            //Cahs Out
            BillsTotals newOut = createRowInOut(BillType.CashOut, "Billed", new BilledBill(), webUser);
            uCard += (newOut.getCard());
            uCash += (newOut.getCash());
            uCheque += (newOut.getCheque());
            uCredit += (newOut.getCredit());
            uSlip += (newOut.getSlip());

            BillsTotals newOutCan = createRowInOut(BillType.CashOut, "Cancelled", new CancelledBill(), webUser);
            uCard += (newOutCan.getCard());
            uCash += (newOutCan.getCash());
            uCheque += (newOutCan.getCheque());
            uCredit += (newOutCan.getCredit());
            uSlip += (newOutCan.getSlip());

            //Drawer Adjustment
            BillsTotals adj = createRowInOut(BillType.DrawerAdjustment, "Adjusment", new BilledBill(), webUser);
            uCard += (adj.getCard());
            uCash += (adj.getCash());
            uCheque += (adj.getCheque());
            uCredit += (adj.getCredit());
            uSlip += (adj.getSlip());

            BillsTotals newSum = new BillsTotals();
            newSum.setName("Total ");
            newSum.setBold(true);
            newSum.setCard(uCard);
            newSum.setCash(uCash);
            newSum.setCheque(uCheque);
            newSum.setCredit(uCredit);
            newSum.setSlip(uSlip);

            if (newSum.getCard() != 0 || newSum.getCash() != 0 || newSum.getCheque() != 0 || newSum.getCredit() != 0 || newSum.getSlip() != 0) {
                billls.add(newSum);
            }

            tmp.setBillsTotals(billls);
            webUserBillsTotals.add(tmp);

        }
        commonController.printReportDetails(fromDate, toDate, startTime, "All cashier summery(/reportCashier/report_cashier_summery_all_total_only.xhtml)");

    }

    public void calCashierDataTotalOnlyUsingReciptNo() {
        fromDate = null;
        toDate = null;
        if (fromReciptNo == null) {
            JsfUtil.addErrorMessage("Please Enter Check Bill No");
            return;
        }
        fromDate = commonReport.fetchDate(fromReciptNo);
        if (fromDate == null) {
            JsfUtil.addErrorMessage("Please Enter Correct From Bill No");
            return;
        }
        if (toReciptNo == null) {
            JsfUtil.addErrorMessage("Please Enter Check Bill No");
            return;
        }
        toDate = commonReport.fetchDate(toReciptNo);
        if (toDate == null) {
            JsfUtil.addErrorMessage("Please Enter Correct To Bill No");
            return;
        }
        calCashierDataTotalOnly();
    }

    public void calCashierDataTotalOnlyWithoutPro() {
        Date startTime = new Date();
        finalCashTot = finalChequeTot = finalCardTot = finalCreditTot = finalSlipTot = 0;
        webUserBillsTotals = new ArrayList<>();
        for (WebUser webUser : getCashiersWithoutPro()) {
            WebUserBillsTotal tmp = new WebUserBillsTotal();
            tmp.setWebUser(webUser);
            List<BillsTotals> billls = new ArrayList<>();

            double uCard = 0;
            double uCash = 0;
            double uCheque = 0;
            double uCredit = 0;
            double uSlip = 0;
            for (BillType btp : getEnumController().getCashFlowBillTypes()) {
                BillsTotals newB = createRowWithoutPro(btp, "Billed", new BilledBill(), webUser);
                BillsTotals newC = createRowWithoutPro(btp, "Cancelled", new CancelledBill(), webUser);
                BillsTotals newR = createRowWithoutPro(btp, "Refunded", new RefundBill(), webUser);

                uCard += (newB.getCard() + newC.getCard() + newR.getCard());
                uCash += (newB.getCash() + newC.getCash() + newR.getCash());
                uCheque += (newB.getCheque() + newC.getCheque() + newR.getCheque());
                uCredit += (newB.getCredit() + newC.getCredit() + newR.getCredit());
                uSlip += (newB.getSlip() + newC.getSlip() + newR.getSlip());

            }

            BillsTotals newSum = new BillsTotals();
            newSum.setName("Total ");
            newSum.setBold(true);
            newSum.setCard(uCard);
            newSum.setCash(uCash);
            newSum.setCheque(uCheque);
            newSum.setCredit(uCredit);
            newSum.setSlip(uSlip);

            if (newSum.getCard() != 0 || newSum.getCash() != 0 || newSum.getCheque() != 0 || newSum.getCredit() != 0 || newSum.getSlip() != 0) {
                billls.add(newSum);
            }
            tmp.setBillsTotals(billls);
            webUserBillsTotals.add(tmp);

        }

        commonController.printReportDetails(fromDate, toDate, startTime, "All cashier summery without professional(/reportCashier/report_cashier_summery_all_total_only_without_pro.xhtml)");

    }

    private List<WebUserBillsTotal> webUserBillsTotals;

    private List<Object> userBillTotalsWithPaymentMethods(WebUser w, Bill billClass, BillType billType) {
        String sql;
        Map temMap = new HashMap();
        sql = "select new com.divudi.data.PaymentMethodValue(b.paymentMethod, sum(b.netTotal+b.vat))"
                + " from Bill b "
                + " where type(b)=:bill "
                + " and b.creater=:cret "
                + " and b.institution=:ins"
                + " and b.retired=false "
                + " and b.billType= :billTp and b.createdAt between :fromDate and :toDate "
                + " group by b.paymentMethod";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("bill", billClass.getClass());
        temMap.put("cret", w);
        temMap.put("ins", getSessionController().getInstitution());
        return getBillFacade().findObjectBySQL(sql, temMap, TemporalType.TIMESTAMP);
    }

//  New One  userBillTotalsWithPaymentMethods
    @Deprecated
    private double calTotalValueOwn(WebUser w, Bill billClass, PaymentMethod pM, BillType billType) {
////        int day= Calendar.HOUR_OF_DAY(getToDate())- Calendar.DATE(getFromDate()) ;
//        Date a;
//        a = Calendar.Date.getToDate()-Date.getFromDate();
//        int day2;
//        day2 = Calendar.DAY_OF_YEAR(getToDate());
//        if(day2>=2){
//                    
//            JsfUtil.addErrorMessage("Please Enter Blow 2 Days");
//            return 0;
//        }
        String sql;
        Map temMap = new HashMap();

        sql = "select sum(b.netTotal+b.vat) from Bill b where type(b)=:bill and b.creater=:cret and "
                + " b.paymentMethod= :payMethod  and b.institution=:ins"
                + " and b.retired=false "
                + " and b.billType= :billTp and b.createdAt between :fromDate and :toDate ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("payMethod", pM);
        temMap.put("bill", billClass.getClass());
        temMap.put("cret", w);
        temMap.put("ins", getSessionController().getInstitution());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }
    
    
   
    private double calTotalValueForLoggedDepartment(WebUser w, Bill billClass, PaymentMethod pM, BillType billType) {
////        int day= Calendar.HOUR_OF_DAY(getToDate())- Calendar.DATE(getFromDate()) ;
//        Date a;
//        a = Calendar.Date.getToDate()-Date.getFromDate();
//        int day2;
//        day2 = Calendar.DAY_OF_YEAR(getToDate());
//        if(day2>=2){
//                    
//            JsfUtil.addErrorMessage("Please Enter Blow 2 Days");
//            return 0;
//        }
        String sql;
        Map temMap = new HashMap();

        sql = "select sum(b.netTotal+b.vat) from Bill b where type(b)=:bill and b.creater=:cret and "
                + " b.paymentMethod= :payMethod  and b.department=:dep"
                + " and b.retired=false "
                + " and b.billType= :billTp and b.createdAt between :fromDate and :toDate ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("payMethod", pM);
        temMap.put("bill", billClass.getClass());
        temMap.put("cret", w);
        temMap.put("dep", getSessionController().getDepartment());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double calTotalValueOwnWithoutPro(WebUser w, Bill billClass, PaymentMethod pM, BillType billType) {
////        int day= Calendar.HOUR_OF_DAY(getToDate())- Calendar.DATE(getFromDate()) ;
//        Date a;
//        a = Calendar.Date.getToDate()-Date.getFromDate();
//        int day2;
//        day2 = Calendar.DAY_OF_YEAR(getToDate());
//        if(day2>=2){
//                    
//            JsfUtil.addErrorMessage("Please Enter Blow 2 Days");
//            return 0;
//        }
        String sql;
        Map temMap = new HashMap();

        sql = "select sum(b.netTotal-b.staffFee) from Bill b where type(b)=:bill and b.creater=:cret and "
                + " b.paymentMethod= :payMethod  and b.institution=:ins"
                + " and b.billType= :billTp and b.createdAt between :fromDate and :toDate ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("payMethod", pM);
        temMap.put("bill", billClass.getClass());
        temMap.put("cret", w);
        temMap.put("ins", getSessionController().getInstitution());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double calTotalValueOwnInOutCash(WebUser w, Bill billClass, BillType billType) {

        String sql;
        Map temMap = new HashMap();

        sql = "select sum(b.cashTransaction.cashValue) from Bill b"
                + "  where type(b)=:bill "
                + " and b.creater=:cret "
                + " and b.institution=:ins"
                + " and b.billType= :billTp "
                + "and b.createdAt between :fromDate and :toDate ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("bill", billClass.getClass());
        temMap.put("cret", w);
        temMap.put("ins", getSessionController().getInstitution());

        double dbl = getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

        return dbl;

    }

    private double calTotalValueOwnInOutCreditCard(WebUser w, Bill billClass, BillType billType) {

        String sql;
        Map temMap = new HashMap();

        sql = "select sum(b.cashTransaction.creditCardValue) from Bill b"
                + "  where type(b)=:bill "
                + " and b.creater=:cret "
                + " and b.institution=:ins"
                + " and b.billType= :billTp "
                + "and b.createdAt between :fromDate and :toDate ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("bill", billClass.getClass());
        temMap.put("cret", w);
        temMap.put("ins", getSessionController().getInstitution());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double calTotalValueOwnInOutCheque(WebUser w, Bill billClass, BillType billType) {

        String sql;
        Map temMap = new HashMap();

        sql = "select sum(b.cashTransaction.chequeValue) from Bill b"
                + "  where type(b)=:bill "
                + " and b.creater=:cret "
                + " and b.institution=:ins"
                + " and b.billType= :billTp "
                + "and b.createdAt between :fromDate and :toDate ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("bill", billClass.getClass());
        temMap.put("cret", w);
        temMap.put("ins", getSessionController().getInstitution());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double calTotalValueOwnInOutSlip(WebUser w, Bill billClass, BillType billType) {

        String sql;
        Map temMap = new HashMap();

        sql = "select sum(b.cashTransaction.slipValue) from Bill b"
                + "  where type(b)=:bill "
                + " and b.creater=:cret "
                + " and b.institution=:ins"
                + " and b.billType= :billTp "
                + "and b.createdAt between :fromDate and :toDate ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("bill", billClass.getClass());
        temMap.put("cret", w);
        temMap.put("ins", getSessionController().getInstitution());

        return getBillFacade().findDoubleByJpql(sql, temMap, TemporalType.TIMESTAMP);

    }

    private double finalSlipTot = 0.0;

    public double getFinalChequeTot() {

        return finalChequeTot;
    }

    public double getFinalChequeTotOwn() {
        finalChequeTot = 0.0;
        for (CashierSummeryData s : getCashierDatasOwn()) {
            finalChequeTot += s.getNetCheque();
        }
        return finalChequeTot;
    }

    void findSummeryOwn(CashierSummeryData c, WebUser w) {
        c.setBilledCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setBilledCredit(calTotOwn(w, new BilledBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setBilledCreditCard(calTotOwn(w, new BilledBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setBilledCheque(calTotOwn(w, new BilledBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setBilledSlip(calTotOwn(w, new BilledBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setCancelledCash(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setCancelledCredit(calTotOwn(w, new CancelledBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setCancelledCreditCard(calTotOwn(w, new CancelledBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setCancelledCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setCancelledSlip(calTotOwn(w, new CancelledBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setRefundedCash(calTotOwn(w, new RefundBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setRefundedCredit(calTotOwn(w, new RefundBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setRefundedCreditCard(calTotOwn(w, new RefundBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setRefundCheque(calTotOwn(w, new RefundBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setRefundSlip(calTotOwn(w, new RefundBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setPaymentCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.PaymentBill));
        c.setPaymentCashCancel(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.PaymentBill));

        c.setPettyCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.PettyCash));
        c.setPettyCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.PettyCash));

        c.setPettyCancelCash(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.PettyCash));
        c.setPettyCancelCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.PettyCash));

        c.setCompanyCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.CashRecieveBill));
        c.setCompanyCheque(calTotOwn(w, new BilledBill(), PaymentMethod.Cheque, BillType.CashRecieveBill));
        c.setCompanySlip(calTotOwn(w, new BilledBill(), PaymentMethod.Slip, BillType.CashRecieveBill));

        c.setCompanyCancelCash(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.CashRecieveBill));
        c.setCompanyCancelCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.CashRecieveBill));
        c.setCompanyCancelSlip(calTotOwn(w, new CancelledBill(), PaymentMethod.Slip, BillType.CashRecieveBill));

        c.setAgentCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.AgentPaymentReceiveBill));
        c.setAgentCheque(calTotOwn(w, new BilledBill(), PaymentMethod.Cheque, BillType.AgentPaymentReceiveBill));
        c.setAgentSlip(calTotOwn(w, new BilledBill(), PaymentMethod.Slip, BillType.AgentPaymentReceiveBill));

        c.setAgentCancelCash(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.AgentPaymentReceiveBill));
        c.setAgentCancelCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.AgentPaymentReceiveBill));
        c.setAgentCancelSlip(calTotOwn(w, new CancelledBill(), PaymentMethod.Slip, BillType.AgentPaymentReceiveBill));

        c.setInwardPaymentCash(calTotOwn(w, new BilledBill(), PaymentMethod.Cash, BillType.InwardPaymentBill));
        c.setInwardPaymentCheque(calTotOwn(w, new BilledBill(), PaymentMethod.Cheque, BillType.InwardPaymentBill));
        c.setInwardPaymentSlip(calTotOwn(w, new BilledBill(), PaymentMethod.Slip, BillType.InwardPaymentBill));

        c.setInwardCancelCash(calTotOwn(w, new CancelledBill(), PaymentMethod.Cash, BillType.InwardPaymentBill));
        c.setInwardCancelCheque(calTotOwn(w, new CancelledBill(), PaymentMethod.Cheque, BillType.InwardPaymentBill));
        c.setInwardCancelSlip(calTotOwn(w, new CancelledBill(), PaymentMethod.Slip, BillType.InwardPaymentBill));
    }

    double calTotOwn(WebUser w, Bill billClass, PaymentMethod pM, BillType billType) {
        String sql;
        Map temMap = new HashMap();

        if (getSessionController().getInstitution() == null) {
            return 0.0;
        }

        if (billType == BillType.InwardPaymentBill) {
            sql = "select b from Bill b where type(b)=:bill and b.creater=:cret and "
                    + " b.paymentMethod=:payMethod  and b.institution=:ins"
                    + " and b.billType= :billTp and b.createdAt between :fromDate and :toDate ";
        } else {
            sql = "select b from Bill b where type(b)=:bill and b.creater=:cret and "
                    + " b.paymentMethod= :payMethod  and b.institution=:ins"
                    + " and b.billType= :billTp and b.createdAt between :fromDate and :toDate ";
        }

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("payMethod", pM);
        temMap.put("bill", billClass.getClass());
        temMap.put("cret", w);
        temMap.put("ins", getSessionController().getInstitution());

        List<Bill> bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        double tot = 0;
        for (Bill b : bills) {
            tot += b.getNetTotal();
        }
        return tot;

    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunction().getStartOfDay(new Date());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
        recreteModal();

    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunction().getEndOfDay(new Date());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
        recreteModal();
    }

    public CommonFunctions getCommonFunction() {
        return commonFunction;
    }

    public void setCommonFunction(CommonFunctions commonFunction) {
        this.commonFunction = commonFunction;
    }

    public WebUserFacade getWebUserFacade() {
        return webUserFacade;
    }

    public void setWebUserFacade(WebUserFacade webUserFacade) {
        this.webUserFacade = webUserFacade;
    }

    public List<WebUser> getCashiers() {
        String sql;
        Map temMap = new HashMap();
        BillType[] btpArr = enumController.getCashFlowBillTypes();
        List<BillType> btpList = Arrays.asList(btpArr);
        sql = "select DISTINCT(b.creater) from "
                + " Bill b "
                + " where b.institution=:ins "
                + " and b.billType in :btp "
                + " and b.createdAt between :fromDate and :toDate";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", btpList);
        temMap.put("ins", sessionController.getInstitution());
        cashiers = getWebUserFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (cashiers == null) {
            cashiers = new ArrayList<>();
        }

        return cashiers;
    }

    public List<WebUser> getCashiersCashier() {
        String sql;
        Map temMap = new HashMap();
        BillType[] btpArr = enumController.getCashFlowBillTypesCashier();
        List<BillType> btpList = Arrays.asList(btpArr);
        sql = "select us from "
                + " Bill b "
                + " join b.creater us "
                + " where b.retired=false "
                + " and b.institution=:ins "
                + " and b.billType in :btp "
                + " and b.createdAt between :fromDate and :toDate "
                + " group by us "
                + " having sum(b.netTotal)!=0 ";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", btpList);
        temMap.put("ins", sessionController.getInstitution());
        cashiers = getWebUserFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (cashiers == null) {
            cashiers = new ArrayList<>();
        }

        return cashiers;
    }

    public List<WebUser> getCashiersChannel() {
        String sql;
        Map temMap = new HashMap();
        BillType[] btpArr = enumController.getCashFlowBillTypesChannel();
        List<BillType> btpListTmp = Arrays.asList(btpArr);
        List<BillType> btpList = new ArrayList<>();
        btpList.addAll(btpListTmp);
        btpList.add(BillType.ChannelAgent);//Get Only Agency cash Cancel users
        sql = "select us from "
                + " Bill b "
                + " join b.creater us "
                + " where b.retired=false "
                + " and b.institution=:ins "
                + " and b.paymentMethod!=:pm "
                + " and b.billType in :btp "
                + " and b.createdAt between :fromDate and :toDate "
                + " group by us "
                + " having sum(b.netTotal+b.vat)!=0 ";
        temMap.put("pm", PaymentMethod.Agent);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", btpList);
        temMap.put("ins", sessionController.getInstitution());
        cashiers = getWebUserFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (cashiers == null) {
            cashiers = new ArrayList<>();
        }

        return cashiers;
    }

    public List<WebUser> getCashiersWithoutPro() {
        String sql;
        Map temMap = new HashMap();
        BillType[] btpArr = enumController.getCashFlowBillTypes();
        List<BillType> btpList = Arrays.asList(btpArr);
        sql = "select us from "
                + " Bill b "
                + " join b.creater us "
                + " where b.retired=false "
                + " and b.institution=:ins "
                + " and b.billType in :btp "
                + " and b.createdAt between :fromDate and :toDate "
                + " group by us "
                + " having sum(b.netTotal)!=0 ";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", btpList);
        temMap.put("ins", sessionController.getInstitution());
        cashiers = getWebUserFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        if (cashiers == null) {
            cashiers = new ArrayList<>();
        }

        return cashiers;
    }

    public void setCashiers(List<WebUser> cashiers) {
        this.cashiers = cashiers;

    }

    public List<CashierSummeryData> getCashierDatas() {
        cashierDatas = new ArrayList<>();
        for (WebUser w : getCashiers()) {
            CashierSummeryData temp = new CashierSummeryData();
            temp.setCasheir(w);
            findSummery(temp, w);
            cashierDatas.add(temp);

        }

        return cashierDatas;
    }

    void findSummery(CashierSummeryData c, WebUser w) {
        c.setBilledCash(calTot(w, new BilledBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setBilledCredit(calTot(w, new BilledBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setBilledCreditCard(calTot(w, new BilledBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setBilledCheque(calTot(w, new BilledBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setBilledSlip(calTot(w, new BilledBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setCancelledCash(calTot(w, new CancelledBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setCancelledCredit(calTot(w, new CancelledBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setCancelledCreditCard(calTot(w, new CancelledBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setCancelledCheque(calTot(w, new CancelledBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setCancelledSlip(calTot(w, new CancelledBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setRefundedCash(calTot(w, new RefundBill(), PaymentMethod.Cash, BillType.OpdBill));
        c.setRefundedCredit(calTot(w, new RefundBill(), PaymentMethod.Credit, BillType.OpdBill));
        c.setRefundedCreditCard(calTot(w, new RefundBill(), PaymentMethod.Card, BillType.OpdBill));
        c.setRefundCheque(calTot(w, new RefundBill(), PaymentMethod.Cheque, BillType.OpdBill));
        c.setRefundSlip(calTot(w, new RefundBill(), PaymentMethod.Slip, BillType.OpdBill));

        c.setPaymentCash(calTot(w, new BilledBill(), PaymentMethod.Cash, BillType.PaymentBill));
        c.setPaymentCashCancel(calTot(w, new CancelledBill(), PaymentMethod.Cash, BillType.PaymentBill));

        c.setPettyCash(calTot(w, new BilledBill(), PaymentMethod.Cash, BillType.PettyCash));
        c.setPettyCheque(calTot(w, new BilledBill(), PaymentMethod.Cheque, BillType.PettyCash));

        c.setPettyCancelCash(calTot(w, new CancelledBill(), PaymentMethod.Cash, BillType.PettyCash));
        c.setPettyCancelCheque(calTot(w, new CancelledBill(), PaymentMethod.Cheque, BillType.PettyCash));

        c.setCompanyCash(calTot(w, new BilledBill(), PaymentMethod.Cash, BillType.CashRecieveBill));
        c.setCompanyCheque(calTot(w, new BilledBill(), PaymentMethod.Cheque, BillType.CashRecieveBill));
        c.setCompanySlip(calTot(w, new BilledBill(), PaymentMethod.Slip, BillType.CashRecieveBill));

        c.setCompanyCancelCash(calTot(w, new CancelledBill(), PaymentMethod.Cash, BillType.CashRecieveBill));
        c.setCompanyCancelCheque(calTot(w, new CancelledBill(), PaymentMethod.Cheque, BillType.CashRecieveBill));
        c.setCompanyCancelSlip(calTot(w, new CancelledBill(), PaymentMethod.Slip, BillType.CashRecieveBill));

        c.setAgentCash(calTot(w, new BilledBill(), PaymentMethod.Cash, BillType.AgentPaymentReceiveBill));
        c.setAgentCheque(calTot(w, new BilledBill(), PaymentMethod.Cheque, BillType.AgentPaymentReceiveBill));
        c.setAgentSlip(calTot(w, new BilledBill(), PaymentMethod.Slip, BillType.AgentPaymentReceiveBill));

        c.setAgentCancelCash(calTot(w, new CancelledBill(), PaymentMethod.Cash, BillType.AgentPaymentReceiveBill));
        c.setAgentCancelCheque(calTot(w, new CancelledBill(), PaymentMethod.Cheque, BillType.AgentPaymentReceiveBill));
        c.setAgentCancelSlip(calTot(w, new CancelledBill(), PaymentMethod.Slip, BillType.AgentPaymentReceiveBill));

    }

    double calTot(WebUser w, Bill bill, PaymentMethod paymentMethod, BillType billType) {
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b  where type(b)=:bill and b.creater=:web and "
                + "b.paymentMethod= :pm "
                + " and b.billType= :billTp and b.createdAt between :fromDate and :toDate";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billTp", billType);
        temMap.put("pm", paymentMethod);
        temMap.put("bill", bill.getClass());
        temMap.put("web", w);
        List<Bill> bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        double tot = 0;
        for (Bill b : bills) {
            tot += b.getNetTotal();
        }
        return tot;
    }

    public void setCashierDatas(List<CashierSummeryData> cashierDatas) {
        this.cashierDatas = cashierDatas;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public double getFinalCashTot() {
        return finalCashTot;
    }

    public double getFinalCashTotOwn() {
        finalCashTot = 0.0;
        for (CashierSummeryData s : getCashierDatasOwn()) {
            finalCashTot += s.getNetCash();
        }
        return finalCashTot;
    }

    public void setFinalCashTot(double finalCashTot) {
        this.finalCashTot = finalCashTot;
    }

    public double getFinalCreditTot() {

        return finalCreditTot;
    }

    public double getFinalCreditTotOwn() {
        finalCreditTot = 0.0;
        for (CashierSummeryData s : getCashierDatasOwn()) {
            finalCreditTot += s.getNetCredit();
        }
        return finalCreditTot;
    }

    public void setFinalCreditTot(double finalCreditTot) {
        this.finalCreditTot = finalCreditTot;
    }

    public double getFinalCardTot() {

        return finalCardTot;
    }

    public double getFinalCreditCardTotOwn() {
        finalCardTot = 0.0;
        for (CashierSummeryData s : getCashierDatasOwn()) {
            finalCardTot += s.getNetCreditCard();
        }

        return finalCardTot;
    }

    public void setFinalCardTot(double finalCardTot) {
        this.finalCardTot = finalCardTot;
    }

    public CashierSummeryData getCurrent() {
        if (current == null) {
            current = new CashierSummeryData();
        }

        return current;
    }

    public void setCurrent(CashierSummeryData current) {
        this.current = current;

    }

    public WebUser getCurrentCashier() {
        if (currentCashier == null) {
            currentCashier = new WebUser();
        }

        return currentCashier;

    }

    private void setCashierData() {
        current = null;

        getCurrent().setCasheir(getCurrentCashier());
        findSummery(getCurrent(), getCurrentCashier());

    }

    public void setCurrentCashier(WebUser currentCashier) {
        this.currentCashier = currentCashier;
        setCashierData();
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public double getFinalSlipTot() {

        return finalSlipTot;
    }

    public double getFinalSlipTotOwn() {
        finalSlipTot = 0.0;
        for (CashierSummeryData s : getCashierDatasOwn()) {
            finalSlipTot += s.getNetSlip();
        }
        return finalSlipTot;
    }

    public void setFinalSlipTot(double finalSlipTot) {
        this.finalSlipTot = finalSlipTot;
    }

    public List<String1Value1> getDataTableDatas() {
        dataTableDatas = new ArrayList<>();
        String1Value1 tmp1 = new String1Value1();
        tmp1.setString("Final Credit Total");
        tmp1.setValue(getFinalCreditTotOwn());

        String1Value1 tmp2 = new String1Value1();
        tmp2.setString("Final Credit Card Total");
        tmp2.setValue(getFinalCreditCardTotOwn());

        String1Value1 tmp3 = new String1Value1();
        tmp3.setString("Final Cheque Total");
        tmp3.setValue(getFinalChequeTotOwn());

        String1Value1 tmp4 = new String1Value1();
        tmp4.setString("Final Slip Total");
        tmp4.setValue(getFinalSlipTotOwn());

        String1Value1 tmp5 = new String1Value1();
        tmp5.setString("Final Cash Total");
        tmp5.setValue(getFinalCashTotOwn());

        dataTableDatas.add(tmp1);
        dataTableDatas.add(tmp2);
        dataTableDatas.add(tmp3);
        dataTableDatas.add(tmp4);
        dataTableDatas.add(tmp5);

        return dataTableDatas;
    }

    public void setDataTableDatas(List<String1Value1> dataTableDatas) {
        this.dataTableDatas = dataTableDatas;
    }

    public List<WebUserBillsTotal> getWebUserBillsTotals() {
        return webUserBillsTotals;
    }

    public void setWebUserBillsTotals(List<WebUserBillsTotal> webUserBillsTotals) {
        this.webUserBillsTotals = webUserBillsTotals;
    }

    public EnumController getEnumController() {
        return enumController;
    }

    public void setEnumController(EnumController enumController) {
        this.enumController = enumController;
    }

    public String getFromReciptNo() {
        return fromReciptNo;
    }

    public void setFromReciptNo(String fromReciptNo) {
        this.fromReciptNo = fromReciptNo;
    }

    public String getToReciptNo() {
        return toReciptNo;
    }

    public void setToReciptNo(String toReciptNo) {
        this.toReciptNo = toReciptNo;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public void setCommonController(CommonController commonController) {
        this.commonController = commonController;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
