package com.divudi.bean.common;

import com.divudi.data.HistoryType;
import com.divudi.data.InstitutionType;
import com.divudi.entity.AgentHistory;
import com.divudi.entity.Institution;
import com.divudi.facade.AgentHistoryFacade;
import com.divudi.facade.InstitutionFacade;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, MSc, MD(Health Informatics) Acting
 * Consultant (Health Informatics)
 */
@Named
@SessionScoped
public class InstitutionController implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Managed Beans
     */
    @Inject
    SessionController sessionController;
    @Inject
    CommonController commonController;
    /**
     * EJBs
     */
    @EJB
    private InstitutionFacade ejbFacade;
    @EJB
    AgentHistoryFacade agentHistoryFacade;
    /**
     * Properties
     */
    List<Institution> selectedItems;
    List<Institution> selectedAgencies;
    private Institution current;
    Institution agency;
    private List<Institution> items = null;
    private List<Institution> itemsToRemove = null;
    private List<Institution> companies = null;
    private List<Institution> creditCompanies = null;
    private List<Institution> banks = null;
    private List<Institution> suppliers = null;
    private List<Institution> agencies = null;
    List<Institution> collectingCentre = null;
    List<Institution> institution;
    String selectText = "";
    private Boolean codeDisabled = false;

    public List<Institution> getSelectedItems() {
        if (selectText.trim().equals("")) {
            selectedItems = completeInstitution(null, InstitutionType.values());
        } else {
            selectedItems = completeInstitution(selectText, InstitutionType.values());
        }
        return selectedItems;
    }

    public void fetchSelectedAgencys() {
        InstitutionType[] types = {InstitutionType.Agency};
        if (selectText.trim().equals("")) {
            selectedAgencies = completeInstitution(null, types);
        } else {
            selectedAgencies = completeInstitution(selectText, types);
        }
    }

    public void fetchSelectedCollectingCentre() {
        InstitutionType[] types = {InstitutionType.Distributor};
        if (selectText.trim().equals("")) {
            collectingCentre = completeInstitution(null, types);
        } else {
            collectingCentre = completeInstitution(selectText, types);
        }
    }

    public List<Institution> completeIns(String qry) {
        return completeInstitution(qry, InstitutionType.values());
    }

    public List<Institution> completeInstitution(String qry, InstitutionType[] types) {
        String sql;
        HashMap hm = new HashMap();
        sql = "select c from Institution c "
                + " where c.retired=false ";
        if (qry != null) {
            sql += " and (upper(c.name) like :qry or upper(c.institutionCode) like :qry) ";
            hm.put("qry", "%" + qry.toUpperCase() + "%");
        }
        if (types != null) {
            List<InstitutionType> lstTypes = Arrays.asList(types);
            hm.put("types", lstTypes);
            sql += "  and c.institutionType in :types";
        }
        sql += " order by c.name";
        return getFacade().findBySQL(sql, hm);
    }

    public List<Institution> getSuppliers() {
        if (suppliers == null) {
            suppliers = completeInstitution(null, InstitutionType.Dealer);
        }
        return suppliers;
    }

    public void setSuppliers(List<Institution> suppliers) {
        this.suppliers = suppliers;
    }

    public List<Institution> getCollectingCentre() {
        if (collectingCentre == null) {
            collectingCentre = completeInstitution(null, InstitutionType.Distributor);
        }

        return collectingCentre;
    }

    public void setCollectingCentre(List<Institution> collectingCentre) {
        this.collectingCentre = collectingCentre;
    }

    public List<Institution> getAgencies() {
        Date startTime = new Date();
        Date fromDate = null;
        Date toDate = null;

        if (selectText.trim().equals("")) {
            agencies = completeInstitution(null, InstitutionType.Agency);
        } else {
            agencies = completeInstitution(selectText, InstitutionType.Agency);
        }

        commonController.printReportDetails(fromDate, toDate, startTime, "Channeling/Reports/Income report/Agent Reports/Agent details(/faces/channel/channel_report_agent_details.xhtml)");

        return agencies;
    }

    public void setAgencies(List<Institution> agencies) {
        this.agencies = agencies;
    }

    public List<Institution> completeInstitution(String qry, InstitutionType type) {
        InstitutionType[] types = new InstitutionType[]{type};
        return completeInstitution(qry, types);
    }

    public List<Institution> completeCompany(String qry) {
        return completeInstitution(qry, InstitutionType.Company);
    }

    public List<Institution> completeCollectingCenter(String qry) {
        return completeInstitution(qry, InstitutionType.Distributor);
    }

    public List<Institution> completeAgency(String qry) {
        return completeInstitution(qry, InstitutionType.Agency);
    }

    public List<Institution> completeBank(String qry) {
        return completeInstitution(qry, InstitutionType.Bank);
    }

    public List<Institution> completeBankBranch(String qry) {
        return completeInstitution(qry, InstitutionType.branch);
    }

    public List<Institution> completeCreditCompany(String qry) {
        return completeInstitution(qry, InstitutionType.Customer);
    }

    public List<Institution> completeSuppliers(String qry) {
        return completeInstitution(qry, new InstitutionType[]{InstitutionType.Dealer, InstitutionType.StoreDealor});
    }

    public List<Institution> getCreditCompanies() {
        if (creditCompanies == null) {
            creditCompanies = completeInstitution(null, InstitutionType.Customer);
        }
        return creditCompanies;
    }

    public List<Institution> getCompanies() {
        if (companies == null) {
            companies = completeInstitution(null, InstitutionType.Company);
        }
        return companies;
    }

    public List<Institution> getBanks() {
        if (banks == null) {
            banks = completeInstitution(null, InstitutionType.Bank);
        }
        return banks;
    }

