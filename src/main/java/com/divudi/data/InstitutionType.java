/*
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package com.divudi.data;

/**
 *
 * @author Buddhika
 */
public enum InstitutionType {
    Company,
    Manufacturer,
    Distributor,
    Customer,
    Bank,
    @Deprecated
    Lab,
    @Deprecated
    Hospital,
    @Deprecated
    Dealer,
    @Deprecated
    StoreDealor,
    @Deprecated
    Importer,
    @Deprecated
    Agency,
    @Deprecated
    branch;

    public String getLabel() {
        switch (this) {
            case Distributor:
                return "Distributor";
            case Customer:
                return "Customer";
            case Company:
                return "Company";
            case Bank:
                return "Bank";
            case Manufacturer:
                return "Manufacturer";
        }
        return this.toString();
    }

}
