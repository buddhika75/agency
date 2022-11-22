/*
 * Open Hospital Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.bean.common;

import com.divudi.data.BillNumberSuffix;
import com.divudi.data.BillType;
import com.divudi.data.DepartmentType;
import com.divudi.data.FeeType;
import com.divudi.data.InstitutionType;
import com.divudi.data.PaymentMethod;
import com.divudi.data.MessageType;
import com.divudi.data.dataStructure.SearchKeyword;
import com.divudi.ejb.CommonFunctions;
import com.divudi.ejb.PharmacyBean;
import com.divudi.entity.Bill;
import com.divudi.entity.BillFee;
import com.divudi.entity.BillItem;
import com.divudi.entity.BilledBill;
import com.divudi.entity.CancelledBill;
import com.divudi.entity.Department;
import com.divudi.entity.Doctor;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.Patient;
import com.divudi.entity.PreBill;
import com.divudi.entity.RefundBill;
import com.divudi.entity.ServiceSession;
import com.divudi.entity.Speciality;
import com.divudi.entity.Staff;
import com.divudi.entity.pharmacy.Stock;
import com.divudi.facade.BillFacade;
import com.divudi.facade.BillFeeFacade;
import com.divudi.facade.BillItemFacade;
import com.divudi.facade.PatientFacade;
import com.divudi.facade.StockFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import org.primefaces.model.file.UploadedFile;

/**
 *
 * @author safrin
 */
@Named
@SessionScoped
public class SearchController implements Serializable {

    /**
     * EJBs
     */
    @EJB
    private CommonFunctions commonFunctions;
    @EJB
    private BillFacade billFacade;
    @EJB
    private BillFeeFacade billFeeFacade;
    @EJB
    private BillItemFacade billItemFacade;
    @EJB
    private PharmacyBean pharmacyBean;
    @EJB
    StockFacade stockFacade;

    /**
     * Inject
     */
    @Inject
    private BillBeanController billBean;
    @Inject
    private SessionController sessionController;
    @Inject
    TransferController transferController;
    @Inject
    private CommonController commonController;
    @Inject
    SmsController smsController;

    /**
     * Properties
     */
    private SearchKeyword searchKeyword;
    Date fromDate;
    Date toDate;
    private int maxResult = 50;
    private BillType billType;
    private PaymentMethod paymentMethod;
    private List<Bill> bills;
    private List<Bill> selectedBills;
    List<Bill> aceptPaymentBills;
    private List<BillFee> billFees;
    private List<BillFee> billFeesDone;
    private List<BillItem> billItems;
    Bill cancellingIssueBill;
    Bill bill;
    Speciality speciality;
    Staff staff;
    Item item;
    double dueTotal;
    double doneTotal;
    double netTotal;
    ServiceSession selectedServiceSession;
    Staff currentStaff;
    List<BillItem> billItem;

    String menuBarSearchText;
    String smsText;
    String uniqueSmsText;
    boolean channelingPanelVisible;
    boolean pharmacyPanelVisible;
    boolean opdPanelVisible;
    boolean inwardPanelVisible;
    boolean labPanelVisile;
    boolean patientPanelVisible;

    List<Bill> channellingBills;
    List<Bill> opdBills;
    List<Bill> pharmacyBills;
    List<Patient> patients;
    List<String> telephoneNumbers;
    List<String> selectedTelephoneNumbers;
    List<PharmacyAdjustmentRow> pharmacyAdjustmentRows;

    UploadedFile file;
    private Institution creditCompany;

    public String menuBarSearch() {
        JsfUtil.addSuccessMessage("Sarched From Menubar" + "\n" + menuBarSearchText);
        return "/index";
    }

    public void fillBillSessions() {
        BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        List<BillType> bts = Arrays.asList(billTypes);

        String sql = "Select bs From BillSession bs "
                + " where bs.retired=false"
                + " and bs.bill.billType in :bt"
                + " and type(bs.bill)=:class "
                + " and ("
                + "    lower(bs.bill.insId) like :txt "
                + " or lower(bs.bill.deptId) like :txt "
                + " or lower(bs.bill.referralNumber) like :txt "
                + " or lower(bs.referenceBillSession.bill.insId) like :txt "
                + " or lower(bs.referenceBillSession.bill.deptId) like :txt "
                + " or lower(bs.referenceBillSession.bill.referralNumber) like :txt "
                + " or lower(bs.bill.referenceBill.insId) like :txt "
                + " or lower(bs.bill.referenceBill.deptId) like :txt "
                + " or lower(bs.bill.referenceBill.referralNumber) like :txt "
                + " )"
                + " order by bs.sessionDate, bs.serialNo ";
        HashMap hh = new HashMap();
        hh.put("bt", bts);
        hh.put("class", BilledBill.class);
        hh.put("txt", "%" + menuBarSearchText.toLowerCase().trim() + "%");
    }

    public void makeListNull() {
        maxResult = 50;
        bills = null;
        aceptPaymentBills = null;
        selectedBills = null;
        billFees = null;
        billItems = null;
        searchKeyword = null;
    }

    public void makeListNull2() {
        billFeesDone = null;
        searchKeyword = null;
        speciality = null;
        staff = null;
        item = null;
        makeListNull();
    }

    public void createPatientInvestigationsTableLogin() {
        Date startTime = new Date();

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  "
                + " and b.department=:dep ";

        Map temMap = new HashMap();

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by pi.id desc  ";
//    

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("dep", getSessionController().getLoggedUser().getDepartment());

        //System.err.println("Sql " + sql);
        commonController.printReportDetails(fromDate, toDate, startTime, "Lab/report.search/logged department(/faces/lab/search_for_reporting_ondemand.xhtml)");
    }

    public void createPreRefundTable() {

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from RefundBill b where b.billType = :billType "
                + " and b.institution=:ins and "
                + " (b.billedBill is null  or type(b.billedBill)=:billedClass ) "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false and b.deptId is not null ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billedClass", PreBill.class);
        temMap.put("billType", BillType.PharmacyPre);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQLWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 50);

        Date startTime = new Date();
        commonController.printReportDetails(fromDate, toDate, startTime, "Search bills for refunds(/pharmacy/pharmacy_search_pre_refund_bill_for_return_cash.xhtml)");

    }

    public void reportSettledOPDBills() {
        Date startTime = new Date();
        settledBills(billType.OpdBill);

        commonController.printReportDetails(fromDate, toDate, startTime, "Credit Payment Bills(/opd_search_bill_full_paid.xhtml)");
    }

    public void reportSettledPharmacyBills() {
        Date startTime = new Date();
        settledBills(billType.PharmacyWholeSale);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Receieve/Credit Company/Pharmacy/Credit bill pay search(/faces/credit/pharmacy_search_bill_full_paid.xhtml)");
    }

    public void settledBills(BillType bt) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where "
                + " b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.referenceBill.billType=:bt "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                //                + " and b.balance=0 "
                + "order by b.createdAt desc ";

