/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.entity.hr;

import static com.divudi.data.InstitutionType.Bank;
import com.divudi.data.hr.PaysheetComponentType;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Staff;
import com.divudi.entity.WebUser;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Buddhika
 */
@Entity
@XmlRootElement
public class StaffSalary implements Serializable {

    @OneToMany(mappedBy = "staffSalary", fetch = FetchType.LAZY)
    private List<StaffSalaryComponant> staffSalaryComponants;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private double payeeValue;
//    private double otValue;
//    private double phValue;
    @ManyToOne
    private SalaryCycle salaryCycle;
    @ManyToOne
    private Staff staff;
    @Transient
    private boolean exist;
    //Created Properties
    @ManyToOne
    private WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    //Retairing properties
    private boolean retired;
    @ManyToOne
    private WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;
    private double basicValue;
    double brValue;
    private double overTimeValue;
    private double extraDutyNormalValue;
    private double extraDutyMerchantileValue;
    private double extraDutyPoyaValue;
    private double extraDutyDayOffValue;
    private double extraDutySleepingDayValue;
    private double noPayValueBasic;
    private double noPayValueAllowance;
    private double merchantileAllowanceValue;
    private double poyaAllowanceValue;
    private double dayOffAllowance;
    double sleepingDayAllowance;
    private double adjustmentToBasic;
    private double adjustmentToAllowance;
    private double etfSatffValue;
    private double etfCompanyValue;
    private double epfStaffValue;
    private double epfCompanyValue;
    double noPayCount;
    double merchantileCount;
    double poyaCount;
    double dayOffCount;
    private double extraDutyNormalMinute;
    private double extraDutyMerchantileMinute;
    private double extraDutyPoyaMinute;
    private double extraDutyDayOffMinute;
    private double extraDutySleepingDayMinute;
    double overTimeMinute;
    double sleepingDayCount;
    double componentValueAddition;
    double componentValueSubstraction;
    double overTimeRatePerMinute;
    double basicRatePerMinute;
    @ManyToOne
    Institution institution;
    @ManyToOne
    Department department;
    @Transient
    private List<StaffSalaryComponant> transStaffSalaryComponantsAddition;
    @Transient
    private List<StaffSalaryComponant> transStaffSalaryComponantsSubtraction;
    //Not Consider For Any Calculation it's Already included for No Pay
    double lateNoPayCount;
    double lateNoPayBasicValue;
    double lateNoPayAllovanceValue;
    //Blocked Propertied
    boolean blocked;
    @ManyToOne
    WebUser blockedUser;
    @Temporal(TemporalType.TIMESTAMP)
    Date blockedDate;
    String blockedComment;
    //Hold Properties
    private boolean hold;
    @ManyToOne
    private WebUser holdUser;
    @Temporal(TemporalType.TIMESTAMP)
    private Date holdDate;
    private String holdComment;
    @ManyToOne
    Institution bankBranch;
    String accountNo;
    @ManyToOne
    Institution epfBankBranch;
    String epfBankAccount;
    private double transAdvanceSalary;
    private boolean holdPaid;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date holdPaidAt;
    @ManyToOne
    private WebUser holdPaidBy;
    @ManyToOne
    Institution chequeBank;    
    @Temporal(javax.persistence.TemporalType.DATE)
    Date chequeDate;
    String chequeNo;

    public String getChequeNo() {
        return chequeNo;
    }

    public void setChequeNo(String chequeNo) {
        this.chequeNo = chequeNo;
    }
    
    
    public Institution getChequeBank() {
        return chequeBank;
    }

    public void setChequeBank(Institution chequeBank) {
        this.chequeBank = chequeBank;
    }

    public Date getChequeDate() {
        return chequeDate;
    }

    public void setChequeDate(Date chequeDate) {
        this.chequeDate = chequeDate;
    }
    

    public Institution getEpfBankBranch() {
        return epfBankBranch;
    }

    public void setEpfBankBranch(Institution epfBankBranch) {
        this.epfBankBranch = epfBankBranch;
    }

    public String getEpfBankAccount() {
        return epfBankAccount;
    }

    public void setEpfBankAccount(String epfBankAccount) {
        this.epfBankAccount = epfBankAccount;
    }

    public Institution getBankBranch() {
        return bankBranch;
    }

