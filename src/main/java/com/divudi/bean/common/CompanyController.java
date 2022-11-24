/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package com.divudi.bean.common;

import com.divudi.data.DepartmentType;
import com.divudi.entity.Department;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.util.JsfUtil;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;

/**
 *
 * @author Buddhika
 */
@Named
@SessionScoped
public class CompanyController implements Serializable {

    @Inject
    WebUserController webUserController;

    @Inject
    SessionController sessionController;

    @EJB
    DepartmentFacade departmentFacade;

    private List<Department> manufacturers;
    private List<Department> distributors;
    private List<Department> customers;
    private List<Department> banks;
    private List<Department> routes;

    private Department selectedDepartment;

    /**
     * Creates a new instance of CompanyController
     */
    public CompanyController() {
    }

    public String toViewManufacturer() {
        if (selectedDepartment == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selectedDepartment.getDepartmentType() != DepartmentType.Manufacturer) {
            JsfUtil.addErrorMessage("Wrong Selection");
            return "";
        }
        return "/company/manufacturer";
    }

    public String toViewDistributor() {
        if (selectedDepartment == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selectedDepartment.getDepartmentType() != DepartmentType.Distributor) {
            JsfUtil.addErrorMessage("Wrong Selection");
            return "";
        }
        return "/company/distributor";
    }

    public String toViewCustomer() {
        if (selectedDepartment == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selectedDepartment.getDepartmentType() != DepartmentType.Customer) {
            JsfUtil.addErrorMessage("Wrong Selection");
            return "";
        }
        return "/company/customer";
    }

    public String toViewBank() {
        if (selectedDepartment == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selectedDepartment.getDepartmentType() != DepartmentType.Bank) {
            JsfUtil.addErrorMessage("Wrong Selection");
            return "";
        }
        return "/company/bank";
    }