//    
        temMap.put("billType", BillType.CashRecieveBill);
        temMap.put("bt", bt);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    List<billsWithbill> withbills;

    public List<billsWithbill> getWithbills() {
        return withbills;
    }

    public void setWithbills(List<billsWithbill> withbills) {
        this.withbills = withbills;
    }

    public void createCreditBillsWithOPDBill() {
        Date startTime = new Date();
        createCreditBillsWithBill(billType.OpdBill);

        commonController.printReportDetails(fromDate, toDate, startTime, "Credit paid bills with OPDbills(/opd_search_bill_full_paid_bills.xhtml)");
    }

    public void createCreditBillsWithPharmacyBill() {
        Date startTime = new Date();

        createCreditBillsWithBill(billType.PharmacyWholeSale);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Receieve/Credit Company/Pharmacy/Credit bill with payment bills(/faces/credit/pharmacy_search_bill_full_paid_bills.xhtml)");
    }

    public void createCreditBillsWithBill(BillType refBillType) {
        bills = fetchBills(BillType.CashRecieveBill, refBillType);
        ////System.out.println("bills = " + bills);
        withbills = new ArrayList<>();

        for (Bill b : bills) {
            billsWithbill bWithbill = new billsWithbill();
            bWithbill.setB(b);
            bWithbill.setCaBills(fetchCreditBills(BillType.CashRecieveBill, b));
            ////System.out.println("bWithbill.getCaBills() = " + bWithbill.getCaBills());
            withbills.add(bWithbill);
        }
        ////System.out.println("withbills = " + withbills);

    }

    public List<Bill> fetchBills(BillType bt, BillType rbt) {

        String sql;
        Map temMap = new HashMap();

        sql = "select DISTINCT(b.referenceBill) from Bill b where "
                + " b.billType =:billType "
                + " and b.referenceBill.billType =:billTypeRef "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        temMap.put("billType", bt);
        temMap.put("billTypeRef", rbt);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
    }

    public List<Bill> fetchCreditBills(BillType bt, Bill b) {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where "
                + " b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.referenceBill=:bid ";

        temMap.put("billType", bt);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bid", b);
        temMap.put("ins", getSessionController().getInstitution());

        return getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public void setCommonController(CommonController commonController) {
        this.commonController = commonController;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    private void checkLabReportsApproved(List<Bill> bills) {
        for (Bill b : bills) {
            String sql;
            Map m = new HashMap();
            sql = " select pr from PatientReport pr where "
                    + " pr.retired=false "
                    + " and pr.patientInvestigation.billItem.bill=:b "
                    + " and pr.approved=true ";

            m.put("b", b);
        }
    }

    private void checkLabReportsApprovedBillItem(List<BillItem> billItems) {
        for (BillItem bi : billItems) {
            String sql;
            Map m = new HashMap();
            sql = " select pr from PatientReport pr where "
                    + " pr.retired=false "
                    + " and pr.patientInvestigation.billItem=:bi "
                    + " and pr.approved=true ";

            m.put("bi", bi);
        }
    }

    public Institution getCreditCompany() {
        return creditCompany;
    }

    public void setCreditCompany(Institution creditCompany) {
        this.creditCompany = creditCompany;
    }

    public Department getFromDepartment() {
        return fromDepartment;
    }

    public void setFromDepartment(Department fromDepartment) {
        this.fromDepartment = fromDepartment;
    }

    public Department getToDepartment() {
        return toDepartment;
    }

    public void setToDepartment(Department toDepartment) {
        this.toDepartment = toDepartment;
    }

    public PatientFacade getPatientFacade() {
        return patientFacade;
    }

    public class billsWithbill {

        Bill b;
        List<Bill> caBills;

        public Bill getB() {
            return b;
        }

        public void setB(Bill b) {
            this.b = b;
        }

        public List<Bill> getCaBills() {
            if (caBills == null) {
                caBills = new ArrayList<>();
            }
            return caBills;
        }

        public void setCaBills(List<Bill> caBills) {
            this.caBills = caBills;
        }

    }

    public void createReturnSaleBills() {
        Date startTime = new Date();

        createReturnSaleBills(BillType.PharmacyPre, true);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Sale Bills/Search return bill(/faces//pharmacy/pharmacy_search_return_bill_pre.xhtml)");

    }

    public void createReturnSaleAllBills() {
        Date startTime = new Date();

        createReturnSaleBills(BillType.PharmacyPre, false);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Sale Bills/Search return bill(search all)(/faces//pharmacy/pharmacy_search_return_bill_pre.xhtml)");
    }

    public void createReturnWholeSaleBills() {
        createReturnSaleBills(BillType.PharmacyWholesalePre, true);
    }

    public void createReturnWholeSaleAllBills() {
        createReturnSaleBills(BillType.PharmacyWholesalePre, false);
    }

    public void createReturnSaleBills(BillType billType, boolean maxNum) {

        Map m = new HashMap();
        m.put("bt", billType);
        m.put("billedClass", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from RefundBill b where  b.retired=false "
                + " and b.institution=:ins and "
                + " (b.billedBill is null  or type(b.billedBill)=:billedClass ) "
                + " and b.createdAt between :fd and :td"
                + " and b.billType=:bt ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  (upper(b.billedBill.deptId) like :rNo )";
            m.put("rNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
        if (maxNum == true) {
            bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 25);
        } else {
            bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);
        }

    }

    public void createTableByKeywordToPayBills() {
        Date startTime = new Date();
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where "
                + " b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.balance>0 ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((upper(b.deptId) like :billNo )or(upper(b.insId) like :billNo ))";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.OpdBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        commonController.printReportDetails(fromDate, toDate, startTime, "OPD Credit bill/Bill to pay search(/opd_search_bill_to_pay.xhtml)");
    }

    public void createTablePharmacyCreditToPayBills() {
        Date startTime = new Date();

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where "
                + " b.billType in :billTypes "
                + " and b.institution=:ins "
                + " and b.department=:dep "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and (b.netTotal-b.paidAmount)>0 "
                + " and b.paymentMethod=:pm ";

        if (getSearchKeyword().getInstitution() != null && !getSearchKeyword().getInstitution().trim().equals("")) {
            sql += " and  (upper(b.toInstitution.name) like :comp )";
            temMap.put("comp", "%" + getSearchKeyword().getInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.toStaff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((upper(b.deptId) like :billNo )or(upper(b.insId) like :billNo ))";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billTypes", Arrays.asList(new BillType[]{BillType.PharmacyWholeSale, BillType.PharmacySale}));
        temMap.put("pm", PaymentMethod.Credit);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("dep", getSessionController().getDepartment());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Receieve/Credit Company/Pharmacy/Credit bill to pay search(/faces/credit/pharmacy_search_bill_to_pay.xhtml)");

    }

    public void createReturnBhtBills() {
        Date startTime = new Date();

        createReturnBhtBills(BillType.PharmacyBhtPre);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/BHT Bills/Search return bill(/faces/inward/pharmacy_search_return_bill_bht.xhtml)");

    }

    public void createReturnBhtBillsStore() {
        createReturnBhtBills(BillType.StoreBhtPre);
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    private void createReturnBhtBills(BillType billType) {

        Map m = new HashMap();
        m.put("bt", billType);
        m.put("billedClass", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from RefundBill b where  b.retired=false "
                + " and b.institution=:ins and "
                + " (b.billedBill is null  or type(b.billedBill)=:billedClass ) "
                + " and b.createdAt between :fd and :td"
                + " and b.billType=:bt ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  (upper(b.billedBill.deptId) like :rNo )";
            m.put("rNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            m.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createVariantReportSearch() {
        Date startTime = new Date();

        String sql = "";
        HashMap tmp = new HashMap();

        sql = "Select b From PreBill b where b.cancelledBill is null  "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false and b.billType= :bTp ";

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :dep )";
            tmp.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCategory() != null && !getSearchKeyword().getCategory().trim().equals("")) {
            sql += " and  (upper(b.category.name) like :cat )";
            tmp.put("cat", "%" + getSearchKeyword().getCategory().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("bTp", BillType.PharmacyMajorAdjustment);
        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Reports/Administration/Major stock adjustments/Stock variant adjustments(/faces/pharmacy/pharmacy_variant_ajustment_pre_list.xhtml)");
    }

    List<Bill> prescreptionBills;
    Department department;
    private Department fromDepartment;
    private Department toDepartment;

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<Bill> getPrescreptionBills() {
        return prescreptionBills;
    }

    public void setPrescreptionBills(List<Bill> prescreptionBills) {
        this.prescreptionBills = prescreptionBills;
    }

    public void createPharmacyPrescriptionBillTable() {
        Date startTime = new Date();

        Map m = new HashMap();
        m.put("bt", BillType.PharmacyPre);
        m.put("rBt", BillType.PharmacySale);
        m.put("class", PreBill.class);
        m.put("rClass", BilledBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;
        sql = "Select b from Bill b "
                + " where b.retired=false and b.createdAt between :fd and :td and b.billType=:bt "
                + " and b.referredBy is not null "
                + " and b.institution=:ins "
                + " and b.referenceBill.billType=:rBt "
                + " and type(b)=:class "
                + " and type(b.referenceBill)=:rClass ";

        if (department != null) {
            sql += " and b.department=:dept ";
            m.put("dept", department);
        }

        sql += " order by b.createdAt ";

        prescreptionBills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Reports/Sale Reports/Prescription report(/faces/pharmacy/report_prescreption.xhtml)");

    }

    public void createPharmacyRetailBills() {
        Date startTime = new Date();

        createPharmacyRetailBills(BillType.PharmacyPre, true);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Sale Bills/search sale bills(/faces/pharmacy/pharmacy_search_sale_bill.xhtml)");
    }

    public void createPharmacyWholesaleBills() {
        createPharmacyRetailBills(BillType.PharmacyWholesalePre, true);
    }

    public void createPharmacyRetailAllBills() {
        Date startTime = new Date();

        createPharmacyRetailBills(BillType.PharmacyPre, false);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Sale Bills/search sale bills(search all)(/faces/pharmacy/pharmacy_search_sale_bill.xhtml)");

    }

    public void createPharmacyWholesaleAllBills() {
        createPharmacyRetailBills(BillType.PharmacyWholesalePre, false);
    }

    public void createPharmacyRetailBills(BillType billtype, boolean maxNum) {

        Map m = new HashMap();
        m.put("bt", billtype);
        //   m.put("class", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        m.put("ldep", getSessionController().getLoggedUser().getDepartment());
        String sql;

        sql = "Select b from PreBill b where "
                + " b.createdAt between :fd and :td "
                + " and b.billType=:bt"
                + " and b.billedBill is null "
                + " and b.institution=:ins "
                + " and b.department=:ldep";
        //  + " and type(b)=:class ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :dep )";
            m.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }
        if (getPaymentMethod() != null) {
            sql += " and b.paymentMethod=:pay ";
            m.put("pay", paymentMethod);
        }

        sql += " order by b.createdAt desc  ";
//    
        //     //////System.out.println("sql = " + sql);

        if (maxNum == true) {
            bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 25);
        } else {
            bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);
        }
        netTotal = 0.0;
        for (Bill b : bills) {
            netTotal += b.getNetTotal();
        }
    }

    public String searchMyPharmacyBills() {
        BillType billtype = BillType.PharmacyPre;
        String sql;
        if (false) {
            Bill b = new Bill();
            b.getPatient().getPerson();
            sessionController.getLoggedUser().getWebUserPerson();
        }
        sql = "Select b from PreBill b where "
                + " b.billType=:bt"
                + " and b.billedBill is null "
                + " and b.patient.person=:person";
        sql += " order by b.createdAt desc  ";
        Map m = new HashMap();
        m.put("bt", billtype);
        m.put("person", sessionController.getLoggedUser().getWebUserPerson());

        boolean maxNum = true;
        if (maxNum == true) {
            bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 25);
        } else {
            bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);
        }
        netTotal = 0.0;
        for (Bill b : bills) {
            netTotal += b.getNetTotal();
        }

        return "/mobile/my_pharmacy_bills";
    }

    double netTotalValue;

    public void createPharmacyStaffBill() {
        Date startTime = new Date();

        Map m = new HashMap();
        m.put("bt", BillType.PharmacyPre);
        //   m.put("class", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from PreBill b where "
                + " b.createdAt between :fd and :td "
                + " and b.billType=:bt"
                + " and b.billedBill is null "
                + " and b.institution=:ins "
                + " and b.toStaff is not null "
                + " order by b.createdAt ";
//    
        //     //////System.out.println("sql = " + sql);
        bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);

        netTotalValue = 0.0;
        for (Bill b : bills) {
            netTotalValue += b.getNetTotal();
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Reports/Summeries/BHT issue/BHT issue - staff(/faces/pharmacy/pharmacy_report_staff_issue_bill.xhtml)");
    }

    public void createPharmacyTableRe() {
        Date startTime = new Date();

        Map m = new HashMap();
        m.put("bt", BillType.PharmacyPre);
        //     m.put("class", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from PreBill b where "
                + " b.createdAt between :fd and :td "
                + " and b.billType=:bt "
                + " and b.cancelled=true "
                + " and b.institution=:ins";
        //+ " and type(b)=:class ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :dep )";
            m.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        //     //////System.out.println("sql = " + sql);
        bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Sale Bills/Search sale pre bill(/faces/pharmacy/pharmacy_search_sale_pre_bill.xhtml)");

    }

    public void listPharmacyIssue() {
        Date startTime = new Date();

        listPharmacyPreBills(BillType.PharmacyIssue);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Issue to units/Search Issue Bill(/faces/pharmacy/pharmacy_search_issue_bill.xhtml)");
    }

    public void listStoreIssue() {
        Date startTime = new Date();
        listPharmacyPreBills(BillType.StoreIssue);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Unit Issue/Search Issue Bill(/faces/store/store_search_issue_bill.xhtml)");
    }

    public void listPharmacyCancelled() {
        listPharmacyCancelledBills(BillType.PharmacyIssue);
    }

    public void listPharmacyReturns() {
        Date startTime = new Date();

        listPharmacyStoreReturnedBills(BillType.PharmacyIssue);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Issue to units/Search issue return bills (/faces/pharmacy/pharmacy_search_issue_bill_return.xhtml)");
    }

    public void listStoreReturns() {
        Date startTime = new Date();
        listPharmacyStoreReturnedBills(BillType.StoreIssue);

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Unit Issue/Search issue return bill(/faces/store/store_search_issue_bill_return.xhtml)");
    }

    public void listPharmacyBilledBills(BillType bt) {
        listPharmacyBills(bt, BilledBill.class);
    }

    public void listPharmacyPreBills(BillType bt) {
        listPharmacyBills(bt, PreBill.class);
    }

    public void listPharmacyCancelledBills(BillType bt) {
        listPharmacyBills(bt, CancelledBill.class);
    }

    public void listPharmacyStoreReturnedBills(BillType bt) {
        listReturnBills(bt, RefundBill.class);
    }

    public ServiceSession getSelectedServiceSession() {
        return selectedServiceSession;
    }

    public void setSelectedServiceSession(ServiceSession selectedServiceSession) {
        this.selectedServiceSession = selectedServiceSession;
    }

    public Staff getCurrentStaff() {
        return currentStaff;
    }

    public void setCurrentStaff(Staff currentStaff) {
        this.currentStaff = currentStaff;
    }

    public void listPharmacyBills(BillType bt, Class bc) {

        Map m = new HashMap();
        m.put("bt", bt);
        m.put("class", bc);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        String sql;

        sql = "Select b from Bill b where "
                + " b.createdAt between :fd and :td "
                + " and b.billType=:bt "
                + " and b.institution=:ins "
                + " and type(b)=:class "
                + " and b.billedBill is null ";

        if (getSearchKeyword().getRequestNo() != null && !getSearchKeyword().getRequestNo().trim().equals("")) {
            sql += " and  (upper(b.invoiceNumber) like :requestNo )";
            m.put("requestNo", "%" + getSearchKeyword().getRequestNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :dep )";
            m.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        //     //////System.out.println("sql = " + sql);
        bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void listReturnBills(BillType bt, Class bc) {

        Map m = new HashMap();
        m.put("bt", bt);
        m.put("class", bc);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        m.put("dept", getSessionController().getDepartment());
        String sql;

        sql = "Select b from Bill b where "
                + " b.createdAt between :fd and :td "
                + " and b.billType=:bt "
                + " and b.institution=:ins "
                + " and b.department=:dept "
                + " and type(b)=:class ";

        if (getSearchKeyword().getRequestNo() != null && !getSearchKeyword().getRequestNo().trim().equals("")) {
            sql += " and  (upper(b.invoiceNumber) like :requestNo )";
            m.put("requestNo", "%" + getSearchKeyword().getRequestNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :dep )";
            m.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        //     //////System.out.println("sql = " + sql);
        bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyTableBht() {
        Date startTime = new Date();

        createTableBht(BillType.PharmacyBhtPre);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/BHT Bills/Theater Billing(/faces/inward/pharmacy_search_sale_bill_bht.xhtml)");
    }

    public void createStoreTableIssue() {
        createTableBht(BillType.StoreIssue);
    }

    public void createStoreTableBht() {
        Date startTime = new Date();
        createTableBht(BillType.StoreBhtPre);

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/BHT issue/Search issue billing(/faces/store/store_search_sale_bill_bht.xhtml)");
    }

    public void createTableBht(BillType btp) {

        Map m = new HashMap();
        m.put("bt", btp);
        m.put("class", PreBill.class);
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        m.put("ins", getSessionController().getInstitution());
        m.put("dep", getSessionController().getDepartment());
        String sql;

        sql = "Select b from Bill b "
                + " where b.retired=false "
                + " and b.billedBill is null "
                + " and b.createdAt between :fd and :td "
                + " and b.billType=:bt"
                + " and b.institution=:ins "
                + " and b.department=:dep "
                + " and type(b)=:class ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            m.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :dep )";
            m.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            m.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        //     //////System.out.println("sql = " + sql);
        bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createIssueTable() {
        Date startTime = new Date();

        String sql;
        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.PharmacyTransferIssue);
        sql = "Select b From BilledBill b where b.retired=false and "
                + " b.toDepartment=:dep and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.toStaff.person.name) like :stf )";
            tmp.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :fDep )";
            tmp.put("fDep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setTmpRefBill(getRefBill(b));

        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Transfer/receive (/faces/pharmacy/pharmacy_transfer_issued_list.xhtml)");

    }

    public void createIssueReport1() {
        String sql;
        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        //tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.PharmacyTransferIssue);
        sql = "Select b From BilledBill b "
                + " where b.retired=false "
                //+ " and b.toDepartment=:dep "
                + " and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.toStaff.person.name) like :stf )";
            tmp.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFrmDepartment() != null) {
            sql += " and b.department=:frmdep";
            tmp.put("frmdep", getSearchKeyword().getFrmDepartment());
        }

        if (getSearchKeyword().getTooDepartment() != null) {
            sql += " and b.toDepartment=:tdep";
            tmp.put("tdep", getSearchKeyword().getTooDepartment());
        }

        sql += " order by b.createdAt desc  ";

        List<Bill> list = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);
        bills = new ArrayList<>();
        netTotalValue = 0.0;
        for (Bill b : list) {
//            ////System.out.println("b = ");

            Bill refBill = getActiveRefBill(b);
            if (refBill == null) {
                ////System.out.println("b = " + refBill);
                netTotalValue += b.getNetTotal();
                bills.add(b);
            }
        }

    }

    public void createIssuePharmacyReport() {
        Date startTime = new Date();
//        fetchPharmacyBills(BillType.PharmacyTransferIssue, BillType.PharmacyTransferReceive);
        fetchPharmacyBillsNew(BillType.PharmacyTransferIssue, BillType.PharmacyTransferReceive);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Reports/Transfer Report/Report transfer issued not receieved(/faces/pharmacy/pharmacy_transfer_issued_list_report.xhtml)");
    }

    public void createIssueStoreReport() {
        fetchPharmacyBills(BillType.StoreTransferIssue, BillType.StoreTransferReceive);
    }

    public void createPoNotPharmacyApproveReport() {
        Date startTime = new Date();

        fetchPharmacyBills(BillType.PharmacyOrder, BillType.PharmacyAdjustment);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Reports/Purchase Reports/Purchase ordered not approved.(/faces/pharmacy/pharmacy_transfer_issued_list_not_approve_report.xhtml)");
    }

    public void createPoNotStoreApproveReport() {
        fetchPharmacyBills(BillType.StoreOrder, BillType.StoreOrderApprove);
    }

    public void fetchPharmacyBills(BillType billType, BillType billType2) {
        String sql;
        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        //tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", billType);
        sql = "Select b From BilledBill b "
                + " where b.retired=false "
                //+ " and b.toDepartment=:dep "
                + " and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.toStaff.person.name) like :stf )";
            tmp.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFrmDepartment() != null) {
            sql += " and b.department=:frmdep";
            tmp.put("frmdep", getSearchKeyword().getFrmDepartment());
        }

        if (getSearchKeyword().getTooDepartment() != null) {
            sql += " and b.toDepartment=:tdep";
            tmp.put("tdep", getSearchKeyword().getTooDepartment());
        }

        sql += " order by b.createdAt desc  ";

        List<Bill> list = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);
        bills = new ArrayList<>();
        netTotalValue = 0.0;
        for (Bill b : list) {
//            ////System.out.println("b = ");

            Bill refBill = getActiveRefBillnotApprove(b, billType2);
            if (refBill == null) {
                ////System.out.println("b = " + refBill);
                netTotalValue += b.getNetTotal();
                bills.add(b);
            }
        }

    }

    public void fetchPharmacyBillsNew(BillType billType, BillType billType2) {
        String sql;
        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        //tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", billType);
        sql = "Select b From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false "
                + " and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.toStaff.person.name) like :stf )";
            tmp.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFrmDepartment() != null) {
            sql += " and b.department=:frmdep";
            tmp.put("frmdep", getSearchKeyword().getFrmDepartment());
        }

        if (getSearchKeyword().getTooDepartment() != null) {
            sql += " and b.toDepartment=:tdep";
            tmp.put("tdep", getSearchKeyword().getTooDepartment());
        }

        sql += " order by b.createdAt desc  ";

        List<Bill> list = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP);
        bills = new ArrayList<>();
        netTotalValue = 0.0;
        for (Bill b : list) {
//            ////System.out.println("b = ");

            Bill refBill = getActiveRefBillnotApprove(b, billType2);
            if (refBill == null) {
                ////System.out.println("b = " + refBill);
                netTotalValue += b.getNetTotal();
                bills.add(b);
            }
        }

    }

    public void createIssueTableStore() {
        Date startTime = new Date();

        String sql;
        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.StoreTransferIssue);
        sql = "Select b From BilledBill b where b.retired=false and "
                + " b.toDepartment=:dep and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.toStaff.person.name) like :stf )";
            tmp.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :fDep )";
            tmp.put("fDep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setTmpRefBill(getRefBill(b));

        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Transfer/Receieve (/faces/store/store_transfer_issued_list.xhtml)");

    }

    private Bill getRefBill(Bill b) {
        String sql = "Select b From Bill b where b.retired=false "
                + " and b.cancelled=false and b.billType=:btp and "
                + " b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyTransferReceive);
        return getBillFacade().findFirstBySQL(sql, hm);
    }

    private Bill getActiveRefBill(Bill b) {
        String sql = "Select b From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false"
                + "  and b.billType=:btp"
                + " and b.backwardReferenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyTransferReceive);
        return getBillFacade().findFirstBySQL(sql, hm);
    }

    private Bill getActiveRefBillnotApprove(Bill b, BillType billType) {
        String sql = "Select b From BilledBill b "
                + " where b.retired=false "
                + " and b.cancelled=false"
                + "  and b.billType=:btp"
                + " and b.backwardReferenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", billType);
        return getBillFacade().findFirstBySQL(sql, hm);
    }

    public void makeNull() {
        searchKeyword = null;
        bills = null;

    }

    public void createTableByBillType() {
        Date startTime = new Date();

        if (billType == null) {
            JsfUtil.addErrorMessage("Please Select Bill Type");
            return;

        }

        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where b.retired=false and "
                + " (type(b)=:class1 or type(b)=:class2) "
                + " and b.department=:dep and b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRequestNo() != null && !getSearchKeyword().getRequestNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :requestNo )";
            temMap.put("requestNo", "%" + getSearchKeyword().getRequestNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromDepartment() != null && !getSearchKeyword().getFromDepartment().trim().equals("")) {
            sql += " and  (upper(b.fromDepartment.name) like :frmDept )";
            temMap.put("frmDept", "%" + getSearchKeyword().getFromDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  (upper(b.toInstitution.name) like :toIns )";
            temMap.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToDepartment() != null && !getSearchKeyword().getToDepartment().trim().equals("")) {
            sql += " and  (upper(b.toDepartment.name) like :toDept )";
            temMap.put("toDept", "%" + getSearchKeyword().getToDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.deptId) like :refId )";
            temMap.put("refId", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.invoiceNumber) like :inv )";
            temMap.put("inv", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " (upper(bItem.item.name) like :itm ))";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " (upper(bItem.item.code) like :cde ))";
            temMap.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("class1", BilledBill.class);
        temMap.put("class2", PreBill.class);
        temMap.put("billType", billType);
        temMap.put("dep", getSessionController().getDepartment());
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        //temMap.put("dep", getSessionController().getDepartment());
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, maxResult);
        //     //System.err.println("SIZE : " + lst.size());

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Search/(/faces/pharmacy/pharmacy_search.xhtml)");
    }

    public void createGRNRegistory() {
        String sql;
        Map m = new HashMap();

        sql = "select b from Bill b where b.retired=false and "
                + " (type(b)=:class1 or type(b)=:class2) "
                + " and b.department=:dep and b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate ";

        sql += " order by b.createdAt desc  ";

        m.put("class1", BilledBill.class);
        m.put("class2", PreBill.class);
        m.put("billType", BillType.PharmacyGrnBill);
        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        //temMap.put("dep", getSessionController().getDepartment());
        bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);
        //     //System.err.println("SIZE : " + lst.size());

    }

    public void createTableByBillTypeAllDepartment() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where b.retired=false and "
                + " (type(b)=:class1 or type(b)=:class2) "
                + " and  b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRequestNo() != null && !getSearchKeyword().getRequestNo().trim().equals("")) {
            sql += " and (upper(b.insId) like :requestNo)";
            temMap.put("requestNo", "%" + getSearchKeyword().getRequestNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  (upper(b.toInstitution.name) like :toIns )";
            temMap.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.deptId) like :refId )";
            temMap.put("refId", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.invoiceNumber) like :inv )";
            temMap.put("inv", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " (upper(bItem.item.name) like :itm ))";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and b.id in (select bItem.bill.id  "
                    + " from BillItem bItem where bItem.retired=false and  "
                    + " (upper(bItem.item.code) like :cde ))";
            temMap.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("class1", BilledBill.class);
        temMap.put("class2", PreBill.class);
        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        //temMap.put("dep", getSessionController().getDepartment());
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, maxResult);
        //     //System.err.println("SIZE : " + lst.size());

    }

    public void createRequestTable() {
        Date startTime = new Date();

        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("toDep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.PharmacyTransferRequest);

        sql = "Select b From Bill b where "
                + " b.retired=false and  b.toDepartment=:toDep"
                + " and b.billType= :bTp and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :dep )";
            tmp.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getIssudBills(b));
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Transfer/Issue(/faces/pharmacy/pharmacy_transfer_request_list.xhtml)");

    }

    public void createInwardBHTRequestTable() {
        Date startTime = new Date();

        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("dep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.InwardPharmacyRequest);

        sql = "Select b From Bill b where "
                + " b.retired=false and  b.department=:dep "
                + " and b.billType= :bTp and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and ((upper(b.insId) like :billNo ) or (upper(b.deptId) like :billNo )) ";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            tmp.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getBHTIssudBills(b));
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Transfer/Issue(/faces/pharmacy/pharmacy_transfer_request_list.xhtml)");

    }

    public void createInwardBHTForIssueTableAll() {
        createInwardBHTForIssueTable(null);
    }

    public void createInwardBHTForNotIssueTable() {
        createInwardBHTForIssueTable(true);
    }

    public void createInwardBHTForIssueOnlyTable() {
        createInwardBHTForIssueTable(false);
    }

    public void createInwardBHTForIssueTable(Boolean bool) {
        Date startTime = new Date();

        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("toDep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.InwardPharmacyRequest);

        sql = "Select b From Bill b where "
                + " b.retired=false and  b.toDepartment=:toDep"
                + " and b.billType= :bTp and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and ((upper(b.insId) like :billNo ) or (upper(b.deptId) like :billNo )) ";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            tmp.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 100);

        for (Bill b : bills) {
            b.setListOfBill(getBHTIssudBills(b));
        }

        if (bool != null) {
            List<Bill> bs = new ArrayList<>();
            for (Bill b : bills) {
            }
            if (bool) {
                bills = bs;
            } else {
                bills.removeAll(bs);
            }
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Transfer/Issue(/faces/pharmacy/pharmacy_transfer_request_list.xhtml)");

    }

    public long createInwardBHTForIssueBillCount() {
        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("toDep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.InwardPharmacyRequest);

        sql = "Select COUNT(b) From Bill b "
                + " where b.retired=false "
                + " and b.toDepartment=:toDep "
                + " and b.cancelled=false "
                + " and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        long count = 0l;
        count = getBillFacade().countBySql(sql, tmp, TemporalType.TIMESTAMP);

        return count;

    }

    public void createRequestTableStore() {
        Date startTime = new Date();

        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("toDep", getSessionController().getDepartment());
        tmp.put("bTp", BillType.StoreTransferRequest);

        sql = "Select b From Bill b where "
                + " b.retired=false and  b.toDepartment=:toDep"
                + " and b.billType= :bTp and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :dep )";
            tmp.put("dep", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        for (Bill b : bills) {
            b.setListOfBill(getIssudBills(b));
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Transfer/Issue(/faces/store/store_transfer_request_list.xhtml)");

    }

    public void createListToCashRecieve() {
        String sql;

        HashMap tmp = new HashMap();
        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("toWeb", getSessionController().getLoggedUser());
        tmp.put("bTp", BillType.CashOut);

        sql = "Select b From Bill b where "
                + " b.retired=false "
                + " and  b.toWebUser=:toWeb"
                + " and b.billType= :bTp"
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :net )";
            tmp.put("net", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPersonName() != null && !getSearchKeyword().getPersonName().trim().equals("")) {
            sql += " and  (upper(b.fromWebUser.webUserPerson.name) like :dep )";
            tmp.put("dep", "%" + getSearchKeyword().getPersonName().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyBillItemTable() {
        Date startTime = new Date();

        createPharmacyBillItemTable(BillType.PharmacyPre, BillType.PharmacySale);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Sale Bills/Search sale bill item(/faces/pharmacy/pharmacy_search_sale_bill_item.xhtml)");
    }

    public void createPharmacyWholeBillItemTable() {
        createPharmacyBillItemTable(BillType.PharmacyWholesalePre, BillType.PharmacyWholeSale);
    }

    public void createPharmacyBillItemTable(BillType billType, BillType refBillType) {
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", billType);
        m.put("rBType", refBillType);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);
        m.put("rClass", BilledBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class and type(bi.bill.referenceBill)=:rClass"
                + " and bi.bill.institution=:ins"
                + " and bi.bill.billType=:bType and "
                + " bi.bill.referenceBill.billType=:rBType "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(bi.bill.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(bi.netValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  (upper(bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyIssueBillItemTable() {
        Date startTime = new Date();
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.PharmacyIssue);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins"
                + " and bi.bill.billType=:bType "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(bi.bill.toDepartment.name) like :deptName )";
            m.put("deptName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(bi.netValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  (upper(bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Issue to units/search Issue Bill items(/faces/pharmacy/pharmacy_search_issue_bill_item.xhtml)");

    }

    public void createStoreIssueBillItemTable() {
        Date startTime = new Date();
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.StoreIssue);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins"
                + " and bi.bill.billType=:bType "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(bi.bill.toDepartment.name) like :deptName )";
            m.put("deptName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(bi.netValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  (upper(bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Unit Issue/Search issue bill items(/faces/store/store_search_issue_bill_item.xhtml)");

    }

    public void createErronousStoreIssueBillItemTable() {

        Date startTime = new Date();
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.StoreIssue);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins "
                + " and bi.bill.billType=:bType "
                + " and bi.item.retired=true "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(bi.bill.toDepartment.name) like :deptName )";
            m.put("deptName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(bi.netValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  (upper(bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Administration/Error checking/Item issued from retired item(/faces/store/store_search_issue_bill_item_error.xhtml)");

    }

    public void createPharmacyAdjustmentBillItemTable() {
        Date startTime = new Date();

        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.PharmacyAdjustment);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins"
                + " and bi.bill.billType=:bType  "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  (upper(bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Adjustments/Search adjustment bills(/faces/pharmacy/pharmacy_search_adjustment_bill_item.xhtml)");

    }

    public void createPharmacyAdjustmentBillItemTableForStockTaking() {
        fetchAdjustmentBillItemTableForStockTaking(BillType.PharmacyAdjustment);
    }

    public void createStoreAdjustmentBillItemTableForStockTaking() {
        fetchAdjustmentBillItemTableForStockTaking(BillType.StoreAdjustment);
    }

    private void fetchAdjustmentBillItemTableForStockTaking(BillType billType) {
        pharmacyAdjustmentRows = new ArrayList<>();

        String sql;
        Map m = new HashMap();

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins "
                + " and bi.bill.department=:dep "
                + " and bi.bill.billType=:bType  "
                + " and bi.createdAt between :fromDate and :toDate ";

        sql += " order by bi.item.name, bi.pharmaceuticalBillItem.stock.itemBatch.id ";

        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", billType);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);

        dueTotal = 0.0;
        doneTotal = 0.0;
        netTotal = 0.0;

    }

    public void createAdjustmentRow(Stock s, BillItem bi, List<BillItem> billItems) {
        PharmacyAdjustmentRow row;
        if (s != null) {
            row = new PharmacyAdjustmentRow(s.getItemBatch().getItem(),
                    s.getItemBatch().getPurcahseRate(),
                    s.getItemBatch().getRetailsaleRate(),
                    s.getStock(),
                    s.getStock(),
                    0,
                    s.getItemBatch().getBatchNo(),
                    s.getItemBatch().getDateOfExpire());
            dueTotal += row.getBefoerVal();
            doneTotal += row.getAfterVal();
            netTotal += row.getAdjusetedVal();
            pharmacyAdjustmentRows.add(row);
        }
        if (bi != null) {
            row = new PharmacyAdjustmentRow(bi.getItem(),
                    bi.getPharmaceuticalBillItem().getStock().getItemBatch().getPurcahseRate(),
                    bi.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate(),
                    bi.getPharmaceuticalBillItem().getStockHistory().getStockQty(),
                    bi.getQty(),
                    bi.getQty() - bi.getPharmaceuticalBillItem().getStockHistory().getStockQty(),
                    bi.getPharmaceuticalBillItem().getStock().getItemBatch().getBatchNo(),
                    bi.getPharmaceuticalBillItem().getStock().getItemBatch().getDateOfExpire());
            dueTotal += row.getBefoerVal();
            doneTotal += row.getAfterVal();
            netTotal += row.getAdjusetedVal();
            pharmacyAdjustmentRows.add(row);
        }
        if (billItems != null) {
            for (BillItem bii : billItems) {
                try {
                    row = new PharmacyAdjustmentRow(bii.getItem(),
                            bii.getPharmaceuticalBillItem().getStock().getItemBatch().getPurcahseRate(),
                            bii.getPharmaceuticalBillItem().getStock().getItemBatch().getRetailsaleRate(),
                            bii.getPharmaceuticalBillItem().getStockHistory().getStockQty(),
                            bii.getQty(),
                            bii.getQty() - bii.getPharmaceuticalBillItem().getStockHistory().getStockQty(),
                            bii.getPharmaceuticalBillItem().getStock().getItemBatch().getBatchNo(),
                            bii.getPharmaceuticalBillItem().getStock().getItemBatch().getDateOfExpire());
                    dueTotal += row.getBefoerVal();
                    doneTotal += row.getAfterVal();
                    netTotal += row.getAdjusetedVal();
                    pharmacyAdjustmentRows.add(row);
                } catch (Exception e) {
                }
            }
        }
    }

    public void createStoreAdjustmentBillItemTable() {
        Date startTime = new Date();
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.StoreAdjustment);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins"
                + " and bi.bill.billType=:bType  "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  (upper(bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Adjustments/Search adjustment bill(/faces/store/store_search_adjustment_bill_item.xhtml)");

    }

    public void createDrawerAdjustmentTable() {
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.DrawerAdjustment);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", BilledBill.class);

        sql = "select bi from Bill bi"
                + " where  type(bi)=:class "
                + " and bi.institution=:ins"
                + " and bi.billType=:bType  "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.insId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyBillItemTableIssue() {
        createBillItemTableBht(BillType.StoreIssue);
    }

    public void createPharmacyBillItemTableBht() {
        Date startTime = new Date();

        createBillItemTableBht(BillType.PharmacyBhtPre);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/BHT Bills/search Inward Bill items(/faces/inward/pharmacy_search_sale_bill_item_bht.xhtml)");
    }

    public void createStoreBillItemTableBht() {
        createBillItemTableBht(BillType.StoreBhtPre);
    }

    public List<BillItem> getBillItem() {
        return billItem;
    }

    public void setBillItem(List<BillItem> billItem) {
        this.billItem = billItem;
    }

    public void createBillItemTableBht(BillType btp) {
        //  searchBillItems = null;
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", btp);
        m.put("ins", getSessionController().getInstitution());
        m.put("class", PreBill.class);

        sql = "select bi from BillItem bi"
                + " where  type(bi.bill)=:class "
                + " and bi.bill.institution=:ins"
                + " and bi.bill.billType=:bType and "
                + " bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(bi.bill.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(bi.netValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :itm )";
            m.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCode() != null && !getSearchKeyword().getCode().trim().equals("")) {
            sql += " and  (upper(bi.item.code) like :cde )";
            m.put("cde", "%" + getSearchKeyword().getCode().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.patientEncounter.bhtNo) like :bht )";
            m.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

    }

    public void createPoRequestedAndApprovedPharmacy() {
        Date startTime = new Date();

        createPoRequestedAndApproved(InstitutionType.Dealer, BillType.PharmacyOrder);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Purchase/PO approval(All Requests)(/faces/pharmacy/pharmacy_purhcase_order_list_to_approve.xhtml)");
    }

    public void createPoRequestedAndApprovedStore() {
        Date startTime = new Date();
        createPoRequestedAndApproved(InstitutionType.StoreDealor, BillType.StoreOrder);

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Purchase/PO approve(All request)(/faces/store/store_purhcase_order_list_to_approve.xhtml)");

    }

    public void createPoRequestedAndApproved(InstitutionType institutionType, BillType bt) {
        bills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.billType= :bTp  ";

        sql += createKeySql(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("insTp", institutionType);
        tmp.put("bTp", bt);
        bills = getBillFacade().findBySQLWithoutCache(sql, tmp, TemporalType.TIMESTAMP, maxResult);

    }

    public void createApprovedPharmacy() {
        Date startTime = new Date();

        createApproved(InstitutionType.Dealer, BillType.PharmacyOrder);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Purchase/PO approval(Approved Requests)(/faces/pharmacy/pharmacy_purhcase_order_list_to_approve.xhtml)");
    }

    public void createApprovedStore() {
        Date startTime = new Date();
        createApproved(InstitutionType.StoreDealor, BillType.StoreOrder);

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Purchase/PO approve(Approved Only)(/faces/store/store_purhcase_order_list_to_approve.xhtml)");
    }

    public void createApproved(InstitutionType institutionType, BillType bt) {
        bills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where"
                + " b.referenceBill.creater is not null "
                + " and b.referenceBill.cancelled=false "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.billType= :bTp  ";

        sql += createKeySql(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("insTp", institutionType);
        tmp.put("bTp", bt);
        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, maxResult);

    }

    public void createNotApprovedPharmacy() {
        Date startTime = new Date();

        createNotApproved(InstitutionType.Dealer, BillType.PharmacyOrder);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Purchase/PO approval(To Approve Requests)(/faces/pharmacy/pharmacy_purhcase_order_list_to_approve.xhtml)");
    }

    public void createNotApprovedStore() {
        Date startTime = new Date();

        createNotApproved(InstitutionType.StoreDealor, BillType.StoreOrder);

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Purchase/PO approve(Not Approved)(/faces/store/store_purhcase_order_list_to_approve.xhtml)");
    }

    public void createNotApproved(InstitutionType institutionType, BillType bt) {
        bills = null;
        String sql = "";
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where"
                + "  b.referenceBill is null  "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false"
                + " and b.billType= :bTp ";

        sql += createKeySql(tmp);
        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("insTp", institutionType);
        tmp.put("bTp", bt);
        List<Bill> lst1 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, maxResult);

        sql = "Select b From BilledBill b where "
                + " b.referenceBill.creater is null "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.billType= :bTp  ";

        sql += createKeySql(tmp);
        sql += " order by b.createdAt desc  ";

        List<Bill> lst2 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, maxResult);

        sql = "Select b From BilledBill b where "
                + " b.referenceBill.creater is not null "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.referenceBill.cancelled=true "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false"
                + " and b.billType= :bTp ";

        sql += createKeySql(tmp);
        sql += " order by b.createdAt desc  ";

        List<Bill> lst3 = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, maxResult);

        lst1.addAll(lst2);
        lst1.addAll(lst3);

        bills = lst1;

    }

    private String createKeySql(HashMap tmp) {
        String sql = "";
        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  (upper(b.toInstitution.name) like :toIns )";
            tmp.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getCreator() != null && !getSearchKeyword().getCreator().trim().equals("")) {
            sql += " and  (upper(b.creater.webUserPerson.name) like :crt )";
            tmp.put("crt", "%" + getSearchKeyword().getCreator().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDepartment() != null && !getSearchKeyword().getDepartment().trim().equals("")) {
            sql += " and  (upper(b.department.name) like :crt )";
            tmp.put("crt", "%" + getSearchKeyword().getDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :reqTotal )";
            tmp.put("reqTotal", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.netTotal) like :appTotal )";
            tmp.put("appTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        return sql;

    }

    private String keysForGrnReturn(HashMap tmp) {
        String sql = "";
        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.invoiceNumber) like :invoice )";
            tmp.put("invoice", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.deptId) like :refNo )";
            tmp.put("refNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.name) like :frmIns )";
            tmp.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.referenceBill.netTotal) like :total )";
            tmp.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            tmp.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        return sql;
    }

    public void createGrnTable() {
        Date startTime = new Date();

        bills = null;
        String sql;
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp "
                + " and b.institution=:ins and"
                + " b.createdAt between :fromDate and :toDate ";

        sql += keysForGrnReturn(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", BillType.PharmacyGrnBill);
        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getReturnBill(b, BillType.PharmacyGrnReturn));
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Purchase/GRN return(Logged Institution Search)(/faces/pharmacy/pharmacy_grn_list_for_return.xhtml)");

    }

    public void createGrnTableStore() {
        Date startTime = new Date();

        bills = null;
        String sql;
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp "
                + " and b.institution=:ins and"
                + " b.createdAt between :fromDate and :toDate ";

        sql += keysForGrnReturn(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", BillType.StoreGrnBill);
        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getReturnBill(b, BillType.StoreGrnReturn));
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Purchase/GRN return(Logged Institution Search)(/faces/pharmacy/item_supplier_prices.xhtml)");

    }

    public void createGrnTableAllIns() {
        Date startTime = new Date();

        bills = null;
        String sql;
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        sql += keysForGrnReturn(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        // tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", BillType.PharmacyGrnBill);
        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getReturnBill(b, BillType.PharmacyGrnReturn));
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Purchase/GRN return(All Search)(/faces/pharmacy/pharmacy_grn_list_for_return.xhtmxl)");

    }

    public void createGrnTableAllInsStore() {
        Date startTime = new Date();

        bills = null;
        String sql;
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b where  b.retired=false and b.billType= :bTp "
                + " and b.createdAt between :fromDate and :toDate ";

        sql += keysForGrnReturn(tmp);

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        // tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", BillType.StoreGrnBill);
        bills = getBillFacade().findBySQL(sql, tmp, TemporalType.TIMESTAMP, 50);

        for (Bill b : bills) {
            b.setListOfBill(getReturnBill(b, BillType.StoreGrnReturn));
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Purchase/GRN return(All Search)(/faces/pharmacy/item_supplier_prices.xhtml)");

    }

    private List<Bill> getReturnBill(Bill b, BillType bt) {
        String sql = "Select b From BilledBill b where b.retired=false and b.creater is not null"
                + " and  b.billType=:btp and "
                + " b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", bt);
        return getBillFacade().findBySQL(sql, hm);
    }

    public void createPoTablePharmacy() {
        Date startTime = new Date();

        createPoTable(InstitutionType.Dealer, BillType.PharmacyOrderApprove, BillType.PharmacyGrnBill);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Purchase/GRN receive(search)(/faces/pharmacy/pharmacy_purchase_order_list_for_recieve.xhtml)");
    }

    public void createPoTableStore() {
        Date startTime = new Date();

        createPoTable(InstitutionType.StoreDealor, BillType.StoreOrderApprove, BillType.StoreGrnBill);

        commonController.printReportDetails(fromDate, toDate, startTime, "Store/Purchase/GRN receieve(/faces/pharmacy/item_supplier_prices.xhtml)");
    }

    public void createPoTable(InstitutionType institutionType, BillType bt, BillType referenceBillType) {
        bills = null;
        String sql;
        HashMap tmp = new HashMap();

        sql = "Select b From BilledBill b "
                + " where  b.retired=false"
                + " and b.billType= :bTp"
                + " and b.toInstitution.institutionType=:insTp "
                + " and  b.referenceBill.institution=:ins "
                + " and  b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            tmp.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  (upper(b.toInstitution.name) like :toIns )";
            tmp.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            tmp.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        tmp.put("toDate", getToDate());
        tmp.put("fromDate", getFromDate());
        tmp.put("insTp", institutionType);
        tmp.put("ins", getSessionController().getInstitution());
        tmp.put("bTp", bt);
        for (Bill b : bills) {
            b.setListOfBill(getGrns(b, referenceBillType));
        }

    }

    private List<Bill> getGrns(Bill b, BillType billType) {
        String sql = "Select b From BilledBill b "
                + " where b.retired=false "
                + " and b.creater is not null"
                + " and b.billType=:btp"
                + " and b.referenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", billType);
        return getBillFacade().findBySQL(sql, hm);
    }

    private List<Bill> getIssudBills(Bill b) {
        String sql = "Select b From Bill b where b.retired=false and b.creater is not null"
                + " and b.billType=:btp and "
                + " b.referenceBill=:ref or b.backwardReferenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyTransferIssue);
        return getBillFacade().findBySQL(sql, hm);
    }

    private List<Bill> getBHTIssudBills(Bill b) {
        String sql = "Select b From Bill b where b.retired=false "
                + " and b.billType=:btp "
                + " and b.referenceBill=:ref ";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyBhtPre);
        return getBillFacade().findBySQL(sql, hm);
    }

    private List<Bill> getIssuedBills(Bill b) {
        String sql = "Select b From Bill b where b.retired=false and b.creater is not null"
                + " and b.billType=:btp and "
                + " b.referenceBill=:ref and b.backwardReferenceBill=:ref";
        HashMap hm = new HashMap();
        hm.put("ref", b);
        hm.put("btp", BillType.PharmacyTransferIssue);
        return getBillFacade().findBySQL(sql, hm);
    }

    public void createDueFeeTable() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where b.retired=false and "
                + " (b.bill.billType=:btp or b.bill.billType=:btpc) "
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 and"
                + "  b.bill.createdAt between :fromDate"
                + " and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.OpdBill);
        temMap.put("btpc", BillType.CollectingCentreBill);

        billFees = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);
        List<BillFee> removeingBillFees = new ArrayList<>();
        for (BillFee bf : billFees) {
            sql = "SELECT bi FROM BillItem bi where bi.retired=false and bi.referanceBillItem.id=" + bf.getBillItem().getId();
            BillItem rbi = getBillItemFacade().findFirstBySQL(sql);

            if (rbi != null) {
                removeingBillFees.add(bf);
            }

        }
        billFees.removeAll(removeingBillFees);
        calTotal();

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/OPD/Payment due search/(/faces/opd_search_professional_payment_due.xhtml or /faces/reportIncome/opd_professional_payment_due.xhtml)");
    }

    public void createDueFeeTableAll() {
        Date sartTime = new Date();

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where b.retired=false and "
                + " (b.bill.billType=:btp or b.bill.billType=:btpc) "
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 and"
                + "  b.bill.createdAt between :fromDate"
                + " and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.OpdBill);
        temMap.put("btpc", BillType.CollectingCentreBill);

        billFees = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        List<BillFee> removeingBillFees = new ArrayList<>();
        for (BillFee bf : billFees) {
            sql = "SELECT bi FROM BillItem bi where bi.retired=false and bi.referanceBillItem.id=" + bf.getBillItem().getId();
            BillItem rbi = getBillItemFacade().findFirstBySQL(sql);

            if (rbi != null) {
                removeingBillFees.add(bf);
            }

        }
        billFees.removeAll(removeingBillFees);
        calTotal();

        commonController.printReportDetails(fromDate, toDate, sartTime, "Doctor Payment Due Report(/faces/inward/inward_professional_payment_due.xhtml)");
    }

    public void createDueFeeTableAndPaidFeeTable() {
        Date startTime = new Date();

        dueTotal = 0.0;
        doneTotal = 0.0;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where b.retired=false and "
                + " b.bill.billType=:btp "
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 and"
                + " b.bill.createdAt between :fromDate"
                + " and :toDate ";

        if (speciality != null) {
            sql += " and b.staff.speciality=:special ";
            temMap.put("special", speciality);
            ////System.out.println(speciality);
        }

        if (staff != null) {
            sql += " and b.staff=:staff ";
            temMap.put("staff", staff);
            ////System.out.println(staff);
        }

        if (item != null) {
            sql += " and b.billItem.item=:item ";
            temMap.put("item", item);
            ////System.out.println(item);
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.OpdBill);

        billFees = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        for (BillFee bf : billFees) {
            dueTotal += bf.getFeeValue();
        }

        temMap.clear();
//        BillFee bf=new BillFee();
//        bf.getBillItem().getCreatedAt();
        sql = "select b.paidForBillFee from BillItem b where b.retired=false and "
                + " b.bill.billType=:btp "
                + " and b.referenceBill.billType=:refType "
                + " and b.paidForBillFee.bill.cancelled=false "
                //                + " and b.feeValue > 0 "
                + " and b.createdAt between :fromDate"
                + " and :toDate ";

//        sql = "Select b FROM BillItem b "
//                + " where b.retired=false "
//                + " and b.bill.billType=:bType "
//                + " and b.referenceBill.billType=:refType "
//                + " and b.createdAt between :fromDate and :toDate ";
        if (speciality != null) {
            sql += " and b.paidForBillFee.staff.speciality=:special ";
            temMap.put("special", speciality);
            ////System.out.println(speciality);
        }

        if (staff != null) {
            sql += " and b.paidForBillFee.staff=:staff ";
            temMap.put("staff", staff);
            ////System.out.println(staff);
        }

        if (item != null) {
            sql += " and b.paidForBillFee.billItem.item=:item ";
            temMap.put("item", item);
            ////System.out.println(item);
        }

        sql += "  order by b.paidForBillFee.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.PaymentBill);
        temMap.put("refType", BillType.OpdBill);

        billFeesDone = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        for (BillFee bf2 : billFeesDone) {
            doneTotal += bf2.getFeeValue();
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "OPD doctor payment and due(/opd_search_professional_payment_due_1.xhtml)");
    }

    double totalPaying;

    public void fillDocPayingBillFee() {
        Date startTime = new Date();

        String sql;
        Map m = new HashMap();

        sql = "select b from BillItem b where b.retired=false "
                + " and b.bill.billType=:btp "
                + " and b.referenceBill.billType=:refType "
                //                + " and b.paidForBillFee.bill.cancelled=false "
                + " and b.createdAt between :fromDate and :toDate ";

        if (speciality != null) {
            sql += " and b.paidForBillFee.staff.speciality=:s ";
            m.put("s", speciality);
        }

        if (currentStaff != null) {
            sql += " and b.paidForBillFee.staff=:cs";
            m.put("cs", currentStaff);
        }

        sql += " order by b.bill.insId ";

        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        m.put("btp", BillType.PaymentBill);
        m.put("refType", BillType.OpdBill);

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);

        totalPaying = 0.0;
        if (billItems == null) {
            return;
        }
        for (BillItem dFee : billItems) {
            totalPaying += dFee.getPaidForBillFee().getFeeValue();
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "OPD doctor payments(by bill item)(/reportCashier/report_doctor_payment_opd.xhtml)");

    }

    public void fillDocPayingBill() {

        String sql;
        Map m = new HashMap();

        sql = "select distinct(b.bill) from BillItem b where b.retired=false "
                + " and b.bill.billType=:btp "
                + " and b.referenceBill.billType=:refType "
                //                + " and b.paidForBillFee.bill.cancelled=false "
                + " and b.createdAt between :fromDate and :toDate ";

        if (speciality != null) {
            sql += " and b.paidForBillFee.staff.speciality=:s ";
            m.put("s", speciality);
        }

        if (currentStaff != null) {
            sql += " and b.paidForBillFee.staff=:cs";
            m.put("cs", currentStaff);
        }

        sql += " order by b.bill.insId ";

        m.put("toDate", getToDate());
        m.put("fromDate", getFromDate());
        m.put("btp", BillType.PaymentBill);
        m.put("refType", BillType.OpdBill);

//        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);
        bills = getBillFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);

        totalPaying = 0.0;
        if (bills == null) {
            return;
        }
        for (Bill b : bills) {
            totalPaying += b.getNetTotal();
        }

    }

    public void createDueFeeTableInward() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where "
                + " b.retired=false "
                + " and (b.bill.billType=:btp or b.bill.billType=:btp2 )"
                + " and b.bill.cancelled=false "
                //Starting of newly added code 
                //                + " and b.bill.refunded=false "
                //Ending of newly added code 
                + " and (b.feeValue - b.paidValue) > 0"
                //                + " and  b.bill.billTime between :fromDate and :toDate ";
                + " and  b.bill.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.InwardBill);
        temMap.put("btp2", BillType.InwardProfessional);

        billFees = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);
        List<BillFee> removeingBillFees = new ArrayList<>();
        for (BillFee bf : billFees) {
            temMap = new HashMap();
            temMap.put("btp", BillType.InwardBill);
            sql = "SELECT bi FROM BillItem bi where bi.retired=false "
                    + " and bi.bill.cancelled=false "
                    + " and bi.bill.billType=:btp "
                    //                    + " and bi.bill.toStaff=:stf "
                    + " and bi.referanceBillItem.id=" + bf.getBillItem().getId();
            BillItem rbi = getBillItemFacade().findFirstBySQL(sql, temMap);

            if (rbi != null) {
                removeingBillFees.add(bf);
            }
        }
        billFees.removeAll(removeingBillFees);
        calTotal();

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Inward/Payment due search/(/faces/inward/inward_search_professional_payment_due.xhtml)");
    }

    public void createDueFeeTableInwardAll() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("billClass", BilledBill.class);
        temMap.put("btp", BillType.InwardBill);
        temMap.put("btp2", BillType.InwardProfessional);

        sql = "select b from BillFee b where "
                + " b.retired=false "
                + " and (b.bill.billType=:btp or b.bill.billType=:btp2 )"
                + " and b.bill.cancelled=false "
                + " and type(b.bill)=:billClass "
                //Starting of newly added code 
                //                + " and b.bill.refunded=false "
                //Ending of newly added code 
                + " and (b.feeValue - b.paidValue) > 0"
                //                + " and  b.bill.billTime between :fromDate and :toDate ";
                + " and  b.bill.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += "  order by b.staff.id    ";

        ////System.out.println("temMap = " + temMap);
        ////System.out.println("sql = " + sql);
        billFees = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        List<BillFee> removeingBillFees = new ArrayList<>();
        for (BillFee bf : billFees) {
            temMap = new HashMap();
            temMap.put("btp", BillType.InwardBill);
            sql = "SELECT bi FROM BillItem bi where bi.retired=false "
                    + " and bi.bill.cancelled=false "
                    + " and bi.bill.billType=:btp "
                    //                    + " and bi.bill.toStaff=:stf "
                    + " and bi.referanceBillItem.id=" + bf.getBillItem().getId();
            BillItem rbi = getBillItemFacade().findFirstBySQL(sql, temMap);

            if (rbi != null) {
                removeingBillFees.add(bf);
            }
        }
        billFees.removeAll(removeingBillFees);
        calTotal();

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Inward/Payment due search all/(/faces/inward/inward_search_professional_payment_due.xhtml)");
    }

    public void createDueFeeTableInwardAllWithCancelled() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where "
                + " b.retired=false "
                + " and (b.bill.billType=:btp or b.bill.billType=:btp2 )"
                + " and (b.feeValue - b.paidValue) > 0"
                + " and  b.bill.billDate between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.InwardBill);
        temMap.put("btp2", BillType.InwardProfessional);
        ////System.out.println("temMap = " + temMap);
        ////System.out.println("sql = " + sql);

        billFees = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        calTotal();
    }

    double total;

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    private void calTotal() {
        total = 0;
        if (billFees == null) {
            return;
        }

        for (BillFee billFee : billFees) {
            total += billFee.getFeeValue();
        }
    }

    private void calTotalBillItem() {
        total = 0;
        if (billItems == null) {
            return;
        }

        for (BillItem billFee : billItems) {
            total += billFee.getNetValue();
        }
    }

    public void createDueFeeReportInward() {

        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where "
                + " b.retired=false "
                + " and (b.bill.billType=:btp or b.bill.billType=:btp2 )"
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0"
                + " and  b.bill.billDate between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.billItem.item.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPaymentMethod() != null) {
            sql += " and  b.bill.patientEncounter.paymentMethod =:payme";
            temMap.put("payme", getSearchKeyword().getPaymentMethod());
        }

        if (getSearchKeyword().getIns() != null) {
            sql += " and  b.bill.patientEncounter.creditCompany=:is";
            temMap.put("is", getSearchKeyword().getIns());
        }

        sql += "  order by b.staff.id    ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.InwardBill);
        temMap.put("btp2", BillType.InwardProfessional);

        billFees = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        commonController.printReportDetails(fromDate, toDate, startTime, "Doctor payment due report inward(/faces/inward/inward_report_professional_payment_due.xhtml)");
    }

    public void createPaymentTable() {
        Date startTime = new Date();

        billItems = null;
        HashMap temMap = new HashMap();
        String sql = "Select b FROM BillItem b "
                + " where b.retired=false "
                + " and b.bill.billType=:bType "
                + " and b.referenceBill.billType=:refType "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPaymentMethod() != null) {
            sql += " and  b.paidForBillFee.bill.paymentMethod=:pm ";
            temMap.put("pm", getSearchKeyword().getPaymentMethod());
        }

        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :insId )";
            temMap.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.billItem.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bType", BillType.PaymentBill);
        temMap.put("refType", BillType.OpdBill);

        billItems = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/OPD/Payment done search(search)(/faces/store/store_report_transfer_receive_bill_item.xhtml)");

    }

    public void createPaymentTableAll() {
        billItems = null;
        HashMap temMap = new HashMap();
        String sql = "Select b FROM BillItem b "
                + " where b.retired=false "
                + " and b.bill.billType=:bType "
                + " and b.referenceBill.billType=:refType "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPaymentMethod() != null) {
            sql += " and  b.paidForBillFee.bill.paymentMethod=:pm ";
            temMap.put("pm", getSearchKeyword().getPaymentMethod());
        }

        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :insId )";
            temMap.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.billItem.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bType", BillType.PaymentBill);
        temMap.put("refType", BillType.OpdBill);

        billItems = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createProfessionalPaymentTableInward() {
        Date startTime = new Date();

        billItems = null;
        HashMap temMap = new HashMap();
        temMap.put("bclass", BilledBill.class);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bType", BillType.PaymentBill);
        temMap.put("refType", BillType.InwardBill);
        temMap.put("refType2", BillType.InwardProfessional);
        String sql = "Select b FROM BillItem b "
                + " where b.retired=false "
                //                + " and b.bill.cancelled=false "
                + " and type(b.bill)=:bclass"
                + " and b.bill.billType=:bType "
                + " and (b.referenceBill.billType=:refType "
                + " or b.referenceBill.billType=:refType2) "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :insId )";
            temMap.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.billItem.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bType", BillType.PaymentBill);
        temMap.put("refType", BillType.InwardBill);
        temMap.put("refType2", BillType.InwardProfessional);

        billItems = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Inward/Payment done search/(/faces/inward/inward_search_professional_payment_done.xhtml)");

    }

    public void createProfessionalPaymentTableInwardAll() {
        billItems = null;
        HashMap temMap = new HashMap();
//        temMap.put("bclass", BilledBill.class);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("bType", BillType.PaymentBill);
        temMap.put("refType", BillType.InwardBill);
        temMap.put("refType2", BillType.InwardProfessional);
        String sql = "Select b FROM BillItem b "
                + " where b.retired=false "
                + " and b.bill.billType=:bType "
                //                + " and b.bill.cancelled=false "
                //                + " and type(b.bill)=:bclass"
                + " and (b.referenceBill.billType=:refType "
                + " or b.referenceBill.billType=:refType2) "
                + " and b.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :insId )";
            temMap.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.staff.speciality.name) like :special )";
            temMap.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.staff.person.name) like :staff )";
            temMap.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.billItem.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.paidForBillFee.feeValue) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        billItems = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        calTotalBillItem();
    }

    public void createBillItemTableByKeyword() {
        Date startTime = new Date();
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.OpdBill);
        m.put("ins", getSessionController().getInstitution());

        sql = "select bi from BillItem bi where bi.bill.institution=:ins "
                + " and bi.bill.billType=:bType "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (searchKeyword.getPatientName() != null && !searchKeyword.getPatientName().trim().equals("")) {
            sql += " and  (upper(bi.bill.patient.person.name) like :patientName )";
            m.put("patientName", "%" + searchKeyword.getPatientName().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getPatientPhone() != null && !searchKeyword.getPatientPhone().trim().equals("")) {
            sql += " and  (upper(bi.bill.patient.person.phone) like :patientPhone )";
            m.put("patientPhone", "%" + searchKeyword.getPatientPhone().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getBillNo() != null && !searchKeyword.getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.insId) like :billNo )";
            m.put("billNo", "%" + searchKeyword.getBillNo().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getItemName() != null && !searchKeyword.getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :itemName )";
            m.put("itemName", "%" + searchKeyword.getItemName().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getToInstitution() != null && !searchKeyword.getToInstitution().trim().equals("")) {
            sql += " and  (upper(bi.bill.toInstitution.name) like :toIns )";
            m.put("toIns", "%" + searchKeyword.getToInstitution().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";
        //System.err.println("Sql " + sql);

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

        checkLabReportsApprovedBillItem(billItems);

        //   searchBillItems = new LazyBillItem(tmp);
        commonController.printReportDetails(fromDate, toDate, startTime, "OPD billIltem search(/opd_search_billitem_own.xhtml)");
    }

    public String toCreateBillItemListForCreditCompany() {
        billItems = new ArrayList<>();
        return "/reportLab/credit_company_bill_item_list";
    }

    public void createBillItemListForCreditCompany() {
        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.OpdBill);
        m.put("ins", getCreditCompany());
        sql = "select bi from BillItem bi where bi.bill.creditCompany=:ins "
                + " and bi.bill.billType=:bType "
                + " and bi.createdAt between :fromDate and :toDate ";
        sql += " order by bi.id ";
        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);
//        checkLabReportsApprovedBillItem(billItems);
//        commonController.printReportDetails(fromDate, toDate, startTime, "Lab/report.search/search(/faces/lab/search_for_reporting_ondemand.xhtml)");
    }

    public void createBillItemTableByKeywordAll() {
        Date startTime = new Date();

        String sql;
        Map m = new HashMap();
        m.put("toDate", toDate);
        m.put("fromDate", fromDate);
        m.put("bType", BillType.OpdBill);
        m.put("ins", getSessionController().getInstitution());

        sql = "select bi from BillItem bi where bi.bill.institution=:ins "
                + " and bi.bill.billType=:bType "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (searchKeyword.getPatientName() != null && !searchKeyword.getPatientName().trim().equals("")) {
            sql += " and  (upper(bi.bill.patient.person.name) like :patientName )";
            m.put("patientName", "%" + searchKeyword.getPatientName().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getPatientPhone() != null && !searchKeyword.getPatientPhone().trim().equals("")) {
            sql += " and  (upper(bi.bill.patient.person.phone) like :patientPhone )";
            m.put("patientPhone", "%" + searchKeyword.getPatientPhone().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getBillNo() != null && !searchKeyword.getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.bill.insId) like :billNo )";
            m.put("billNo", "%" + searchKeyword.getBillNo().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getItemName() != null && !searchKeyword.getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :itemName )";
            m.put("itemName", "%" + searchKeyword.getItemName().trim().toUpperCase() + "%");
        }

        if (searchKeyword.getToInstitution() != null && !searchKeyword.getToInstitution().trim().equals("")) {
            sql += " and  (upper(bi.bill.toInstitution.name) like :toIns )";
            m.put("toIns", "%" + searchKeyword.getToInstitution().trim().toUpperCase() + "%");
        }

        sql += " order by bi.id desc  ";
        //System.err.println("Sql " + sql);

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);

        checkLabReportsApprovedBillItem(billItems);

        //   searchBillItems = new LazyBillItem(tmp);
        commonController.printReportDetails(fromDate, toDate, startTime, "Lab/report.search/search(/faces/lab/search_for_reporting_ondemand.xhtml)");
    }

    public void createPatientInvestigationsTable() {
        Date startTime = new Date();

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  ";

//        String sql = "select pi from PatientInvestigation pi where "
//                + " pi.billItem.bill.createdAt between :fromDate and :toDate  ";
        Map temMap = new HashMap();

//        if(webUserController.hasPrivilege("LabSearchBillLoggedInstitution")){
//            //System.out.println("inside ins");
//            sql+="and b.institution =:ins ";
//            temMap.put("ins", getSessionController().getInstitution());
//        }
        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(p.name) like :patientName )";