//    public List<Institution> getCollectingCenter() {
//        if (banks == null) {
//            banks = completeInstitution(null, InstitutionType.Distributor);
//        }
//        return banks;
//    }
    public Institution getInstitutionByName(String name, InstitutionType type) {
        String sql;
        Map m = new HashMap();
        m.put("n", name.toUpperCase());
        m.put("t", type);
        sql = "select i from Institution i where upper(i.name) =:n and i.institutionType=:t";
        Institution i = getFacade().findFirstBySQL(sql, m);
        if (i == null) {
            i = new Institution();
            i.setName(name);
            i.setInstitutionType(type);
            i.setCreatedAt(Calendar.getInstance().getTime());
            i.setCreater(getSessionController().getLoggedUser());
            getFacade().create(i);
        } else {
            i.setRetired(false);
            getFacade().edit(i);
        }
        return i;
    }

    private Boolean checkCodeExist() {
        String sql = "SELECT i FROM Institution i where i.retired=false and i.institutionCode is not null ";
        List<Institution> ins = getEjbFacade().findBySQL(sql);
        if (ins != null) {
            for (Institution i : ins) {
                if (i.getInstitutionCode() == null || i.getInstitutionCode().trim().equals("")) {
                    continue;
                }
                if (i.getInstitutionCode() != null && i.getInstitutionCode().equals(getCurrent().getInstitutionCode())) {
                    UtilityController.addErrorMessage("Insituion Code Already Exist Try another Code");
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean checkCodeExistAgency() {
        String sql = "SELECT i FROM Institution i where i.retired=false and i.institutionCode is not null ";
        List<Institution> ins = getEjbFacade().findBySQL(sql);
        if (ins != null) {
            for (Institution i : ins) {
                if (i.getInstitutionCode() == null || i.getInstitutionCode().trim().equals("")) {
                    continue;
                }
                if (i.getInstitutionCode() != null && i.getInstitutionCode().equals(getAgency().getInstitutionCode())) {
                    UtilityController.addErrorMessage("Insituion Code Already Exist Try another Code");
                    return true;
                }
            }
        }
        return false;
    }

    public void prepareAdd() {
        codeDisabled = false;
        current = new Institution();
    }

    public void prepareAddAgency() {
        codeDisabled = false;
        agency = new Institution();
        agency.setInstitutionType(InstitutionType.Agency);
    }

    public void setSelectedItems(List<Institution> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public String getSelectText() {
        return selectText;
    }

    private void recreateModel() {
        items = null;
        agencies = null;
        suppliers = null;
        companies = null;
        creditCompanies = null;
        banks = null;
        suppliers = null;
        agencies = null;

    }

    public void saveSelected() {
        if (getCurrent().getInstitutionType() == null) {
            UtilityController.addErrorMessage("Select Instituion Type");
            return;
        }

        if (getCurrent().getId() != null && getCurrent().getId() > 0) {

            if (getCurrent().getInstitutionCode() != null) {
                getCurrent().setInstitutionCode(getCurrent().getInstitutionCode());
            }
            getFacade().edit(getCurrent());
            UtilityController.addSuccessMessage("Updated Successfully.");
        } else {
            if (getCurrent().getInstitutionCode() != null) {
                if (!checkCodeExist()) {
                    getCurrent().setInstitutionCode(getCurrent().getInstitutionCode());

                } else {
                    return;
                }
            }
            getCurrent().setCreatedAt(new Date());
            getCurrent().setCreater(getSessionController().getLoggedUser());
            getFacade().create(getCurrent());
            UtilityController.addSuccessMessage("Saved Successfully");
        }
        recreateModel();
        getItems();
    }

    public void saveSelectedAgency() {
        if (getAgency().getInstitutionType() == null) {
            UtilityController.addErrorMessage("Select Instituion Type");
            return;
        }

        if (getAgency().getId() != null && getAgency().getId() > 0) {

            if (getAgency().getInstitutionCode() != null) {
                getAgency().setInstitutionCode(getAgency().getInstitutionCode());
            }
            getFacade().edit(getAgency());
            UtilityController.addSuccessMessage("Updated Successfully.");
        } else {
            if (getAgency().getInstitutionCode() != null) {
                if (!checkCodeExistAgency()) {
                    getAgency().setInstitutionCode(getAgency().getInstitutionCode());

                } else {
                    return;
                }
            }
            getAgency().setCreatedAt(new Date());
            getAgency().setCreater(getSessionController().getLoggedUser());
            getFacade().create(getAgency());
            UtilityController.addSuccessMessage("Saved Successfully");
        }
        recreateModel();
        fetchSelectedAgencys();
    }

    public void updateAgentCreditLimit() {
        updateCreditLimit(HistoryType.AgentBalanceUpdateBill);
    }

    public void updateCollectingCentreCreditLimit() {
        updateCreditLimit(HistoryType.CollectingCentreBalanceUpdateBill);
    }

    public void updateCreditLimit(HistoryType historyType) {
        if (current == null || current.getId() == null) {
            UtilityController.addErrorMessage("Please Select a Agency");
            return;
        }

        if (current.getMaxCreditLimit() == 0.0) {
            UtilityController.addErrorMessage("Please Enter Maximum Credit Limit.");
            return;
        }

        Institution i = getFacade().find(current.getId());
        double mcl = i.getMaxCreditLimit();
        //// // System.out.println("mcl = " + mcl);
        double acl = i.getAllowedCredit();
        double scl = i.getStandardCreditLimit();

        if (current.getStandardCreditLimit() > current.getAllowedCredit()) {
            UtilityController.addErrorMessage("Allowed Credit Limit must Grater Than or Equal To Standard Credit Limit");
            return;
        }

        if (current.getMaxCreditLimit() < current.getAllowedCredit()) {
            UtilityController.addErrorMessage("Allowed Credit Limit must Less Than Maximum Credit Limit");
            return;
        }

        if ((current.getStandardCreditLimit() == scl) && (current.getAllowedCredit() == acl) && (current.getMaxCreditLimit() == mcl)) {
            UtilityController.addErrorMessage("Nothing To Update");
            return;
        }

        if (current.getStandardCreditLimit() != scl) {
            createAgentCreditLimitUpdateHistory(current, scl, current.getStandardCreditLimit(), historyType, "Standard Credit Limit");
            UtilityController.addSuccessMessage("Standard Credit Limit Updated");
        }

        if (current.getAllowedCredit() != acl) {
            createAgentCreditLimitUpdateHistory(current, acl, current.getAllowedCredit(), historyType, "Allowed Credit Limit");
            UtilityController.addSuccessMessage("Allowed Credit Limit Updated");
        }

        if (current.getMaxCreditLimit() != mcl) {
            createAgentCreditLimitUpdateHistory(current, mcl, current.getMaxCreditLimit(), historyType, "Max Credit Limit");
            UtilityController.addSuccessMessage("Max Credit Limit Updated");
        }
        getFacade().edit(current);

        switch (historyType) {
            case AgentBalanceUpdateBill:
                getAgencies();
                break;
            case CollectingCentreBalanceUpdateBill:
                getCollectingCentre();
                break;
        }
    }

    public void createAgentCreditLimitUpdateHistory(Institution ins, double historyValue, double transactionValue, HistoryType historyType, String comment) {
        AgentHistory agentHistory = new AgentHistory();
        agentHistory.setCreatedAt(new Date());
        agentHistory.setCreater(getSessionController().getLoggedUser());
        agentHistory.setBeforeBallance(historyValue);
        agentHistory.setTransactionValue(transactionValue);
        agentHistory.setHistoryType(historyType);
        agentHistory.setComment(comment);
        agentHistory.setInstitution(ins);
        agentHistoryFacade.create(agentHistory);
        UtilityController.addSuccessMessage("History Saved");
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public InstitutionFacade getEjbFacade() {
        return ejbFacade;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public InstitutionController() {
    }

    public Institution getCurrent() {
        if (current == null) {
            current = new Institution();
        }
        return current;
    }

    public void setCurrent(Institution current) {
        codeDisabled = true;
        this.current = current;
    }

    public void delete() {

        if (getCurrent() != null) {
            getCurrent().setRetired(true);
            getCurrent().setRetiredAt(new Date());
            getCurrent().setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(getCurrent());
            UtilityController.addSuccessMessage("Deleted Successfully");
        } else {
            UtilityController.addSuccessMessage("Nothing to Delete");
        }
        recreateModel();
        getItems();
        fetchSelectedAgencys();
        current = null;
        getCurrent();
    }

    public void deleteAgency() {

        if (getAgency() != null) {
            getAgency().setRetired(true);
            getAgency().setRetiredAt(new Date());
            getAgency().setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(getAgency());
            UtilityController.addSuccessMessage("Deleted Successfully");
        } else {
            UtilityController.addSuccessMessage("Nothing to Delete");
        }
        fetchSelectedAgencys();
        prepareAddAgency();
    }

    private InstitutionFacade getFacade() {
        return ejbFacade;
    }

    public List<Institution> getItems() {
        if (items == null) {
            String j;
            j = "select i from Institution i where i.retired=false order by i.name";
            items = getFacade().findBySQL(j);
        }
        return items;
    }

    public void formatAgentSerial() {
        InstitutionType[] types = {InstitutionType.Agency};
        selectedAgencies = completeInstitution(null, types);
        for (Institution a : selectedAgencies) {
//            //// // System.out.println("a.getInstitutionCode() = " + a.getInstitutionCode());
            DecimalFormat df = new DecimalFormat("000");
            double d = Double.parseDouble(a.getInstitutionCode());
//            //// // System.out.println("d = " + d);
            a.setInstitutionCode(df.format(d));
//            //// // System.out.println("a.getInstitutionCode() = " + a.getInstitutionCode());
            getFacade().edit(a);
        }
    }

    public Boolean getCodeDisabled() {
        return codeDisabled;
    }

    public void setCodeDisabled(Boolean codeDisabled) {
        this.codeDisabled = codeDisabled;
    }

    public InstitutionType[] getInstitutionTypes() {
        InstitutionType[] its = {InstitutionType.Company, InstitutionType.Manufacturer, InstitutionType.Distributor, InstitutionType.Customer, InstitutionType.Bank};
        return its;
    }

    public List<Institution> getItemsToRemove() {
        return itemsToRemove;
    }

    public void setItemsToRemove(List<Institution> itemsToRemove) {
        this.itemsToRemove = itemsToRemove;
    }

    public List<Institution> getInstitution() {
        return institution;
    }

    public void setInstitution(List<Institution> institution) {
        this.institution = institution;
    }

    public void removeSelectedItems() {
        for (Institution s : itemsToRemove) {
            s.setRetired(true);
            s.setRetireComments("Bulk Remove");
            s.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(s);
        }
        itemsToRemove = null;
        items = null;
    }

    public List<Institution> getSelectedAgencies() {
        if (selectedAgencies == null) {
            fetchSelectedAgencys();
        }
        return selectedAgencies;
    }

    public void setSelectedAgencies(List<Institution> selectedAgencies) {
        this.selectedAgencies = selectedAgencies;
    }

    public Institution getAgency() {
        if (agency == null) {
            agency = new Institution();
            agency.setInstitutionType(InstitutionType.Agency);
        }
        return agency;
    }

    public void setAgency(Institution agency) {
        this.agency = agency;
    }

    @FacesConverter("institutionConverter")
    public static class InstitutionConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            InstitutionController controller = (InstitutionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "institutionController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Institution) {
                Institution o = (Institution) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + InstitutionController.class.getName());
            }
        }
    }

    /**
     *
     */
    @FacesConverter(forClass = Institution.class)
    public static class InstitutionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            InstitutionController controller = (InstitutionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "institutionController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Institution) {
                Institution o = (Institution) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + InstitutionController.class.getName());
            }
        }
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public void setCommonController(CommonController commonController) {
        this.commonController = commonController;
    }

}
