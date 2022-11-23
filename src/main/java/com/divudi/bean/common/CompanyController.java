/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package com.divudi.bean.common;

import com.divudi.facade.util.JsfUtil;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
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

    /**
     * Creates a new instance of CompanyController
     */
    public CompanyController() {
    }

    public String toManageUsersIndex() {
        return "/company/manage_users";
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

}