//            sql += " and  (upper(pi.billItem.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by pi.id desc  ";
//    

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        commonController.printReportDetails(fromDate, toDate, startTime, "Lab/report.search/search(/faces/lab/search_for_reporting_ondemand.xhtml)");
    }

    public void createPatientInvestigationsTableByLoggedInstitution() {

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  "
                + " and b.institution =:ins ";

//        String sql = "select pi from PatientInvestigation pi where "
//                + " pi.billItem.bill.createdAt between :fromDate and :toDate  ";
        Map temMap = new HashMap();
        temMap.put("ins", getSessionController().getInstitution());

//        if(webUserController.hasPrivilege("LabSearchBillLoggedInstitution")){
//            //System.out.println("inside ins");
//            sql+="and b.institution =:ins ";
//            temMap.put("ins", getSessionController().getInstitution());
//        }
        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(p.name) like :patientName )";
//            sql += " and  (upper(pi.billItem.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by pi.id desc  ";
//    

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        //System.err.println("Sql " + sql);
    }

    public void createPatientInvestigationsTableByLoggedDepartment() {

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  "
                + " and b.department =:dep ";

//        String sql = "select pi from PatientInvestigation pi where "
//                + " pi.billItem.bill.createdAt between :fromDate and :toDate  ";
        Map temMap = new HashMap();
        temMap.put("dep", getSessionController().getDepartment());

//        if(webUserController.hasPrivilege("LabSearchBillLoggedInstitution")){
//            //System.out.println("inside ins");
//            sql+="and b.institution =:ins ";
//            temMap.put("ins", getSessionController().getInstitution());
//        }
        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(p.name) like :patientName )";
