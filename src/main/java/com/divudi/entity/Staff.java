/*
* Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.entity;

import com.divudi.data.IdentifiableWithNameOrCode;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author buddhika
 */
@Entity
@XmlRootElement
public class Staff implements Serializable, IdentifiableWithNameOrCode {

    
    int orderNo;
   
    static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    //Main Properties
    Long id;
    String registration;
    @Lob
    String qualification;
    String code = "";
    @ManyToOne
    Person person;
    @ManyToOne
    Speciality speciality;
    @ManyToOne
    Institution institution;
    @ManyToOne
    Department department;
    //Created Properties
    @ManyToOne
    WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date createdAt;
    //Retairing properties
    boolean retired;
    @ManyToOne
    WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    Date retiredAt;
    String retireComments;
    String staffCode;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    byte[] baImage = new byte[1];
    String fileName;
    String fileType;
    private double charge;
    //////////////////
    @OneToOne
    Department workingDepartment;
    
    @Temporal(javax.persistence.TemporalType.DATE)
    Date dateJoined;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date dateLeft;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date dateRetired;
    @Transient
    double basic;
    @Transient
    double transDblValue;
    @Transient
    double transWorkedDays;
    @Transient
    double transWorkedDaysSalaryFromToDate;
    @ManyToOne
    private Institution bankBranch;
    @ManyToOne
    private Institution epfBankBranch;
    private String epfAccountNo;
    private String accountNo;
    String epfNo;    
  

    String acNo;

//    double workingHourPerShift;
//    double leaveHour;
    double annualWelfareQualified;
    double annualWelfareUtilized;
    double workingTimeForOverTimePerWeek;
    double workingTimeForNoPayPerWeek;
    Integer codeInterger;
    boolean allowedLateInLeave = true;
    boolean allowedEarlyOutLeave = true;
    boolean withOutNotice;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date dateWithOutNotice;
    
    
    @Transient
    private String name;
    
    public double getTransDblValue() {
        return transDblValue;
    }

    public void setTransDblValue(double transDblValue) {
        this.transDblValue = transDblValue;
    }
    
    

    public Date getDateRetired() {
        return dateRetired;
    }

    public void setDateRetired(Date dateRetired) {
        this.dateRetired = dateRetired;
    }
    
    

    public boolean isAllowedLateInLeave() {
        return allowedLateInLeave;
    }

    public void setAllowedLateInLeave(boolean allowedLateInLeave) {
        this.allowedLateInLeave = allowedLateInLeave;
    }

    public boolean isAllowedEarlyOutLeave() {
        return allowedEarlyOutLeave;
    }

    public void setAllowedEarlyOutLeave(boolean allowedEarlyOutLeave) {
        this.allowedEarlyOutLeave = allowedEarlyOutLeave;
    }

    public double getWorkingTimeForNoPayPerWeek() {
        return workingTimeForNoPayPerWeek;
    }

    public void setWorkingTimeForNoPayPerWeek(double workingTimeForNoPayPerWeek) {
        this.workingTimeForNoPayPerWeek = workingTimeForNoPayPerWeek;
    }

    public double getTransWorkedDays() {
        return transWorkedDays;
    }

    public void setTransWorkedDays(double transWorkedDays) {
        this.transWorkedDays = transWorkedDays;
    }

    public double getWorkingTimeForOverTimePerWeek() {
        return workingTimeForOverTimePerWeek;
    }

    public void setWorkingTimeForOverTimePerWeek(double workingTimeForOverTimePerWeek) {
        this.workingTimeForOverTimePerWeek = workingTimeForOverTimePerWeek;
    }

    public Integer getCodeInterger() {
        return codeInterger;
    }

    public void setCodeInterger(Integer codeInterger) {
        this.codeInterger = codeInterger;
    }

    public void chageCodeToInteger() {
        if (code == null || code.isEmpty()) {
            return;
        }

        try {
            codeInterger = Integer.parseInt(code);

        } catch (Exception e) {
        }

    }

//    public double getBasic() {
//        double tmp2=0.0;
//        if (getStaffEmployment()!=null && !getStaffEmployment().getStaffBasics().isEmpty()) {
//            tmp2 = getStaffEmployment().getStaffBasics().get(getStaffEmployment().getStaffBasics().size() - 1).getStaffPaySheetComponentValue();
//        }
//        
//        return tmp2;
//    }
//    public double getLeaveHour() {
//        return leaveHour;
//    }
//
//    public void setLeaveHour(double leaveHour) {
//        this.leaveHour = leaveHour;
//    }
    public byte[] getBaImage() {
        return baImage;
    }