    public void setBankBranch(Institution bankBranch) {
        this.bankBranch = bankBranch;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public WebUser getBlockedUser() {
        return blockedUser;
    }

    public void setBlockedUser(WebUser blockedUser) {
        this.blockedUser = blockedUser;
    }

    public Date getBlockedDate() {
        return blockedDate;
    }

    public void setBlockedDate(Date blockedDate) {
        this.blockedDate = blockedDate;
    }

    public String getBlockedComment() {
        return blockedComment;
    }

    public void setBlockedComment(String blockedComment) {
        this.blockedComment = blockedComment;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public double getLateNoPayCount() {
        return lateNoPayCount;
    }

    public void setLateNoPayCount(double lateNoPayCount) {
        this.lateNoPayCount = lateNoPayCount;
    }

    public double getLateNoPayBasicValue() {
        return lateNoPayBasicValue;
    }

    public void setLateNoPayBasicValue(double lateNoPayBasicValue) {
        this.lateNoPayBasicValue = lateNoPayBasicValue;
    }

    public double getLateNoPayAllovanceValue() {
        return lateNoPayAllovanceValue;
    }

    public void setLateNoPayAllovanceValue(double lateNoPayAllovanceValue) {
        this.lateNoPayAllovanceValue = lateNoPayAllovanceValue;
    }

    public double getOverTimeRatePerMinute() {
        return overTimeRatePerMinute;
    }

    public void setOverTimeRatePerMinute(double overTimeRatePerMinute) {
        this.overTimeRatePerMinute = overTimeRatePerMinute;
    }

    public double getTransExtraDutyValue() {
        return roundOff(extraDutyNormalValue + extraDutyMerchantileValue + extraDutyPoyaValue + extraDutyDayOffValue + extraDutySleepingDayValue);
    }

    public double getOverTimeMinute() {
        return overTimeMinute;
    }

    public void setOverTimeMinute(double overTimeMinute) {
        this.overTimeMinute = overTimeMinute;
    }

    public double getNoPayCount() {
        return noPayCount;
    }

    public void setNoPayCount(double noPayCount) {
        this.noPayCount = noPayCount;
    }

    public double getMerchantileCount() {
        return merchantileCount;
    }

    public void setMerchantileCount(double merchantileCount) {
        this.merchantileCount = merchantileCount;
    }

    public double getPoyaCount() {
        return poyaCount;
    }

    public void setPoyaCount(double poyaCount) {
        this.poyaCount = poyaCount;
    }

    public double getDayOffCount() {
        return dayOffCount;
    }

    public void setDayOffCount(double dayOffCount) {
        this.dayOffCount = dayOffCount;
    }

    public double getSleepingDayCount() {
        return sleepingDayCount;
    }

    public void setSleepingDayCount(double sleepingDayCount) {
        this.sleepingDayCount = sleepingDayCount;
    }

    public double getDayOffAllowance() {
        return dayOffAllowance;
    }

    public void setDayOffAllowance(double dayOffAllowance) {
        this.dayOffAllowance = dayOffAllowance;
    }

    public double getBasicRatePerMinute() {
        return basicRatePerMinute;
    }

    public void setBasicRatePerMinute(double basicRatePerMinute) {
        this.basicRatePerMinute = basicRatePerMinute;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public double getSleepingDayAllowance() {
        return sleepingDayAllowance;
    }

    public void setSleepingDayAllowance(double sleepingDayAllowance) {
        this.sleepingDayAllowance = sleepingDayAllowance;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public double getBrValue() {
        return brValue;
    }

    public void setBrValue(double brValue) {
        this.brValue = brValue;
    }

    public double getComponentValueAddition() {
        return componentValueAddition;
    }

    public void setComponentValueAddition(double componentValueAddition) {
        this.componentValueAddition = componentValueAddition;
    }

    public double getComponentValueSubstraction() {
        return componentValueSubstraction;
    }

    public void setComponentValueSubstraction(double componentValueSubstraction) {
        this.componentValueSubstraction = componentValueSubstraction;
    }

    public Date getFromDate() {
        return getSalaryCycle() != null ? getSalaryCycle().getSalaryFromDate() : null;
    }

    public Date getToDate() {
        return getSalaryCycle() != null ? getSalaryCycle().getSalaryToDate() : null;
    }

    @XmlTransient
    public List<StaffSalaryComponant> getStaffSalaryComponants() {
        if (staffSalaryComponants == null) {
            staffSalaryComponants = new ArrayList<>();
        }
        return staffSalaryComponants;
    }

    public void setStaffSalaryComponants(List<StaffSalaryComponant> staffSalaryComponants) {
        this.staffSalaryComponants = staffSalaryComponants;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof StaffSalary)) {
            return false;
        }
        StaffSalary other = (StaffSalary) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.hr.StaffSalary[ id=" + id + " ]";
    }

    public double getPayeeValue() {
        return payeeValue;
    }

    public void setPayeeValue(double payeeValue) {
        this.payeeValue = payeeValue;
    }

    public void calcualteEpfAndEtf() {
        etfCompanyValue = 0.0;
        etfSatffValue = 0;
        epfCompanyValue = 0;
        epfStaffValue = 0;
        for (StaffSalaryComponant spc : getStaffSalaryComponants()) {
            etfCompanyValue += spc.getEtfCompanyValue();
            etfSatffValue += spc.getEtfValue();
            epfCompanyValue += spc.getEpfCompanyValue();
            epfStaffValue += spc.getEpfValue();
        }

    }

    public double getTransGrossSalary() {
        return roundOff(basicValue
                + merchantileAllowanceValue
                + poyaAllowanceValue
                + dayOffAllowance
                + sleepingDayAllowance
                + adjustmentToBasic);
    }

    public double getTransEpfEtfDiductableSalary() {
        return roundOff(getTransGrossSalary() + noPayValueBasic);
    }

    private double roundOff(double d) {
        DecimalFormat newFormat = new DecimalFormat("#.##");
        try {
            return Double.valueOf(newFormat.format(d));
        } catch (Exception e) {
            return 0;
        }
    }

    public void calculateComponentTotal() {
        basicValue = 0;
        brValue = 0;
        overTimeValue = 0;
        extraDutyNormalValue = 0;
        extraDutyMerchantileValue = 0;
        extraDutyPoyaValue = 0;
        extraDutyDayOffValue = 0;
        extraDutySleepingDayValue = 0;
        noPayValueBasic = 0;
        noPayValueAllowance = 0;
        poyaAllowanceValue = 0;
        merchantileAllowanceValue = 0;
        dayOffAllowance = 0;
        sleepingDayAllowance = 0;
        adjustmentToBasic = 0;
        adjustmentToAllowance = 0;
        componentValueAddition = 0;
        componentValueSubstraction = 0;

        for (StaffSalaryComponant spc : getStaffSalaryComponants()) {
            PaysheetComponentType paysheetComponentType = null;
            double value = 0;

            if (spc.getStaffPaysheetComponent() != null
                    && spc.getStaffPaysheetComponent().getPaysheetComponent() != null) {
                paysheetComponentType = spc.getStaffPaysheetComponent().getPaysheetComponent().getComponentType();
            }

            if (paysheetComponentType != null) {

                //if (paysheetComponentType.getParent(paysheetComponentType) == PaysheetComponentType.addition) {
                value = roundOff(spc.getComponantValue());
//                } else {
//                    value = 0 - spc.getComponantValue();
//                }

                switch (paysheetComponentType) {
                    case BasicSalary:
                        basicValue += value;
                        brValue += spc.getStaffPaysheetComponent().getDblValue();
                        break;
                    case OT:
                        overTimeValue += value;
                        break;
                    case ExtraDutyNormal:
                        extraDutyNormalValue += value;
                        break;
                    case ExtraDutyMerchantile:
                        extraDutyMerchantileValue += value;
                        break;
                    case ExtraDutyPoya:
                        extraDutyPoyaValue += value;
                        break;
                    case ExtraDutyDayOff:
                        extraDutyDayOffValue += value;
                        break;
                    case ExtraDutySleepingDay:
                        extraDutySleepingDayValue += value;
                        break;
                    case No_Pay_Deduction_Basic:
                        noPayValueBasic += value;
                        break;
                    case No_Pay_Deduction_Allowance:
                        noPayValueAllowance += value;
                        break;
                    case MerchantileAllowance:
                        merchantileAllowanceValue += value;
                        break;
                    case PoyaAllowance:
                        poyaAllowanceValue += value;
                        break;
                    case DayOffAllowance:
                        dayOffAllowance += value;
                        break;
                    case SleepingDayAllowance:
                        sleepingDayAllowance += value;
                        break;
                    case AdjustmentAllowanceAdd:
                    case AdjustmentAllowanceSub:
                        adjustmentToAllowance += value;
                        break;
                    case AdjustmentBasicAdd:
                    case AdjustmentBasicSub:
                        adjustmentToBasic += value;
                        break;
                    default:
                        if (paysheetComponentType.getParent(paysheetComponentType) == PaysheetComponentType.addition) {
                            componentValueAddition += value;
                        } else {
                            componentValueSubstraction += value;
                        }
                }

            }

        }

    }

    public double getTransTotalAllowance() {
        return roundOff(componentValueAddition + adjustmentToAllowance + noPayValueAllowance);
    }

    

    public double getTransTotalDeduction() {
        return roundOff(componentValueSubstraction + noPayValueBasic + noPayValueAllowance + epfStaffValue);
    }

    public double getTransNetSalry() {
        return roundOff(getTransGrossSalary() + getTransTotalAllowance() + getTransTotalDeduction());
    }

    public SalaryCycle getSalaryCycle() {
//        if (salaryCycle == null) {
//            salaryCycle = new SalaryCycle();
//        }
        return salaryCycle;
    }

    public void setSalaryCycle(SalaryCycle salaryCycle) {

        this.salaryCycle = salaryCycle;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
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
//
//    public double getPhValue() {
//        return phValue;
//    }
//
//    public void setPhValue(double phValue) {
//        this.phValue = phValue;
//    }
//

    public boolean isExist() {
        return exist;
    }

    public boolean getExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public double getBasicValue() {
        return basicValue;
    }

    public void setBasicValue(double basicValue) {
        this.basicValue = basicValue;
    }

    public double getOverTimeValue() {
        return overTimeValue;
    }

    public void setOverTimeValue(double overTimeValue) {
        this.overTimeValue = overTimeValue;
    }

    public double getNoPayValueBasic() {
        return noPayValueBasic;
    }

    public void setNoPayValueBasic(double noPayValueBasic) {
        this.noPayValueBasic = noPayValueBasic;
    }

    public double getNoPayValueAllowance() {
        return noPayValueAllowance;
    }

    public void setNoPayValueAllowance(double noPayValueAllowance) {
        this.noPayValueAllowance = noPayValueAllowance;
    }

    public double getAdjustmentToBasic() {
        return adjustmentToBasic;
    }

    public void setAdjustmentToBasic(double adjustmentToBasic) {
        this.adjustmentToBasic = adjustmentToBasic;
    }

    public double getAdjustmentToAllowance() {
        return adjustmentToAllowance;
    }

    public void setAdjustmentToAllowance(double adjustmentToAllowance) {
        this.adjustmentToAllowance = adjustmentToAllowance;
    }

    public double getEtfSatffValue() {
        return etfSatffValue;
    }

    public void setEtfSatffValue(double etfSatffValue) {
        this.etfSatffValue = etfSatffValue;
    }

    public double getEtfCompanyValue() {
        return etfCompanyValue;
    }

    public void setEtfCompanyValue(double etfCompanyValue) {
        this.etfCompanyValue = etfCompanyValue;
    }

    public double getEpfStaffValue() {
        return epfStaffValue;
    }

    public void setEpfStaffValue(double epfStaffValue) {
        this.epfStaffValue = epfStaffValue;
    }

    public double getEpfCompanyValue() {
        return epfCompanyValue;
    }

    public void setEpfCompanyValue(double epfCompanyValue) {
        this.epfCompanyValue = epfCompanyValue;
    }

    public List<StaffSalaryComponant> getTransStaffSalaryComponantsAddition() {
        if (transStaffSalaryComponantsAddition == null) {
            transStaffSalaryComponantsAddition = new ArrayList<>();
        }
        return transStaffSalaryComponantsAddition;
    }

    public void setTransStaffSalaryComponantsAddition(List<StaffSalaryComponant> transStaffSalaryComponantsAddition) {
        this.transStaffSalaryComponantsAddition = transStaffSalaryComponantsAddition;
    }

    public List<StaffSalaryComponant> getTransStaffSalaryComponantsSubtraction() {
        if (transStaffSalaryComponantsSubtraction == null) {
            transStaffSalaryComponantsSubtraction = new ArrayList<>();
        }
        return transStaffSalaryComponantsSubtraction;
    }

    public void setTransStaffSalaryComponantsSubtraction(List<StaffSalaryComponant> transStaffSalaryComponantsSubtraction) {
        this.transStaffSalaryComponantsSubtraction = transStaffSalaryComponantsSubtraction;
    }

    public double getMerchantileAllowanceValue() {
        return merchantileAllowanceValue;
    }

    public void setMerchantileAllowanceValue(double merchantileAllowanceValue) {
        this.merchantileAllowanceValue = merchantileAllowanceValue;
    }

    public double getPoyaAllowanceValue() {
        return poyaAllowanceValue;
    }

    public void setPoyaAllowanceValue(double poyaAllowanceValue) {
        this.poyaAllowanceValue = poyaAllowanceValue;
    }

    public double getExtraDutyNormalMinute() {
        return extraDutyNormalMinute;
    }

    public void setExtraDutyNormalMinute(double extraDutyNormalMinute) {
        this.extraDutyNormalMinute = extraDutyNormalMinute;
    }

    public double getExtraDutyMerchantileMinute() {
        return extraDutyMerchantileMinute;
    }

    public void setExtraDutyMerchantileMinute(double extraDutyMerchantileMinute) {
        this.extraDutyMerchantileMinute = extraDutyMerchantileMinute;
    }

    public double getExtraDutyPoyaMinute() {
        return extraDutyPoyaMinute;
    }

    public void setExtraDutyPoyaMinute(double extraDutyPoyaMinute) {
        this.extraDutyPoyaMinute = extraDutyPoyaMinute;
    }

    public double getExtraDutyDayOffMinute() {
        return extraDutyDayOffMinute;
    }

    public void setExtraDutyDayOffMinute(double extraDutyDayOffMinute) {
        this.extraDutyDayOffMinute = extraDutyDayOffMinute;
    }

    public double getExtraDutySleepingDayMinute() {
        return extraDutySleepingDayMinute;
    }

    public void setExtraDutySleepingDayMinute(double extraDutySleepingDayMinute) {
        this.extraDutySleepingDayMinute = extraDutySleepingDayMinute;
    }

    public double getExtraDutyNormalValue() {
        return extraDutyNormalValue;
    }

    public void setExtraDutyNormalValue(double extraDutyNormalValue) {
        this.extraDutyNormalValue = extraDutyNormalValue;
    }

    public double getExtraDutyMerchantileValue() {
        return extraDutyMerchantileValue;
    }

    public void setExtraDutyMerchantileValue(double extraDutyMerchantileValue) {
        this.extraDutyMerchantileValue = extraDutyMerchantileValue;
    }

    public double getExtraDutyPoyaValue() {
        return extraDutyPoyaValue;
    }

    public void setExtraDutyPoyaValue(double extraDutyPoyaValue) {
        this.extraDutyPoyaValue = extraDutyPoyaValue;
    }

    public double getExtraDutyDayOffValue() {
        return extraDutyDayOffValue;
    }

    public void setExtraDutyDayOffValue(double extraDutyDayOffValue) {
        this.extraDutyDayOffValue = extraDutyDayOffValue;
    }

    public double getExtraDutySleepingDayValue() {
        return extraDutySleepingDayValue;
    }

    public void setExtraDutySleepingDayValue(double extraDutySleepingDayValue) {
        this.extraDutySleepingDayValue = extraDutySleepingDayValue;
    }

    public boolean isHold() {
        return hold;
    }

    public void setHold(boolean hold) {
        this.hold = hold;
    }

    public WebUser getHoldUser() {
        return holdUser;
    }

    public void setHoldUser(WebUser holdUser) {
        this.holdUser = holdUser;
    }

    public Date getHoldDate() {
        return holdDate;
    }

    public void setHoldDate(Date holdDate) {
        this.holdDate = holdDate;
    }

    public String getHoldComment() {
        return holdComment;
    }

    public void setHoldComment(String holdComment) {
        this.holdComment = holdComment;
    }

    public double getTransAdvanceSalary() {
        return transAdvanceSalary;
    }

    public void setTransAdvanceSalary(double transAdvanceSalary) {
        this.transAdvanceSalary = transAdvanceSalary;
    }

    public boolean isHoldPaid() {
        return holdPaid;
    }

    public void setHoldPaid(boolean holdPaid) {
        this.holdPaid = holdPaid;
    }

    public Date getHoldPaidAt() {
        return holdPaidAt;
    }

    public void setHoldPaidAt(Date holdPaidAt) {
        this.holdPaidAt = holdPaidAt;
    }

    public WebUser getHoldPaidBy() {
        return holdPaidBy;
    }

    public void setHoldPaidBy(WebUser holdPaidBy) {
        this.holdPaidBy = holdPaidBy;
    }

}
