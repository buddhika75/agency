/*
 * Open Hospital Management Information System
 *
 * Dr M H B Ariyaratne
 * Acting Consultant (Health Informatics)
 * (94) 71 5812399
 * (94) 71 5812399
 */
package com.divudi.bean.common;

import com.divudi.data.Dashboard;
import com.divudi.data.Privileges;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Person;
import com.divudi.entity.WebUser;
import com.divudi.facade.DepartmentFacade;
import com.divudi.facade.InstitutionFacade;
import com.divudi.facade.PersonFacade;
import com.divudi.facade.WebUserFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.primefaces.event.FlowEvent;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, MSc, MD(Health Informatics) Acting
 * Consultant (Health Informatics)
 */
@Named
@SessionScoped
public class WebUserController implements Serializable {

    /**
     * EJBs
     */
    @EJB
    private WebUserFacade ejbFacade;
    @EJB
    private PersonFacade personFacade;
    /**
     * Controllers
     */
    @Inject
    SessionController sessionController;
    @Inject
    SecurityController securityController;

    /**
     * Class Variables
     */
    List<WebUser> items;
    List<WebUser> searchItems;
    private WebUser current;
    private WebUser selected;
    String selectText = "";
    List<Department> departments;
    List<Institution> institutions;
    @EJB
    private DepartmentFacade departmentFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    private Institution institution;
    private Department department;
    private Privileges[] currentPrivilegeses;
    private List<WebUser> webUsers;
    List<WebUser> itemsToRemove;

    private String newPassword;
    private String newPasswordConfirm;


    public void removeSelectedItems() {
        for (WebUser s : itemsToRemove) {
            s.setRetired(true);
            s.setRetireComments("Bulk Remove");
            s.setRetirer(getSessionController().getLoggedUser());
            try {
                getFacade().edit(s);
            } catch (Exception e) {
            }
        }
        itemsToRemove = null;
        items = null;
    }

    public void updateWebUser(WebUser webUser) {
        personFacade.edit(webUser.getWebUserPerson());
    }

    public void updateWebUserForCompanyAdmin() {
        if(current==null){
            JsfUtil.addErrorMessage("No User Selected to Update");
            return ;
        }
        personFacade.edit(current.getWebUserPerson());
        getFacade().edit(current);
    }

    public void createWebUserDrawers() {
        String sql = "select c from WebUser c "
                + " where c.retired=false "
                + " c.drawer is not null "
                + " order by c.drawer.name,c.webUserPerson.name";
        webUsers = getFacade().findBySQL(sql);
    }

    public List<Department> getInstitutionDepatrments() {
        List<Department> d;
        if (getInstitution() == null) {
            return new ArrayList<>();
        } else {
            String sql = "Select d From Department d where d.retired=false and d.institution.id=" + getInstitution().getId();
            d = getDepartmentFacade().findBySQL(sql);
        }
        return d;
    }

    public void saveUser() {
        if (current == null) {
            return;
        }
        if (current.getId() == null || current.getId() == 0) {
            getFacade().create(current);
            UtilityController.addSuccessMessage("Saved");
        } else {
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Updated");
        }
    }

    public void removeUser() {

        if (selected == null) {
            UtilityController.addErrorMessage("Select a user to remove");
            return;
        }
        selected.getWebUserPerson().setRetired(true);
        selected.getWebUserPerson().setRetirer(getSessionController().getLoggedUser());
        selected.getWebUserPerson().setRetiredAt(Calendar.getInstance().getTime());
        getPersonFacade().edit(selected.getWebUserPerson());

        selected.setName(selected.getId().toString());
        selected.setRetired(true);
        selected.setRetirer(getSessionController().getLoggedUser());
        selected.setRetiredAt(Calendar.getInstance().getTime());
        //getFacade().edit(removingUser);
        getFacade().edit(selected);
        UtilityController.addErrorMessage("User Removed");
    }

    public List<WebUser> completeUser(String qry) {
        List<WebUser> a = null;
        if (qry != null) {
            a = getFacade().findBySQL("select c from WebUser c where c.retired=false and  (upper(c.webUserPerson.name) like '%" + qry.toUpperCase() + "%' or upper(c.code) like '%" + qry.toUpperCase() + "%') order by c.webUserPerson.name");
        }
        if (a == null) {
            a = new ArrayList<>();
        }
        return a;
    }

