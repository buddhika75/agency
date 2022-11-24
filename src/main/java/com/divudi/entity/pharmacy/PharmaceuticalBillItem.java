/*
* Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.entity.pharmacy;

import com.divudi.bean.common.TimeUtils;
import com.divudi.entity.BillItem;
import com.divudi.entity.Category;
import com.divudi.entity.Institution;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;

/**
 *
 * @author Buddhika
 */
@Entity
public class PharmaceuticalBillItem implements Serializable {

    @OneToOne(mappedBy = "pbItem")
    private StockHistory stockHistory;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    BillItem billItem;
    @Temporal(javax.persistence.TemporalType.DATE)
    Date doe;
    @ManyToOne
    ItemBatch itemBatch;
    private String stringValue;
    double qty;
    double freeQty;
    double purchaseRate;
    private double lastPurchaseRate;
    double retailRate;
    double wholesaleRate;
    @ManyToOne
    Stock stock;
    @ManyToOne
    private Stock staffStock;

    @ManyToOne
    Category make;
    String model;
    String code;
    String serialNo;
    @Lob
    String description;
    String barcode;
    String registrationNo;
    String chassisNo;
    String engineNo;
    String colour;
    int numberOfAccessories;
    String warrentyCertificateNumber;
    long warrentyDuration;
    double totalAcquicitionCost;
    double deprecitionRate;
    @Lob
    String otherNotes;

    @ManyToOne
    Institution manufacturer;

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getWarrentyCertificateNumber() {
        return warrentyCertificateNumber;
    }

    public void setWarrentyCertificateNumber(String warrentyCertificateNumber) {
        this.warrentyCertificateNumber = warrentyCertificateNumber;
    }

    public long getWarrentyDuration() {
        return warrentyDuration;
    }

    public String getWarrentyDurationInYearsAndDays() {
        return TimeUtils.millisToYearsAndDates(warrentyDuration);
    }

    public void setWarrentyDuration(long warrentyDuration) {
        this.warrentyDuration = warrentyDuration;
    }

    public double getTotalAcquicitionCost() {
        return totalAcquicitionCost;
    }

    public void setTotalAcquicitionCost(double totalAcquicitionCost) {
        this.totalAcquicitionCost = totalAcquicitionCost;
    }

    public double getDeprecitionRate() {
        return deprecitionRate;
    }

    public void setDeprecitionRate(double deprecitionRate) {
        this.deprecitionRate = deprecitionRate;
    }

    public String getOtherNotes() {
        return otherNotes;
    }

    public void setOtherNotes(String otherNotes) {
        this.otherNotes = otherNotes;
    }

    public Institution getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Institution manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getChassisNo() {
        return chassisNo;
    }

    public void setChassisNo(String chassisNo) {
        this.chassisNo = chassisNo;
    }

    public String getEngineNo() {
        return engineNo;
    }