    public String toViewRoute() {
        if (selectedDepartment == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (selectedDepartment.getDepartmentType() != DepartmentType.Route) {
            JsfUtil.addErrorMessage("Wrong Selection");
            return "";
        }
        return "/company/route";
    }
    
    
    public void saveDepartment(Department dept){
        if(dept==null){
            JsfUtil.addErrorMessage("Nothing to save");
            return;
        }
        if(dept.getId()==null){
            departmentFacade.create(dept);
            JsfUtil.addSuccessMessage("Saved");
        }else{
             departmentFacade.edit(dept);
            JsfUtil.addSuccessMessage("Updated");
        }
    }
    
    public String saveDepartment() {
        if (selectedDepartment == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        saveDepartment(selectedDepartment);
        switch (selectedDepartment.getDepartmentType()) {
            case Bank:
                return toListBanks();
            case Customer:
                return toListCustomers();
            case Distributor:
                return toListDistributors();
            case Manufacturer:
                return toListManufacturers();
            case Route:
                return toListRoutes();

        }
        return "";
    }

    public String removeSelectedDepartment() {
        if (selectedDepartment == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        selectedDepartment.setRetired(true);
        selectedDepartment.setRetirer(sessionController.getLoggedUser());
        departmentFacade.edit(selectedDepartment);
        switch (selectedDepartment.getDepartmentType()) {
            case Bank:
                return toListBanks();
            case Customer:
                return toListCustomers();
            case Distributor:
                return toListDistributors();
            case Manufacturer:
                return toListManufacturers();
            case Route:
                return toListRoutes();

        }
        return "";
    }

    public String toAddNewManufacturer() {
        selectedDepartment = new Department();
        selectedDepartment.setInstitution(sessionController.getInstitution());
        selectedDepartment.setDepartmentType(DepartmentType.Manufacturer);
        return "/company/manufacturer";
    }

    public String toAddNewDistributor() {
        selectedDepartment = new Department();
        selectedDepartment.setInstitution(sessionController.getInstitution());
        selectedDepartment.setDepartmentType(DepartmentType.Distributor);
        return "/company/distributor";
    }

    public String toAddNewBank() {
        selectedDepartment = new Department();
        selectedDepartment.setInstitution(sessionController.getInstitution());
        selectedDepartment.setDepartmentType(DepartmentType.Bank);
        return "/company/bank";
    }

    public String toAddNewCustomer() {
        selectedDepartment = new Department();
        selectedDepartment.setInstitution(sessionController.getInstitution());
        selectedDepartment.setDepartmentType(DepartmentType.Customer);
        return "/company/customer";
    }

    public String toAddNewRoute() {
        selectedDepartment = new Department();
        selectedDepartment.setInstitution(sessionController.getInstitution());
        selectedDepartment.setDepartmentType(DepartmentType.Route);
        return "/company/route";
    }

    public String toListManufacturers() {
        manufacturers = fillDepartment(DepartmentType.Manufacturer);
        return "/company/manufacturers";
    }

    public String toListRoutes() {
        routes = fillDepartment(DepartmentType.Route);
        return "/company/routes";
    }

    public String toListBanks() {
        banks = fillDepartment(DepartmentType.Bank);
        return "/company/banks";
    }

    public String toListDistributors() {
        distributors = fillDepartment(DepartmentType.Distributor);
        return "/company/distributors";
    }

    public String toListCustomers() {
        customers = fillDepartment(DepartmentType.Customer);
        return "/company/customers";
    }

    public List<Department> fillDepartment(DepartmentType type) {
        String j = "select d "
                + " from Department d "
                + " where d.retired=:ret ";
        Map m = new HashMap();
        m.put("ret", false);
        if (type != null) {
            j += " and d.departmentType=:type ";
            m.put("type", type);
        }
        return departmentFacade.findBySQL(j, m);
    }

    public String toManageUsersIndex() {
        return "/company/manage_users";
    }

    public String toManageInstitutionIndex() {
        return "/company/manage_institutions";
    }

    public String toListUsers() {
        webUserController.fillUsersForCompanyAdmin();
        return "/company/view_users";
    }

    public String toAddNewUser() {
        webUserController.prepairAddNewUserForCompanyAdmin();
        return "/company/add_new_user";
    }

    public String toUserLogins() {
        return "/company/user_logins";
    }

    public String toEditUser() {
        return "/company/edit_user";
    }

    public String toChangePassword() {
        return "/company/change_password";
    }

    public String saveNewUser() {
        webUserController.saveNewUserForCompanyAdmin();
        JsfUtil.addSuccessMessage("New User Added");
        return toListUsers();
    }

    public String updateUser() {
        webUserController.updateWebUserForCompanyAdmin();
        JsfUtil.addSuccessMessage("User Updated");
        return toListUsers();
    }

    public String updatePassword() {
        webUserController.changePasswordForCompanyAdmin();
        JsfUtil.addSuccessMessage("Password Updated");
        return toListUsers();
    }

    public String removeUser() {
        webUserController.delete();
        JsfUtil.addSuccessMessage("User Deleted");
        return toListUsers();
    }

    public List<Department> getManufacturers() {
        return manufacturers;
    }

    public void setManufacturers(List<Department> manufacturers) {
        this.manufacturers = manufacturers;
    }

    public List<Department> getDistributors() {
        return distributors;
    }

    public void setDistributors(List<Department> distributors) {
        this.distributors = distributors;
    }

    public List<Department> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Department> customers) {
        this.customers = customers;
    }

    public List<Department> getBanks() {
        return banks;
    }

    public void setBanks(List<Department> banks) {
        this.banks = banks;
    }

    public List<Department> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Department> routes) {
        this.routes = routes;
    }

    public Department getSelectedDepartment() {
        return selectedDepartment;
    }

    public void setSelectedDepartment(Department selectedDepartment) {
        this.selectedDepartment = selectedDepartment;
    }
    
    

}