    public List<Department> getDepartments() {
        if (departments == null) {
            String sql;
            if (getInstitution() != null && getInstitution().getId() != null) {
                sql = "select d from Department d where d.retired=false and d.institution.id = " + getInstitution().getId();
                departments = getDepartmentFacade().findBySQL(sql);
            }
        }
        if (departments == null) {
            departments = new ArrayList<>();
        }
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<Institution> getInstitutions() {
        if (institutions == null) {
            String sql;
            sql = "select i from Institution i where i.retired=false order by i.name";
            institutions = getInstitutionFacade().findBySQL(sql);
        }
        return institutions;
    }

    public void setInstitutions(List<Institution> institutions) {
        this.institutions = institutions;
        departments = null; // This line is essential. Othervice departments will not be refreshed when institution is changed

    }
    boolean skip;

    public String onFlowProcess(FlowEvent event) {
        if (skip) {
            skip = false;   //reset in case user goes back
            return "confirm";
        } else {
            return event.getNewStep();
        }
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public WebUserController() {
    }

    public List<WebUser> getItems() {
        if (items == null) {
            items = getFacade().findBySQL("Select d From WebUser d where d.retired = false order by d.webUserPerson.name");
            dycryptName();
        }

        return items;
    }

    private void dycryptName() {
        List<WebUser> temp = items;

        for (int i = 0; i < temp.size(); i++) {
            WebUser w = temp.get(i);
            w.setName((w.getName()).toLowerCase());
            temp.set(i, w);
        }

        items = temp;
    }

    public void setItems(List<WebUser> items) {
        this.items = items;
    }

    public WebUser getCurrent() {
        if (current == null) {
            current = new WebUser();
            Person p = new Person();
            current.setWebUserPerson(p);
        }
        return current;
    }

    public void setCurrent(WebUser current) {

        this.current = current;
    }

    private WebUserFacade getFacade() {
        return ejbFacade;
    }

    private void recreateModel() {
        items = null;
    }

    public void prepareAdd() {
        current = new WebUser();
    }

    public void prepairAddNewUser() {
        setCurrent(new WebUser());
        Person p = new Person();
        getCurrent().setWebUserPerson(p);
        currentPrivilegeses = null;
        department = null;
        institution = null;
    }

    public void prepairAddNewUserForCompanyAdmin() {
        setCurrent(new WebUser());
        Person p = new Person();
        getCurrent().setWebUserPerson(p);
        department = null;
        institution = sessionController.getInstitution();
    }

    public SecurityController getSecurityController() {
        return securityController;
    }

    public void setSecurityController(SecurityController securityController) {
        this.securityController = securityController;
    }

    public Boolean userNameAvailable(String userName) {
        boolean available = false;
        String j;
        j = "select w from WebUser w where w.retired=false";
        List<WebUser> allUsers = getFacade().findBySQL(j);
        if (allUsers == null) {
            return false;
        }
        for (WebUser w : allUsers) {

            if (userName != null && w != null && w.getName() != null) {
                if (userName.toLowerCase().equals((w.getName()).toLowerCase())) {
                    //////// // System.out.println("Ift");
                    available = true;
                    return available;// ok. that is may be the issue. we will try with it ok
                }
            }
        }
        return available;
    }

    public String saveNewUser() {
        if (current == null) {
            UtilityController.addErrorMessage("Nothing to save");
            return "";
        }
        if (userNameAvailable(getCurrent().getName())) {
            UtilityController.addErrorMessage("User name already exists. Plese enter another user name");
            return "";
        }

        getCurrent().getWebUserPerson().setCreatedAt(new Date());
        getCurrent().getWebUserPerson().setCreater(getSessionController().getLoggedUser());
        getPersonFacade().create(getCurrent().getWebUserPerson());

        getCurrent().setInstitution(sessionController.getInstitution());

        getCurrent().setDepartment(getDepartment());

        //Save Web User
        getCurrent().setCreatedAt(new Date());
        getCurrent().setCreater(sessionController.loggedUser);
        getCurrent().setWebUserPassword(getSecurityController().hash(getCurrent().getWebUserPassword()));
        getFacade().create(getCurrent());

        UtilityController.addSuccessMessage("A New User Added");

        recreateModel();
        prepairAddNewUser();
        selectText = "";
        return BackToAdminManageUsers();
    }

    public void saveNewUserForCompanyAdmin() {
        if (current == null) {
            UtilityController.addErrorMessage("Nothing to save");
            return;
        }
        if (userNameAvailable(getCurrent().getName())) {
            UtilityController.addErrorMessage("User name already exists. Plese enter another user name");
            return;
        }
        getCurrent().getWebUserPerson().setCreatedAt(new Date());
        getCurrent().getWebUserPerson().setCreater(getSessionController().getLoggedUser());
        getPersonFacade().create(getCurrent().getWebUserPerson());

        getCurrent().setInstitution(sessionController.getInstitution());
        getCurrent().setDepartment(getDepartment());

        //Save Web User
        getCurrent().setCreatedAt(new Date());
        getCurrent().setCreater(sessionController.loggedUser);
        getCurrent().setWebUserPassword(getSecurityController().hash(getCurrent().getWebUserPassword()));
        getFacade().create(getCurrent());
    }

    public List<WebUser> getToApproveUsers() {
        String temSQL;
        temSQL = "SELECT u FROM WebUser u WHERE u.retired=false AND u.activated=false";
        return getEjbFacade().findBySQL(temSQL);
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public WebUserFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(WebUserFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public List<WebUser> getSearchItems() {
        if (searchItems == null) {
            if (selectText.equals("")) {
                searchItems = getFacade().findAll("name", true);
            } else {
                searchItems = getFacade().findAll("name", "%" + selectText + "%",
                        true);
                if (searchItems.size() > 0) {
                    current = searchItems.get(0);
                } else {
                    current = null;
                }
            }
        }
        return searchItems;
    }

    public void setSearchItems(List<WebUser> searchItems) {
        this.searchItems = searchItems;
    }

    public void delete() {
        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(new Date());
            current.setRetirer(sessionController.loggedUser);
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Deleted Successful");
        } else {
            UtilityController.addErrorMessage("Nothing To Delete");
        }
        recreateModel();
        getItems();
        current = null;
    }

    public void createTable() {

        if (selectText.trim().equals("")) {
            items = getFacade().findBySQL("select c from WebUser c where c.retired=false order by c.webUserPerson.name");
        } else {
            items = getFacade().findBySQL("select c from WebUser c where c.retired=false and upper(c.webUserPerson.name) like '%" + getSelectText().toUpperCase() + "%' order by c.webUserPerson.name");
        }
        dycryptName();
    }

    public void fillUsersForCompanyAdmin() {
        String jpql = "select c from WebUser c where c.retired=:ret order by c.webUserPerson.name";
        Map m = new HashMap();
        m.put("ret", false);
        items = getFacade().findBySQL(jpql, m);
    }

    public List<WebUser> getSelectedItems() {

        return items;
    }

    public String getSelectText() {
        return selectText;
    }

    public void setSelectText(String selectText) {
        this.selectText = selectText;
    }

    public DepartmentFacade getDepartmentFacade() {
        return departmentFacade;
    }

    public void setDepartmentFacade(DepartmentFacade departmentFacade) {
        this.departmentFacade = departmentFacade;
    }

    public InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

    public void setInstitutionFacade(InstitutionFacade institutionFacade) {
        this.institutionFacade = institutionFacade;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        departments = null;
        this.institution = institution;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Privileges[] getPrivilegeses() {
        return Privileges.values();
    }

    public PersonFacade getPersonFacade() {
        return personFacade;
    }

    public void setPersonFacade(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }

    public Privileges[] getCurrentPrivilegeses() {
        return currentPrivilegeses;
    }

    public void setCurrentPrivilegeses(Privileges[] currentPrivilegeses) {
        this.currentPrivilegeses = currentPrivilegeses;
    }

    public List<WebUser> getWebUsers() {
        return webUsers;
    }

    public void setWebUsers(List<WebUser> webUsers) {
        this.webUsers = webUsers;
    }

    public List<WebUser> getItemsToRemove() {
        return itemsToRemove;
    }

    public void setItemsToRemove(List<WebUser> itemsToRemove) {
        this.itemsToRemove = itemsToRemove;
    }

    public WebUser getSelected() {
        return selected;
    }

    public void setSelected(WebUser selected) {
        this.selected = selected;
    }

    public String toManageUser() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select a user");
            return "";
        }
        current = selected;
        return "/admin_user";
    }

    public String toManagePassword() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select a user");
            return "";
        }
        current = selected;
        return "/admin_change_password";
    }