    public void setBaImage(byte[] baImage) {
        this.baImage = baImage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
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

        if (!(object instanceof Staff)) {
            return false;
        }
        Staff other = (Staff) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.Staff[ id=" + id + " ]";
    }

    public Person getPerson() {
        if (person == null) {
            person = new Person();
        }
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        chageCodeToInteger();
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

    public String getRegistration() {
        if(registration==null||registration.trim().equals("")){
            registration = "Consultant";
        }
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public Institution getInstitution() {
        if(institution != null){
            institution.split();
        }
        
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

   
    @Transient
    int repeatedDay;

    public int getRepeatedDay() {
        return repeatedDay;
    }

    public void setRepeatedDay(int repeatedDay) {
        if (this.repeatedDay == 0) {
            this.repeatedDay = repeatedDay;
        } else {
            this.repeatedDay = repeatedDay--;
        }

    }
    @Transient
    boolean dayOff;

    public boolean isDayOff() {
        return dayOff;
    }

    public void setDayOff(boolean dayOff) {

        this.dayOff = dayOff;

    }

    public Department getWorkingDepartment() {
        return workingDepartment;
    }

    public void setWorkingDepartment(Department workingDepartment) {
        this.workingDepartment = workingDepartment;
        this.department = workingDepartment;
    }

  

    public Date getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(Date dateJoined) {
        this.dateJoined = dateJoined;
    }

    public Date getDateLeft() {
        return dateLeft;
    }

    public void setDateLeft(Date dateLeft) {
        this.dateLeft = dateLeft;
    }

    

    @Transient
    boolean tmp;

    public boolean isTmp() {
        return tmp;
    }

    public void setTmp(boolean tmp) {
        this.tmp = tmp;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

  

    public double getBasic() {
        return basic;
    }

    public void setBasic(double basic) {
        this.basic = basic;
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

    public String getEpfNo() {
        return epfNo;
    }

    public void setEpfNo(String epfNo) {
        this.epfNo = epfNo;
    }

    
    public String getAcNo() {
        return acNo;
    }

    public void setAcNo(String acNo) {
        this.acNo = acNo;
    }

//    public double getWorkingHourPerShift() {
//        return workingHourPerShift;
//    }
//
//    public void setWorkingHourPerShift(double workingHourPerShift) {
//        this.workingHourPerShift = workingHourPerShift;
//    }
    public double getCharge() {
        return charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public double getAnnualWelfareQualified() {
        return annualWelfareQualified;
    }

    public void setAnnualWelfareQualified(double annualWelfareQualified) {
        this.annualWelfareQualified = annualWelfareQualified;
    }

    public double getAnnualWelfareUtilized() {
        return annualWelfareUtilized;
    }

    public void setAnnualWelfareUtilized(double annualWelfareUtilized) {
        this.annualWelfareUtilized = annualWelfareUtilized;
    }

    public Institution getEpfBankBranch() {
        return epfBankBranch;
    }

    public void setEpfBankBranch(Institution epfBankBranch) {
        this.epfBankBranch = epfBankBranch;
    }

    public String getEpfAccountNo() {
        return epfAccountNo;
    }

    public void setEpfAccountNo(String epfAccountNo) {
        this.epfAccountNo = epfAccountNo;
    }

    public double getTransWorkedDaysSalaryFromToDate() {
        return transWorkedDaysSalaryFromToDate;
    }

    public void setTransWorkedDaysSalaryFromToDate(double transWorkedDaysSalaryFromToDate) {
        this.transWorkedDaysSalaryFromToDate = transWorkedDaysSalaryFromToDate;
    }

    public boolean isWithOutNotice() {
        return withOutNotice;
    }

    public void setWithOutNotice(boolean withOutNotice) {
        this.withOutNotice = withOutNotice;
    }

    public Date getDateWithOutNotice() {
        return dateWithOutNotice;
    }

    public void setDateWithOutNotice(Date dateWithOutNotice) {
        this.dateWithOutNotice = dateWithOutNotice;
    }

    public String getName() {
        if(getPerson()!=null){
            name = getPerson().getNameWithTitle();
        }
        return name;
    }


    
}