//            sql += " and  (upper(pi.billItem.bill.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by pi.id desc  ";
//    

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        //System.err.println("Sql " + sql);
    }

    public void searchPatientInvestigations() {
        Date startTime = new Date();

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  ";
        Map temMap = new HashMap();
        sql += " order by pi.id ";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        commonController.printReportDetails(fromDate, toDate, startTime, "Lab/reporting(/faces/lab/search_reports.xhtml)");
    }

    public String fillUserPatientReportMobile() {
        return fillUserPatientReport(false);
    }

    public String fillUserPatientReportWeb() {
        return fillUserPatientReport(true);
    }

    private String fillUserPatientReport(boolean web) {
        String jpql;
        Map m = new HashMap();
        m.put("pn", getSessionController().getPhoneNo());
        m.put("bn", getSessionController().getBillNo());
        //System.out.println("getSessionController().getPhoneNo() = " + getSessionController().getPhoneNo());
        if (getSessionController().getPhoneNo() == null || getSessionController().getPhoneNo().equals("")) {
            JsfUtil.addErrorMessage("Please Enter Phone Number");
            return "";
        }
        if (getSessionController().getBillNo() == null || getSessionController().getBillNo().equals("")) {
            JsfUtil.addErrorMessage("Please Enter Bill Number");
            return "";
        }

        jpql = " select pr from PatientInvestigation pr where pr.retired=false and "
                + " upper(pr.billItem.bill.patient.person.phone)=:pn and "
                + " (upper(pr.billItem.bill.insId)=:bn or upper(pr.billItem.bill.deptId)=:bn) "
                + " order by pr.id desc ";
        if (web) {
            m.put("pn", getSessionController().getPhoneNo());
        } else {
            m.put("pn", getSessionController().getPhoneNo().substring(0, 3) + "-" + getSessionController().getPhoneNo().substring(3));
        }
        m.put("bn", getSessionController().getBillNo());

        return "";
    }

    public void createPatientInvestigationsTableSingle() {
        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient p where "
                + " p=:pt ";
        Map temMap = new HashMap();
        sql += " order by pi.id desc  ";
        temMap.put("pt", getTransferController().getPatient());
//        //////System.out.println("temMap = " + temMap);
//        //////System.out.println("sql = " + sql);
//        //////System.out.println("patientInvestigations.size() = " + patientInvestigations.size());
    }

    public void createPatientInvestigationsTableAll() {
        Date startTime = new Date();

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  ";

        Map temMap = new HashMap();

//        if(webUserController.hasPrivilege("LabSearchBillLoggedInstitution")){
//            //System.out.println("inside ins");
//            sql+="and b.institution =:ins ";
//            temMap.put("ins", getSessionController().getInstitution());
//        }
        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by pi.id desc  ";
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        //System.err.println("Sql " + sql);
        //System.err.println("Sql " + sql);
        //System.err.println("Sql " + sql);
        commonController.printReportDetails(fromDate, toDate, startTime, "Lab/report.search/search all(/faces/lab/search_for_reporting_ondemand.xhtml)");

    }

    public void searchApprovedReportsWithCancelledOrReturnedBills() {
        String sql = "select pr "
                + " from PatientReport pr"
                + " join pr.patientInvestigation pi "
                + " join pi.billItem.bill b "
                + " where "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.cancelled=:c "
                + " and pr.approved=:a "
                + " order by b.id";

        Map temMap = new HashMap();
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("c", true);
        temMap.put("a", true);
    }

    public void createPatientInvestigationsTableAllByLoggedInstitution() {

        String sql = "select pi from PatientInvestigation pi join pi.investigation  "
                + " i join pi.billItem.bill b join b.patient.person p where "
                + " b.createdAt between :fromDate and :toDate  "
                + " and b.institution =:ins ";

        Map temMap = new HashMap();
        temMap.put("ins", getSessionController().getInstitution());

//        if(webUserController.hasPrivilege("LabSearchBillLoggedInstitution")){
//            //System.out.println("inside ins");
//            sql+="and b.institution =:ins ";
//            temMap.put("ins", getSessionController().getInstitution());
//        }
        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(p.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(p.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(i.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by pi.id desc  ";
//    

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        //System.err.println("Sql " + sql);
    }

    public void createPreBillsForReturn() {
        Date startTime = new Date();

        createPreBillsForReturn(BillType.PharmacyPre, BillType.PharmacySale);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Sale Bills/Return - item only(/faces/pharmacy/pharmacy_search_pre_bill_for_return_item_only.xhtml or /faces/pharmacy/pharmacy_search_pre_bill_for_return_item_and_cash.xhtml)");
    }

    public void createWholePreBillsForReturn() {
        createPreBillsForReturn(BillType.PharmacyWholesalePre, BillType.PharmacyWholeSale);
    }

    public void createPreBillsForReturn(BillType billType, BillType refBillType) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b where b.billType = :billType and "
                + " b.institution=:ins and (b.billedBill is null) and "
                + " b.referenceBill.billType=:refBillType "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false "
                // for remove cancel bills
                + " and b.referenceBill.cancelled=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("billType", billType);
        temMap.put("refBillType", refBillType);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);
        temMap.put("ins", getSessionController().getInstitution());

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public BillBeanController getBillBean() {
        return billBean;
    }

    public void setBillBean(BillBeanController billBean) {
        this.billBean = billBean;
    }

    public void createPreBillsNotPaid() {
        Date startTime = new Date();
        Date fromDate = null;
        Date toDate = null;

        bills = getBillBean().billsForTheDayNotPaid(BillType.PharmacyPre, getSessionController().getDepartment());

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Sale Bills/Add to stock/refresh(/faces/pharmacy/pharmacy_search_pre_bill_not_paid.xhtml)");

    }

    public void createWholePreBillsNotPaid() {

        bills = getBillBean().billsForTheDayNotPaid(BillType.PharmacyWholesalePre, getSessionController().getDepartment());

    }

    public void addToStock() {
        Date startTime = new Date();
        Date fromDate = null;
        Date toDate = null;

        for (Bill b : getSelectedBills()) {
            b = getBillFacade().find(b.getId());
            if (b.getReferenceBill() != null) {
                JsfUtil.addErrorMessage("This Bill " + b.getDeptId() + " alrady Paid can't add to stock.");
                continue;
            }
            if (b.checkActiveCashPreBill()) {
                continue;
            }

            Bill prebill = getPharmacyBean().reAddToStock(b, getSessionController().getLoggedUser(),
                    getSessionController().getDepartment(), BillNumberSuffix.PRECAN);

            if (prebill != null) {
                b.setCancelled(true);
                b.setCancelledBill(prebill);
                getBillFacade().edit(b);
            }
        }

        createPreBillsNotPaid();

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Sale Bills/Add to stock(/faces/pharmacy/pharmacy_search_pre_bill_not_paid.xhtml)");

    }

    public void cancelIssueToUnitBills() {
        if (cancellingIssueBill == null) {
            JsfUtil.addErrorMessage("Select a bill to cancel");
            return;
        }
        Bill b = cancellingIssueBill;

        if (b.isCancelled() || b.isRefunded()) {
            JsfUtil.addErrorMessage("Can not cancel already cancelled or returned bills");
            return;
        }
        if (b instanceof PreBill) {
            Bill prebill = getPharmacyBean().readdStockForIssueBills((PreBill) b, getSessionController().getLoggedUser(),
                    getSessionController().getDepartment(), BillNumberSuffix.DIC);
            b.setCancelled(true);
            b.setCancelledBill(prebill);
            getBillFacade().edit(b);
            JsfUtil.addSuccessMessage("Cancelled");
        } else {
            JsfUtil.addErrorMessage("Not an Issue Bill. Can not cancell");
        }

    }

    private String createPharmacyPayKeyword(Map temMap) {
        String sql = "";
        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.deptId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        return sql;

    }

    public void createOpdBathcBillPreTable() {
        Date startTime = new Date();
        aceptPaymentBills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins"
                //                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";
//                + " and b.deptId is not null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.OpdBathcBillPre);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        aceptPaymentBills = getBillFacade().findBySQLWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);

        commonController.printReportDetails(fromDate, toDate, startTime, "OPD bill search to pay/Search All(/opd_search_pre_batch_bill.xhtml)");
    }

    public void createOpdBathcBillPreTablePaidOnly() {
        Date startTime = new Date();
        aceptPaymentBills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins"
                + " and b.referenceBill.balance=0 "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";
//                + " and b.deptId is not null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.OpdBathcBillPre);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        aceptPaymentBills = getBillFacade().findBySQLWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);

        commonController.printReportDetails(fromDate, toDate, startTime, "OPD bill search to pay/Search Paid only(/opd_search_pre_batch_bill.xhtml)");
    }

    public void createOpdBathcBillPreTableNotPaidOly() {
        Date startTime = new Date();
        aceptPaymentBills = new ArrayList<>();
        String sql;
        Map temMap = new HashMap();

        List<Bill> abs = new ArrayList<>();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins"
                //                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";
//                + " and b.deptId is not null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.OpdBathcBillPre);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        abs = getBillFacade().findBySQLWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 50);

        List<Bill> pbs = new ArrayList<>();
        Map temMap2 = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins"
                + " and b.referenceBill.balance=0 "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";
//                + " and b.deptId is not null ";

        sql += createPharmacyPayKeyword(temMap2);
        sql += " order by b.createdAt desc  ";
//    
        temMap2.put("billType", BillType.OpdBathcBillPre);
        temMap2.put("toDate", getToDate());
        temMap2.put("fromDate", getFromDate());
        temMap2.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        pbs = getBillFacade().findBySQLWithoutCache(sql, temMap2, TemporalType.TIMESTAMP, 50);

        abs.removeAll(pbs);
        aceptPaymentBills.addAll(abs);

        commonController.printReportDetails(fromDate, toDate, startTime, "OPD bill search to pay/Search not Paid only(/opd_search_pre_batch_bill.xhtml)");
    }

    public void createOpdPreTable() {
        createPreTable(BillType.OpdPreBill);
    }

    public void createOpdPreTableNotPaid() {
        createPreTableNotPaid(BillType.OpdPreBill);
    }

    public void createOpdPreTablePaid() {
        createPreTablePaid(BillType.OpdPreBill);
    }

    public void createPharmacyPreTable() {
        Date startTime = new Date();
        createPreTable(BillType.PharmacyPre);
        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy Bill Search to Pay(Search All :/pharmacy/pharmacy_search_pre_bill.xhtml)");
    }

    public void createPharmacyPreTableNotPaid() {
        Date startTime = new Date();
        createPreTableNotPaid(BillType.PharmacyPre);
        commonController.printReportDetails(fromDate, toDate, startTime, "PharmacyPreTableNotPaid(/pharmacy/pharmacy_search_pre_bill.xhtml)");
    }

    public void createPharmacyPreTablePaid() {
        Date startTime = new Date();
        createPreTablePaid(BillType.PharmacyPre);
        commonController.printReportDetails(fromDate, toDate, startTime, "PharmacyPreTablePaid(/pharmacy/pharmacy_search_pre_bill.xhtml)");
    }

    public void createPreTable(BillType bt) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins"
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false "
                + " and b.deptId is not null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", bt);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQLWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);

    }

    public void createPreTableNotPaid(BillType bt) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false "
                + " and b.deptId is not null "
                + " and b.cancelled=false"
                + " and b.referenceBill is null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", bt);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQLWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);

    }

    public void createPreTablePaid(BillType bt) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from PreBill b "
                + " where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false "
                + " and b.deptId is not null "
                + " and b.cancelled=false"
                + " and b.referenceBill is not null ";

        sql += createPharmacyPayKeyword(temMap);
        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", bt);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQLWithoutCache(sql, temMap, TemporalType.TIMESTAMP, 25);

    }

    public void createGrnPaymentTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b"
                + " where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.GrnPayment);
        temMap.put("insTp", InstitutionType.Dealer);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createGrnPaymentTableStore() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b"
                + " where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.GrnPayment);
        temMap.put("insTp", InstitutionType.StoreDealor);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createPharmacyPayment() {
        Date startTime = new Date();

        InstitutionType[] institutionTypes = {InstitutionType.Dealer};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPayment);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Dealer Payments/GRN Payment done search(Search Pharmacy Payment)(/faces/dealorPayment/search_dealor_payment.xhtml)");

    }

    public void createStorePayment() {
        Date startTime = new Date();

        InstitutionType[] institutionTypes = {InstitutionType.StoreDealor};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPayment);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Dealer Payments/GRN Payment done search(Search Store Payment)(/faces/dealorPayment/search_dealor_payment.xhtml)");
    }

    public void createStorePaharmacyPayment() {
        Date startTime = new Date();

        InstitutionType[] institutionTypes = {InstitutionType.Dealer, InstitutionType.StoreDealor};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPayment);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Dealer Payments/GRN Payment done search(Search All Payment)(/faces/dealorPayment/search_dealor_payment.xhtml)");
    }

    public void createPharmacyPaymentPre() {
        Date startTime = new Date();

        InstitutionType[] institutionTypes = {InstitutionType.Dealer};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPaymentPre);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Dealer Payments/GRN Payment approval(Search Pharmacy Payment)(/faces/dealorPayment/search_dealor_payment_pre.xhtml)");
    }

    public void createStorePaymentPre() {
        Date startTime = new Date();

        InstitutionType[] institutionTypes = {InstitutionType.StoreDealor};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPaymentPre);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Dealer Payments/GRN Payment approval(Search Store Payment)(/faces/dealorPayment/search_dealor_payment_pre.xhtml)");
    }

    public void createStorePaharmacyPaymentPre() {
        Date startTime = new Date();

        InstitutionType[] institutionTypes = {InstitutionType.Dealer, InstitutionType.StoreDealor};
        createGrnPaymentTable(Arrays.asList(institutionTypes), BillType.GrnPaymentPre);

        commonController.printReportDetails(fromDate, toDate, startTime, "Pharmacy/Dealer Payments/GRN Payment approval(Search All Payment)(/faces/dealorPayment/search_dealor_payment_pre.xhtml)");
    }

    private void createGrnPaymentTable(List<InstitutionType> institutionTypes, BillType billType) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b "
                + " where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.toInstitution.institutionType in :insTp "
                + " and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", billType);
        temMap.put("insTp", institutionTypes);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        //  temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createGrnPaymentTableAllStore() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b "
                + " where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.toInstitution.institutionType=:insTp "
                + " and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.GrnPayment);
        temMap.put("insTp", InstitutionType.StoreDealor);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        //  temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    @Inject
    WebUserController webUserController;

    public void createOpdBillSearch() {
        Date startTime = new Date();
        createTableByKeyword(BillType.OpdBill);
        checkLabReportsApproved(bills);

        commonController.printReportDetails(fromDate, toDate, startTime, "OPD Bill Search(/opd_search_bill_own.xhtml)");
    }

    public void listOpdBills() {
        Date startTime = new Date();
        createTableByKeyword(BillType.OpdBill);
        checkLabReportsApproved(bills);

        commonController.printReportDetails(fromDate, toDate, startTime, "OPD Bill Search(/opd_search_bill_own.xhtml)");
    }

    public void createCollectingCentreBillSearch() {
        Date startTime = new Date();
        createTableByKeyword(BillType.CollectingCentreBill);
        checkLabReportsApproved(bills);
        commonController.printReportDetails(fromDate, toDate, startTime, "Collecting Center Bill Search(/opd_search_pre_batch_bill.xhtml)");
    }

    public void listOpdBilledBills() {
        listBills(BillType.OpdBill, BilledBill.class, false, false, null, null, null, null, null, null);
    }

    public void listBills(BillType billType, Class billClass, Boolean onlyCancelledBills, Boolean onlyReturnedBills,
            Institution fromInstitution, Department fromDepartment,
            Institution toInstitution, Department toDepartment,
            Doctor referredDoctor, Institution referredInstitution) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (billClass != null) {
            sql += " and type(b.bill)=:class ";
            temMap.put("class", billClass);
        }

        if (onlyCancelledBills == true) {
            sql += " and b.cancelled=:cancelled ";
            temMap.put("cancelled", true);
        }
        if (onlyReturnedBills == true) {
            sql += " and b.refunded=:refunded ";
            temMap.put("refunded", true);
        }
        if (fromInstitution != null) {
            sql += " and b.fromInstitution=:fromIns ";
            temMap.put("fromIns", fromInstitution);
        }
        if (fromDepartment != null) {
            sql += " and b.fromDepartment=:fromDepartment ";
            temMap.put("fromDepartment", fromDepartment);
        }
        if (toInstitution != null) {
            sql += " and b.toInstitution=:toIns ";
            temMap.put("toIns", toInstitution);
        }
        if (toDepartment != null) {
            sql += " and b.toDepartment=:toDepartment ";
            temMap.put("toDepartment", toDepartment);
        }
        if (referredDoctor != null) {
            sql += " and b.referredBy=:referredDoctor ";
            temMap.put("fromIns", referredDoctor);
        }
        if (referredInstitution != null) {
            sql += " and b.referredByInstitution=:referredInstitution ";
            temMap.put("fromDepartment", referredInstitution);
        }

        /**
         *
         *
         *
         *
         * temp.setStaff(staff); temp.setToStaff(toStaff);
         * temp.setReferredBy(referredBy); temp.setReferralNumber(referralId);
         * temp.setReferredByInstitution(referredByInstitution);
         * temp.setCreditCompany(creditCompany);
         * temp.setCollectingCentre(collectingCentre);
         *
         */
        sql += " order by b.createdAt desc  ";

        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void fillOpdClients() {
        fillClients(BillType.OpdBill);
    }

    public void fillClients(BillType billType) {
        patients = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select distinct(b.patient) from BilledBill b where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (fromDepartment != null) {
            sql += " and b.fromDepartment=:fdep ";
            temMap.put("fdep", fromDepartment);
        }
        if (toDepartment != null) {
            sql += " and b.toDepartment=:tdep ";
            temMap.put("tdep", toDepartment);
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        //System.err.println("Sql " + sql);
        patients = getPatientFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    @EJB
    private PatientFacade patientFacade;

    public void createTableByKeyword(BillType billType) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (!webUserController.hasPrivilege("AdminFilterWithoutDepartment")) {
            if (billType != BillType.CollectingCentreBill) {
                sql += " and b.institution=:ins ";
                temMap.put("ins", getSessionController().getInstitution());
            }

        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  ((upper(b.insId) like :billNo )or(upper(b.deptId) like :billNo ))";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public String viewOPD(Bill b) {
        if (b.getBillType() == BillType.OpdBill) {
            return "/bill_reprint";
        } else {
            JsfUtil.addErrorMessage("Please Search Again and View Bill");
            bills = new ArrayList<>();
            return "";
        }
    }

    public void createTableCashIn() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false"
                + " and b.creater=:w ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPersonName() != null && !getSearchKeyword().getPersonName().trim().equals("")) {
            sql += " and  (upper(b.fromWebUser.webUserPerson.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPersonName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.CashIn);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("w", getSessionController().getLoggedUser());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createTableCashOut() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false"
                + " and b.creater=:w ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPersonName() != null && !getSearchKeyword().getPersonName().trim().equals("")) {
            sql += " and  (upper(b.toWebUser.webUserPerson.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPersonName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.CashOut);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("w", getSessionController().getLoggedUser());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createSearchBill() {
        if (getSearchKeyword().getInsId() == null && getSearchKeyword().getDeptId() == null
                && getSearchKeyword().getBhtNo() == null && getSearchKeyword().getRefBillNo() == null) {
            JsfUtil.addErrorMessage("Enter BHT No or Bill No");
            return;
        }
        bills = null;
        String sql;
        Map m = new HashMap();

        sql = "select b from Bill b where "
                + " b.id is not null ";

        if (!getSearchKeyword().getInsId().isEmpty()) {
            sql += " and (b.insId=:insId or b.deptId=:insId)  ";
            m.put("insId", getSearchKeyword().getInsId());
        }

        if (!getSearchKeyword().getDeptId().isEmpty()) {
            sql += " and (b.insId=:deptId or b.deptId=:deptId)  ";
            m.put("deptId", getSearchKeyword().getDeptId());
        }

        if (!getSearchKeyword().getBhtNo().trim().isEmpty()) {
            sql += " and b.patientEncounter.bhtNo=:bht";
            m.put("bht", getSearchKeyword().getBhtNo());
        }
        if (getSearchKeyword().getRefBillNo() != null) {
            try {
                long l = Long.parseLong(getSearchKeyword().getRefBillNo());
                sql += " and b.id=:id";
                m.put("id", l);
            } catch (Exception e) {
            }

        }
        sql += " order by b.insId ";
//        m.put("class", PreBill.class);
        bills = getBillFacade().findBySQL(sql, m);
    }

    public void createSearchAll() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from Bill b where "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getInstitution() != null && !getSearchKeyword().getInstitution().trim().equals("")) {
            sql += " and  (upper(b.institution.name) like :ins )";
            temMap.put("ins", "%" + getSearchKeyword().getInstitution().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getToInstitution() != null && !getSearchKeyword().getToInstitution().trim().equals("")) {
            sql += " and  (upper(b.toInstitution.name) like :toIns )";
            temMap.put("toIns", "%" + getSearchKeyword().getToInstitution().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getToDepartment() != null && !getSearchKeyword().getToDepartment().trim().equals("")) {
            sql += " and  (upper(b.toDepartment.name) like :toDept )";
            temMap.put("toDept", "%" + getSearchKeyword().getToDepartment().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }
        if (getSearchKeyword().getFromDepartment() != null && !getSearchKeyword().getFromDepartment().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getFromDepartment().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPaymentScheme() != null && !getSearchKeyword().getPaymentScheme().trim().equals("")) {
            sql += " and  (upper(b.paymentScheme.name) like :pScheme )";
            temMap.put("pScheme", "%" + getSearchKeyword().getPaymentScheme().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPaymentmethod() != null && !getSearchKeyword().getPaymentmethod().trim().equals("")) {
            sql += " and  (upper(b.paymentMethod) like :pm )";
            temMap.put("pm", "%" + getSearchKeyword().getPaymentmethod().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  (upper(b.insId) like :insId )";
            temMap.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getDeptId() != null && !getSearchKeyword().getDeptId().trim().equals("")) {
            sql += " and  (upper(b.insId) like :deptId )";
            temMap.put("deptId", "%" + getSearchKeyword().getDeptId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createCollectingTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where "
                + " b.billType = :billType and "
                + " b.institution=:ins "
                + " and b.createdAt between :fromDate "
                + " and :toDate and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.LabBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createCollectingTableAll() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.LabBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createCollectingBillItemTable() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select bi from BillItem bi join bi.bill b where "
                + " b.billType = :billType and "
                + " b.institution=:ins "
                + " and b.createdAt between :fromDate "
                + " and :toDate and b.retired=false "
                + " and type(b)=:class ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.LabBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("class", BilledBill.class);

        billItems = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);
        ////System.out.println("billItems = " + billItems);

    }

    public void createCollectingBillItemTableAll() {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select bi from BillItem bi join bi.bill b where "
                + " b.billType = :billType and "
                + " b.institution=:ins "
                + " and b.createdAt between :fromDate "
                + " and :toDate and b.retired=false "
                + " and type(b)=:class ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(bi.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.LabBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());
        temMap.put("class", BilledBill.class);

        billItems = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        ////System.out.println("billItems = " + billItems);

    }

    public void createCreditTable() {
        Date startTime = new Date();

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b "
                + " where b.billType = :billType"
                + "  and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBank() != null && !getSearchKeyword().getBank().trim().equals("")) {
            sql += " and  (upper(b.bank.name) like :bank )";
            temMap.put("bank", "%" + getSearchKeyword().getBank().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.chequeRefNo) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.CashRecieveBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Receieve/Credit Company/Payment done search(/faces/credit/credit_company_bill_search.xhtml)");

    }

    public void createCreditTableBillItemAll() {
        Date startTime = new Date();

        createCreditTableBillItem(null, true);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Receieve/Credit Company/Credit Company/Payment done search bill item(/faces/credit/credit_company_bill_search_billItems.xhtml)");
    }

    public void createCreditTableBillItemOpd() {
        Date startTime = new Date();
        createCreditTableBillItem(BillType.OpdBill, false);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Receieve/Credit Company/Payment done search bill item(Search OPD)(/faces/credit/credit_company_bill_search_billItems.xhtml)");
    }

    public void createCreditTableBillItemBht() {
        Date startTime = new Date();
        createCreditTableBillItem(null, false);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Receieve/Credit Company/Payment done search bill item(Search BHT)(/faces/credit/credit_company_bill_search_billItems.xhtml)");
    }

    public void createCreditTableBillItem(BillType billType, boolean all) {
        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillItem b "
                + " where b.bill.billType = :billType"
                + "  and b.bill.institution=:ins "
                + " and b.bill.createdAt between :fromDate and :toDate "
                + " and b.bill.retired=false ";

        if (!all) {
            if (billType != null) {
                sql += " and b.referenceBill.billType=:refBtp";
                temMap.put("refBtp", billType);
            } else {
                sql += " and b.patientEncounter is not null ";
            }

        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }
        if (billType != null) {
            if (getSearchKeyword().getRefBillNo() != null && !getSearchKeyword().getRefBillNo().trim().equals("")) {
                sql += " and  (upper(b.referenceBill.insId) like :reBillNo )";
                temMap.put("reBillNo", "%" + getSearchKeyword().getRefBillNo().trim().toUpperCase() + "%");
            }
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.bill.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.bill.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBank() != null && !getSearchKeyword().getBank().trim().equals("")) {
            sql += " and  (upper(b.bill.bank.name) like :bank )";
            temMap.put("bank", "%" + getSearchKeyword().getBank().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.bill.chequeRefNo) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.bill.createdAt desc  ";
//    
        temMap.put("billType", BillType.CashRecieveBill);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        billItems = getBillItemFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public List<Bill> getChannelPaymentBillsOld() {
        if (bills == null) {
            String sql;
            Map temMap = new HashMap();
            if (bills == null) {
                sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.id in"
                        + "(Select bt.bill.id From BillItem bt Where bt.referenceBill.billType=:btp"
                        + " or bt.referenceBill.billType=:btp2) and b.billType=:type and b.createdAt "
                        + "between :fromDate and :toDate order by b.id";

                temMap.put("toDate", getToDate());
                temMap.put("fromDate", getFromDate());
                temMap.put("type", BillType.PaymentBill);
                temMap.put("btp", BillType.ChannelPaid);
                temMap.put("btp2", BillType.ChannelCredit);
                bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 100);

                if (bills == null) {
                    bills = new ArrayList<>();
                }
            }
        }
        return bills;

    }

    public void channelPaymentBills() {
        Date startTime = new Date();

        String sql;
        Map m = new HashMap();

        BillType[] bt = {
            BillType.ChannelOnCall,
            BillType.ChannelCash,
            BillType.ChannelAgent,
            BillType.ChannelStaff,
            BillType.ChannelCredit,};

        List<BillType> bts = Arrays.asList(bt);

        sql = "SELECT bi FROM BillItem bi WHERE bi.retired = false "
                + " and bi.bill.billType=:bt "
                + " and type(bi.bill)=:class "
                + " and bi.paidForBillFee.bill.billType in :bts "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(bi.paidForBillFee.bill.patientEncounter.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.paidForBillFee.bill.insId) like :billNo or upper(bi.paidForBillFee.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  (upper(bi.bill.insId) like :insId )";
            m.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(bi.paidForBillFee.staff.speciality.name) like :special )";
            m.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(bi.paidForBillFee.staff.person.name) like :staff )";
            m.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(bi.paidForBillFee.feeValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by bi.bill.createdAt desc  ";

        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("bt", BillType.PaymentBill);
        m.put("bts", bts);
        m.put("class", BilledBill.class);

        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Channeling/Payment/Payment done search(/faces/channel/channel_payment_bill_search.xhtml)");

    }

    public void channelPaymentBillsNew() {
        Date startTime = new Date();

        String sql;
        Map m = new HashMap();

        BillType[] bt = {
            BillType.ChannelOnCall,
            BillType.ChannelCash,
            BillType.ChannelAgent,
            BillType.ChannelStaff,
            BillType.ChannelPaid,
            BillType.ChannelCredit,};

        List<BillType> bts = Arrays.asList(bt);

        sql = "SELECT bi FROM BillItem bi WHERE bi.retired = false "
                + " and bi.bill.billType=:bt "
                + " and type(bi.bill)=:class "
                + " and bi.paidForBillFee.bill.billType in :bts "
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(bi.paidForBillFee.bill.patientEncounter.patient.person.name) like :patientName )";
            m.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(bi.paidForBillFee.bill.insId) like :billNo or upper(bi.paidForBillFee.bill.deptId) like :billNo )";
            m.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getInsId() != null && !getSearchKeyword().getInsId().trim().equals("")) {
            sql += " and  (upper(bi.bill.insId) like :insId )";
            m.put("insId", "%" + getSearchKeyword().getInsId().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(bi.paidForBillFee.staff.speciality.name) like :special )";
            m.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(bi.paidForBillFee.staff.person.name) like :staff )";
            m.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(bi.paidForBillFee.feeValue) like :total )";
            m.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by bi.bill.createdAt desc  ";

        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("bt", BillType.ChannelProPayment);
        m.put("bts", bts);
        m.put("class", BilledBill.class);
        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP, 50);
        commonController.printReportDetails(fromDate, toDate, startTime, "Channeling/Payment/Payment done search(/faces/channel/channel_payment_bill_search.xhtml)");
    }

    public void channelAgentPaymentBills() {
        Date startTime = new Date();

        String sql;
        Map m = new HashMap();

        sql = "SELECT bi FROM BillItem bi WHERE bi.retired = false "
                + " and bi.bill.billType=:bt"
                + " and bi.createdAt between :fromDate and :toDate ";

        if (getSearchKeyword().getIns() != null) {
            sql += " and bi.bill.toInstitution=:ins";
            m.put("ins", getSearchKeyword().getIns());
        }

        m.put("fromDate", getFromDate());
        m.put("toDate", getToDate());
        m.put("bt", BillType.ChannelAgencyPayment);
        billItems = getBillItemFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/OPD/Channel/Agency/Payment done search(/faces/channel/channel_payment_agency_bill_search.xhtml)");

    }

    public void createChannelDueBillFeeOld() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BillFee b where b.retired=false and (b.bill.billType=:btp or b.bill.billType=:btp2) "
                + " and b.bill.id in(Select bs.bill.id From BillSession bs where bs.retired=false ) "
                + "and b.bill.cancelled=false and (b.feeValue - b.paidValue) > 0 and  "
                + "b.bill.createdAt between :fromDate and :toDate order by b.staff.id  ";

        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("btp", BillType.ChannelPaid);
        temMap.put("btp2", BillType.ChannelCredit);

        billFees = getBillFeeFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createChannelDueBillFee() {
        Date startTime = new Date();

        selectedServiceSession = null;

        BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        List<BillType> bts = Arrays.asList(billTypes);
        String sql = " SELECT b FROM BillFee b "
                + "  where type(b.bill)=:class "
                + " and b.bill.retired=false "
                + " and b.bill.paidAmount!=0 "
                + " and b.bill.refunded=false "
                + " and b.fee.feeType=:ftp"
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 "
                + " and b.bill.billType in :bt ";

        HashMap hm = new HashMap();
        if (getFromDate() != null && getToDate() != null) {
            sql += " and b.bill.appointmentAt between :frm and  :to";
            hm.put("frm", getFromDate());
            hm.put("to", getToDate());
        }

        if (getSelectedServiceSession() != null) {
            sql += " and bs.serviceSession=:ss";
            hm.put("ss", getSelectedServiceSession());
        }

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.bill.patient.person.name) like :patientName )";
            hm.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :billNo )";
            hm.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.feeValue) like :total )";
            hm.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getSpeciality() != null && !getSearchKeyword().getSpeciality().trim().equals("")) {
            sql += " and  (upper(b.staff.speciality.name) like :special )";
            hm.put("special", "%" + getSearchKeyword().getSpeciality().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :staff )";
            hm.put("staff", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        sql += " order by b.speciality.name ";

        //hm.put("ins", sessionController.getInstitution());
        hm.put("bt", bts);
        hm.put("ftp", FeeType.Staff);
        hm.put("class", BilledBill.class);
        billFees = billFeeFacade.findBySQL(sql, hm, TemporalType.TIMESTAMP, 50);
        commonController.printReportDetails(fromDate, toDate, startTime, "Channeling/Payment/Payment due search(/faces/channel/channel_payments_due_search.xhtml)");
    }

    public void createChannelDueBillFeeByAgent() {
        selectedServiceSession = null;

        //BillType[] billTypes = {BillType.ChannelAgent, BillType.ChannelCash, BillType.ChannelOnCall, BillType.ChannelStaff};
        // List<BillType> bts = Arrays.asList(billTypes);
        String sql = " SELECT b FROM BillFee b "
                + "  where type(b.bill)=:class "
                + " and b.bill.retired=false "
                + " and b.bill.paidAmount!=0 "
                + " and b.bill.refunded=false "
                + " and b.fee.feeType=:ftp"
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 "
                + " and b.bill.billType in :bt ";

        HashMap hm = new HashMap();
        if (getFromDate() != null && getToDate() != null) {
            sql += " and b.bill.appointmentAt between :frm and  :to";
            hm.put("frm", getFromDate());
            hm.put("to", getToDate());
        }

        if (getSelectedServiceSession() != null) {
            sql += " and bs.serviceSession=:ss";
            hm.put("ss", getSelectedServiceSession());
        }

//        if (getCurrentStaff() != null) {
//            sql += " and b.staff=:stf ";
//            hm.put("stf", getCurrentStaff());
//        }
        //hm.put("ins", sessionController.getInstitution());
        hm.put("bt", BillType.ChannelAgent);
        hm.put("ftp", FeeType.OtherInstitution);
        hm.put("class", BilledBill.class);
        billFees = billFeeFacade.findBySQL(sql, hm, TemporalType.TIMESTAMP);

    }

    public void createChannelAgencyPaymentTable() {
        Date startTime = new Date();

        createAgentPaymentTable(BillType.AgentPaymentReceiveBill);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Receieve/Agent/Agent payment bill search(/faces/agent_bill_search_own.xhtml)");
    }

    public void createChannelAgencyCreditNoteTable() {

        createAgentPaymentTable(BillType.AgentCreditNoteBill);

    }

    public void createChannelAgencyDebitNoteTable() {

        createAgentPaymentTable(BillType.AgentDebitNoteBill);

    }

    public void createCollectingCenterCreditNoteTable() {

        createAgentPaymentTable(BillType.CollectingCentreCreditNoteBill);

    }

    public void createCollectingCenterDebitNoteTable() {

        createAgentPaymentTable(BillType.CollectingCentreDebitNoteBill);

    }

    public void createCollectingCentrePaymentTable() {
        Date startTime = new Date();
        createAgentPaymentTable(BillType.CollectingCentrePaymentReceiveBill);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Receieve/Collecting center/Collecting center bill serach(/faces/lab/collecting_centre_bill_search_own.xhtml)");
    }

    public void createAgentPaymentTable(BillType billType) {
        bills = new ArrayList<>();
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType "
                + " and b.institution=:ins and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getFromInstitution() != null && !getSearchKeyword().getFromInstitution().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.name) like :frmIns )";
            temMap.put("frmIns", "%" + getSearchKeyword().getFromInstitution().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.fromInstitution.institutionCode) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";

        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createInwardServiceTable() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select (b.bill) from BillItem b where "
                + " b.bill.billType = :billType "
                + " and type(b.bill)=:class "
                + " and b.bill.createdAt between :fromDate and :toDate"
                + " and b.bill.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.bill.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by b.bill.insId desc ";
        temMap.put("billType", BillType.InwardBill);
        temMap.put("class", BilledBill.class);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        commonController.printReportDetails(fromDate, toDate, startTime, "Search/Service bill (/faces/inward/inward_search_service.xhtml)");

    }

    public void createInwardServiceTablebyLoggedDepartment() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select (b.bill) from BillItem b where "
                + " b.bill.billType = :billType "
                + " and b.bill.createdAt between :fromDate and :toDate"
                + " and b.bill.retired=false  "
                + " and b.bill.department = :dep";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.bill.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.bill.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.bill.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.item.name) like :item )";
            temMap.put("item", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        sql += " order by b.bill.insId desc ";
        temMap.put("dep", getSessionController().getDepartment());
        temMap.put("billType", BillType.InwardBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        commonController.printReportDetails(fromDate, toDate, startTime, "lab/inward billing/search bills(/faces/lab/lab/inward_search_service.xhtml)");

    }

    public void createInwardServiceTableDischarged() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.createdAt is not null "
                + " and b.patientEncounter.discharged=true and"
                + " b.billType = :billType and b.createdAt between :fromDate and :toDate "
                + "and b.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += "order by b.insId desc";

        temMap.put("billType", BillType.InwardBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createInwardFinalBillsCheck() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType and "
                + " b.patientEncounter.dateOfDischarge between :fromDate and :toDate "
                + "and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.insId desc  ";

        temMap.put("billType", BillType.InwardFinalBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "search/Final Bill Search By Discharg date(/faces/inward/inward_search_final_check.xhtml)");

    }

    public void createInwardFinalBills() {
//        double d = commonController.dateDifferenceInMinutes(fromDate, toDate) / (60 * 24);
//        if (d > 32 && getReportKeyWord().isBool1()) {
//            JsfUtil.addErrorMessage("Date Range To Long");
//            return;
//        }
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + "and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.insId desc  ";

        temMap.put("billType", BillType.InwardFinalBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

//        if (getReportKeyWord().isBool1()) {
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//        } else {
//            bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);
//        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Search/Final bill search(/faces/inward/inward_search_final.xhtml)");
    }

    public void createCancelledInwardFinalBills() {
        Date startTime = new Date();
        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false "
                + " and b.cancelled=true ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.insId desc  ";

        temMap.put("billType", BillType.InwardFinalBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

//        if (getReportKeyWord().isBool1()) {
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//        } else {
//            bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);
//        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Search/Final Cancel bill search(/faces/inward/inward_search_final.xhtml)");
    }

    public void createInwardIntrimBills() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + "and b.retired=false ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.insId desc  ";

        temMap.put("billType", BillType.InwardIntrimBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Billing/Intrim Bill Search(/faces/inward/inward_search_intrim.xhtml)");

    }

    public void createInwardPaymentBills() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where"
                + " b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.insId desc  ";

        temMap.put("billType", BillType.InwardPaymentBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Receieve/Inward Deposite/Payemnt Search(/faces/inward/inward_search_payment.xhtml)");

    }

    public void createInwardRefundBills() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from RefundBill b where "
                + " b.billType = :billType "
                + " and b.billedBill is null "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.insId desc  ";

        temMap.put("billType", BillType.InwardPaymentBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Receieve/Inward Deposite/Refund Search(/faces/inward/inward_search_refund.xhtml)");
    }

    public void createInwardSurgeryBills() {
        Date startTime = new Date();

        if (searchKeyword.isActiveAdvanceOption() && searchKeyword.getItem() == null && searchKeyword.getItemName().equals("")) {
            JsfUtil.addErrorMessage("You Need To select Surgury to Search All");
            return;
        }

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where "
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false  ";

        if (getSearchKeyword().getStaffName() != null
                && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :staffName )";
            temMap.put("staffName", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientName() != null
                && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null
                && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null
                && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null
                && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null
                && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.procedure.item.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItem() != null) {
            sql += " and b.procedure.item=:item ";
            temMap.put("item", getSearchKeyword().getItem());
        }

        if (getSearchKeyword().getTotal() != null
                && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.insId desc  ";

        temMap.put("billType", BillType.SurgeryBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        if (searchKeyword.isActiveAdvanceOption()) {
            bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
        } else {
            bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Theater/Search(/faces/theater/inward_search_surgery.xhtml)");

    }

    public void createInwardSurgeryBillsReport() {

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where "
                + " b.billType = :billType and "
                + " b.createdAt between :fromDate and :toDate "
                + " and b.retired=false  ";

        if (getSearchKeyword().getStaffName() != null
                && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :staffName )";
            temMap.put("staffName", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientName() != null
                && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null
                && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null
                && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null
                && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getItemName() != null
                && !getSearchKeyword().getItemName().trim().equals("")) {
            sql += " and  (upper(b.procedure.item.name) like :itm )";
            temMap.put("itm", "%" + getSearchKeyword().getItemName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getTotal() != null
                && !getSearchKeyword().getTotal().trim().equals("")) {
            sql += " and  (upper(b.total) like :total )";
            temMap.put("total", "%" + getSearchKeyword().getTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.insId desc  ";

        temMap.put("billType", BillType.SurgeryBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

    }

    public void createInwardPaymentBillsDischarged() {

        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where  "
                + " and b.patientEncounter.discharged=true"
                + " and b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += "order by b.insId desc";

        temMap.put("billType", BillType.InwardPaymentBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createInwardProBills() {
        Date startTime = new Date();

        String sql;
        Map temMap = new HashMap();
        sql = "select b from BilledBill b where "
                + " b.billType = :billType "
                + " and b.createdAt between :fromDate and :toDate"
                + " and b.retired=false";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += " order by b.insId desc";

        temMap.put("billType", BillType.InwardProfessional);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);

        commonController.printReportDetails(fromDate, toDate, startTime, "Search/Professional Bills(/faces/inward/inward_search_professional.xhtml)");

    }

    public void createInwardProBillsDischarged() {

        String sql;
        Map temMap = new HashMap();
//        sql = "select b from BilledBill b where b.createdAt is not null and b.billType = :billType and b.patientEncounter.discharged=true and "
//                + " b.id in(Select bf.bill.id From BillFee bf where bf.retired=false and bf.createdAt between :fromDate and :toDate and bf.billItem is null)"
//                + " and b.createdAt between :fromDate and :toDate and b.retired=false";

        sql = "select b from BilledBill b where b.createdAt is not null "
                + " and b.patientEncounter.discharged=true and"
                + " b.billType = :billType and b.createdAt between :fromDate and :toDate "
                + "and b.retired=false  ";

        if (getSearchKeyword().getPatientName() != null && !getSearchKeyword().getPatientName().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.name) like :patientName )";
            temMap.put("patientName", "%" + getSearchKeyword().getPatientName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPatientPhone() != null && !getSearchKeyword().getPatientPhone().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.patient.person.phone) like :patientPhone )";
            temMap.put("patientPhone", "%" + getSearchKeyword().getPatientPhone().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBhtNo() != null && !getSearchKeyword().getBhtNo().trim().equals("")) {
            sql += " and  (upper(b.patientEncounter.bhtNo) like :bht )";
            temMap.put("bht", "%" + getSearchKeyword().getBhtNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        sql += "order by b.insId desc";

        temMap.put("billType", BillType.InwardBill);
        temMap.put("toDate", toDate);
        temMap.put("fromDate", fromDate);

        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

    }

    public void createPettyTable() {
        Date startTime = new Date();

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType = :billType and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :stf )";
            temMap.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPersonName() != null && !getSearchKeyword().getPersonName().trim().equals("")) {
            sql += " and  (upper(b.person.name) like :per )";
            temMap.put("per", "%" + getSearchKeyword().getPersonName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.invoiceNumber) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", BillType.PettyCash);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Petty Cash/ Petty Cash Bill search(/faces/petty_cash_bill_search_own.xhtml)");

    }

    public void createIncomeBillTable() {
        fetchBillTable(BillType.ChannelIncomeBill);
    }

    public void createExpensesBillTable() {
        fetchBillTable(BillType.ChannelExpenesBill);
    }

    public void fetchBillTable(BillType billType) {
        Date startTime = new Date();

        bills = null;
        String sql;
        Map temMap = new HashMap();

        sql = "select b from BilledBill b where b.billType =:billType "
                + " and b.institution=:ins "
                + " and b.createdAt between :fromDate and :toDate "
                + " and b.retired=false ";

        if (getSearchKeyword().getBillNo() != null && !getSearchKeyword().getBillNo().trim().equals("")) {
            sql += " and  (upper(b.insId) like :billNo )";
            temMap.put("billNo", "%" + getSearchKeyword().getBillNo().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getStaffName() != null && !getSearchKeyword().getStaffName().trim().equals("")) {
            sql += " and  (upper(b.staff.person.name) like :stf )";
            temMap.put("stf", "%" + getSearchKeyword().getStaffName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getPersonName() != null && !getSearchKeyword().getPersonName().trim().equals("")) {
            sql += " and  (upper(b.person.name) like :per )";
            temMap.put("per", "%" + getSearchKeyword().getPersonName().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNetTotal() != null && !getSearchKeyword().getNetTotal().trim().equals("")) {
            sql += " and  (upper(b.netTotal) like :netTotal )";
            temMap.put("netTotal", "%" + getSearchKeyword().getNetTotal().trim().toUpperCase() + "%");
        }

        if (getSearchKeyword().getNumber() != null && !getSearchKeyword().getNumber().trim().equals("")) {
            sql += " and  (upper(b.invoiceNumber) like :num )";
            temMap.put("num", "%" + getSearchKeyword().getNumber().trim().toUpperCase() + "%");
        }

        sql += " order by b.createdAt desc  ";
//    
        temMap.put("billType", billType);
        temMap.put("toDate", getToDate());
        temMap.put("fromDate", getFromDate());
        temMap.put("ins", getSessionController().getInstitution());

        //System.err.println("Sql " + sql);
        bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 50);

        commonController.printReportDetails(fromDate, toDate, startTime, "Payments/Petty Cash/ Petty Cash Bill search(/faces/petty_cash_bill_search_own.xhtml)");

    }

    public void createAllBillContacts() {
        String sql;
        Map temMap = new HashMap();
        telephoneNumbers = new ArrayList<>();

        sql = "select b from Bill b where ";

        sql += " b.retired = false "
                + " and b.cancelled=false "
                + " and b.refunded=false "
                + " and (b.patient.person.phone is not null "
                + " or b.patient.person.phone!=:em) "
                + " and b.createdAt between :fd and :td  ";

        sql += " order by b.patient.person.phone ";

        temMap.put("em", "");
        temMap.put("fd", fromDate);
        temMap.put("td", toDate);

    }

    public void importToExcel() {
//        if (file == null) {
//            UtilityController.addErrorMessage("Select File");
//            return;
//        }
        telephoneNumbers = new ArrayList<>();
        String number;
        File inputWorkbook;
        Workbook w;
        Cell cell;
        InputStream in;
        UtilityController.addSuccessMessage(file.getFileName());
        try {
            System.err.println("in 1");
            UtilityController.addSuccessMessage(file.getFileName());
            System.err.println("in 2");
            in = file.getInputStream();
            File f;
            f = new File(Calendar.getInstance().getTimeInMillis() + file.getFileName());
            FileOutputStream out = new FileOutputStream(f);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            in.close();
            out.flush();
            out.close();

            inputWorkbook = new File(f.getAbsolutePath());

            UtilityController.addSuccessMessage("Excel File Opened");
            w = Workbook.getWorkbook(inputWorkbook);
            Sheet sheet = w.getSheet(0);

            for (int i = 0; i < sheet.getRows(); i++) {
                cell = sheet.getCell(1, i);
                number = cell.getContents();
                if (number.contains("077") || number.contains("076")
                        || number.contains("070")
                        || number.contains("071") || number.contains("072")
                        || number.contains("075") || number.contains("078")) {
                    telephoneNumbers.add(number);
                }
            }
            UtilityController.addSuccessMessage("Succesful. All the data in Excel File Impoted.");
        } catch (Exception e) {
        }
    }

    public void sendSms() {
        smsController.sendSmsToNumberList(uniqueSmsText, getSessionController().getLoggedPreference().getApplicationInstitution(), smsText, null, MessageType.Marketing);
    }

    public void sendSmsAll() {
        if (selectedTelephoneNumbers == null) {
            JsfUtil.addErrorMessage("Please Select Numbers");
            return;
        }
        if (selectedTelephoneNumbers.size() > 10000) {
            JsfUtil.addErrorMessage("Please Contact System Development Team.You are trying to send more than 10,000 sms.");
            return;
        }
        if (smsText.equals("") || smsText == null) {
            JsfUtil.addErrorMessage("Enter Message");
            return;
        }
        for (String stn : selectedTelephoneNumbers) {

            smsController.sendSmsToNumberList(stn, getSessionController().getLoggedPreference().getApplicationInstitution(), smsText, null, MessageType.Marketing);
            JsfUtil.addSuccessMessage("Done.");
        }

    }

    public void createDocPaymentDue() {
        fetchDueFeeTable(new BillType[]{BillType.OpdBill, BillType.CollectingCentreBill}, false);
    }

    private void fetchDueFeeTable(BillType[] billTypes, boolean isInward) {
        String sql;
        Map m = new HashMap();

        sql = "select b from BillFee b where"
                + " b.retired=false "
                + " and b.bill.billType in :bts "
                + " and b.bill.cancelled=false "
                + " and (b.feeValue - b.paidValue) > 0 "
                + " and b.bill.createdAt between :fromDate and :toDate ";

        sql += " order by b.staff.person.name ";

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        //System.out.println("1.cal.getTime() = " + cal.getTime());
        cal.set(2013, 00, 01, 00, 00, 00);
        //System.out.println("2.cal.getTime() = " + cal.getTime());
        m.put("fromDate", cal.getTime());
        m.put("toDate", getToDate());
        m.put("bts", Arrays.asList(billTypes));
//        temMap.put("btp", BillType.OpdBill);
//        temMap.put("btpc", BillType.CollectingCentreBill);
//        temMap.put("btp", BillType.InwardBill);
//        temMap.put("btp2", BillType.InwardProfessional);

        billFees = getBillFeeFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);
        //System.out.println("billFees.size() = " + billFees.size());

        List<BillFee> afterPaid = new ArrayList<>();

        sql = "Select bi.paidForBillFee FROM BillItem bi "
                + " where bi.retired=false "
                + " and bi.bill.billType=:bType "
                + " and bi.referenceBill.billType in :refType "
                + " and bi.bill.createdAt > :toDate "
                + " and bi.paidForBillFee.bill.createdAt <= :toDate";

//        sql += " order by b.createdAt desc  ";
        m = new HashMap();
        m.put("toDate", getToDate());
        m.put("bType", BillType.PaymentBill);
        m.put("refType", Arrays.asList(billTypes));
//        temMap.put("refType", BillType.OpdBill);

        afterPaid = getBillFeeFacade().findBySQL(sql, m, TemporalType.TIMESTAMP);
        billFees.addAll(afterPaid);
        List<BillFee> removeingBillFees = new ArrayList<>();

        for (BillFee bf : billFees) {
            sql = "SELECT bi FROM BillItem bi where bi.retired=false "
                    + " and bi.bill.cancelled=false "
                    + " and bi.referanceBillItem.id=" + bf.getBillItem().getId();

            BillItem rbi = null;
            if (isInward) {
                m = new HashMap();
                m.put("btp", BillType.InwardBill);
                sql = "SELECT bi FROM BillItem bi where bi.retired=false "
                        + " and bi.bill.cancelled=false "
                        + " and bi.bill.billType=:btp "
                        + " and bi.referanceBillItem.id=" + bf.getBillItem().getId();
            } else {
                m = new HashMap();
//                m.put("class", RefundBill.class);
                sql = "SELECT bi FROM BillItem bi where "
                        + " bi.retired=false"
                        + " and bi.bill.cancelled=false "
                        //                        + " and type(bi.bill)=:class "
                        + " and bi.referanceBillItem.id=" + bf.getBillItem().getId();
            }
            rbi = getBillItemFacade().findFirstBySQL(sql, m);

            if (rbi != null) {
                removeingBillFees.add(bf);
            }
        }
        billFees.removeAll(removeingBillFees);
        total = 0.0;
        for (BillFee bf : billFees) {
            total += bf.getFeeValue();
        }
    }

//    public void createAllBillContacts() {
//        Map temMap = new HashMap();
//        bills=new ArrayList<>();
//
//        String sql = "select  b from Bill b where "
//                + " b.retired = false "
//                + " and b.cancelled=false "
//                + " and b.refunded=false "
//                + " and (b.patient.person.phone is not null "
//                + " or b.patient.person.phone=:em) "
//                + " and b.createdAt between :fd and :td  "
//                + " group by b.patient.person.phone  ";
//        
//        temMap.put("fd", fromDate);
//        temMap.put("td", toDate);
//        temMap.put("em", "");
//        
//        bills=getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP);
//        //System.out.println("sql = " + sql);
//        //System.out.println("temMap = " + temMap);
//        //System.out.println("bills.size() = " + bills.size());
//
//    }
//     public List<Bill> getInstitutionPaymentBills() {
//        if (bills == null) {
//            String sql;
//            Map temMap = new HashMap();
//            if (bills == null) {
//                if (txtSearch == null || txtSearch.trim().equals("")) {
//                    sql = "SELECT b FROM BilledBill b WHERE b.retired=false and b.billType=:type and b.createdAt between :fromDate and :toDate order by b.id";
//                    temMap.put("toDate", getToDate());
//                    temMap.put("fromDate", getFromDate());
//                    temMap.put("type", BillType.PaymentBill);
//                    bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 100);
//
//                } else {
//                    sql = "select b from BilledBill b where b.retired=false and b.billType=:type and b.createdAt between :fromDate and :toDate and (upper(b.staff.person.name) like '%" + txtSearch.toUpperCase() + "%'  or upper(b.staff.person.phone) like '%" + txtSearch.toUpperCase() + "%'  or upper(b.insId) like '%" + txtSearch.toUpperCase() + "%') order by b.createdAt desc  ";
//                    temMap.put("toDate", getToDate());
//                    temMap.put("fromDate", getFromDate());
//                    temMap.put("type", BillType.PaymentBill);
//                    bills = getBillFacade().findBySQL(sql, temMap, TemporalType.TIMESTAMP, 100);
//                }
//                if (bills == null) {
//                    bills = new ArrayList<Bill>();
//                }
//            }
//        }
//        return bills;
//
//    }
    public void listnerBillTypeChange() {
    }

    public void listnerReportSearch() {
    }

    public SearchController() {
    }

    public class PharmacyAdjustmentRow {

        Item itm;
        double purchaseRate;
        double saleRate;
        double befoerQty;
        double afterQty;
        double adjusetedQty;
        double befoerVal;
        double afterVal;
        double adjusetedVal;
        String batchNo;
        Date expiry;

        public PharmacyAdjustmentRow() {
        }

        public PharmacyAdjustmentRow(Item itm, double purchaseRate, double saleRate, double befoerQty, double afterQty, double adjusetedQty, String batchNo, Date expiry) {
            this.itm = itm;
            this.purchaseRate = purchaseRate;
            this.saleRate = saleRate;
            this.befoerQty = befoerQty;
            this.afterQty = afterQty;
            this.adjusetedQty = adjusetedQty;
            this.batchNo = batchNo;
            this.expiry = expiry;
            this.adjusetedVal = adjusetedQty * purchaseRate;
            this.befoerVal = befoerQty * purchaseRate;
            this.afterVal = afterQty * purchaseRate;
        }

        public Item getItm() {
            return itm;
        }

        public void setItm(Item itm) {
            this.itm = itm;
        }

        public double getPurchaseRate() {
            return purchaseRate;
        }

        public void setPurchaseRate(double purchaseRate) {
            this.purchaseRate = purchaseRate;
        }

        public double getSaleRate() {
            return saleRate;
        }

        public void setSaleRate(double saleRate) {
            this.saleRate = saleRate;
        }

        public double getBefoerQty() {
            return befoerQty;
        }

        public void setBefoerQty(double befoerQty) {
            this.befoerQty = befoerQty;
        }

        public double getAfterQty() {
            return afterQty;
        }

        public void setAfterQty(double afterQty) {
            this.afterQty = afterQty;
        }

        public double getAdjusetedQty() {
            return adjusetedQty;
        }

        public void setAdjusetedQty(double adjusetedQty) {
            this.adjusetedQty = adjusetedQty;
        }

        public String getBatchNo() {
            return batchNo;
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }

        public Date getExpiry() {
            return expiry;
        }

        public void setExpiry(Date expiry) {
            this.expiry = expiry;
        }

        public double getBefoerVal() {
            return befoerVal;
        }

        public void setBefoerVal(double befoerVal) {
            this.befoerVal = befoerVal;
        }

        public double getAfterVal() {
            return afterVal;
        }

        public void setAfterVal(double afterVal) {
            this.afterVal = afterVal;
        }

        public double getAdjusetedVal() {
            return adjusetedVal;
        }

        public void setAdjusetedVal(double adjusetedVal) {
            this.adjusetedVal = adjusetedVal;
        }
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = getCommonFunctions().getEndOfDay(new Date());
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = getCommonFunctions().getStartOfDay(new Date());
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public SearchKeyword getSearchKeyword() {
        if (searchKeyword == null) {
            searchKeyword = new SearchKeyword();
        }
        return searchKeyword;
    }

    public void setSearchKeyword(SearchKeyword searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public CommonFunctions getCommonFunctions() {
        return commonFunctions;
    }

    public void setCommonFunctions(CommonFunctions commonFunctions) {
        this.commonFunctions = commonFunctions;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public BillFacade getBillFacade() {
        return billFacade;
    }

    public void setBillFacade(BillFacade billFacade) {
        this.billFacade = billFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public BillFeeFacade getBillFeeFacade() {
        return billFeeFacade;
    }

    public void setBillFeeFacade(BillFeeFacade billFeeFacade) {
        this.billFeeFacade = billFeeFacade;
    }

    public List<BillFee> getBillFees() {
        return billFees;
    }

    public void setBillFees(List<BillFee> billFees) {
        this.billFees = billFees;
    }

    public List<BillItem> getBillItems() {
        return billItems;
    }

    public void setBillItems(List<BillItem> billItems) {
        this.billItems = billItems;
    }

    public BillItemFacade getBillItemFacade() {
        return billItemFacade;
    }

    public void setBillItemFacade(BillItemFacade billItemFacade) {
        this.billItemFacade = billItemFacade;
    }

    public int getMaxResult() {

        return maxResult;
    }

    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    public List<Bill> getSelectedBills() {
        return selectedBills;
    }

    public void setSelectedBills(List<Bill> selectedBills) {
        this.selectedBills = selectedBills;
    }

    public PharmacyBean getPharmacyBean() {
        return pharmacyBean;
    }

    public void setPharmacyBean(PharmacyBean pharmacyBean) {
        this.pharmacyBean = pharmacyBean;
    }

    public BillType getBillType() {
        return billType;
    }

    public void setBillType(BillType billType) {
        this.billType = billType;
    }

    public TransferController getTransferController() {
        return transferController;
    }

    public void setTransferController(TransferController transferController) {
        this.transferController = transferController;
    }

    public Bill getCancellingIssueBill() {
        return cancellingIssueBill;
    }

    public void setCancellingIssueBill(Bill cancellingIssueBill) {
        this.cancellingIssueBill = cancellingIssueBill;
    }

    public double getNetTotalValue() {
        return netTotalValue;
    }

    public void setNetTotalValue(double netTotalValue) {
        this.netTotalValue = netTotalValue;
    }

    public List<BillFee> getBillFeesDone() {
        return billFeesDone;
    }

    public void setBillFeesDone(List<BillFee> billFeesDone) {
        this.billFeesDone = billFeesDone;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public double getDueTotal() {
        return dueTotal;
    }

    public void setDueTotal(double dueTotal) {
        this.dueTotal = dueTotal;
    }

    public double getDoneTotal() {
        return doneTotal;
    }

    public void setDoneTotal(double doneTotal) {
        this.doneTotal = doneTotal;
    }

    public double getTotalPaying() {
        return totalPaying;
    }

    public void setTotalPaying(double totalPaying) {
        this.totalPaying = totalPaying;
    }

    public double getNetTotal() {
        return netTotal;
    }

    public void setNetTotal(double netTotal) {
        this.netTotal = netTotal;
    }

    public List<Bill> getAceptPaymentBills() {
        if (aceptPaymentBills == null) {
            aceptPaymentBills = new ArrayList<>();
        }
        return aceptPaymentBills;
    }

    public void setAceptPaymentBills(List<Bill> aceptPaymentBills) {
        this.aceptPaymentBills = aceptPaymentBills;
    }

    public String getMenuBarSearchText() {
        return menuBarSearchText;
    }

    public void setMenuBarSearchText(String menuBarSearchText) {
        this.menuBarSearchText = menuBarSearchText;
    }

    public boolean isChannelingPanelVisible() {
        return channelingPanelVisible;
    }

    public void setChannelingPanelVisible(boolean channelingPanelVisible) {
        this.channelingPanelVisible = channelingPanelVisible;
    }

    public boolean isPharmacyPanelVisible() {
        return pharmacyPanelVisible;
    }

    public void setPharmacyPanelVisible(boolean pharmacyPanelVisible) {
        this.pharmacyPanelVisible = pharmacyPanelVisible;
    }

    public boolean isOpdPanelVisible() {
        return opdPanelVisible;
    }

    public void setOpdPanelVisible(boolean opdPanelVisible) {
        this.opdPanelVisible = opdPanelVisible;
    }

    public boolean isInwardPanelVisible() {
        return inwardPanelVisible;
    }

    public void setInwardPanelVisible(boolean inwardPanelVisible) {
        this.inwardPanelVisible = inwardPanelVisible;
    }

    public boolean isLabPanelVisile() {
        return labPanelVisile;
    }

    public void setLabPanelVisile(boolean labPanelVisile) {
        this.labPanelVisile = labPanelVisile;
    }

    public boolean isPatientPanelVisible() {
        return patientPanelVisible;
    }

    public void setPatientPanelVisible(boolean patientPanelVisible) {
        this.patientPanelVisible = patientPanelVisible;
    }

    public List<Bill> getChannellingBills() {
        return channellingBills;
    }

    public void setChannellingBills(List<Bill> channellingBills) {
        this.channellingBills = channellingBills;
    }

    public List<Bill> getOpdBills() {
        return opdBills;
    }

    public void setOpdBills(List<Bill> opdBills) {
        this.opdBills = opdBills;
    }

    public List<Bill> getPharmacyBills() {
        return pharmacyBills;
    }

    public void setPharmacyBills(List<Bill> pharmacyBills) {
        this.pharmacyBills = pharmacyBills;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    public List<String> getTelephoneNumbers() {
        return telephoneNumbers;
    }

    public void setTelephoneNumbers(List<String> telephoneNumbers) {
        this.telephoneNumbers = telephoneNumbers;
    }

    public List<String> getSelectedTelephoneNumbers() {
        return selectedTelephoneNumbers;
    }

    public void setSelectedTelephoneNumbers(List<String> selectedTelephoneNumbers) {
        this.selectedTelephoneNumbers = selectedTelephoneNumbers;
    }

    boolean paginator = true;
    int rows = 20;

    public boolean isPaginator() {
        return paginator;
    }

    public void setPaginator(boolean paginator) {
        this.paginator = paginator;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getSmsText() {
        return smsText;
    }

    public void setSmsText(String smsText) {
        this.smsText = smsText;
    }

    public String getUniqueSmsText() {
        return uniqueSmsText;
    }

    public void setUniqueSmsText(String uniqueSmsText) {
        this.uniqueSmsText = uniqueSmsText;
    }

    public StockFacade getStockFacade() {
        return stockFacade;
    }

    public void setStockFacade(StockFacade stockFacade) {
        this.stockFacade = stockFacade;
    }

    public List<PharmacyAdjustmentRow> getPharmacyAdjustmentRows() {
        return pharmacyAdjustmentRows;
    }

    public void setPharmacyAdjustmentRows(List<PharmacyAdjustmentRow> pharmacyAdjustmentRows) {
        this.pharmacyAdjustmentRows = pharmacyAdjustmentRows;
    }

}