    public String toManageSignature() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select a user");
            return "";
        }
        return "/admin_staff_signature";
    }

    public String BackToAdminManageUsers() {
        return "/admin_manage_users";
    }

    public String backToViewUsers() {
        return "/admin_view_user";
    }

    public String changeCurrentUserPassword() {
        if (getCurrent() == null) {
            UtilityController.addErrorMessage("Select a User");
            return "";
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            UtilityController.addErrorMessage("Password and Re-entered password are not maching");
            return "";
        }

        current.setWebUserPassword(getSecurityController().hash(newPassword));
        getFacade().edit(current);
        UtilityController.addSuccessMessage("Password changed");
        return "/admin_manage_users";
    }
    
    public void changePasswordForCompanyAdmin() {
        if (getCurrent() == null) {
            UtilityController.addErrorMessage("Select a User");
            return ;
        }
        current.setWebUserPassword(getSecurityController().hash(newPassword));
        getFacade().edit(current);
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }

    public void setNewPasswordConfirm(String newPasswordConfirm) {
        this.newPasswordConfirm = newPasswordConfirm;
    }

    @FacesConverter("webUs")
    public static class WebUserControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            WebUserController controller = (WebUserController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "webUserController");
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
            if (object instanceof WebUser) {
                WebUser o = (WebUser) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + WebUserController.class.getName());
            }
        }
    }
}