    public void setEngineNo(String engineNo) {
        this.engineNo = engineNo;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public int getNumberOfAccessories() {
        return numberOfAccessories;
    }

    public void setNumberOfAccessories(int numberOfAccessories) {
        this.numberOfAccessories = numberOfAccessories;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void copy(PharmaceuticalBillItem ph) {
        if (ph == null) {
            return;
        }
        qty = ph.qty;
        freeQty = ph.freeQty;
        purchaseRate = ph.purchaseRate;
        doe = ph.getDoe();
        stringValue = ph.getStringValue();
        itemBatch = ph.getItemBatch();
        retailRate = ph.getRetailRate();
        stock = ph.getStock();
        staffStock = ph.getStaffStock();
        stringValue = ph.getStringValue();
        //  remainingQty=ph.getRemainingQty();

        make = ph.getMake();
        model = ph.getModel();
        code = ph.getCode();
        description = ph.getDescription();
        barcode = ph.getBarcode();
        serialNo = ph.getSerialNo();
        registrationNo = ph.getRegistrationNo();
        chassisNo = ph.getChassisNo();
        engineNo = ph.getEngineNo();
        colour = ph.getColour();
        warrentyCertificateNumber = ph.getWarrentyCertificateNumber();
        warrentyDuration = ph.getWarrentyDuration();
        deprecitionRate = ph.getDeprecitionRate();
        manufacturer = ph.getManufacturer();
        otherNotes = ph.getOtherNotes();

    }

    public void invertValue(PharmaceuticalBillItem ph) {
        if (ph == null) {
            return;
        }
        qty = 0 - ph.qty;
        // //System.err.println("QTY "+qty);
        freeQty = 0 - ph.freeQty;
        //  purchaseRate=0-ph.purchaseRate;
        //  lastPurchaseRate=0-ph.lastPurchaseRate;
        // retailRate=0-ph.retailRate;
        //  wholesaleRate=0-ph.wholesaleRate;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public StockHistory getStockHistory() {
        return stockHistory;
    }

    public void setStockHistory(StockHistory stockHistory) {
        this.stockHistory = stockHistory;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BillItem getBillItem() {
        return billItem;
    }

    public void setBillItem(BillItem billItem) {
        this.billItem = billItem;
    }

    public Date getDoe() {
        return doe;
    }

    public void setDoe(Date doe) {
        this.doe = doe;
    }

    public ItemBatch getItemBatch() {
        return itemBatch;
    }

    public void setItemBatch(ItemBatch itemBatch) {
        this.itemBatch = itemBatch;
        if (itemBatch != null) {
            retailRate = itemBatch.getRetailsaleRate();
            purchaseRate = itemBatch.getPurcahseRate();

        }
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public double getQtyInUnit() {
        return qty;
    }

    public void setQtyInUnit(double qty) {
        this.qty = qty;
    }

    public double getFreeQtyInUnit() {
        return freeQty;
    }

    public void setFreeQtyInUnit(double freeQty) {
        this.freeQty = freeQty;
    }

    public double getFreeQty() {
        return freeQty;
    }

    public void setFreeQty(double freeQty) {
        this.freeQty = freeQty;
    }

    public double getPurchaseRate() {
        return purchaseRate;
    }

    public double getPurchaseRateInUnit() {
        return purchaseRate;
    }

    public void setPurchaseRateInUnit(double purchaseRate) {
        this.purchaseRate = purchaseRate;
    }

    public void setPurchaseRate(double purchaseRate) {
        this.purchaseRate = purchaseRate;
    }

    public double getRetailRateInUnit() {
        return retailRate;
    }

    public void setRetailRateInUnit(double retailRate) {
        this.retailRate = retailRate;
    }

    public double getRetailRate() {
        return retailRate;
    }

    public void setRetailRate(double retailRate) {
        this.retailRate = retailRate;
    }

    public double getWholesaleRate() {
        return wholesaleRate;
    }

    public void setWholesaleRate(double wholesaleRate) {
        this.wholesaleRate = wholesaleRate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof PharmaceuticalBillItem)) {
            return false;
        }
        PharmaceuticalBillItem other = (PharmaceuticalBillItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.pharmacy.PharmaceuticalBillItem[ id=" + id + " ]";
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public double getLastPurchaseRateInUnit() {

        return lastPurchaseRate;

    }

    public void setLastPurchaseRateInUnit(double lastPurchaseRate) {

        this.lastPurchaseRate = lastPurchaseRate;

    }

    public double getLastPurchaseRate() {
        return lastPurchaseRate;
    }

    public void setLastPurchaseRate(double lastPurchaseRate) {
        this.lastPurchaseRate = lastPurchaseRate;
    }

    public Stock getStaffStock() {
        return staffStock;
    }

    public void setStaffStock(Stock staffStock) {
        this.staffStock = staffStock;
    }

    public Category getMake() {
        return make;
    }

    public void setMake(Category make) {
        this.make = make;
    }

}
