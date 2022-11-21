/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.divudi.data;

/**
 *
 * @author buddh
 */
public enum WebUserRole {
    System_Administrator,
    Company_Administrator,
    Company_User,
    Agency_Administrator,
    Agency_User,
    Distributor_Administrator,
    Distributor_User,
    Customer_User,
    Customer_Administrator;

    public String getLabel() {
        switch (this) {
            case Agency_Administrator:
                return "Agency Administrator";
            case Agency_User:
                return "Agency User";
            case Company_Administrator:
                return "Company Administrator";
            case Company_User:
                return "Company User";
            case Customer_Administrator:
                return "Customer Administrator";
            case Customer_User:
                return "Customer User";
            case Distributor_Administrator:
                return "Distributor Administrator";
            case Distributor_User:
                return "Distributor User";
            case System_Administrator:
                return "System Administrator";

            default:
                throw new AssertionError();
        }
    }
}
