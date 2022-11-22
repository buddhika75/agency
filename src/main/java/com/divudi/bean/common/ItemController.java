package com.divudi.bean.common;

import com.divudi.data.DepartmentType;
import com.divudi.data.FeeType;
import com.divudi.data.ItemType;
import com.divudi.entity.BillExpense;
import com.divudi.entity.Category;
import com.divudi.entity.Department;
import com.divudi.entity.Institution;
import com.divudi.entity.Item;
import com.divudi.entity.ItemFee;
import com.divudi.entity.Packege;
import com.divudi.entity.Service;
import com.divudi.entity.ServiceCategory;
import com.divudi.entity.ServiceSubCategory;
import com.divudi.entity.pharmacy.Amp;
import com.divudi.entity.pharmacy.Ampp;
import com.divudi.entity.pharmacy.Vmp;
import com.divudi.entity.pharmacy.Vmpp;
import com.divudi.facade.ItemFacade;
import com.divudi.facade.ItemFeeFacade;
import com.divudi.facade.util.JsfUtil;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.TemporalType;
import org.apache.commons.beanutils.BeanUtils;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, MSc, MD(Health Informatics)
 * Acting Consultant (Health Informatics)
 */
@Named
@SessionScoped
public class ItemController implements Serializable {

    /**
     * EJBs
     */
    private static final long serialVersionUID = 1L;
    @EJB
    private ItemFacade ejbFacade;
    @EJB
    private ItemFeeFacade itemFeeFacade;
    /**
     * Managed Beans
     */
    @Inject
    SessionController sessionController;
    @Inject
    ItemFeeManager itemFeeManager;
    @Inject
    DepartmentController departmentController;
    
    /**
     * Properties
     */
    private Item current;
    private Item sampleComponent;
    private List<Item> items = null;
    private List<Item> investigationsAndServices = null;
    private List<Item> itemlist;
    List<Item> allItems;
    List<ItemFee> allItemFees;
    List<Item> selectedList;
    List<ItemFee> selectedItemFeeList;
    private Institution instituion;
    Department department;
    FeeType feeType;
    List<Department> departments;
    private List<Item> machineTests;
    private List<Item> investigationSampleComponents;

    public List<Department> getDepartments() {
        departments = departmentController.getInstitutionDepatrments(instituion);
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

   
    public List<Item> completeDealorItem(String query) {
        List<Item> suggestions;
        String sql;
        HashMap hm = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c.item from ItemsDistributors c"
                    + " where c.retired=false "
                    + " and c.item.retired=false "
                    + " and c.institution=:ins and (upper(c.item.name) like :q or "
                    + " upper(c.item.barcode) like :q or upper(c.item.code) like :q )order by c.item.name";
            hm.put("ins", getInstituion());
            hm.put("q", "%" + query + "%");
            //////// // System.out.println(sql);
            suggestions = getFacade().findBySQL(sql, hm, 20);
        }
        return suggestions;

    }

    public List<Item> getDealorItem() {
        List<Item> suggestions;
        String sql;
        HashMap hm = new HashMap();

        sql = "select c.item from ItemsDistributors c where c.retired=false "
                + " and c.institution=:ins "
                + " order by c.item.name";
        hm.put("ins", getInstituion());

        //////// // System.out.println(sql);
        suggestions = getFacade().findBySQL(sql, hm);

        return suggestions;

    }

    public List<Item> completeItem(String query, Class[] itemClasses, DepartmentType[] departmentTypes, int count) {
        String sql;
        List<Item> lst;
        HashMap tmpMap = new HashMap();
        if (query == null) {
            lst = new ArrayList<>();
        } else {
            sql = "select c "
                    + " from Item c "
                    + " where c.retired=false ";

            if (departmentTypes != null) {
                sql += " and c.departmentType in :deps ";
                tmpMap.put("deps", Arrays.asList(departmentTypes));
            }

            if (itemClasses != null) {
                sql += " and type(c) in :types ";
                tmpMap.put("types", Arrays.asList(itemClasses));
            }

            sql += " and (upper(c.name) like :q or upper(c.code) like :q or upper(c.barcode) like :q  ) ";
            tmpMap.put("q", "%" + query.toUpperCase() + "%");

            sql += " order by c.name";

            if (count != 0) {
                lst = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, count);
            } else {
                lst = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP);
            }
        }
        return lst;
    }

    public List<Item> completeItem(String query) {
        return completeItem(query, null, null, 20);
//        List<Item> suggestions;
//        String sql;
//        HashMap hm = new HashMap();
//        if (query == null) {
//            suggestions = new ArrayList<>();
//        } else {
//            sql = "select c from Item c "
//                    + " where c.retired=false"
//                    + "  and (upper(c.name) like :q"
//                    + "  or upper(c.barcode) like :q"
//                    + "  or upper(c.code) like :q )"
//                    + " order by c.name";
//            hm.put("q", "%" + query.toUpperCase() + "%");
////////// // System.out.println(sql);
//            suggestions = getFacade().findBySQL(sql, hm, 20);
//        }
//        return suggestions;
//
    }

    List<Item> itemList;

    List<Item> suggestions;

    public List<Item> completeMedicine(String query) {
        DepartmentType[] dts = new DepartmentType[]{DepartmentType.Pharmacy, null};
        Class[] classes = new Class[]{Vmp.class, Amp.class, Vmp.class, Amp.class, Vmpp.class, Ampp.class};
        return completeItem(query, classes, dts, 0);
    }

    public List<Item> completeItem(String query, Class[] itemClasses, DepartmentType[] departmentTypes) {
        return completeItem(query, itemClasses, departmentTypes, 0);
    }

    public List<Item> completeAmpItem(String query) {
//        DepartmentType[] dts = new DepartmentType[]{DepartmentType.Pharmacy, null};
//        Class[] classes = new Class[]{Amp.class};
//        return completeItem(query, classes, dts, 30);
//        
        String sql;
        HashMap tmpMap = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {

            sql = "select c from Item c where c.retired=false "
                    + " and (type(c)= :amp) and "
                    + " ( c.departmentType is null or c.departmentType!=:dep ) "
                    + " and (upper(c.name) like :str or upper(c.code) like :str or"
                    + " upper(c.barcode) like :str ) order by c.name";
            //////// // System.out.println(sql);
            tmpMap.put("dep", DepartmentType.Store);
            tmpMap.put("amp", Amp.class);
            tmpMap.put("str", "%" + query.toUpperCase() + "%");
            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 30);
        }
        return suggestions;

    }

    public List<Item> completeAmpItemAll(String query) {
//        DepartmentType[] dts = new DepartmentType[]{DepartmentType.Pharmacy, null};
//        Class[] classes = new Class[]{Amp.class};
//        return completeItem(query, classes, dts, 0);
        String sql;
        HashMap tmpMap = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {

            sql = "select c from Item c where "
                    + " (type(c)= :amp) and "
                    + " ( c.departmentType is null or c.departmentType!=:dep ) "
                    + " and (upper(c.name) like :str or upper(c.code) like :str or"
                    + " upper(c.barcode) like :str ) order by c.name";
            //////// // System.out.println(sql);
            tmpMap.put("dep", DepartmentType.Store);
            tmpMap.put("amp", Amp.class);
            tmpMap.put("str", "%" + query.toUpperCase() + "%");
            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 30);
        }
        return suggestions;

    }

    public List<Item> completeStoreItem(String query) {
        DepartmentType[] dts = new DepartmentType[]{DepartmentType.Store, DepartmentType.Inventry};
        Class[] classes = new Class[]{Amp.class};
        return completeItem(query, classes, dts, 0);
//        String sql;
//        HashMap tmpMap = new HashMap();
//        if (query == null) {
//            suggestions = new ArrayList<>();
//        } else {
//
//            sql = "select c from Item c "
//                    + "where c.retired=false and "
//                    + "(type(c)= :amp) "
//                    + "and (c.departmentType=:dep or c.departmentType=:inven )"
//                    + "and (upper(c.name) like :str or "
//                    + "upper(c.code) like :str or "
//                    + "upper(c.barcode) like :str) "
//                    + "order by c.name";
//            //////// // System.out.println(sql);
//            tmpMap.put("amp", Amp.class);
//            tmpMap.put("dep", DepartmentType.Store);
//            tmpMap.put("inven", DepartmentType.Inventry);
//            tmpMap.put("str", "%" + query.toUpperCase() + "%");
//            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 30);
//        }
//        return suggestions;
//
    }

    public List<Item> completeStoreInventryItem(String query) {
        DepartmentType[] dts = new DepartmentType[]{DepartmentType.Inventry};
        Class[] classes = new Class[]{Amp.class};
        return completeItem(query, classes, dts, 0);
//        String sql;
//        HashMap tmpMap = new HashMap();
//        if (query == null) {
//            suggestions = new ArrayList<>();
//        } else {
//
//            sql = "select c from Item c "
//                    + "where c.retired=false and "
//                    + "(type(c)= :amp) "
//                    + "and c.departmentType=:dep "
//                    + "and (upper(c.name) like :str or "
//                    + "upper(c.code) like :str or "
//                    + "upper(c.barcode) like :str) "
//                    + "order by c.name";
//            //////// // System.out.println(sql);
//            tmpMap.put("amp", Amp.class);
//            tmpMap.put("dep", DepartmentType.Inventry);
//            tmpMap.put("str", "%" + query.toUpperCase() + "%");
//            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 30);
//        }
//        return suggestions;

    }

    public List<Item> completeStoreItemOnly(String query) {
        DepartmentType[] dts = new DepartmentType[]{DepartmentType.Store};
        Class[] classes = new Class[]{Amp.class};
        return completeItem(query, classes, dts, 0);
//        String sql;
//        HashMap tmpMap = new HashMap();
//        if (query == null) {
//            suggestions = new ArrayList<>();
//        } else {
//
//            sql = "select c from Item c "
//                    + "where c.retired=false and "
//                    + "(type(c)= :amp) "
//                    + "and c.departmentType=:dep "
//                    + "and (upper(c.name) like :str or "
//                    + "upper(c.code) like :str or "
//                    + "upper(c.barcode) like :str) "
//                    + "order by c.name";
//            //////// // System.out.println(sql);
//            tmpMap.put("amp", Amp.class);
//            tmpMap.put("dep", DepartmentType.Store);
//            tmpMap.put("str", "%" + query.toUpperCase() + "%");
//            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 30);
//        }
//        return suggestions;
//
    }

    public List<Item> completeExpenseItem(String query) {
        Class[] classes = new Class[]{BillExpense.class};
        return completeItem(query, classes, null, 0);
//        String sql;
//        HashMap tmpMap = new HashMap();
//        if (query == null) {
//            suggestions = new ArrayList<>();
//        } else {
//            sql = "select c from Item c "
//                    + "where c.retired=false and "
//                    + "(type(c)= :amp) "
//                    + "and (upper(c.name) like :str or "
//                    + "upper(c.code) like :str or "
//                    + "upper(c.barcode) like :str) "
//                    + "order by c.name";
//            //////// // System.out.println(sql);
//            tmpMap.put("amp", BillExpense.class);
//            tmpMap.put("str", "%" + query.toUpperCase() + "%");
//            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 30);
//        }
//        return suggestions;
    }

    public List<Item> fetchStoreItem() {
        List<Item> suggestions;
        String sql;
        HashMap tmpMap = new HashMap();

        sql = "select c from Item c"
                + "  where c.retired=false and "
                + " (type(c)= :amp) "
                + " and c.departmentType=:dep "
                + " order by c.name";
        //////// // System.out.println(sql);
        tmpMap.put("amp", Amp.class);
        tmpMap.put("dep", DepartmentType.Store);

        suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP);

        return suggestions;

    }

    public List<Item> completeAmpAndAmppItem(String query) {
        List<Item> suggestions;
        String sql;
        HashMap tmpMap = new HashMap();
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            if (query.length() > 4) {
                sql = "select c from Item c where c.retired=false and (type(c)= :amp or type(c)=:ampp ) and (upper(c.name) like '%" + query.toUpperCase() + "%' or upper(c.code) like '%" + query.toUpperCase() + "%' or upper(c.barcode) like '%" + query.toUpperCase() + "%') order by c.name";
            } else {
                sql = "select c from Item c where c.retired=false and (type(c)= :amp or type(c)=:ampp ) and (upper(c.name) like '%" + query.toUpperCase() + "%' or upper(c.code) like '%" + query.toUpperCase() + "%') order by c.name";
            }

//////// // System.out.println(sql);
            tmpMap.put("amp", Amp.class);
            tmpMap.put("ampp", Ampp.class);
            suggestions = getFacade().findBySQL(sql, tmpMap, TemporalType.TIMESTAMP, 30);
        }
        return suggestions;

    }

    public List<Item> completePackage(String query) {
        List<Item> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<>();
        } else {
            sql = "select c from Item c where c.retired=false"
                    + " and (c.inactive=false or c.inactive is null) "
                    + "and type(c)=Packege "
                    + "and upper(c.name) like '%" + query.toUpperCase() + "%' order by c.name";
            //////// // System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        return suggestions;

    }

    public List<Item> completeService(String query) {
        List<Item> suggestions;
        String sql;
        HashMap hm = new HashMap();

        sql = "select c from Item c where c.retired=false and type(c)=:cls"
                + " and upper(c.name) like :q order by c.name";

        hm.put("cls", Service.class);
        hm.put("q", "%" + query.toUpperCase() + "%");
        suggestions = getFacade().findBySQL(sql, hm, 20);

        return suggestions;

    }

    Category category;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    private List<Item> fetchInwardItems(String query, Department department) {
        HashMap m = new HashMap();
        String sql;
        sql = "select c from Item c "
                + " where c.retired=false "
                + " and type(c)!=:pac "
                + " and (type(c)=:ser "
                + " or type(c)=:inward "
                + " or type(c)=:inv) "
                + " and (c.inactive=false or c.inactive is null) "
                + " and upper(c.name) like :q";
        if (department != null) {
            sql += " and c.department=:dep ";
            m.put("dep", department);
        }
        sql += " order by c.name";
        m.put("pac", Packege.class);
        m.put("ser", Service.class);
        m.put("q", "%" + query.toUpperCase() + "%");

        return getFacade().findBySQL(sql, m, 20);

    }

    private List<Item> fetchInwardItems(String query, Category cat, Department department) {
        HashMap m = new HashMap();
        String sql;
        sql = "select c from Item c "
                + " where c.retired=false "
                + " and c.category=:ct"
                + " and type(c)!=:pac "
                + " and (type(c)=:ser "
                + " or type(c)=:inward "
                + " or type(c)=:inv) "
                + " and (c.inactive=false or c.inactive is null) "
                + " and upper(c.name) like :q";
        if (department != null) {
            sql += " and c.department=:dep ";
            m.put("dep", department);
        }
        sql += " order by c.name";
        m.put("ct", cat);
        m.put("pac", Packege.class);
        m.put("ser", Service.class);
        m.put("q", "%" + query.toUpperCase() + "%");
        return getFacade().findBySQL(sql, m, 20);

    }


    public void makeItemsAsActiveOrInactiveByRetiredStatus() {
        String j = "select i from Item i";
        List<Item> tis = getFacade().findBySQL(j);
        for (Item i : tis) {
            if (i.isRetired()) {
                i.setInactive(true);
            } else {
                i.setInactive(false);
            }
            getFacade().edit(i);
        }
    }
    
     public void toggleItemIctiveInactiveState() {
        String j = "select i from Item i";
        List<Item> tis = getFacade().findBySQL(j);
        for (Item i : tis) {
            if (i.isInactive()) {
                i.setInactive(false);
            } else {
                i.setInactive(true);
            }
            getFacade().edit(i);
        }
    }

    public List<Item> completeOpdItemsByNamesAndCodeInstitutionSpecificOrNotSpecific(String query, boolean spcific) {
        if (query == null || query.trim().equals("")) {
            return new ArrayList<>();
        }
        List<Item> mySuggestions;
        HashMap m = new HashMap();
        String sql;

        sql = "select c from Item c "
                + " where c.retired<>true "
                + " and (c.inactive<>true) "
                + " and type(c)!=:pac "
                + " and type(c)!=:inw "
                + " and (type(c)=:ser "
                + " or type(c)=:inv)  "
                + " and (upper(c.name) like :q or upper(c.fullName) like :q or "
                + " upper(c.code) like :q or upper(c.printName) like :q ) ";
        if (spcific) {
            sql += " and c.institution=:ins";
            m.put("ins", getSessionController().getInstitution());
        }

        sql += " order by c.name";
        m.put("pac", Packege.class);
        m.put("ser", Service.class);
        m.put("q", "%" + query.toUpperCase() + "%");
        mySuggestions = getFacade().findBySQL(sql, m, 20);
        return mySuggestions;
    }

    public List<Item> completeOpdItems(String query) {
        List<Item> mySuggestions;
        HashMap m = new HashMap();
        String sql;
        if (query == null) {
            mySuggestions = new ArrayList<>();
        } else {
            sql = "select c from Item c "
                    + " where c.retired=false "
                    + " and type(c)!=:pac "
                    + " and type(c)!=:inw "
                    + " and (type(c)=:ser "
                    + " or type(c)=:inv)  "
                    + " and upper(c.name) like :q"
                    + " order by c.name";
            m.put("pac", Packege.class);
            m.put("ser", Service.class);
            m.put("q", "%" + query.toUpperCase() + "%");
            //    //////// // System.out.println(sql);
            mySuggestions = getFacade().findBySQL(sql, m, 20);
        }
        return mySuggestions;
    }

    public List<Item> completeItemWithoutPackOwn(String query) {
        List<Item> suggestions;
        String sql;
        if (query == null) {
            suggestions = new ArrayList<Item>();
        } else {
            sql = "select c from Item c where c.institution.id = " + getSessionController().getInstitution().getId() + " and c.retired=false and type(c)!=Packege and type(c)!=TimedItem and upper(c.name) like '%" + query.toUpperCase() + "%' order by c.name";
            //////// // System.out.println(sql);
            suggestions = getFacade().findBySQL(sql);
        }
        return suggestions;
    }

    public void makeSelectedAsMasterItems() {
        for (Item i : selectedList) {
            ////// // System.out.println("i = " + i.getInstitution());
            if (i.getInstitution() != null) {
                ////// // System.out.println("i = " + i.getInstitution().getName());
                i.setInstitution(null);
                getFacade().edit(i);
            }
        }
    }

    public List<Item> fetchOPDItemList(boolean ins) {
        List<Item> items = new ArrayList<>();
        HashMap m = new HashMap();
        String sql;

        sql = "select c from Item c "
                + " where c.retired=false "
                + " and type(c)!=:pac "
                + " and type(c)!=:inw "
                + " and (type(c)=:ser "
                + " or type(c)=:inv)  ";

        if (ins) {
            sql += " and c.institution is null ";
        }

        sql += " order by c.name";

        m.put("pac", Packege.class);
        m.put("ser", Service.class);
        ////// // System.out.println(sql);
        items = getFacade().findBySQL(sql, m);
        return items;
    }

    public List<ItemFee> fetchOPDItemFeeList(boolean ins, FeeType ftype) {
        List<ItemFee> itemFees = new ArrayList<>();
        HashMap m = new HashMap();
        String sql;

        sql = "select c from ItemFee c "
                + " where c.retired=false "
                + " and type(c.item)!=:pac "
                + " and type(c.item)!=:inw "
                + " and (type(c.item)=:ser "
                + " or type(c.item)=:inv)  ";

        if (ftype != null) {
            sql += " and c.feeType=:fee ";
            m.put("fee", ftype);
        }

        if (ins) {
            sql += " and c.institution is null ";
        }

        sql += " order by c.name";

        m.put("pac", Packege.class);
        m.put("ser", Service.class);
        ////// // System.out.println(sql);
        itemFees = getItemFeeFacade().findBySQL(sql, m);
        return itemFees;
    }

    public void createMasterItemsList() {
        allItems = new ArrayList<>();
        allItems = fetchOPDItemList(false);
    }

    public void createAllItemsList() {
        allItems = new ArrayList<>();
        allItems = fetchOPDItemList(true);
    }

    public void createAllItemsFeeList() {
        allItemFees = new ArrayList<>();
        allItemFees = fetchOPDItemFeeList(false, feeType);
    }

    public void updateSelectedOPDItemList() {

    }

    /**
     *
     */
    public ItemController() {
    }

    /**
     * Prepare to add new Collecting Centre
     *
     * @return
     */
    public void prepareAdd() {
        current = new Item();
    }

    /**
     *
     * @return
     */
    public ItemFacade getEjbFacade() {
        return ejbFacade;
    }

    /**
     *
     * @param ejbFacade
     */
    public void setEjbFacade(ItemFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    /**
     *
     * @return
     */
    public SessionController getSessionController() {
        return sessionController;
    }

    /**
     *
     * @param sessionController
     */
    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    /**
     * Return the current item
     *
     * @return
     */
    public Item getCurrent() {
        if (current == null) {
            current = new Item();
        }
        return current;
    }

    /**
     * Set the current item
     *
     * @param current
     */
    public void setCurrent(Item current) {
        this.current = current;
    }

    private ItemFacade getFacade() {
        return ejbFacade;
    }

    /**
     *
     * @return
     */
    public List<Item> getItems() {
        return items;
    }

    
   
    public List<Item> getItems(Category category) {
        String temSql;
        HashMap h = new HashMap();
        temSql = "SELECT i FROM Item i where i.category=:cat and i.retired=false order by i.name";
        h.put("cat", category);
        return getFacade().findBySQL(temSql, h);
    }

    /**
     *
     * Set all Items to null
     *
     */
    private void recreateModel() {
        items = null;
    }

    /**
     *
     */
    public void saveSelected() {
        saveSelected(getCurrent());
        JsfUtil.addSuccessMessage("Saved");
        recreateModel();
        getItems();
    }

    public void saveSelected(Item item) {
        if (item.getId() != null && item.getId() > 0) {
            getFacade().edit(item);
        } else {
            item.setCreatedAt(new Date());
            item.setCreater(getSessionController().getLoggedUser());
            getFacade().create(item);
        }
    }

    /**
     *
     * Delete the current Item
     *
     */
    public void delete() {

        if (current != null) {
            current.setRetired(true);
            current.setRetiredAt(new Date());
            current.setRetirer(getSessionController().getLoggedUser());
            getFacade().edit(current);
            UtilityController.addSuccessMessage("Deleted Successfully");
        } else {
            UtilityController.addSuccessMessage("Nothing to Delete");
        }
        recreateModel();
        getItems();
        current = null;
        getCurrent();
    }

    public Institution getInstituion() {
        if (instituion == null) {
            instituion = getSessionController().getInstitution();
        }
        return instituion;
    }

    public void setInstituion(Institution instituion) {
        this.instituion = instituion;
    }

    public FeeType getFeeType() {
        return feeType;
    }

    public void setFeeType(FeeType feeType) {
        this.feeType = feeType;
    }

    public List<Item> getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(List<Item> selectedList) {
        this.selectedList = selectedList;
    }

    public List<Item> getAllItems() {
        return allItems;
    }

    public void setAllItems(List<Item> allItems) {
        this.allItems = allItems;
    }

    public List<ItemFee> getAllItemFees() {
        return allItemFees;
    }

    public void setAllItemFees(List<ItemFee> allItemFees) {
        this.allItemFees = allItemFees;
    }

    public List<ItemFee> getSelectedItemFeeList() {
        return selectedItemFeeList;
    }

    public void setSelectedItemFeeList(List<ItemFee> selectedItemFeeList) {
        this.selectedItemFeeList = selectedItemFeeList;
    }

    public Department getDepartment() {
        if (department == null) {
            department = getSessionController().getDepartment();
        }
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    public List<Item> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Item> suggestions) {
        this.suggestions = suggestions;
    }

    public ItemFeeFacade getItemFeeFacade() {
        return itemFeeFacade;
    }

    public void setItemFeeFacade(ItemFeeFacade itemFeeFacade) {
        this.itemFeeFacade = itemFeeFacade;
    }

    public ItemFeeManager getItemFeeManager() {
        return itemFeeManager;
    }

    public void setItemFeeManager(ItemFeeManager itemFeeManager) {
        this.itemFeeManager = itemFeeManager;
    }

    public List<Item> getItemlist() {
        return itemlist;
    }

    public void setItemlist(List<Item> itemlist) {
        this.itemlist = itemlist;
    }



    public void setInvestigationsAndServices(List<Item> investigationsAndServices) {
        this.investigationsAndServices = investigationsAndServices;
    }

   

    public List<Item> getMachineTests() {
        return machineTests;
    }

    public void setMachineTests(List<Item> machineTests) {
        this.machineTests = machineTests;
    }

    public List<Item> getInvestigationSampleComponents() {
        return investigationSampleComponents;
    }

    public void setInvestigationSampleComponents(List<Item> investigationSampleComponents) {
        this.investigationSampleComponents = investigationSampleComponents;
    }

    public Item getSampleComponent() {
        return sampleComponent;
    }

    public void setSampleComponent(Item sampleComponent) {
        this.sampleComponent = sampleComponent;
    }

    @FacesConverter(forClass = Item.class)
    public static class ItemControllerConverter implements Converter {

        /**
         *
         * @param facesContext
         * @param component
         * @param value
         * @return
         */
        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ItemController controller = (ItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "itemController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key = 0l;
            try {
                key = Long.valueOf(value);
            } catch (Exception e) {

            }

            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        /**
         *
         * @param facesContext
         * @param component
         * @param object
         * @return
         */
        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Item) {
                Item o = (Item) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + ItemController.class.getName());
            }
        }
    }

    /**
     *
     */
    @FacesConverter("itemcon")
    public static class ItemConverter implements Converter {

        /**
         *
         * @param facesContext
         * @param component
         * @param value
         * @return
         */
        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ItemController controller = (ItemController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "itemController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key = 0l;
            try {
                key = Long.valueOf(value);
            } catch (Exception e) {

            }

            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        /**
         *
         * @param facesContext
         * @param component
         * @param object
         * @return
         */
        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Item) {
                Item o = (Item) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + ItemController.class.getName());
            }
        }
    }
}
